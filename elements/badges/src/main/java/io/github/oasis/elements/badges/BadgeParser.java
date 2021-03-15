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
import io.github.oasis.core.elements.AbstractDef;
import io.github.oasis.core.elements.AbstractElementParser;
import io.github.oasis.core.elements.AbstractRule;
import io.github.oasis.core.elements.EventExecutionFilterFactory;
import io.github.oasis.core.elements.Scripting;
import io.github.oasis.core.elements.spec.BaseSpecification;
import io.github.oasis.core.elements.spec.TimeUnitDef;
import io.github.oasis.core.external.messages.PersistedDef;
import io.github.oasis.core.utils.Timestamps;
import io.github.oasis.core.utils.Utils;
import io.github.oasis.elements.badges.rules.BadgeRule;
import io.github.oasis.elements.badges.rules.ConditionalBadgeRule;
import io.github.oasis.elements.badges.rules.FirstEventBadgeRule;
import io.github.oasis.elements.badges.rules.PeriodicBadgeRule;
import io.github.oasis.elements.badges.rules.PeriodicOccurrencesRule;
import io.github.oasis.elements.badges.rules.PeriodicOccurrencesStreakNRule;
import io.github.oasis.elements.badges.rules.PeriodicStreakNRule;
import io.github.oasis.elements.badges.rules.StreakNBadgeRule;
import io.github.oasis.elements.badges.rules.TimeBoundedStreakNRule;
import io.github.oasis.elements.badges.spec.BadgeSpecification;
import io.github.oasis.elements.badges.spec.RewardDef;
import io.github.oasis.elements.badges.spec.Streak;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static io.github.oasis.elements.badges.BadgeDef.CONDITIONAL_KIND;
import static io.github.oasis.elements.badges.BadgeDef.FIRST_EVENT_KIND;
import static io.github.oasis.elements.badges.BadgeDef.PERIODIC_ACCUMULATIONS_KIND;
import static io.github.oasis.elements.badges.BadgeDef.PERIODIC_ACCUMULATIONS_STREAK_KIND;
import static io.github.oasis.elements.badges.BadgeDef.PERIODIC_OCCURRENCES_KIND;
import static io.github.oasis.elements.badges.BadgeDef.PERIODIC_OCCURRENCES_STREAK_KIND;
import static io.github.oasis.elements.badges.BadgeDef.STREAK_N_KIND;
import static io.github.oasis.elements.badges.BadgeDef.TIME_BOUNDED_STREAK_KIND;

/**
 * @author Isuru Weerarathna
 */
public class BadgeParser extends AbstractElementParser {
    @Override
    public BadgeDef parse(PersistedDef persistedObj) {
        return loadFrom(persistedObj, BadgeDef.class);
    }

    @Override
    public AbstractRule convert(AbstractDef<? extends BaseSpecification> definition) {
        if (definition instanceof BadgeDef) {
            return convertDef((BadgeDef) definition);
        }
        throw new IllegalArgumentException("Unknown definition type for badge parser! " + definition);
    }

