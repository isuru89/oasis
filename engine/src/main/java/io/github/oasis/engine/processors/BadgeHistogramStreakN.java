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
import io.github.oasis.engine.rules.BadgeHistogramStreakNRule;
import io.github.oasis.engine.rules.signals.BadgeSignal;
import io.github.oasis.engine.rules.signals.HistogramBadgeRemovalSignal;
import io.github.oasis.engine.rules.signals.HistogramBadgeSignal;
import io.github.oasis.engine.external.Db;
import io.github.oasis.engine.external.DbContext;
import io.github.oasis.engine.external.Mapped;
import io.github.oasis.engine.external.Sorted;
import io.github.oasis.model.Event;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
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
public class BadgeHistogramStreakN extends BadgeProcessor<BadgeHistogramStreakNRule> {

    public BadgeHistogramStreakN(Db pool, RuleContext<BadgeHistogramStreakNRule> ruleContext) {
        super(pool, ruleContext);
    }

    @Override
    public List<BadgeSignal> process(Event event, BadgeHistogramStreakNRule rule, DbContext db) {
        String badgeKey = ID.getBadgeHistogramKey(event.getGameId(), event.getUser(), rule.getId());
        Sorted sortedRange = db.SORTED(badgeKey);
        long timestamp = event.getTimestamp() - (event.getTimestamp() % rule.getTimeUnit());
        BigDecimal value = evaluateForValue(event).setScale(SCALE, BigDecimal.ROUND_HALF_UP);
        Optional<String> memberByScore = sortedRange.getMemberByScore(timestamp);
        BigDecimal prev = BigDecimal.ZERO;
        String prevMember = null;
        if (memberByScore.isPresent()) {
            String member = memberByScore.get();
            String[] parts = member.split(COLON);
            prev = asDecimal(parts[1]);
            prevMember = member;
        }
        BigDecimal updatedValue = addToScale(prev, value, SCALE);
        if (prevMember != null) {
            sortedRange.remove(prevMember);
        }
        sortedRange.add(timestamp + COLON + updatedValue.toString(), timestamp);
        if (isThresholdCrossedUp(prev, updatedValue, rule.getThreshold())) {
            List<Record> seq = sortedRange.getRangeByScoreWithScores(
                    timestamp - rule.getTimeUnit() * rule.getMaxStreak(),
                    timestamp + rule.getTimeUnit() * rule.getMaxStreak());

            if (rule.isConsecutive()) {
                return fold(seq, event, rule, db, false);
            } else {
                String metaBadgesInfoKey = ID.getUserBadgesMetaKey(event.getGameId(), event.getUser());
                Mapped map = db.MAP(metaBadgesInfoKey);
                String totalSubKey = String.format("%s:total", rule.getId());
                String firstSubKey = String.format("%s:first", rule.getId());
                String lastHitSubKey = String.format("%s:hit", rule.getId());
                int curr = map.getValueAsInt(totalSubKey);
                if (rule.isMaxStreakPassed(curr)) {
                    map.setValue(totalSubKey, 0);
                    map.remove(firstSubKey);
                }
                int total = map.incrementByInt(totalSubKey, 1);
                map.setIfNotExists(firstSubKey, event.getTimestamp() + COLON + event.getExternalId());
                if (rule.containsStreakMargin(total)) {
                    List<String> first = map.getValues(firstSubKey, lastHitSubKey);
                    String[] parts = first.get(0).split(COLON);
                    long ts = Long.parseLong(parts[0]);
                    long lastTs = event.getTimestamp();
                    lastTs = lastTs - (lastTs % rule.getTimeUnit());
                    map.setValue(lastHitSubKey, event.getTimestamp() + COLON + event.getExternalId());
                    return Collections.singletonList(new HistogramBadgeSignal(rule.getId(),
                            event,
                            total,
                            ts - (ts % rule.getTimeUnit()),
                            lastTs,
                            event.getExternalId()));
                }
            }

        } else if (isThresholdCrossedDown(prev, updatedValue, rule.getThreshold())) {
            List<Record> seq = sortedRange.getRangeByScoreWithScores(
                    timestamp - rule.getTimeUnit() * rule.getMaxStreak(),
                    timestamp + rule.getTimeUnit() * rule.getMaxStreak());

            if (rule.isConsecutive()) {
                return unfold(seq, event, timestamp, rule, db);
            } else {
                String metaBadgesInfoKey = ID.getUserBadgesMetaKey(event.getGameId(), event.getUser());
                Mapped map = db.MAP(metaBadgesInfoKey);
                String totalSubKey = String.format("%s:total", rule.getId());
                String firstSubKey = String.format("%s:first", rule.getId());
                String lastHitSubKey = String.format("%s:hit", rule.getId());
                int curr = map.getValueAsInt(totalSubKey);
                if (curr <= 0) {
                    return null;
                }
                int total = map.incrementByInt(totalSubKey, -1);
                if (total == 0) {
                    map.remove(firstSubKey);
                }
                if (rule.containsStreakMargin(total + 1)) {
                    List<String> first = map.getValues(firstSubKey, lastHitSubKey);
                    String[] parts = first.get(0).split(COLON);
                    long ts = Long.parseLong(parts[0]);
                    long lastTs = event.getTimestamp();
                    if (first.size() > 1 && first.get(1) != null) {
                        lastTs = Long.parseLong(first.get(1).split(COLON)[0]);
                    }
                    lastTs = lastTs - (lastTs % rule.getTimeUnit());
                    map.setValue(lastHitSubKey, event.getTimestamp() + COLON + event.getExternalId());
                    return Collections.singletonList(new HistogramBadgeRemovalSignal(rule.getId(),
                            event.asEventScope(),
                            total + 1,
                            ts - (ts % rule.getTimeUnit()),
                            lastTs));
                }
            }
        }
        return null;
    }

