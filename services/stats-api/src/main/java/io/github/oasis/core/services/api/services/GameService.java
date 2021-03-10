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
import io.github.oasis.core.elements.AttributeInfo;
import io.github.oasis.core.external.PaginatedResult;
import io.github.oasis.core.external.messages.GameState;
import io.github.oasis.core.services.api.beans.BackendRepository;
import io.github.oasis.core.services.api.exceptions.DataValidationException;
import io.github.oasis.core.services.api.exceptions.ErrorCodes;
import io.github.oasis.core.services.api.to.GameObjectRequest;
import io.github.oasis.core.services.exceptions.OasisApiException;
import io.github.oasis.core.utils.Texts;
import io.github.oasis.core.utils.Utils;
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
            "pause", GameState.PAUSED,
            "stop", GameState.STOPPED);

    private final IEngineManager engineManager;

    public GameService(BackendRepository backendRepository, IEngineManager engineManager) {
        super(backendRepository);
        this.engineManager = engineManager;
    }

    public Game addGame(GameObjectRequest gameObjectRequest) throws OasisApiException {
        Game game = gameObjectRequest.createGame();

        validateGameObjectForCreation(game);

        Game addedGame = backendRepository.addNewGame(game);

        // add attributes associated with game
        if (Utils.isNotEmpty(gameObjectRequest.getAttributes())) {
            for (AttributeInfo attribute : gameObjectRequest.getAttributes()) {
                backendRepository.addAttribute(addedGame.getId(), attribute);
            }
        }
        return addedGame;
    }

    public Game updateGame(int gameId, Game game) throws OasisApiException {
        validateGameObjectForEdit(game);

        return backendRepository.updateGame(gameId, game);
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

    public Game changeStatusOfGame(int gameId, String newStatus) throws OasisApiException {
        GameState gameState = validateGameState(newStatus);

        Game game = backendRepository.readGame(gameId);
        engineManager.changeGameStatus(gameState, game);
        return game;
    }

    private GameState validateGameState(String status) throws OasisApiException {
        if (Objects.isNull(status)) {
            throw new OasisApiException(ErrorCodes.GAME_UNKNOWN_STATE,
                    HttpStatus.BAD_REQUEST.value(), "Unknown game state!");
        }

        GameState gameState = availableStatuses.get(status);
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

    private void validateGameObjectForEdit(Game game) throws OasisApiException {
        if (Objects.isNull(game.getId())) {
            throw new DataValidationException(ErrorCodes.GAME_ID_NOT_SPECIFIED);
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
