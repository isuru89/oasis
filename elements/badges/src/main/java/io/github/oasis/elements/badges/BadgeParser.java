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

package io.github.oasis.elements.badges;

import io.github.oasis.core.VariableNames;
import io.github.oasis.core.elements.*;
import io.github.oasis.core.external.messages.PersistedDef;
import io.github.oasis.core.utils.Numbers;
import io.github.oasis.core.utils.Texts;
import io.github.oasis.core.utils.Timestamps;
import io.github.oasis.core.utils.Utils;
import io.github.oasis.elements.badges.rules.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static io.github.oasis.elements.badges.BadgeDef.*;

/**
 * @author Isuru Weerarathna
 */
public class BadgeParser extends AbstractElementParser {
    @Override
    public BadgeDef parse(PersistedDef persistedObj) {
        return loadFrom(persistedObj, BadgeDef.class);
    }

    @Override
    public AbstractRule convert(AbstractDef definition) {
        if (definition instanceof BadgeDef) {
            return convertDef((BadgeDef) definition);
        }
        throw new IllegalArgumentException("Unknown definition type for badge parser! " + definition);
    }

    private AbstractRule convertDef(BadgeDef def) {
        BadgeRule rule;
        String kind = def.getKind();
        String id = Utils.firstNonNullAsStr(def.getId(), def.generateUniqueHash());
        if (FIRST_EVENT_KIND.equals(kind)) {
            String event = (String) def.getEvent();
            rule = new FirstEventBadgeRule(def.generateUniqueHash(), event, def.getAttribute());
            AbstractDef.defToRule(def, rule);
        } else if (STREAK_N_KIND.equals(kind)) {
            StreakNBadgeRule temp = new StreakNBadgeRule(id);
            AbstractDef.defToRule(def, temp);
            temp.setStreaks(toStreakMap(def.getStreaks()));
            temp.setCriteria(temp.getEventFilter());
            temp.setEventFilter(EventExecutionFilterFactory.ALWAYS_TRUE);
            temp.setRetainTime(toLongTimeUnit(def));
            rule = temp;
        } else if (CONDITIONAL_KIND.equals(kind)) {
            ConditionalBadgeRule temp = new ConditionalBadgeRule(id);
            AbstractDef.defToRule(def, temp);
            temp.setConditions(def.getConditions().stream()
                    .map(BadgeDef.Condition::toRuleCondition)
                    .collect(Collectors.toList()));
            temp.setMaxAwardTimes(Numbers.ifNull(def.getMaxAwardTimes(), Integer.MAX_VALUE));
            rule = temp;
        } else if (TIME_BOUNDED_STREAK_KIND.equals(kind)) {
            TimeBoundedStreakNRule temp = new TimeBoundedStreakNRule(id);
            AbstractDef.defToRule(def, temp);
            temp.setStreaks(toStreakMap(def.getStreaks()));
            temp.setCriteria(temp.getEventFilter());
            temp.setConsecutive(def.getConsecutive());
            temp.setTimeUnit(toLongTimeUnit(def));
            rule = temp;
        } else if (PERIODIC_ACCUMULATIONS_KIND.equals(kind)) {
            PeriodicBadgeRule temp = new PeriodicBadgeRule(id);
            AbstractDef.defToRule(def, temp);
            temp.setTimeUnit(toLongTimeUnit(def));
            temp.setCriteria(temp.getEventFilter());
            temp.setThresholds(def.getThresholds().stream()
                    .map(BadgeDef.Threshold::toRuleThreshold).collect(Collectors.toList()));
            temp.setValueResolver(Scripting.create((String) def.getAggregatorExtractor(), VariableNames.CONTEXT_VAR));
            rule = temp;
        } else if (PERIODIC_OCCURRENCES_KIND.equals(kind)) {
            PeriodicOccurrencesRule temp = new PeriodicOccurrencesRule(id);
            AbstractDef.defToRule(def, temp);
            temp.setTimeUnit(toLongTimeUnit(def));
            temp.setCriteria(temp.getEventFilter());
            temp.setThresholds(def.getThresholds().stream()
                    .map(BadgeDef.Threshold::toRuleThreshold).collect(Collectors.toList()));
            rule = temp;
        } else if (PERIODIC_ACCUMULATIONS_STREAK_KIND.equals(kind)) {
            PeriodicStreakNRule temp = new PeriodicStreakNRule(id);
            AbstractDef.defToRule(def, temp);
            temp.setConsecutive(def.getConsecutive());
            temp.setTimeUnit(toLongTimeUnit(def));
            temp.setValueResolver(Scripting.create((String) def.getAggregatorExtractor(), VariableNames.CONTEXT_VAR));
            temp.setThreshold(def.getThreshold());
            temp.setStreaks(toStreakMap(def.getStreaks()));
            rule = temp;
        } else if (PERIODIC_OCCURRENCES_STREAK_KIND.equals(kind)) {
            PeriodicOccurrencesStreakNRule temp = new PeriodicOccurrencesStreakNRule(id);
            AbstractDef.defToRule(def, temp);
            temp.setConsecutive(def.getConsecutive());
            temp.setTimeUnit(toLongTimeUnit(def));
            temp.setThreshold(def.getThreshold());
            temp.setStreaks(toStreakMap(def.getStreaks()));
            rule = temp;
        } else {
            throw new IllegalArgumentException("Unknown badge kind! " + kind);
        }

        setPointDetails(def, rule);

        return rule;
    }

    private void setPointDetails(BadgeDef def, BadgeRule rule) {
        if (!Texts.isEmpty(def.getPointId())) {
            rule.setPointId(def.getPointId());
            rule.setPointAwards(Utils.toBigDecimal(def.getPointAwards()));
        }
    }

    private Map<Integer, StreakNBadgeRule.StreakProps> toStreakMap(List<BadgeDef.Streak> streaks) {
        return streaks.stream().collect(Collectors.toMap(BadgeDef.Streak::getStreak,
                streak -> new StreakNBadgeRule.StreakProps(streak.getAttribute(), Utils.toBigDecimal(streak.getPointAwards()))));
    }

    private long toLongTimeUnit(BadgeDef def) {
        Object timeUnit = def.getTimeUnit();
        if (Objects.isNull(timeUnit)) {
            return 0;
        }
        if (timeUnit instanceof Number) {
            return ((Number) timeUnit).longValue();
        } else if (timeUnit instanceof String) {
            return Timestamps.parseTimeUnit((String) timeUnit);
        }
        return 0;
    }

}
