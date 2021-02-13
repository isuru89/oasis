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

import io.github.oasis.core.model.PlayerObject;
import io.github.oasis.core.model.TeamObject;
import io.github.oasis.core.services.api.beans.BackendRepository;
import io.github.oasis.core.services.api.to.PlayerCreateRequest;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Isuru Weerarathna
 */
@Service
public class PlayerTeamService extends AbstractOasisService {

    public PlayerTeamService(BackendRepository backendRepository) {
        super(backendRepository);
    }

    public PlayerObject addPlayer(PlayerCreateRequest request) {
        PlayerObject playerObject = new PlayerObject();
        playerObject.setDisplayName(request.getDisplayName());
        playerObject.setEmail(request.getEmail());
        playerObject.setGender(request.getGender());
        playerObject.setAvatarRef(request.getAvatarRef());
        playerObject.setTimeZone(request.getTimeZone());

        PlayerObject oasisUser = backendRepository.addPlayer(playerObject);

        request.setUserId(playerObject.getId());
        //userManagementSupport.createUser(request);

        return oasisUser;
    }

    public PlayerObject readPlayer(long userId) {
        return backendRepository.readPlayer(userId);
    }

    public PlayerObject readPlayer(String userEmail) {
        return backendRepository.readPlayer(userEmail);
    }

    public PlayerObject updatePlayer(long userId, PlayerObject updatingUser) {
        return backendRepository.updatePlayer(userId, updatingUser);
    }

    public PlayerObject deactivatePlayer(long userId) {
        PlayerObject playerObject = backendRepository.deletePlayer(userId);

        //userManagementSupport.deleteUser(playerObject.getEmail());
        return playerObject;
    }

    public List<TeamObject> getTeamsOfPlayer(long userId) {
        return backendRepository.getPlayerTeams(userId);
    }

    public TeamObject addTeam(TeamObject teamObject) {
        return backendRepository.addTeam(teamObject);
    }

    public TeamObject updateTeam(int teamId, TeamObject updatingTeam) {
        return backendRepository.updateTeam(teamId, updatingTeam);
    }

    public List<PlayerObject> listAllUsersInTeam(int teamId) {
        return backendRepository.getTeamPlayers(teamId);
    }

    public void addPlayerToTeam(long userId, int gameId, int teamId) {
        backendRepository.addPlayerToTeam(userId, gameId, teamId);
    }

    public void addPlayersToTeam(int teamId, List<Integer> userIds) {
        TeamObject teamObject = backendRepository.readTeam(teamId);
        int gameId = teamObject.getGameId();

        for (int userId : userIds) {
            backendRepository.addPlayerToTeam(userId, gameId, teamId);
        }
    }
}
