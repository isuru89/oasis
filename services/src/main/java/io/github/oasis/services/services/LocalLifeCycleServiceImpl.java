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

package io.github.oasis.services.services;

import io.github.oasis.model.defs.ChallengeDef;
import io.github.oasis.model.defs.GameDef;
import io.github.oasis.services.configs.OasisConfigurations;
import io.github.oasis.services.exception.InputValidationException;
import io.github.oasis.services.services.control.LocalScheduler;
import io.github.oasis.services.utils.Checks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.Future;

/**
 * @author iweerarathna
 */
@Service("localLifecycleService")
public class LocalLifeCycleServiceImpl implements ILifecycleService {

    private final LocalScheduler gameController;
    private final IGameDefService gameDefService;
    private final OasisConfigurations configurations;

    @Autowired
    public LocalLifeCycleServiceImpl(LocalScheduler gameController, IGameDefService gameDefService,
                                     OasisConfigurations configurations) {
        this.gameController = gameController;
        this.gameDefService = gameDefService;
        this.configurations = configurations;
    }

    @Override
    public Future<?> start(long gameId) throws Exception {
        Checks.greaterThanZero(gameId, "gameId");
        GameDef gameDef = gameDefService.readGame(gameId);

        return gameController.startGame(gameDef.getId());
    }

    @Override
    public boolean stop(long gameId) throws Exception {
        Checks.greaterThanZero(gameId, "gameId");
        gameController.stopGame(gameId);
        return true;
    }

    @Override
    public boolean startChallenge(long challengeId) throws Exception {
        Checks.greaterThanZero(challengeId, "challengeId");

        ChallengeDef challengeDef = gameDefService.readChallenge(challengeId);
        if (challengeDef == null) {
            throw new InputValidationException("No challenge is found by id " + challengeId + "!");
        }
        gameController.startChallenge(challengeDef);
        return true;
    }

    @Override
    public boolean stopChallenge(long challengeId) throws Exception {
        Checks.greaterThanZero(challengeId, "challengeId");

        ChallengeDef challengeDef = gameDefService.readChallenge(challengeId);
        if (challengeDef == null) {
            throw new InputValidationException("No challenge is found by id " + challengeId + "!");
        }
        gameController.stopChallenge(challengeDef);
        return true;
    }

    @Override
    public boolean resumeGame(long gameId) throws Exception {
        Checks.greaterThanZero(gameId, "gameId");

        GameDef gameDef = gameDefService.readGame(gameId);
        if (gameDef == null) {
            throw new InputValidationException("No game is found by id " + gameId + "!");
        }
        gameController.resumeGame(gameDef);
        return true;
    }

    @Override
    public boolean resumeChallenge(long challengeId) throws Exception {
        Checks.greaterThanZero(challengeId, "challengeId");

        ChallengeDef challengeDef = gameDefService.readChallenge(challengeId);
        if (challengeDef == null) {
            throw new InputValidationException("No challenge is found by id " + challengeId + "!");
        }
        gameController.resumeChallenge(challengeDef);
        return true;
    }
}
