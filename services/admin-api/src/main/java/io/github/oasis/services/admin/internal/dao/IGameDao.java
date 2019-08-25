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

package io.github.oasis.services.admin.internal.dao;

import io.github.oasis.services.admin.domain.GameState;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.sqlobject.transaction.Transaction;

import java.util.Optional;

/**
 * @author Isuru Weerarathna
 */
public interface IGameDao {

    @SqlUpdate("UPDATE OA_GAME_DEF SET is_active = false WHERE game_id = :id")
    int deactivateGame(@Bind("id") int gameId);

    int pauseGame(@Bind("id") int gameId);

    int stopGame(@Bind("id") int gameId);

    int startGame(@Bind("id") int gameId);

    @SqlQuery("SELECT current_state FROM OA_GAME_STATE WHERE game_id = :id ORDER BY changed_at DESC LIMIT 1")
    Optional<GameState> readCurrentGameState(@Bind("id") int gameId);

    @Transaction
    default void removeGame(@Bind("id") int gameId) {
        GameState currentState = readCurrentGameState(gameId).orElse(GameState.CREATED);

        if (GameState.canDeactivateState(currentState)) {
            deactivateGame(gameId);
        } else {

        }
    }

}
