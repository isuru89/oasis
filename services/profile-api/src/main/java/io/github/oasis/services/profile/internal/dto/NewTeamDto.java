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

import io.github.oasis.services.common.OasisValidationException;
import io.github.oasis.services.common.Validation;
import io.github.oasis.services.profile.internal.ErrorCodes;

import java.time.Instant;

/**
 * @author Isuru Weerarathna
 */
public class NewTeamDto {

    private String name;
    private String motto;
    private String avatar;
    private Instant createdAt;

    public NewTeamDto() {
        createdAt = Instant.now();
    }

    public NewTeamDto validate() {
        if (Validation.isEmpty(name)) {
            throw new OasisValidationException(ErrorCodes.INVALID_NEW_TEAM,
                    "Team name cannot be empty!");
        }
        return this;
    }

    public NewUserDto createAssociatedUser() {
        NewUserDto teamUser = new NewUserDto();
        teamUser.setAutoUser(true);
        teamUser.setAvatar(avatar);
        teamUser.setCreatedAt(createdAt);
        teamUser.setEmail("team@");
        return teamUser;
    }

    public void fixName() {
        this.name = this.name.trim();
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

    public Instant getCreatedAt() {
        return createdAt;
    }
}
