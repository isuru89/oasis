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

package io.github.oasis.elements.challenges.stats.to;

import java.util.List;

/**
 * @author Isuru Weerarathna
 */
public class UserChallengesLog {

    private Integer gameId;
    private Long userId;

    private List<ChallengeRecord> winnings;

    public static class ChallengeRecord {
        private String challengeId;
        private int rank;
        private long wonAt;
        private String causedEventId;

        public ChallengeRecord() {
        }

        public ChallengeRecord(String challengeId, int rank, long wonAt, String causedEventId) {
            this.challengeId = challengeId;
            this.rank = rank;
            this.wonAt = wonAt;
            this.causedEventId = causedEventId;
        }

        public String getChallengeId() {
            return challengeId;
        }

        public void setChallengeId(String challengeId) {
            this.challengeId = challengeId;
        }

        public int getRank() {
            return rank;
        }

        public void setRank(int rank) {
            this.rank = rank;
        }

        public long getWonAt() {
            return wonAt;
        }

        public void setWonAt(long wonAt) {
            this.wonAt = wonAt;
        }

        public String getCausedEventId() {
            return causedEventId;
        }

        public void setCausedEventId(String causedEventId) {
            this.causedEventId = causedEventId;
        }
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

    public List<ChallengeRecord> getWinnings() {
        return winnings;
    }

    public void setWinnings(List<ChallengeRecord> winnings) {
        this.winnings = winnings;
    }
}
