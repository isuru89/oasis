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

import java.util.Set;

/**
 * @author Isuru Weerarathna
 */
public class GameChallengeRequest {

    private Integer gameId;

    private Set<String> challengeIds;

    private Integer rankStart;
    private Integer rankEnd;
    private Integer latestWinnerCount = 1;
    private Integer firstWinnerCount;

    public boolean isCustomRange() {
        return rankStart != null && rankEnd != null;
    }

    public Integer getRankStart() {
        return rankStart;
    }

    public void setRankStart(Integer rankStart) {
        this.rankStart = rankStart;
    }

    public Integer getRankEnd() {
        return rankEnd;
    }

    public void setRankEnd(Integer rankEnd) {
        this.rankEnd = rankEnd;
    }

    public Integer getLatestWinnerCount() {
        return latestWinnerCount;
    }

    public void setLatestWinnerCount(Integer latestWinnerCount) {
        this.latestWinnerCount = latestWinnerCount;
    }

    public Integer getFirstWinnerCount() {
        return firstWinnerCount;
    }

    public void setFirstWinnerCount(Integer firstWinnerCount) {
        this.firstWinnerCount = firstWinnerCount;
    }

    public Integer getGameId() {
        return gameId;
    }

    public void setGameId(Integer gameId) {
        this.gameId = gameId;
    }

    public Set<String> getChallengeIds() {
        return challengeIds;
    }

    public void setChallengeIds(Set<String> challengeIds) {
        this.challengeIds = challengeIds;
    }
}
