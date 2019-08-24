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

public class BadgeSummaryRes {

    private long count;
    private List<BadgeSummaryRecord> records;

    public static class BadgeSummaryRecord {
        private Long userId;
        private Long teamId;
        private Long teamScopeId;

        private Integer badgeId;
        private String badgeName;
        private String badgeDisplayName;

        private String subBadgeId;
        private Integer badgeAttribute;
        private String badgeAttributeName;

        private Long badgeCount;

        public String getBadgeName() {
            return badgeName;
        }

        public void setBadgeName(String badgeName) {
            this.badgeName = badgeName;
        }

        public String getBadgeDisplayName() {
            return badgeDisplayName;
        }

        public void setBadgeDisplayName(String badgeDisplayName) {
            this.badgeDisplayName = badgeDisplayName;
        }

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
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

        public Integer getBadgeId() {
            return badgeId;
        }

        public void setBadgeId(Integer badgeId) {
            this.badgeId = badgeId;
        }

        public String getSubBadgeId() {
            return subBadgeId;
        }

        public void setSubBadgeId(String subBadgeId) {
            this.subBadgeId = subBadgeId;
        }

        public Integer getBadgeAttribute() {
            return badgeAttribute;
        }

        public void setBadgeAttribute(Integer badgeAttribute) {
            this.badgeAttribute = badgeAttribute;
        }

        public String getBadgeAttributeName() {
            return badgeAttributeName;
        }

        public void setBadgeAttributeName(String badgeAttributeName) {
            this.badgeAttributeName = badgeAttributeName;
        }

        public Long getBadgeCount() {
            return badgeCount;
        }

        public void setBadgeCount(Long badgeCount) {
            this.badgeCount = badgeCount;
        }
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public List<BadgeSummaryRecord> getRecords() {
        return records;
    }

    public void setRecords(List<BadgeSummaryRecord> records) {
        this.records = records;
    }
}
