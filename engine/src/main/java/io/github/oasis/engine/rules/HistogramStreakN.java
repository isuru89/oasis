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
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static io.github.oasis.engine.utils.Constants.COLON;
import static io.github.oasis.engine.utils.Constants.SCALE;
import static io.github.oasis.engine.utils.Numbers.addToScale;
import static io.github.oasis.engine.utils.Numbers.asDecimal;
import static io.github.oasis.engine.utils.Numbers.asInt;
import static io.github.oasis.engine.utils.Numbers.asLong;
import static io.github.oasis.engine.utils.Numbers.isThresholdCrossedDown;
import static io.github.oasis.engine.utils.Numbers.isThresholdCrossedUp;

/**
 * @author Isuru Weerarathna
 */
public class HistogramStreakN extends BadgeProcessor implements Consumer<Event> {

    private HistogramStreakNRule rule;

    public HistogramStreakN(JedisPool pool, HistogramStreakNRule rule) {
        super(pool);
        this.rule = rule;
    }

    @Override
    public void accept(Event event) {
        if (!isMatchEvent(event, rule)) {
            return;
        }

        try (Jedis jedis = pool.getResource()) {
            List<BadgeSignal> signals = process(event, rule, jedis);
            if (signals != null) {
                signals.forEach(s -> {
                    beforeBatchEmit(s, event, rule, jedis);
                    rule.getConsumer().accept(s);
                });
            }
        }
    }

    private List<BadgeSignal> process(Event event, HistogramStreakNRule rule, Jedis jedis) {
        String badgeKey = ID.getBadgeHistogramKey(event.getGameId(), event.getUser(), rule.getId());
        long timestamp = event.getTimestamp() - (event.getTimestamp() % rule.getTimeUnit());
        BigDecimal value = evaluateForValue(event).setScale(SCALE, BigDecimal.ROUND_HALF_UP);
        Set<Tuple> tupleRange = jedis.zrangeByScoreWithScores(badgeKey, timestamp, timestamp);
        BigDecimal prev = BigDecimal.ZERO;
        String prevMember = null;
        if (!tupleRange.isEmpty()) {
            Tuple existingTuple = tupleRange.iterator().next();
            String[] parts = existingTuple.getElement().split(COLON);
            prev = asDecimal(parts[1]);
            prevMember = existingTuple.getElement();
        }
        BigDecimal updatedValue = addToScale(prev, value, SCALE);
        if (prevMember != null) {
            jedis.zrem(badgeKey, prevMember);
        }
        jedis.zadd(badgeKey, timestamp, timestamp + COLON + updatedValue.toString());
        if (isThresholdCrossedUp(prev, updatedValue, rule.getThreshold())) {
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
                jedis.hsetnx(metaBadgesInfoKey, firstSubKey, event.getTimestamp() + COLON + event.getExternalId());
                if (rule.containsStreakMargin(total)) {
                    List<String> first = jedis.hmget(metaBadgesInfoKey, firstSubKey, lastHitSubKey);
                    String[] parts = first.get(0).split(COLON);
                    long ts = Long.parseLong(parts[0]);
                    long lastTs = event.getTimestamp();
                    lastTs = lastTs - (lastTs % rule.getTimeUnit());
                    jedis.hset(metaBadgesInfoKey, lastHitSubKey, event.getTimestamp() + COLON + event.getExternalId());
                    return Collections.singletonList(new HistogramBadgeSignal(rule.getId(),
                            total,
                            ts - (ts % rule.getTimeUnit()),
                            lastTs,
                            event.getExternalId()));
                }
            }

        } else if (isThresholdCrossedDown(prev, updatedValue, rule.getThreshold())) {
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
                    String[] parts = first.get(0).split(COLON);
                    long ts = Long.parseLong(parts[0]);
                    long lastTs = event.getTimestamp();
                    if (first.size() > 1 && first.get(1) != null) {
                        lastTs = Long.parseLong(first.get(1).split(COLON)[0]);
                    }
                    lastTs = lastTs - (lastTs % rule.getTimeUnit());
                    jedis.hset(metaBadgesInfoKey, lastHitSubKey, event.getTimestamp() + COLON + event.getExternalId());
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
            String[] parts = t.getElement().split(COLON);
            if (Long.parseLong(parts[0]) != ts) {
                return t;
            } else {
                return new Tuple(parts[0] + COLON + options.getThreshold().toString(), t.getScore());
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
            String[] parts = t.getElement().split(COLON);
            return Long.parseLong(parts[0]) > ts;
        }).collect(Collectors.toCollection(LinkedHashSet::new));
        fold(futureTuples, event, options, false).forEach(s -> options.getConsumer().accept(s));
        return signals;
    }

    public List<BadgeSignal> fold(Set<Tuple> tuples, Event event, HistogramStreakNRule rule, boolean skipOldCheck) {
        List<BadgeSignal> signals = new ArrayList<>();
        List<List<Tuple>> partitions = splitPartitions(tuples, rule);
        if (partitions.isEmpty()) {
            return signals;
        }

        long lastBadgeTs = 0L;
        int lastBadgeStreak = 0;
        try (Jedis jedis = pool.getResource()) {
            String badgeMetaKey = ID.getUserBadgesMetaKey(event.getGameId(), event.getUser());
            List<String> badgeInfos = jedis.hmget(badgeMetaKey, getMetaEndTimeKey(rule), getMetaStreakKey(rule));
            lastBadgeTs = asLong(badgeInfos.get(0));
            lastBadgeStreak = asInt(badgeInfos.get(1));
        }

        List<Integer> streaks = rule.getStreaks();
        partitionStart: for (List<Tuple> partition : partitions) {
            int n = partition.size();
            Tuple firstTuple = partition.get(0);
            long startTs = Math.round(firstTuple.getScore());
            for (int streak : streaks) {
                if (n >= streak) {
                    Tuple tupleAtStreak = partition.get(streak - 1);
                    long ts = Math.round(tupleAtStreak.getScore());
                    if (ts - startTs >= rule.getTimeUnit() * streak) {
                        continue partitionStart;
                    }
                    if (!skipOldCheck && lastBadgeStreak == streak && startTs <= lastBadgeTs) {
                        continue;
                    }
                    signals.add(new HistogramBadgeSignal(
                            rule.getId(),
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
            BigDecimal metric = asDecimal(parts[1]);
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

    private BigDecimal evaluateForValue(Event event) {
        return BigDecimal.valueOf(rule.getValueResolver().apply(event));
    }

}
