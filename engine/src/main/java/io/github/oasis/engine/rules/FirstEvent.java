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

package io.github.oasis.engine.rules;

import io.github.oasis.engine.model.ID;
import io.github.oasis.engine.rules.signals.BadgeSignal;
import io.github.oasis.model.Event;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Collections;
import java.util.List;

/**
 * @author Isuru Weerarathna
 */
public class FirstEvent implements BadgeHandler {

    private final JedisPool pool;
    private final FirstEventRule options;

    public FirstEvent(JedisPool pool, FirstEventRule firstEventOptions) {
        this.pool = pool;
        this.options = firstEventOptions;
    }

    @Override
    public List<BadgeSignal> handle(Event event) {
        String key = ID.getUserFirstEventsKey(event.getGameId(), event.getUser());
        try (Jedis jedis = pool.getResource()) {
            if (isConditionSatisfied(event, options)) {
                long ts = event.getTimestamp();
                String id = event.getExternalId();
                String subKey = options.getEventName();
                String value = ts + ":" + id + ":" + System.currentTimeMillis();
                if (isFirstOne(jedis.hsetnx(key, subKey, value))) {
                    return Collections.singletonList(new BadgeSignal(options.getId(),
                            1,
                            ts, ts,
                            id, id));
                }
            }
        }
        return null;
    }

    private boolean isConditionSatisfied(Event event, FirstEventRule rule) {
        return eventMatches(event.getEventType(), rule.getEventName())
                && (rule.getCondition() == null || rule.getCondition().test(event));
    }

    private boolean eventMatches(String eventType, String pattern) {
        return pattern.equals(eventType);
    }

    private boolean isFirstOne(Long value) {
        return value != null && value == 1;
    }

    @Override
    public void accept(Event event) {
        List<BadgeSignal> signals = handle(event);
        if (signals != null) {
            signals.forEach(s -> options.getCollector().accept(s));
        }
    }
}
