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

import io.github.oasis.core.TeamMetadata;
import io.github.oasis.core.external.PaginatedResult;
import io.github.oasis.core.model.PlayerObject;
import io.github.oasis.core.model.TeamObject;
import io.github.oasis.core.services.annotations.ForAdmin;
import io.github.oasis.core.services.annotations.ForCurator;
import io.github.oasis.core.services.annotations.ForPlayer;
import io.github.oasis.core.services.api.controllers.AbstractController;
import io.github.oasis.core.services.api.services.IPlayerAssignmentService;
import io.github.oasis.core.services.api.services.IPlayerManagementService;
import io.github.oasis.core.services.api.services.ITeamManagementService;
import io.github.oasis.core.services.api.to.*;
import io.github.oasis.core.services.exceptions.OasisApiException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Isuru Weerarathna
 */
@RestController
@RequestMapping(
        produces = MediaType.APPLICATION_JSON_VALUE
)
@Tag(name = "Players and Teams", description = "Players and Team management related APIs")
public class PlayerController extends AbstractController {

    private final IPlayerManagementService playerManagementService;
    private final IPlayerAssignmentService playerAssignmentService;
    private final ITeamManagementService teamManagementService;

    public PlayerController(IPlayerManagementService playerManagementService,
                            IPlayerAssignmentService assignmentService,
                            ITeamManagementService teamManagementService) {
        this.playerAssignmentService = assignmentService;
        this.playerManagementService = playerManagementService;
        this.teamManagementService = teamManagementService;
    }

    @Operation(
            summary = "Register a new player to the system",
            tags = {"admin"}
    )
    @ForAdmin
    @PostMapping(value = "/players", consumes = MediaType.APPLICATION_JSON_VALUE)
    public PlayerObject registerPlayer(@Valid @RequestBody PlayerCreateRequest user) {
        return playerManagementService.addPlayer(user);
    }

    @Operation(
            summary = "Gets a player by email"
    )
    @ForPlayer
    @GetMapping("/players")
    public PlayerObject readPlayerProfileByEmail(@RequestParam(name = "email") String email,
                                                 @RequestParam(name = "verbose", required = false, defaultValue = "false") Boolean verbose) {
        return playerManagementService.readPlayerByEmail(email, verbose);
    }

    @Operation(
            summary = "Gets a player by user id"
    )
    @ForPlayer
    @GetMapping("/players/{playerId}")
    public PlayerObject readPlayerProfile(@PathVariable("playerId") Long playerId) {
        return playerManagementService.readPlayer(playerId);
    }

    @Operation(
            summary = "Gets all players of a team"
    )
    @ForPlayer
    @GetMapping("/teams/{teamId}/players")
    public List<PlayerObject> browsePlayers(@PathVariable("teamId") Integer teamId) {
        return playerAssignmentService.listAllUsersInTeam(teamId);
    }

    @Operation(
            summary = "Update player details",
            tags = {"admin", "curator"}
    )
    @ForCurator
    @PatchMapping(value = "/players/{playerId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public PlayerObject updatePlayer(@PathVariable("playerId") Long playerId,
                                     @Valid @RequestBody PlayerUpdateRequest updateRequest) {
        return playerManagementService.updatePlayer(playerId, updateRequest);
    }

    @Operation(
            summary = "Deactivate a player from the system",
            tags = {"admin", "curator"}
    )
    @ForAdmin
    @DeleteMapping("/players/{playerId}")
    public PlayerObject deactivatePlayer(@PathVariable("playerId") Long playerId) {
        return playerManagementService.deactivatePlayer(playerId);
    }

    @Operation(
            summary = "Add a new team to the system",
            tags = {"admin", "curator"}
    )
    @ForCurator
    @PostMapping(value = "/teams", consumes = MediaType.APPLICATION_JSON_VALUE)
    public TeamObject addTeam(@Valid @RequestBody TeamCreateRequest request) {
        return teamManagementService.addTeam(request);
    }

    @Operation(
            summary = "Gets a single team by team id"
    )
    @ForPlayer
    @GetMapping("/teams/{teamId}")
    public TeamObject readTeamInfo(@PathVariable("teamId") Integer teamId) throws OasisApiException {
        return teamManagementService.readTeam(teamId);
    }

    @Operation(
            summary = "Gets a single team information by its name"
    )
    @ForPlayer
    @GetMapping("/teams")
    public TeamObject readTeamInfoByName(@RequestParam(name = "name") String name) throws OasisApiException {
        return teamManagementService.readTeamByName(name);
    }

    @Operation(
            summary = "Search teams by its prefix"
    )
    @ForPlayer
    @GetMapping("/teams/search")
    public PaginatedResult<TeamMetadata> searchTeams(@RequestParam(name = "name") String teamPrefix,
                                                     @RequestParam(name = "offset") String offset,
                                                     @RequestParam(name = "pageSize") Integer pageSize) {
        return teamManagementService.searchTeam(teamPrefix, offset, pageSize);
    }

    @Operation(
            summary = "Update team details",
            tags = {"admin", "curator"}
    )
    @ForCurator
    @PatchMapping(value = "/teams/{teamId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public TeamObject updateTeam(@PathVariable("teamId") Integer teamId,
                                 @Valid @RequestBody TeamUpdateRequest request) {
        return teamManagementService.updateTeam(teamId, request);
    }

    @Operation(
            summary = "Add a player to the provided team",
            tags = {"admin", "curator"}
    )
    @ForCurator
    @PostMapping(value = "/players/{playerId}/teams/{teamId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void addPlayerToTeam(@PathVariable("playerId") Long playerId,
                                @PathVariable("teamId") Integer teamId) {
        playerAssignmentService.addPlayerToTeam(playerId, teamId);
    }

    @Operation(
            summary = "Remove a player from the team",
            tags = {"admin", "curator"}
    )
    @ForCurator
    @DeleteMapping("/teams/{teamId}/players/{playerId}")
    public void removePlayerFromTeam(@PathVariable("playerId") Long playerId,
                                    @PathVariable("teamId") Integer teamId) {
        playerAssignmentService.removePlayerFromTeam(playerId, teamId);
    }

    @Operation(
            summary = "Gets all teams a user has been associated with"
    )
    @ForPlayer
    @GetMapping("/players/{playerId}/teams")
    public List<TeamObject> browsePlayerTeams(@PathVariable("playerId") Long playerId) {
        return playerAssignmentService.getTeamsOfPlayer(playerId);
    }

    @Operation(
            summary = "Add multiple players at once to a team",
            tags = {"admin", "curator"}
    )
    @ForCurator
    @PostMapping(value = "/teams/{teamId}/players", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void addPlayersToTeam(@PathVariable("teamId") Integer teamId,
                                @RequestParam("playerIds") List<Long> playerId) {
        playerAssignmentService.addPlayersToTeam(teamId, playerId);
    }
}
