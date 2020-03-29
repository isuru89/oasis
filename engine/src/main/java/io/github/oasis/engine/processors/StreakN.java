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

package io.github.oasis.engine.processors;

import io.github.oasis.engine.model.ID;
import io.github.oasis.engine.model.Record;
import io.github.oasis.engine.model.RuleContext;
import io.github.oasis.engine.rules.StreakNRule;
import io.github.oasis.engine.rules.signals.BadgeRemoveSignal;
import io.github.oasis.engine.rules.signals.BadgeSignal;
import io.github.oasis.engine.rules.signals.StreakBadgeSignal;
import io.github.oasis.engine.storage.Db;
import io.github.oasis.engine.storage.DbContext;
import io.github.oasis.engine.storage.Sorted;
import io.github.oasis.model.Event;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static io.github.oasis.engine.utils.Constants.COLON;
import static io.github.oasis.engine.utils.Numbers.asInt;
import static io.github.oasis.engine.utils.Numbers.asLong;
import static io.github.oasis.engine.utils.Numbers.isZero;

/**
 * Awards badges when a condition is satisfied continuously for a number of times.
 * There is no time constraint here.
 *
 * @author Isuru Weerarathna
 */
public class StreakN extends BadgeProcessor<StreakNRule> {

    public StreakN(Db pool, RuleContext<StreakNRule> ruleContext) {
        super(pool, ruleContext);
    }

    @Override
    public List<BadgeSignal> process(Event event, StreakNRule rule, DbContext db) {
        String key = ID.getUserBadgeStreakKey(event.getGameId(), event.getUser(), rule.getId());
        Sorted sortedRange = db.SORTED(key);
        long ts = event.getTimestamp();
        if (rule.getCriteria().test(event)) {
            String member = ts + ":1:" + event.getExternalId();
            sortedRange.add(member, ts);
            long rank = sortedRange.getRank(member);
            long start = Math.max(0, rank - rule.getMaxStreak());
            List<Record> tupleRange = sortedRange.getRangeByRankWithScores(start, rank + rule.getMaxStreak());
            return fold(tupleRange, event, rule, db);
        } else {
            String member = ts + ":0:" + event.getExternalId();
            sortedRange.add(member, ts);
            sortedRange.removeRangeByScore(0, ts - rule.getRetainTime());
            long rank = sortedRange.getRank(member);
            long start = Math.max(0, rank - rule.getMaxStreak());
            List<Record> tupleRange = sortedRange.getRangeByRankWithScores(start, rank + rule.getMaxStreak());
            return unfold(tupleRange, event, ts, rule, db);
        }
    }

    public List<BadgeSignal> unfold(List<Record> tuples, Event event, long ts, StreakNRule rule, DbContext db) {
        List<BadgeSignal> signals = new ArrayList<>();
        List<Record> filteredTuples = tuples.stream()
                .filter(t -> asLong(t.getMember().split(COLON)[0]) != ts)
                .collect(Collectors.toCollection(LinkedList::new));
        List<BadgeSignal> badgesAwarded = fold(filteredTuples, event, rule, db);
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

        List<Record> futureTuples = tuples.stream()
                .filter(t -> asLong(t.getMember().split(COLON)[0]) > ts)
                .collect(Collectors.toCollection(LinkedList::new));
        signals.addAll(fold(futureTuples, event, rule, db));
        return signals;
    }

    public List<BadgeSignal> fold(List<Record> tuples, Event event, StreakNRule options, DbContext db) {
        Record start = null;
        int len = 0;
        List<BadgeSignal> signals = new ArrayList<>();
        for (Record tuple : tuples) {
            int prev = asInt(options.getStreakMap().floor(len));
            String[] parts = tuple.getMember().split(COLON);
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
                String[] startParts = start.getMember().split(COLON);
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
