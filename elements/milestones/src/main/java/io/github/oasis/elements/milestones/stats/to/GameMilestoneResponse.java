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
import java.util.List;
import java.util.Map;

/**
 * @author Isuru Weerarathna
 */
public class GameMilestoneResponse {

    private Integer gameId;

    private Map<String, MilestoneSummary> summaries;
    private List<UserMilestoneRecord> records;

    public static class UserMilestoneRecord {
        private long userId;
        private long rank;
        private BigDecimal score;

        public UserMilestoneRecord() {
        }

        public UserMilestoneRecord(long userId, long rank, BigDecimal score) {
            this.userId = userId;
            this.rank = rank;
            this.score = score;
        }

        public long getUserId() {
            return userId;
        }

        public void setUserId(long userId) {
            this.userId = userId;
        }

        public long getRank() {
            return rank;
        }

        public void setRank(long rank) {
            this.rank = rank;
        }

        public BigDecimal getScore() {
            return score;
        }

        public void setScore(BigDecimal score) {
            this.score = score;
        }
    }

    public static class MilestoneSummary {
        private String milestoneId;

        private Map<String, Map<String, Long>> byTeams;
        private Map<String, Long> all;

        public String getMilestoneId() {
            return milestoneId;
        }

        public void setMilestoneId(String milestoneId) {
            this.milestoneId = milestoneId;
        }

        public Map<String, Map<String, Long>> getByTeams() {
            return byTeams;
        }

        public void setByTeams(Map<String, Map<String, Long>> byTeams) {
            this.byTeams = byTeams;
        }

        public Map<String, Long> getAll() {
            return all;
        }

        public void setAll(Map<String, Long> all) {
            this.all = all;
        }
    }

    public Integer getGameId() {
        return gameId;
    }

    public void setGameId(Integer gameId) {
        this.gameId = gameId;
    }

    public Map<String, MilestoneSummary> getSummaries() {
        return summaries;
    }

    public void setSummaries(Map<String, MilestoneSummary> summaries) {
        this.summaries = summaries;
    }

    public List<UserMilestoneRecord> getRecords() {
        return records;
    }

    public void setRecords(List<UserMilestoneRecord> records) {
        this.records = records;
    }
}
