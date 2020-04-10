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

package io.github.oasis.services.utils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Commons {

    public static boolean isNullOrEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    public static boolean isNullOrEmpty(Collection<?> list) {
        return list == null || list.isEmpty();
    }

    public static boolean isNullOrEmpty(String text) {
        return text == null || text.trim().isEmpty();
    }

    public static double asDouble(Object val) {
        if (val == null) {
            return Double.NaN;
        } else if (val instanceof Number) {
            return ((Number) val).doubleValue();
        } else {
            try {
                return Double.parseDouble(val.toString());
            } catch (NumberFormatException e) {
                return Double.NaN;
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
                return Long.parseLong(val.toString());
            } catch (NumberFormatException e) {
                return null;
            }
        }
    }

    public static String fixSearchQuery(String param) {
        if (param == null) {
            return null;
        }
        return param.replace("!", "!!")
                .replace("%", "!%")
                .replace("_", "!_")
                .replace("[", "![");
    }

    public static <T> T orDefault(T value, T defValue) {
        if (value == null) {
            return defValue;
        } else {
            return value;
        }
    }

    public static <T> T firstNonNull(T... items) {
        if (items == null) {
            return null;
        }
        for (T item : items) {
            if (item != null) {
                return item;
            }
        }
        return null;
    }

    public static <T> Stream<List<T>> batches(List<T> source, int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("length must be a valid! [" + length + "]");
        }
        int size = source.size();
        if (size <= 0) {
            return Stream.empty();
        }
        int fullChunks = (size - 1) / length;
        return IntStream.range(0, fullChunks + 1).mapToObj(
                n -> source.subList(n * length, n == fullChunks ? size : (n + 1) * length));
    }
}
