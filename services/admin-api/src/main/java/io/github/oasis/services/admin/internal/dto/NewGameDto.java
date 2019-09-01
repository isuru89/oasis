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

package io.github.oasis.services.admin.internal.dto;

import io.github.oasis.services.admin.domain.GameState;
import io.github.oasis.services.admin.internal.ErrorCodes;
import io.github.oasis.services.common.OasisValidationException;
import io.github.oasis.services.common.Validation;

/**
 * @author Isuru Weerarathna
 */
public class NewGameDto {

    private String name;
    private String description;

    private GameState currentState = GameState.CREATED;

    public NewGameDto() {
    }

    public NewGameDto(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public void validate() {
        if (Validation.isEmpty(name)) {
            throw new OasisValidationException(ErrorCodes.INVALID_GAME_DETAILS, "Game 'name' is mandatory!");
        }
    }

    public GameState getCurrentState() {
        return currentState;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

}
