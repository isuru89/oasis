/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.github.oasis.elements.badges.processors;

import io.github.oasis.core.Event;
import io.github.oasis.core.context.ExecutionContext;
import io.github.oasis.core.elements.RuleContext;
import io.github.oasis.core.external.Db;
import io.github.oasis.core.external.DbContext;
import io.github.oasis.core.external.Mapped;
import io.github.oasis.core.utils.Utils;
import io.github.oasis.elements.badges.BadgeIDs;
import io.github.oasis.elements.badges.rules.ConditionalBadgeRule;
import io.github.oasis.elements.badges.signals.BadgeSignal;
import io.github.oasis.elements.badges.signals.ConditionalBadgeSignal;

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
public class ConditionalBadgeProcessor extends AbstractBadgeProcessor<ConditionalBadgeRule> {

    public static final String ATTR_DELIMETER = ":attr:";

    public ConditionalBadgeProcessor(Db pool, RuleContext<ConditionalBadgeRule> ruleContext) {
        super(pool, ruleContext);
    }

    @Override
    public List<BadgeSignal> process(Event event, ConditionalBadgeRule rule, ExecutionContext context, DbContext db) {
        List<ConditionalBadgeRule.Condition> conditions = rule.getConditions();
        if (conditions.isEmpty()) {
            return null;
        }

        Optional<ConditionalBadgeRule.Condition> first = conditions.stream()
                .filter(condition -> condition.getCondition().matches(event, rule, context))
                .findFirst();
        if (first.isPresent()) {
            ConditionalBadgeRule.Condition condition = first.get();
            int attrId = condition.getAttribute();
            String badgeId = Utils.firstNonNull(rule.getBadgeId(), rule.getId());
            String badgeMetaKey = BadgeIDs.getUserBadgesMetaKey(event.getGameId(), event.getUser());
            String attrKey = rule.getId() + ATTR_DELIMETER + attrId;
            Mapped map = db.MAP(badgeMetaKey);
            long count = map.incrementByOne(attrKey);
            if (condition.getMaxBadgesAllowed() >= count) {
                return List.of(ConditionalBadgeSignal.create(rule.getId(), badgeId, event, attrId));
            } else {
                map.decrementByOne(attrKey);
            }
        }
        return null;
    }

}
