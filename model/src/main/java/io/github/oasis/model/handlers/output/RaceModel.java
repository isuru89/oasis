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

package io.github.oasis.model.handlers.output;

import java.io.Serializable;

public class RaceModel implements Serializable {

    private Long userId;
    private Long teamId;
    private Long teamScopeId;

    private Integer sourceId;
    private Integer gameId;

    private Integer raceId;
    private Double points;
    private Long raceStartedAt;
    private Long raceEndedAt;
    private Integer rank;
    private Double scoredPoints;
    private Long scoredCount;

    private Long ts;

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

    public Integer getSourceId() {
        return sourceId;
    }

    public void setSourceId(Integer sourceId) {
        this.sourceId = sourceId;
    }

    public Integer getGameId() {
        return gameId;
    }

    public void setGameId(Integer gameId) {
        this.gameId = gameId;
    }

    public Integer getRaceId() {
        return raceId;
    }

    public void setRaceId(Integer raceId) {
        this.raceId = raceId;
    }

    public Double getPoints() {
        return points;
    }

    public void setPoints(Double points) {
        this.points = points;
    }

    public Long getRaceStartedAt() {
        return raceStartedAt;
    }

    public void setRaceStartedAt(Long raceStartedAt) {
        this.raceStartedAt = raceStartedAt;
    }

    public Long getRaceEndedAt() {
        return raceEndedAt;
    }

    public void setRaceEndedAt(Long raceEndedAt) {
        this.raceEndedAt = raceEndedAt;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    public Double getScoredPoints() {
        return scoredPoints;
    }

    public void setScoredPoints(Double scoredPoints) {
        this.scoredPoints = scoredPoints;
    }

    public Long getScoredCount() {
        return scoredCount;
    }

    public void setScoredCount(Long scoredCount) {
        this.scoredCount = scoredCount;
    }

    public Long getTs() {
        return ts;
    }

    public void setTs(Long ts) {
        this.ts = ts;
    }
}
