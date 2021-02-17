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

package io.github.oasis.core.services.api.beans;

import io.github.oasis.core.Game;
import io.github.oasis.core.TeamMetadata;
import io.github.oasis.core.configs.OasisConfigs;
import io.github.oasis.core.elements.AttributeInfo;
import io.github.oasis.core.elements.ElementDef;
import io.github.oasis.core.external.OasisRepository;
import io.github.oasis.core.external.PaginatedResult;
import io.github.oasis.core.model.EventSource;
import io.github.oasis.core.model.PlayerObject;
import io.github.oasis.core.model.TeamObject;
import io.github.oasis.core.services.api.exceptions.ErrorCodes;
import io.github.oasis.core.services.api.exceptions.OasisApiRuntimeException;
import org.jdbi.v3.core.JdbiException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Composition of admin and engine repositories where it takes the responsibility of
 * managing and sync two repositories with service operations.
 *
 * @author Isuru Weerarathna
 */
@Component
public class BackendRepository implements OasisRepository {

    private OasisRepository engineRepository;
    private OasisRepository adminRepository;

    public BackendRepository(){}

    public BackendRepository(Map<String, OasisRepository> oasisServiceMap, OasisConfigs oasisConfigs) {
        this.adminRepository = oasisServiceMap.get(oasisConfigs.get("oasis.db.admin", null));
        this.engineRepository = oasisServiceMap.get(oasisConfigs.get("oasis.db.engine", "redis"));
    }

    private BackendRepository(OasisRepository engineRepository, OasisRepository adminRepository) {
        this.adminRepository = adminRepository;
        this.engineRepository = engineRepository;
    }

    public static BackendRepository create(OasisRepository engineRepository, OasisRepository adminRepository) {
        return new BackendRepository(engineRepository, adminRepository);
    }

    @Override
    public EventSource addEventSource(EventSource eventSource) {
        EventSource source = adminRepository.addEventSource(eventSource);
        engineRepository.addEventSource(source);
        return source;
    }

    @Override
    public EventSource deleteEventSource(int id) {
        EventSource source = adminRepository.deleteEventSource(id);
        engineRepository.deleteEventSource(id);
        return source;
    }

    @Override
    public EventSource readEventSource(int id) {
        return null;
    }

    @Override
    public EventSource readEventSource(String token) {
        return null;
    }

    @Override
    public List<EventSource> listAllEventSources() {
        return null;
    }

    @Override
    public List<EventSource> listAllEventSourcesOfGame(int gameId) {
        return null;
    }

    @Override
    public void addEventSourceToGame(int sourceId, int gameId) {

    }

    @Override
    public void removeEventSourceFromGame(int sourceId, int gameId) {

    }

    @Override
    public Game addNewGame(Game game) {
        return null;
    }

    @Override
    public Game updateGame(int gameId, Game game) {
        return null;
    }

    @Override
    public Game readGame(int gameId) {
        return null;
    }

    @Override
    public Game deleteGame(int gameId) {
        return null;
    }

    @Override
    public boolean existsGame(String gameName) {
        return false;
    }

    @Override
    public List<Game> listGames() {
        return null;
    }

    //
    // PLAYER AND TEAM related service methods
    //

    @Override
    public PlayerObject readPlayer(long userId) {
        return adminRepository.readPlayer(userId);
    }

    @Override
    public PlayerObject readPlayer(String email) {
        return adminRepository.readPlayer(email);
    }

    @Override
    public PlayerObject addPlayer(PlayerObject newUser) {
        PlayerObject newPlayer = adminRepository.addPlayer(newUser);
        engineRepository.addPlayer(newPlayer);
        return newPlayer;
    }

    @Override
    public boolean existsPlayer(String email) {
        return Objects.nonNull(adminRepository.readPlayer(email));
    }

    @Override
    public boolean existsPlayer(long userId) {
        PlayerObject dbPlayer = adminRepository.readPlayer(userId);
        System.out.println(dbPlayer);
        return Objects.nonNull(dbPlayer) && dbPlayer.isActive();
    }

