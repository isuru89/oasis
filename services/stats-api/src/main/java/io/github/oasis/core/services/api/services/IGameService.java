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
 *
 */

package io.github.oasis.core.services.api.services;

import io.github.oasis.core.Game;
import io.github.oasis.core.external.PaginatedResult;
import io.github.oasis.core.model.GameStatus;
import io.github.oasis.core.services.api.to.GameCreateRequest;
import io.github.oasis.core.services.api.to.GameUpdateRequest;
import io.github.oasis.core.services.exceptions.OasisApiException;

import java.util.List;

/**
 * Represents the functionality of the game related activities.
 *
 * @author Isuru Weerarathna
 */
public interface IGameService {

    Game addGame(GameCreateRequest gameCreateRequest) throws OasisApiException;

    Game updateGame(int gameId, GameUpdateRequest updateRequest);

    Game deleteGame(int gameId) throws OasisApiException;

    Game readGame(int gameId);

    PaginatedResult<Game> listAllGames(String offset, int pageSize);

    Game getGameByName(String name);

    Game changeStatusOfGameWithoutPublishing(int gameId, String newStatus, Long updatedAt) throws OasisApiException;

    Game changeStatusOfGame(int gameId, String newStatus, Long updatedAt) throws OasisApiException;

    GameStatus getCurrentGameStatus(int gameId);

    List<GameStatus> listGameStatusHistory(int gameId, Long startFrom, Long endTo);
}
