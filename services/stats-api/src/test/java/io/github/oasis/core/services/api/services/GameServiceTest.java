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
import io.github.oasis.core.exception.OasisRuntimeException;
import io.github.oasis.core.external.messages.GameState;
import io.github.oasis.core.model.EventSource;
import io.github.oasis.core.model.GameStatus;
import io.github.oasis.core.services.api.TestUtils;
import io.github.oasis.core.services.api.exceptions.ErrorCodes;
import io.github.oasis.core.services.api.exceptions.OasisApiRuntimeException;
import io.github.oasis.core.services.api.handlers.CacheClearanceListener;
import io.github.oasis.core.services.api.handlers.events.EntityChangeType;
import io.github.oasis.core.services.api.handlers.events.GameSpecChangedEvent;
import io.github.oasis.core.services.api.services.impl.GameService;
import io.github.oasis.core.services.api.to.ElementCreateRequest;
import io.github.oasis.core.services.api.to.EventSourceCreateRequest;
import io.github.oasis.core.services.api.to.GameCreateRequest;
import io.github.oasis.core.services.api.to.GameUpdateRequest;
import io.github.oasis.core.services.events.GameStatusChangeEvent;
import io.github.oasis.core.services.exceptions.OasisApiException;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Isuru Weerarathna
 */
public class GameServiceTest extends AbstractServiceTest {

    @Autowired
    private IGameService gameService;

    @SpyBean
    private CacheClearanceListener cacheClearanceListener;

    @SpyBean
    private IEngineManager engineManager;

    public static final String TESTPOINT = "testpoint";
    public static final String TESTBADGE = "testbadge";

//    private final IEngineManager engineManager = Mockito.mock(IEngineManager.class);

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    private final GameCreateRequest stackOverflow = GameCreateRequest.builder()
            .name("Stack-overflow \uD83D\uDC7D")
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
    void addGame() {
        Game game = doPostSuccess("/games", stackOverflow, Game.class);
        System.out.println(game);
        Assertions.assertNotNull(game);
        assertGame(game, stackOverflow);
        assertCurrentGameStatus(game.getId(), "CREATED");

        System.out.println(promotions);
        Game pGame = doPostSuccess("/games", promotions, Game.class);
        assertGame(pGame, promotions);
        assertCurrentGameStatus(game.getId(), "CREATED");

        doPostError("/games", stackOverflow, HttpStatus.BAD_REQUEST, ErrorCodes.GAME_ALREADY_EXISTS);
    }

    @Test
    void addGameWithStartAndEnd() {
        var request = promotions.toBuilder()
                .startTime(System.currentTimeMillis())
                .endTime(LocalDate.of(2050, 1, 1).atStartOfDay()
                        .toInstant(ZoneOffset.UTC).toEpochMilli())
                .build();
        Game game = doPostSuccess("/games", request, Game.class);
        System.out.println(game);
        Assertions.assertNotNull(game);
        assertGame(game, request);
        assertCurrentGameStatus(game.getId(), "CREATED");
    }

    @Test
    void addGameValidations() {
        doPostError("/games", stackOverflow.toBuilder().name(null).build(), HttpStatus.BAD_REQUEST, ErrorCodes.INVALID_PARAMETER);
        doPostError("/games", stackOverflow.toBuilder().name("").build(), HttpStatus.BAD_REQUEST, ErrorCodes.INVALID_PARAMETER);
        doPostError("/games", stackOverflow.toBuilder().name(RandomStringUtils.randomAscii(256)).build(), HttpStatus.BAD_REQUEST, ErrorCodes.INVALID_PARAMETER);
    }

    @Test
    void listGames() {
        assertEquals(0, doGetPaginatedSuccess("/games", Game.class).getRecords().size());

        doPostSuccess("/games", stackOverflow, Game.class);
        var records = doGetPaginatedSuccess("/games", Game.class).getRecords();
        assertEquals(1, records.size());
        assertGame(records.get(0), stackOverflow);

        doPostSuccess("/games", promotions, Game.class);
        assertEquals(2, doGetPaginatedSuccess("/games", Game.class).getRecords().size());
        assertEquals(1, doGetPaginatedSuccess("/games?page=0&pageSize=1", Game.class).getRecords().size());
        assertEquals(1, doGetPaginatedSuccess("/games?page=1&pageSize=1", Game.class).getRecords().size());

        doPostError("/games", stackOverflow, HttpStatus.BAD_REQUEST, ErrorCodes.GAME_ALREADY_EXISTS);
        assertEquals(2, doGetPaginatedSuccess("/games", Game.class).getRecords().size());
    }

