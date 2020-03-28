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
import io.github.oasis.engine.rules.signals.ConditionalBadge;
import io.github.oasis.model.Event;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * @author Isuru Weerarathna
 */
public class ConditionalBadgeProcessor extends BadgeProcessor implements Consumer<Event> {

    private ConditionalBadgeRule rule;

    public ConditionalBadgeProcessor(JedisPool pool, ConditionalBadgeRule rule) {
        super(pool);
        this.rule = rule;
    }

    private List<BadgeSignal> handle(Event event, Jedis jedis) {
        List<ConditionalBadgeRule.Condition> conditions = rule.getConditions();
        if (conditions.isEmpty()) {
            return null;
        }

        Optional<ConditionalBadgeRule.Condition> first = conditions.stream()
                .filter(condition -> condition.getCondition().test(event))
                .findFirst();
        if (first.isPresent()) {
            ConditionalBadgeRule.Condition condition = first.get();
            int attrId = condition.getAttribute();
            String badgeMetaKey = ID.getUserBadgesMetaKey(event.getGameId(), event.getUser());
            long count = asLong(jedis.hincrBy(badgeMetaKey, rule.getId() + ":attr:" + attrId, 1));
            if (rule.getMaxAwardTimes() >= count) {
                return Collections.singletonList(new ConditionalBadge(rule.getId(),
                        attrId,
                        event.getTimestamp(),
                        event.getExternalId()));
            } else {
                jedis.hincrBy(badgeMetaKey, rule.getId() + ":attr:" + attrId, -1);
            }
        }
        return null;
    }

    @Override
    public void accept(Event event) {
        if (!isMatchEvent(event, rule)) {
            return;
        }

        try (Jedis jedis = pool.getResource()) {
            List<BadgeSignal> signals = handle(event, jedis);
            if (signals != null) {
                signals.forEach(signal -> {
                    beforeBatchEmit(signal, event, rule, jedis);
                    rule.getCollector().accept(signal);
                });
            }
        }
    }
}
