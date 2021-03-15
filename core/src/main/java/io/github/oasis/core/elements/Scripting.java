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

package io.github.oasis.core.elements;

import io.github.oasis.core.Event;
import org.mvel2.MVEL;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static io.github.oasis.core.VariableNames.EVENT_VAR;

/**
 * @author Isuru Weerarathna
 */
public class Scripting {

    public static <I extends Serializable, J extends Serializable> EventBiValueResolver<I, J> create(String scriptText,
                                                                                                     String firstParamName,
                                                                                                     String secondParamName) {
        return new ScriptedBiValueResolver<>(MVEL.compileExpression(scriptText), firstParamName, secondParamName);
    }

    public static <I extends Serializable> EventValueResolver<I> create(String scriptText, String paramName) {
        return new ScriptedValueResolver<>(MVEL.compileExpression(scriptText), paramName);
    }

    private static class ScriptedValueResolver<I extends Serializable> implements EventValueResolver<I> {

        private final Serializable compiledExpression;
        private final String paramName;

        private ScriptedValueResolver(Serializable compiledExpression, String paramName) {
            this.compiledExpression = compiledExpression;
            this.paramName = paramName;
        }

        @Override
        public BigDecimal resolve(Event event, I input) {
            Map<String, Object> vars = new HashMap<>();
            vars.put(EVENT_VAR , (event != null) ? event.getAllFieldValues() : null);
            vars.put(paramName, input);
            Object result = MVEL.executeExpression(compiledExpression, vars);
            if (result instanceof BigDecimal) {
                return (BigDecimal)result;
            } else if (result instanceof Number) {
                return BigDecimal.valueOf(((Number)result).doubleValue());
            }
            return BigDecimal.ZERO;
        }
    }

    private static class ScriptedBiValueResolver<I extends Serializable, J extends Serializable> implements EventBiValueResolver<I, J> {

        private final Serializable compiledExpression;
        private final String firstParamName;
        private final String secondParamName;

        private ScriptedBiValueResolver(Serializable compiledExpression, String firstParamName, String secondParamName) {
            this.compiledExpression = compiledExpression;
            this.firstParamName = firstParamName;
            this.secondParamName = secondParamName;
        }

        @Override
        public BigDecimal resolve(Event event, I input, J otherInput) {
            Map<String, Object> vars = new HashMap<>();
            vars.put(EVENT_VAR, event.getAllFieldValues());
            vars.put(firstParamName, input);
            vars.put(secondParamName, otherInput);
            Object result = MVEL.executeExpression(compiledExpression, vars);
            if (result instanceof BigDecimal) {
                return (BigDecimal)result;
            } else if (result instanceof Number) {
                return BigDecimal.valueOf(((Number)result).doubleValue());
            }
            return BigDecimal.ZERO;
        }
    }

}
