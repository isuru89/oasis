/*
 *
 *  * Licensed to the Apache Software Foundation (ASF) under one
 *  * or more contributor license agreements.  See the NOTICE file
 *  * distributed with this work for additional information
 *  * regarding copyright ownership.  The ASF licenses this file
 *  * to you under the Apache License, Version 2.0 (the
 *  * "License"); you may not use this file except in compliance
 *  * with the License.  You may obtain a copy of the License at
 *  *
 *  *    http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing,
 *  * software distributed under the License is distributed on an
 *  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  * KIND, either express or implied.  See the License for the
 *  * specific language governing permissions and limitations
 *  * under the License.
 *
 *
 */

package io.github.oasis.core.services.api.services.impl;

import io.github.oasis.core.Game;
import io.github.oasis.core.external.OasisRepository;
import io.github.oasis.core.external.PaginatedResult;
import io.github.oasis.core.external.messages.GameState;
import io.github.oasis.core.model.GameStatus;
import io.github.oasis.core.services.annotations.AdminDbRepository;
import io.github.oasis.core.services.api.exceptions.DataValidationException;
import io.github.oasis.core.services.api.exceptions.ErrorCodes;
import io.github.oasis.core.services.api.services.IGameService;
import io.github.oasis.core.services.api.to.GameCreateRequest;
import io.github.oasis.core.services.api.to.GameUpdateRequest;
import io.github.oasis.core.services.events.GameStatusChangeEvent;
import io.github.oasis.core.services.exceptions.OasisApiException;
import io.github.oasis.core.utils.Numbers;
import io.github.oasis.core.utils.Texts;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Isuru Weerarathna
 */
@Service
public class GameService extends AbstractOasisService implements IGameService {

    private static final long FAR_FUTURE_EPOCH = LocalDate.of(3001, 1, 1).atStartOfDay()
            .toInstant(ZoneOffset.UTC).toEpochMilli();

    private final Map<String, GameState> availableStatuses = Map.of(
            "create", GameState.CREATED,
            "created", GameState.CREATED,
            "start", GameState.STARTED,
            "started", GameState.STARTED,
            "pause", GameState.PAUSED,
            "paused", GameState.PAUSED,
            "stop", GameState.STOPPED,
            "stopped", GameState.STOPPED);

    private ApplicationEventPublisher publisher;

    public GameService(@AdminDbRepository OasisRepository backendRepository, ApplicationEventPublisher eventPublisher) {
        super(backendRepository);
        this.publisher = eventPublisher;
    }

    @Override
    @Transactional
    public Game addGame(GameCreateRequest gameCreateRequest) throws OasisApiException {
        Game game = gameCreateRequest.createGame();

        validateGameObjectForCreation(game);

        Game dbGame = backendRepository.addNewGame(game);
        backendRepository.updateGameStatus(dbGame.getId(), GameState.CREATED.name(), System.currentTimeMillis());
        return dbGame;
    }

    @Override
    public Game updateGame(int gameId, GameUpdateRequest updateRequest) {
        Game dbGame = backendRepository.readGame(gameId);
        Game updatingGame = dbGame.toBuilder()
                .motto(StringUtils.defaultIfEmpty(updateRequest.getMotto(), dbGame.getMotto()))
                .logoRef(ObjectUtils.defaultIfNull(updateRequest.getLogoRef(), dbGame.getLogoRef()))
                .description(StringUtils.defaultIfEmpty(updateRequest.getDescription(), dbGame.getDescription()))
                .version(updateRequest.getVersion())
                .build();

        return backendRepository.updateGame(gameId, updatingGame);
    }

    @Override
    public Game deleteGame(int gameId) throws OasisApiException {
        changeStatusOfGame(gameId, GameState.STOPPED.name(), System.currentTimeMillis());

        // deleting game elements
        backendRepository.readElementsByGameId(gameId)
                .forEach(def -> backendRepository.deleteElement(gameId, def.getElementId()));

        // deleting event sources
        backendRepository.listAllEventSourcesOfGame(gameId)
                .forEach(eventSource -> backendRepository.removeEventSourceFromGame(eventSource.getId(), gameId));

        return backendRepository.deleteGame(gameId);
    }

    @Override
    public Game readGame(int gameId) {
        return backendRepository.readGame(gameId);
    }

    @Override
    public PaginatedResult<Game> listAllGames(String offset, int pageSize) {
        return backendRepository.listGames(offset, pageSize);
    }

    @Override
    public Game getGameByName(String name) {
        return backendRepository.readGameByName(name);
    }

    @Override
    public Game changeStatusOfGameWithoutPublishing(int gameId, String newStatus, Long updatedAt) throws OasisApiException {
        GameState gameState = validateGameState(newStatus);
        long updatedTs = Numbers.ifNull(updatedAt, System.currentTimeMillis());

        Game game = backendRepository.readGame(gameId);
        if (game == null) {
            throw new OasisApiException(ErrorCodes.GAME_NOT_EXISTS, HttpStatus.NOT_FOUND.value(), "No game is found by id " + gameId);
        }
        return backendRepository.updateGameStatus(gameId, gameState.name(), updatedTs);
    }

    @Override
    public Game changeStatusOfGame(int gameId, String newStatus, Long updatedAt) throws OasisApiException {
        GameState gameState = validateGameState(newStatus);
        long updatedTs = Numbers.ifNull(updatedAt, System.currentTimeMillis());

        Game updatedGame = changeStatusOfGameWithoutPublishing(gameId, newStatus, updatedTs);

        GameStatusChangeEvent statusChangeEvent = new GameStatusChangeEvent(gameState, updatedGame);
        publisher.publishEvent(statusChangeEvent);
        return updatedGame;
    }

    @Override
    public GameStatus getCurrentGameStatus(int gameId) {
        return backendRepository.readCurrentGameStatus(gameId);
    }

    @Override
    public List<GameStatus> listGameStatusHistory(int gameId, Long startFrom, Long endTo) {
        long startTime = startFrom == null ? 0 : startFrom;
        long endTime = endTo == null ? FAR_FUTURE_EPOCH : endTo;
        return backendRepository.readGameStatusHistory(gameId, startTime, endTime);
    }

    private GameState validateGameState(String status) throws OasisApiException {
        if (Objects.isNull(status)) {
            throw new OasisApiException(ErrorCodes.GAME_UNKNOWN_STATE,
                    HttpStatus.BAD_REQUEST.value(), "Game state cannot be null or empty!");
        }

        GameState gameState = availableStatuses.get(status.toLowerCase());
        if (Objects.isNull(gameState)) {
            throw new OasisApiException(ErrorCodes.GAME_UNKNOWN_STATE,
                    HttpStatus.BAD_REQUEST.value(), "Unknown game state for given status " + status + "!");
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

    public void setPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }
}
