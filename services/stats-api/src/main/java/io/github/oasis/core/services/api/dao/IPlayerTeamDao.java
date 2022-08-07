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
import io.github.oasis.core.services.api.dao.configs.DaoConstants;
import io.github.oasis.core.services.api.dao.configs.UseOasisSqlLocator;
import io.github.oasis.core.services.api.dao.dto.PlayerUpdatePart;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.sqlobject.transaction.Transaction;

import java.util.List;

/**
 * @author Isuru Weerarathna
 */
@UseOasisSqlLocator("io/github/oasis/db/scripts/players")
@RegisterBeanMapper(PlayerObject.class)
@RegisterBeanMapper(TeamObject.class)
public interface IPlayerTeamDao {

    @SqlQuery
    PlayerObject readPlayer(@Bind("id") long playerId);

    @SqlQuery
    PlayerObject readPlayerByEmail(@Bind("email") String playerEmail);

    @SqlUpdate
    @GetGeneratedKeys(DaoConstants.ID)
    int insertPlayer(@BindBean PlayerObject newPlayer, @Bind("ts") long ts);

    default int insertPlayer(PlayerObject newPlayer) {
        return insertPlayer(newPlayer, System.currentTimeMillis());
    }

    @SqlUpdate
    int updatePlayer(@Bind("id") long playerId, @BindBean PlayerUpdatePart updateData, @Bind("ts") long ts);

    default int updatePlayer(long playerId, PlayerUpdatePart updateData) {
        return updatePlayer(playerId, updateData, System.currentTimeMillis());
    }

    @Transaction
    default PlayerObject insertAndGet(PlayerObject newPlayer) {
        int id = insertPlayer(newPlayer);
        return readPlayer(id);
    }

    @SqlUpdate
    void deletePlayer(@Bind("id") long playerId);

    @SqlUpdate
    @GetGeneratedKeys(DaoConstants.ID)
    int insertTeam(@BindBean TeamObject newTeam, @Bind("ts") long timestamp);

    default int insertTeam(TeamObject newTeam) {
        return insertTeam(newTeam, System.currentTimeMillis());
    }

    @SqlQuery
    TeamObject readTeam(@Bind("id") int teamId);

    @SqlQuery
    TeamObject readTeamByName(@Bind("name") String name);

    @SqlUpdate
    int updateTeam(@Bind("id") int teamId, @BindBean TeamObject teamObject, @Bind("ts") long ts);

    default int updateTeam(int teamId, TeamObject teamObject) {
        return updateTeam(teamId, teamObject, System.currentTimeMillis());
    }

    @SqlQuery
    List<TeamObject> readTeamsByName(@Bind("name") String teamName, @Bind("offset") int offset, @Bind("limit") int size);

    @SqlUpdate
    void insertPlayerToTeam(@Bind("playerId") long playerId,
                            @Bind("teamId") int teamId,
                            @Bind("ts") long timestamp);

    default void insertPlayerToTeam(long playerId, int teamId) {
        insertPlayerToTeam(playerId, teamId, System.currentTimeMillis());
    }

    @SqlUpdate
    void removePlayerFromTeam(@Bind("gameId") int gameId, @Bind("playerId") long playerId, @Bind("teamId") int teamId);

    @SqlQuery
    List<TeamObject> readPlayerTeams(@Bind("playerId") long playerId);

    @SqlQuery
    List<PlayerObject> readTeamPlayers(@Bind("teamId") int teamId);
}
