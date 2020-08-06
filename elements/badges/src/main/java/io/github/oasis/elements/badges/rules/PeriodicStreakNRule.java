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

import io.github.oasis.core.context.ExecutionContext;
import io.github.oasis.core.elements.EventValueResolver;
import io.github.oasis.elements.badges.signals.BadgeSignal;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * @author Isuru Weerarathna
 */
public class PeriodicStreakNRule extends BadgeRule {

    private int maxStreak = 0;
    private int minStreak = Integer.MAX_VALUE;
    private List<Integer> orderedStreakList;
    private Map<Integer, StreakNBadgeRule.StreakProps> streakMap;
    private long timeUnit;
    protected EventValueResolver<ExecutionContext> valueResolver;

    private boolean consecutive;
    protected BigDecimal threshold;

    public PeriodicStreakNRule(String id) {
        super(id);
    }

    @Override
    public void derivePointsInTo(BadgeSignal signal) {
        int attrId = signal.getAttribute();
        Map.Entry<Integer, StreakNBadgeRule.StreakProps> matchedStreak = streakMap.entrySet().stream()
                .filter(entry -> entry.getValue().getAttribute() == attrId)
                .findFirst()
                .orElse(null);

        if (matchedStreak != null) {
            if (Objects.nonNull(matchedStreak.getValue().getPoints())) {
                signal.setPointAwards(getPointId(), matchedStreak.getValue().getPoints());
            } else {
                super.derivePointsInTo(signal);
            }
        }
    }

    public EventValueResolver<ExecutionContext> getValueResolver() {
        return valueResolver;
    }

    public void setValueResolver(EventValueResolver<ExecutionContext> valueResolver) {
        this.valueResolver = valueResolver;
    }

    public int getMaxStreak() {
        return maxStreak;
    }

    public boolean containsStreakMargin(int streak) {
        return streakMap.containsKey(streak);
    }

    public boolean isMaxStreakPassed(int streak) {
        return streak >= maxStreak;
    }

    public boolean isConsecutive() {
        return consecutive;
    }

    public void setConsecutive(boolean consecutive) {
        this.consecutive = consecutive;
    }

    public List<Integer> getStreaks() {
        return orderedStreakList;
    }

    public int getMinStreak() {
        return minStreak;
    }

    public int findAttributeOfStreak(int streak) {
        return streakMap.getOrDefault(streak, StreakNBadgeRule.DEFAULT_STREAK_PROPS).getAttribute();
    }

    public void setStreaks(Map<Integer, StreakNBadgeRule.StreakProps> streaks) {
        this.streakMap = new TreeMap<>(streaks);
        this.orderedStreakList = new ArrayList<>(streaks.keySet());
        this.orderedStreakList.sort(Comparator.naturalOrder());
        for (int streak : orderedStreakList) {
            maxStreak = Math.max(streak, maxStreak);
            minStreak = Math.min(streak, minStreak);
        }
    }

    public long getTimeUnit() {
        return timeUnit;
    }

    public void setTimeUnit(long timeUnit) {
        this.timeUnit = timeUnit;
    }

    public BigDecimal getThreshold() {
        return threshold;
    }

    public void setThreshold(BigDecimal threshold) {
        this.threshold = threshold;
    }
}
