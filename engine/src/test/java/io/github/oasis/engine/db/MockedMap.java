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

import io.github.oasis.engine.external.Mapped;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Isuru Weerarathna
 */
public class MockedMap implements Mapped {

    private Map<String, String> data = new ConcurrentHashMap<>();

    @Override
    public Map<String, String> getAll() {
        return data;
    }

    @Override
    public String getValue(String key) {
        return data.get(key);
    }

    @Override
    public void setValue(String key, String value) {
        data.put(key, value);
    }

    @Override
    public void setValues(String... keyValuePairs) {
        for (int i = 0; i < keyValuePairs.length; i+=2) {
            data.put(keyValuePairs[i], keyValuePairs[i+1]);
        }
    }

    @Override
    public long incrementBy(String key, long byValue) {
        BigDecimal add = new BigDecimal(data.getOrDefault(key, "0")).add(BigDecimal.valueOf(byValue));
        data.put(key, add.toString());
        return add.longValue();
    }

    @Override
    public int incrementByInt(String key, int byValue) {
        BigDecimal add = new BigDecimal(data.getOrDefault(key, "0")).add(BigDecimal.valueOf(byValue));
        data.put(key, add.toString());
        return add.intValue();
    }

    @Override
    public BigDecimal incrementByDecimal(String key, BigDecimal byValue) {
        BigDecimal add = new BigDecimal(data.getOrDefault(key, "0")).add(byValue);
        data.put(key, add.toString());
        return add;
    }

    @Override
    public void remove(String key) {
        data.remove(key);
    }

    @Override
    public List<String> getValues(String... keys) {
        List<String> vals = new ArrayList<>();
        for (Map.Entry<String, String> stringStringEntry : data.entrySet()) {
            vals.add(stringStringEntry.getValue());
        }
        return vals;
    }

    @Override
    public boolean setIfNotExists(String key, String value) {
        if (data.containsKey(key)) {
            return false;
        } else {
            data.put(key, value);
            return true;
        }
    }
}
