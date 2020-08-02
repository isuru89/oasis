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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Isuru Weerarathna
 */
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
    private Object valueExtractorExpression;

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
        attrs.add(Utils.firstNonNullAsStr(getValueExtractorExpression(), EMPTY));
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

    public Object getValueExtractorExpression() {
        return valueExtractorExpression;
    }

    public void setValueExtractorExpression(Object valueExtractorExpression) {
        this.valueExtractorExpression = valueExtractorExpression;
    }

    public Integer getMaxAwardTimes() {
        return maxAwardTimes;
    }

    public void setMaxAwardTimes(Integer maxAwardTimes) {
        this.maxAwardTimes = maxAwardTimes;
    }

    public Integer getAttribute() {
        return attribute;
    }

    public void setAttribute(Integer attribute) {
        this.attribute = attribute;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public Boolean getConsecutive() {
        return consecutive;
    }

    public void setConsecutive(Boolean consecutive) {
        this.consecutive = consecutive;
    }

    public Object getTimeUnit() {
        return timeUnit;
    }

    public void setTimeUnit(Object timeUnit) {
        this.timeUnit = timeUnit;
    }

    public BigDecimal getThreshold() {
        return threshold;
    }

    public void setThreshold(BigDecimal threshold) {
        this.threshold = threshold;
    }

    public List<Condition> getConditions() {
        return conditions;
    }

    public void setConditions(List<Condition> conditions) {
        this.conditions = conditions;
    }

    public List<Streak> getStreaks() {
        return streaks;
    }

    public void setStreaks(List<Streak> streaks) {
        this.streaks = streaks;
    }

    public List<Threshold> getThresholds() {
        return thresholds;
    }

    public void setThresholds(List<Threshold> thresholds) {
        this.thresholds = thresholds;
    }

    public String getPointId() {
        return pointId;
    }

    public void setPointId(String pointId) {
        this.pointId = pointId;
    }

    public Object getPointAwards() {
        return pointAwards;
    }

    public void setPointAwards(Object pointAwards) {
        this.pointAwards = pointAwards;
    }

    public static class Streak {
        private Integer streak;
        private Integer attribute;
        private Object pointAwards;

        public Streak() {
        }

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

        public Object getPointAwards() {
            return pointAwards;
        }

        public void setPointAwards(Object pointAwards) {
            this.pointAwards = pointAwards;
        }

        public Integer getStreak() {
            return streak;
        }

        public void setStreak(Integer streak) {
            this.streak = streak;
        }

        public Integer getAttribute() {
            return attribute;
        }

        public void setAttribute(Integer attribute) {
            this.attribute = attribute;
        }
    }

    public static class Threshold {
        private BigDecimal value;
        private Integer attribute;
        private Object pointAwards;

        public Threshold() {
        }

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

        public Object getPointAwards() {
            return pointAwards;
        }

        public void setPointAwards(Object pointAwards) {
            this.pointAwards = pointAwards;
        }

        public BigDecimal getValue() {
            return value;
        }

        public void setValue(BigDecimal value) {
            this.value = value;
        }

        public Integer getAttribute() {
            return attribute;
        }

        public void setAttribute(Integer attribute) {
            this.attribute = attribute;
        }
    }

    public static class Condition {
        private Integer priority;
        private Object condition;
        private Integer attribute;
        private Object pointAwards;

        public Condition() {
        }

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

        public Object getPointAwards() {
            return pointAwards;
        }

        public void setPointAwards(Object pointAwards) {
            this.pointAwards = pointAwards;
        }

        public Integer getPriority() {
            return priority;
        }

        public void setPriority(Integer priority) {
            this.priority = priority;
        }

        public Object getCondition() {
            return condition;
        }

        public void setCondition(Object condition) {
            this.condition = condition;
        }

        public Integer getAttribute() {
            return attribute;
        }

        public void setAttribute(Integer attribute) {
            this.attribute = attribute;
        }
    }

}
