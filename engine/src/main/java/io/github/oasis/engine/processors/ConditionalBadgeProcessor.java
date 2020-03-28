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

import io.github.oasis.engine.model.ID;
import io.github.oasis.engine.rules.ConditionalBadgeRule;
import io.github.oasis.engine.rules.signals.BadgeSignal;
import io.github.oasis.engine.rules.signals.ConditionalBadge;
import io.github.oasis.model.Event;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static io.github.oasis.engine.utils.Numbers.asLong;

/**
 * Awards a badge based on a different range of criteria satisfied for the event.
 * This badge is independent of time units.
 * <p>
 * For e.g:   Award,<br>
 *     <ul>
 *       <li>bronze badge <= if score > 50 </li>
 *       <li>silver badge <= if score > 75 </li>
 *       <li>gold badge <= if score > 90 </li>
 *     </ul>
 * </p>
 *
 * @author Isuru Weerarathna
 */
public class ConditionalBadgeProcessor extends BadgeProcessor<ConditionalBadgeRule> {

    public ConditionalBadgeProcessor(JedisPool pool, ConditionalBadgeRule rule) {
        super(pool, rule);
    }

    @Override
    public List<BadgeSignal> process(Event event, ConditionalBadgeRule rule, Jedis jedis) {
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

}
