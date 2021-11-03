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

package io.github.oasis.core.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @author Isuru Weerarathna
 */
public final class Numbers {

    private static final String ZERO = "0";

    public static boolean isNegative(BigDecimal value) {
        return value.signum() < 0;
    }

    public static boolean isZero(String value) {
        return ZERO.equals(value);
    }

    public static boolean isFirstOne(Long value) {
        return value != null && value == 1;
    }

    public static boolean isIncreasedOrEqual(BigDecimal prev, BigDecimal now) {
        return now.compareTo(prev) >= 0;
    }

    public static boolean isThresholdCrossedUp(BigDecimal prev, BigDecimal now, BigDecimal threshold) {
        return now.compareTo(threshold) >= 0 && prev.compareTo(threshold) < 0;
    }

    public static boolean isThresholdCrossedDown(BigDecimal prev, BigDecimal now, BigDecimal threshold) {
        return now.compareTo(threshold) < 0 && prev.compareTo(threshold) >= 0;
    }

    public static int asInt(boolean value) {
        return value ? 1 : 0;
    }

    public static int asInt(String value) {
        return value == null ? 0 : Integer.parseInt(value);
    }

    public static int asInt(Long value) {
        return value == null ? 0 : value.intValue();
    }

    public static int asInt(Integer value) {
        return value == null ? 0 : value;
    }

    public static long asLong(String value) {
        return value == null ? 0 : Long.parseLong(value);
    }

    public static long asLong(Long value) {
        return value == null ? 0 : value;
    }

    public static BigDecimal addToScale(BigDecimal num1, BigDecimal num2, int scale) {
        return num1.add(num2).setScale(scale, RoundingMode.HALF_UP);
    }

    public static BigDecimal asDecimal(Double val) {
        return val == null ? BigDecimal.ZERO : BigDecimal.valueOf(val);
    }

    public static BigDecimal asDecimal(String val) {
        return val == null ? BigDecimal.ZERO : new BigDecimal(val);
    }

    public static BigDecimal asDecimal(Object val) {
        if (val == null) {
            return BigDecimal.ZERO;
        }

        if (val instanceof Number) {
            return BigDecimal.valueOf(((Number) val).doubleValue());
        } else if (val instanceof String) {
            return asDecimal((String) val);
        }
        return null;
    }

    public static int ifNull(Integer val, int defaultValue) {
        return val == null ? defaultValue : val;
    }

    public static long ifNull(Long val, long defaultValue) {
        return val == null ? defaultValue : val;
    }
}
