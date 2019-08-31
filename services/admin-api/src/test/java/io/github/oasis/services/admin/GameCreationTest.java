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
import io.github.oasis.services.admin.json.game.GameJson;
import io.github.oasis.services.common.OasisValidationException;
import io.github.oasis.services.common.internal.events.game.GameCreatedEvent;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Isuru Weerarathna
 */
@DisplayName("New Game Creation")
public class GameCreationTest extends AbstractTest {

    @Autowired private IExternalAppDao externalAppDao;
    @Autowired private IGameStateDao gameDao;
    @Autowired private IGameCreationDao gameCreationDao;

    @MockBean private ApplicationEventPublisher publisher;

    private AdminAggregate admin;

    @Captor
    private ArgumentCaptor<GameCreatedEvent> gameCreatedEventArgumentCaptor;

    @BeforeEach
    public void beforeEach() {
        MockitoAnnotations.initMocks(this);
        ExternalAppService externalAppService = new ExternalAppService(externalAppDao);
        GameStateService gameStateService = new GameStateService(gameDao);
        Game game = new Game(gameCreationDao);
        admin = new AdminAggregate(publisher, externalAppService, gameStateService, game);

        super.runBeforeEach();
    }

    @DisplayName("Admin should be able to create games")
    @Test
    public void testGameCreation() {
        NewGameDto gameDto = new NewGameDto("game-test", "this game is for testing");
        GameJson createdGame = admin.createGame(gameDto);
        assertNewGame(gameDto, createdGame);

        verifyGameCreatedEventFired(createdGame.getId());
        verifyOnlyOneGameExists();
    }

    @DisplayName("Game name is mandatory")
    @Test
    public void testNameConstraints() {
        assertThrows(OasisValidationException.class,
                () -> admin.createGame(new NewGameDto(null, "this game is for testing")));
        verifyNoGameCreatedEventFired();
        verifyNoGameExists();

        assertThrows(OasisValidationException.class,
                () -> admin.createGame(new NewGameDto("", "this game is for testing")));
        verifyNoGameCreatedEventFired();
        verifyNoGameExists();
    }

    @DisplayName("Game description is optional")
    @Test
    public void testDescriptionConstraints() {
        NewGameDto gameDto = new NewGameDto("game-test", null);
        GameJson createdGame = admin.createGame(gameDto);
        assertNewGame(gameDto, createdGame);

        verifyGameCreatedEventFired(createdGame.getId());
        verifyOnlyOneGameExists();
    }

    private void verifyOnlyOneGameExists() {
        List<GameJson> allGames = admin.listAllGames();
        assertEquals(1, allGames.size(), "Only one game should be in the db!");
    }

    private void verifyNoGameExists() {
        List<GameJson> allGames = admin.listAllGames();
        assertEquals(0, allGames.size(), "No game should be in the db!");
    }

    private void assertNewGame(NewGameDto dto, GameJson created) {
        assertNotNull(created);
        assertEquals(dto.getName(), created.getName(), "Game name is different!");
        assertEquals(dto.getDescription(), created.getDescription(), "Game description is different!");
        assertEquals(GameState.CREATED, created.getCurrentState(), "Initial game state must be CREATED!");
    }

    private void verifyNoGameCreatedEventFired() {
        Mockito.verify(publisher, NONE).publishEvent(gameCreatedEventArgumentCaptor.capture());
        Mockito.clearInvocations(publisher);
    }

    private void verifyGameCreatedEventFired(int withGameId) {
        Mockito.verify(publisher, SINGLE).publishEvent(gameCreatedEventArgumentCaptor.capture());
        assertEquals(withGameId,
                gameCreatedEventArgumentCaptor.getValue().getGameId(),
                "Fired event has a different ID than created one!");
        Mockito.clearInvocations(publisher);
    }
}
