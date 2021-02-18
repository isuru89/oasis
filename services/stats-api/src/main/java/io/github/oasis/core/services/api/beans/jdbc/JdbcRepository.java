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

import com.google.gson.reflect.TypeToken;
import io.github.oasis.core.Game;
import io.github.oasis.core.TeamMetadata;
import io.github.oasis.core.elements.AttributeInfo;
import io.github.oasis.core.elements.ElementDef;
import io.github.oasis.core.external.OasisRepository;
import io.github.oasis.core.external.PaginatedResult;
import io.github.oasis.core.model.EventSource;
import io.github.oasis.core.model.PlayerObject;
import io.github.oasis.core.model.TeamObject;
import io.github.oasis.core.services.SerializationSupport;
import io.github.oasis.core.services.api.dao.IElementDao;
import io.github.oasis.core.services.api.dao.IEventSourceDao;
import io.github.oasis.core.services.api.dao.IGameDao;
import io.github.oasis.core.services.api.dao.IPlayerTeamDao;
import io.github.oasis.core.services.api.dao.dto.GameUpdatePart;
import io.github.oasis.core.services.api.dao.dto.PlayerUpdatePart;
import io.github.oasis.core.services.api.exceptions.ErrorCodes;
import io.github.oasis.core.services.api.exceptions.OasisApiRuntimeException;
import io.github.oasis.core.services.api.to.ElementDto;
import org.jdbi.v3.core.JdbiException;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
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
    private final SerializationSupport serializationSupport;

    private final Type mapType = new TypeToken<Map<String, Object>>() {}.getType();

    public JdbcRepository(IGameDao gameDao,
                          IEventSourceDao eventSourceDao,
                          IElementDao elementDao,
                          IPlayerTeamDao playerTeamDao,
                          SerializationSupport serializationSupport) {
        this.gameDao = gameDao;
        this.eventSourceDao = eventSourceDao;
        this.elementDao = elementDao;
        this.playerTeamDao = playerTeamDao;
        this.serializationSupport = serializationSupport;
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
        try {
            int newGameId = gameDao.insertGame(game);
            return gameDao.readGame(newGameId);
        } catch (JdbiException e) {
            throw new OasisApiRuntimeException(ErrorCodes.GAME_ALREADY_EXISTS, e);
        }
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
        return Objects.nonNull(gameDao.readGameByName(gameName));
    }

    @Override
    public Game readGameByName(String gameName) {
        return gameDao.readGameByName(gameName);
    }

    @Override
    public PaginatedResult<Game> listGames(String offsetAttr, int pageSize) {
        int offset = Integer.parseInt(offsetAttr);
        List<Game> games = gameDao.listGames(offset, pageSize);

        int next = games.size() == pageSize ? offset + pageSize : -1;
        return new PaginatedResult<>(String.valueOf(next), games);
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
        try {
            int newId = playerTeamDao.insertTeam(teamObject);
            return playerTeamDao.readTeam(newId);
        } catch (JdbiException e) {
            throw new OasisApiRuntimeException(ErrorCodes.TEAM_EXISTS, e);
        }
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
        try {
            playerTeamDao.insertPlayerToTeam(gameId, playerId, teamId);
        } catch (JdbiException e) {
            throw new OasisApiRuntimeException(ErrorCodes.PLAYER_ALREADY_IN_TEAM, e);
        }
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
        try {
            ElementDto dto = toElementDto(gameId, elementDef);
            elementDao.insertNewElement(dto);
            return toElementDef(elementDao.readElementWithData(elementDef.getElementId()));
        } catch (JdbiException ex) {
            throw new OasisApiRuntimeException(ErrorCodes.ELEMENT_ALREADY_EXISTS, ex);
        }
    }

    @Override
    public ElementDef updateElement(int gameId, String id, ElementDef elementDef) {
        var dto = toElementDto(gameId, elementDef);
        elementDao.updateElement(elementDef.getElementId(), dto);
        return toElementDef(elementDao.readElement(id));
    }

    @Override
    public ElementDef deleteElement(int gameId, String id) {
        ElementDef elementDef = toElementDef(elementDao.readElementWithData(id));
        elementDao.deleteElement(elementDef.getId());
        return elementDef;
    }

    @Override
    public ElementDef readElement(int gameId, String id) {
        var dto = elementDao.readElementWithData(id);
        if (dto != null && dto.isActive()) {
            return toElementDef(dto);
        }
        return null;
    }

    @Override
    public ElementDef readElementWithoutData(int gameId, String id) {
        var dto = elementDao.readElement(id);
        if (dto != null && dto.isActive()) {
            return toElementDef(dto);
        }
        return null;
    }

    @Override
    public List<ElementDef> readElementsByType(int gameId, String type) {
        return elementDao.readElementsByType(gameId, type)
                .stream()
                .map(this::toElementDef)
                .collect(Collectors.toList());
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

    private ElementDto toElementDto(int gameId, ElementDef def) {
        ElementDto dto = ElementDto.fromWithoutData(def);
        dto.setGameId(gameId);
        dto.setData(serializationSupport.serialize(def.getData()).getBytes(StandardCharsets.UTF_8));
        return dto;
    }

    private ElementDef toElementDef(ElementDto dto) {
        ElementDef resultDef = dto.toDefWithoutData();
        if (Objects.nonNull(dto.getData())) {
            resultDef.setData(serializationSupport.deserialize(dto.getData(), mapType));
        }
        return resultDef;
    }
}
