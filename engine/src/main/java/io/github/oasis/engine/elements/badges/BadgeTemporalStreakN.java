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

package io.github.oasis.engine.elements.badges;

import io.github.oasis.engine.elements.badges.rules.BadgeStreakNRule;
import io.github.oasis.engine.elements.badges.rules.BadgeTemporalStreakNRule;
import io.github.oasis.engine.elements.badges.signals.BadgeRemoveSignal;
import io.github.oasis.engine.elements.badges.signals.BadgeSignal;
import io.github.oasis.engine.elements.badges.signals.StreakBadgeSignal;
import io.github.oasis.engine.external.Db;
import io.github.oasis.engine.external.DbContext;
import io.github.oasis.engine.external.Sorted;
import io.github.oasis.engine.model.ExecutionContext;
import io.github.oasis.engine.model.ID;
import io.github.oasis.engine.model.Record;
import io.github.oasis.engine.model.RuleContext;
import io.github.oasis.model.Event;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import static io.github.oasis.engine.utils.Numbers.asInt;
import static io.github.oasis.engine.utils.Numbers.asLong;

/**
 * Awards a badge when a condition fulfilled for a N number of times within a time unit T.
 *
 * @author Isuru Weerarathna
 */
public class BadgeTemporalStreakN extends BadgeStreakN {
    public BadgeTemporalStreakN(Db pool, RuleContext<BadgeStreakNRule> ruleContext) {
        super(pool, ruleContext);
    }

    private long captureTsFromTuple(Record tuple) {
        String[] parts = tuple.getMember().split(":");
        return Long.parseLong(parts[0]);
    }

    private String captureEventIdFromTuple(Record tuple) {
        String[] parts = tuple.getMember().split(":");
        return parts[2];
    }

    @Override
    public List<BadgeSignal> process(Event event, BadgeStreakNRule ruleRef, ExecutionContext context, DbContext db) {
        BadgeTemporalStreakNRule rule = (BadgeTemporalStreakNRule) ruleRef;
        if (rule.isConsecutive()) {
            return super.process(event, rule, context, db);
        } else {
            return nonConsecutiveAccept(event, rule, context, db);
        }
    }

    private List<BadgeSignal> nonConsecutiveAccept(Event event, BadgeTemporalStreakNRule rule, ExecutionContext context, DbContext db) {
        String key = ID.getUserBadgeStreakKey(event.getGameId(), event.getUser(), rule.getId());
        Sorted sortedRange = db.SORTED(key);
        long ts = event.getTimestamp();
        if (rule.getCriteria().matches(event, rule, context)) {
            String badgeMetaKey = ID.getUserBadgesMetaKey(event.getGameId(), event.getUser());
            String lastOffering = db.getValueFromMap(badgeMetaKey, rule.getId() + ":lasttime");  // <timestamp>:<streak>:<id>
            long lastTimeOffered = 0L;
            if (lastOffering != null) {
                lastTimeOffered = asLong(lastOffering.split(":")[0]);
            }
            if (ts <= lastTimeOffered) {
                return null;
            }

            sortedRange.add(ts + ":" + event.getExternalId(), ts);
            long start = Math.max(ts - rule.getTimeUnit(), 0);
            List<Record> tupleRange = sortedRange.getRangeByScoreWithScores(start, ts + rule.getTimeUnit());
            return countFold(tupleRange, event, lastOffering, rule, db);
        }
        return null;
    }

