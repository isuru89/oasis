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

import io.github.oasis.core.Game;
import io.github.oasis.core.exception.OasisException;
import io.github.oasis.core.external.PaginatedResult;
import io.github.oasis.core.model.GameStatus;
import io.github.oasis.core.services.annotations.ForAdmin;
import io.github.oasis.core.services.annotations.ForPlayer;
import io.github.oasis.core.services.api.controllers.AbstractController;
import io.github.oasis.core.services.api.services.IGameService;
import io.github.oasis.core.services.api.to.GameCreateRequest;
import io.github.oasis.core.services.api.to.GameUpdateRequest;
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
@Tag(name = "Games", description = "Game API")
public class GamesController extends AbstractController {

    private final IGameService gameService;

    public GamesController(IGameService gameService) {
        this.gameService = gameService;
    }

    @Operation(
            summary = "Creates a new game",
            tags = {"admin"}
    )
    @ForAdmin
    @PostMapping(path = "/games", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Game addGame(@Valid @RequestBody GameCreateRequest request) throws OasisException {
        return gameService.addGame(request);
    }

    @Operation(
            summary = "List all available games"
    )
    @ForPlayer
    @GetMapping(path = "/games")
    public PaginatedResult<Game> listGames(@RequestParam(name = "page", defaultValue = "0") String page,
                                           @RequestParam(name = "pageSize", defaultValue = "20") Integer pageSize) {
        return gameService.listAllGames(page, pageSize);
    }

    @Operation(
            summary = "Check existence of game by name"
    )
    @ForPlayer
    @GetMapping(path = "/games/search")
    public Game getGameByName(@RequestParam(name = "name") String gameName) {
        return gameService.getGameByName(gameName);
    }

    @Operation(
            summary = "Reads the information about a game"
    )
    @ForPlayer
    @GetMapping(path = "/games/{gameId}")
    public Game readGame(@PathVariable("gameId") Integer gameId) {
        return gameService.readGame(gameId);
    }

    @Operation(
            summary = "Updates information about existing game",
            tags = {"admin"}
    )
    @ForAdmin
    @PatchMapping(path = "/games/{gameId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Game updateGame(@PathVariable("gameId") Integer gameId,
                           @Valid @RequestBody GameUpdateRequest request) throws OasisException {
        return gameService.updateGame(gameId, request);
    }

    @Operation(
            summary = "Deletes an existing game",
            description = "When a game is deleted, all of its associated elements, event sources will be deleted as well.",
            tags = {"admin"}
    )
    @ForAdmin
    @DeleteMapping(path = "/games/{gameId}")
    public Game deleteGame(@PathVariable("gameId") Integer gameId) throws OasisApiException {
        return gameService.deleteGame(gameId);
    }

    @Operation(
            summary = "Gets the current status of game",
            tags = {"player"}
    )
    @ForPlayer
    @GetMapping(path = "/games/{gameId}/status")
    public GameStatus readGameCurrentStatus(@PathVariable("gameId") Integer gameId) {
        return gameService.getCurrentGameStatus(gameId);
    }

    @Operation(
            summary = "Gets the status change history of a game",
            tags = {"player"}
    )
    @ForPlayer
    @GetMapping(path = "/games/{gameId}/status/history")
    public List<GameStatus> readGameStatusHistory(@PathVariable("gameId") Integer gameId,
                                                  @RequestParam(value = "startFrom", required = false) Long startFrom,
                                                  @RequestParam(value = "endTo", required = false) Long endTo) {
        return gameService.listGameStatusHistory(gameId, startFrom, endTo);
    }

    @Operation(
            summary = "Updates the status of game",
            tags = {"admin"}
    )
    @ForAdmin
    @PutMapping(path = "/games/{gameId}/{status}")
    public Game updateGameStatus(@PathVariable("gameId") Integer gameId,
                                 @PathVariable("status") String status) throws OasisException {
        return gameService.changeStatusOfGame(gameId, status, System.currentTimeMillis());
    }

}
