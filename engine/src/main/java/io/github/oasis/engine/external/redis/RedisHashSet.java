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

package io.github.oasis.engine.external.redis;

import io.github.oasis.core.external.Mapped;
import io.github.oasis.core.utils.Numbers;
import redis.clients.jedis.Jedis;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static io.github.oasis.core.utils.Numbers.asDecimal;
import static io.github.oasis.core.utils.Numbers.asInt;
import static io.github.oasis.core.utils.Numbers.asLong;

/**
 * @author Isuru Weerarathna
 */
public class RedisHashSet implements Mapped {

    private final String baseKey;
    private final Jedis jedis;

    public RedisHashSet(Jedis jedis, String baseKey) {
        this.baseKey = baseKey;
        this.jedis = jedis;
    }

    @Override
    public Map<String, String> getAll() {
        return jedis.hgetAll(baseKey);
    }

    @Override
    public String getValue(String key) {
        return jedis.hget(baseKey, key);
    }

    @Override
    public void setValue(String key, String value) {
        jedis.hset(baseKey, key, value);
    }

    @Override
    public void setValue(String key, byte[] data) {
        jedis.hset(baseKey.getBytes(StandardCharsets.US_ASCII), key.getBytes(StandardCharsets.US_ASCII), data);
    }

    @Override
    public byte[] readValue(String key) {
        return jedis.hget(baseKey.getBytes(StandardCharsets.US_ASCII), key.getBytes(StandardCharsets.US_ASCII));
    }

    @Override
    public void setValues(String... keyValuePairs) {
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < keyValuePairs.length; i += 2) {
            map.put(keyValuePairs[i], keyValuePairs[i + 1]);
        }
        jedis.hmset(baseKey, map);
    }

    @Override
    public BigDecimal setValueIfMax(String key, BigDecimal value) {
        String existVal = jedis.hget(baseKey, key);
        if (Objects.isNull(existVal) || value.compareTo(asDecimal(existVal)) > 0) {
            jedis.hset(baseKey, key, value.toString());
            return value;
        }
        return asDecimal(existVal);
    }

    @Override
    public long incrementBy(String key, long byValue) {
        return asLong(jedis.hincrBy(baseKey, key, byValue));
    }

    @Override
    public int incrementByInt(String key, int byValue) {
        return asInt(jedis.hincrBy(baseKey, key, byValue));
    }

    @Override
    public BigDecimal incrementByDecimal(String key, BigDecimal byValue) {
        return BigDecimal.valueOf(jedis.hincrByFloat(baseKey, key, byValue.doubleValue()));
    }

    @Override
    public Mapped expireIn(long milliseconds) {
        jedis.expire(baseKey, (int) Math.ceil(milliseconds / 1000.0));
        return this;
    }

    @Override
    public void remove(String key) {
        jedis.hdel(baseKey, key);
    }

    @Override
    public List<String> getValues(String... keys) {
        return jedis.hmget(baseKey, keys);
    }

    @Override
    public boolean setIfNotExists(String key, String value) {
        return Numbers.isFirstOne(jedis.hsetnx(baseKey, key, value));
    }
}
