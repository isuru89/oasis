/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.github.oasis.elements.milestones.stats.to;

import java.math.BigDecimal;
import java.util.Map;

/**
 * @author Isuru Weerarathna
 */
public class UserMilestoneSummary {

    private Integer gameId;
    private Long userId;

    private Map<String, MilestoneSummary> milestones;

    public Integer getGameId() {
        return gameId;
    }

    public void setGameId(Integer gameId) {
        this.gameId = gameId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Map<String, MilestoneSummary> getMilestones() {
        return milestones;
    }

    public void setMilestones(Map<String, MilestoneSummary> milestones) {
        this.milestones = milestones;
    }

    public static class MilestoneSummary {
        private String milestoneId;
        private BigDecimal currentValue;
        private int currentLevel;
        private boolean completed;
        private int nextLevel;
        private BigDecimal nextLevelValue;
        private long lastLevelUpdatedAt;
        private String lastCausedEventId;
        private long lastUpdatedAt;

        public BigDecimal getCurrentValue() {
            return currentValue;
        }

        public void setCurrentValue(BigDecimal currentValue) {
            this.currentValue = currentValue;
        }

        public String getMilestoneId() {
            return milestoneId;
        }

        public void setMilestoneId(String milestoneId) {
            this.milestoneId = milestoneId;
        }

        public int getCurrentLevel() {
            return currentLevel;
        }

        public void setCurrentLevel(int currentLevel) {
            this.currentLevel = currentLevel;
        }

        public boolean isCompleted() {
            return completed;
        }

        public void setCompleted(boolean completed) {
            this.completed = completed;
        }

        public int getNextLevel() {
            return nextLevel;
        }

        public void setNextLevel(int nextLevel) {
            this.nextLevel = nextLevel;
        }

        public BigDecimal getNextLevelValue() {
            return nextLevelValue;
        }

        public void setNextLevelValue(BigDecimal nextLevelValue) {
            this.nextLevelValue = nextLevelValue;
        }

        public long getLastLevelUpdatedAt() {
            return lastLevelUpdatedAt;
        }

        public void setLastLevelUpdatedAt(long lastLevelUpdatedAt) {
            this.lastLevelUpdatedAt = lastLevelUpdatedAt;
        }

        public long getLastUpdatedAt() {
            return lastUpdatedAt;
        }

        public void setLastUpdatedAt(long lastUpdatedAt) {
            this.lastUpdatedAt = lastUpdatedAt;
        }

        public String getLastCausedEventId() {
            return lastCausedEventId;
        }

        public void setLastCausedEventId(String lastCausedEventId) {
            this.lastCausedEventId = lastCausedEventId;
        }
    }

}
