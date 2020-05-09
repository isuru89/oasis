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

import io.github.oasis.core.elements.AbstractDef;
import io.github.oasis.core.elements.AbstractRule;
import io.github.oasis.core.elements.EventExecutionFilterFactory;
import io.github.oasis.core.elements.Scripting;

import java.math.BigDecimal;

import static io.github.oasis.core.VariableNames.CONTEXT_VAR;

/**
 * @author Isuru Weerarathna
 */
public class PointDef extends AbstractDef {

    private Object award;

    public AbstractRule toRule() {
        PointRule rule = new PointRule(generateUniqueHash());
        super.toRule(rule);
        rule.setCriteria(EventExecutionFilterFactory.ALWAYS_TRUE);
        if (award instanceof Number) {
            rule.setAmountToAward(BigDecimal.valueOf(((Number) award).doubleValue()));
        } else {
            rule.setAmountExpression(Scripting.create((String) award, CONTEXT_VAR));
        }
        return rule;
    }


    public Object getAward() {
        return award;
    }

    public void setAward(Object award) {
        this.award = award;
    }
}
