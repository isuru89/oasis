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

package io.github.oasis.engine.rules;

import io.github.oasis.model.Event;

import java.math.BigDecimal;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

/**
 * @author Isuru Weerarathna
 */
public class PointRule extends AbstractRule {

    private BiPredicate<Event, PointRule> criteria;
    private BigDecimal amountToAward;
    private BiFunction<Event, PointRule, BigDecimal> amountExpression;

    public PointRule(String id) {
        super(id);
    }

    public boolean isAwardBasedOnEvent() {
        return amountExpression != null;
    }

    public BiPredicate<Event, PointRule> getCriteria() {
        return criteria;
    }

    public void setCriteria(BiPredicate<Event, PointRule> criteria) {
        this.criteria = criteria;
    }

    public BigDecimal getAmountToAward() {
        return amountToAward;
    }

    public void setAmountToAward(BigDecimal amountToAward) {
        this.amountToAward = amountToAward;
    }

    public BiFunction<Event, PointRule, BigDecimal> getAmountExpression() {
        return amountExpression;
    }

    public void setAmountExpression(BiFunction<Event, PointRule, BigDecimal> amountExpression) {
        this.amountExpression = amountExpression;
    }
}
