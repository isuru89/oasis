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
import io.github.oasis.services.admin.internal.ErrorCodes;
import io.github.oasis.services.admin.internal.exceptions.GameStateChangeException;
import org.jdbi.v3.core.enums.EnumByName;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.sqlobject.transaction.Transaction;

import java.time.Instant;
import java.util.Optional;

/**
 * @author Isuru Weerarathna
 */
@EnumByName
public interface IGameStateDao {

    @SqlUpdate("UPDATE OA_GAME_DEF SET is_active = false, current_state = :state WHERE game_id = :id")
    int deactivateGame(@Bind("id") int gameId,
                       @Bind("state") GameState state);

    @SqlUpdate("INSERT INTO OA_GAME_STATE" +
            " (game_id, prev_state, current_state, changed_at)" +
            " VALUES" +
            " (:id, :prevState, :currState, :changedAt)")
    void insertGameStateChangedRecord(@Bind("id") int gameId,
                                      @Bind("prevState") GameState prevState,
                                      @Bind("currState") GameState currState,
                                      @Bind("changedAt") Instant changedAt);

    @Transaction
    default void pauseGame(int gameId) {
        GameState currentState = readCurrentGameState(gameId).orElse(GameState.CREATED);

        if (GameState.canPauseableState(currentState)) {
            insertGameStateChangedRecord(gameId, currentState, GameState.PAUSED, Instant.now());
            updateCurrentGameState(gameId, GameState.PAUSED);
        } else {
            throw new GameStateChangeException(ErrorCodes.GAME_CANNOT_PAUSE,
                    "Game is currently in '%s' state. Cannot be paused!");
        }
    }

    @Transaction
    default void stopGame(int gameId) {
        GameState currentState = readCurrentGameState(gameId).orElse(GameState.CREATED);

        if (GameState.canStoppableState(currentState)) {
            insertGameStateChangedRecord(gameId, currentState, GameState.STOPPED, Instant.now());
            updateCurrentGameState(gameId, GameState.STOPPED);
        } else {
            throw new GameStateChangeException(ErrorCodes.GAME_CANNOT_STOP,
                    "Game is currently in '%s' state. Cannot be stopped!");
        }
    }

    @Transaction
    default void startGame(int gameId) {
        GameState currentState = readCurrentGameState(gameId).orElse(GameState.CREATED);

        if (GameState.canStartableState(currentState)) {
            insertGameStateChangedRecord(gameId, currentState, GameState.RUNNING, Instant.now());
            updateCurrentGameState(gameId, GameState.RUNNING);
        } else {
            throw new GameStateChangeException(ErrorCodes.GAME_CANNOT_START,
                    "Game is currently in '%s' state. Cannot be started!");
        }
    }

    @SqlQuery("SELECT current_state FROM OA_GAME_STATE WHERE game_id = :id ORDER BY changed_at DESC LIMIT 1")
    Optional<GameState> readCurrentGameState(@Bind("id") int gameId);

    @SqlUpdate("UPDATE OA_GAME_DEF SET current_state = :state WHERE game_id = :id")
    void updateCurrentGameState(@Bind("id") int gameId, @Bind("state") GameState state);

    @Transaction
    default void removeGame(int gameId) throws GameStateChangeException {
        GameState currentState = readCurrentGameState(gameId).orElse(GameState.CREATED);

        if (GameState.canDeactivateState(currentState) && deactivateGame(gameId, GameState.DELETED) > 0) {
            insertGameStateChangedRecord(gameId, currentState, GameState.DELETED, Instant.now());
        } else {
            throw new GameStateChangeException(ErrorCodes.GAME_ALREADY_REMOVED,
                    "Given game is already removed!");
        }
    }

}
