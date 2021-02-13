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
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * @author Isuru Weerarathna
 */
@Getter
@Setter
public class PointRule extends AbstractRule {

    private String pointId;
    private EventExecutionFilter criteria;
    private BigDecimal amountToAward;
    private EventValueResolver<ExecutionContext> amountExpression;

    private BigDecimal capLimit;
    private String capDuration;

    public PointRule(String id) {
        super(id);
    }

    public boolean isCapped() {
        return capDuration != null && capLimit != null;
    }

    public boolean isAwardBasedOnEvent() {
        return amountExpression != null;
    }
}
