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

package io.github.oasis.engine.rules;

import io.github.oasis.engine.model.ID;
import io.github.oasis.engine.rules.signals.BadgeSignal;
import io.github.oasis.engine.rules.signals.HistogramBadgeRemovalSignal;
import io.github.oasis.engine.rules.signals.HistogramBadgeSignal;
import io.github.oasis.model.Event;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Tuple;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * @author Isuru Weerarathna
 */
public class HistogramStreakN implements BadgeHandler {

    private JedisPool pool;
    private HistogramStreakNRule rule;

    public HistogramStreakN(JedisPool pool, HistogramStreakNRule rule) {
        this.rule = rule;
        this.pool = pool;
    }

    @Override
    public void accept(Event event) {
        handle(event);
    }

    @Override
    public List<BadgeSignal> handle(Event event) {
        try (Jedis jedis = pool.getResource()) {
            List<BadgeSignal> signals = alternativeProcess(event, jedis);
            if (signals != null) {
                signals.forEach(s -> {
                    beforeBatchEmit(s, event, rule, jedis);
                    rule.getConsumer().accept(s);
                });
            }
            return signals;
        }
    }

    private List<BadgeSignal> alternativeProcess(Event event, Jedis jedis) {
        String badgeKey = ID.getBadgeHistogramKey(event.getGameId(), event.getUser(), rule.getId());
        long timestamp = event.getTimestamp() - (event.getTimestamp() % rule.getTimeUnit());
        BigDecimal value = evaluateForValue(event).setScale(0, BigDecimal.ROUND_HALF_UP);
        Set<Tuple> zrange = jedis.zrangeByScoreWithScores(badgeKey, timestamp, timestamp);
        BigDecimal prev = BigDecimal.ZERO;
        String prevMember = null;
        if (!zrange.isEmpty()) {
            Tuple existingTuple = zrange.iterator().next();
            String[] parts = existingTuple.getElement().split(":");
            prev = new BigDecimal(parts[1]);
            prevMember = existingTuple.getElement();
        }
        BigDecimal incr = prev.add(value).setScale(2, BigDecimal.ROUND_HALF_UP);
        if (prevMember != null) {
            jedis.zrem(badgeKey, prevMember);
        }
        jedis.zadd(badgeKey, timestamp, timestamp + ":" + incr.toString());
        if (isThresholdCrossedUp(prev, incr, rule.getThreshold())) {
            Set<Tuple> seq = jedis.zrangeByScoreWithScores(badgeKey,
                    timestamp - rule.getTimeUnit() * rule.getMaxStreak(),
                    timestamp + rule.getTimeUnit() * rule.getMaxStreak());

            if (rule.isConsecutive()) {
                return fold(seq, event, rule, false);
            } else {
                String metaBadgesInfoKey = ID.getUserBadgesMetaKey(event.getGameId(), event.getUser());
                String totalSubKey = String.format("%s:total", rule.getId());
                String firstSubKey = String.format("%s:first", rule.getId());
                String lastHitSubKey = String.format("%s:hit", rule.getId());
                int curr = asInt(jedis.hget(metaBadgesInfoKey, totalSubKey));
                if (rule.isMaxStreakPassed(curr)) {
                    jedis.hset(metaBadgesInfoKey, totalSubKey, "0");
                    jedis.hdel(metaBadgesInfoKey, firstSubKey);
                }
                int total = jedis.hincrBy(metaBadgesInfoKey, totalSubKey, 1).intValue();
                jedis.hsetnx(metaBadgesInfoKey, firstSubKey, event.getTimestamp() + ":" + event.getExternalId());
                if (rule.containsStreakMargin(total)) {
                    List<String> first = jedis.hmget(metaBadgesInfoKey, firstSubKey, lastHitSubKey);
                    String[] parts = first.get(0).split(":");
                    long ts = Long.parseLong(parts[0]);
                    long lastTs = event.getTimestamp();
                    lastTs = lastTs - (lastTs % rule.getTimeUnit());
                    jedis.hset(metaBadgesInfoKey, lastHitSubKey, event.getTimestamp() + ":" + event.getExternalId());
                    return Collections.singletonList(new HistogramBadgeSignal(rule.getId(),
                            total,
                            ts - (ts % rule.getTimeUnit()),
                            lastTs,
                            event.getExternalId()));
                }
            }

        } else if (isThresholdCrossedDown(prev, incr, rule.getThreshold())) {
            Set<Tuple> seq = jedis.zrangeByScoreWithScores(badgeKey,
                    timestamp - rule.getTimeUnit() * rule.getMaxStreak(),
                    timestamp + rule.getTimeUnit() * rule.getMaxStreak());

            if (rule.isConsecutive()) {
                return unfold(seq, event, timestamp, rule);
            } else {
                String metaBadgesInfoKey = ID.getUserBadgesMetaKey(event.getGameId(), event.getUser());
                String totalSubKey = String.format("%s:total", rule.getId());
                String firstSubKey = String.format("%s:first", rule.getId());
                String lastHitSubKey = String.format("%s:hit", rule.getId());
                int curr = asInt(jedis.hget(metaBadgesInfoKey, totalSubKey));
                if (curr <= 0) {
                    return null;
                }
                int total = jedis.hincrBy(metaBadgesInfoKey, totalSubKey, -1).intValue();
                if (total == 0) {
                    jedis.del(metaBadgesInfoKey, firstSubKey);
                }
                if (rule.containsStreakMargin(total + 1)) {
                    List<String> first = jedis.hmget(metaBadgesInfoKey, firstSubKey, lastHitSubKey);
                    String[] parts = first.get(0).split(":");
                    long ts = Long.parseLong(parts[0]);
                    long lastTs = event.getTimestamp();
                    if (first.size() > 1 && first.get(1) != null) {
                        lastTs = Long.parseLong(first.get(1).split(":")[0]);
                    }
                    lastTs = lastTs - (lastTs % rule.getTimeUnit());
                    jedis.hset(metaBadgesInfoKey, lastHitSubKey, event.getTimestamp() + ":" + event.getExternalId());
                    return Collections.singletonList(new HistogramBadgeRemovalSignal(rule.getId(),
                            total + 1,
                            ts - (ts % rule.getTimeUnit()),
                            lastTs));
                }
            }
        }
        return null;
    }

