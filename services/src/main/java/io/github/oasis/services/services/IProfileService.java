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

package io.github.oasis.services.services;

import io.github.oasis.services.dto.crud.TeamProfileAddDto;
import io.github.oasis.services.dto.crud.TeamProfileEditDto;
import io.github.oasis.services.dto.crud.TeamScopeAddDto;
import io.github.oasis.services.dto.crud.TeamScopeEditDto;
import io.github.oasis.services.dto.crud.UserProfileAddDto;
import io.github.oasis.services.dto.crud.UserProfileEditDto;
import io.github.oasis.services.dto.stats.UserCountStat;
import io.github.oasis.services.model.TeamProfile;
import io.github.oasis.services.model.TeamScope;
import io.github.oasis.services.model.UserProfile;
import io.github.oasis.services.model.UserTeam;
import io.github.oasis.services.model.UserTeamScope;

import java.util.List;

public interface IProfileService {

    long addUserProfile(UserProfileAddDto profile) throws Exception;
    long addUserProfile(UserProfileAddDto profile, long teamId, int roleId) throws Exception;
    UserProfile readUserProfile(long userId) throws Exception;
    UserProfile readUserProfile(String email) throws Exception;
    UserProfile readUserProfileByExtId(long extUserId) throws Exception;
    boolean editUserProfile(long userId, UserProfileEditDto profile) throws Exception;
    boolean deleteUserProfile(long userId) throws Exception;
    List<UserProfile> findUser(String email, String name) throws Exception;

    List<UserProfile> listUsers(long teamId, long offset, long size) throws Exception;
    List<UserCountStat> listUserCountInTeams(long atTime, boolean withAutoUsers) throws Exception;
    List<UserCountStat> listUserCountInTeamScopes(long atTime, boolean withAutoUsers) throws Exception;

    long addTeam(TeamProfileAddDto teamProfile) throws Exception;
    TeamProfile readTeam(long teamId) throws Exception;
    boolean editTeam(long teamId, TeamProfileEditDto teamProfile) throws Exception;
    List<TeamProfile> listTeams(long scopeId) throws Exception;

    long addTeamScope(TeamScopeAddDto teamScope) throws Exception;
    TeamScope readTeamScope(long scopeId) throws Exception;
    TeamScope readTeamScope(String scopeName) throws Exception;
    List<TeamScope> listTeamScopes() throws Exception;
    boolean editTeamScope(long scopeId, TeamScopeEditDto scope) throws Exception;

    boolean assignUserToTeam(long userId, long teamId, int roleId) throws Exception;
    boolean assignUserToTeam(long userId, long teamId, int roleId, boolean pendingApproval) throws Exception;
    boolean assignUserToTeam(long userId, long teamId, int roleId, boolean pendingApproval,
                          long since) throws Exception;
    UserTeam findCurrentTeamOfUser(long userId) throws Exception;
    UserTeam findCurrentTeamOfUser(long userId, boolean returnApprovedOnly) throws Exception;
    UserTeam findCurrentTeamOfUser(long userId, boolean returnApprovedOnly, long atTime) throws Exception;
    TeamProfile findTeamByName(String name) throws Exception;

    long requestForRole(long byUser, int teamScopeId, int roleId, long startTime) throws Exception;
    boolean rejectRequestedRole(int requestId, long rejectedBy) throws Exception;
    boolean removeCurrentRole(long userId, int teamScopeId, long endTime, long removedBy) throws Exception;
    boolean approveRole(int requestId, long approvedTime, long approvedBy) throws Exception;
    List<UserTeamScope> listCurrentUserRoles(long userId) throws Exception;

    boolean logoutUser(long userId, long ts) throws Exception;

}
