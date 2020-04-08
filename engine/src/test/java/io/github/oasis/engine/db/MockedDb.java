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

package io.github.oasis.engine.db;

import io.github.oasis.engine.external.DbContext;
import io.github.oasis.engine.external.Mapped;
import io.github.oasis.engine.external.Sorted;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Isuru Weerarathna
 */
public class MockedDb implements DbContext {

    private Map<String, Object> map = new ConcurrentHashMap<>();

    @Override
    public Set<String> allKeys(String pattern) {
        return map.keySet();
    }

    @Override
    public void removeKey(String key) {
        map.remove(key);
    }

    @Override
    public void setValueInMap(String contextKey, String field, String value) {
        MAP(contextKey).setValue(field, value);
    }

    @Override
    public String getValueFromMap(String contextKey, String key) {
        return MAP(contextKey).getValue(key);
    }

    @Override
    public void addToSorted(String contextKey, String member, long value) {

    }

    @Override
    public boolean setIfNotExistsInMap(String contextKey, String key, String value) {
        return MAP(contextKey).setIfNotExists(key, value);
    }

    @Override
    public List<String> getValuesFromMap(String contextKey, String... keys) {
        return MAP(contextKey).getValues(keys);
    }

    @Override
    public Sorted SORTED(String contextKey) {
        Object o = map.computeIfAbsent(contextKey, k -> new MockedSorted());
        if (o instanceof Sorted) {
            return (Sorted) o;
        } else {
            throw new IllegalStateException("Given key does have a different data structure than a sorted map!");
        }
    }

    @Override
    public Mapped MAP(String contextKey) {
        Object o = map.computeIfAbsent(contextKey, k -> new MockedMap());
        if (o instanceof Mapped) {
            return (Mapped) o;
        } else {
            throw new IllegalStateException("Given key does have a different data structure than a map!");
        }
    }

    @Override
    public void close() {

    }
}
