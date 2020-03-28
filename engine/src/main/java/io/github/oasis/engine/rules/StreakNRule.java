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

import io.github.oasis.engine.rules.signals.Signal;
import io.github.oasis.model.Event;

import java.util.List;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * @author Isuru Weerarathna
 */
public class StreakNRule extends BadgeRule {

    private int maxStreak = 0;
    private int minStreak = Integer.MAX_VALUE;
    private TreeSet<Integer> streakMap;
    private List<Integer> streaks;
    private Predicate<Event> criteria;
    private long retainTime;
    private Consumer<Signal> collector;

    public Predicate<Event> getCriteria() {
        return criteria;
    }

    public void setCriteria(Predicate<Event> criteria) {
        this.criteria = criteria;
    }

    public StreakNRule(String id) {
        super(id);
    }

    public Consumer<Signal> getCollector() {
        return collector;
    }

    public void setCollector(Consumer<Signal> collector) {
        this.collector = collector;
    }

    public int getMaxStreak() {
        return maxStreak;
    }

    public TreeSet<Integer> getStreakMap() {
        return streakMap;
    }

    public void setStreaks(List<Integer> streaks) {
        this.streaks = streaks;
        streakMap = new TreeSet<>();
        streakMap.add(0);
        for (Integer streak : streaks) {
            maxStreak = Math.max(streak, maxStreak);
            minStreak = Math.min(streak, minStreak);
            streakMap.add(streak);
        }
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
        return streaks;
    }

}
