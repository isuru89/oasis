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

package io.github.oasis.services.profile.domain;

import io.github.oasis.services.profile.internal.dao.IUserDao;
import io.github.oasis.services.profile.internal.dto.EditUserDto;
import io.github.oasis.services.profile.internal.dto.NewUserDto;
import io.github.oasis.services.profile.internal.dto.UserRecord;
import io.github.oasis.services.profile.internal.exceptions.UserNotFoundException;
import io.github.oasis.services.profile.json.NewUserJson;
import org.apache.commons.validator.routines.EmailValidator;

import java.util.Optional;

/**
 * @author Isuru Weerarathna
 */
public class User {

    private static final EmailValidator EMAIL_VALIDATOR = EmailValidator.getInstance();

    private IUserDao user;

    public User(IUserDao user) {
        this.user = user;
    }

    public NewUserJson addUser(NewUserDto newUser) {
        newUser.validate(EMAIL_VALIDATOR);

        int newUserId = user.addUser(newUser);
        return NewUserJson.from(newUserId);
    }

    public UserRecord editUser(int userId, EditUserDto editUser) {
        return user.editUser(userId, editUser);
    }

    public void deleteUser(int userId) {
        user.deactivateUser(userId);
    }

    public UserRecord readUserById(int userId) throws UserNotFoundException {
        return user.readUserById(userId)
                .orElseThrow(() -> new UserNotFoundException(String.valueOf(userId)));
    }

    public Optional<UserRecord> readUserByEmail(String userEmail) {
        return user.readUserByEmail(userEmail);
    }

    public UserRecord readUserWithTeams(int userId) throws UserNotFoundException {
        return Optional.ofNullable(user.readTeamsOfUser(userId))
                .orElseThrow(() -> new UserNotFoundException(String.valueOf(userId)));
    }

    public void updateUserStatus(int userId, int statusId) {
        user.updateUserStatus(userId, statusId);
    }
}
