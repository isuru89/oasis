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

import io.github.oasis.services.profile.internal.dto.EditUserDto;
import io.github.oasis.services.profile.internal.dto.NewUserDto;
import io.github.oasis.services.profile.internal.dto.TeamRecord;
import io.github.oasis.services.profile.internal.dto.UserRecord;
import io.github.oasis.services.profile.internal.exceptions.UserNotFoundException;
import org.jdbi.v3.core.result.LinkedHashMapRowReducer;
import org.jdbi.v3.core.result.RowView;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.sqlobject.statement.UseRowReducer;
import org.jdbi.v3.sqlobject.transaction.Transaction;

import java.util.Map;
import java.util.Optional;

/**
 * @author Isuru Weerarathna
 */
public interface IUserDao {

    @SqlUpdate("INSERT INTO OA_USER" +
            " (email, first_name, last_name, nickname, avatar_ref, user_status, gender, is_auto_user, created_at)" +
            " VALUES" +
            " (:email, :firstName, :lastName, :nickname, :avatar, :status, :gender, :autoUser, :createdAt)")
    @GetGeneratedKeys("user_id")
    int addUser(@BindBean NewUserDto newUserInfo);

    @SqlQuery("SELECT user_id id, email, first_name firstName, last_name lastName," +
            " nickname, avatar_ref avatar, user_status status, gender, is_active active," +
            " is_auto_user autoUser, created_at createdAt " +
            " FROM OA_USER WHERE email = :email")
    @RegisterBeanMapper(UserRecord.class)
    Optional<UserRecord> readUserByEmail(@Bind("email") String email);

    @SqlQuery("SELECT user_id id, email, first_name firstName, last_name lastName," +
            " nickname, avatar_ref avatar, user_status status, gender, is_active active," +
            " is_auto_user autoUser, created_at createdAt " +
            " FROM OA_USER WHERE user_id = :userId")
    @RegisterBeanMapper(UserRecord.class)
    Optional<UserRecord> readUserById(@Bind("userId") int userId);

    @SqlUpdate("UPDATE OA_USER" +
            " SET" +
            "   first_name = :firstName, last_name = :lastName, nickname = :nickname," +
            "   avatar_ref = :avatar, gender = :gender" +
            " WHERE user_id = :userId")
    void updateUser(@Bind("userId") int userId, @BindBean UserRecord userRecord);

    @Transaction
    default UserRecord editUser(int userId, EditUserDto editUserInfo) throws UserNotFoundException {
        UserRecord userRecord = readUserById(userId)
                .orElseThrow(() -> new UserNotFoundException(String.valueOf(userId)));

        updateUser(userId, userRecord.mergeChanges(editUserInfo));
        return readUserById(userId).orElse(null);
    }

    @SqlUpdate("UPDATE OA_USER SET is_active = false, user_status = 9 WHERE user_id = :userId")
    void deactivateUser(@Bind("userId") int userId);

    @SqlQuery("SELECT oau.user_id u_id," +
            "         oau.email u_email," +
            "         oau.nickname u_firstName," +
            "         oau.nickname u_lastName," +
            "         oau.nickname u_nickname," +
            "         oau.avatar_ref u_avatar," +
            "         oau.user_status u_status," +
            "         oau.gender u_gender," +
            "         oau.is_active u_active," +
            "         oau.is_auto_user u_autoUser," +
            "         oat.team_id t_id," +
            "         oat.name t_name," +
            "         oat.motto t_motto," +
            "         oat.avatar_ref t_avatar" +
            " FROM OA_USER oau" +
            "   INNER JOIN OA_USER_TEAM oaut ON oau.user_id = oaut.user_id" +
            "   INNER JOIN OA_TEAM oat ON oaut.team_id = oat.team_id" +
            " WHERE" +
            "   oau.user_id = :userId " +
            "   AND" +
            "   oat.is_active = true")
    @RegisterBeanMapper(value = UserRecord.class, prefix = "u")
    @RegisterBeanMapper(value = TeamRecord.class, prefix = "t")
    @UseRowReducer(UserReducer.class)
    UserRecord readTeamsOfUser(@Bind("userId") int userId);

    @SqlUpdate("UPDATE OA_USER" +
            " SET user_status = :status" +
            " WHERE user_id = :userId")
    void updateUserStatus(@Bind("userId") int userId,
                          @Bind("status") int status);

    class UserReducer implements LinkedHashMapRowReducer<Integer, UserRecord> {

        @Override
        public void accumulate(Map<Integer, UserRecord> row, RowView rowView) {
            UserRecord userRecord = row.computeIfAbsent(rowView.getColumn("u_id", Integer.class),
                    id -> rowView.getRow(UserRecord.class));

            if (rowView.getColumn("t_id", Integer.class) != null) {
                userRecord.addTeam(rowView.getRow(TeamRecord.class));
            }
        }
    }
}
