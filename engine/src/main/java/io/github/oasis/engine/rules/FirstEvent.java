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
import java.util.function.Consumer;

import static io.github.oasis.engine.utils.Numbers.isFirstOne;

/**
 * @author Isuru Weerarathna
 */
public class FirstEvent extends BadgeProcessor implements Consumer<Event> {

    private final FirstEventRule rule;

    public FirstEvent(JedisPool pool, FirstEventRule firstEventOptions) {
        super(pool);
        this.rule = firstEventOptions;
    }

    @Override
    public void accept(Event event) {
        if (!isMatchEvent(event, rule)) {
            return;
        }

        if (unableToProcess(event, rule)) {
            return;
        }

        List<BadgeSignal> signals = handle(event);
        if (signals != null) {
            signals.forEach(s -> rule.getCollector().accept(s));
        }
    }

    public List<BadgeSignal> handle(Event event) {
        String key = ID.getUserFirstEventsKey(event.getGameId(), event.getUser());
        try (Jedis jedis = pool.getResource()) {
            long ts = event.getTimestamp();
            String id = event.getExternalId();
            String subKey = rule.getEventName();
            String value = ts + ":" + id + ":" + System.currentTimeMillis();
            if (isFirstOne(jedis.hsetnx(key, subKey, value))) {
                return Collections.singletonList(new BadgeSignal(rule.getId(),
                        1,
                        ts, ts,
                        id, id));
            }
        }
        return null;
    }

}
