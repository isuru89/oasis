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
import io.github.oasis.engine.model.RuleContext;
import io.github.oasis.engine.rules.AbstractRule;
import io.github.oasis.engine.rules.signals.Signal;
import io.github.oasis.engine.storage.Db;
import io.github.oasis.engine.storage.DbContext;
import io.github.oasis.model.Event;

import java.io.Serializable;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author Isuru Weerarathna
 */
public abstract class AbstractProcessor<R extends AbstractRule, S extends Signal> implements Consumer<Event>, Serializable {

    protected final Db dbPool;
    protected final R rule;
    private final RuleContext<R> ruleContext;

    public AbstractProcessor(Db dbPool, RuleContext<R> ruleCtx) {
        this.dbPool = dbPool;
        this.ruleContext = ruleCtx;
        this.rule = ruleCtx.getRule();
    }

    public boolean isDenied(Event event) {
        return !isMatchEvent(event, rule) || unableToProcess(event, rule);
    }

    @Override
    public void accept(Event event) {
        if (isDenied(event)) {
            return;
        }

        try (DbContext db = dbPool.createContext()) {
            List<S> signals = process(event, rule, db);
            if (signals != null) {
                signals.forEach(signal -> {
                    beforeEmit(signal, event, rule, db);
                    ruleContext.getCollector().accept(signal);
                });
            }
            afterEmitAll(signals, event, rule, db);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Calls before sending each and every signal to the collector.
     *
     * @param signal signal to be sent.
     * @param event event triggered this.
     * @param rule rule reference.
     * @param db db context.
     */
    protected abstract void beforeEmit(S signal, Event event, R rule, DbContext db);

    /**
     * Calls after all of signals are sent to the collector.
     *
     * @param signals list of signals generated.
     * @param event event caused to trigger.
     * @param rule rule reference.
     * @param db db context.
     */
    protected void afterEmitAll(List<S> signals, Event event, R rule, DbContext db) {
        // do nothing.
    }

    /**
     * Process the given event against given rule and returns signals.
     *
     * @param event event to process.
     * @param rule rule reference.
     * @param db db context.
     * @return list of signals to notify.
     */
    public abstract List<S> process(Event event, R rule, DbContext db);

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
