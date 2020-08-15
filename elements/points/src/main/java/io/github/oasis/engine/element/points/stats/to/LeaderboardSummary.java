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

package io.github.oasis.engine.element.points.stats.to;

import io.github.oasis.core.api.AbstractStatsApiResponse;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Isuru Weerarathna
 */
public class LeaderboardSummary extends AbstractStatsApiResponse {

    private Integer teamId;

    private List<LeaderboardRecord> records = new ArrayList<>();

    public void addRecord(LeaderboardRecord record) {
        records.add(record);
    }

    public Integer getTeamId() {
        return teamId;
    }

    public void setTeamId(Integer teamId) {
        this.teamId = teamId;
    }

    public List<LeaderboardRecord> getRecords() {
        return records;
    }

    public void setRecords(List<LeaderboardRecord> records) {
        this.records = records;
    }

    public static class LeaderboardRecord {

        private int rank;
        private Long userId;
        private BigDecimal score;

        public LeaderboardRecord(int rank, Long userId, BigDecimal score) {
            this.rank = rank;
            this.userId = userId;
            this.score = score;
        }

        public int getRank() {
            return rank;
        }

        public void setRank(int rank) {
            this.rank = rank;
        }

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public BigDecimal getScore() {
            return score;
        }

        public void setScore(BigDecimal score) {
            this.score = score;
        }
    }

}
