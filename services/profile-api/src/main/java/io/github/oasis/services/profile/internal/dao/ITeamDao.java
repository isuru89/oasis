/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.github.oasis.services.profile.internal.dao;

import io.github.oasis.services.profile.internal.ErrorCodes;
import io.github.oasis.services.profile.internal.dto.EditTeamDto;
import io.github.oasis.services.profile.internal.dto.NewTeamDto;
import io.github.oasis.services.profile.internal.dto.TeamRecord;
import io.github.oasis.services.profile.internal.dto.UserRecord;
import io.github.oasis.services.profile.internal.exceptions.TeamNotFoundException;
import io.github.oasis.services.profile.internal.exceptions.TeamUpdateException;
import org.jdbi.v3.core.JdbiException;
import org.jdbi.v3.core.result.LinkedHashMapRowReducer;
import org.jdbi.v3.core.result.RowView;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.BindList;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.sqlobject.statement.UseRowReducer;
import org.jdbi.v3.sqlobject.transaction.Transaction;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author Isuru Weerarathna
 */
public interface ITeamDao {


    @SqlUpdate("INSERT INTO OA_TEAM" +
            " (name, motto, avatar_ref, created_at)" +
            " VALUES" +
            " (:name, :motto, :avatar, :createdAt)")
    @GetGeneratedKeys("team_id")
    int addTeam(@BindBean NewTeamDto newTeam);

    default int insertTeam(NewTeamDto newTeam) throws TeamUpdateException {
        try {
            return addTeam(newTeam);
        } catch (JdbiException e) {
            throw new TeamUpdateException(ErrorCodes.TEAM_NAME_EXISTS,
                    "Team is already exist by name '" + newTeam.getName() + "'!");
        }
    }

    @SqlUpdate("UPDATE OA_TEAM" +
            " SET name = :name, motto = :motto, avatar_ref = :avatar" +
            " WHERE team_id = :teamId")
    int updateTeam(@Bind("teamId") int teamId,
                    @BindBean TeamRecord updatedTeam);

    @Transaction
    default int editTeam(int teamId, EditTeamDto editTeamInfo) throws TeamNotFoundException, TeamUpdateException {
        TeamRecord teamRecord = readTeamById(teamId)
                .orElseThrow(() -> new TeamNotFoundException(String.valueOf(teamId)));

        try {
            return updateTeam(teamId, teamRecord.mergeChanges(editTeamInfo));
        } catch (Exception e) {
            throw new TeamUpdateException(ErrorCodes.TEAM_NAME_EXISTS,
                    "There is a team already by name '" + editTeamInfo.getName() + "'!", e);
        }
    }

    @SqlQuery("SELECT team_id id, name, motto, avatar_ref avatar, is_active active " +
            " FROM OA_TEAM" +
            " WHERE team_id = :teamId")
    @RegisterBeanMapper(TeamRecord.class)
    Optional<TeamRecord> readTeamById(@Bind("teamId") int teamId);

    @SqlQuery("SELECT team_id id, name, motto, avatar_ref avatar, is_active active " +
            " FROM OA_TEAM" +
            " WHERE name = :teamName")
    @RegisterBeanMapper(TeamRecord.class)
    Optional<TeamRecord> readTeamByName(@Bind("teamName") String teamName);

    @SqlQuery("SELECT team_id id, name, motto, avatar_ref avatar, is_active active " +
            " FROM OA_TEAM" +
            " WHERE team_id IN (<teamIds>)")
    @RegisterBeanMapper(TeamRecord.class)
    List<TeamRecord> readTeams(@BindList("teamIds") List<Integer> teamIds);

    @SqlQuery("SELECT oau.user_id u_id," +
            "         oau.email u_email," +
            "         oau.nickname u_firstName," +
            "         oau.nickname u_lastName," +
            "         oau.nickname u_nickname," +
            "         oau.avatar_ref u_avatar," +
            "         oau.user_status u_status," +
            "         oau.gender u_gender," +
            "         oat.team_id t_id," +
            "         oat.name t_name," +
            "         oat.motto t_motto," +
            "         oat.avatar_ref t_avatar" +
            " FROM OA_TEAM oat" +
            "   INNER JOIN OA_USER_TEAM oaut ON oat.team_id = oaut.team_id" +
            "   INNER JOIN OA_USER oau ON oau.user_id = oaut.user_id" +
            " WHERE" +
            "   oat.team_id = :teamId " +
            "   AND" +
            "   oau.is_active = true")
    @RegisterBeanMapper(value = UserRecord.class, prefix = "u")
    @RegisterBeanMapper(value = TeamRecord.class, prefix = "t")
    @UseRowReducer(TeamReducer.class)
    TeamRecord listTeamUsers(@Bind("teamId") int teamId);

    @SqlUpdate("INSERT INTO OA_USER_TEAM" +
            " (user_id, team_id, :joined_at)" +
            " VALUES " +
            " (:userId, :teamId, :joinedAt)")
    int addUserToTeam(@Bind("userId") int userId,
                      @Bind("teamId") int teamId,
                      @Bind("joinedAt") Instant joinedAt);

    @SqlUpdate("DELETE FROM OA_USER_TEAM" +
            " WHERE user_id = :userId AND team_id = :teamId")
    int removeUserFromTeam(@Bind("userId") int userId,
                           @Bind("teamId") int teamId);

    class TeamReducer implements LinkedHashMapRowReducer<Integer, TeamRecord> {

        @Override
        public void accumulate(Map<Integer, TeamRecord> row, RowView rowView) {
            TeamRecord teamRecord = row.computeIfAbsent(rowView.getColumn("t_id", Integer.class),
                    id -> rowView.getRow(TeamRecord.class));

            if (rowView.getColumn("u_id", Integer.class) != null) {
                teamRecord.addUser(rowView.getRow(UserRecord.class));
            }
        }
    }
}
