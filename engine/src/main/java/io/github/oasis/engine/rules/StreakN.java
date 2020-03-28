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

import static io.github.oasis.engine.utils.Numbers.asInt;
import static io.github.oasis.engine.utils.Numbers.asLong;
import static io.github.oasis.engine.utils.Numbers.isZero;

/**
 * Awards badges when a condition is satisfied continuously for a number of time.
 * There is no time constraint here.
 *
 * @author Isuru Weerarathna
 */
public class StreakN extends BadgeProcessor implements Consumer<Event> {

    public static final String COLON = ":";
    protected StreakNRule rule;

    public StreakN(JedisPool pool, StreakNRule rule) {
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
                signals.forEach(badgeSignal -> {
                    beforeBatchEmit(badgeSignal, event, rule, jedis);
                    rule.getCollector().accept(badgeSignal);
                });
            }
        }
    }

    public List<BadgeSignal> process(Event event, StreakNRule rule, Jedis jedis) {
        String key = ID.getUserBadgeStreakKey(event.getGameId(), event.getUser(), rule.getId());
        long ts = event.getTimestamp();
        if (this.rule.getCondition().test(event)) {
            String member = ts + ":1:" + event.getExternalId();
            jedis.zadd(key, ts, member);
            long rank = jedis.zrank(key, member);
            long start = Math.max(0, rank - rule.getMaxStreak());
            Set<Tuple> tupleRange = jedis.zrangeWithScores(key, start, rank + rule.getMaxStreak());
            return fold(tupleRange, event, rule);
        } else {
            String member = ts + ":0:" + event.getExternalId();
            jedis.zadd(key, ts, member);
            jedis.zremrangeByScore(key, 0, ts - rule.getRetainTime());
            long rank = jedis.zrank(key, member);
            long start = Math.max(0, rank - rule.getMaxStreak());
            Set<Tuple> tupleRange = jedis.zrangeWithScores(key, start, rank + rule.getMaxStreak());
            return unfold(tupleRange, event, ts, rule);
        }
    }

    public List<BadgeSignal> unfold(Set<Tuple> tuples, Event event, long ts, StreakNRule rule) {
        List<BadgeSignal> signals = new ArrayList<>();
        LinkedHashSet<Tuple> filteredTuples = tuples.stream()
                .filter(t -> asLong(t.getElement().split(COLON)[0]) != ts)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        List<BadgeSignal> badgesAwarded = fold(filteredTuples, event, rule);
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

        LinkedHashSet<Tuple> futureTuples = tuples.stream()
                .filter(t -> asLong(t.getElement().split(COLON)[0]) > ts)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        fold(futureTuples, event, rule).forEach(s -> rule.getCollector().accept(s));
        return signals;
    }

    public List<BadgeSignal> fold(Set<Tuple> tuples, Event event, StreakNRule options) {
        Tuple start = null;
        int len = 0;
        List<BadgeSignal> signals = new ArrayList<>();
        for (Tuple tuple : tuples) {
            int prev = asInt(options.getStreakMap().floor(len));
            String[] parts = tuple.getElement().split(COLON);
            if (isZero(parts[1])) {
                start = null;
                len = 0;
            } else {
                if (start == null) {
                    start = tuple;
                }
                len++;
            }
            int now = asInt(options.getStreakMap().floor(len));
            if (prev < now && start != null) {
                String[] startParts = start.getElement().split(COLON);
                BadgeSignal signal = new StreakBadgeSignal(options.getId(),
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

}
