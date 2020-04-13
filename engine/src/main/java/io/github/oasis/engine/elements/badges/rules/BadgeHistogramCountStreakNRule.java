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

package io.github.oasis.engine.elements.badges.rules;

import io.github.oasis.engine.model.EventExecutionFilter;
import io.github.oasis.engine.model.EventValueResolver;
import io.github.oasis.engine.model.ExecutionContext;

import java.math.BigDecimal;

/**
 * @author Isuru Weerarathna
 */
public class BadgeHistogramCountStreakNRule extends BadgeHistogramStreakNRule {

    public BadgeHistogramCountStreakNRule(String id) {
        super(id);

        super.threshold = BigDecimal.ONE;
    }

    @Override
    public void setValueResolver(EventValueResolver<ExecutionContext> valueResolver) {
        throw new IllegalStateException("Use condition instead of value resolver!");
    }

    public void setCondition(EventExecutionFilter condition) {
        super.valueResolver = (event, ctx) -> {
            if (condition.matches(event, this, ctx)) {
                return BigDecimal.ONE;
            } else {
                return BigDecimal.ZERO;
            }
        };
    }
}
