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
import io.github.oasis.core.elements.ElementDef;
import io.github.oasis.core.exception.OasisException;
import io.github.oasis.core.external.messages.GameState;
import io.github.oasis.core.model.EventSource;
import io.github.oasis.core.services.api.TestUtils;
import io.github.oasis.core.services.api.exceptions.ErrorCodes;
import io.github.oasis.core.services.api.exceptions.OasisApiRuntimeException;
import io.github.oasis.core.services.api.services.impl.GameService;
import io.github.oasis.core.services.api.to.ElementCreateRequest;
import io.github.oasis.core.services.api.to.EventSourceCreateRequest;
import io.github.oasis.core.services.api.to.GameCreateRequest;
import io.github.oasis.core.services.api.to.GameUpdateRequest;
import io.github.oasis.core.services.events.GameStatusChangeEvent;
import io.github.oasis.core.services.exceptions.OasisApiException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Isuru Weerarathna
 */
public class GameServiceTest extends AbstractServiceTest {

    @Autowired
    private IGameService gameService;

    @Autowired
    private IEventSourceService eventSourceService;

    @Autowired
    private IElementService elementService;

    public static final String TESTPOINT = "testpoint";
    public static final String TESTBADGE = "testbadge";

    private final IEngineManager engineManager = Mockito.mock(IEngineManager.class);

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    private final GameCreateRequest stackOverflow = GameCreateRequest.builder()
            .name("Stack-overflow")
            .description("Stackoverflow badges and points system")
            .logoRef("https://oasis.io/assets/so.jpeg")
            .motto("Help the community")
            .build();

    private final GameCreateRequest promotions = GameCreateRequest.builder()
            .name("Promotions")
            .description("Provides promotions for customers based on their loyality")
            .logoRef("https://oasis.io/assets/pm.jpeg")
            .motto("Serve your customers")
            .build();

