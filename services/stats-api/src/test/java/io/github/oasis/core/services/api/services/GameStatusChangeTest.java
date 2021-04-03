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
import io.github.oasis.core.configs.OasisConfigs;
import io.github.oasis.core.exception.OasisException;
import io.github.oasis.core.external.Db;
import io.github.oasis.core.external.EventDispatcher;
import io.github.oasis.core.external.messages.GameState;
import io.github.oasis.core.services.api.TestUtils;
import io.github.oasis.core.services.api.beans.BackendRepository;
import io.github.oasis.core.services.api.beans.StatsApiContext;
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

import java.util.List;

/**
 * @author Isuru Weerarathna
 */
public class GameStatusChangeTest extends AbstractServiceTest {

    private IEngineManager engineManager;

    private final EventDispatcher dispatcher = Mockito.mock(EventDispatcher.class);

    private GamesController gamesController;
    private ElementsController elementsController;

    private StatsApiContext statsApiContext;

    private final GameCreateRequest stackOverflow = GameCreateRequest.builder()
            .name("Stack-overflow")
            .description("Stackoverflow badges and points system")
            .logoRef("https://oasis.io/assets/so.jpeg")
            .motto("Help the community")
            .build();

    @Test
    void updateGameStatusWithElements() throws Exception {
        int stackId = gamesController.addGame(stackOverflow).getId();

        List<ElementCreateRequest> elementCreateRequests = TestUtils.parseElementRules("rules.yml", stackId);
        ElementCreateRequest samplePoint = TestUtils.findById("testpoint", elementCreateRequests);
        ElementCreateRequest sampleBadge = TestUtils.findById("testbadge", elementCreateRequests);

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
    protected void prepareContext(Db dbPool, OasisConfigs configs) throws OasisException {
        statsApiContext = new StatsApiContext(dbPool, configs);
        statsApiContext.init();
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
        ElementService elementService = new ElementService(backendRepository, statsApiContext);
        EngineManagerImpl manager = new EngineManagerImpl(null, elementService);
        manager.setDispatchSupport(dispatcher);
        engineManager = manager;
        gamesController = new GamesController(new GameService(backendRepository, engineManager));
        elementsController = new ElementsController(elementService);
    }
}
