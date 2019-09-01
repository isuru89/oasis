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

package io.github.oasis.services.model;

import io.github.oasis.model.defs.ChallengeDef;
import io.github.oasis.model.defs.GameDef;

import java.io.Closeable;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * @author iweerarathna
 */
public interface IGameController extends Closeable {

    void submitEvent(long gameId, String token, Map<String, Object> event) throws Exception;
    Future<?> startGame(long gameId) throws Exception;
    void startChallenge(ChallengeDef challengeDef) throws Exception;
    void stopChallenge(ChallengeDef challengeDef) throws Exception;
    void stopGame(long gameId) throws Exception;
    void resumeChallenge(ChallengeDef challengeDef) throws Exception;
    void resumeGame(GameDef gameDef) throws Exception;
}
