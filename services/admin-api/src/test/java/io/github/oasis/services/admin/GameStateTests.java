/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.github.oasis.services.admin;

import io.github.oasis.services.admin.domain.ExternalAppService;
import io.github.oasis.services.admin.domain.Game;
import io.github.oasis.services.admin.domain.GameState;
import io.github.oasis.services.admin.domain.GameStateService;
import io.github.oasis.services.admin.internal.dao.IExternalAppDao;
import io.github.oasis.services.admin.internal.dao.IGameCreationDao;
import io.github.oasis.services.admin.internal.dao.IGameStateDao;
import io.github.oasis.services.admin.internal.dto.NewGameDto;
import io.github.oasis.services.admin.internal.exceptions.GameStateChangeException;
import io.github.oasis.services.admin.json.game.GameJson;
import io.github.oasis.services.common.internal.events.game.GameCreatedEvent;
import io.github.oasis.services.common.internal.events.game.GamePausedEvent;
import io.github.oasis.services.common.internal.events.game.GameRemovedEvent;
import io.github.oasis.services.common.internal.events.game.GameRestartedEvent;
import io.github.oasis.services.common.internal.events.game.GameStartedEvent;
import io.github.oasis.services.common.internal.events.game.GameStatusChangedEvent;
import io.github.oasis.services.common.internal.events.game.GameStoppedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;

import static io.github.oasis.services.admin.utils.TestUtils.NONE;
import static io.github.oasis.services.admin.utils.TestUtils.SINGLE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @TODO fill remaining tests
 *
 * @author Isuru Weerarathna
 */
@DisplayName("Game State Changes")
public class GameStateTests extends AbstractTest {

    @Autowired private IExternalAppDao externalAppDao;
    @Autowired private IGameStateDao gameDao;
    @Autowired private IGameCreationDao gameCreationDao;

    @MockBean
    private ApplicationEventPublisher publisher;

    private AdminAggregate admin;

    @Captor private ArgumentCaptor<GameStartedEvent> startedEventArgumentCaptor;
    @Captor private ArgumentCaptor<GameStoppedEvent> stoppedEventArgumentCaptor;
    @Captor private ArgumentCaptor<GamePausedEvent> pausedEventArgumentCaptor;
    @Captor private ArgumentCaptor<GameRestartedEvent> restartedEventArgumentCaptor;
    @Captor private ArgumentCaptor<GameRemovedEvent> removedEventArgumentCaptor;
    @Captor private ArgumentCaptor<GameCreatedEvent> createdEventArgumentCaptor;

    @BeforeEach
    void beforeEach() {
        MockitoAnnotations.initMocks(this);
        ExternalAppService externalAppService = new ExternalAppService(externalAppDao);
        GameStateService gameStateService = new GameStateService(gameDao);
        Game game = new Game(gameCreationDao);
        admin = new AdminAggregate(publisher, externalAppService, gameStateService, game);

        super.runBeforeEach();
    }

    @DisplayName("should be able to start a freshly created game")
    @Test
    void testGameCanStart() {
        int gameId = createGameDef("test-start-game");
        verifyOnlyOneGameExistsWithId(gameId, GameState.CREATED);

        admin.startGame(gameId);
        verifyGameEventFired(GameState.RUNNING, gameId);
        verifyOnlyOneGameExistsWithId(gameId, GameState.RUNNING);
    }

    @DisplayName("should be able to start a paused game")
    @Test
    void testGameCanStartPaused() {
        int gameId = createGameDef("test-start-game");
        admin.startGame(gameId);
        verifyGameEventFired(GameState.RUNNING, gameId);

        admin.pauseGame(gameId);
        verifyGameEventFired(GameState.PAUSED, gameId);

        admin.startGame(gameId);
        verifyGameEventFired(GameState.RUNNING, gameId);

        verifyOnlyOneGameExistsWithId(gameId, GameState.RUNNING);
    }

    @DisplayName("should be able to start a stopped game")
    @Test
    void testGameCanStartStopped() {
        int gameId = createGameDef("test-start-game");
        admin.startGame(gameId);
        verifyGameEventFired(GameState.RUNNING, gameId);

        admin.stopGame(gameId);
        verifyGameEventFired(GameState.STOPPED, gameId);

        admin.startGame(gameId);
        verifyGameEventFired(GameState.RUNNING, gameId);

        verifyOnlyOneGameExistsWithId(gameId, GameState.RUNNING);
    }

    @DisplayName("should not be able to start a running game")
    @Test
    void testGameCannotStartRunning() {
        int gameId = createGameDef("test-start-game");
        admin.startGame(gameId);
        verifyGameEventFired(GameState.RUNNING, gameId);

        assertThrows(GameStateChangeException.class, () -> admin.startGame(gameId));
        verifyNoGameEventFired();
    }

