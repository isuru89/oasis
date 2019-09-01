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
public class UserStateStatDto {

    private Integer userId;
    private Integer teamId;
    private Integer teamScopeId;

    private Integer ratingId;
    private String ratingDefName;
    private String ratingDefDisplayName;

    private Integer currentState;
    private String currentStateName;
    private String currentValue;
    private Double currentPoints;

    private String extId;
    private Long lastChangedAt;

    public String getCurrentStateName() {
        return currentStateName;
    }

    public void setCurrentStateName(String currentStateName) {
        this.currentStateName = currentStateName;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
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

    public Integer getRatingId() {
        return ratingId;
    }

    public void setRatingId(Integer ratingId) {
        this.ratingId = ratingId;
    }

    public String getRatingDefName() {
        return ratingDefName;
    }

    public void setRatingDefName(String ratingDefName) {
        this.ratingDefName = ratingDefName;
    }

    public String getRatingDefDisplayName() {
        return ratingDefDisplayName;
    }

    public void setRatingDefDisplayName(String ratingDefDisplayName) {
        this.ratingDefDisplayName = ratingDefDisplayName;
    }

    public Integer getCurrentState() {
        return currentState;
    }

    public void setCurrentState(Integer currentState) {
        this.currentState = currentState;
    }

    public String getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(String currentValue) {
        this.currentValue = currentValue;
    }

    public Double getCurrentPoints() {
        return currentPoints;
    }

    public void setCurrentPoints(Double currentPoints) {
        this.currentPoints = currentPoints;
    }

    public String getExtId() {
        return extId;
    }

    public void setExtId(String extId) {
        this.extId = extId;
    }

    public Long getLastChangedAt() {
        return lastChangedAt;
    }

    public void setLastChangedAt(Long lastChangedAt) {
        this.lastChangedAt = lastChangedAt;
    }
}
