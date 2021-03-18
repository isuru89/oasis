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

import io.github.oasis.core.external.DbContext;
import io.github.oasis.core.external.Mapped;
import io.github.oasis.core.external.Sorted;
import io.github.oasis.core.utils.Numbers;
import redis.clients.jedis.Jedis;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Isuru Weerarathna
 */
public class RedisContext implements DbContext {

    private static final String INCRALL = "O.INCRALL";
    private static final String ZINCRALL = "O.ZINCRALL";
    private static final String HINCRCAPPED = "O.INCRCAPPED";

    private static final int TWO_KEYS = 2;

    private final Jedis jedis;
    private final RedisDb db;

    public RedisContext(RedisDb db, Jedis jedis) {
        this.db = db;
        this.jedis = jedis;
    }

    @Override
    public void close() {
        if (jedis != null) {
            jedis.close();
        }
    }

    @Override
    public boolean keyExists(String key) {
        return jedis.exists(key);
    }

    @Override
    public boolean mapKeyExists(String baseKey, String subKey) {
        return jedis.hexists(baseKey, subKey);
    }

    @Override
    public Set<String> allKeys(String pattern) {
        return jedis.keys(pattern);
    }

    @Override
    public void removeKey(String key) {
        jedis.del(key);
    }

    @Override
    public BigDecimal incrementScoreInSorted(String contextKey, String member, BigDecimal byScore) {
        return BigDecimal.valueOf(jedis.zincrby(contextKey, byScore.doubleValue(), member));
    }

    @Override
    public void setValueInMap(String contextKey, String field, String value) {
        jedis.hset(contextKey, field, value);
    }

    @Override
    public void setRawValueInMap(String contextKey, String field, byte[] value) {
        jedis.hset(contextKey.getBytes(StandardCharsets.US_ASCII), field.getBytes(StandardCharsets.US_ASCII), value);
    }

    @Override
    public String getValueFromMap(String contextKey, String key) {
        return jedis.hget(contextKey, key);
    }

    @Override
    public byte[] getValueFromMap(String contextKey, byte[] key) {
        return jedis.hget(contextKey.getBytes(StandardCharsets.US_ASCII), key);
    }

    @Override
    public List<byte[]> getRawValuesFromMap(String contextKey, String... keys) {
        List<byte[]> listKeys = new ArrayList<>();
        for (String k : keys) {
            listKeys.add(k.getBytes(StandardCharsets.US_ASCII));
        }
        return jedis.hmget(contextKey.getBytes(StandardCharsets.US_ASCII),
                listKeys.toArray(new byte[0][]));
    }

    @Override
    public boolean removeKeyFromMap(String contextKey, String... keys) {
        return Numbers.asLong(jedis.hdel(contextKey, keys)) > 0;
    }

    @Override
    public void addToSorted(String contextKey, String member, long value) {
        jedis.zadd(contextKey, value, member);
    }

    @Override
    public boolean setIfNotExistsInMap(String contextKey, String key, String value) {
        return Numbers.isFirstOne(jedis.hsetnx(contextKey, key, value));
    }

    @Override
    public List<String> getValuesFromMap(String contextKey, String... keys) {
        return jedis.hmget(contextKey, keys);
    }

    @Override
    public Sorted SORTED(String contextKey) {
        return new RedisSortedSet(jedis, contextKey);
    }

    @Override
    public Mapped MAP(String contextKey) {
        return new RedisHashSet(jedis, contextKey);
    }

    @Override
    public void incrementAll(int value, String baseKey, List<String> keys) {
        incrementAll(String.valueOf(value), baseKey, keys);
    }

    @Override
    public void incrementAll(BigDecimal value, String baseKey, List<String> keys) {
        incrementAll(value.toString(), baseKey, keys);
    }

    @Override
    public void queueOffer(String listName, String data) {
        jedis.rpush(listName, data);
    }

    @Override
    public List<String> queuePoll(String listName, int timeOut) {
        if (timeOut >= 0) {
            return jedis.blpop(timeOut, listName);
        } else {
            if (jedis.exists(listName)) {
                return List.of(jedis.lpop(listName));
            }
            return List.of();
        }
    }

    @Override
    public void incrementAllInSorted(BigDecimal value, String commonMember, List<String> baseKeys) {
        List<String> allArgs = new ArrayList<>();
        allArgs.add(commonMember);
        allArgs.addAll(baseKeys);
        allArgs.add(value.toString());
        String[] args = allArgs.toArray(new String[0]);
        runScript(ZINCRALL, args.length - 1, args);
    }

    @Override
    public BigDecimal incrementCapped(BigDecimal value, String baseKey, String childKey, BigDecimal limit) {
        List<String> allArgs = new ArrayList<>();
        allArgs.add(baseKey);
        allArgs.add(childKey);
        allArgs.add(value.toString());
        allArgs.add(limit.toString());
        String[] args = allArgs.toArray(new String[0]);
        Object result = runScript(HINCRCAPPED, TWO_KEYS, args);
        return Numbers.asDecimal(result.toString());
    }

    private void incrementAll(String value, String baseKey, List<String> keys) {
        List<String> allArgs = new ArrayList<>();
        allArgs.add(baseKey);
        allArgs.addAll(keys);
        allArgs.add(value);
        String[] args = allArgs.toArray(new String[0]);
        runScript(INCRALL, args.length - 1, args);
    }

    @Override
    public Object runScript(String scriptName, int noOfKeys, String... args) {
        RedisDb.RedisScript scriptSha = db.getScriptSha(scriptName);
        return jedis.evalsha(scriptSha.getSha(), noOfKeys, args);
    }
}
