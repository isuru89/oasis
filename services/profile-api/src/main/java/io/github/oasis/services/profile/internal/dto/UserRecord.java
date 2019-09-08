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

package io.github.oasis.services.profile.internal.dto;

import io.github.oasis.services.common.Utils;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * @author Isuru Weerarathna
 */
public class UserRecord {

    private int id;
    private String firstName;
    private String lastName;
    private String nickname;
    private String email;

    private String avatar;

    private int status;
    private int gender;

    private boolean autoUser = false;
    private boolean active = true;

    private Set<TeamRecord> teams;

    public UserRecord mergeChanges(EditUserDto editUser) {
        this.nickname = Utils.firstNonNull(nickname, editUser.getNickname());
        this.firstName = Utils.firstNonNull(firstName, editUser.getFirstName());
        this.lastName = Utils.firstNonNull(lastName, editUser.getLastName());
        this.avatar = Utils.firstNonNull(avatar, editUser.getAvatar());
        this.gender = Utils.compareAndGetLatest(gender, editUser.getGender());
        return this;
    }

    public void addTeam(TeamRecord teamRecord) {
        if (teams == null) {
            teams = new HashSet<>();
        }
        teams.add(teamRecord);
    }

    public boolean isAutoUser() {
        return autoUser;
    }

    public void setAutoUser(boolean autoUser) {
        this.autoUser = autoUser;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Set<TeamRecord> getTeams() {
        return teams;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserRecord that = (UserRecord) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
