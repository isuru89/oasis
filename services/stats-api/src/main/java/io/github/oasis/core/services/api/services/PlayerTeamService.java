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

import io.github.oasis.core.TeamMetadata;
import io.github.oasis.core.external.PaginatedResult;
import io.github.oasis.core.model.PlayerObject;
import io.github.oasis.core.model.TeamObject;
import io.github.oasis.core.services.api.beans.BackendRepository;
import io.github.oasis.core.services.api.exceptions.ErrorCodes;
import io.github.oasis.core.services.api.to.PlayerCreateRequest;
import io.github.oasis.core.services.api.to.PlayerUpdateRequest;
import io.github.oasis.core.services.api.to.TeamCreateRequest;
import io.github.oasis.core.services.api.to.TeamUpdateRequest;
import io.github.oasis.core.services.exceptions.OasisApiException;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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

    public PlayerObject updatePlayer(long userId, PlayerUpdateRequest updatingUser) {
        PlayerObject dbPlayer = backendRepository.readPlayer(userId);
        PlayerObject playerUpdating = dbPlayer.toBuilder()
                .displayName(StringUtils.defaultIfBlank(updatingUser.getDisplayName(), dbPlayer.getDisplayName()))
                .avatarRef(updatingUser.getAvatarRef())
                .gender(ObjectUtils.defaultIfNull(updatingUser.getGender(), dbPlayer.getGender()))
                .timeZone(StringUtils.defaultIfBlank(updatingUser.getTimeZone(), dbPlayer.getTimeZone()))
                .build();

        return backendRepository.updatePlayer(userId, playerUpdating);
    }

    public PlayerObject deactivatePlayer(long userId) {
        PlayerObject playerObject = backendRepository.deletePlayer(userId);

        //userManagementSupport.deleteUser(playerObject.getEmail());
        return playerObject;
    }

    public List<TeamObject> getTeamsOfPlayer(long userId) {
        return backendRepository.getPlayerTeams(userId);
    }

    public TeamObject addTeam(TeamCreateRequest request) {
        return backendRepository.addTeam(TeamObject.builder()
                .name(request.getName())
                .colorCode(request.getColorCode())
                .gameId(request.getGameId())
                .avatarRef(request.getAvatarRef())
                .build());
    }

    public TeamObject readTeam(String name) throws OasisApiException {
        return Optional.ofNullable(backendRepository.readTeam(name))
                .orElseThrow(() -> new OasisApiException(ErrorCodes.TEAM_NOT_EXISTS, HttpStatus.NOT_FOUND.value(),
                        "Provided team is not found!"));
    }

    public TeamObject readTeam(int teamId) throws OasisApiException {
        return Optional.ofNullable(backendRepository.readTeam(teamId))
                .orElseThrow(() -> new OasisApiException(ErrorCodes.TEAM_NOT_EXISTS, HttpStatus.NOT_FOUND.value(),
                        "Provided team is not found!"));

    }

    public TeamObject updateTeam(int teamId, TeamUpdateRequest request) {
        TeamObject dbTeam = backendRepository.readTeam(teamId);

        return backendRepository.updateTeam(teamId, dbTeam.toBuilder()
                .avatarRef(ObjectUtils.defaultIfNull(request.getAvatarRef(), dbTeam.getAvatarRef()))
                .colorCode(StringUtils.defaultIfBlank(request.getColorCode(), dbTeam.getColorCode()))
                .build());
    }

    public List<PlayerObject> listAllUsersInTeam(int teamId) {
        return backendRepository.getTeamPlayers(teamId);
    }

    public void addPlayerToTeam(long userId, int gameId, int teamId) {
        backendRepository.addPlayerToTeam(userId, gameId, teamId);
    }

    public void removePlayerFromTeam(long playerId, int teamId) {
        TeamObject dbTeam = backendRepository.readTeam(teamId);
        backendRepository.removePlayerFromTeam(playerId, dbTeam.getGameId(), teamId);
    }

    public PaginatedResult<TeamMetadata> searchTeam(String teamPrefix, String offset, int maxSize) {
        return backendRepository.searchTeam(teamPrefix, offset, maxSize);
    }

    public void addPlayersToTeam(int teamId, List<Long> userIds) {
        TeamObject teamObject = backendRepository.readTeam(teamId);
        int gameId = teamObject.getGameId();

        for (long userId : userIds) {
            backendRepository.addPlayerToTeam(userId, gameId, teamId);
        }
    }
}
