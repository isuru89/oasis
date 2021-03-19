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
import io.github.oasis.core.elements.SimpleElementDefinition;
import io.github.oasis.core.external.EventDispatcher;
import io.github.oasis.core.external.messages.GameState;
import io.github.oasis.core.services.api.beans.BackendRepository;
import io.github.oasis.core.services.api.beans.jdbc.JdbcRepository;
import io.github.oasis.core.services.api.controllers.admin.ElementsController;
import io.github.oasis.core.services.api.controllers.admin.GamesController;
import io.github.oasis.core.services.api.dao.IElementDao;
import io.github.oasis.core.services.api.dao.IGameDao;
import io.github.oasis.core.services.api.to.GameObjectRequest;
import io.github.oasis.elements.badges.BadgeDef;
import io.github.oasis.engine.element.points.PointDef;
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

    private final ElementDef samplePoint = ElementDef.builder()
            .elementId("test.point1")
            .gameId(1)
            .impl(PointDef.class.getName())
            .type("point")
            .metadata(new SimpleElementDefinition("test.point1", "Star points", "blah blah blah"))
            .data(Map.of("f1", "v1", "f2", "v2"))
            .build();

    private final ElementDef sampleBadge = ElementDef.builder()
            .elementId("test.badge")
            .gameId(1)
            .impl(BadgeDef.class.getName())
            .type("badge")
            .metadata(new SimpleElementDefinition("test.badge", "Mega badge", "another description"))
            .data(Map.of("f3", "v3", "f4", "v4"))
            .build();

    private final GameObjectRequest stackOverflow = GameObjectRequest.builder()
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
