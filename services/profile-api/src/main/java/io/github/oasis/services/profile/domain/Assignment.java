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

import io.github.oasis.services.profile.internal.dao.ITeamDao;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * @author Isuru Weerarathna
 */
@Component
public class Assignment {

    private ITeamDao team;

    public Assignment(ITeamDao team) {
        this.team = team;
    }

    public boolean assignToTeam(int userId, int teamId) {
        return isSuccessful(team.addUserToTeam(userId, teamId, Instant.now()));
    }

    public boolean removeFromTeam(int userId, int teamId) {
        return isSuccessful(team.removeUserFromTeam(userId, teamId));
    }

    private boolean isSuccessful(int result) {
        return result > 0;
    }

}
