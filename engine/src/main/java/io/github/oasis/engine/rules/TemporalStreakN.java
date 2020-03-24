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
import io.github.oasis.model.Event;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Tuple;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Isuru Weerarathna
 */
public class TemporalStreakN extends StreakN {
    public TemporalStreakN(JedisPool pool, TemporalStreakNRule options) {
        super(pool, options);
    }

    private long captureTsFromTuple(Tuple tuple) {
        String[] parts = tuple.getElement().split(":");
        return Long.parseLong(parts[0]);
    }

    private String captureEventIdFromTuple(Tuple tuple) {
        String[] parts = tuple.getElement().split(":");
        return parts[2];
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
            List<String> badgeInfos = jedis.hmget(badgeMetaKey, options.getId(), options.getId() + ".streak");
            lastBadgeTs = getLongValue(badgeInfos.get(0), 0L);
            lastBadgeStreak = getIntValue(badgeInfos.get(1), 0);
            System.out.println(">>> Last Badge end " + lastBadgeTs + ",   streak: " + lastBadgeStreak);
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
                    signals.add(new BadgeSignal(
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
    public List<BadgeSignal> unfold(Set<Tuple> tuples, Event event, long ts, StreakNRule optionsRef) {
        TemporalStreakNRule options = (TemporalStreakNRule) optionsRef;
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
        signals.addAll(fold(tuples, event, optionsRef));
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