    private List<BadgeSignal> countFold(List<Record> tuplesAll, Event event, String lastOffering, BadgeTemporalStreakNRule rule, DbContext db) {
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
        Map<Long, Record> tupleMap = new HashMap<>();
        countMap.put(0L, 0);
        countMap.put(firstTs, 0);
        countMap.put(lastTs, lastStreak);
        tupleMap.put(firstTs, new Record(lastTs + ":" + prevBadgeFirstId, firstTs * 1.0));
        List<Integer> streakList = rule.getStreaks();
        int size = 0;
        for (Record record : tuplesAll) {
            long ts = (long) record.getScore();
            tupleMap.put(ts, record);
            countMap.put(ts, ++size);
        }

        long marker = lastTs;
        List<Record> tuples = new ArrayList<>(tuplesAll);
        for (int i = 0; i < tuples.size(); i++) {
            Record tuple = tuples.get(i);
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
                Record startTuple = tupleMap.get(prevEntry.getKey());
                String firstId;
                if (startTuple == null) {
                    firstId = prevBadgeFirstId;
                } else {
                    firstId = startTuple.getMember().split(":")[1];
                }
                signals.add(new StreakBadgeSignal(rule.getId(),
                        event,
                        currStreak,
                        badgeStartTs,
                        ts,
                        firstId,
                        tuple.getMember().split(":")[1]));
                db.setValueInMap(badgeMetaKey, rule.getId() + ":lasttime", ts + ":" + currStreak + ":" + badgeStartTs + ":" + firstId);

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
    public List<BadgeSignal> fold(List<Record> tuples, Event event, BadgeStreakNRule rule, DbContext db) {
        List<BadgeSignal> signals = new ArrayList<>();
        BadgeTemporalStreakNRule options = (BadgeTemporalStreakNRule) rule;
        List<List<Record>> partitions = splitPartitions(tuples, options);
        if (partitions.isEmpty()) {
            return signals;
        }

        long lastBadgeTs = 0L;
        int lastBadgeStreak = 0;
        String badgeMetaKey = ID.getUserBadgesMetaKey(event.getGameId(), event.getUser());
        List<String> badgeInfos = db.getValuesFromMap(badgeMetaKey, getMetaEndTimeKey(this.rule), getMetaStreakKey(this.rule));
        lastBadgeTs = asLong(badgeInfos.get(0));
        lastBadgeStreak = asInt(badgeInfos.get(1));

        List<Integer> streaks = options.getStreaks();
        partitionStart: for (List<Record> partition : partitions) {
            int n = partition.size();
            Record firstTuple = partition.get(0);
            long startTs = captureTsFromTuple(firstTuple);
            for (int streak : streaks) {
                if (n >= streak) {
                    Record tupleAtStreak = partition.get(streak - 1);
                    long ts = captureTsFromTuple(tupleAtStreak);
                    if (ts - startTs > options.getTimeUnit()) {
                        continue partitionStart;
                    }
                    if (lastBadgeStreak == streak && startTs <= lastBadgeTs) {
                        continue;
                    }
                    signals.add(new StreakBadgeSignal(
                            options.getId(),
                            event,
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
    public List<BadgeSignal> unfold(List<Record> tuples, Event event, long ts, BadgeStreakNRule rule, DbContext db) {
        BadgeTemporalStreakNRule options = (BadgeTemporalStreakNRule) rule;
        List<BadgeSignal> signals = new ArrayList<>();
        long startTs = Math.max(0, ts - options.getTimeUnit());
        String badgeSpecKey = ID.getUserBadgeSpecKey(event.getGameId(), event.getUser(), options.getId());
        List<Record> badgesInRange = db.SORTED(badgeSpecKey).getRangeByScoreWithScores(startTs, ts);
        if (!badgesInRange.isEmpty()) {
            badgesInRange.stream().filter(t -> {
                String[] parts = t.getMember().split(":");
                long badgeEndTs = Long.parseLong(parts[0]);
                if (badgeEndTs < ts) {
                    return false;
                }
                return parts[1].equals(options.getId());
            }).map(t -> {
                String[] parts = t.getMember().split(":");
                return new BadgeRemoveSignal(options.getId(),
                        event.asEventScope(),
                        Integer.parseInt(parts[3]),
                        BigDecimal.valueOf(t.getScore()).longValue());
            }).forEach(signals::add);
        }
        signals.addAll(fold(tuples, event, rule, db));
        return signals;
    }

    public List<List<Record>> splitPartitions(List<Record> tuples, BadgeTemporalStreakNRule options) {
        List<Record> currentPartition = new ArrayList<>();
        List<List<Record>> partitions = new ArrayList<>();
        for (Record tuple : tuples) {
            String[] parts = tuple.getMember().split(":");
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
