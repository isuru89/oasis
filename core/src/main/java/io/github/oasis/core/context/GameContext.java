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

package io.github.oasis.core.context;

import java.io.Serializable;

/**
 * @author Isuru Weerarathna
 */
public class GameContext implements Serializable {

    public static final GameContext NOT_FOUND = new GameContext(-1, Long.MAX_VALUE);

    private int gameId;
    private long startTime;

    public GameContext(int gameId) {
        this.gameId = gameId;
    }

    private GameContext(int gameId, long startTime) {
        this.gameId = gameId;
        this.startTime = startTime;
    }

    public boolean fallsWithinGamePeriod(long ts) {
        return startTime <= ts;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public int getGameId() {
        return gameId;
    }
}
