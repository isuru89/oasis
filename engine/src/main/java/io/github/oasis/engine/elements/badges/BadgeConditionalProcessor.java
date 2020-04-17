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

package io.github.oasis.engine.elements.badges;

import io.github.oasis.core.Event;
import io.github.oasis.engine.elements.badges.rules.BadgeConditionalRule;
import io.github.oasis.engine.elements.badges.signals.BadgeSignal;
import io.github.oasis.engine.elements.badges.signals.ConditionalBadgeSignal;
import io.github.oasis.core.external.Db;
import io.github.oasis.core.external.DbContext;
import io.github.oasis.core.external.Mapped;
import io.github.oasis.core.context.ExecutionContext;
import io.github.oasis.engine.model.ID;
import io.github.oasis.core.elements.RuleContext;

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
    public List<BadgeSignal> process(Event event, BadgeConditionalRule rule, ExecutionContext context, DbContext db) {
        List<BadgeConditionalRule.Condition> conditions = rule.getConditions();
        if (conditions.isEmpty()) {
            return null;
        }

        Optional<BadgeConditionalRule.Condition> first = conditions.stream()
                .filter(condition -> condition.getCondition().matches(event, rule, context))
                .findFirst();
        if (first.isPresent()) {
            BadgeConditionalRule.Condition condition = first.get();
            int attrId = condition.getAttribute();
            String badgeMetaKey = ID.getUserBadgesMetaKey(event.getGameId(), event.getUser());
            String attrKey = rule.getId() + ":attr:" + attrId;
            Mapped map = db.MAP(badgeMetaKey);
            long count = map.incrementByOne(attrKey);
            if (rule.getMaxAwardTimes() >= count) {
                return List.of(ConditionalBadgeSignal.create(rule.getId(), event, attrId));
            } else {
                map.decrementByOne(attrKey);
            }
        }
        return null;
    }

}
