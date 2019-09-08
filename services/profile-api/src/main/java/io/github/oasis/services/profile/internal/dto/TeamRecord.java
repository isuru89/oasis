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
public class TeamRecord {

    private int id;
    private String name;
    private String motto;
    private String avatar;

    private boolean active;

    private Set<UserRecord> users;

    public TeamRecord mergeChanges(EditTeamDto editTeam) {
        this.name = Utils.firstNonNull(editTeam.getName(), name);
        this.motto = Utils.firstNonNull(editTeam.getMotto(), motto);
        this.avatar = Utils.firstNonNull(editTeam.getAvatar(), avatar);
        return this;
    }

    public void addUser(UserRecord userRecord) {
        if (users == null) {
            users = new HashSet<>();
        }
        users.add(userRecord);
    }

    public Set<UserRecord> getUsers() {
        return users;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMotto() {
        return motto;
    }

    public void setMotto(String motto) {
        this.motto = motto;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TeamRecord that = (TeamRecord) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
