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
import io.github.oasis.core.context.ExecutionContext;
import io.github.oasis.core.elements.matchers.ScriptedEventFilter;
import io.github.oasis.core.elements.spec.EventFilterDef;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

/**
 * @author Isuru Weerarathna
 */
public class EventExecutionFilterFactory {

    public static final EventExecutionFilter ALWAYS_TRUE = new AlwaysTrueEventFilter();

    private static class AlwaysTrueEventFilter implements EventExecutionFilter {

        @Override
        public boolean matches(Event event, AbstractRule rule, ExecutionContext ctx) {
            return true;
        }
    }

    public static EventExecutionFilter create(EventFilterDef filterDef) {
        if (Objects.isNull(filterDef)) {
            return ALWAYS_TRUE;
        }

        if (StringUtils.isNotBlank(filterDef.getExpression())) {
            return ScriptedEventFilter.create(filterDef.getExpression());
        }
        throw new IllegalArgumentException("Unsupported event filter specification!");
    }

    public static EventExecutionFilter create(Object source) {
        if (Objects.isNull(source)) {
            return ALWAYS_TRUE;
        }

        if (source instanceof String) {
            return ScriptedEventFilter.create((String)source);
        }
        throw new IllegalArgumentException("Unknown event filter type!");
    }

}
