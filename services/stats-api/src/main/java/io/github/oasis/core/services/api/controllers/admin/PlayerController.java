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

import io.github.oasis.core.model.PlayerObject;
import io.github.oasis.core.model.TeamObject;
import io.github.oasis.core.services.annotations.ForAdmin;
import io.github.oasis.core.services.annotations.ForCurator;
import io.github.oasis.core.services.annotations.ForPlayer;
import io.github.oasis.core.services.api.controllers.AbstractController;
import io.github.oasis.core.services.api.services.PlayerTeamService;
import io.github.oasis.core.services.api.to.PlayerCreateRequest;
import io.github.oasis.core.services.api.to.PlayerGameAssociationRequest;
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
@Tag(name = "Players", description = "Players related APIs")
public class PlayerController extends AbstractController {

    private final PlayerTeamService playerTeamService;

    public PlayerController(PlayerTeamService playerTeamService) {
        this.playerTeamService = playerTeamService;
    }

    @Operation(
            summary = "Register a new player to the system",
            tags = {"admin"}
    )
    @ForAdmin
    @PostMapping("/players")
    public PlayerObject registerPlayer(@RequestBody PlayerCreateRequest user) {
        return playerTeamService.addPlayer(user);
    }

    @Operation(
            summary = "Gets a single player by email"
    )
    @ForPlayer
    @GetMapping("/players")
    public PlayerObject readPlayerProfileByEmail(@RequestParam(name = "email") String email) {
        return playerTeamService.readPlayer(email);
    }

    @Operation(
            summary = "Gets a single player by user id"
    )
    @ForPlayer
    @GetMapping("/players/{playerId}")
    public PlayerObject readPlayerProfile(@PathVariable("playerId") Integer playerId) {
        return playerTeamService.readPlayer(playerId);
    }

    @Operation(
            summary = "Gets all players of a team"
    )
    @ForPlayer
    @GetMapping("/teams/{teamId}/players")
    public List<PlayerObject> browsePlayers(@PathVariable("teamId") Integer teamId) {
        return playerTeamService.listAllUsersInTeam(teamId);
    }

    @Operation(
            summary = "Update player details",
            tags = {"admin", "curator"}
    )
    @ForCurator
    @PutMapping("/players/{playerId}")
    public PlayerObject updatePlayer(@PathVariable("playerId") Integer playerId,
                                     @RequestBody PlayerObject playerObject) {
        return playerTeamService.updatePlayer(playerId, playerObject);
    }

    @Operation(
            summary = "Deactivate a player from the system",
            tags = {"admin", "curator"}
    )
    @ForAdmin
    @DeleteMapping("/players/{playerId}")
    public PlayerObject deactivatePlayer(@PathVariable("playerId") Integer playerId) {
        return playerTeamService.deactivatePlayer(playerId);
    }

    @Operation(
            summary = "Add a new team to the system",
            tags = {"admin", "curator"}
    )
    @ForCurator
    @PostMapping("/teams")
    public TeamObject addTeam(@RequestBody TeamObject team) {
        return playerTeamService.addTeam(team);
    }

    @Operation(
            summary = "Update team details",
            tags = {"admin", "curator"}
    )
    @ForCurator
    @PutMapping("/teams/{teamId}")
    public TeamObject updateTeam(@PathVariable("teamId") Integer teamId,
                                 @RequestBody TeamObject teamObject) {
        return playerTeamService.updateTeam(teamId, teamObject);
    }

    @Operation(
            summary = "Add a player to the provided team",
            tags = {"admin", "curator"}
    )
    @ForCurator
    @PostMapping("/players/{playerId}/teams")
    public void addPlayerToTeam(@PathVariable("playerId") Integer playerId,
                                @RequestBody PlayerGameAssociationRequest request) {
        playerTeamService.addPlayerToTeam(playerId, request.getGameId(), request.getTeamId());
    }

    @Operation(
            summary = "Gets all teams a user has been associated with"
    )
    @ForPlayer
    @GetMapping("/players/{userId}/teams")
    public List<TeamObject> browsePlayerTeams(@PathVariable("playerId") Integer playerId) {
        return playerTeamService.getTeamsOfPlayer(playerId);
    }

    @Operation(
            summary = "Add multiple players at once to a team",
            tags = {"admin", "curator"}
    )
    @ForCurator
    @PostMapping("/teams/{teamId}/players")
    public void addPlayerToTeam(@PathVariable("teamId") Integer teamId,
                                @RequestParam("playerIds") List<Integer> playerId) {
        playerTeamService.addPlayersToTeam(teamId, playerId);
    }
}
