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
import io.github.oasis.engine.rules.signals.BadgeRemoveSignal;
import io.github.oasis.engine.rules.signals.BadgeSignal;
import io.github.oasis.engine.rules.signals.StreakBadgeSignal;
import io.github.oasis.model.Event;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Tuple;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

import static io.github.oasis.engine.utils.Numbers.asInt;
import static io.github.oasis.engine.utils.Numbers.asLong;

/**
 * Awards a badge when a condition fulfilled for a N number of times within a time unit T.
 *
 * @author Isuru Weerarathna
 */
public class TemporalStreakN extends StreakN {
    public TemporalStreakN(JedisPool pool, TemporalStreakNRule rule) {
        super(pool, rule);
    }

    private long captureTsFromTuple(Tuple tuple) {
        String[] parts = tuple.getElement().split(":");
        return Long.parseLong(parts[0]);
    }

    private String captureEventIdFromTuple(Tuple tuple) {
        String[] parts = tuple.getElement().split(":");
        return parts[2];
    }

    @Override
    public List<BadgeSignal> process(Event event, StreakNRule ruleRef, Jedis jedis) {
        TemporalStreakNRule rule = (TemporalStreakNRule) ruleRef;
        if (rule.isConsecutive()) {
            return super.process(event, rule, jedis);
        } else {
            return nonConsecutiveAccept(event, rule, jedis);
        }
    }

    private List<BadgeSignal> nonConsecutiveAccept(Event event, TemporalStreakNRule rule, Jedis jedis) {
        String key = ID.getUserBadgeStreakKey(event.getGameId(), event.getUser(), rule.getId());
        long ts = event.getTimestamp();
        if (rule.getCondition().test(event)) {
            String badgeMetaKey = ID.getUserBadgesMetaKey(event.getGameId(), event.getUser());
            String lastOffering = jedis.hget(badgeMetaKey, rule.getId() + ":lasttime");  // <timestamp>:<streak>:<id>
            long lastTimeOffered = 0L;
            if (lastOffering != null) {
                lastTimeOffered = asLong(lastOffering.split(":")[0]);
            }
            if (ts <= lastTimeOffered) {
                return null;
            }

            jedis.zadd(key, ts, ts + ":" + event.getExternalId());
            long start = Math.max(ts - rule.getTimeUnit(), 0);
            Set<Tuple> tupleRange = jedis.zrangeByScoreWithScores(key, start, ts + rule.getTimeUnit());
            return countFold(tupleRange, event, lastOffering, rule, jedis);
        }
        return null;
    }

    private List<BadgeSignal> countFold(Set<Tuple> tuplesAll, Event event, String lastOffering, TemporalStreakNRule rule, Jedis jedis) {
        List<BadgeSignal> signals = new ArrayList<>();
        int lastStreak = 0;
        long lastTs = 0L;
        long firstTs = 0L;
        String prevBadgeFirstId = null;
        if (lastOffering != null) {
            String[] parts = lastOffering.split(":");
            lastTs = asLong(parts[0]) + 1;
            lastStreak = asInt(parts[1]);
            firstTs = asLong(parts[2]);
            prevBadgeFirstId = parts[3];
        }

        // not enough entries for new badges
        if (lastStreak == 0 && tuplesAll.size() < rule.getMinStreak()) {
            return signals;
        }

        NavigableMap<Long, Integer> countMap = new TreeMap<>();
        Map<Long, Tuple> tupleMap = new HashMap<>();
        countMap.put(0L, 0);
        countMap.put(firstTs, 0);
        countMap.put(lastTs, lastStreak);
        tupleMap.put(firstTs, new Tuple(lastTs + ":" + prevBadgeFirstId, firstTs * 1.0));
        List<Integer> streakList = rule.getStreaks();
        int size = 0;
        for (Tuple tuple : tuplesAll) {
            long ts = (long) tuple.getScore();
            tupleMap.put(ts, tuple);
            countMap.put(ts, ++size);
        }

        long marker = lastTs;
        List<Tuple> tuples = new ArrayList<>(tuplesAll);
        for (int i = 0; i < tuples.size(); i++) {
            Tuple tuple = tuples.get(i);
            long ts = (long) tuple.getScore();
            if (ts <= marker) {
                continue;
            }
            long start = Math.max(lastStreak == rule.getMaxStreak() ? marker : firstTs, ts - rule.getTimeUnit());

            Map.Entry<Long, Integer> prevEntry = countMap.ceilingEntry(start);
            int currStreak = countMap.ceilingEntry(ts).getValue() - prevEntry.getValue() + 1;
            int streakIndex = streakList.indexOf(currStreak);
            if (streakIndex >= 0) {
                String badgeMetaKey = ID.getUserBadgesMetaKey(event.getGameId(), event.getUser());
                long badgeStartTs = prevEntry.getKey();
                Tuple startTuple = tupleMap.get(prevEntry.getKey());
                String firstId;
                if (startTuple == null) {
                    firstId = prevBadgeFirstId;
                } else {
                    firstId = startTuple.getElement().split(":")[1];
                }
                signals.add(new StreakBadgeSignal(rule.getId(),
                        currStreak,
                        badgeStartTs,
                        ts,
                        firstId,
                        tuple.getElement().split(":")[1]));
                jedis.hset(badgeMetaKey, rule.getId() + ":lasttime", ts + ":" + currStreak + ":" + badgeStartTs + ":" + firstId);

                // no more streaks to find
//                if (streakIndex == streakList.size() - 1) {
//                    continue;
//                }
//
//                // a streak found... let's find further streaks
//                for (int j = streakIndex + 1; j < streakList.size(); j++) {
//                    int nextStreak = streakList.get(j);
//                    int furtherReqElements = nextStreak - currStreak;
//                    if (i + furtherReqElements > tuples.size() - 1) {
//                        break;
//                    }
//
//                    Tuple elementAtNextStreak = tuples.get(i + furtherReqElements);
//                    long ets = (long) elementAtNextStreak.getScore();
//                    if (ets - badgeStartTs > rule.getTimeUnit()) {
//                        // TODO adjust time
//                        break;
//                    }
//
//                    // within time frame
//                    signals.add(new BadgeSignal(rule.getId(),
//                            nextStreak,
//                            badgeStartTs,
//                            ets,
//                            firstId,
//                            elementAtNextStreak.getElement().split(":")[0]));
//                    jedis.hset(badgeMetaKey, rule.getId() + ":lasttime", ets + ":" + nextStreak + ":" + badgeStartTs + ":" + firstId);
//                    marker = ets;
//                }
            }
        }
        return signals;
    }


