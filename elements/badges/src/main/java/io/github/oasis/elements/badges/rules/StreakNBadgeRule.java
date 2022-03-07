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

package io.github.oasis.elements.badges.rules;

import io.github.oasis.core.elements.EventExecutionFilter;
import io.github.oasis.elements.badges.signals.BadgeSignal;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.TreeMap;

/**
 * @author Isuru Weerarathna
 */
public class StreakNBadgeRule extends BadgeRule {

    static final StreakProps DEFAULT_STREAK_PROPS = new StreakProps(null, 0);

    private int maxStreak = 0;
    private int minStreak = Integer.MAX_VALUE;
    private List<Integer> orderedStreakList;
    private NavigableMap<Integer, StreakProps> streakProps;
    private EventExecutionFilter criteria;
    private long retainTime;

    public StreakNBadgeRule(String id) {
        super(id);
    }

    @Override
    public void derivePointsInTo(BadgeSignal signal) {
        int attrId = signal.getAttribute();
        Map.Entry<Integer, StreakNBadgeRule.StreakProps> matchedStreak = streakProps.entrySet().stream()
                .filter(entry -> entry.getValue().getAttribute() == attrId)
                .findFirst()
                .orElse(null);

        if (matchedStreak != null) {
            if (Objects.nonNull(matchedStreak.getValue().getPoints())) {
                signal.setPointAwards(matchedStreak.getValue().getPointId(), matchedStreak.getValue().getPoints());
            } else {
                super.derivePointsInTo(signal);
            }
        }
    }

    public EventExecutionFilter getCriteria() {
        return criteria;
    }

    public void setCriteria(EventExecutionFilter criteria) {
        this.criteria = criteria;
    }

    public int getMaxStreak() {
        return maxStreak;
    }

    public boolean isLastStreak(int streak) {
        return streak == maxStreak;
    }

    public int findOnGoingStreak(int currStreak) {
        Integer streak = streakProps.floorKey(currStreak);
        return streak == null ? 0 : streak;
    }

    public int getAttributeForStreak(int streak) {
        return streakProps.getOrDefault(streak, DEFAULT_STREAK_PROPS).getAttribute();
    }

    public String getBadgeIdForStreak(int streak) {
        return streakProps.getOrDefault(streak, DEFAULT_STREAK_PROPS).getBadgeId();
    }

    public void setStreaks(Map<Integer, StreakProps> streaks) {
        this.streakProps = new TreeMap<>(streaks);
        this.streakProps.put(0, DEFAULT_STREAK_PROPS);
        for (Integer streak : streaks.keySet()) {
            maxStreak = Math.max(streak, maxStreak);
            minStreak = Math.min(streak, minStreak);
        }
        orderedStreakList = new ArrayList<>(streaks.keySet());
        orderedStreakList.sort(Comparator.naturalOrder());
    }

    public int getMinStreak() {
        return minStreak;
    }

    public void setRetainTime(long retainTime) {
        this.retainTime = retainTime;
    }

    public long getRetainTime() {
        return retainTime;
    }

    public List<Integer> getStreaks() {
        return orderedStreakList;
    }

    @Getter
    @Builder(toBuilder = true)
    public static class StreakProps {
        private final String badgeId;
        private final int attribute;
        private final String pointId;
        private final BigDecimal points;

        public StreakProps(String badgeId, int attribute) {
            this(badgeId, attribute, null);
        }

        public StreakProps(String badgeId, int attribute, BigDecimal points) {
            this.badgeId = badgeId;
            this.attribute = attribute;
            this.points = points;
            this.pointId = null;
        }

        public StreakProps(String badgeId, int attribute, String pointId, BigDecimal points) {
            this.badgeId = badgeId;
            this.attribute = attribute;
            this.pointId = pointId;
            this.points = points;
        }
    }
}
