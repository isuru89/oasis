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
import io.github.oasis.engine.model.RuleContext;
import io.github.oasis.engine.rules.BadgeConditionalRule;
import io.github.oasis.engine.rules.signals.BadgeSignal;
import io.github.oasis.engine.rules.signals.ConditionalBadge;
import io.github.oasis.engine.storage.Db;
import io.github.oasis.engine.storage.DbContext;
import io.github.oasis.engine.storage.Mapped;
import io.github.oasis.model.Event;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
public class BadgeConditionalProcessor extends BadgeProcessor<BadgeConditionalRule> {

    public BadgeConditionalProcessor(Db pool, RuleContext<BadgeConditionalRule> ruleContext) {
        super(pool, ruleContext);
    }

    @Override
    public List<BadgeSignal> process(Event event, BadgeConditionalRule rule, DbContext db) {
        List<BadgeConditionalRule.Condition> conditions = rule.getConditions();
        if (conditions.isEmpty()) {
            return null;
        }

        Optional<BadgeConditionalRule.Condition> first = conditions.stream()
                .filter(condition -> condition.getCondition().test(event))
                .findFirst();
        if (first.isPresent()) {
            BadgeConditionalRule.Condition condition = first.get();
            int attrId = condition.getAttribute();
            String badgeMetaKey = ID.getUserBadgesMetaKey(event.getGameId(), event.getUser());
            String attrKey = rule.getId() + ":attr:" + attrId;
            Mapped map = db.MAP(badgeMetaKey);
            long count = map.incrementBy(attrKey, 1);
            if (rule.getMaxAwardTimes() >= count) {
                return Collections.singletonList(ConditionalBadge.create(rule.getId(), event, attrId));
            } else {
                map.incrementBy(attrKey, -1);
            }
        }
        return null;
    }

}
