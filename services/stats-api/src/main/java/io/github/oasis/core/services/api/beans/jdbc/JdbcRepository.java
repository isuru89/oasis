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

package io.github.oasis.core.services.api.beans.jdbc;

import io.github.oasis.core.Game;
import io.github.oasis.core.TeamMetadata;
import io.github.oasis.core.elements.AttributeInfo;
import io.github.oasis.core.elements.ElementDef;
import io.github.oasis.core.external.OasisRepository;
import io.github.oasis.core.external.PaginatedResult;
import io.github.oasis.core.model.EventSource;
import io.github.oasis.core.model.PlayerObject;
import io.github.oasis.core.model.TeamObject;
import io.github.oasis.core.services.api.dao.IElementDao;
import io.github.oasis.core.services.api.dao.IEventSourceDao;
import io.github.oasis.core.services.api.dao.IGameDao;
import io.github.oasis.core.services.api.dao.IPlayerTeamDao;
import io.github.oasis.core.services.api.dao.dto.GameUpdatePart;
import io.github.oasis.core.services.api.dao.dto.PlayerUpdatePart;
import io.github.oasis.core.services.api.exceptions.ErrorCodes;
import io.github.oasis.core.services.api.exceptions.OasisApiRuntimeException;
import org.jdbi.v3.core.JdbiException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * JDBC database implementation for admin database.
 *
 * @author Isuru Weerarathna
 */
@Component("jdbc")
public class JdbcRepository implements OasisRepository {

    private final IGameDao gameDao;
    private final IEventSourceDao eventSourceDao;
    private final IElementDao elementDao;
    private final IPlayerTeamDao playerTeamDao;

    public JdbcRepository(IGameDao gameDao, IEventSourceDao eventSourceDao, IElementDao elementDao, IPlayerTeamDao playerTeamDao) {
        this.gameDao = gameDao;
        this.eventSourceDao = eventSourceDao;
        this.elementDao = elementDao;
        this.playerTeamDao = playerTeamDao;
    }

    @Override
    public EventSource addEventSource(EventSource eventSource) {
        int newId = eventSourceDao.insertEventSource(eventSource);
        return eventSourceDao.readEventSource(newId);
    }

    @Override
    public EventSource deleteEventSource(int id) {
        EventSource toBeRemoved = eventSourceDao.readEventSource(id);
        eventSourceDao.deleteEventSource(id);
        return toBeRemoved;
    }

    @Override
    public EventSource readEventSource(int id) {
        return eventSourceDao.readEventSource(id);
    }

    @Override
    public EventSource readEventSource(String token) {
        return eventSourceDao.readEventSource(token);
    }

    @Override
    public List<EventSource> listAllEventSources() {
        return eventSourceDao.readAllEventSources();
    }

    @Override
    public List<EventSource> listAllEventSourcesOfGame(int gameId) {
        return eventSourceDao.readEventSourcesOfGame(gameId);
    }

    @Override
    public void addEventSourceToGame(int sourceId, int gameId) {
        eventSourceDao.addEventSourceToGame(gameId, sourceId);
    }

    @Override
    public void removeEventSourceFromGame(int sourceId, int gameId) {
        eventSourceDao.addEventSourceToGame(gameId, sourceId);
    }

    @Override
    public Game addNewGame(Game game) {
        int newGameId = gameDao.insertGame(game);
        return gameDao.readGame(newGameId);
    }

    @Override
    public Game updateGame(int gameId, Game game) {
        gameDao.updateGame(gameId, GameUpdatePart.from(game));
        return gameDao.readGame(gameId);
    }

    @Override
    public Game readGame(int gameId) {
        return gameDao.readGame(gameId);
    }

    @Override
    public Game deleteGame(int gameId) {
        Game toBeDeletedGame = gameDao.readGame(gameId);
        gameDao.deleteGame(gameId);
        return toBeDeletedGame;
    }

    @Override
    public boolean existsGame(String gameName) {
        return Objects.nonNull(gameDao.getGameByName(gameName));
    }

    @Override
    public List<Game> listGames() {
        return gameDao.listGames(0, 50);
    }



    @Override
    public PlayerObject readPlayer(long playerId) {
        return playerTeamDao.readPlayer(playerId);
    }

    @Override
    public PlayerObject readPlayer(String email) {
        return playerTeamDao.readPlayerByEmail(email);
    }