    @Test
    void updateGame() {
        Game stackGame = doPostSuccess("/games", stackOverflow, Game.class);
        int stackId = stackGame.getId();

        GameUpdateRequest updateRequest = GameUpdateRequest.builder()
                .id(stackId)
                .motto("new motto")
                .description("new description")
                .logoRef("new logo ref")
                .version(stackGame.getVersion())
                .build();
        Game updatedGame = doPatchSuccess("/games/" + stackId, updateRequest, Game.class);
        assertGame(updatedGame, updateRequest);
        assertEquals(stackGame.getVersion() + 1, updatedGame.getVersion());
        assertCurrentGameStatus(stackId, "CREATED");

        assertThirdPartyCacheClearanceForGame(stackId, EntityChangeType.MODIFIED);
    }

    @Test
    void updateStartTimeOfGame() {
        Game stackGame = doPostSuccess("/games", stackOverflow, Game.class);
        int stackId = stackGame.getId();

        GameUpdateRequest updateRequest = GameUpdateRequest.builder()
                .id(stackId)
                .version(stackGame.getVersion())
                .startTime(LocalDate.of(2022, 1, 1).atStartOfDay()
                        .toInstant(ZoneOffset.UTC).toEpochMilli())
                .build();
        Game updatedGame = doPatchSuccess("/games/" + stackId, updateRequest, Game.class);
        assertGame(updatedGame, updateRequest);
        assertEquals(stackGame.getVersion() + 1, updatedGame.getVersion());
        assertCurrentGameStatus(stackId, "CREATED");

        assertThirdPartyCacheClearanceForGame(stackId, EntityChangeType.MODIFIED);
    }

    @Test
    void updateShouldFailWithoutGameVersion() {
        Game stackGame = doPostSuccess("/games", stackOverflow, Game.class);
        int stackId = stackGame.getId();

        {
            // no version
            GameUpdateRequest updateRequest = GameUpdateRequest.builder()
                    .id(stackId)
                    .motto("new motto")
                    .description("new description")
                    .logoRef("new logo ref")
                    .build();
            doPatchError("/games/" + stackId, updateRequest, HttpStatus.BAD_REQUEST, ErrorCodes.INVALID_PARAMETER);
            Mockito.verify(cacheClearanceListener, Mockito.never()).handleGameUpdateEvent(Mockito.any());
        }

        {
            // incorrect version
            GameUpdateRequest updateRequest = GameUpdateRequest.builder()
                    .id(stackId)
                    .motto("new motto")
                    .description("new description")
                    .logoRef("new logo ref")
                    .version(stackGame.getVersion() + 1000)
                    .build();
            doPatchError("/games/" + stackId, updateRequest, HttpStatus.CONFLICT, ErrorCodes.GAME_UPDATE_CONFLICT);
            Mockito.verify(cacheClearanceListener, Mockito.never()).handleGameUpdateEvent(Mockito.any());
        }
    }

    @Test
    void shouldNotUpdateGameWithStatus() {
        Game stackGame = doPostSuccess("/games", stackOverflow, Game.class);
        int stackId = stackGame.getId();

        GameUpdateRequest updateRequest = GameUpdateRequest.builder()
                .id(stackId)
                .motto("new motto")
                .description("new description")
                .logoRef("new logo ref")
                .version(stackGame.getVersion())
                .build();
        Game updatedGame = doPatchSuccess("/games/" + stackId, updateRequest, Game.class);
        assertGame(updatedGame, updateRequest);
        assertEquals(stackGame.getVersion() + 1, updatedGame.getVersion());
        assertCurrentGameStatus(stackId, "CREATED");
    }

    @Test
    void updateGameStatusOnly() {
        Game stackGame = doPostSuccess("/games", stackOverflow, Game.class);
        int stackId = stackGame.getId();

        sleepSafe(100);

        Game updatedGame = doPutSuccess("/games/" + stackId + "/start", null, Game.class);
        assertCurrentGameStatus(stackId, "STARTED");
    }

    @Test
    void readGame() {
        int stackId = doPostSuccess("/games", stackOverflow, Game.class).getId();
        Game stackGame = doGetSuccess("/games/" + stackId, Game.class);
        assertGame(stackGame, stackOverflow);

        // read nonexisting game
        doGetError("/games/99999", HttpStatus.NOT_FOUND, ErrorCodes.GAME_NOT_EXISTS);
    }

