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
import org.redisson.api.RBatch;
import org.redisson.api.RMap;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RScoredSortedSetAsync;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.redisson.client.protocol.ScoredEntry;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Redis sorted set implementation.
 *
 * @author Isuru Weerarathna
 */
public class RedisSortedSet implements Sorted {

    private static final boolean INCLUSIVE = true;

    private final RedissonClient client;
    private final String baseKey;

    public RedisSortedSet(RedissonClient client, String baseKey) {
        this.client = client;
        this.baseKey = baseKey;
    }

    @Override
    public boolean add(String member, long value) {
        RScoredSortedSet<String> scoredSortedSet = client.getScoredSortedSet(baseKey, StringCodec.INSTANCE);
        return scoredSortedSet.add(value, member);
    }

    @Override
    public Pair<Long, Long> addAndGetRankSize(String member, long value) {
        RBatch batch = client.createBatch();
        RScoredSortedSetAsync<String> scoredSortedSet = batch.getScoredSortedSet(baseKey, StringCodec.INSTANCE);
        scoredSortedSet.addAsync(value, member);
        scoredSortedSet.rankAsync(member);
        scoredSortedSet.sizeAsync();
        List<?> responses = batch.execute().getResponses();
        return Pair.of(((Integer) responses.get(1)).longValue(),
                ((Integer) responses.get(2)).longValue());
    }

    @Override
    public void add(String member, double value) {
        RScoredSortedSet<String> scoredSortedSet = client.getScoredSortedSet(baseKey, StringCodec.INSTANCE);
        scoredSortedSet.add(value, member);
    }

    @Override
    public void addRef(String member, long value, String refKey, String refValue) {
        add(member, value);
        RMap<String, String> map = client.getMap(refKey, StringCodec.INSTANCE);
        map.put(member, refValue);
    }

    @Override
    public List<Record> getRangeByScoreWithScores(long from, long to) {
        RScoredSortedSet<String> scoredSortedSet = client.getScoredSortedSet(baseKey, StringCodec.INSTANCE);
        return scoredSortedSet.entryRange(from, INCLUSIVE, to, INCLUSIVE)
                .stream()
                .map(tuple -> new Record(tuple.getValue(), tuple.getScore()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Record> getRangeByScoreWithScores(BigDecimal from, BigDecimal to) {
        return getRangeByScoreWithScores(from.longValue(), to.longValue());
    }

    @Override
    public List<Record> getRangeByRankWithScores(int from, int to) {
        RScoredSortedSet<String> scoredSortedSet = client.getScoredSortedSet(baseKey, StringCodec.INSTANCE);
        return scoredSortedSet.entryRange(from, to)
                .stream()
                .map(tuple -> new Record(tuple.getValue(), tuple.getScore()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Record> getRevRangeByRankWithScores(int from, int to) {
        RScoredSortedSet<String> scoredSortedSet = client.getScoredSortedSet(baseKey, StringCodec.INSTANCE);
        return scoredSortedSet.entryRangeReversed(from, to)
                .stream()
                .map(tuple -> new Record(tuple.getValue(), tuple.getScore()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Record> getRefRangeByRankWithScores(int from, int to, String refKey) {
        List<Record> memberRecords = getRangeByRankWithScores(from, to);

        RMap<String, String> map = client.getMap(refKey, StringCodec.INSTANCE);
        Map<String, String> refValues = map.getAll(memberRecords.stream().map(Record::getMember).collect(Collectors.toSet()));

        return memberRecords.stream().map(r -> new Record(refValues.get(r.getMember()), r.getScore())).collect(Collectors.toList());
    }

    @Override
    public BigDecimal incrementScore(String member, BigDecimal byScore) {
        RScoredSortedSet<String> scoredSortedSet = client.getScoredSortedSet(baseKey, StringCodec.INSTANCE);
        return BigDecimal.valueOf(scoredSortedSet.addScore(member, byScore.doubleValue()));
    }

    @Override
    public Sorted expireIn(long milliseconds) {
        RScoredSortedSet<String> scoredSortedSet = client.getScoredSortedSet(baseKey, StringCodec.INSTANCE);
        scoredSortedSet.expire(milliseconds, TimeUnit.MILLISECONDS);
        return this;
    }

    @Override
    public void removeRangeByScore(long from, long to) {
        RScoredSortedSet<String> scoredSortedSet = client.getScoredSortedSet(baseKey, StringCodec.INSTANCE);
        scoredSortedSet.removeRangeByScore(from, INCLUSIVE, to, INCLUSIVE);
    }

    @Override
    public boolean memberExists(String member) {
        RScoredSortedSet<String> scoredSortedSet = client.getScoredSortedSet(baseKey, StringCodec.INSTANCE);
        return scoredSortedSet.rank(member) != null;
    }

    @Override
    public int getRank(String member) {
        RScoredSortedSet<String> scoredSortedSet = client.getScoredSortedSet(baseKey, StringCodec.INSTANCE);
        Integer rank = scoredSortedSet.rank(member);
        return rank == null ? -1 : rank;
    }

    @Override
    public Optional<String> getMemberByScore(long score) {
        RScoredSortedSet<String> scoredSortedSet = client.getScoredSortedSet(baseKey, StringCodec.INSTANCE);
        return scoredSortedSet.entryRange(score, INCLUSIVE, score, INCLUSIVE)
                .stream()
                .findFirst()
                .map(ScoredEntry::getValue);
    }

    @Override
    public boolean remove(String member) {
        RScoredSortedSet<String> scoredSortedSet = client.getScoredSortedSet(baseKey, StringCodec.INSTANCE);
        return scoredSortedSet.remove(member);
    }
}
