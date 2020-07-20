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

import io.github.oasis.core.elements.TimeRangeMatcher;
import io.github.oasis.core.utils.Timestamps;
import org.mvel2.MVEL;
import org.mvel2.ParserConfiguration;
import org.mvel2.ParserContext;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.Month;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

import static io.github.oasis.core.VariableNames.EVENT_TS_VAR;

/**
 * @author Isuru Weerarathna
 */
public class ScriptedTimeMatcher implements TimeRangeMatcher {

    private static final String JAVA_TIME_PACKAGE = "java.time";

    private final Serializable expression;

    private ScriptedTimeMatcher(Serializable expression) {
        this.expression = expression;
    }

    public static ScriptedTimeMatcher create(String scriptText) {
        ParserConfiguration configuration = new ParserConfiguration();
        configuration.addPackageImport(JAVA_TIME_PACKAGE);
        configuration.addPackageImport(DayOfWeek.class.getName());
        configuration.addPackageImport(Month.class.getName());
        ParserContext context = new ParserContext(configuration);
        return new ScriptedTimeMatcher(MVEL.compileExpression(scriptText, context));
    }

    @Override
    public boolean isBetween(long timeMs, String timeZone) {
        ZonedDateTime userTime = Timestamps.getUserSpecificTime(timeMs, timeZone);
        Map<String, Object> vars = new HashMap<>();
        vars.put(EVENT_TS_VAR, userTime);
        Object result = MVEL.executeExpression(expression, vars);
        if (result != null && Boolean.class.isAssignableFrom(result.getClass())) {
            return (Boolean) result;
        }
        return false;
    }
}
