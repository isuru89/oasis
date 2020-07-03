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

package io.github.oasis.engine.element.points;

import io.github.oasis.core.context.ExecutionContext;
import io.github.oasis.core.elements.AbstractRule;
import io.github.oasis.core.elements.EventExecutionFilter;
import io.github.oasis.core.elements.EventValueResolver;

import java.math.BigDecimal;

/**
 * @author Isuru Weerarathna
 */
public class PointRule extends AbstractRule {

    private String pointId;
    private EventExecutionFilter criteria;
    private BigDecimal amountToAward;
    private EventValueResolver<ExecutionContext> amountExpression;

    public PointRule(String id) {
        super(id);
    }

    public boolean isAwardBasedOnEvent() {
        return amountExpression != null;
    }

    public EventExecutionFilter getCriteria() {
        return criteria;
    }

    public void setCriteria(EventExecutionFilter criteria) {
        this.criteria = criteria;
    }

    public BigDecimal getAmountToAward() {
        return amountToAward;
    }

    public void setAmountToAward(BigDecimal amountToAward) {
        this.amountToAward = amountToAward;
    }

    public EventValueResolver<ExecutionContext> getAmountExpression() {
        return amountExpression;
    }

    public void setAmountExpression(EventValueResolver<ExecutionContext> amountExpression) {
        this.amountExpression = amountExpression;
    }

    public String getPointId() {
        return pointId;
    }

    public void setPointId(String pointId) {
        this.pointId = pointId;
    }
}
