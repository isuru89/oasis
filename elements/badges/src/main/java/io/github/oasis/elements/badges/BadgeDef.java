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

import io.github.oasis.core.elements.AbstractDef;
import io.github.oasis.core.elements.EventExecutionFilter;
import io.github.oasis.core.elements.EventExecutionFilterFactory;
import io.github.oasis.core.utils.Utils;
import io.github.oasis.elements.badges.rules.ConditionalBadgeRule;
import io.github.oasis.elements.badges.rules.PeriodicBadgeRule;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Definition for a badge rule. All badge rules will be represented by this single definition.
 *
 * @author Isuru Weerarathna
 */
@Getter
@Setter
public class BadgeDef extends AbstractDef {

    public static final String FIRST_EVENT_KIND = "firstEvent";
    public static final String NTH_EVENT_KIND = "NthEvent";
    public static final String CONDITIONAL_KIND = "conditional";
    public static final String STREAK_N_KIND = "streak";
    public static final String TIME_BOUNDED_STREAK_KIND = "timeBoundedStreak";
    public static final String PERIODIC_OCCURRENCES_KIND = "periodicOccurrences";
    public static final String PERIODIC_OCCURRENCES_STREAK_KIND = "periodicOccurrencesStreak";
    public static final String PERIODIC_ACCUMULATIONS_KIND = "periodicAccumulation";
    public static final String PERIODIC_ACCUMULATIONS_STREAK_KIND = "periodicAccumulationStreak";

    private Integer maxAwardTimes;
    private String kind;
    private Integer attribute;
    private Boolean consecutive;

    private Object timeUnit;
    private BigDecimal threshold;
    private Object aggregatorExtractor;

    private String pointId;
    private Object pointAwards;

    private List<Condition> conditions;
    private List<Streak> streaks;
    private List<Threshold> thresholds;

    @Override
    protected List<String> getSensitiveAttributes() {
        List<String> attrs = new ArrayList<>(super.getSensitiveAttributes());
        attrs.add(Utils.firstNonNullAsStr(getAttribute(), EMPTY));
        attrs.add(Utils.firstNonNullAsStr(getConsecutive(), EMPTY));
        attrs.add(Utils.firstNonNullAsStr(getThreshold(), EMPTY));
        attrs.add(Utils.firstNonNullAsStr(getTimeUnit(), EMPTY));
        attrs.add(Utils.firstNonNullAsStr(getAggregatorExtractor(), EMPTY));
        attrs.add(Utils.firstNonNullAsStr(getPointAwards(), EMPTY));

        if (Objects.nonNull(conditions)) {
            attrs.add(getConditions().stream()
                    .sorted(Comparator.comparingInt(Condition::getPriority))
                    .flatMap(c -> c.getSensitiveAttributes().stream())
                    .collect(Collectors.joining()));
        }
        if (Objects.nonNull(streaks)) {
            attrs.add(getStreaks().stream()
                    .sorted(Comparator.comparingInt(Streak::getStreak))
                    .flatMap(s -> s.getSensitiveAttributes().stream()).collect(Collectors.joining()));
        }
        if (Objects.nonNull(thresholds)) {
            attrs.add(getThresholds().stream()
                    .sorted(Comparator.comparing(Threshold::getValue))
                    .flatMap(t -> t.getSensitiveAttributes().stream()).collect(Collectors.joining()));
        }

        return attrs;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Streak {
        private Integer streak;
        private Integer attribute;
        private Object pointAwards;

        public Streak(Integer streak, Integer attribute) {
            this.streak = streak;
            this.attribute = attribute;
        }

        private List<String> getSensitiveAttributes() {
            return List.of(
                    Utils.firstNonNullAsStr(getAttribute(), EMPTY),
                    Utils.firstNonNullAsStr(getStreak(), EMPTY)
            );
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Threshold {
        private BigDecimal value;
        private Integer attribute;
        private Object pointAwards;

        public Threshold(BigDecimal value, Integer attribute) {
            this.value = value;
            this.attribute = attribute;
        }

        private List<String> getSensitiveAttributes() {
            return List.of(
                    Utils.firstNonNullAsStr(getAttribute(), EMPTY),
                    Utils.firstNonNullAsStr(getValue(), EMPTY)
            );
        }

        PeriodicBadgeRule.Threshold toRuleThreshold() {
            return new PeriodicBadgeRule.Threshold(attribute, value, Utils.toBigDecimal(pointAwards));
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Condition {
        private Integer priority;
        private Object condition;
        private Integer attribute;
        private Object pointAwards;

        public Condition(Integer priority, Object condition, Integer attribute) {
            this.priority = priority;
            this.condition = condition;
            this.attribute = attribute;
        }

        private List<String> getSensitiveAttributes() {
            return List.of(
                    Utils.firstNonNullAsStr(getPriority(), EMPTY),
                    Utils.firstNonNullAsStr(getAttribute(), EMPTY),
                    Utils.firstNonNullAsStr(getCondition(), EMPTY)
            );
        }

        ConditionalBadgeRule.Condition toRuleCondition() {
            EventExecutionFilter filter = EventExecutionFilterFactory.create(condition);
            return new ConditionalBadgeRule.Condition(priority, filter, attribute, Utils.toBigDecimal(pointAwards));
        }
    }

}
