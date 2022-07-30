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

package io.github.oasis.core.external;

import io.github.oasis.core.Game;
import io.github.oasis.core.TeamMetadata;
import io.github.oasis.core.elements.RankInfo;
import io.github.oasis.core.elements.ElementDef;
import io.github.oasis.core.elements.SimpleElementDefinition;
import io.github.oasis.core.model.*;

import java.util.List;

/**
 * @author Isuru Weerarathna
 */
public interface OasisRepository {

    EventSource addEventSource(EventSource eventSource);
    EventSource deleteEventSource(int id);
    EventSource readEventSource(int id);
    EventSource readEventSource(String token);
    EventSourceSecrets readEventSourcePublicSecrets(int id);
    EventSourceSecrets readEventSourceSecrets(int id);
    List<EventSource> listAllEventSources();
    List<EventSource> listAllEventSourcesOfGame(int gameId);
    void addEventSourceToGame(int sourceId, int gameId);
    void removeEventSourceFromGame(int sourceId, int gameId);

    Game addNewGame(Game game);
    Game updateGame(int gameId, Game game);
    Game updateGameStatus(int gameId, String status, long updatedAt);
    Game readGame(int gameId);
    Game deleteGame(int gameId);
    boolean existsGame(String gameName);
    Game readGameByName(String gameName);
    PaginatedResult<Game> listGames(String offset, int pageSize);
    GameStatus readCurrentGameStatus(int gameId);
    List<GameStatus> readGameStatusHistory(int gameId, long startFrom, long endTo);

    PlayerObject readPlayer(long userId);
    PlayerObject readPlayer(String email);
    PlayerObject addPlayer(PlayerObject newUser);
    boolean existsPlayer(String email);
    boolean existsPlayer(long userId);
    PlayerObject updatePlayer(long userId, PlayerObject updatedUser);
    PlayerObject deletePlayer(long userId);

    TeamObject addTeam(TeamObject teamObject);
    TeamObject readTeam(int teamId);
    TeamObject readTeam(String teamName);
    TeamObject updateTeam(int teamId, TeamObject updatedTeam);
    boolean existsTeam(String teamName);
    boolean existsTeam(int teamId);
    PaginatedResult<TeamMetadata> searchTeam(String teamName, String offset, int maxRecords);

    void removePlayerFromTeam(long userId, int gameId, int teamId);
    void addPlayerToTeam(long userId, int gameId, int teamId);
    List<TeamObject> getPlayerTeams(long userId);
    List<PlayerObject> getTeamPlayers(int teamId);

    ElementDef addNewElement(int gameId, ElementDef elementDef);
    ElementDef updateElement(int gameId, String id, SimpleElementDefinition elementDef);
    ElementDef deleteElement(int gameId, String id);
    ElementDef readElement(int gameId, String id);
    ElementDef readElementWithoutData(int gameId, String id);
    List<ElementDef> readElementsByType(int gameId, String type);
    List<ElementDef> readElementsByGameId(int gameId);

    RankInfo addRank(int gameId, RankInfo newRank);
    List<RankInfo> listAllRanks(int gameId);


}
