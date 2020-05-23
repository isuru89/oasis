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

package io.github.oasis.elements.milestones;

import io.github.oasis.core.context.ExecutionContext;
import io.github.oasis.core.elements.AbstractRule;
import io.github.oasis.core.elements.EventBiValueResolver;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.TreeMap;

/**
 * @author Isuru Weerarathna
 */
public class MilestoneRule extends AbstractRule {

    private static final BigDecimal SMALL_DELTA = new BigDecimal("0.007");

    public static final String TRACK_PENALTIES = "TRACK_PENALTIES";
    public static final String SKIP_NEGATIVE_VALUES = "SKIP_NEGATIVE_VALUES";

    private List<Level> levels;
    private NavigableMap<BigDecimal, Level> levelMap;
    private BigDecimal lastLevelMilestone = BigDecimal.ZERO;
    private EventBiValueResolver<MilestoneRule, ExecutionContext> valueExtractor;

    public MilestoneRule(String id) {
        super(id);
    }

    public Optional<Level> getNextLevel(BigDecimal currentValue) {
        boolean exactValueContains = levelMap.containsKey(currentValue);
        Map.Entry<BigDecimal, Level> levelEntry = levelMap.ceilingEntry(exactValueContains ? currentValue.add(SMALL_DELTA) : currentValue);
        if (levelEntry == null) {
            return Optional.empty();
        } else {
            return Optional.of(levelEntry.getValue());
        }
    }

    public Optional<Level> getLevelFor(BigDecimal value) {
        Map.Entry<BigDecimal, Level> levelEntry = levelMap.floorEntry(value);
        if (levelEntry == null || levelEntry.getValue() == Level.LEVEL_ZERO) {
            return Optional.empty();
        } else {
            return Optional.of(levelEntry.getValue());
        }
    }

    public EventBiValueResolver<MilestoneRule, ExecutionContext> getValueExtractor() {
        return valueExtractor;
    }

    public void setValueExtractor(EventBiValueResolver<MilestoneRule, ExecutionContext> valueExtractor) {
        this.valueExtractor = valueExtractor;
    }

    public void setLevels(List<Level> levels) {
        this.levels = new ArrayList<>(levels);
        Collections.sort(this.levels);
        this.levelMap = new TreeMap<>();
        levelMap.put(BigDecimal.ZERO, Level.LEVEL_ZERO);
        for (Level level : levels) {
            levelMap.put(level.getMilestone(), level);
            lastLevelMilestone = lastLevelMilestone.max(level.getMilestone());
        }
    }

    public static class Level implements Comparable<Level> {

        static final Level LEVEL_ZERO = new Level(0, BigDecimal.ZERO);

        private int level;
        private BigDecimal milestone;

        public Level(int level, BigDecimal milestone) {
            this.level = level;
            this.milestone = milestone;
        }

        public int getLevel() {
            return level;
        }

        public BigDecimal getMilestone() {
            return milestone;
        }

        @Override
        public int compareTo(Level o) {
            return Comparator.comparing(Level::getMilestone).compare(this, o);
        }
    }
}
