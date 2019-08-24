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

import io.github.oasis.model.events.JsonEvent;

import java.io.Serializable;

/**
 * @author iweerarathna
 */
public class MilestoneModel implements Serializable {

    private Long userId;
    private Long teamId;
    private Long teamScopeId;

    private JsonEvent event;
    private String eventType;
    private Integer level;
    private Integer maximumLevel;
    private Long milestoneId;
    private Long ts;
    private Integer sourceId;
    private Integer gameId;

    public Integer getMaximumLevel() {
        return maximumLevel;
    }

    public void setMaximumLevel(Integer maximumLevel) {
        this.maximumLevel = maximumLevel;
    }

    public Integer getGameId() {
        return gameId;
    }

    public void setGameId(Integer gameId) {
        this.gameId = gameId;
    }

    public Integer getSourceId() {
        return sourceId;
    }

    public void setSourceId(Integer sourceId) {
        this.sourceId = sourceId;
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

    public JsonEvent getEvent() {
        return event;
    }

    public void setEvent(JsonEvent event) {
        this.event = event;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Long getMilestoneId() {
        return milestoneId;
    }

    public void setMilestoneId(Long milestoneId) {
        this.milestoneId = milestoneId;
    }

    public Long getTs() {
        return ts;
    }

    public void setTs(Long ts) {
        this.ts = ts;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }
}
