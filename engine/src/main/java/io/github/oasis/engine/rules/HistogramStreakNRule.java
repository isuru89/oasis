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

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author Isuru Weerarathna
 */
public class HistogramStreakNRule extends BadgeRule {

    private int maxStreak = 0;
    private int minStreak = Integer.MAX_VALUE;
    private List<Integer> streaks;
    private long timeUnit;
    private Consumer<Signal> consumer;
    private Function<Event, Double> valueResolver;

    private boolean consecutive;
    private BigDecimal threshold;

    public Function<Event, Double> getValueResolver() {
        return valueResolver;
    }

    public void setValueResolver(Function<Event, Double> valueResolver) {
        this.valueResolver = valueResolver;
    }

    public Consumer<Signal> getConsumer() {
        return consumer;
    }

    public void setConsumer(Consumer<Signal> consumer) {
        this.consumer = consumer;
    }

    public int getMaxStreak() {
        return maxStreak;
    }

    public boolean containsStreakMargin(int streak) {
        return streaks.contains(streak);
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
        return streaks;
    }

    public int getMinStreak() {
        return minStreak;
    }

    public void setStreaks(List<Integer> streaks) {
        this.streaks = streaks;
        for (int streak : streaks) {
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