    @Test
    void addGame() throws OasisException {
        Game game = gameService.addGame(stackOverflow);
        System.out.println(game);
        Assertions.assertNotNull(game);
        assertGame(game, stackOverflow);
        assertEquals(GameState.CREATED.name(), game.getCurrentStatus());

        System.out.println(promotions);
        Game pGame = gameService.addGame(promotions);
        assertGame(pGame, promotions);
        assertEquals(GameState.CREATED.name(), pGame.getCurrentStatus());

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> gameService.addGame(stackOverflow))
                .isInstanceOf(OasisApiRuntimeException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCodes.GAME_ALREADY_EXISTS);
    }

    @Test
    void listGames() throws OasisException {
        assertEquals(0, gameService.listAllGames("0", 50).getRecords().size());

        gameService.addGame(stackOverflow);
        assertEquals(1, gameService.listAllGames("0", 50).getRecords().size());

        gameService.addGame(promotions);
        assertEquals(2, gameService.listAllGames("0", 50).getRecords().size());

        Assertions.assertThrows(OasisApiRuntimeException.class, () -> gameService.addGame(stackOverflow));
        assertEquals(2, gameService.listAllGames("0", 50).getRecords().size());
    }

    @Test
    void updateGame() throws OasisException {
        Game stackGame = gameService.addGame(stackOverflow);
        int stackId = stackGame.getId();

        GameUpdateRequest updateRequest = GameUpdateRequest.builder()
                .id(stackId)
                .motto("new motto")
                .description("new description")
                .logoRef("new logo ref")
                .build();
        Game updatedGame = gameService.updateGame(stackId, updateRequest);
        assertGame(updatedGame, updateRequest);
        assertEquals(GameState.CREATED.name(), updatedGame.getCurrentStatus());
    }

    @Test
    void shouldNotUpdateGameWithStatus() throws OasisException {
        Game stackGame = gameService.addGame(stackOverflow);
        int stackId = stackGame.getId();

        GameUpdateRequest updateRequest = GameUpdateRequest.builder()
                .id(stackId)
                .motto("new motto")
                .description("new description")
                .logoRef("new logo ref")
                .build();
        Game updatedGame = gameService.updateGame(stackId, updateRequest);
        assertGame(updatedGame, updateRequest);
        assertEquals(GameState.CREATED.name(), updatedGame.getCurrentStatus());
    }

    @Test
    void updateGameStatusOnly() throws OasisException {
        Game stackGame = gameService.addGame(stackOverflow);
        int stackId = stackGame.getId();

        Game updatedGame = gameService.changeStatusOfGame(stackId, GameState.STARTED.name(), System.currentTimeMillis());
        assertEquals(GameState.STARTED.name(), updatedGame.getCurrentStatus());
    }

    @Test
    void readGame() throws OasisException {
        int stackId = gameService.addGame(stackOverflow).getId();
        Game stackGame = gameService.readGame(stackId);
        assertGame(stackGame, stackOverflow);
    }

    @Test
    void readGameByName() throws OasisException {
        gameService.addGame(stackOverflow);
        Game stackGame = gameService.getGameByName(stackOverflow.getName());
        assertGame(stackGame, stackOverflow);
    }

    @Test
    void deleteGame() throws OasisException {
        Mockito.reset(engineManager);
        ApplicationEventPublisher spy = Mockito.spy(eventPublisher);
        if (gameService instanceof GameService) {
            // we know this is the service
            ((GameService) gameService).setPublisher(spy);
        }

        int stackId = gameService.addGame(stackOverflow).getId();
        Game dbGame = gameService.readGame(stackId);
        EventSource eventSource = eventSourceService.registerEventSource(EventSourceCreateRequest.builder().name("test-1").build());
        eventSourceService.assignEventSourceToGame(eventSource.getId(), stackId);

        List<ElementCreateRequest> elementCreateRequests = TestUtils.parseElementRules("rules.yml", stackId);
        ElementDef elementPoint = elementService.addElement(stackId, TestUtils.findById(TESTPOINT, elementCreateRequests));
        ElementDef elementBadge = elementService.addElement(stackId, TestUtils.findById(TESTBADGE, elementCreateRequests));

        assertEquals(1, eventSourceService.listAllEventSourcesOfGame(stackId).size());
        assertEquals(elementBadge.getElementId(), elementService.readElement(stackId, TESTBADGE, false).getElementId());
        assertEquals(elementPoint.getElementId(), elementService.readElement(stackId, TESTPOINT, false).getElementId());

        Mockito.reset(spy);
        assertNotNull(gameService.deleteGame(stackId));

        assertTrue(eventSourceService.listAllEventSourcesOfGame(stackId).isEmpty());
        // but still event source must exist
        EventSource dbSource = eventSourceService.readEventSource(eventSource.getId());
        assertNotNull(dbSource);
        assertTrue(dbSource.isActive());
        assertThrows(OasisApiException.class, () -> elementService.readElement(stackId, TESTBADGE, false));
        assertThrows(OasisApiException.class, () -> elementService.readElement(stackId, TESTPOINT, false));

        // game stopped message should dispatch
        ArgumentCaptor<GameStatusChangeEvent> eventArgumentCaptor = ArgumentCaptor.forClass(GameStatusChangeEvent.class);
        assertEngineManagerOnceCalledWithState(spy, GameState.STOPPED, dbGame.toBuilder().currentStatus(GameState.STOPPED.name()).build(), eventArgumentCaptor);
    }


    @Test
    void updateGameStatus() throws OasisException {
        ApplicationEventPublisher spy = Mockito.spy(eventPublisher);
        if (gameService instanceof GameService) {
            // we know this is the service
            ((GameService) gameService).setPublisher(spy);
        }

        int stackId = gameService.addGame(stackOverflow).getId();
        ArgumentCaptor<GameStatusChangeEvent> eventArgumentCaptor = ArgumentCaptor.forClass(GameStatusChangeEvent.class);

        Mockito.reset(spy);
        Game gameRef = gameService.changeStatusOfGame(stackId, "start", System.currentTimeMillis());
        assertEngineManagerOnceCalledWithState(spy, GameState.STARTED, gameRef, eventArgumentCaptor);

        Mockito.reset(spy);
        gameRef = gameService.changeStatusOfGame(stackId, "stop", System.currentTimeMillis());
        assertEngineManagerOnceCalledWithState(spy, GameState.STOPPED, gameRef, eventArgumentCaptor);

        Mockito.reset(spy);
        gameRef = gameService.changeStatusOfGame(stackId, "pause", System.currentTimeMillis());
        assertEngineManagerOnceCalledWithState(spy, GameState.PAUSED, gameRef, eventArgumentCaptor);

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> gameService.changeStatusOfGame(stackId, null, System.currentTimeMillis()))
                .isInstanceOf(OasisApiException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCodes.GAME_UNKNOWN_STATE);
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> gameService.changeStatusOfGame(stackId, "", System.currentTimeMillis()))
                .isInstanceOf(OasisApiException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCodes.GAME_UNKNOWN_STATE);
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> gameService.changeStatusOfGameWithoutPublishing(stackId, "hello", System.currentTimeMillis()))
                .isInstanceOf(OasisApiException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCodes.GAME_UNKNOWN_STATE);
    }

    private void assertEngineManagerOnceCalledWithState(ApplicationEventPublisher publisher, GameState state, Game game, ArgumentCaptor<GameStatusChangeEvent> captor) {
        if (publisher != null) {
            Mockito.verify(publisher,
                    Mockito.times(1)).publishEvent(captor.capture());
        }

        GameStatusChangeEvent value = captor.getValue();
        Assertions.assertEquals(state, value.getNewGameState());
        Assertions.assertEquals(game, value.getGameRef());
    }

    private void assertGame(Game db, GameCreateRequest other) {
        assertTrue(db.getId() > 0);
        assertEquals(other.getName(), db.getName());
        assertEquals(other.getDescription(), db.getDescription());
        assertEquals(other.getLogoRef(), db.getLogoRef());
        assertEquals(other.getMotto(), db.getMotto());
    }

    private void assertGame(Game db, GameUpdateRequest other) {
        assertTrue(db.getId() > 0);
        assertEquals(other.getDescription(), db.getDescription());
        assertEquals(other.getLogoRef(), db.getLogoRef());
        assertEquals(other.getMotto(), db.getMotto());
    }
}
