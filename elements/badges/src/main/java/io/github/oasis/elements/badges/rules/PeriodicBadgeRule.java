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

import io.github.oasis.core.context.ExecutionContext;
import io.github.oasis.core.elements.EventExecutionFilter;
import io.github.oasis.core.elements.EventValueResolver;
import io.github.oasis.elements.badges.signals.BadgeSignal;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * @author Isuru Weerarathna
 */
@Getter
@Setter
public class PeriodicBadgeRule extends BadgeRule {

    private long timeUnit;
    private EventExecutionFilter criteria;
    protected EventValueResolver<ExecutionContext> valueResolver;
    private List<Threshold> thresholds;

    public PeriodicBadgeRule(String id) {
        super(id);
    }

    @Override
    public void derivePointsInTo(BadgeSignal signal) {
        int attrId = signal.getAttribute();
        Threshold matchedThreshold = thresholds.stream()
                .filter(threshold -> threshold.getAttribute() == attrId)
                .findFirst()
                .orElse(null);

        if (matchedThreshold != null) {
            if (Objects.nonNull(matchedThreshold.getPointAwards())) {
                signal.setPointAwards(getPointId(), matchedThreshold.getPointAwards());
            } else {
                super.derivePointsInTo(signal);
            }
        }
    }

    public void setThresholds(List<Threshold> thresholds) {
        this.thresholds = new LinkedList<>(thresholds);
        this.thresholds.sort(Comparator.reverseOrder());
    }

    @Getter
    public static class Threshold implements Comparable<Threshold> {
        private final int attribute;
        private final BigDecimal value;
        private final BigDecimal pointAwards;

        public Threshold(int attribute, BigDecimal value) {
            this(attribute, value, null);
        }

        public Threshold(int attribute, BigDecimal value, BigDecimal pointAwards) {
            this.attribute = attribute;
            this.value = value;
            this.pointAwards = pointAwards;
        }

        @Override
        public int compareTo(Threshold o) {
            return this.getValue().compareTo(o.getValue());
        }
    }
}