    @DisplayName("should not be able to delete a running game")
    @Test
    void testGameCannotDeleteRunning() {
        int gameId = createGameDef("test-delete-game");
        admin.startGame(gameId);
        verifyGameEventFired(GameState.RUNNING, gameId);

        assertThrows(GameStateChangeException.class, () -> admin.removeGame(gameId));
        verifyNoGameEventFired();
    }

    @DisplayName("should not be able to start a deleted game")
    @Test
    void testGameCannotStartDeleted() {
        int gameId = createGameDef("test-start-game");
        admin.removeGame(gameId);
        verifyGameEventFired(GameState.DELETED, gameId);

        assertThrows(GameStateChangeException.class, () -> admin.startGame(gameId));
        verifyNoGameEventFired();
    }

    @DisplayName("should not be able to pause a stopped game")
    @Test
    void testGameCannotPause() {
        int gameId = createGameDef("test-pause-game");

        admin.stopGame(gameId);
        verifyGameEventFired(GameState.STOPPED, gameId);

        assertThrows(GameStateChangeException.class, () -> admin.pauseGame(gameId));
        verifyNoGameEventFired();
    }

    @DisplayName("should not be able to pause a deleted game")
    @Test
    void testGameCannotPauseDeleted() {
        int gameId = createGameDef("test-pause-game");
        admin.removeGame(gameId);
        verifyGameEventFired(GameState.DELETED, gameId);

        assertThrows(GameStateChangeException.class, () -> admin.pauseGame(gameId));
        verifyNoGameEventFired();
    }

    @DisplayName("should not be able to stop a deleted game")
    @Test
    void testGameCannotStopDeleted() {
        int gameId = createGameDef("test-stop-game");
        admin.removeGame(gameId);
        verifyGameEventFired(GameState.DELETED, gameId);

        assertThrows(GameStateChangeException.class, () -> admin.stopGame(gameId));
        verifyNoGameEventFired();
    }

    int createGameDef(String name) {
        NewGameDto dto = new NewGameDto(name, name);
        int id = admin.createGame(dto).getId();
        verifyGameCreatedEventFired(id);
        return id;
    }

    private void verifyGameCreatedEventFired(int withGameId) {
        Mockito.verify(publisher, SINGLE).publishEvent(createdEventArgumentCaptor.capture());
        assertEquals(withGameId,
                createdEventArgumentCaptor.getValue().getGameId(),
                "Fired event has a different ID than created one!");
        Mockito.clearInvocations(publisher);
    }

    private void verifyOnlyOneGameExistsWithId(int gameId, GameState state) {
        List<GameJson> allGames = admin.listAllGames();
        assertEquals(1, allGames.size(), "Only one game should be in the db!");
        GameJson onlyGame = allGames.get(0);
        assertEquals(gameId, onlyGame.getId(), "The game id is different with the db record!");
        assertEquals(state, onlyGame.getCurrentState(), "The game state is different with db record!");
    }

    void verifyNoGameEventFired() {
        Mockito.verify(publisher, NONE).publishEvent(Mockito.any(GameStatusChangedEvent.class));
    }

    void verifyGameEventFired(GameState expectedState, int gameId) {
        verifyGameEventFired(expectedState, false, gameId);
    }

    void verifyGameEventFired(GameState expectedState, boolean restart, int gameId) {
        if (expectedState == GameState.RUNNING) {
            if (restart) {
                Mockito.verify(publisher, SINGLE).publishEvent(restartedEventArgumentCaptor.capture());
                GameRestartedEvent event = restartedEventArgumentCaptor.getValue();
                assertEquals(gameId, event.getGameId());
            } else {
                Mockito.verify(publisher, SINGLE).publishEvent(startedEventArgumentCaptor.capture());
                GameStartedEvent event = startedEventArgumentCaptor.getValue();
                assertEquals(gameId, event.getGameId());
            }
        } else if (expectedState == GameState.STOPPED) {
            Mockito.verify(publisher, SINGLE).publishEvent(stoppedEventArgumentCaptor.capture());
            GameStoppedEvent event = stoppedEventArgumentCaptor.getValue();
            assertEquals(gameId, event.getGameId());
        } else if (expectedState == GameState.PAUSED) {
            Mockito.verify(publisher, SINGLE).publishEvent(pausedEventArgumentCaptor.capture());
            GamePausedEvent event = pausedEventArgumentCaptor.getValue();
            assertEquals(gameId, event.getGameId());
        } else if (expectedState == GameState.DELETED) {
            Mockito.verify(publisher, SINGLE).publishEvent(removedEventArgumentCaptor.capture());
            GameRemovedEvent event = removedEventArgumentCaptor.getValue();
            assertEquals(gameId, event.getGameId());
        } else if (expectedState == GameState.CREATED) {
            Mockito.verify(publisher, SINGLE).publishEvent(createdEventArgumentCaptor.capture());
            GameCreatedEvent event = createdEventArgumentCaptor.getValue();
            assertEquals(gameId, event.getGameId());
        } else {
            fail("Unknown verification for game state!");
        }
        Mockito.clearInvocations(publisher);
    }
}
