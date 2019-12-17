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

package io.github.oasis.model.rules;

import io.github.oasis.model.Event;
import io.github.oasis.model.defs.BaseDef;

import java.io.Serializable;
import java.util.List;

/**
 * @author iweerarathna
 */
public class PointRule extends BaseDef implements Serializable {

    private String forEvent;
    private String source;
    private Serializable conditionExpression;
    private double amount;
    private boolean currency;
    private Serializable amountExpression;
    private List<AdditionalPointReward> additionalPoints;

    public boolean isSelfAggregatedRule() {
        return "POINTS".equalsIgnoreCase(source);
    }

    public boolean canApplyForEvent(Event event) {
        return event.getEventType().equals(forEvent);
    }

    public static class AdditionalPointReward implements Serializable {
        private String toUser;
        private Serializable amount;
        private String name;
        private boolean currency;

        public boolean isCurrency() {
            return currency;
        }

        public void setCurrency(boolean currency) {
            this.currency = currency;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getToUser() {
            return toUser;
        }

        public void setToUser(String toUser) {
            this.toUser = toUser;
        }

        public Serializable getAmount() {
            return amount;
        }

        public void setAmount(Serializable amount) {
            this.amount = amount;
        }
    }

    public boolean isCurrency() {
        return currency;
    }

    public void setCurrency(boolean currency) {
        this.currency = currency;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Serializable getAmountExpression() {
        return amountExpression;
    }

    public void setAmountExpression(Serializable amountExpression) {
        this.amountExpression = amountExpression;
    }

    public List<AdditionalPointReward> getAdditionalPoints() {
        return additionalPoints;
    }

    public void setAdditionalPoints(List<AdditionalPointReward> additionalPoints) {
        this.additionalPoints = additionalPoints;
    }

    public String getForEvent() {
        return forEvent;
    }

    public void setForEvent(String forEvent) {
        this.forEvent = forEvent;
    }

    public Serializable getConditionExpression() {
        return conditionExpression;
    }

    public void setCondition(Serializable condition) {
        this.conditionExpression = condition;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        return "PointRule=" + getId();
    }
}
