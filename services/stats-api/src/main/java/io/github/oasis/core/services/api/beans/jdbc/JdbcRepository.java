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
import io.github.oasis.core.ID;
import io.github.oasis.core.TeamMetadata;
import io.github.oasis.core.elements.RankInfo;
import io.github.oasis.core.elements.ElementDef;
import io.github.oasis.core.elements.SimpleElementDefinition;
import io.github.oasis.core.external.OasisRepository;
import io.github.oasis.core.external.PaginatedResult;
import io.github.oasis.core.model.*;
import io.github.oasis.core.services.SerializationSupport;
import io.github.oasis.core.services.api.dao.IElementDao;
import io.github.oasis.core.services.api.dao.IEventSourceDao;
import io.github.oasis.core.services.api.dao.IGameDao;
import io.github.oasis.core.services.api.dao.IPlayerTeamDao;
import io.github.oasis.core.services.api.dao.dto.*;
import io.github.oasis.core.services.api.exceptions.ErrorCodes;
import io.github.oasis.core.services.api.exceptions.OasisApiRuntimeException;
import org.jdbi.v3.core.JdbiException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * JDBC database implementation for admin database.
 *
 * @author Isuru Weerarathna
 */
@Component("jdbc")
public class JdbcRepository implements OasisRepository {

    public static final int KEY_DOWNLOAD_LIMIT = 1;

