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
import io.github.oasis.core.collect.Record;
import io.github.oasis.core.external.Sorted;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;
import redis.clients.jedis.Tuple;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static io.github.oasis.core.utils.Numbers.isFirstOne;

/**
 * @author Isuru Weerarathna
 */
public class RedisSortedSet implements Sorted {

    private final Jedis jedis;
    private final String baseKey;
    private final byte[] baseKeyBytes;

    public RedisSortedSet(Jedis jedis, String baseKey) {
        this.jedis = jedis;
        this.baseKey = baseKey;
        this.baseKeyBytes = baseKey.getBytes(StandardCharsets.US_ASCII);
    }

    @Override
    public boolean add(String member, long value) {
        return isFirstOne(jedis.zadd(baseKey, value, member));
    }

    @Override
    public Pair<Long, Long> addAndGetRankSize(String member, long value) {
        Pipeline pipe = jedis.pipelined();
        pipe.zadd(baseKey, value, member);
        Response<Long> ranking = pipe.zrank(baseKey, member);
        Response<Long> total = pipe.zcard(baseKey);
        pipe.sync();
        return Pair.of(ranking.get(), total.get());
    }

    @Override
    public void add(byte[] member, long value) {
        jedis.zadd(baseKeyBytes, value, member);
    }

    @Override
    public void add(String member, double value) {
        jedis.zadd(baseKey, value, member);
    }

    @Override
    public void addRef(String member, long value, String refKey, String refValue) {
        jedis.zadd(baseKey, value, member);
        jedis.hset(refKey, member, refValue);
    }

    @Override
    public List<Record> getRangeByScoreWithScores(long from, long to) {
        return jedis.zrangeByScoreWithScores(baseKey, from, to)
                .stream()
                .map(tuple -> new Record(tuple.getElement(), tuple.getScore()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Record> getRangeByScoreWithScores(BigDecimal from, BigDecimal to) {
        return jedis.zrangeByScoreWithScores(baseKey, from.doubleValue(), to.doubleValue())
                .stream()
                .map(tuple -> new Record(tuple.getElement(), tuple.getScore()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Record> getRangeByRankWithScores(long from, long to) {
        return jedis.zrangeWithScores(baseKey, from, to)
                .stream()
                .map(tuple -> new Record(tuple.getElement(), tuple.getScore()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Record> getRevRangeByRankWithScores(long from, long to) {
        return jedis.zrevrangeWithScores(baseKey, from, to)
                .stream()
                .map(tuple -> new Record(tuple.getElement(), tuple.getScore()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Record> getRefRangeByRankWithScores(long from, long to, String refKey) {
        Set<Tuple> tuples = jedis.zrangeWithScores(baseKey, from, to);
        String[] memberIdArray = tuples.stream().map(Tuple::getElement).toArray(String[]::new);
        List<String> refValues = jedis.hmget(refKey, memberIdArray);
        Map<String, String> mapped = new HashMap<>();
        for (int i = 0; i < memberIdArray.length; i++) {
            mapped.put(memberIdArray[i], refValues.get(i));
        }
        return tuples.stream().map(t -> new Record(mapped.get(t.getElement()), t.getScore())).collect(Collectors.toList());
    }

    @Override
    public BigDecimal incrementScore(String member, BigDecimal byScore) {
        return BigDecimal.valueOf(jedis.zincrby(baseKey, byScore.doubleValue(), member));
    }

    @Override
    public Sorted expireIn(long milliseconds) {
        jedis.expire(baseKey, (int) Math.ceil(milliseconds / 1000.0));
        return this;
    }

    @Override
    public void removeRangeByScore(long from, long to) {
        jedis.zremrangeByScore(baseKey, from, to);
    }

    @Override
    public boolean memberExists(String member) {
        return !Objects.isNull(jedis.zscore(baseKey, member));
    }

    @Override
    public long getRank(String member) {
        return jedis.zrank(baseKey, member);
    }

    @Override
    public Optional<String> getMemberByScore(long score) {
        Set<Tuple> tuples = jedis.zrangeByScoreWithScores(baseKey, score, score);
        if (tuples.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(tuples.iterator().next().getElement());
        }
    }

    @Override
    public boolean remove(String member) {
        Long result = jedis.zrem(baseKey, member);
        return result != null && result > 0;
    }
}