    private AbstractRule convertDef(BadgeDef def) {
        def.validate();

        BadgeRule rule;
        BadgeSpecification spec = def.getSpec();
        String kind = spec.getKind();

        String id = def.getId();
        if (FIRST_EVENT_KIND.equals(kind)) {
            String event = spec.getSelector().getMatchEvent();
            rule = new FirstEventBadgeRule(id, event, spec.getRewards().getBadge().getAttribute());
            AbstractDef.defToRule(def, rule);
        } else if (STREAK_N_KIND.equals(kind)) {
            StreakNBadgeRule temp = new StreakNBadgeRule(id);
            AbstractDef.defToRule(def, temp);
            temp.setStreaks(toStreakMap(spec.getStreaks(), def));
            if (spec.getCondition() != null) {
                temp.setCriteria(EventExecutionFilterFactory.create(spec.getCondition()));
            } else {
                temp.setCriteria(EventExecutionFilterFactory.ALWAYS_TRUE);
            }
            if (spec.getRetainTime() == null) {
                throw new IllegalArgumentException("Field 'retainTime' must be specified!");
            }
            temp.setRetainTime(toLongTimeUnit(spec.getRetainTime()));
            rule = temp;
        } else if (CONDITIONAL_KIND.equals(kind)) {
            ConditionalBadgeRule temp = new ConditionalBadgeRule(id);
            AbstractDef.defToRule(def, temp);
            temp.setConditions(spec.getConditions().stream()
                    .map(cond -> cond.toRuleCondition(def))
                    .collect(Collectors.toList()));
            rule = temp;
        } else if (TIME_BOUNDED_STREAK_KIND.equals(kind)) {
            TimeBoundedStreakNRule temp = new TimeBoundedStreakNRule(id);
            AbstractDef.defToRule(def, temp);
            temp.setStreaks(toStreakMap(spec.getStreaks(), def));
            if (spec.getCondition() != null) {
                temp.setCriteria(EventExecutionFilterFactory.create(spec.getCondition()));
            } else {
                temp.setCriteria(EventExecutionFilterFactory.ALWAYS_TRUE);
            }
            if (spec.getRetainTime() == null) {
                throw new IllegalArgumentException("Field 'retainTime' must be specified!");
            }
            if (spec.getTimeRange() == null) {
                throw new IllegalArgumentException("Field 'timeRange' must be specified!");
            }
            temp.setRetainTime(toLongTimeUnit(spec.getRetainTime()));
            temp.setConsecutive(Utils.firstNonNull(spec.getConsecutive(), Boolean.TRUE));
            temp.setTimeUnit(toLongTimeUnit(spec.getTimeRange()));
            rule = temp;
        } else if (PERIODIC_ACCUMULATIONS_KIND.equals(kind)) {
            PeriodicBadgeRule temp = new PeriodicBadgeRule(id);
            AbstractDef.defToRule(def, temp);
            temp.setTimeUnit(toLongTimeUnit(spec.getPeriod()));
            temp.setCriteria(temp.getEventFilter());
            temp.setThresholds(spec.getThresholds().stream()
                    .map(t -> t.toRuleThreshold(def)).collect(Collectors.toList()));
            temp.setValueResolver(Scripting.create(spec.getAggregatorExtractor().getExpression(), VariableNames.CONTEXT_VAR));
            rule = temp;
        } else if (PERIODIC_OCCURRENCES_KIND.equals(kind)) {
            PeriodicOccurrencesRule temp = new PeriodicOccurrencesRule(id);
            AbstractDef.defToRule(def, temp);
            temp.setTimeUnit(toLongTimeUnit(spec.getPeriod()));
            temp.setCriteria(temp.getEventFilter());
            temp.setThresholds(spec.getThresholds().stream()
                    .map(t -> t.toRuleThreshold(def)).collect(Collectors.toList()));
            rule = temp;
        } else if (PERIODIC_ACCUMULATIONS_STREAK_KIND.equals(kind)) {
            PeriodicStreakNRule temp = new PeriodicStreakNRule(id);
            AbstractDef.defToRule(def, temp);
            temp.setConsecutive(Utils.firstNonNull(spec.getConsecutive(), Boolean.TRUE));
            temp.setTimeUnit(toLongTimeUnit(spec.getPeriod()));
            temp.setValueResolver(Scripting.create(spec.getAggregatorExtractor().getExpression(), VariableNames.CONTEXT_VAR));
            temp.setThreshold(spec.getThreshold());
            temp.setStreaks(toStreakMap(spec.getStreaks(), def));
            rule = temp;
        } else if (PERIODIC_OCCURRENCES_STREAK_KIND.equals(kind)) {
            PeriodicOccurrencesStreakNRule temp = new PeriodicOccurrencesStreakNRule(id);
            AbstractDef.defToRule(def, temp);
            temp.setConsecutive(spec.getConsecutive());
            temp.setTimeUnit(toLongTimeUnit(spec.getPeriod()));
            temp.setThreshold(spec.getThreshold());
            temp.setStreaks(toStreakMap(spec.getStreaks(), def));
            rule = temp;
        } else {
            throw new IllegalArgumentException("Unknown badge kind! " + kind);
        }

        setPointDetails(spec, rule);

        return rule;
    }

    private void setPointDetails(BadgeSpecification spec, BadgeRule rule) {
        if (Objects.nonNull(spec.getRewards()) && Objects.nonNull(spec.getRewards().getPoints())) {
            rule.setPointId(spec.getRewards().getPoints().getId());
            rule.setPointAwards(spec.getRewards().getPoints().getAmount());
        }
    }

    private Map<Integer, StreakNBadgeRule.StreakProps> toStreakMap(List<Streak> streaks, BadgeDef def) {
        return streaks.stream()
                .collect(Collectors.toMap(Streak::getStreak,
                    streak -> {
                    RewardDef mergedRewards = RewardDef.merge(streak.getRewards(), def.getSpec().getRewards());
                    if (mergedRewards.getPoints() != null) {
                        return new StreakNBadgeRule.StreakProps(mergedRewards.getBadge().getAttribute(),
                                mergedRewards.getPoints().getId(),
                                mergedRewards.getPoints().getAmount());
                    } else {
                        return new StreakNBadgeRule.StreakProps(mergedRewards.getBadge().getAttribute());
                    }
                }));
    }

    private long toLongTimeUnit(TimeUnitDef timeUnit) {
        if (Objects.isNull(timeUnit)) {
            return 0;
        }

        return Timestamps.parseTimeUnit(timeUnit);
    }

}
