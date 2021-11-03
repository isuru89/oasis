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
import org.redisson.api.RMap;
import org.redisson.api.RQueue;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.DoubleCodec;
import org.redisson.client.codec.IntegerCodec;
import org.redisson.client.codec.StringCodec;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Isuru Weerarathna
 */
public class RedisContext implements DbContext {

    private static final String INCRALL = "O.INCRALL";
    private static final String ZINCRALL = "O.ZINCRALL";
    private static final String HINCRCAPPED = "O.INCRCAPPED";

    private RedissonClient client;
    private RedisDb db;

    public RedisContext(RedisDb db, RedissonClient redissonClient) {
        this.db = db;
        this.client = redissonClient;
    }

    @Override
    public void close() {
        client = null;
        db = null;
    }

    @Override
    public boolean keyExists(String key) {
        return client.getBucket(key).isExists();
    }

    @Override
    public boolean mapKeyExists(String baseKey, String subKey) {
        return client.getMap(baseKey, StringCodec.INSTANCE).containsKey(subKey);
    }

    @Override
    public Set<String> allKeys(String pattern) {
        Set<String> keys = new HashSet<>();
        for (String key : client.getKeys().getKeysByPattern(pattern)) {
            keys.add(key);
        }
        return keys;
    }

    @Override
    public void removeKey(String key) {
        client.getBucket(key).delete();
    }

    @Override
    public void setValueInMap(String contextKey, String field, String value) {
        client.getMap(contextKey, StringCodec.INSTANCE).fastPut(field, value);
    }

    @Override
    public String getValueFromMap(String contextKey, String key) {
        RMap<String, String> map = client.getMap(contextKey, StringCodec.INSTANCE);
        return map.get(key);
    }

    @Override
    public boolean removeKeyFromMap(String contextKey, String... keys) {
        RMap<String, Object> map = client.getMap(contextKey, StringCodec.INSTANCE);
        return map.fastRemove(keys) > 0;
    }

    @Override
    public void addToSorted(String contextKey, String member, long value) {
        RScoredSortedSet<String> scoredSortedSet = client.getScoredSortedSet(contextKey, StringCodec.INSTANCE);
        scoredSortedSet.add(value, member);
    }

    @Override
    public boolean setIfNotExistsInMap(String contextKey, String key, String value) {
        RMap<String, String> map = client.getMap(contextKey, StringCodec.INSTANCE);
        return map.putIfAbsent(key, value) == null;
    }

    @Override
    public List<String> getValuesFromMap(String contextKey, String... keys) {
        return MAP(contextKey).getValues(keys);
    }

    @Override
    public Sorted SORTED(String contextKey) {
        return new RedisSortedSet(client, contextKey);
    }

    @Override
    public Mapped MAP(String contextKey) {
        return new RedisHashSet(client, contextKey);
    }

    @Override
    public void incrementAll(int value, String baseKey, List<String> keys) {
        List<Object> allArgs = new ArrayList<>();
        allArgs.add(baseKey);
        allArgs.addAll(keys);
        runScript(INCRALL, allArgs, value);
    }

    @Override
    public void incrementAll(BigDecimal value, String baseKey, List<String> subKeys) {
        List<Object> allArgs = List.of(baseKey);
        Object[] values = new Object[subKeys.size() + 1];
        values[0] = value.doubleValue();
        for (int i = 0; i < subKeys.size(); i++) {
            values[i + 1] = subKeys.get(i);
        }
        runScript(INCRALL, allArgs, values);
    }

    @Override
    public void queueOffer(String listName, String data) {
        RQueue<String> queue = client.getQueue(listName, StringCodec.INSTANCE);
        queue.add(data);
    }

    @Override
    public void incrementAllInSorted(BigDecimal value, String commonMember, List<String> baseKeys) {
        List<Object> allArgs = List.copyOf(baseKeys);
        runScript(ZINCRALL, allArgs, value.doubleValue(), commonMember);
    }

    @Override
    public BigDecimal incrementCapped(BigDecimal value, String baseKey, String childKey, BigDecimal limit) {
        List<Object> allArgs = List.of(baseKey);
        Object result = runScript(HINCRCAPPED, allArgs, value, limit, childKey);
        return Numbers.asDecimal(result.toString());
    }

    @Override
    public Object runScript(String scriptName, List<Object> keys, Object... values) {
        RedisDb.RedisScript scriptSha = db.getScriptSha(scriptName);
        RScript script = createScriptInstance(scriptSha);
        return script.evalSha(
                scriptSha.isReadOnly() ? RScript.Mode.READ_ONLY : RScript.Mode.READ_WRITE,
                scriptSha.getSha(),
                deriveScriptReturnType(scriptSha),
                keys,
                values);
    }

    private RScript createScriptInstance(RedisDb.RedisScript redisScript) {
        if ("int".equals(redisScript.getReturnType())) {
            return client.getScript(IntegerCodec.INSTANCE);
        } else if ("double".equals(redisScript.getReturnType())) {
            return client.getScript(DoubleCodec.INSTANCE);
        } else {
            return client.getScript(StringCodec.INSTANCE);
        }
    }

    private RScript.ReturnType deriveScriptReturnType(RedisDb.RedisScript redisScript) {
        if ("list".equals(redisScript.getReturnType())) {
            return RScript.ReturnType.MULTI;
        } else if ("int".equals(redisScript.getReturnType())) {
            return RScript.ReturnType.INTEGER;
        } else {
            return RScript.ReturnType.VALUE;
        }
    }
}
