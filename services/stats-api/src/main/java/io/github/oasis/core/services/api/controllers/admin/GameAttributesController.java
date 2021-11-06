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

package io.github.oasis.core.services.api.controllers.admin;

import io.github.oasis.core.elements.AttributeInfo;
import io.github.oasis.core.services.annotations.ForAdmin;
import io.github.oasis.core.services.annotations.ForPlayer;
import io.github.oasis.core.services.api.controllers.AbstractController;
import io.github.oasis.core.services.api.services.impl.GameRankingService;
import io.github.oasis.core.services.api.to.GameAttributeCreateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Isuru Weerarathna
 */
@RestController
@RequestMapping(
        produces = MediaType.APPLICATION_JSON_VALUE
)
@Tag(name = "Attributes", description = "Game Attributes API")
public class GameAttributesController extends AbstractController {

    private final GameRankingService gameRankingService;

    public GameAttributesController(GameRankingService gameRankingService) {
        this.gameRankingService = gameRankingService;
    }

    @Operation(
            summary = "Creates a new element attribute (e.g. gold, silver, platinum) under a game",
            tags = {"admin"}
    )
    @ForAdmin
    @PostMapping(path = "/games/{gameId}/attributes", consumes = MediaType.APPLICATION_JSON_VALUE)
    public AttributeInfo addAttribute(@PathVariable("gameId") Integer gameId,
                                      @RequestBody GameAttributeCreateRequest request) {
        return gameRankingService.addAttribute(gameId, request);
    }

    @Operation(
            summary = "Gets all attributes under a game",
            tags = {"admin"}
    )
    @ForPlayer
    @GetMapping(path = "/games/{gameId}/attributes")
    public List<AttributeInfo> listAttributes(@PathVariable("gameId") Integer gameId) {
        return gameRankingService.listAttributes(gameId);
    }
}
