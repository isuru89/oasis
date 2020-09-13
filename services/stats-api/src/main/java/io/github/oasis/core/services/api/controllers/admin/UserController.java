/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.github.oasis.core.services.api.controllers.admin;

import io.github.oasis.core.model.TeamObject;
import io.github.oasis.core.model.UserObject;
import io.github.oasis.core.services.annotations.ForAdmin;
import io.github.oasis.core.services.annotations.ForCurator;
import io.github.oasis.core.services.annotations.ForPlayer;
import io.github.oasis.core.services.api.controllers.AbstractController;
import io.github.oasis.core.services.api.services.UserTeamService;
import io.github.oasis.core.services.api.to.UserCreateRequest;
import io.github.oasis.core.services.api.to.UserGameAssociationRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Isuru Weerarathna
 */
@RestController
@RequestMapping(
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
)
public class UserController extends AbstractController {

    private final UserTeamService userTeamService;

    public UserController(UserTeamService userTeamService) {
        this.userTeamService = userTeamService;
    }

    @ForAdmin
    @PostMapping("/admin/users")
    public UserObject registerUser(@RequestBody UserCreateRequest user) {
        return userTeamService.addUser(user);
    }

    @ForPlayer
    @GetMapping("/admin/users")
    public UserObject readUserProfileByEmail(@RequestParam(name = "email") String email) {
        return userTeamService.readUser(email);
    }

    @ForPlayer
    @GetMapping("/admin/users/{userId}")
    public UserObject readUserProfile(@PathVariable("userId") Integer userId) {
        return userTeamService.readUser(userId);
    }

    @ForPlayer
    @GetMapping("/admin/teams/{teamId}/users")
    public List<UserObject> browseUsers(@PathVariable("teamId") Integer teamId) {
        return userTeamService.listAllUsersInTeam(teamId);
    }

    @ForCurator
    @PutMapping("/admin/users/{userId}")
    public UserObject updateUser(@PathVariable("userId") Integer userId,
                                 @RequestBody UserObject userObject) {
        return userTeamService.updateUser(userId, userObject);
    }

    @ForAdmin
    @DeleteMapping("/admin/users/{userId}")
    public UserObject deactivateUser(@PathVariable("userId") Integer userId) {
        return userTeamService.deactivateUser(userId);
    }

    @ForCurator
    @PostMapping("/admin/teams")
    public TeamObject addTeam(@RequestBody TeamObject team) {
        return userTeamService.addTeam(team);
    }

    @ForCurator
    @PutMapping("/admin/teams/{teamId}")
    public TeamObject updateTeam(@PathVariable("teamId") Integer teamId,
                                 @RequestBody TeamObject teamObject) {
        return userTeamService.updateTeam(teamId, teamObject);
    }

    @ForCurator
    @PostMapping("/admin/users/{userId}/teams")
    public void addUserToTeam(@PathVariable("userId") Integer userId,
                              @RequestBody UserGameAssociationRequest request) {
        userTeamService.addUserToTeam(userId, request.getGameId(), request.getTeamId());
    }

    @ForPlayer
    @GetMapping("/admin/users/{userId}/teams")
    public List<TeamObject> browseUserTeams(@PathVariable("userId") Integer userId) {
        return userTeamService.getUserTeams(userId);
    }

    @ForCurator
    @PostMapping("/admin/teams/{teamId}/users")
    public void addUsersToTeam(@PathVariable("teamId") Integer teamId,
                               @RequestParam("userIds") List<Integer> userIds) {
        userTeamService.addUsersToTeam(teamId, userIds);
    }
}
