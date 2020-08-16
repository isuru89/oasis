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

import java.util.Map;

/**
 * @author Isuru Weerarathna
 */
public class GameChallengesSummary {

    private Integer gameId;

    private Map<String, ChallengeSummary> challenges;

    public Integer getGameId() {
        return gameId;
    }

    public void setGameId(Integer gameId) {
        this.gameId = gameId;
    }

    public Map<String, ChallengeSummary> getChallenges() {
        return challenges;
    }

    public void setChallenges(Map<String, ChallengeSummary> challenges) {
        this.challenges = challenges;
    }

    public static class ChallengeSummary {
        private String challengeId;
        private int count;

        public ChallengeSummary() {
        }

        public ChallengeSummary(String challengeId, int count) {
            this.challengeId = challengeId;
            this.count = count;
        }

        public String getChallengeId() {
            return challengeId;
        }

        public void setChallengeId(String challengeId) {
            this.challengeId = challengeId;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }
    }

}
