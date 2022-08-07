/*
 *
 *  * Licensed to the Apache Software Foundation (ASF) under one
 *  * or more contributor license agreements.  See the NOTICE file
 *  * distributed with this work for additional information
 *  * regarding copyright ownership.  The ASF licenses this file
 *  * to you under the Apache License, Version 2.0 (the
 *  * "License"); you may not use this file except in compliance
 *  * with the License.  You may obtain a copy of the License at
 *  *
 *  *    http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing,
 *  * software distributed under the License is distributed on an
 *  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  * KIND, either express or implied.  See the License for the
 *  * specific language governing permissions and limitations
 *  * under the License.
 *
 *
 */

package io.github.oasis.core.services.api.services.impl;

import io.github.oasis.core.TeamMetadata;
import io.github.oasis.core.external.OasisRepository;
import io.github.oasis.core.external.PaginatedResult;
import io.github.oasis.core.model.PlayerObject;
import io.github.oasis.core.model.PlayerWithTeams;
import io.github.oasis.core.model.TeamObject;
import io.github.oasis.core.services.annotations.AdminDbRepository;
import io.github.oasis.core.services.api.exceptions.ErrorCodes;
import io.github.oasis.core.services.api.exceptions.OasisApiRuntimeException;
import io.github.oasis.core.services.api.handlers.events.BasePlayerRelatedEvent;
import io.github.oasis.core.services.api.handlers.events.EntityChangeType;
import io.github.oasis.core.services.api.services.IPlayerAssignmentService;
import io.github.oasis.core.services.api.services.IPlayerManagementService;
import io.github.oasis.core.services.api.services.ITeamManagementService;
import io.github.oasis.core.services.api.to.PlayerCreateRequest;
import io.github.oasis.core.services.api.to.PlayerUpdateRequest;
import io.github.oasis.core.services.api.to.TeamCreateRequest;
import io.github.oasis.core.services.api.to.TeamUpdateRequest;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * @author Isuru Weerarathna
 */
@Service
public class PlayerTeamService extends AbstractOasisService implements IPlayerManagementService, ITeamManagementService, IPlayerAssignmentService {

    private final ApplicationEventPublisher eventPublisher;

    public PlayerTeamService(@AdminDbRepository OasisRepository backendRepository,
                             ApplicationEventPublisher eventPublisher) {
        super(backendRepository);

        this.eventPublisher = eventPublisher;
    }

    @Override
    public PlayerObject addPlayer(PlayerCreateRequest request) {
        PlayerObject playerObject = new PlayerObject();
        playerObject.setDisplayName(request.getDisplayName());
        playerObject.setEmail(request.getEmail());
        playerObject.setGender(request.getGender());
        playerObject.setAvatarRef(request.getAvatarRef());
        playerObject.setTimeZone(request.getTimeZone());

        return backendRepository.addPlayer(playerObject);
    }

    @Override
    public PlayerObject readPlayer(long userId) {
        return backendRepository.readPlayer(userId);
    }

    @Override
    public PlayerObject readPlayerByEmail(String userEmail, boolean verbose) {
        PlayerObject playerObject = backendRepository.readPlayer(userEmail);

        if (verbose) {
            PlayerWithTeams playerWithTeams = new PlayerWithTeams();
            playerWithTeams.setId(playerObject.getId());
            playerWithTeams.setEmail(playerObject.getEmail());
            playerWithTeams.setGender(playerObject.getGender());
            playerWithTeams.setDisplayName(playerObject.getDisplayName());
            playerWithTeams.setTimeZone(playerObject.getTimeZone());
            playerWithTeams.setAvatarRef(playerObject.getAvatarRef());
            playerWithTeams.setActive(playerObject.isActive());
            playerWithTeams.setVersion(playerObject.getVersion());
            playerWithTeams.setCreatedAt(playerObject.getCreatedAt());
            playerWithTeams.setUpdatedAt(playerObject.getUpdatedAt());
            List<TeamObject> teamsOfPlayer = getTeamsOfPlayer(playerObject.getId());
            playerWithTeams.setTeams(teamsOfPlayer);
            return playerWithTeams;
        } else {
            return playerObject;
        }
    }

