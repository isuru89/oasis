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

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author Isuru Weerarathna
 */
public class StreakN implements Consumer<Event> {

    protected final JedisPool pool;
    protected StreakNRule options;

    public StreakN(JedisPool pool, StreakNRule options) {
        this.pool = pool;
        this.options = options;
    }

    @Override
    public void accept(Event event) {
        String key = getKey();
        long value = (long) event.getFieldValue("value");
        long ts = event.getTimestamp();
        System.out.println("Processed: " + value);
        try (Jedis jedis = pool.getResource()) {
            if (this.options.getCondition().test(value)) {
                jedis.zadd(key, ts, ts + ":1:" + event.getExternalId());
                long rank = jedis.zrank(key, ts  + ":1:" + event.getExternalId());
                long start = Math.max(0, rank - options.getMaxStreak());
                Set<Tuple> zrange = jedis.zrangeWithScores(key, start, rank + options.getMaxStreak());
                System.out.println(zrange);
                fold(zrange, event, options).forEach(b -> {
                    beforeBatchEmit(b, event, options, jedis);
                    options.getCollector().accept(b);
                });
            } else {
                jedis.zadd(key, ts, ts + ":0:" + event.getExternalId());
                jedis.zremrangeByScore(key, 0, ts - options.getRetainTime());
                long rank = jedis.zrank(key, ts + ":0:" + event.getExternalId());
                long start = Math.max(0, rank - options.getMaxStreak());
                Set<Tuple> zrange = jedis.zrangeWithScores(key, start, rank + options.getMaxStreak());
                System.out.println(zrange);
                unfold(zrange, event, ts, options).forEach(b -> {
                    beforeBatchEmit(b, event, options, jedis);
                    options.getCollector().accept(b);
                });
            }
        }
    }

    public List<BadgeSignal> unfold(Set<Tuple> tuples, Event event, long ts, StreakNRule options) {
        List<BadgeSignal> signals = new ArrayList<>();
        LinkedHashSet<Tuple> filteredTuples = tuples.stream().filter(t -> {
            String[] parts = t.getElement().split(":");
            return Long.parseLong(parts[0]) != ts;
        }).collect(Collectors.toCollection(LinkedHashSet::new));
        List<BadgeSignal> badgesAwarded = fold(filteredTuples, event, options);
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
                signals.add(new BadgeRemoveSignal(signal));
            }
        }

        LinkedHashSet<Tuple> futureTuples = tuples.stream().filter(t -> {
            String[] parts = t.getElement().split(":");
            return Long.parseLong(parts[0]) > ts;
        }).collect(Collectors.toCollection(LinkedHashSet::new));
        fold(futureTuples, event, options).forEach(s -> options.getCollector().accept(s));
        return signals;
    }

    public List<BadgeSignal> fold(Set<Tuple> tuples, Event event, StreakNRule options) {
        Tuple start = null;
        int len = 0;
        List<BadgeSignal> signals = new ArrayList<>();
        for (Tuple tuple : tuples) {
            int prev = options.getStreakMap().floor(len).intValue();
            String[] parts = tuple.getElement().split(":");
            if ("0".equals(parts[1])) {
                start = null;
                len = 0;
            } else {
                if (start == null) {
                    start = tuple;
                }
                len++;
            }
            int now = options.getStreakMap().floor(len).intValue();
            if (prev < now) {
                String[] startParts = start.getElement().split(":");
                BadgeSignal signal = new BadgeSignal(options.getId(),
                        now,
                        Long.parseLong(startParts[0]),
                        Long.parseLong(parts[0]),
                        startParts[2],
                        parts[2]);
                signals.add(signal);
            }
        }
        return signals;
    }

    public String getKey() {
        return "streak.0";
    }

    protected void beforeBatchEmit(BadgeSignal signal, Event event, StreakNRule options, Jedis jedis) {
        jedis.zadd(ID.getUserBadgeSpecKey(event.getGameId(), event.getUser(), options.getId()),
                signal.getStartTime(),
                String.format("%d:%s:%d:%d", signal.getEndTime(), options.getId(), signal.getStartTime(), signal.getStreak()));
        String userBadgesMeta = ID.getUserBadgesMetaKey(event.getGameId(), event.getUser());
        String value = jedis.hget(userBadgesMeta, options.getId());
        if (value == null) {
            jedis.hset(userBadgesMeta, options.getId(), String.valueOf(signal.getEndTime()));
            jedis.hset(userBadgesMeta, options.getId() + ".streak", String.valueOf(signal.getStreak()));
        } else {
            long val = Long.parseLong(value);
            if (signal.getEndTime() >= val) {
                jedis.hset(userBadgesMeta, options.getId() + ".streak", String.valueOf(signal.getStreak()));
            }
            jedis.hset(userBadgesMeta, options.getId(), String.valueOf(Math.max(signal.getEndTime(), val)));
        }
    }
}
