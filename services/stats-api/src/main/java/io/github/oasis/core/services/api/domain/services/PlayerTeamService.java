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

package io.github.oasis.core.services.api.domain.services;

import io.github.oasis.core.TeamMetadata;
import io.github.oasis.core.external.storage.OasisDao;
import io.github.oasis.core.external.PaginatedResult;
import io.github.oasis.core.external.storage.PlayerDao;
import io.github.oasis.core.external.storage.AssignmentDao;
import io.github.oasis.core.external.storage.TeamDao;
import io.github.oasis.core.model.PlayerObject;
import io.github.oasis.core.model.PlayerWithTeams;
import io.github.oasis.core.model.TeamObject;
import io.github.oasis.core.services.annotations.AdminDbRepository;
import io.github.oasis.core.services.api.exceptions.ErrorCodes;
import io.github.oasis.core.services.api.exceptions.OasisApiRuntimeException;
import io.github.oasis.core.services.api.handlers.events.BasePlayerRelatedEvent;
import io.github.oasis.core.services.api.handlers.events.EntityChangeType;
import io.github.oasis.core.services.api.app.request.PlayerCreateRequest;
import io.github.oasis.core.services.api.app.request.PlayerUpdateRequest;
import io.github.oasis.core.services.api.app.request.TeamCreateRequest;
import io.github.oasis.core.services.api.app.request.TeamUpdateRequest;
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
public class PlayerTeamService extends AbstractOasisService implements IPlayerManagementService, ITeamManagementService, IAssignmentService {

    private final ApplicationEventPublisher eventPublisher;
    private final TeamDao teamDao;
    private final PlayerDao playerDao;
    private final AssignmentDao assignmentDao;

    public PlayerTeamService(@AdminDbRepository OasisDao dao,
                             ApplicationEventPublisher eventPublisher) {
        super(dao);

        this.teamDao = dao.getTeamDao();
        this.playerDao = dao.getPlayerDao();
        this.assignmentDao = dao.getAssignmentDao();
        this.eventPublisher = eventPublisher;
    }

    @Override
    public PlayerObject addPlayer(PlayerCreateRequest request) {
        var playerObject = PlayerObject.builder()
                .displayName(request.getDisplayName())
                .email(request.getEmail())
                .gender(request.getGender())
                .avatarRef(request.getAvatarRef())
                .timeZone(request.getTimeZone())
                .build();

        return playerDao.addPlayer(playerObject);
    }

    @Override
    public PlayerObject readPlayer(long userId) {
        return playerDao.readPlayer(userId);
    }

    @Override
    public PlayerObject readPlayerByEmail(String userEmail, boolean verbose) {
        var playerObject = playerDao.readPlayer(userEmail);

        if (verbose) {
            var playerWithTeams = new PlayerWithTeams();
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

    @Transactional
    @Override
    public PlayerObject updatePlayer(long playerId, PlayerUpdateRequest updatingUser) {
        var dbPlayer = playerDao.readPlayer(playerId);
        var playerUpdating = dbPlayer.toBuilder()
                .displayName(StringUtils.defaultIfBlank(updatingUser.getDisplayName(), dbPlayer.getDisplayName()))
                .avatarRef(updatingUser.getAvatarRef())
                .gender(ObjectUtils.defaultIfNull(updatingUser.getGender(), dbPlayer.getGender()))
                .timeZone(StringUtils.defaultIfBlank(updatingUser.getTimeZone(), dbPlayer.getTimeZone()))
                .active(ObjectUtils.defaultIfNull(updatingUser.getIsActive(), Boolean.TRUE))
                .version(updatingUser.getVersion())
                .build();

        var updatedPlayer = playerDao.updatePlayer(playerId, playerUpdating);
        eventPublisher.publishEvent(BasePlayerRelatedEvent.builder()
                .changeType(EntityChangeType.MODIFIED)
                .email(updatedPlayer.getEmail())
                .userId(playerId)
                .build());
        return updatedPlayer;
    }

    @Override
    public PlayerObject deactivatePlayer(long playerId) {
        var deletedPlayer = playerDao.deletePlayer(playerId);

        eventPublisher.publishEvent(BasePlayerRelatedEvent.builder()
                .changeType(EntityChangeType.REMOVED)
                .email(deletedPlayer.getEmail())
                .userId(playerId)
                .build());
        return deletedPlayer;
    }

    @Override
    public TeamObject addTeam(TeamCreateRequest request) {
        return teamDao.addTeam(TeamObject.builder()
                .name(request.getName())
                .colorCode(request.getColorCode())
                .gameId(request.getGameId())
                .avatarRef(request.getAvatarRef())
                .build());
    }

    @Override
    public TeamObject readTeamByName(String name) {
        return Optional.ofNullable(teamDao.readTeam(name))
                .orElseThrow(() -> new OasisApiRuntimeException(ErrorCodes.TEAM_NOT_EXISTS, HttpStatus.NOT_FOUND));
    }

    @Override
    public TeamObject readTeam(int teamId) {
        return Optional.ofNullable(teamDao.readTeam(teamId))
                .orElseThrow(() -> new OasisApiRuntimeException(ErrorCodes.TEAM_NOT_EXISTS, HttpStatus.NOT_FOUND));
    }

    @Override
    public TeamObject updateTeam(int teamId, TeamUpdateRequest request) {
        TeamObject dbTeam = readTeam(teamId);

        return teamDao.updateTeam(teamId, dbTeam.toBuilder()
                .avatarRef(ObjectUtils.defaultIfNull(request.getAvatarRef(), dbTeam.getAvatarRef()))
                .colorCode(StringUtils.defaultIfBlank(request.getColorCode(), dbTeam.getColorCode()))
                .version(request.getVersion())
                .build());
    }

    @Override
    public PaginatedResult<TeamMetadata> searchTeam(String teamPrefix, String offset, int maxSize) {
        return teamDao.searchTeam(teamPrefix, offset, maxSize);
    }

    @Override
    public List<PlayerObject> listAllUsersInTeam(int teamId) {
        return assignmentDao.getTeamPlayers(teamId);
    }

    @Override
    public List<TeamObject> getTeamsOfPlayer(long playerId) {
        return assignmentDao.getPlayerTeams(playerId);
    }

    @Override
    public void addPlayerToTeam(long playerId, int teamId) {
        PlayerObject playerRef = readPlayer(playerId);
        TeamObject teamRef = readTeam(teamId);
        assignmentDao.addPlayerToTeam(playerRef.getId(), teamRef.getGameId(), teamRef.getId());

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
        assignmentDao.removePlayerFromTeam(playerRef.getId(), dbTeam.getGameId(), dbTeam.getId());

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
            PlayerObject playerObject = playerDao.readPlayer(playerId);

            assignmentDao.addPlayerToTeam(playerId, gameId, teamId);

            eventPublisher.publishEvent(BasePlayerRelatedEvent.builder()
                    .changeType(EntityChangeType.MODIFIED)
                    .email(playerObject.getEmail())
                    .userId(playerId)
                    .build());
        }
    }
}
