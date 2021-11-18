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
import io.github.oasis.core.external.messages.GameState;
import io.github.oasis.core.services.api.TestUtils;
import io.github.oasis.core.services.api.services.impl.GameService;
import io.github.oasis.core.services.api.to.ElementCreateRequest;
import io.github.oasis.core.services.api.to.GameCreateRequest;
import io.github.oasis.core.services.events.GameStatusChangeEvent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;

/**
 * @author Isuru Weerarathna
 */
public class GameStatusChangeTest extends AbstractServiceTest {

    @Autowired
    private IGameService gameService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    private final GameCreateRequest stackOverflow = GameCreateRequest.builder()
            .name("Stack-overflow")
            .description("Stackoverflow badges and points system")
            .logoRef("https://oasis.io/assets/so.jpeg")
            .motto("Help the community")
            .build();

    @Test
    void updateGameStatusWithElements() {
        ApplicationEventPublisher spy = Mockito.spy(eventPublisher);
        if (gameService instanceof GameService) {
            // we know this is the service
            ((GameService) gameService).setPublisher(spy);
        }

        int stackId = doPostSuccess("/games", stackOverflow, Game.class).getId();
        ArgumentCaptor<GameStatusChangeEvent> eventArgumentCaptor = ArgumentCaptor.forClass(GameStatusChangeEvent.class);

        List<ElementCreateRequest> elementCreateRequests = TestUtils.parseElementRules("rules.yml", stackId);
        ElementCreateRequest samplePoint = TestUtils.findById("testpoint", elementCreateRequests);
        ElementCreateRequest sampleBadge = TestUtils.findById("testbadge", elementCreateRequests);

        doPostSuccess("/games/" + stackId + "/elements", samplePoint, ElementDef.class);
        doPostSuccess("/games/" + stackId + "/elements", sampleBadge, ElementDef.class);

        Mockito.reset(spy);
        doPutSuccess("/games/" + stackId + "/start", null, Game.class);
        Mockito.verify(spy,
                Mockito.times(1)).publishEvent(eventArgumentCaptor.capture());
        GameStatusChangeEvent capturedEvent = eventArgumentCaptor.getValue();
        Assertions.assertEquals(GameState.STARTED, capturedEvent.getNewGameState());


        Mockito.reset(spy);
        doPutSuccess("/games/" + stackId + "/stop", null, Game.class);
        Mockito.verify(spy,
                Mockito.times(1)).publishEvent(eventArgumentCaptor.capture());
        Assertions.assertEquals(GameState.STOPPED, eventArgumentCaptor.getValue().getNewGameState());
    }

}
