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

package io.github.oasis.services.services.control;

import io.github.oasis.model.defs.ChallengeDef;
import io.github.oasis.model.defs.GameDef;
import io.github.oasis.services.model.IEventDispatcher;
import io.github.oasis.services.model.IGameController;
import io.github.oasis.services.services.dispatchers.DispatcherManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * @author iweerarathna
 */
@Component("schedulerRemote")
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class FlinkScheduler implements IGameController {

    private IEventDispatcher eventDispatcher;

    @Autowired
    public FlinkScheduler(DispatcherManager dispatcherManager) {
        this.eventDispatcher = dispatcherManager.getEventDispatcher();
    }

    @Override
    public void submitEvent(long gameId, String token, Map<String, Object> event) throws Exception {
        eventDispatcher.dispatch(gameId, event);
    }

    @Override
    public Future<?> startGame(long gameId) throws Exception {
        return null;
    }

    @Override
    public void startChallenge(ChallengeDef challengeDef) throws Exception {

    }

    @Override
    public void stopChallenge(ChallengeDef challengeDef) throws Exception {

    }

    @Override
    public void stopGame(long gameId) {

    }

    @Override
    public void resumeChallenge(ChallengeDef challengeDef) throws Exception {

    }

    @Override
    public void resumeGame(GameDef gameDef) throws Exception {

    }

    @Override
    public void close() throws IOException {

    }
}