    public List<BadgeSignal> unfold(Set<Tuple> tuples, Event event, long ts, HistogramStreakNRule options) {
        List<BadgeSignal> signals = new ArrayList<>();
        LinkedHashSet<Tuple> filteredTuples = tuples.stream().map(t -> {
            String[] parts = t.getElement().split(":");
            if (Long.parseLong(parts[0]) != ts) {
                return t;
            } else {
                return new Tuple(parts[0] + ":" + options.getThreshold().toString(), t.getScore());
            }
        }).collect(Collectors.toCollection(LinkedHashSet::new));
        List<BadgeSignal> badgesAwarded = fold(filteredTuples, event, options, true);
        if (badgesAwarded.isEmpty()) {
            return signals;
        }
        NavigableMap<Long, List<BadgeSignal>> badgesAwardedGrouping = new TreeMap<>(badgesAwarded.stream()
                .collect(Collectors.groupingBy(BadgeSignal::getStartTime)));
        Map.Entry<Long, List<BadgeSignal>> nearestEntries = badgesAwardedGrouping.floorEntry(ts);
        if (Objects.isNull(nearestEntries) || nearestEntries.getValue().isEmpty()) {
            return signals;
        }

        for (BadgeSignal signal : nearestEntries.getValue()) {
            if (signal.getEndTime() >= ts) {
                signals.add(new HistogramBadgeRemovalSignal(signal));
            }
        }

        LinkedHashSet<Tuple> futureTuples = tuples.stream().filter(t -> {
            String[] parts = t.getElement().split(":");
            return Long.parseLong(parts[0]) > ts;
        }).collect(Collectors.toCollection(LinkedHashSet::new));
        fold(futureTuples, event, options, false).forEach(s -> options.getConsumer().accept(s));
        return signals;
    }