    @Test
    void readGameByName() {
        doPostSuccess("/games", stackOverflow, Game.class);
        Game stackGame = gameService.getGameByName(stackOverflow.getName());
        assertGame(stackGame, stackOverflow);
    }

    @Test
    void deleteGame() {
        Mockito.reset(engineManager);
        ApplicationEventPublisher spy = Mockito.spy(eventPublisher);
        if (gameService instanceof GameService) {
            // we know this is the service
            ((GameService) gameService).setPublisher(spy);
        }

        int stackId = doPostSuccess("/games", stackOverflow, Game.class).getId();
        Game dbGame = doGetSuccess("/games/" + stackId, Game.class);
        EventSource eventSource = doPostSuccess("/admin/event-sources", EventSourceCreateRequest.builder().name("test-1").build(), EventSource.class);
        doPostSuccess("/admin/games/" + stackId + "/event-sources/" + eventSource.getId(), null, null);

        List<ElementCreateRequest> elementCreateRequests = TestUtils.parseElementRules("rules.yml", stackId);
        ElementDef elementPoint = doPostSuccess("/games/" + stackId + "/elements", TestUtils.findById(TESTPOINT, elementCreateRequests), ElementDef.class);
        ElementDef elementBadge = doPostSuccess("/games/" + stackId + "/elements", TestUtils.findById(TESTBADGE, elementCreateRequests), ElementDef.class);

        assertEquals(1, doGetListSuccess("/admin/games/" + stackId + "/event-sources", EventSource.class).size());

        assertEquals(elementBadge.getElementId(), doGetSuccess("/games/" + stackId + "/elements/" + TESTBADGE + "?withData=false", ElementDef.class).getElementId());
        assertEquals(elementPoint.getElementId(), doGetSuccess("/games/" + stackId + "/elements/" + TESTPOINT + "?withData=false", ElementDef.class).getElementId());

        Mockito.reset(spy);
        assertNotNull(doDeleteSuccess("/games/" + stackId, Game.class));

        // read back the game
        var gameDeleted = doGetSuccess("/games/" + stackId, Game.class);
        assertFalse(gameDeleted.isActive());
        assertCurrentGameStatus(stackId, GameState.STOPPED.name());

        assertTrue(doGetListSuccess("/admin/games/" + stackId + "/event-sources", EventSource.class).isEmpty());
        // but still event source must exist

        EventSource dbSource = doGetSuccess("/admin/event-sources/" + eventSource.getId(), EventSource.class);
        assertNotNull(dbSource);
        assertTrue(dbSource.isActive());
        doGetError("/games/" + stackId + "/elements/" + TESTBADGE + "?withData=false", HttpStatus.NOT_FOUND, ErrorCodes.ELEMENT_NOT_EXISTS);
        doGetError("/games/" + stackId + "/elements/" + TESTPOINT + "?withData=false", HttpStatus.NOT_FOUND, ErrorCodes.ELEMENT_NOT_EXISTS);

        // game removed message should dispatch
        ArgumentCaptor<Object> eventArgumentCaptor = ArgumentCaptor.forClass(Object.class);
        Mockito.verify(spy, Mockito.times(1)).publishEvent(eventArgumentCaptor.capture());
        {
            GameSpecChangedEvent specChangedEvent = (GameSpecChangedEvent) eventArgumentCaptor.getValue();
            assertEquals(EntityChangeType.REMOVED, specChangedEvent.getChangeType());
            assertEquals(stackId, specChangedEvent.getGameId());
        }

        Mockito.verify(engineManager, Mockito.times(1))
                .notifyGameStatusChange(Mockito.eq(GameState.STOPPED), Mockito.any());
    }


