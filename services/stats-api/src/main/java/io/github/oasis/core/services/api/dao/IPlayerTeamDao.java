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

package io.github.oasis.core.services.api.dao;

import io.github.oasis.core.model.PlayerObject;
import io.github.oasis.core.model.TeamObject;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;

/**
 * @author Isuru Weerarathna
 */
public interface IPlayerTeamDao {

    @SqlQuery
    PlayerObject readPlayer(long playerId);

    @SqlQuery
    PlayerObject readPlayerByEmail(String playerEmail);

    @SqlUpdate
    long insertPlayer(PlayerObject newPlayer);

    @SqlUpdate
    void updatePlayer(long playerId, PlayerObject updateData);

    @SqlUpdate
    void deletePlayer(long playerId);

    @SqlUpdate
    int insertTeam(TeamObject newTeam);

    @SqlQuery
    TeamObject readTeam(int teamId);

    @SqlQuery
    TeamObject readTeamByName(String name);

    @SqlUpdate
    void updateTeam(int teamId, TeamObject teamObject);

    @SqlQuery
    List<TeamObject> readTeamsByName(String teamName, int offset, int size);

    @SqlUpdate
    void insertPlayerToTeam(int gameId, long playerId, int teamId);

    @SqlUpdate
    void removePlayerFromTeam(int gameId, long playerId, int teamId);

    @SqlQuery
    List<TeamObject> readPlayerTeams(long playerId);

    @SqlQuery
    List<PlayerObject> readTeamPlayers(int teamId);
}
