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

import java.io.Closeable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

/**
 * Basic interface to interact with engine database.
 *
 * @author Isuru Weerarathna
 */
public interface DbContext extends Closeable {

    boolean keyExists(String key);
    boolean mapKeyExists(String baseKey, String subKey);
    Set<String> allKeys(String pattern);
    void removeKey(String key);

    void setValueInMap(String contextKey, String field, String value);
    String getValueFromMap(String contextKey, String key);
    boolean removeKeyFromMap(String contextKey, String... keys);
    void addToSorted(String contextKey, String member, long value);
    boolean setIfNotExistsInMap(String contextKey, String key, String value);
    List<String> getValuesFromMap(String contextKey, String... keys);

    Sorted SORTED(String contextKey);

    Mapped MAP(String contextKey);

    void incrementAll(int value, String baseKey, List<String> keys);
    void incrementAll(BigDecimal value, String baseKey, List<String> keys);
    void incrementAllInSorted(BigDecimal value, String commonMember, List<String> baseKeys);
    BigDecimal incrementCapped(BigDecimal value, String baseKey, String childKey, BigDecimal limit);
    Object runScript(String scriptName, List<Object> keys, Object... values);

    void queueOffer(String listName, String data);
}
