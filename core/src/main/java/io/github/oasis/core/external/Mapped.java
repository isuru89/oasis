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

package io.github.oasis.core.external;

import io.github.oasis.core.utils.Numbers;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author Isuru Weerarathna
 */
public interface Mapped {

    Map<String, String> getAll();

    boolean existKey(String key);

    String getValue(String key);
    default int getValueAsInt(String key) {
        return Numbers.asInt(getValue(key));
    }

    void setValue(String key, String value);
    default void setValue(String key, long value) {
        setValue(key, String.valueOf(value));
    }
    default void setValue(String key, int value) {
        setValue(key, String.valueOf(value));
    }
    void setValue(String key, byte[] data);
    byte[] readValue(String key);

    void setValues(String... keyValuePairs);

    BigDecimal setValueIfMax(String key, BigDecimal value);

    long incrementBy(String key, long byValue);
    int incrementByInt(String key, int byValue);
    default int incrementByOne(String key) {
        return incrementByInt(key, 1);
    }
    default int decrementByOne(String key) {
        return incrementByInt(key, -1);
    }
    BigDecimal incrementByDecimal(String key, BigDecimal byValue);

    Mapped expireIn(long milliseconds);

    void remove(String key);

    List<String> getValues(String... keys);

    <T> PaginatedResult<T> search(String pattern, int count, String cursor);
    <T> PaginatedResult<T> search(String pattern, int count);

    /**
     * Returns true if key did not exist and newly created.
     *
     * @param key
     * @param value
     * @return if the key has been created newly.
     */
    boolean setIfNotExists(String key, String value);

}