    @Override
    public PlayerObject addPlayer(PlayerObject newPlayer) {
        try {
            long newId = playerTeamDao.insertPlayer(newPlayer);
            return playerTeamDao.readPlayer(newId);
        } catch (JdbiException e) {
            throw new OasisApiRuntimeException(ErrorCodes.PLAYER_EXISTS, e);
        }
    }

    @Override
    public boolean existsPlayer(String email) {
        return Objects.nonNull(playerTeamDao.readPlayerByEmail(email));
    }

    @Override
    public boolean existsPlayer(long playerId) {
        return Objects.nonNull(playerTeamDao.readPlayer(playerId));
    }

    @Override
    public PlayerObject updatePlayer(long playerId, PlayerObject updatedPlayer) {
        playerTeamDao.updatePlayer(playerId, PlayerUpdatePart.from(updatedPlayer));
        return playerTeamDao.readPlayer(playerId);
    }

    @Override
    public PlayerObject deletePlayer(long playerId) {
        PlayerObject player = playerTeamDao.readPlayer(playerId);
        playerTeamDao.deletePlayer(playerId);
        return player;
    }

    @Override
    public TeamObject addTeam(TeamObject teamObject) {
        int newId = playerTeamDao.insertTeam(teamObject);
        return playerTeamDao.readTeam(newId);
    }

    @Override
    public TeamObject readTeam(int teamId) {
        return playerTeamDao.readTeam(teamId);
    }

    @Override
    public TeamObject readTeam(String teamName) {
        return playerTeamDao.readTeamByName(teamName);
    }

    @Override
    public TeamObject updateTeam(int teamId, TeamObject updatedTeam) {
        playerTeamDao.updateTeam(teamId, updatedTeam);
        return playerTeamDao.readTeam(teamId);
    }

    @Override
    public boolean existsTeam(String teamName) {
        return Objects.nonNull(playerTeamDao.readTeamByName(teamName));
    }

    @Override
    public boolean existsTeam(int teamId) {
        return Objects.nonNull(playerTeamDao.readTeam(teamId));
    }

    @Override
    public PaginatedResult<TeamMetadata> searchTeam(String teamName, String offsetAsStr, int maxRecords) {
        int offset = Integer.parseInt(offsetAsStr);
        List<TeamMetadata> metadata = playerTeamDao.readTeamsByName(teamName, offset, maxRecords)
                .stream()
                .map(TeamMetadata::from)
                .collect(Collectors.toList());

        int next = metadata.size() == maxRecords ? offset + maxRecords : -1;
        return new PaginatedResult<>(String.valueOf(next), metadata);
    }

    @Override
    public void removePlayerFromTeam(long playerId, int gameId, int teamId) {
        playerTeamDao.removePlayerFromTeam(gameId, playerId, teamId);
    }

    @Override
    public void addPlayerToTeam(long playerId, int gameId, int teamId) {
        playerTeamDao.insertPlayerToTeam(gameId, playerId, teamId);
    }

    @Override
    public List<TeamObject> getPlayerTeams(long playerId) {
        return playerTeamDao.readPlayerTeams(playerId);
    }

    @Override
    public List<PlayerObject> getTeamPlayers(int teamId) {
        return playerTeamDao.readTeamPlayers(teamId);
    }



    @Override
    public ElementDef addNewElement(int gameId, ElementDef elementDef) {
        int newId = elementDao.insertNewElement(gameId, elementDef);
        return elementDao.readElement(newId);
    }

    @Override
    public ElementDef updateElement(int gameId, String id, ElementDef elementDef) {
        elementDao.updateElement(id, elementDef);
        return elementDao.readElementById(id);
    }

    @Override
    public ElementDef deleteElement(int gameId, String id) {
        ElementDef elementDef = elementDao.readElementById(id);
        elementDao.deleteElementById(id);
        return elementDef;
    }

    @Override
    public ElementDef readElement(int gameId, String id) {
        return elementDao.readElementById(id);
    }

    @Override
    public AttributeInfo addAttribute(int gameId, AttributeInfo newAttribute) {
        int newAttrId = elementDao.insertAttribute(gameId, newAttribute);
        return elementDao.readAttribute(gameId, newAttrId);
    }

    @Override
    public List<AttributeInfo> listAllAttributes(int gameId) {
        return elementDao.readAllAttributes(gameId);
    }
}
