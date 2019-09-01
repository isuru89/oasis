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

package io.github.oasis.services.dto.game;

public class TeamLeaderboardRecordDto {

    private Long userId;
    private String userName;

    private Long teamId;
    private String teamName;

    private Long teamScopeId;
    private String teamScopeName;

    private Double totalPoints;
    private Long totalCount;
    private Integer rankInTeam;
    private Integer rankInTeamScope;
    private Double nextTeamRankValue;
    private Double nextTeamScopeRankValue;
    private Long calculatedTime;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Long getTeamId() {
        return teamId;
    }

    public void setTeamId(Long teamId) {
        this.teamId = teamId;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public Long getTeamScopeId() {
        return teamScopeId;
    }

    public void setTeamScopeId(Long teamScopeId) {
        this.teamScopeId = teamScopeId;
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

    public Long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Long totalCount) {
        this.totalCount = totalCount;
    }

    public Integer getRankInTeam() {
        return rankInTeam;
    }

    public void setRankInTeam(Integer rankInTeam) {
        this.rankInTeam = rankInTeam;
    }

    public Integer getRankInTeamScope() {
        return rankInTeamScope;
    }

    public void setRankInTeamScope(Integer rankInTeamScope) {
        this.rankInTeamScope = rankInTeamScope;
    }

    public Double getNextTeamRankValue() {
        return nextTeamRankValue;
    }

    public void setNextTeamRankValue(Double nextTeamRankValue) {
        this.nextTeamRankValue = nextTeamRankValue;
    }

    public Double getNextTeamScopeRankValue() {
        return nextTeamScopeRankValue;
    }

    public void setNextTeamScopeRankValue(Double nextTeamScopeRankValue) {
        this.nextTeamScopeRankValue = nextTeamScopeRankValue;
    }

    public Long getCalculatedTime() {
        return calculatedTime;
    }

    public void setCalculatedTime(Long calculatedTime) {
        this.calculatedTime = calculatedTime;
    }

    @Override
    public String toString() {
        return "{" +
                "userId=" + userId +
                ", userName='" + userName + '\'' +
                ", teamId=" + teamId +
                ", teamName='" + teamName + '\'' +
                ", teamScopeId=" + teamScopeId +
                ", teamScopeName='" + teamScopeName + '\'' +
                ", totalPoints=" + totalPoints +
                ", totalCount=" + totalCount +
                ", rankInTeam=" + rankInTeam +
                ", rankInTeamScope=" + rankInTeamScope +
                ", nextTeamRankValue=" + nextTeamRankValue +
                ", nextTeamScopeRankValue=" + nextTeamScopeRankValue +
                ", calculatedTime=" + calculatedTime +
                '}';
    }
}
