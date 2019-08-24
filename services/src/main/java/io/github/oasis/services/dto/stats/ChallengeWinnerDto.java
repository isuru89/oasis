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

/**
 * @author iweerarathna
 */
public class ChallengeWinnerDto {

    private Long userId;
    private String userName;
    private String userNickname;
    private String userEmail;
    private String userAvatar;
    private Long teamId;
    private String teamName;
    private Long teamScopeId;
    private String teamScopeDisplayName;
    private Double pointsScored;
    private Integer winNo;
    private Long wonAt;
    private Long gameId;

    public Integer getWinNo() {
        return winNo;
    }

    public void setWinNo(Integer winNo) {
        this.winNo = winNo;
    }

    public String getUserNickname() {
        return userNickname;
    }

    public void setUserNickname(String userNickname) {
        this.userNickname = userNickname;
    }

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

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserAvatar() {
        return userAvatar;
    }

    public void setUserAvatar(String userAvatar) {
        this.userAvatar = userAvatar;
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

    public String getTeamScopeDisplayName() {
        return teamScopeDisplayName;
    }

    public void setTeamScopeDisplayName(String teamScopeDisplayName) {
        this.teamScopeDisplayName = teamScopeDisplayName;
    }

    public Double getPointsScored() {
        return pointsScored;
    }

    public void setPointsScored(Double pointsScored) {
        this.pointsScored = pointsScored;
    }

    public Long getWonAt() {
        return wonAt;
    }

    public void setWonAt(Long wonAt) {
        this.wonAt = wonAt;
    }

    public Long getGameId() {
        return gameId;
    }

    public void setGameId(Long gameId) {
        this.gameId = gameId;
    }
}
