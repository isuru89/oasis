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

package io.github.oasis.core.services.api;

import io.github.oasis.core.Game;
import io.github.oasis.core.exception.OasisException;
import io.github.oasis.core.exception.OasisRuntimeException;
import io.github.oasis.core.external.messages.GameState;
import io.github.oasis.core.services.api.beans.BackendRepository;
import io.github.oasis.core.services.api.beans.jdbc.JdbcRepository;
import io.github.oasis.core.services.api.controllers.admin.GamesController;
import io.github.oasis.core.services.api.dao.IGameDao;
import io.github.oasis.core.services.api.exceptions.ErrorCodes;
import io.github.oasis.core.services.api.exceptions.OasisApiRuntimeException;
import io.github.oasis.core.services.api.services.GameService;
import io.github.oasis.core.services.api.services.IEngineManager;
import io.github.oasis.core.services.api.to.GameObjectRequest;
import io.github.oasis.core.services.exceptions.OasisApiException;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Isuru Weerarathna
 */
public class GameServiceTest extends AbstractServiceTest {

    private GamesController controller;
    private final IEngineManager engineManager = Mockito.mock(IEngineManager.class);

    private final GameObjectRequest stackOverflow = GameObjectRequest.builder()
            .name("Stack-overflow")
            .description("Stackoverflow badges and points system")
            .logoRef("https://oasis.io/assets/so.jpeg")
            .motto("Help the community")
            .build();

    private final GameObjectRequest promotions = GameObjectRequest.builder()
            .name("Promotions")
            .description("Provides promotions for customers based on their loyality")
            .logoRef("https://oasis.io/assets/pm.jpeg")
            .motto("Serve your customers")
            .build();

    @Test
    void addGame() throws OasisException {
        Game game = controller.addGame(stackOverflow);
        System.out.println(game);
        Assertions.assertNotNull(game);
        assertGame(game, stackOverflow);

        System.out.println(promotions);
        Game pGame = controller.addGame(promotions);
        assertGame(pGame, promotions);

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> controller.addGame(stackOverflow))
                .isInstanceOf(OasisApiRuntimeException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCodes.GAME_ALREADY_EXISTS);
    }

    @Test
    void listGames() throws OasisException {
        assertEquals(0, controller.listGames("0", 50).getRecords().size());

        controller.addGame(stackOverflow);
        assertEquals(1, controller.listGames("0", 50).getRecords().size());

        controller.addGame(promotions);
        assertEquals(2, controller.listGames("0", 50).getRecords().size());

        Assertions.assertThrows(OasisApiRuntimeException.class, () -> controller.addGame(stackOverflow));
        assertEquals(2, controller.listGames("0", 50).getRecords().size());
    }

    @Test
    void updateGame() throws OasisException {
        int stackId = controller.addGame(stackOverflow).getId();

        assertGame(engineRepo.readGame(stackId), stackOverflow);
        assertGame(adminRepo.readGame(stackId), stackOverflow);

        GameObjectRequest updateRequest = stackOverflow.toBuilder()
                .id(stackId)
                .motto("new motto")
                .description("new description")
                .logoRef("new logo ref")
                .build();
        Game updatedGame = controller.updateGame(stackId, updateRequest);
        assertGame(updatedGame, updateRequest);
    }

    @Test
    void readGame() throws OasisException {
        int stackId = controller.addGame(stackOverflow).getId();
        Game stackGame = controller.readGame(stackId);
        assertGame(stackGame, stackOverflow);
    }

    @Test
    void readGameByName() throws OasisException {
        controller.addGame(stackOverflow);
        Game stackGame = controller.getGameByName(stackOverflow.getName());
        assertGame(stackGame, stackOverflow);
    }

    @Test
    void deleteGame() throws OasisException {
        int stackId = controller.addGame(stackOverflow).getId();

        assertGame(engineRepo.readGame(stackId), stackOverflow);
        assertGame(adminRepo.readGame(stackId), stackOverflow);

        assertNotNull(controller.deleteGame(stackId));

        assertFalse(adminRepo.readGame(stackId).isActive());
        assertThrows(OasisRuntimeException.class, () -> engineRepo.readGame(stackId));
    }

    @Test
    void updateGameStatus() throws OasisException {
        int stackId = controller.addGame(stackOverflow).getId();

        Mockito.reset(engineManager);
        Game gameRef = controller.updateGameStatus(stackId, "start");
        assertEngineManagerOnceCalledWithState(GameState.STARTED, gameRef);

        Mockito.reset(engineManager);
        gameRef = controller.updateGameStatus(stackId, "stop");
        assertEngineManagerOnceCalledWithState(GameState.STOPPED, gameRef);

        Mockito.reset(engineManager);
        gameRef = controller.updateGameStatus(stackId, "pause");
        assertEngineManagerOnceCalledWithState(GameState.PAUSED, gameRef);

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> controller.updateGameStatus(stackId, null))
                .isInstanceOf(OasisApiException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCodes.GAME_UNKNOWN_STATE);
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> controller.updateGameStatus(stackId, ""))
                .isInstanceOf(OasisApiException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCodes.GAME_UNKNOWN_STATE);
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> controller.updateGameStatus(stackId, "hello"))
                .isInstanceOf(OasisApiException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCodes.GAME_UNKNOWN_STATE);
    }

    private void assertEngineManagerOnceCalledWithState(GameState state, Game game) {
        Mockito.verify(engineManager,
                Mockito.times(1)).changeGameStatus(state, game);
    }

    @Override
    JdbcRepository createJdbcRepository(Jdbi jdbi) {
        return new JdbcRepository(
                jdbi.onDemand(IGameDao.class),
                null,
                null,
                null,
                null
        );
    }

    @Override
    void createServices(BackendRepository backendRepository) {
        controller = new GamesController(new GameService(backendRepository, engineManager));
    }

    private void assertGame(Game db, GameObjectRequest other) {
        assertTrue(db.getId() > 0);
        assertEquals(other.getName(), db.getName());
        assertEquals(other.getDescription(), db.getDescription());
        assertEquals(other.getLogoRef(), db.getLogoRef());
        assertEquals(other.getMotto(), db.getMotto());
    }
}
