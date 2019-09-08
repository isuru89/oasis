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

package io.github.oasis.services.profile.internal;

import com.github.slugify.Slugify;
import io.github.oasis.services.profile.domain.Gender;
import io.github.oasis.services.profile.domain.UserStatus;
import io.github.oasis.services.profile.internal.dto.NewTeamDto;
import io.github.oasis.services.profile.internal.dto.NewUserDto;

/**
 * @author Isuru Weerarathna
 */
public class OasisTeamUserGenerator implements ITeamUserGenerator {

    public static final OasisTeamUserGenerator INSTANCE = new OasisTeamUserGenerator();

    private static final Slugify SLUGIFY = new Slugify().withLowerCase(true);
    private static final String TEAM = "Team";

    private OasisTeamUserGenerator() {}

    @Override
    public NewUserDto createTeamUser(NewTeamDto newTeamInfo) {
        NewUserDto user = new NewUserDto();
        String normalizedName = SLUGIFY.slugify(newTeamInfo.getName());
        String email = String.format("team@%s.oasis.io", normalizedName);
        user.setEmail(email);
        user.setFirstName(TEAM);
        user.setLastName(newTeamInfo.getName());
        user.setNickname(newTeamInfo.getName());
        user.setAutoUser(true);
        user.setAvatar(newTeamInfo.getAvatar());
        user.setGender(Gender.UNKNOWN.getId());
        user.setCreatedAt(newTeamInfo.getCreatedAt());
        user.setStatus(UserStatus.VERIFIED.getId());
        return user;
    }
}
