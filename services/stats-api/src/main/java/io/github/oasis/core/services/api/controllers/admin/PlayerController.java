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
import io.github.oasis.core.services.api.services.PlayerTeamService;
import io.github.oasis.core.services.api.to.PlayerCreateRequest;
import io.github.oasis.core.services.api.to.PlayerGameAssociationRequest;
import io.github.oasis.core.services.api.to.PlayerUpdateRequest;
import io.github.oasis.core.services.api.to.TeamCreateRequest;
import io.github.oasis.core.services.api.to.TeamUpdateRequest;
import io.github.oasis.core.services.exceptions.OasisApiException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
    @PostMapping(value = "/players", consumes = MediaType.APPLICATION_JSON_VALUE)
    public PlayerObject registerPlayer(@RequestBody PlayerCreateRequest user) {
        return playerTeamService.addPlayer(user);
    }

    @Operation(
            summary = "Gets a player by email"
    )
    @ForPlayer
    @GetMapping("/players")
    public PlayerObject readPlayerProfileByEmail(@RequestParam(name = "email") String email) {
        return playerTeamService.readPlayer(email);
    }

    @Operation(
            summary = "Gets a player by user id"
    )
    @ForPlayer
    @GetMapping("/players/{playerId}")
    public PlayerObject readPlayerProfile(@PathVariable("playerId") Long playerId) {
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
    @PatchMapping(value = "/players/{playerId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public PlayerObject updatePlayer(@PathVariable("playerId") Long playerId,
                                     @RequestBody PlayerUpdateRequest updateRequest) {
        return playerTeamService.updatePlayer(playerId, updateRequest);
    }

    @Operation(
            summary = "Deactivate a player from the system",
            tags = {"admin", "curator"}
    )
    @ForAdmin
    @DeleteMapping("/players/{playerId}")
    public PlayerObject deactivatePlayer(@PathVariable("playerId") Long playerId) {
        return playerTeamService.deactivatePlayer(playerId);
    }

    @Operation(
            summary = "Add a new team to the system",
            tags = {"admin", "curator"}
    )
    @ForCurator
    @PostMapping(value = "/teams", consumes = MediaType.APPLICATION_JSON_VALUE)
    public TeamObject addTeam(@RequestBody TeamCreateRequest request) {
        return playerTeamService.addTeam(request);
    }

    @Operation(
            summary = "Gets a single team by team id"
    )
    @ForPlayer
    @GetMapping("/teams/{teamId}")
    public TeamObject readTeamInfo(@PathVariable("teamId") Integer teamId) throws OasisApiException {
        return playerTeamService.readTeam(teamId);
    }

    @Operation(
            summary = "Gets a single team information by its name"
    )
    @ForPlayer
    @GetMapping("/teams")
    public TeamObject readTeamInfoByName(@RequestParam(name = "name") String name) throws OasisApiException {
        return playerTeamService.readTeam(name);
    }

    @Operation(
            summary = "Search teams by its prefix"
    )
    @ForPlayer
    @GetMapping("/teams/search")
    public PaginatedResult<TeamMetadata> searchTeams(@RequestParam(name = "name") String teamPrefix,
                                                     @RequestParam(name = "offset") String offset,
                                                     @RequestParam(name = "pageSize") Integer pageSize) {
        return playerTeamService.searchTeam(teamPrefix, offset, pageSize);
    }

    @Operation(
            summary = "Update team details",
            tags = {"admin", "curator"}
    )
    @ForCurator
    @PatchMapping(value = "/teams/{teamId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public TeamObject updateTeam(@PathVariable("teamId") Integer teamId,
                                 @RequestBody TeamUpdateRequest request) {
        return playerTeamService.updateTeam(teamId, request);
    }

    @Operation(
            summary = "Add a player to the provided team",
            tags = {"admin", "curator"}
    )
    @ForCurator
    @PostMapping(value = "/players/{playerId}/teams", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void addPlayerToTeam(@PathVariable("playerId") Long playerId,
                                @RequestBody PlayerGameAssociationRequest request) {
        playerTeamService.addPlayerToTeam(playerId, request.getGameId(), request.getTeamId());
    }

    @Operation(
            summary = "Remove a player from the team",
            tags = {"admin", "curator"}
    )
    @ForCurator
    @DeleteMapping("/teams/{teamId}/players/{playerId}")
    public void removePlayerFromTeam(@PathVariable("playerId") Long playerId,
                                    @PathVariable("teamId") Integer teamId) {
        playerTeamService.removePlayerFromTeam(playerId, teamId);
    }

    @Operation(
            summary = "Gets all teams a user has been associated with"
    )
    @ForPlayer
    @GetMapping("/players/{playerId}/teams")
    public List<TeamObject> browsePlayerTeams(@PathVariable("playerId") Long playerId) {
        return playerTeamService.getTeamsOfPlayer(playerId);
    }

    @Operation(
            summary = "Add multiple players at once to a team",
            tags = {"admin", "curator"}
    )
    @ForCurator
    @PostMapping(value = "/teams/{teamId}/players", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void addPlayersToTeam(@PathVariable("teamId") Integer teamId,
                                @RequestParam("playerIds") List<Long> playerId) {
        playerTeamService.addPlayersToTeam(teamId, playerId);
    }
}