    @Override
    public List<BadgeSignal> fold(Set<Tuple> tuples, Event event, StreakNRule optionsRef) {
        List<BadgeSignal> signals = new ArrayList<>();
        TemporalStreakNRule options = (TemporalStreakNRule) optionsRef;
        List<List<Tuple>> partitions = splitPartitions(tuples, options);
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

        List<Integer> streaks = options.getStreaks();
        partitionStart: for (List<Tuple> partition : partitions) {
            int n = partition.size();
            Tuple firstTuple = partition.get(0);
            long startTs = captureTsFromTuple(firstTuple);
            for (int streak : streaks) {
                if (n >= streak) {
                    Tuple tupleAtStreak = partition.get(streak - 1);
                    long ts = captureTsFromTuple(tupleAtStreak);
                    if (ts - startTs > options.getTimeUnit()) {
                        continue partitionStart;
                    }
                    if (lastBadgeStreak == streak && startTs <= lastBadgeTs) {
                        continue;
                    }
                    signals.add(new StreakBadgeSignal(
                            options.getId(),
                            streak,
                            startTs,
                            ts,
                            captureEventIdFromTuple(firstTuple),
                            captureEventIdFromTuple(tupleAtStreak)
                    ));
                }
            }
        }
        return signals;
    }

    @Override
    public List<BadgeSignal> unfold(Set<Tuple> tuples, Event event, long ts, StreakNRule rule) {
        TemporalStreakNRule options = (TemporalStreakNRule) rule;
        List<BadgeSignal> signals = new ArrayList<>();
        try (Jedis jedis = pool.getResource()) {
            long startTs = Math.max(0, ts - options.getTimeUnit());
            String badgeSpecKey = ID.getUserBadgeSpecKey(event.getGameId(), event.getUser(), options.getId());
            Set<Tuple> badgesInRange = jedis.zrangeByScoreWithScores(String.format(badgeSpecKey, event.getUser(), options.getId()), startTs, ts);
            if (!badgesInRange.isEmpty()) {
                badgesInRange.stream().filter(t -> {
                    String[] parts = t.getElement().split(":");
                    long badgeEndTs = Long.parseLong(parts[0]);
                    if (badgeEndTs < ts) {
                        return false;
                    }
                    return parts[1].equals(options.getId());
                }).map(t -> {
                    String[] parts = t.getElement().split(":");
                    return new BadgeRemoveSignal(options.getId(),
                            Integer.parseInt(parts[3]),
                            BigDecimal.valueOf(t.getScore()).longValue());
                }).forEach(signals::add);
            }
        }
        signals.addAll(fold(tuples, event, rule));
        return signals;
    }

    public List<List<Tuple>> splitPartitions(Set<Tuple> tuples, TemporalStreakNRule options) {
        List<Tuple> currentPartition = new ArrayList<>();
        List<List<Tuple>> partitions = new ArrayList<>();
        for (Tuple tuple : tuples) {
            String[] parts = tuple.getElement().split(":");
            if ("1".equals(parts[1])) {
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
}
