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

package io.github.oasis.elements.badges.rules;

import io.github.oasis.core.elements.EventExecutionFilter;
import io.github.oasis.core.utils.Utils;
import io.github.oasis.elements.badges.signals.BadgeSignal;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * @author Isuru Weerarathna
 */
@Getter
@Setter
public class ConditionalBadgeRule extends BadgeRule {

    private List<Condition> conditions;

    public ConditionalBadgeRule(String id) {
        super(id);
    }

    @Override
    public void derivePointsInTo(BadgeSignal signal) {
        int attrId = signal.getAttribute();
        Condition matchedCondition = conditions.stream()
                .filter(condition -> condition.getAttribute() == attrId)
                .findFirst()
                .orElse(null);

        if (matchedCondition != null) {
            if (Objects.nonNull(matchedCondition.getPointAwards())) {
                signal.setPointAwards(matchedCondition.getPointId(), matchedCondition.getPointAwards());
            } else {
                super.derivePointsInTo(signal);
            }
        }
    }

    public void setConditions(List<Condition> conditions) {
        this.conditions = new LinkedList<>(conditions);
        Collections.sort(this.conditions);
    }

    @Getter
    @Builder
    public static class Condition implements Comparable<Condition> {
        private final int priority;
        private final EventExecutionFilter condition;
        private final int attribute;
        private final int maxBadgesAllowed;
        private final String pointId;
        private final BigDecimal pointAwards;

        public Condition(int priority, EventExecutionFilter condition, int attribute, Integer maxBadgesAllowed) {
            this(priority, condition, attribute, maxBadgesAllowed, null, null);
        }

        public Condition(int priority, EventExecutionFilter condition,
                         int attribute, Integer maxBadgesAllowed,
                         String pointId, BigDecimal pointAwards) {
            this.priority = priority;
            this.condition = condition;
            this.attribute = attribute;
            this.maxBadgesAllowed = Utils.firstNonNull(maxBadgesAllowed, Integer.MAX_VALUE);
            this.pointId = pointId;
            this.pointAwards = pointAwards;
        }

        @Override
        public int compareTo(Condition o) {
            return Integer.compare(this.getPriority(), o.getPriority());
        }
    }

}
