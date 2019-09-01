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

package io.github.oasis.services.admin.domain;

import io.github.oasis.services.admin.internal.dao.IGameCreationDao;
import io.github.oasis.services.admin.internal.dto.NewGameDto;
import io.github.oasis.services.admin.json.game.GameJson;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Isuru Weerarathna
 */
@Component
public class Game {

    private IGameCreationDao gameCreationDao;

    public Game(IGameCreationDao gameCreationDao) {
        this.gameCreationDao = gameCreationDao;
    }

    public GameJson createGame(NewGameDto game) {
        int gameId = gameCreationDao.insertGame(game);
        return GameJson.from(gameId, game, GameState.CREATED);
    }

    public List<GameJson> readAllGames() {
        return gameCreationDao.readAllGames();
    }

}