    public List<BadgeSignal> unfold(List<Record> tuples, Event event, long ts, BadgeHistogramStreakNRule rule, DbContext db) {
        List<BadgeSignal> signals = new ArrayList<>();
        List<Record> filteredTuples = tuples.stream().map(t -> {
            String[] parts = t.getMember().split(COLON);
            if (Long.parseLong(parts[0]) != ts) {
                return new Record(t.getMember(), t.getScore());
            } else {
                return new Record(parts[0] + COLON + rule.getThreshold().toString(), t.getScore());
            }
        }).collect(Collectors.toCollection(LinkedList::new));
        List<BadgeSignal> badgesAwarded = fold(filteredTuples, event, rule, db, true);
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

        List<Record> futureTuples = tuples.stream().filter(t -> {
            String[] parts = t.getMember().split(COLON);
            return Long.parseLong(parts[0]) > ts;
        }).map(tuple -> new Record(tuple.getMember(), tuple.getScore()))
                .collect(Collectors.toCollection(LinkedList::new));
        signals.addAll(fold(futureTuples, event, rule, db, false));
        return signals;
    }

    public List<BadgeSignal> fold(List<Record> tuples, Event event, BadgeHistogramStreakNRule rule, DbContext db, boolean skipOldCheck) {
        List<BadgeSignal> signals = new ArrayList<>();
        List<List<Record>> partitions = splitPartitions(tuples, rule);
        if (partitions.isEmpty()) {
            return signals;
        }

        long lastBadgeTs = 0L;
        int lastBadgeStreak = 0;
        String badgeMetaKey = ID.getUserBadgesMetaKey(event.getGameId(), event.getUser());
        List<String> badgeInfos = db.getValuesFromMap(badgeMetaKey, getMetaEndTimeKey(rule), getMetaStreakKey(rule));
        lastBadgeTs = asLong(badgeInfos.get(0));
        lastBadgeStreak = asInt(badgeInfos.get(1));

        List<Integer> streaks = rule.getStreaks();
        partitionStart: for (List<Record> partition : partitions) {
            int n = partition.size();
            Record firstTuple = partition.get(0);
            long startTs = Math.round(firstTuple.getScore());
            for (int streak : streaks) {
                if (n >= streak) {
                    Record tupleAtStreak = partition.get(streak - 1);
                    long ts = Math.round(tupleAtStreak.getScore());
                    if (ts - startTs >= rule.getTimeUnit() * streak) {
                        continue partitionStart;
                    }
                    if (!skipOldCheck && lastBadgeStreak == streak && startTs <= lastBadgeTs) {
                        continue;
                    }
                    signals.add(new HistogramBadgeSignal(
                            rule.getId(),
                            event,
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

    private List<List<Record>> splitPartitions(List<Record> tuples, BadgeHistogramStreakNRule options) {
        List<Record> currentPartition = new ArrayList<>();
        List<List<Record>> partitions = new ArrayList<>();
        for (Record tuple : tuples) {
            String[] parts = tuple.getMember().split(":");
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
