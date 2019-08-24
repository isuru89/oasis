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

package io.github.oasis.model.defs;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * @author iweerarathna
 */
public class ChallengeDef extends BaseDef implements Serializable {

    private long expireAfter;
    private Long startAt;
    private long winnerCount;

    private double points;

    private Collection<String> forEvents;
    private Long forTeamId;
    private String forTeam;
    private Long forUserId;
    private String forUser;
    private String forTeamScope;
    private Long forTeamScopeId;
    private List<String> conditions;

    private Long gameId;

    public Long getGameId() {
        return gameId;
    }

    public void setGameId(Long gameId) {
        this.gameId = gameId;
    }

    public String getForTeamScope() {
        return forTeamScope;
    }

    public void setForTeamScope(String forTeamScope) {
        this.forTeamScope = forTeamScope;
    }

    public Long getForTeamScopeId() {
        return forTeamScopeId;
    }

    public void setForTeamScopeId(Long forTeamScopeId) {
        this.forTeamScopeId = forTeamScopeId;
    }

    public Long getForTeamId() {
        return forTeamId;
    }

    public void setForTeamId(Long forTeamId) {
        this.forTeamId = forTeamId;
    }

    public String getForTeam() {
        return forTeam;
    }

    public void setForTeam(String forTeam) {
        this.forTeam = forTeam;
    }

    public Long getForUserId() {
        return forUserId;
    }

    public void setForUserId(Long forUserId) {
        this.forUserId = forUserId;
    }

    public String getForUser() {
        return forUser;
    }

    public void setForUser(String forUser) {
        this.forUser = forUser;
    }

    public long getExpireAfter() {
        return expireAfter;
    }

    public void setExpireAfter(long expireAfter) {
        this.expireAfter = expireAfter;
    }

    public Long getStartAt() {
        return startAt;
    }

    public void setStartAt(Long startAt) {
        this.startAt = startAt;
    }

    public long getWinnerCount() {
        return winnerCount;
    }

    public void setWinnerCount(long winnerCount) {
        this.winnerCount = winnerCount;
    }

    public double getPoints() {
        return points;
    }

    public void setPoints(double points) {
        this.points = points;
    }

    public Collection<String> getForEvents() {
        return forEvents;
    }

    public void setForEvents(Collection<String> forEvents) {
        this.forEvents = forEvents;
    }

    public List<String> getConditions() {
        return conditions;
    }

    public void setConditions(List<String> conditions) {
        this.conditions = conditions;
    }
}
