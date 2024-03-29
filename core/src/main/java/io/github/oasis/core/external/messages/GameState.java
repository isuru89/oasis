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

/**
 * Indicates all life cycle state of a game.
 *
 * @author Isuru Weerarathna
 */
public enum GameState {

    CREATED(EngineMessage.GAME_CREATED),
    STARTED(EngineMessage.GAME_STARTED),
    PAUSED(EngineMessage.GAME_PAUSED),
    UPDATED(EngineMessage.GAME_UPDATED),
    STOPPED(EngineMessage.GAME_REMOVED);

    private final String command;

    GameState(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }

}
