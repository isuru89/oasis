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

package io.github.oasis.core.services.api.services;

import io.github.oasis.core.Game;
import io.github.oasis.core.external.PaginatedResult;
import io.github.oasis.core.external.messages.GameState;
import io.github.oasis.core.services.api.beans.BackendRepository;
import io.github.oasis.core.services.api.exceptions.DataValidationException;
import io.github.oasis.core.services.api.exceptions.ErrorCodes;
import io.github.oasis.core.services.api.to.GameCreateRequest;
import io.github.oasis.core.services.api.to.GameUpdateRequest;
import io.github.oasis.core.services.exceptions.OasisApiException;
import io.github.oasis.core.utils.Texts;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;

/**
 * @author Isuru Weerarathna
 */
@Service
public class GameService extends AbstractOasisService {

    private final Map<String, GameState> availableStatuses = Map.of(
            "start", GameState.STARTED,
            "started", GameState.STARTED,
            "pause", GameState.PAUSED,
            "paused", GameState.PAUSED,
            "stop", GameState.STOPPED,
            "stopped", GameState.STOPPED);

    private final IEngineManager engineManager;

    public GameService(BackendRepository backendRepository, IEngineManager engineManager) {
        super(backendRepository);
        this.engineManager = engineManager;
    }

    public Game addGame(GameCreateRequest gameCreateRequest) throws OasisApiException {
        Game game = gameCreateRequest.createGame();

        validateGameObjectForCreation(game);

        game.setCurrentStatus(GameState.CREATED.name());
        return backendRepository.addNewGame(game);
    }

    public Game updateGame(int gameId, GameUpdateRequest updateRequest) throws OasisApiException {
        Game dbGame = backendRepository.readGame(gameId);
        Game updatingGame = dbGame.toBuilder()
                .motto(StringUtils.defaultIfEmpty(updateRequest.getMotto(), dbGame.getMotto()))
                .logoRef(ObjectUtils.defaultIfNull(updateRequest.getLogoRef(), dbGame.getLogoRef()))
                .description(StringUtils.defaultIfEmpty(updateRequest.getDescription(), dbGame.getDescription()))
                .build();

        return backendRepository.updateGame(gameId, updatingGame);
    }

    public Game deleteGame(int gameId) {
        return backendRepository.deleteGame(gameId);
    }

    public Game readGame(int gameId) {
        return backendRepository.readGame(gameId);
    }

    public PaginatedResult<Game> listAllGames(String offset, int pageSize) {
        return backendRepository.listGames(offset, pageSize);
    }

    public Game getGameByName(String name) {
        return backendRepository.readGameByName(name);
    }

    public Game changeStatusOfGame(int gameId, String newStatus, long updatedAt) throws OasisApiException {
        GameState gameState = validateGameState(newStatus);

        Game game = backendRepository.readGame(gameId);
        if (game == null) {
            throw new OasisApiException(ErrorCodes.GAME_NOT_EXISTS, HttpStatus.NOT_FOUND.value(), "No game is found by id " + gameId);
        }
        Game updatedGame = backendRepository.updateGameStatus(gameId, gameState.name(), updatedAt);
        engineManager.changeGameStatus(gameState, updatedGame);
        return updatedGame;
    }

    private GameState validateGameState(String status) throws OasisApiException {
        if (Objects.isNull(status)) {
            throw new OasisApiException(ErrorCodes.GAME_UNKNOWN_STATE,
                    HttpStatus.BAD_REQUEST.value(), "Unknown game state!");
        }

        GameState gameState = availableStatuses.get(status.toLowerCase());
        if (Objects.isNull(gameState)) {
            throw new OasisApiException(ErrorCodes.GAME_UNKNOWN_STATE,
                    HttpStatus.BAD_REQUEST.value(), "Unknown game state!");
        }
        return gameState;
    }

    private void validateGameObjectForCreation(Game game) throws OasisApiException {
        if (Texts.isEmpty(game.getName())) {
            throw new DataValidationException(ErrorCodes.GAME_ID_SHOULD_NOT_SPECIFIED);
        }
        validateCommonGameAttributes(game);
    }

    private void validateCommonGameAttributes(Game game) throws OasisApiException {
        if (Texts.isNotEmpty(game.getMotto()) && game.getMotto().length() > 64) {
            throw new DataValidationException(ErrorCodes.GAME_EXCEEDED_MOTTO_LEN);
        } else if (Texts.isNotEmpty(game.getDescription()) && game.getDescription().length() > 255) {
            throw new DataValidationException(ErrorCodes.GAME_EXCEEDED_DESC_LEND);
        }
    }
}
