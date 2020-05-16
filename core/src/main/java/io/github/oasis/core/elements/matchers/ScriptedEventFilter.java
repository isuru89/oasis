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

package io.github.oasis.core.elements.matchers;

import io.github.oasis.core.Event;
import io.github.oasis.core.context.ExecutionContext;
import io.github.oasis.core.elements.AbstractRule;
import io.github.oasis.core.elements.EventExecutionFilter;
import org.mvel2.MVEL;

import java.io.Serializable;
import java.util.Map;

/**
 * Event filter using a provided MVEL script.
 *
 * @author Isuru Weerarathna
 */
public class ScriptedEventFilter implements EventExecutionFilter {

    private final Serializable compiledExpression;

    private ScriptedEventFilter(Serializable compiledExpression) {
        this.compiledExpression = compiledExpression;
    }

    public static ScriptedEventFilter create(String scriptText) {
        return new ScriptedEventFilter(MVEL.compileExpression(scriptText));
    }

    @Override
    public boolean matches(Event event, AbstractRule rule, ExecutionContext context) {
        Map<String, Serializable> variables = Map.of("e", event, "rule", rule, "ctx", context);
        Object result = MVEL.executeExpression(compiledExpression, variables);
        if (result instanceof Boolean) {
            return (boolean) result;
        }
        return false;
    }
}
