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

package io.github.oasis.services.controllers;

import io.github.oasis.services.dto.StatusResponse;
import io.github.oasis.services.services.ILifecycleService;
import io.github.oasis.services.services.LifecycleImplManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/control")
public class ControlController {

    private ILifecycleService lifecycleService;

    @Autowired
    public ControlController(LifecycleImplManager lifecycleImplHolder) {
        this.lifecycleService = lifecycleImplHolder.get();
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/game/{gameId}/start")
    public StatusResponse startGame(@PathVariable("gameId") long gameId) throws Exception {
        return new StatusResponse(lifecycleService.start(gameId) != null);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/game/{gameId}/stop")
    public StatusResponse stopGame(@PathVariable("gameId") long gameId) throws Exception {
        return new StatusResponse(lifecycleService.stop(gameId));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/challenge/{challengeId}/start")
    public StatusResponse startChallenge(@PathVariable("challengeId") long challengeId) throws Exception {
        return new StatusResponse(lifecycleService.startChallenge(challengeId));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/challenge/{challengeId}/stop")
    public StatusResponse stopChallenge(@PathVariable("challengeId") long challengeId) throws Exception {
        return new StatusResponse(lifecycleService.stop(challengeId));
    }
}
