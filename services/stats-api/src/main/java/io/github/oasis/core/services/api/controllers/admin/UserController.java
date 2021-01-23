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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Isuru Weerarathna
 */
@RestController
@RequestMapping(
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
)
@Tag(name = "Users", description = "Users related APIs")
public class UserController extends AbstractController {

    private final UserTeamService userTeamService;

    public UserController(UserTeamService userTeamService) {
        this.userTeamService = userTeamService;
    }

    @Operation(
            summary = "Register a new user to the system",
            tags = {"admin"}
    )
    @ForAdmin
    @PostMapping("/admin/users")
    public UserObject registerUser(@RequestBody UserCreateRequest user) {
        return userTeamService.addUser(user);
    }

    @Operation(
            summary = "Gets a single user by email"
    )
    @ForPlayer
    @GetMapping("/admin/users")
    public UserObject readUserProfileByEmail(@RequestParam(name = "email") String email) {
        return userTeamService.readUser(email);
    }

    @Operation(
            summary = "Gets a single user by user id"
    )
    @ForPlayer
    @GetMapping("/admin/users/{userId}")
    public UserObject readUserProfile(@PathVariable("userId") Integer userId) {
        return userTeamService.readUser(userId);
    }

    @Operation(
            summary = "Gets all users of a team"
    )
    @ForPlayer
    @GetMapping("/admin/teams/{teamId}/users")
    public List<UserObject> browseUsers(@PathVariable("teamId") Integer teamId) {
        return userTeamService.listAllUsersInTeam(teamId);
    }

    @Operation(
            summary = "Update user details",
            tags = {"admin", "curator"}
    )
    @ForCurator
    @PutMapping("/admin/users/{userId}")
    public UserObject updateUser(@PathVariable("userId") Integer userId,
                                 @RequestBody UserObject userObject) {
        return userTeamService.updateUser(userId, userObject);
    }

    @Operation(
            summary = "Deactivate a user from the system",
            tags = {"admin", "curator"}
    )
    @ForAdmin
    @DeleteMapping("/admin/users/{userId}")
    public UserObject deactivateUser(@PathVariable("userId") Integer userId) {
        return userTeamService.deactivateUser(userId);
    }

    @Operation(
            summary = "Add a new team to the system",
            tags = {"admin", "curator"}
    )
    @ForCurator
    @PostMapping("/admin/teams")
    public TeamObject addTeam(@RequestBody TeamObject team) {
        return userTeamService.addTeam(team);
    }

    @Operation(
            summary = "Update team details",
            tags = {"admin", "curator"}
    )
    @ForCurator
    @PutMapping("/admin/teams/{teamId}")
    public TeamObject updateTeam(@PathVariable("teamId") Integer teamId,
                                 @RequestBody TeamObject teamObject) {
        return userTeamService.updateTeam(teamId, teamObject);
    }

    @Operation(
            summary = "Add a user to the provided team",
            tags = {"admin", "curator"}
    )
    @ForCurator
    @PostMapping("/admin/users/{userId}/teams")
    public void addUserToTeam(@PathVariable("userId") Integer userId,
                              @RequestBody UserGameAssociationRequest request) {
        userTeamService.addUserToTeam(userId, request.getGameId(), request.getTeamId());
    }

    @Operation(
            summary = "Gets all teams a user has been associated with"
    )
    @ForPlayer
    @GetMapping("/admin/users/{userId}/teams")
    public List<TeamObject> browseUserTeams(@PathVariable("userId") Integer userId) {
        return userTeamService.getUserTeams(userId);
    }

    @Operation(
            summary = "Add multiple users at once to a team",
            tags = {"admin", "curator"}
    )
    @ForCurator
    @PostMapping("/admin/teams/{teamId}/users")
    public void addUsersToTeam(@PathVariable("teamId") Integer teamId,
                               @RequestParam("userIds") List<Integer> userIds) {
        userTeamService.addUsersToTeam(teamId, userIds);
    }
}
