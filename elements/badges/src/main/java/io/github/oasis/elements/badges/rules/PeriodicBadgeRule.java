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

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Isuru Weerarathna
 */
public class PeriodicBadgeRule extends BadgeRule {

    private long timeUnit;
    private EventExecutionFilter criteria;
    protected EventValueResolver<ExecutionContext> valueResolver;
    private List<Threshold> thresholds;

    public PeriodicBadgeRule(String id) {
        super(id);
    }

    public EventExecutionFilter getCriteria() {
        return criteria;
    }

    public void setCriteria(EventExecutionFilter criteria) {
        this.criteria = criteria;
    }

    public EventValueResolver<ExecutionContext> getValueResolver() {
        return valueResolver;
    }

    public void setValueResolver(EventValueResolver<ExecutionContext> valueResolver) {
        this.valueResolver = valueResolver;
    }

    public List<Threshold> getThresholds() {
        return thresholds;
    }

    public long getTimeUnit() {
        return timeUnit;
    }

    public void setTimeUnit(long timeUnit) {
        this.timeUnit = timeUnit;
    }

    public void setThresholds(List<Threshold> thresholds) {
        this.thresholds = new LinkedList<>(thresholds);
        this.thresholds.sort(Comparator.reverseOrder());
    }

    public static class Threshold implements Comparable<Threshold> {
        private final int attribute;
        private final BigDecimal value;

        public Threshold(int attribute, BigDecimal value) {
            this.attribute = attribute;
            this.value = value;
        }

        public int getAttribute() {
            return attribute;
        }

        public BigDecimal getValue() {
            return value;
        }

        @Override
        public int compareTo(Threshold o) {
            return this.getValue().compareTo(o.getValue());
        }
    }
}
