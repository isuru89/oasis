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

package io.github.oasis.game.states;

import io.github.oasis.model.Milestone;
import lombok.Getter;

import java.io.Serializable;
import java.util.Optional;

/**
 * @author Isuru Weerarathna
 */
@Getter
public class MilestoneState implements Serializable {

    private long milestoneId;

    private long totalCount;

    private int currentLevel;
    private int nextLevel;

    public static MilestoneState from(Milestone milestone) {
        MilestoneState state = new MilestoneState();
        state.totalCount = 0;
        state.currentLevel = milestone.getStartingLevel() != null ? milestone.getStartingLevel().intValue() : 0;
        state.nextLevel = state.currentLevel + 1;
        state.milestoneId = milestone.getId();
        return state;
    }

    public long getCurrentLevelTarget(Milestone milestone) {
        Milestone.Level level = milestone.getLevel(currentLevel);
        if (level != null) {
            return level.getNumber().longValue();
        }
        return -1;
    }

    public long getNextLevelTarget(Milestone milestone) {
        Milestone.Level level = milestone.getLevel(nextLevel);
        if (level != null) {
            return level.getNumber().longValue();
        }
        return -1;
    }

    public boolean isAllLevelsReached() {
        return nextLevel < 0;
    }

    public MilestoneState incrementCountAndGet() {
        totalCount++;
        return this;
    }

    public boolean hasLevelChanged(Milestone.Level derivedLevel) {
        return derivedLevel.getLevel() > currentLevel;
    }

    public void updateLevelTo(Milestone.Level derivedLevel, Milestone milestone) {
        currentLevel = derivedLevel.getLevel();
        nextLevel = -1;
        Optional<Milestone.Level> nextLevelForValue = milestone.findNextLevelForValue(totalCount);
        nextLevelForValue.ifPresent(level -> nextLevel = level.getLevel());
    }
}
