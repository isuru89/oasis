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
import io.github.oasis.core.external.OasisRepository;
import io.github.oasis.core.services.api.exceptions.DataValidationException;
import io.github.oasis.core.services.api.exceptions.ErrorCodes;
import io.github.oasis.core.services.api.to.GameObjectRequest;
import io.github.oasis.core.services.exceptions.OasisApiException;
import io.github.oasis.core.utils.Texts;
import io.github.oasis.core.utils.Utils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * @author Isuru Weerarathna
 */
@Service
public class GameService {

    private final OasisRepository repository;

    public GameService(OasisRepository repository) {
        this.repository = repository;
    }

    public Game addGame(GameObjectRequest gameObjectRequest) throws OasisApiException {
        Game game = new Game();
        game.setName(gameObjectRequest.getName());
        game.setDescription(gameObjectRequest.getDescription());
        game.setMotto(gameObjectRequest.getMotto());

        validateGameObjectForCreation(game);

        Game addedGame = repository.addNewGame(game);

        // add attributes associated with game
        if (Utils.isNotEmpty(gameObjectRequest.getAttributes())) {
            for (AttributeInfo attribute : gameObjectRequest.getAttributes()) {
                repository.addAttribute(addedGame.getId(), attribute);
            }
        }
        return addedGame;
    }

    public Game updateGame(int gameId, Game game) throws OasisApiException {
        validateGameObjectForEdit(game);

        return repository.updateGame(gameId, game);
    }

    public Game deleteGame(int gameId) {
        return repository.deleteGame(gameId);
    }

    public Game readGame(int gameId) {
        return repository.readGame(gameId);
    }

    public List<Game> listAllGames() {
        return repository.listGames();
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
