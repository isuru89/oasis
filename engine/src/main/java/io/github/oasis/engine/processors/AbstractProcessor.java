/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.github.oasis.engine.processors;

import io.github.oasis.engine.model.EventFilter;
import io.github.oasis.engine.rules.AbstractRule;
import io.github.oasis.engine.rules.signals.Signal;
import io.github.oasis.model.Event;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.List;
import java.util.function.Consumer;

/**
 * @author Isuru Weerarathna
 */
public abstract class AbstractProcessor<R extends AbstractRule, S extends Signal> implements Consumer<Event> {

    protected final JedisPool pool;
    protected final R rule;

    public AbstractProcessor(JedisPool pool, R rule) {
        this.pool = pool;
        this.rule = rule;
    }

    public boolean isDenied(Event event) {
        return !isMatchEvent(event, rule) || unableToProcess(event, rule);
    }

    @Override
    public void accept(Event event) {
        if (isDenied(event)) {
            return;
        }

        try (Jedis jedis = pool.getResource()) {
            List<S> signals = process(event, rule, jedis);
            if (signals != null) {
                signals.forEach(signal -> {
                    beforeEmit(signal, event, rule, jedis);
                    rule.getCollector().accept(signal);
                });
            }
            afterEmitAll(signals, event, rule, jedis);
        }
    }

    /**
     * Calls before sending each and every signal to the collector.
     *
     * @param signal signal to be sent.
     * @param event event triggered this.
     * @param rule rule reference.
     * @param jedis jedis context.
     */
    protected abstract void beforeEmit(S signal, Event event, R rule, Jedis jedis);

    /**
     * Calls after all of signals are sent to the collector.
     *
     * @param signals list of signals generated.
     * @param event event caused to trigger.
     * @param rule rule reference.
     * @param jedis jedis context.
     */
    protected void afterEmitAll(List<S> signals, Event event, R rule, Jedis jedis) {
        // do nothing.
    }

    /**
     * Process the given event against given rule and returns signals.
     *
     * @param event event to process.
     * @param rule rule reference.
     * @param jedis jedis context.
     * @return list of signals to notify.
     */
    public abstract List<S> process(Event event, R rule, Jedis jedis);

    private boolean isMatchEvent(Event event, AbstractRule rule) {
        return rule.getEventTypeMatcher().matches(event.getEventType());
    }

    private boolean unableToProcess(Event event, AbstractRule rule) {
        EventFilter condition = rule.getCondition();
        if (condition == null) {
            return false;
        }
        return !condition.matches(event, rule);
    }

}
