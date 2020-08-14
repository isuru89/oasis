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

package io.github.oasis.elements.badges.stats.to;

import java.util.List;

/**
 * @author Isuru Weerarathna
 */
public class UserBadgeLog {

    private Integer gameId;
    private Long userId;

    private List<BadgeLogRecord> log;

    public List<BadgeLogRecord> getLog() {
        return log;
    }

    public void setLog(List<BadgeLogRecord> log) {
        this.log = log;
    }

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

    public static class BadgeLogRecord {
        private String badgeId;
        private int attribute;
        private long streakStartedAt;
        private String causedEventId;
        private long awardedAt;

        public BadgeLogRecord(String badgeId, int attribute, String causedEventId, long awardedAt) {
            this.badgeId = badgeId;
            this.attribute = attribute;
            this.causedEventId = causedEventId;
            this.awardedAt = awardedAt;
        }

        public BadgeLogRecord(String badgeId, int attribute, long streakStartedAt, long awardedAt) {
            this.badgeId = badgeId;
            this.attribute = attribute;
            this.streakStartedAt = streakStartedAt;
            this.awardedAt = awardedAt;
        }

        public String getBadgeId() {
            return badgeId;
        }

        public void setBadgeId(String badgeId) {
            this.badgeId = badgeId;
        }

        public int getAttribute() {
            return attribute;
        }

        public void setAttribute(int attribute) {
            this.attribute = attribute;
        }

        public long getStreakStartedAt() {
            return streakStartedAt;
        }

        public void setStreakStartedAt(long streakStartedAt) {
            this.streakStartedAt = streakStartedAt;
        }

        public String getCausedEventId() {
            return causedEventId;
        }

        public void setCausedEventId(String causedEventId) {
            this.causedEventId = causedEventId;
        }

        public long getAwardedAt() {
            return awardedAt;
        }

        public void setAwardedAt(long awardedAt) {
            this.awardedAt = awardedAt;
        }
    }
}
