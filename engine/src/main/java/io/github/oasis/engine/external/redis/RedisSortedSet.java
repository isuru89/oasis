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

import io.github.oasis.engine.model.Record;
import io.github.oasis.engine.external.Sorted;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Tuple;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Isuru Weerarathna
 */
public class RedisSortedSet implements Sorted {

    private final Jedis jedis;
    private final String baseKey;

    public RedisSortedSet(Jedis jedis, String baseKey) {
        this.jedis = jedis;
        this.baseKey = baseKey;
    }

    @Override
    public void add(String member, long value) {
        jedis.zadd(baseKey, value, member);
    }

    @Override
    public void add(String member, double value) {
        jedis.zadd(baseKey, value, member);
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
    public void remove(String member) {
        jedis.zrem(baseKey, member);
    }
}
