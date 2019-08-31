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

package io.github.oasis.services.admin.json.game;

import io.github.oasis.services.admin.domain.GameState;
import io.github.oasis.services.admin.internal.dto.NewGameDto;

/**
 * @author Isuru Weerarathna
 */
public class GameJson {

    private int id;
    private String name;
    private String description;

    private GameState currentState;

    public static GameJson from(int id, NewGameDto gameDto, GameState state) {
        GameJson json = new GameJson();
        json.setId(id);
        json.setName(gameDto.getName());
        json.setDescription(gameDto.getDescription());
        json.setCurrentState(state);
        return json;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public GameState getCurrentState() {
        return currentState;
    }

    public void setCurrentState(GameState currentState) {
        this.currentState = currentState;
    }
}
