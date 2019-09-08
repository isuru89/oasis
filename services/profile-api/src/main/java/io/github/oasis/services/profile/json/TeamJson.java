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

import io.github.oasis.services.profile.internal.dto.TeamRecord;
import io.github.oasis.services.profile.internal.dto.UserRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Isuru Weerarathna
 */
public class TeamJson {

    private int id;
    private String name;
    private String motto;
    private String avatar;

    private boolean active;
    private List<UserRecord> users;

    public static TeamJson from(TeamRecord record) {
        TeamJson team = new TeamJson();
        team.name = record.getName();
        team.motto = record.getMotto();
        team.avatar = record.getAvatar();
        team.active = record.isActive();
        team.id = record.getId();
        if (Objects.isNull(record.getUsers())) {
            team.users = null;
        } else {
            team.users = new ArrayList<>(record.getUsers());
        }
        return team;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getMotto() {
        return motto;
    }

    public String getAvatar() {
        return avatar;
    }

    public boolean isActive() {
        return active;
    }

    public List<UserRecord> getUsers() {
        return users;
    }
}
