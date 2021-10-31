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

package io.github.oasis.db.redis;

import io.github.oasis.core.collect.Pair;
import io.github.oasis.core.external.Mapped;
import io.github.oasis.core.external.PaginatedResult;
import org.redisson.RedissonKeys;
import org.redisson.api.RKeys;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.ByteArrayCodec;
import org.redisson.client.codec.DoubleCodec;
import org.redisson.client.codec.IntegerCodec;
import org.redisson.client.codec.LongCodec;
import org.redisson.client.codec.StringCodec;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static io.github.oasis.core.utils.Numbers.asDecimal;
import static io.github.oasis.core.utils.Numbers.asInt;
import static io.github.oasis.core.utils.Numbers.asLong;

/**
 * @author Isuru Weerarathna
 */
public class RedisHashSet implements Mapped {

    private final String baseKey;
    private final RedissonClient client;

    public RedisHashSet(RedissonClient redisson, String baseKey) {
        this.baseKey = baseKey;
        this.client = redisson;
    }

    @Override
    public Map<String, String> getAll() {
        RMap<String, String> map = client.getMap(baseKey, StringCodec.INSTANCE);
        return map.getAll(map.keySet());
    }

    @Override
    public boolean existKey(String key) {
        RMap<String, String> map = client.getMap(baseKey, StringCodec.INSTANCE);
        return map.containsKey(key);
    }

    @Override
    public String getValue(String key) {
        RMap<String, String> map = client.getMap(baseKey, StringCodec.INSTANCE);
        return map.get(key);
    }

    @Override
    public void setValue(String key, String value) {
        RMap<String, String> map = client.getMap(baseKey, StringCodec.INSTANCE);
        map.put(key, value);
    }

    @Override
    public void setValue(String key, byte[] data) {
        RMap<String, byte[]> map = client.getMap(baseKey, ByteArrayCodec.INSTANCE);
        map.put(key, data);
    }

    @Override
    public byte[] readValue(String key) {
        RMap<String, byte[]> map = client.getMap(baseKey, ByteArrayCodec.INSTANCE);
        return map.get(key);
    }

    @Override
    public void setValues(String... keyValuePairs) {
        RMap<String, String> map = client.getMap(baseKey, StringCodec.INSTANCE);
        for (int i = 0; i < keyValuePairs.length; i += 2) {
            map.put(keyValuePairs[i], keyValuePairs[i + 1]);
        }
    }

    @Override
    public BigDecimal setValueIfMax(String key, BigDecimal value) {
        RMap<String, String> map = client.getMap(baseKey, StringCodec.INSTANCE);
        String existVal = map.getOrDefault(key, "0");
        if (Objects.isNull(existVal) || value.compareTo(asDecimal(existVal)) > 0) {
            map.put(key, value.toString());
            return value;
        }
        return asDecimal(existVal);
    }

    @Override
    public long incrementBy(String key, long byValue) {
        RMap<String, Long> map = client.getMap(baseKey, LongCodec.INSTANCE);
        return asLong(map.addAndGet(key, byValue));
    }

    @Override
    public int incrementByInt(String key, int byValue) {
        RMap<String, Integer> map = client.getMap(baseKey, IntegerCodec.INSTANCE);
        return asInt(map.addAndGet(key, byValue));
    }

    @Override
    public BigDecimal incrementByDecimal(String key, BigDecimal byValue) {
        RMap<String, Double> map = client.getMap(baseKey, DoubleCodec.INSTANCE);
        return BigDecimal.valueOf(map.addAndGet(key, byValue.doubleValue()));
    }

    @Override
    public Mapped expireIn(long milliseconds) {
        RMap<String, String> map = client.getMap(baseKey, StringCodec.INSTANCE);
        map.expire(milliseconds, TimeUnit.MILLISECONDS);
        return this;
    }

    @Override
    public void remove(String key) {
        RMap<String, String> map = client.getMap(baseKey, StringCodec.INSTANCE);
        map.remove(key);
    }

    @Override
    public List<String> getValues(String... keys) {
        RMap<String, String> map = client.getMap(baseKey, StringCodec.INSTANCE);
        Map<String, String> subValues = map.getAll(Set.of(keys));
        List<String> vals = new LinkedList<>();
        for (String k : keys) {
            vals.add(subValues.get(k));
        }
        return vals;
    }

    @Override
    public boolean setIfNotExists(String key, String value) {
        RMap<String, String> map = client.getMap(baseKey, StringCodec.INSTANCE);
        return map.fastPutIfAbsent(key, value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public PaginatedResult<Pair<String, String>> search(String pattern, int count) {
        return search(pattern, count, null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public PaginatedResult<Pair<String, String>> search(String pattern, int count, String cursor) {
        // todo provide searcheable index using reddisson
        RMap<String, String> map = client.getMap(baseKey, StringCodec.INSTANCE);
        Iterator<String> iterator = map.keySet(pattern).iterator();
        List<Pair<String, String>> records = new LinkedList<>();
        for (int i = 0; i < count; i++) {
            if (!iterator.hasNext()) {
                break;
            }
            String key = iterator.next();
            records.add(Pair.of(key, map.get(key)));
        }
        return new PaginatedResult<>(null, records);
    }
}
