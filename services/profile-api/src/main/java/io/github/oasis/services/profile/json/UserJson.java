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

package io.github.oasis.services.profile.json;

import io.github.oasis.services.profile.domain.Gender;
import io.github.oasis.services.profile.domain.UserStatus;
import io.github.oasis.services.profile.internal.dto.TeamRecord;
import io.github.oasis.services.profile.internal.dto.UserRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Isuru Weerarathna
 */
public class UserJson {

    private int id;
    private String firstName;
    private String lastName;
    private String nickname;
    private String email;

    private String avatar;

    private UserStatus status;
    private Gender gender;

    private boolean autoUser;
    private boolean active;

    private List<TeamRecord> teams;

    public static UserJson from(UserRecord record) {
        UserJson user = new UserJson();
        user.id = record.getId();
        user.firstName = record.getFirstName();
        user.lastName = record.getLastName();
        user.email = record.getEmail();
        user.avatar = record.getAvatar();
        user.gender = Gender.from(record.getGender());
        user.status = UserStatus.from(record.getStatus());
        user.active = record.isActive();
        user.nickname = record.getNickname();
        user.autoUser = record.isAutoUser();
        if (Objects.isNull(record.getTeams())) {
            user.teams = null;
        } else {
            user.teams = new ArrayList<>(record.getTeams());
        }
        return user;
    }

    public boolean isAutoUser() {
        return autoUser;
    }

    public int getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getNickname() {
        return nickname;
    }

    public String getEmail() {
        return email;
    }

    public String getAvatar() {
        return avatar;
    }

    public UserStatus getStatus() {
        return status;
    }

    public Gender getGender() {
        return gender;
    }

    public boolean isActive() {
        return active;
    }

    public List<TeamRecord> getTeams() {
        return teams;
    }
}
