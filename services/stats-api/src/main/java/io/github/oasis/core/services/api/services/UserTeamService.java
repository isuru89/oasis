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

package io.github.oasis.core.services.api.services;

import io.github.oasis.core.model.TeamObject;
import io.github.oasis.core.model.UserObject;
import io.github.oasis.core.services.api.beans.BackendRepository;
import io.github.oasis.core.services.api.to.UserCreateRequest;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Isuru Weerarathna
 */
@Service
public class UserTeamService extends AbstractOasisService {

    public UserTeamService(BackendRepository backendRepository) {
        super(backendRepository);
    }

    public UserObject addUser(UserCreateRequest request) {
        UserObject userObject = new UserObject();
        userObject.setDisplayName(request.getFirstName() + " " + request.getLastName());
        userObject.setEmail(request.getEmail());
        userObject.setGender(request.getGender());
        userObject.setTimeZone(request.getTimeZone());

        UserObject oasisUser = backendRepository.addUser(userObject);

        request.setUserId(userObject.getUserId());
        //userManagementSupport.createUser(request);

        return oasisUser;
    }

    public UserObject readUser(long userId) {
        return backendRepository.readUser(userId);
    }

    public UserObject readUser(String userEmail) {
        return backendRepository.readUser(userEmail);
    }

    public UserObject updateUser(long userId, UserObject updatingUser) {
        return backendRepository.updateUser(userId, updatingUser);
    }

    public UserObject deactivateUser(long userId) {
        UserObject userObject = backendRepository.deleteUser(userId);

        //userManagementSupport.deleteUser(userObject.getEmail());
        return userObject;
    }

    public List<TeamObject> getUserTeams(long userId) {
        return backendRepository.getUserTeams(userId);
    }

    public TeamObject addTeam(TeamObject teamObject) {
        return backendRepository.addTeam(teamObject);
    }

    public TeamObject updateTeam(int teamId, TeamObject updatingTeam) {
        return backendRepository.updateTeam(teamId, updatingTeam);
    }

    public List<UserObject> listAllUsersInTeam(int teamId) {
        return backendRepository.getTeamUsers(teamId);
    }

    public void addUserToTeam(long userId, int gameId, int teamId) {
        backendRepository.addUserToTeam(userId, gameId, teamId);
    }

    public void addUsersToTeam(int teamId, List<Integer> userIds) {
        TeamObject teamObject = backendRepository.readTeam(teamId);
        int gameId = teamObject.getGameId();

        for (int userId : userIds) {
            backendRepository.addUserToTeam(userId, gameId, teamId);
        }
    }
}
