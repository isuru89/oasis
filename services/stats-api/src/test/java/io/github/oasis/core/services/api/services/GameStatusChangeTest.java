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
import io.github.oasis.core.elements.SimpleElementDefinition;
import io.github.oasis.core.external.EventDispatcher;
import io.github.oasis.core.external.messages.GameState;
import io.github.oasis.core.services.api.beans.BackendRepository;
import io.github.oasis.core.services.api.beans.jdbc.JdbcRepository;
import io.github.oasis.core.services.api.controllers.admin.ElementsController;
import io.github.oasis.core.services.api.controllers.admin.GamesController;
import io.github.oasis.core.services.api.dao.IElementDao;
import io.github.oasis.core.services.api.dao.IGameDao;
import io.github.oasis.core.services.api.to.ElementCreateRequest;
import io.github.oasis.core.services.api.to.GameCreateRequest;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Map;

/**
 * @author Isuru Weerarathna
 */
public class GameStatusChangeTest extends AbstractServiceTest {

    private IEngineManager engineManager;

    private final EventDispatcher dispatcher = Mockito.mock(EventDispatcher.class);

    private GamesController gamesController;
    private ElementsController elementsController;

    private final ElementCreateRequest samplePoint = ElementCreateRequest.builder()
            .gameId(1)
            .type("core:point")
            .metadata(new SimpleElementDefinition("test.point1", "Star points", "blah blah blah"))
            .data(Map.of("f1", "v1", "f2", "v2"))
            .build();

    private final ElementCreateRequest sampleBadge = ElementCreateRequest.builder()
            .gameId(1)
            .type("core:badge")
            .metadata(new SimpleElementDefinition("test.badge", "Mega badge", "another description"))
            .data(Map.of("f3", "v3", "f4", "v4"))
            .build();

    private final GameCreateRequest stackOverflow = GameCreateRequest.builder()
            .name("Stack-overflow")
            .description("Stackoverflow badges and points system")
            .logoRef("https://oasis.io/assets/so.jpeg")
            .motto("Help the community")
            .build();


    @Test
    void updateGameStatusWithElements() throws Exception {
        int stackId = gamesController.addGame(stackOverflow).getId();
        elementsController.add(stackId, samplePoint);
        elementsController.add(stackId, sampleBadge);

        Mockito.reset(dispatcher);
        gamesController.updateGameStatus(stackId, "start");
        Mockito.verify(dispatcher,
                Mockito.times(4)).broadcast(Mockito.any());

        Mockito.reset(dispatcher);
        gamesController.updateGameStatus(stackId, "stop");
        Mockito.verify(dispatcher,
                Mockito.times(1)).broadcast(Mockito.any());
    }

    private void assertEngineManagerOnceCalledWithState(GameState state, Game game) {
        Mockito.verify(engineManager,
                Mockito.times(1)).changeGameStatus(state, game);
    }

    @Override
    protected JdbcRepository createJdbcRepository(Jdbi jdbi) {
        return new JdbcRepository(
                jdbi.onDemand(IGameDao.class),
                null,
                jdbi.onDemand(IElementDao.class),
                null,
                serializationSupport
        );
    }

    @Override
    protected void createServices(BackendRepository backendRepository) {
        ElementService elementService = new ElementService(backendRepository);
        EngineManagerImpl manager = new EngineManagerImpl(null, elementService);
        manager.setDispatchSupport(dispatcher);
        engineManager = manager;
        gamesController = new GamesController(new GameService(backendRepository, engineManager));
        elementsController = new ElementsController(elementService);
    }
}