    @Override
    public PlayerObject updatePlayer(long playerId, PlayerUpdateRequest updatingUser) {
        PlayerObject dbPlayer = backendRepository.readPlayer(playerId);
        PlayerObject playerUpdating = dbPlayer.toBuilder()
                .displayName(StringUtils.defaultIfBlank(updatingUser.getDisplayName(), dbPlayer.getDisplayName()))
                .avatarRef(updatingUser.getAvatarRef())
                .gender(ObjectUtils.defaultIfNull(updatingUser.getGender(), dbPlayer.getGender()))
                .timeZone(StringUtils.defaultIfBlank(updatingUser.getTimeZone(), dbPlayer.getTimeZone()))
                .active(ObjectUtils.defaultIfNull(updatingUser.getIsActive(), Boolean.TRUE))
                .version(updatingUser.getVersion())
                .build();

        PlayerObject updatedPlayer = backendRepository.updatePlayer(playerId, playerUpdating);
        eventPublisher.publishEvent(BasePlayerRelatedEvent.builder()
                .changeType(EntityChangeType.MODIFIED)
                .email(updatedPlayer.getEmail())
                .userId(playerId)
                .build());
        return updatedPlayer;
    }

    @Override
    public PlayerObject deactivatePlayer(long playerId) {
        PlayerObject deletedPlayer = backendRepository.deletePlayer(playerId);

        eventPublisher.publishEvent(BasePlayerRelatedEvent.builder()
                .changeType(EntityChangeType.REMOVED)
                .email(deletedPlayer.getEmail())
                .userId(playerId)
                .build());
        return deletedPlayer;
    }

    @Override
    public TeamObject addTeam(TeamCreateRequest request) {
        return backendRepository.addTeam(TeamObject.builder()
                .name(request.getName())
                .colorCode(request.getColorCode())
                .gameId(request.getGameId())
                .avatarRef(request.getAvatarRef())
                .build());
    }

    @Override
    public TeamObject readTeamByName(String name) {
        return Optional.ofNullable(backendRepository.readTeam(name))
                .orElseThrow(() -> new OasisApiRuntimeException(ErrorCodes.TEAM_NOT_EXISTS, HttpStatus.NOT_FOUND));
    }

    @Override
    public TeamObject readTeam(int teamId) {
        return Optional.ofNullable(backendRepository.readTeam(teamId))
                .orElseThrow(() -> new OasisApiRuntimeException(ErrorCodes.TEAM_NOT_EXISTS, HttpStatus.NOT_FOUND));
    }

    @Override
    public TeamObject updateTeam(int teamId, TeamUpdateRequest request) {
        TeamObject dbTeam = readTeam(teamId);

        return backendRepository.updateTeam(teamId, dbTeam.toBuilder()
                .avatarRef(ObjectUtils.defaultIfNull(request.getAvatarRef(), dbTeam.getAvatarRef()))
                .colorCode(StringUtils.defaultIfBlank(request.getColorCode(), dbTeam.getColorCode()))
                .version(request.getVersion())
                .build());
    }

    @Override
    public PaginatedResult<TeamMetadata> searchTeam(String teamPrefix, String offset, int maxSize) {
        return backendRepository.searchTeam(teamPrefix, offset, maxSize);
    }

    @Override
    public List<PlayerObject> listAllUsersInTeam(int teamId) {
        return backendRepository.getTeamPlayers(teamId);
    }

    @Override
    public List<TeamObject> getTeamsOfPlayer(long playerId) {
        return backendRepository.getPlayerTeams(playerId);
    }

    @Override
    public void addPlayerToTeam(long playerId, int teamId) {
        PlayerObject playerRef = readPlayer(playerId);
        TeamObject teamRef = readTeam(teamId);
        backendRepository.addPlayerToTeam(playerRef.getId(), teamRef.getGameId(), teamRef.getId());

        eventPublisher.publishEvent(BasePlayerRelatedEvent.builder()
                .changeType(EntityChangeType.MODIFIED)
                .email(playerRef.getEmail())
                .userId(playerId)
                .build());
    }

    @Override
    public void removePlayerFromTeam(long playerId, int teamId) {
        PlayerObject playerRef = readPlayer(playerId);
        TeamObject dbTeam = readTeam(teamId);
        backendRepository.removePlayerFromTeam(playerRef.getId(), dbTeam.getGameId(), dbTeam.getId());

        eventPublisher.publishEvent(BasePlayerRelatedEvent.builder()
                .changeType(EntityChangeType.MODIFIED)
                .email(playerRef.getEmail())
                .userId(playerId)
                .build());
    }

    @Override
    @Transactional
    public void addPlayersToTeam(int teamId, List<Long> playerIds) {
        TeamObject teamObject = readTeam(teamId);
        int gameId = teamObject.getGameId();

        for (long playerId : playerIds) {
            PlayerObject playerObject = backendRepository.readPlayer(playerId);

            backendRepository.addPlayerToTeam(playerId, gameId, teamId);

            eventPublisher.publishEvent(BasePlayerRelatedEvent.builder()
                    .changeType(EntityChangeType.MODIFIED)
                    .email(playerObject.getEmail())
                    .userId(playerId)
                    .build());
        }
    }
}