    @Override
    public PlayerObject updatePlayer(long userId, PlayerObject updatedUser) {
        adminRepository.updatePlayer(userId, updatedUser);
        engineRepository.updatePlayer(userId, updatedUser);
        return adminRepository.readPlayer(userId);
    }

    @Override
    public PlayerObject deletePlayer(long userId) {
        adminRepository.deletePlayer(userId);
        engineRepository.deletePlayer(userId);
        return adminRepository.readPlayer(userId);
    }

    @Override
    public TeamObject addTeam(TeamObject teamObject) {
        TeamObject newTeam = adminRepository.addTeam(teamObject);
        engineRepository.addTeam(newTeam);
        return newTeam;
    }

    @Override
    public TeamObject readTeam(int teamId) {
        return adminRepository.readTeam(teamId);
    }

    @Override
    public TeamObject readTeam(String teamName) {
        return adminRepository.readTeam(teamName);
    }

    @Override
    public TeamObject updateTeam(int teamId, TeamObject updatedTeam) {
        TeamObject savedTeam = adminRepository.updateTeam(teamId, updatedTeam);
        engineRepository.updateTeam(savedTeam.getId(), savedTeam);
        return savedTeam;
    }

    @Override
    public boolean existsTeam(String teamName) {
        return Objects.nonNull(adminRepository.readTeam(teamName));
    }

    @Override
    public boolean existsTeam(int teamId) {
        return Objects.nonNull(adminRepository.readTeam(teamId));
    }

    @Override
    public PaginatedResult<TeamMetadata> searchTeam(String teamName, String offset, int maxRecords) {
        return adminRepository.searchTeam(teamName, offset, maxRecords);
    }

    @Override
    public void removePlayerFromTeam(long playerId, int gameId, int teamId) {
        adminRepository.removePlayerFromTeam(playerId, gameId, teamId);
        engineRepository.removePlayerFromTeam(playerId, gameId, teamId);
    }

    @Override
    public void addPlayerToTeam(long playerId, int gameId, int teamId) {
        adminRepository.addPlayerToTeam(playerId, gameId, teamId);
        engineRepository.addPlayerToTeam(playerId, gameId, teamId);
    }

    @Override
    public List<TeamObject> getPlayerTeams(long playerId) {
        return adminRepository.getPlayerTeams(playerId);
    }

    @Override
    public List<PlayerObject> getTeamPlayers(int teamId) {
        return adminRepository.getTeamPlayers(teamId);
    }

    @Override
    public ElementDef addNewElement(int gameId, ElementDef elementDef) {
        ElementDef def = adminRepository.addNewElement(gameId, elementDef);
        engineRepository.addNewElement(gameId, def);
        return def;
    }

    @Override
    public ElementDef updateElement(int gameId, String id, ElementDef elementDef) {
        return null;
    }

    @Override
    public ElementDef deleteElement(int gameId, String id) {
        ElementDef def = adminRepository.readElement(gameId, id);
        adminRepository.deleteElement(gameId, id);
        engineRepository.deleteElement(gameId, id);
        return def;
    }

    @Override
    public ElementDef readElement(int gameId, String id) {
        return adminRepository.readElement(gameId, id);
    }

    @Override
    public ElementDef readElementWithoutData(int gameId, String id) {
        return adminRepository.readElementWithoutData(gameId, id);
    }

    @Override
    public List<ElementDef> readElementsByType(int gameId, String type) {
        return adminRepository.readElementsByType(gameId, type);
    }

    @Override
    public AttributeInfo addAttribute(int gameId, AttributeInfo newAttribute) {
        try {
            return adminRepository.addAttribute(gameId, newAttribute);
        } catch (JdbiException e) {
            throw new OasisApiRuntimeException(ErrorCodes.ATTRIBUTE_EXISTS, e);
        }
    }

    @Override
    public List<AttributeInfo> listAllAttributes(int gameId) {
        return adminRepository.listAllAttributes(gameId);
    }
}
