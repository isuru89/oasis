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

package io.github.oasis.game.utils;


import io.github.oasis.model.Event;
import io.github.oasis.model.events.EventNames;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.mvel2.MVEL;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    private static final Pattern TIME_PATTERN = Pattern.compile("([0-9]+)\\s*([a-zA-Z]+)");
    private static final Pattern NUMBER_PATTERN = Pattern.compile("([0-9\\\\.]+)\\s*([kKmMbB]?)");

    public static <T> T firstNonNull(T v1, T v2) {
        if (v1 != null) {
            return v1;
        } else {
            return v2;
        }
    }

    public static <T> T orDefault(T v1, T v2) {
        return firstNonNull(v1, v2);
    }

    public static boolean eventEquals(Event against, String eventType) {
        return eventType.equals(against.getEventType());
    }

    public static String queueReplace(String name) {
        return name.replace("{gid}", System.getProperty(Constants.ENV_OASIS_GAME_ID, ""));
    }

    public static boolean isEndOfStream(Event event) {
        return Utils.eventEquals(event, EventNames.TERMINATE_GAME);
    }

    public static boolean isNonEmpty(Collection<?> list) {
        return list != null && !list.isEmpty();
    }

    public static boolean isNullOrEmpty(Collection<?> list) {
        return list == null || list.isEmpty();
    }

    public static boolean isNullOrEmpty(String text) {
        return text == null || text.trim().isEmpty();
    }

    public static Serializable compileExpression(String expr) {
        return MVEL.compileExpression(expr);
    }

    public static boolean evaluateCondition(Serializable expression, Map<String, Object> variables) throws IOException {
        Object result = executeExpression(expression, variables);
        if (result == null) {
            throw new IOException("Expression does not return anything after evaluation! Make sure 'return' is specified.");
        }
        return (Boolean) result;
    }

    public static boolean evaluateConditionSafe(Serializable expression, Map<String, Object> variables) {
        Object result = executeExpression(expression, variables);
        if (result == null) {
            return false;
        }
        return (Boolean) result;
    }

    public static Object executeExpression(Serializable expression, Map<String, Object> variables) {
        return MVEL.executeExpression(expression, variables);
    }

    public static double toDouble(Number number) {
        return BigDecimal.valueOf(number.doubleValue()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    public static Double strNum(String numStr) {
        Matcher matcher = NUMBER_PATTERN.matcher(numStr);
        if (matcher.find()) {
            double val = Double.parseDouble(matcher.group(1));
            String unit = matcher.group(2).toLowerCase();
            if (unit.startsWith("k")) {
                return val * 1000;
            } else if (unit.startsWith("m")) {
                return val * 1000 * 1000;
            } else if (unit.startsWith("b")) {
                return val * 1000 * 1000 * 1000;
            } else {
                return val;
            }
        }
        throw new IllegalArgumentException("Given number is not in the correct format! [" + numStr + "]");
    }

    public static Time fromStr(String durationStr) {
        Matcher matcher = TIME_PATTERN.matcher(durationStr);
        if (matcher.find()) {
            int val = Integer.parseInt(matcher.group(1));
            String unit = matcher.group(2).toLowerCase();
            if (unit.startsWith("d") || unit.startsWith("b")) {
                return Time.days(val);
            } else if (unit.startsWith("s")) {
                return Time.seconds(val);
            } else if (unit.startsWith("min")) {
                return Time.minutes(val);
            } else if (unit.startsWith("h")) {
                return Time.hours(val);
            } else if (unit.startsWith("m")) {
                return Time.days(val * 30L);
            }
        }
        throw new IllegalArgumentException("Given duration string found to be invalid format! [" + durationStr + "]");
    }

    public static boolean isDurationBusinessDaysOnly(String durationStr) {
        Matcher matcher = TIME_PATTERN.matcher(durationStr);
        if (matcher.find()) {
            String unit = matcher.group(2).toLowerCase();
            return unit.startsWith("b");
        }
        return false;
    }

    public static double asDouble(Object val) {
        if (val == null) {
            return 0.0;
        } else if (val instanceof Number) {
            return ((Number) val).doubleValue();
        } else {
            try {
                return Double.parseDouble(String.valueOf(val));
            } catch (NumberFormatException nfe) {
                return 0.0;
            }
        }
    }

    public static Long asLong(Object val) {
        if (val == null) {
            return null;
        } else if (val instanceof Number) {
            return ((Number) val).longValue();
        } else {
            try {
                return Long.parseLong(String.valueOf(val));
            } catch (NumberFormatException nfe) {
                return null;
            }
        }
    }

//    public static Event getLatestEvent(Event e1, Event e2) {
//        if (e1 == null) {
//            if (e2 == null) {
//                return null;
//            } else {
//                return e2;
//            }
//        } else {
//            if (e2 == null) {
//                return e1;
//            } else {
//                if (e1.getTimestamp() > e1.getTimestamp()) {
//                    return e1;
//                } else {
//                    return e2;
//                }
//            }
//        }
//    }
}
