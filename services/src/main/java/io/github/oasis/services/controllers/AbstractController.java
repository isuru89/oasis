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

package io.github.oasis.services.controllers;

import io.github.oasis.services.model.TeamProfile;
import io.github.oasis.services.model.UserRole;
import io.github.oasis.services.security.UserPrincipal;
import io.github.oasis.services.services.IProfileService;
import io.github.oasis.services.utils.Commons;

abstract class AbstractController {

    private boolean isCurator(UserPrincipal authUser) {
        return hasRole(authUser, UserRole.ROLE_CURATOR);
    }

    private boolean hasRole(UserPrincipal authUser, String role) {
        if (authUser == null) {
            return false;
        }

        // no roles expecting for a user
        if (role == null) {
            return Commons.isNullOrEmpty(authUser.getAuthorities());
        } else {
            return authUser.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals(role));
        }
    }

    private boolean teamNotInScope(IProfileService profileService, long scopeId, long teamId) throws Exception {
        TeamProfile goingToAdd = profileService.readTeam(teamId);
        return !goingToAdd.getTeamScope().equals(scopeId);
    }

    boolean curatorNotOwnsTeam(IProfileService profileService, UserPrincipal authUser, long teamId) throws Exception {
        if (isCurator(authUser)) {
            return teamNotInScope(profileService, authUser.getTeam().getScopeId(), teamId);
        } else {
            return false;
        }
    }

}
