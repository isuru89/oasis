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

package io.github.oasis.services.model;

/**
 * @author iweerarathna
 */
public class UserTeam {

    private Long id;
    private Long userId;
    private Integer teamId;
    private Integer roleId;
    private Integer scopeId;
    private Long joinedTime;
    private Long deallocatedTime;
    private boolean approved;
    private boolean autoTeam;

    public boolean isAutoTeam() {
        return autoTeam;
    }

    public void setAutoTeam(boolean autoTeam) {
        this.autoTeam = autoTeam;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public Long getDeallocatedTime() {
        return deallocatedTime;
    }

    public void setDeallocatedTime(Long deallocatedTime) {
        this.deallocatedTime = deallocatedTime;
    }

    public Integer getRoleId() {
        return roleId;
    }

    public void setRoleId(Integer roleId) {
        this.roleId = roleId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getTeamId() {
        return teamId;
    }

    public void setTeamId(Integer teamId) {
        this.teamId = teamId;
    }

    public Long getJoinedTime() {
        return joinedTime;
    }

    public void setJoinedTime(Long joinedTime) {
        this.joinedTime = joinedTime;
    }

    public Integer getScopeId() {
        return scopeId;
    }

    public void setScopeId(Integer scopeId) {
        this.scopeId = scopeId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserTeam team = (UserTeam) o;

        if (userId != null ? !userId.equals(team.userId) : team.userId != null) return false;
        if (teamId != null ? !teamId.equals(team.teamId) : team.teamId != null) return false;
        return roleId != null ? roleId.equals(team.roleId) : team.roleId == null;
    }

    @Override
    public int hashCode() {
        int result = userId != null ? userId.hashCode() : 0;
        result = 31 * result + (teamId != null ? teamId.hashCode() : 0);
        result = 31 * result + (roleId != null ? roleId.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "UserTeam{" +
                "userId=" + userId +
                ", teamId=" + teamId +
                ", roleId=" + roleId +
                ", scopeId=" + scopeId +
                ", joinedTime=" + joinedTime +
                ", deallocatedTime=" + deallocatedTime +
                ", approved=" + approved +
                ", autoTeam=" + autoTeam +
                '}';
    }
}
