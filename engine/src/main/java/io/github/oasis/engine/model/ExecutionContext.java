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

package io.github.oasis.engine.model;

/**
 * @author Isuru Weerarathna
 */
public class ExecutionContext {

    private int userTimeOffset;
    private GameContext gameContext;

    public GameContext getGameContext() {
        return gameContext;
    }

    public int getUserTimeOffset() {
        return userTimeOffset;
    }

    public static ExecutionContext withUserTz(int offSet) {
        ExecutionContext context = new ExecutionContext();
        context.userTimeOffset = offSet;
        return context;
    }

    public static Builder from(GameContext gameContext) {
        return new Builder().using(gameContext);
    }

    public static class Builder {
        private ExecutionContext context = new ExecutionContext();

        public ExecutionContext build() {
            return context;
        }

        public Builder using(GameContext gameContext) {
            context.gameContext = gameContext;
            return this;
        }

        public Builder withUserTz(int offSetInSeconds) {
            context.userTimeOffset = offSetInSeconds;
            return this;
        }
    }
}
