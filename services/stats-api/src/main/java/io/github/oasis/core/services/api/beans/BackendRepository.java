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
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Composition of admin and engine repositories where it takes the responsibility of
 * managing and sync two repositories with service operations.
 *
 * @author Isuru Weerarathna
 */
@Component
public class BackendRepository implements OasisRepository {

    private final OasisRepository engineRepository;
    private final OasisRepository adminRepository;

    public BackendRepository(Map<String, OasisRepository> oasisServiceMap, OasisConfigs oasisConfigs) {
        this.adminRepository = oasisServiceMap.get(oasisConfigs.get("oasis.db.admin", null));
        this.engineRepository = oasisServiceMap.get(oasisConfigs.get("oasis.db.engine", "redis"));
    }

    @Override
    public EventSource addEventSource(EventSource eventSource) {
        return null;
    }

    @Override
    public EventSource deleteEventSource(int id) {
        return null;
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

    @Override
    public PlayerObject readPlayer(long userId) {
        return null;
    }

    @Override
    public PlayerObject readPlayer(String email) {
        return null;
    }

    @Override
    public PlayerObject addPlayer(PlayerObject newUser) {
        return null;
    }

    @Override
    public boolean existsPlayer(String email) {
        return false;
    }

    @Override
    public boolean existsPlayer(long userId) {
        return false;
    }

    @Override
    public PlayerObject updatePlayer(long userId, PlayerObject updatedUser) {
        return null;
    }

    @Override
    public PlayerObject deletePlayer(long userId) {
        return null;
    }

    @Override
    public TeamObject addTeam(TeamObject teamObject) {
        return null;
    }

    @Override
    public TeamObject readTeam(int teamId) {
        return null;
    }

    @Override
    public TeamObject updateTeam(int teamId, TeamObject updatedTeam) {
        return null;
    }

    @Override
    public boolean existsTeam(String teamName) {
        return false;
    }

    @Override
    public boolean existsTeam(int teamId) {
        return false;
    }

    @Override
    public PaginatedResult<TeamMetadata> searchTeam(String teamName, String offset, int maxRecords) {
        return null;
    }

    @Override
    public void removePlayerFromTeam(long userId, int gameId, int teamId) {

    }

    @Override
    public void addPlayerToTeam(long userId, int gameId, int teamId) {

    }

    @Override
    public List<TeamObject> getPlayerTeams(long userId) {
        return null;
    }

    @Override
    public List<PlayerObject> getTeamPlayers(int teamId) {
        return null;
    }

    @Override
    public ElementDef addNewElement(int gameId, ElementDef elementDef) {
        return null;
    }

    @Override
    public ElementDef updateElement(int gameId, String id, ElementDef elementDef) {
        return null;
    }

    @Override
    public ElementDef deleteElement(int gameId, String id) {
        return null;
    }

    @Override
    public ElementDef readElement(int gameId, String id) {
        return null;
    }

    @Override
    public AttributeInfo addAttribute(int gameId, AttributeInfo newAttribute) {
        return null;
    }

    @Override
    public List<AttributeInfo> listAllAttributes(int gameId) {
        return null;
    }
}
