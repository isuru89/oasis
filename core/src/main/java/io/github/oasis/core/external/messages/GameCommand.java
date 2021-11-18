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

package io.github.oasis.core.external.messages;

import io.github.oasis.core.Game;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Base command class for every game related commands.
 * This command is being used to signal game status changes, such as,
 * START, PAUSE, REMOVE, etc.
 *
 * {@link GameLifecycle} to view available game statuses.
 *
 * @author Isuru Weerarathna
 */
@Getter
@Setter
@ToString
public class GameCommand implements OasisCommand {

    private int gameId;
    private Object messageId;
    private GameLifecycle status;
    private Game game;

    public static GameCommand create(int gameId, GameLifecycle status) {
        GameCommand cmd = new GameCommand();
        cmd.setGameId(gameId);
        cmd.setStatus(status);
        return cmd;
    }

    public static GameCommand create(int gameId, GameLifecycle status, Game gameObject) {
        GameCommand cmd = new GameCommand();
        cmd.setGameId(gameId);
        cmd.setStatus(status);
        cmd.setGame(gameObject);
        return cmd;
    }

    /**
     * Life cycle statuses of a game.
     */
    public enum GameLifecycle {
        CREATE,
        START,
        PAUSE,
        UPDATE,
        REMOVE;

        public static GameState convertTo(GameLifecycle lifecycle) {
            switch (lifecycle) {
                case CREATE: return GameState.CREATED;
                case START: return GameState.STARTED;
                case REMOVE: return GameState.STOPPED;
                case PAUSE: return GameState.PAUSED;
                case UPDATE: return GameState.UPDATED;
                default: return null;
            }
        }
    }
}
