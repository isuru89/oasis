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
 */

package io.github.oasis.core.services.api.services.impl;

import io.github.oasis.core.Game;
import io.github.oasis.core.configs.OasisConfigs;
import io.github.oasis.core.external.EngineManagerSubscription;
import io.github.oasis.core.external.EventDispatcher;
import io.github.oasis.core.external.messages.EngineStatusChangedMessage;
import io.github.oasis.core.external.messages.GameState;
import io.github.oasis.core.services.api.services.IGameService;
import io.github.oasis.core.services.exceptions.OasisApiException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

public class EngineManagerImplTest {

    private static final int GAME_ID = 1;
    private static final Game TEST_GAME = Game.builder()
            .id(GAME_ID)
            .name("Game-Test")
            .description("Test Game description")
            .logoRef("https://images.oasis.io/games/" + GAME_ID)
            .motto("All the way")
            .active(true)
            .createdAt(System.currentTimeMillis())
            .updatedAt(System.currentTimeMillis()).build();

    private EngineManagerImpl engineManager;
    private ElementService elementService;
    private IGameService gameService;
    private EventDispatcher eventDispatcher;
    private MockedEngineSubscription subscription;


    @BeforeEach
    void beforeEach() {
        elementService = Mockito.mock(ElementService.class);
        gameService = Mockito.mock(IGameService.class);
        eventDispatcher = Mockito.mock(EventDispatcher.class);
        subscription = new MockedEngineSubscription();

        Mockito.when(gameService.readGame(Mockito.eq(GAME_ID))).thenReturn(TEST_GAME);
        engineManager = new EngineManagerImpl(elementService, gameService, eventDispatcher, subscription);
        engineManager.beforeInitialized();
    }

    @Test
    void changeGameStatus() {

    }

    @Test
    void testGameEventConsumeGameExists() throws OasisApiException {
        ArgumentCaptor<String> newGameStatusCapture = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Integer> gameIdCapture = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Long> updatedAtCapture = ArgumentCaptor.forClass(Long.class);

        var msg = new EngineStatusChangedMessage(GAME_ID, GameState.PAUSED, "engine-abcd", 123L, null);
        subscription.invoke(msg);

        Mockito.verify(gameService, Mockito.times(1))
                .changeStatusOfGameWithoutPublishing(gameIdCapture.capture(), newGameStatusCapture.capture(), updatedAtCapture.capture());
        assertEquals(gameIdCapture.getValue(), GAME_ID);
        assertEquals(newGameStatusCapture.getValue(), "paused");
        assertEquals(updatedAtCapture.getValue(), 123L);
    }

    @Test
    void testGameEventConsumeGameNotExists() throws OasisApiException {
        var msg = new EngineStatusChangedMessage(9999, GameState.STOPPED, "engine-abcd", 234L, null);
        subscription.invoke(msg);

        Mockito.verify(gameService, Mockito.never())
                .changeStatusOfGameWithoutPublishing(Mockito.anyInt(), Mockito.anyString(), Mockito.anyLong());
    }

    @Test
    void initEngineStatusSubscription() {
        subscription = Mockito.mock(MockedEngineSubscription.class);
        engineManager = new EngineManagerImpl(elementService, gameService, eventDispatcher, subscription);
        engineManager.beforeInitialized();
        Mockito.verify(subscription, Mockito.times(1)).subscribe(Mockito.any());

        Mockito.clearInvocations(subscription);
        engineManager = new EngineManagerImpl(elementService, gameService, eventDispatcher, null);
        Mockito.verify(subscription, Mockito.never()).subscribe(Mockito.any());
    }

    @Test
    void close() {
        try {
            new EngineManagerImpl(elementService, gameService, eventDispatcher, null).close();
        } catch (IOException e) {
            Assertions.fail("Should not expected to fail!");
        }

        try {
            new EngineManagerImpl(elementService, gameService, eventDispatcher, subscription).close();
        } catch (IOException e) {
            Assertions.fail("Should not expected to fail!");
        }
    }

    private static class MockedEngineSubscription implements EngineManagerSubscription {

        private Consumer<EngineStatusChangedMessage> consumer;

        @Override
        public void init(OasisConfigs configs) {

        }

        @Override
        public void subscribe(Consumer<EngineStatusChangedMessage> engineStatusChangedMessageConsumer) {
            this.consumer = engineStatusChangedMessageConsumer;
        }

        void invoke(EngineStatusChangedMessage msg) {
            this.consumer.accept(msg);
        }

        @Override
        public void close() {

        }
    }
}