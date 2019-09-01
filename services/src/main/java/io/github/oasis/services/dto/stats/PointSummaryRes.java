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

package io.github.oasis.services.dto.stats;

import java.util.List;

public class PointSummaryRes {

    private long count;
    private List<PointSummaryRecord> records;

    public static class PointSummaryRecord {
        private Long userId;
        private Long teamId;
        private Long teamScopeId;

        private Integer pointId;
        private String pointName;
        private String pointDisplayName;

        private Double totalPoints;
        private Long occurrences;
        private Double minAchieved;
        private Double maxAchieved;
        private Long firstAchieved;
        private Long lastAchieved;

        public String getPointName() {
            return pointName;
        }

        public void setPointName(String pointName) {
            this.pointName = pointName;
        }

        public String getPointDisplayName() {
            return pointDisplayName;
        }

        public void setPointDisplayName(String pointDisplayName) {
            this.pointDisplayName = pointDisplayName;
        }

        public Long getTeamId() {
            return teamId;
        }

        public void setTeamId(Long teamId) {
            this.teamId = teamId;
        }

        public Long getTeamScopeId() {
            return teamScopeId;
        }

        public void setTeamScopeId(Long teamScopeId) {
            this.teamScopeId = teamScopeId;
        }

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public Integer getPointId() {
            return pointId;
        }

        public void setPointId(Integer pointId) {
            this.pointId = pointId;
        }

        public Double getTotalPoints() {
            return totalPoints;
        }

        public void setTotalPoints(Double totalPoints) {
            this.totalPoints = totalPoints;
        }

        public Long getOccurrences() {
            return occurrences;
        }

        public void setOccurrences(Long occurrences) {
            this.occurrences = occurrences;
        }

        public Double getMinAchieved() {
            return minAchieved;
        }

        public void setMinAchieved(Double minAchieved) {
            this.minAchieved = minAchieved;
        }

        public Double getMaxAchieved() {
            return maxAchieved;
        }

        public void setMaxAchieved(Double maxAchieved) {
            this.maxAchieved = maxAchieved;
        }

        public Long getFirstAchieved() {
            return firstAchieved;
        }

        public void setFirstAchieved(Long firstAchieved) {
            this.firstAchieved = firstAchieved;
        }

        public Long getLastAchieved() {
            return lastAchieved;
        }

        public void setLastAchieved(Long lastAchieved) {
            this.lastAchieved = lastAchieved;
        }
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public List<PointSummaryRecord> getRecords() {
        return records;
    }

    public void setRecords(List<PointSummaryRecord> records) {
        this.records = records;
    }
}