    private final IGameDao gameDao;
    private final IEventSourceDao eventSourceDao;
    private final IElementDao elementDao;
    private final IPlayerTeamDao playerTeamDao;
    private final SerializationSupport serializationSupport;

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
        try {
            EventSourceDto dto = eventSourceDao.insertEventSource(EventSourceDto.from(eventSource));
            return dto.toEventSource();
        } catch (JdbiException e) {
            throw new OasisApiRuntimeException(ErrorCodes.EVENT_SOURCE_ALREADY_EXISTS, HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public EventSource deleteEventSource(int id) {
        EventSourceDto toBeRemoved = eventSourceDao.readEventSource(id);
        if (toBeRemoved != null) {
            eventSourceDao.deleteEventSource(id);
            return toBeRemoved.toEventSource();
        }
        throw new OasisApiRuntimeException(ErrorCodes.EVENT_SOURCE_NOT_EXISTS, HttpStatus.BAD_REQUEST);
    }

    @Override
    public EventSource readEventSource(int id) {
        return Optional.ofNullable(eventSourceDao.readEventSource(id))
                .map(EventSourceDto::toEventSource)
                .map(src -> {
                    List<Integer> gameIds = eventSourceDao.readEventSourceGames(src.getId());
                    src.setGames(Set.copyOf(gameIds));
                    return src;
                })
                .orElseThrow(() -> new OasisApiRuntimeException(ErrorCodes.EVENT_SOURCE_NOT_EXISTS, HttpStatus.NOT_FOUND));
    }

    @Override
    public EventSource readEventSource(String token) {
        return Optional.ofNullable(eventSourceDao.readEventSourceByToken(token))
                .map(EventSourceDto::toEventSource)
                .map(src -> {
                    List<Integer> gameIds = eventSourceDao.readEventSourceGames(src.getId());
                    src.setGames(Set.copyOf(gameIds));
                    return src;
                })
                .orElseThrow(() -> new OasisApiRuntimeException(ErrorCodes.EVENT_SOURCE_NOT_EXISTS, HttpStatus.NOT_FOUND));
    }

    @Override
    public EventSourceSecrets readEventSourcePublicSecrets(int id) {
        EventSourceSecretsDto eventSourceKeys = eventSourceDao.readEventSourceKeys(id);
        EventSourceSecrets secrets = new EventSourceSecrets();
        secrets.setPublicKey(eventSourceKeys.getPublicKey());
        return secrets;
    }

    @Override
    public EventSourceSecrets readEventSourceSecrets(int id) {
        EventSourceSecretsDto dto = eventSourceDao.readKeysAndIncrement(id, KEY_DOWNLOAD_LIMIT);
        if (dto.getDownloadCount() >= KEY_DOWNLOAD_LIMIT) {
            throw new OasisApiRuntimeException(ErrorCodes.EVENT_SOURCE_DOWNLOAD_LIMIT_EXCEEDED, HttpStatus.FORBIDDEN);
        }
        EventSourceSecrets secrets = new EventSourceSecrets();
        secrets.setPrivateKey(dto.getPrivateKey());
        secrets.setPublicKey(dto.getPublicKey());
        return secrets;
    }

    @Override
    public List<EventSource> listAllEventSources() {
        return eventSourceDao.readAllEventSources()
                .stream()
                .map(EventSourceDto::toEventSource)
                .collect(Collectors.toList());
    }

    @Override
    public List<EventSource> listAllEventSourcesOfGame(int gameId) {
        return eventSourceDao.readEventSourcesOfGame(gameId)
                .stream()
                .map(EventSourceDto::toEventSource)
                .collect(Collectors.toList());
    }

    @Override
    public void addEventSourceToGame(int sourceId, int gameId) {
        try {
            eventSourceDao.addEventSourceToGame(gameId, sourceId);
        } catch (JdbiException e) {
            throw new OasisApiRuntimeException(ErrorCodes.EVENT_SOURCE_ALREADY_MAPPED, HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public void removeEventSourceFromGame(int sourceId, int gameId) {
        eventSourceDao.removeEventSourceFromGame(gameId, sourceId);
    }

    @Override
    public Game addNewGame(Game game) {
        try {
            int newGameId = gameDao.insertGame(game);
            return gameDao.readGame(newGameId);
        } catch (JdbiException e) {
            throw new OasisApiRuntimeException(ErrorCodes.GAME_ALREADY_EXISTS, HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public Game updateGame(int gameId, Game game) {
        Game toBeUpdatedGame = gameDao.readGame(gameId);
        if (toBeUpdatedGame == null) {
            throw new OasisApiRuntimeException(ErrorCodes.GAME_NOT_EXISTS);
        }

        if (toBeUpdatedGame.getVersion() != game.getVersion()) {
            throw new OasisApiRuntimeException(ErrorCodes.GAME_UPDATE_CONFLICT, HttpStatus.CONFLICT);
        }

        GameUpdatePart gameUpdatePart = GameUpdatePart.from(game);
        gameDao.updateGame(gameId, gameUpdatePart, System.currentTimeMillis());
        return gameDao.readGame(gameId);
    }

    @Override
    public Game updateGameStatus(int gameId, String status, long updatedAt) {
        gameDao.updateGameStatus(gameId, status, updatedAt);
        return gameDao.readGame(gameId);
    }

    @Override
    public Game readGame(int gameId) {
        Game game = gameDao.readGame(gameId);
        if (game == null) {
            throw new OasisApiRuntimeException(ErrorCodes.GAME_NOT_EXISTS, HttpStatus.NOT_FOUND);
        }
        return game;
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
    public GameStatus readCurrentGameStatus(int gameId) {
        GameStatusDto dto = gameDao.readGameStatus(gameId);
        if (dto == null) {
            throw new OasisApiRuntimeException(ErrorCodes.GAME_NOT_EXISTS);
        }

        return toGameStatus(gameId, dto);
    }

    @Override
    public List<GameStatus> readGameStatusHistory(int gameId, long startFrom, long endTo) {
        return gameDao.listGameStatusHistory(gameId, startFrom, endTo).stream()
                .map(r -> toGameStatus(gameId, r))
                .collect(Collectors.toList());
    }


    @Override
    public PlayerObject readPlayer(long playerId) {
        var player = playerTeamDao.readPlayer(playerId);
        if (player == null) {
            throw new OasisApiRuntimeException(ErrorCodes.PLAYER_DOES_NOT_EXISTS, HttpStatus.NOT_FOUND);
        }
        return player;
    }

    @Override
    public PlayerObject readPlayer(String email) {
        var player = playerTeamDao.readPlayerByEmail(email);
        if (player == null) {
            throw new OasisApiRuntimeException(ErrorCodes.PLAYER_DOES_NOT_EXISTS, HttpStatus.NOT_FOUND);
        }
        return player;
    }

    @Override
    public PlayerObject addPlayer(PlayerObject newPlayer) {
        try {
            long newId = playerTeamDao.insertPlayer(newPlayer);
            return playerTeamDao.readPlayer(newId);
        } catch (JdbiException e) {
            throw new OasisApiRuntimeException(ErrorCodes.PLAYER_EXISTS, HttpStatus.BAD_REQUEST);
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

    @CacheEvict(value = ID.CACHE_USERS_META, key = "#playerId")
    @Override
    public PlayerObject updatePlayer(long playerId, PlayerObject updatedPlayer) {
        PlayerObject player = playerTeamDao.readPlayer(playerId);
        if (player == null) {
            throw new OasisApiRuntimeException(ErrorCodes.PLAYER_DOES_NOT_EXISTS, HttpStatus.NOT_FOUND);
        }

        int updatedRecords = playerTeamDao.updatePlayer(player.getId(), PlayerUpdatePart.from(updatedPlayer));
        if (updatedRecords < 1) {
            throw new OasisApiRuntimeException(ErrorCodes.PLAYER_UPDATE_CONFLICT, HttpStatus.CONFLICT);
        }

        return playerTeamDao.readPlayer(playerId);
    }

    @CacheEvict(value = ID.CACHE_USERS_META, key = "#playerId")
    @Override
    public PlayerObject deletePlayer(long playerId) {
        PlayerObject player = playerTeamDao.readPlayer(playerId);
        if (player == null) {
            throw new OasisApiRuntimeException(ErrorCodes.PLAYER_DOES_NOT_EXISTS, HttpStatus.NOT_FOUND);
        } else if (!player.isActive()) {
            throw new OasisApiRuntimeException(ErrorCodes.PLAYER_IS_DEACTIVATED, HttpStatus.BAD_REQUEST);
        }
        playerTeamDao.deletePlayer(playerId);
        return player;
    }

    @Override
    public TeamObject addTeam(TeamObject teamObject) {
        try {
            int newId = playerTeamDao.insertTeam(teamObject);
            return playerTeamDao.readTeam(newId);
        } catch (JdbiException e) {
            throw new OasisApiRuntimeException(ErrorCodes.TEAM_EXISTS, HttpStatus.BAD_REQUEST);
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

    @CacheEvict(value = ID.CACHE_TEAMS_META, key = "#teamId")
    @Override
    public TeamObject updateTeam(int teamId, TeamObject updatedTeam) {
        int updatedRecords = playerTeamDao.updateTeam(teamId, updatedTeam);
        if (updatedRecords < 1) {
            throw new OasisApiRuntimeException(ErrorCodes.TEAM_UPDATE_CONFLICT, HttpStatus.CONFLICT);
        }

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
            playerTeamDao.insertPlayerToTeam(playerId, teamId);
        } catch (JdbiException e) {
            throw new OasisApiRuntimeException(ErrorCodes.PLAYER_ALREADY_IN_TEAM, HttpStatus.BAD_REQUEST);
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
            throw new OasisApiRuntimeException(ErrorCodes.ELEMENT_ALREADY_EXISTS, HttpStatus.BAD_REQUEST);
        }
    }

    @Caching(evict = {
            @CacheEvict(value = ID.CACHE_ELEMENTS, key = "{#gameId, #id}"),
            @CacheEvict(value = ID.CACHE_ELEMENTS_META, key = "{#gameId, #id}")
    })
    @Override
    public ElementDef updateElement(int gameId, String id, SimpleElementDefinition elementDef) {
        ElementDef dbDef = readElementWithoutData(gameId, id);
        if (dbDef == null) {
            throw new OasisApiRuntimeException(ErrorCodes.ELEMENT_NOT_EXISTS, HttpStatus.NOT_FOUND);
        }

        if (dbDef.getVersion() != elementDef.getVersion()) {
            throw new OasisApiRuntimeException(ErrorCodes.ELEMENT_UPDATE_CONFLICT, HttpStatus.CONFLICT);
        }

        ElementUpdateDto dto = new ElementUpdateDto();
        dto.setName(elementDef.getName());
        dto.setDescription(elementDef.getDescription());
        dto.setVersion(elementDef.getVersion());

        elementDao.updateElement(id, dto, System.currentTimeMillis());
        return toElementDef(elementDao.readElement(id));
    }

    @Caching(evict = {
            @CacheEvict(value = ID.CACHE_ELEMENTS, key = "{#gameId, #id}"),
            @CacheEvict(value = ID.CACHE_ELEMENTS_META, key = "{#gameId, #id}")
    })
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
                .filter(ElementDto::isActive)
                .map(this::toElementDef)
                .collect(Collectors.toList());
    }

    @Override
    public List<ElementDef> readElementsByGameId(int gameId) {
        return elementDao.readElementsByGameId(gameId)
                .stream()
                .map(this::toElementDef)
                .collect(Collectors.toList());
    }

    @CacheEvict(value = ID.CACHE_RANKS, key = "#gameId")
    @Override
    public RankInfo addRank(int gameId, RankInfo newRank) {
        try {
            int newAttrId = elementDao.insertRank(gameId, newRank);
            return elementDao.readRank(gameId, newAttrId);
        } catch (Exception e) {
            throw new OasisApiRuntimeException(ErrorCodes.RANK_EXISTS, HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public List<RankInfo> listAllRanks(int gameId) {
        return elementDao.readAllRanks(gameId);
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
            resultDef.setData(serializationSupport.deserializeToMap(dto.getData()));
        }
        return resultDef;
    }

    private GameStatus toGameStatus(int gameId, GameStatusDto dto) {
        var status = new GameStatus();
        status.setStatus(dto.getStatus());
        status.setGameId(gameId);
        status.setUpdatedAt(dto.getUpdatedAt());
        return status;
    }
}