    public List<BadgeSignal> fold(Set<Tuple> tuples, Event event, HistogramStreakNRule options, boolean skipOldCheck) {
        List<BadgeSignal> signals = new ArrayList<>();
        List<List<Tuple>> partitions = splitPartitions(tuples, options);
        if (partitions.isEmpty()) {
            return signals;
        }

        long lastBadgeTs = 0L;
        int lastBadgeStreak = 0;
        try (Jedis jedis = pool.getResource()) {
            String badgeMetaKey = ID.getUserBadgesMetaKey(event.getGameId(), event.getUser());
            List<String> badgeInfos = jedis.hmget(badgeMetaKey, options.getId(), options.getId() + ".streak");
            lastBadgeTs = getLongValue(badgeInfos.get(0), 0L);
            lastBadgeStreak = getIntValue(badgeInfos.get(1), 0);
            System.out.println(">>> Last Badge end " + lastBadgeTs + ",   streak: " + lastBadgeStreak);
        }

        List<Integer> streaks = options.getStreaks();
        partitionStart: for (List<Tuple> partition : partitions) {
            int n = partition.size();
            Tuple firstTuple = partition.get(0);
            long startTs = Math.round(firstTuple.getScore());
            for (int streak : streaks) {
                if (n >= streak) {
                    Tuple tupleAtStreak = partition.get(streak - 1);
                    long ts = Math.round(tupleAtStreak.getScore());
                    if (ts - startTs >= options.getTimeUnit() * streak) {
                        continue partitionStart;
                    }
                    if (!skipOldCheck && lastBadgeStreak == streak && startTs <= lastBadgeTs) {
                        continue;
                    }
                    signals.add(new HistogramBadgeSignal(
                            options.getId(),
                            streak,
                            startTs,
                            ts,
                            event.getExternalId()
                    ));
                }
            }
        }
        return signals;
    }

    private List<List<Tuple>> splitPartitions(Set<Tuple> tuples, HistogramStreakNRule options) {
        List<Tuple> currentPartition = new ArrayList<>();
        List<List<Tuple>> partitions = new ArrayList<>();
        for (Tuple tuple : tuples) {
            String[] parts = tuple.getElement().split(":");
            BigDecimal metric = zeroIfNull(parts[1]);
            if (metric.compareTo(options.getThreshold()) >= 0) {
                currentPartition.add(tuple);
            } else {
                if (currentPartition.size() >= options.getMinStreak()) {
                    partitions.add(currentPartition);
                }
                currentPartition = new ArrayList<>();
            }
        }
        if (!currentPartition.isEmpty() && currentPartition.size() >= options.getMinStreak()) {
            partitions.add(currentPartition);
        }
        return partitions;
    }

    private boolean isThresholdCrossedUp(BigDecimal prevValue, BigDecimal currValue, BigDecimal threshold) {
        return prevValue.compareTo(threshold) < 0 && currValue.compareTo(threshold) >= 0;
    }

    private boolean isThresholdCrossedDown(BigDecimal prevValue, BigDecimal currValue, BigDecimal threshold) {
        return prevValue.compareTo(threshold) >= 0 && currValue.compareTo(threshold) < 0;
    }

    protected void beforeBatchEmit(BadgeSignal signal, Event event, HistogramStreakNRule options, Jedis jedis) {
        jedis.zadd(ID.getUserBadgeSpecKey(event.getGameId(), event.getUser(), options.getId()),
                signal.getStartTime(),
                String.format("%d:%s:%d:%d", signal.getEndTime(), options.getId(), signal.getStartTime(), signal.getAttribute()));
        String userBadgesMeta = ID.getUserBadgesMetaKey(event.getGameId(), event.getUser());
        String value = jedis.hget(userBadgesMeta, options.getId());
        if (value == null) {
            jedis.hset(userBadgesMeta, options.getId(), String.valueOf(signal.getEndTime()));
            jedis.hset(userBadgesMeta, options.getId() + ".streak", String.valueOf(signal.getAttribute()));
        } else {
            long val = Long.parseLong(value);
            if (signal.getEndTime() >= val) {
                jedis.hset(userBadgesMeta, options.getId() + ".streak", String.valueOf(signal.getAttribute()));
            }
            jedis.hset(userBadgesMeta, options.getId(), String.valueOf(Math.max(signal.getEndTime(), val)));
        }
    }

    private static int asInt(String value) {
        return value == null ? 0 : Integer.parseInt(value);
    }

    private BigDecimal evaluateForValue(Event event) {
        return BigDecimal.valueOf(rule.getValueResolver().apply(event));
    }

    private static BigDecimal zeroIfNull(Double value) {
        return value == null ? BigDecimal.ZERO : new BigDecimal(value).setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    private static BigDecimal zeroIfNull(String value) {
        return value == null ? BigDecimal.ZERO : new BigDecimal(value);
    }

    private long getLongValue(String val, long defaultValue) {
        if (val == null) {
            return defaultValue;
        }
        return Long.parseLong(val);
    }

    private int getIntValue(String val, int defaultValue) {
        if (val == null) {
            return defaultValue;
        }
        return Integer.parseInt(val);
    }

}
