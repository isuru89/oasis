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

package io.github.oasis.services.admin.controller;

import io.github.oasis.services.common.security.AllowedRoles;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static io.github.oasis.services.admin.internal.EndPoints.GAME.GAME;
import static io.github.oasis.services.admin.internal.EndPoints.GAME.GAME_ID;
import static io.github.oasis.services.admin.internal.EndPoints.GAME.PAUSE;
import static io.github.oasis.services.admin.internal.EndPoints.GAME.RESTART;
import static io.github.oasis.services.admin.internal.EndPoints.GAME.START;
import static io.github.oasis.services.admin.internal.EndPoints.GAME.STOP;

/**
 * @author Isuru Weerarathna
 */
@RestController
@RequestMapping(GAME)
public class GameController {

    @PreAuthorize(AllowedRoles.ONLY_ADMIN)
    @PostMapping(START)
    public void startGame(@PathVariable(GAME_ID) int gameId) {

    }

    @PreAuthorize(AllowedRoles.ONLY_ADMIN)
    @PostMapping(STOP)
    public void stopGame(@PathVariable(GAME_ID) int gameId) {

    }

    @PreAuthorize(AllowedRoles.ONLY_ADMIN)
    @PostMapping(PAUSE)
    public void pauseGame(@PathVariable(GAME_ID) int gameId) {

    }

    @PreAuthorize(AllowedRoles.ONLY_ADMIN)
    @PostMapping(RESTART)
    public void restartGame(@PathVariable(GAME_ID) int gameId) {

    }

}
