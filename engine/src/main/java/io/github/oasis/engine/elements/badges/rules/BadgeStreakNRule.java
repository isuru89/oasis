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

package io.github.oasis.engine.elements.badges.rules;

import io.github.oasis.core.elements.EventExecutionFilter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * @author Isuru Weerarathna
 */
public class BadgeStreakNRule extends BadgeRule {

    private int maxStreak = 0;
    private int minStreak = Integer.MAX_VALUE;
    private List<Integer> orderedStreakList;
    private NavigableMap<Integer, Integer> streakAttrMap;
    private EventExecutionFilter criteria;
    private long retainTime;

    public EventExecutionFilter getCriteria() {
        return criteria;
    }

    public void setCriteria(EventExecutionFilter criteria) {
        this.criteria = criteria;
    }

    public BadgeStreakNRule(String id) {
        super(id);
    }

    public int getMaxStreak() {
        return maxStreak;
    }

    public boolean isLastStreak(int streak) {
        return streak == maxStreak;
    }

    public int findOnGoingStreak(int currStreak) {
        Integer streak = streakAttrMap.floorKey(currStreak);
        return streak == null ? 0 : streak;
    }

    public int getAttributeForStreak(int streak) {
        return streakAttrMap.getOrDefault(streak, 0);
    }

    public void setStreaks(Map<Integer, Integer> streaks) {
        this.streakAttrMap = new TreeMap<>(streaks);
        this.streakAttrMap.put(0, 0);
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

}