    @Test
    void deleteGameTransactional() {
        Mockito.reset(engineManager);
        ApplicationEventPublisher spy = Mockito.mock(ApplicationEventPublisher.class);
        if (gameService instanceof GameService) {
            // we know this is the service
            ((GameService) gameService).setPublisher(spy);
        }

        int stackId = doPostSuccess("/games", stackOverflow, Game.class).getId();
        Game dbGame = doGetSuccess("/games/" + stackId, Game.class);
        EventSource eventSource = doPostSuccess("/admin/event-sources", EventSourceCreateRequest.builder().name("test-1").build(), EventSource.class);
        doPostSuccess("/admin/games/" + stackId + "/event-sources/" + eventSource.getId(), null, null);

        List<ElementCreateRequest> elementCreateRequests = TestUtils.parseElementRules("rules.yml", stackId);
        ElementDef elementPoint = doPostSuccess("/games/" + stackId + "/elements", TestUtils.findById(TESTPOINT, elementCreateRequests), ElementDef.class);
        ElementDef elementBadge = doPostSuccess("/games/" + stackId + "/elements", TestUtils.findById(TESTBADGE, elementCreateRequests), ElementDef.class);

        assertEquals(1, doGetListSuccess("/admin/games/" + stackId + "/event-sources", EventSource.class).size());

        assertEquals(elementBadge.getElementId(), doGetSuccess("/games/" + stackId + "/elements/" + TESTBADGE + "?withData=false", ElementDef.class).getElementId());
        assertEquals(elementPoint.getElementId(), doGetSuccess("/games/" + stackId + "/elements/" + TESTPOINT + "?withData=false", ElementDef.class).getElementId());

        Mockito.reset(spy);
        Mockito.doThrow(new OasisApiRuntimeException(ErrorCodes.UNABLE_TO_CHANGE_GAME_STATE))
                .when(spy).publishEvent(Mockito.eq(GameSpecChangedEvent.builder()
                        .gameId(stackId)
                        .changeType(EntityChangeType.REMOVED)
                        .build()));
        doDeletetError("/games/" + stackId, HttpStatus.INTERNAL_SERVER_ERROR, ErrorCodes.UNABLE_TO_CHANGE_GAME_STATE);

        // read back the game
        var gameDeleted = doGetSuccess("/games/" + stackId, Game.class);
        assertTrue(gameDeleted.isActive());
        assertCurrentGameStatus(stackId, GameState.CREATED.name());

        assertEquals(doGetListSuccess("/admin/games/" + stackId + "/event-sources", EventSource.class).size(), 1);

        EventSource dbSource = doGetSuccess("/admin/event-sources/" + eventSource.getId(), EventSource.class);
        assertNotNull(dbSource);
        assertTrue(dbSource.isActive());
        assertNotNull(doGetSuccess("/games/" + stackId + "/elements/" + TESTBADGE + "?withData=false", ElementDef.class));
        assertNotNull(doGetSuccess("/games/" + stackId + "/elements/" + TESTPOINT + "?withData=false", ElementDef.class));

        // game stopped message should dispatch
        ArgumentCaptor<Object> eventArgumentCaptor = ArgumentCaptor.forClass(Object.class);
        Mockito.verify(spy, Mockito.times(1)).publishEvent(eventArgumentCaptor.capture());
        {
            var value = (GameSpecChangedEvent) eventArgumentCaptor.getAllValues().get(0);
            Assertions.assertEquals(EntityChangeType.REMOVED, value.getChangeType());
            Assertions.assertEquals(stackId, value.getGameId());
        }
    }


    @Test
    void deleteNonExistingGame() {
        int stackId = doPostSuccess("/games", stackOverflow, Game.class).getId();
        doGetSuccess("/games/" + stackId, Game.class);

        doDeletetError("/games/999999", HttpStatus.NOT_FOUND, ErrorCodes.GAME_NOT_EXISTS);
    }

    @Test
    void updateGameStatus() {
        ApplicationEventPublisher spy = Mockito.spy(eventPublisher);
        if (gameService instanceof GameService) {
            // we know this is the service
            ((GameService) gameService).setPublisher(spy);
        }

        int stackId = doPostSuccess("/games", stackOverflow, Game.class).getId();
        ArgumentCaptor<GameStatusChangeEvent> eventArgumentCaptor = ArgumentCaptor.forClass(GameStatusChangeEvent.class);

        Mockito.reset(spy);
        sleepSafe(10);
        Game gameRef = doPutSuccess("/games/" + stackId + "/start", null, Game.class);
        assertEngineManagerOnceCalledWithState(spy, GameState.STARTED, gameRef, eventArgumentCaptor);

        Mockito.reset(spy);
        sleepSafe(10);
        gameRef = doPutSuccess("/games/" + stackId + "/stop", null, Game.class);
        assertEngineManagerOnceCalledWithState(spy, GameState.STOPPED, gameRef, eventArgumentCaptor);

        Mockito.reset(spy);
        sleepSafe(10);
        gameRef = doPutSuccess("/games/" + stackId + "/pause", null, Game.class);
        assertEngineManagerOnceCalledWithState(spy, GameState.PAUSED, gameRef, eventArgumentCaptor);

        assertCurrentGameStatus(stackId, "PAUSED");

        doPutError("/games/" + stackId + "/null", null, HttpStatus.BAD_REQUEST, ErrorCodes.GAME_UNKNOWN_STATE);
        doPutError("/games/" + stackId + "/", null, HttpStatus.NOT_FOUND, null);
        doPutError("/games/" + stackId + "/hello", null, HttpStatus.BAD_REQUEST, ErrorCodes.GAME_UNKNOWN_STATE);

        assertCurrentGameStatus(stackId, "PAUSED");
    }

