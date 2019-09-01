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

public class TeamHistoryRecordDto {

    private Integer userId;
    private Integer teamId;
    private Integer teamScopeId;
    private String teamName;
    private String teamScopeName;

    private Double totalPoints;
    private Long totalCount;
    private Integer totalBadges;
    private Integer totalUniqueBadges;
    private Integer totalChallengeWins;
    private Integer totalRaceWins;

    public Integer getTotalRaceWins() {
        return totalRaceWins;
    }

    public void setTotalRaceWins(Integer totalRaceWins) {
        this.totalRaceWins = totalRaceWins;
    }

    public Long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Long totalCount) {
        this.totalCount = totalCount;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getTotalUniqueBadges() {
        return totalUniqueBadges;
    }

    public void setTotalUniqueBadges(Integer totalUniqueBadges) {
        this.totalUniqueBadges = totalUniqueBadges;
    }

    public Integer getTeamId() {
        return teamId;
    }

    public void setTeamId(Integer teamId) {
        this.teamId = teamId;
    }

    public Integer getTeamScopeId() {
        return teamScopeId;
    }

    public void setTeamScopeId(Integer teamScopeId) {
        this.teamScopeId = teamScopeId;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public String getTeamScopeName() {
        return teamScopeName;
    }

    public void setTeamScopeName(String teamScopeName) {
        this.teamScopeName = teamScopeName;
    }

    public Double getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(Double totalPoints) {
        this.totalPoints = totalPoints;
    }

    public Integer getTotalBadges() {
        return totalBadges;
    }

    public void setTotalBadges(Integer totalBadges) {
        this.totalBadges = totalBadges;
    }

    public Integer getTotalChallengeWins() {
        return totalChallengeWins;
    }

    public void setTotalChallengeWins(Integer totalChallengeWins) {
        this.totalChallengeWins = totalChallengeWins;
    }
}
