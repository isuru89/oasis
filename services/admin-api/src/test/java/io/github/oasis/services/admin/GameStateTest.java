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
import io.github.oasis.services.admin.domain.GameState;
import io.github.oasis.services.admin.domain.GameStateService;
import io.github.oasis.services.admin.internal.dao.IExternalAppDao;
import io.github.oasis.services.common.internal.events.game.GamePausedEvent;
import io.github.oasis.services.common.internal.events.game.GameRemovedEvent;
import io.github.oasis.services.common.internal.events.game.GameRestartedEvent;
import io.github.oasis.services.common.internal.events.game.GameStartedEvent;
import io.github.oasis.services.common.internal.events.game.GameStatusChangedEvent;
import io.github.oasis.services.common.internal.events.game.GameStoppedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static io.github.oasis.services.admin.utils.TestUtils.NONE;
import static io.github.oasis.services.admin.utils.TestUtils.SINGLE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author Isuru Weerarathna
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = OasisAdminConfiguration.class)
@DisplayName("Game State Changes")
public class GameStateTest {

    @MockBean
    private IExternalAppDao dao;

    @MockBean
    private ApplicationEventPublisher publisher;

    @InjectMocks
    private ExternalAppService externalAppService;

    @InjectMocks
    private GameStateService gameStateService;

    private AdminAggregate admin;

    @Captor private ArgumentCaptor<GameStartedEvent> startedEventArgumentCaptor;
    @Captor private ArgumentCaptor<GameStoppedEvent> stoppedEventArgumentCaptor;
    @Captor private ArgumentCaptor<GamePausedEvent> pausedEventArgumentCaptor;
    @Captor private ArgumentCaptor<GameRestartedEvent> restartedEventArgumentCaptor;
    @Captor private ArgumentCaptor<GameRemovedEvent> removedEventArgumentCaptor;

    @BeforeEach
    public void beforeEach() {
        MockitoAnnotations.initMocks(this);
        admin = new AdminAggregate(publisher, externalAppService, gameStateService);
    }

    @DisplayName("Admin should be able to start a created game")
    @Test
    public void testGameCanStart() {

    }

    @DisplayName("Admin should not be able to start a running or deleted game")
    @Test
    public void testGameCannotStart() {
        verifyNoGameEventFired();
    }

    @DisplayName("Admin should be able to pause a created or running game")
    @Test
    public void testGameCanPause() {

    }

    @DisplayName("Admin should not be able to pause a stopped or deleted game")
    @Test
    public void testGameCannotPause() {
        verifyNoGameEventFired();
    }

    @DisplayName("Admin should be able to stop a created, running or paused game")
    @Test
    public void testGameCanStop() {

    }

    @DisplayName("Admin should not be able to stop a stopped or deleted game")
    @Test
    public void testGameCannotStop() {
        verifyNoGameEventFired();
    }

    @DisplayName("Admin should be able to restart a created, paused or stopped game")
    @Test
    public void testGameCanRestart() {
        int gameId = createGameDef("game-test-restart");
        admin.restartGame(gameId);

        verifyGameEventFired(GameState.RUNNING, true, gameId);
    }

    @DisplayName("Admin should not be able to restart a running or deleted game")
    @Test
    public void testGameCannotRestart() {
        int gameId = createGameDef("game-test-no-restart");
        admin.restartGame(gameId);

        verifyNoGameEventFired();
    }

    int createGameDef(String name) {
        return 0;
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
        } else {
            fail("Unknown verification for game state!");
        }
        Mockito.clearInvocations(publisher);
    }
}