    @Test
    void testGameStatusHistory() {
        int stackId = doPostSuccess("/games", stackOverflow, Game.class).getId();

        sleepSafe(10);
        doPutSuccess("/games/" + stackId + "/start", null, Game.class);
        sleepSafe(10);
        doPutSuccess("/games/" + stackId + "/pause", null, Game.class);

        assertCurrentGameStatus(stackId, "PAUSED");
        {
            var list = doGetListSuccess("/games/" + stackId + "/status/history", GameStatus.class);
            List<String> actual = list.stream()
                    .sorted(Comparator.comparingLong(GameStatus::getUpdatedAt))
                    .map(GameStatus::getStatus).collect(Collectors.toList());
            List<String> expected = List.of("CREATED", "STARTED", "PAUSED");
            assertEquals(expected, actual);
        }

        sleepSafe(10);
        doPutSuccess("/games/" + stackId + "/start", null, Game.class);
        assertCurrentGameStatus(stackId, "STARTED");
        {
            var list = doGetListSuccess("/games/" + stackId + "/status/history", GameStatus.class);
            List<String> actual = list.stream()
                    .sorted(Comparator.comparingLong(GameStatus::getUpdatedAt))
                    .map(GameStatus::getStatus).collect(Collectors.toList());
            List<String> expected = List.of("CREATED", "STARTED", "PAUSED", "STARTED");
            assertEquals(expected, actual);
        }
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

    private void assertCurrentGameStatus(int gameId, String expectedStatus) {
        var status = doGetSuccess("/games/" + gameId + "/status", GameStatus.class);
        assertEquals(expectedStatus, status.getStatus());
    }

    private void assertGame(Game db, GameCreateRequest other) {
        assertTrue(db.getId() > 0);
        assertEquals(other.getName(), db.getName());
        assertEquals(other.getDescription(), db.getDescription());
        assertEquals(other.getLogoRef(), db.getLogoRef());
        assertEquals(other.getMotto(), db.getMotto());
        if (other.getStartTime() == null) {
            assertEquals(db.getStartTime(), db.getCreatedAt());
        }
        if (other.getEndTime() == null) {
            assertEquals(db.getEndTime(), LocalDate.of(3001, 1, 1).atStartOfDay()
                    .toInstant(ZoneOffset.UTC).toEpochMilli());
        }
    }

    private void assertGame(Game db, GameUpdateRequest other) {
        assertTrue(db.getId() > 0);
        if (other.getDescription() != null) {
            assertEquals(other.getDescription(), db.getDescription());
        }
        if (other.getLogoRef() != null) {
            assertEquals(other.getLogoRef(), db.getLogoRef());
        }
        if (other.getMotto() != null) {
            assertEquals(other.getMotto(), db.getMotto());
        }
        if (other.getStartTime() != null) {
            assertEquals(other.getStartTime(), db.getStartTime());
        } else {
            assertTrue(db.getStartTime() >= 0);
        }
        if (other.getEndTime() != null) {
            assertEquals(other.getEndTime(), db.getEndTime());
        } else {
            assertTrue(db.getEndTime() >= 0);
        }
    }

    private void assertThirdPartyCacheClearanceForGame(int gameId, EntityChangeType changeType) {
        ArgumentCaptor<GameSpecChangedEvent> captor = ArgumentCaptor.forClass(GameSpecChangedEvent.class);
        Mockito.verify(cacheClearanceListener, Mockito.times(1))
                .handleGameUpdateEvent(captor.capture());
        assertEquals(gameId, captor.getValue().getGameId());
        assertEquals(EntityChangeType.MODIFIED, captor.getValue().getChangeType());
    }
}
