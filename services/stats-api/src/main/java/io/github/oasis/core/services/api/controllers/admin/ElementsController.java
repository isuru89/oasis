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

import io.github.oasis.core.elements.ElementDef;
import io.github.oasis.core.services.annotations.ForCurator;
import io.github.oasis.core.services.annotations.ForPlayer;
import io.github.oasis.core.services.api.controllers.AbstractController;
import io.github.oasis.core.services.api.services.impl.ElementService;
import io.github.oasis.core.services.api.to.ElementCreateRequest;
import io.github.oasis.core.services.api.to.ElementUpdateRequest;
import io.github.oasis.core.services.exceptions.OasisApiException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * @author Isuru Weerarathna
 */
@RestController
@RequestMapping(
    produces = MediaType.APPLICATION_JSON_VALUE
)
@Tag(name = "Game Elements", description = "Game elements API")
public class ElementsController extends AbstractController {

    private final ElementService elementService;

    public ElementsController(ElementService elementService) {
        this.elementService = elementService;
    }

    @Operation(
            summary = "Gets a game element by its id"
    )
    @ForPlayer
    @GetMapping(path = "/games/{gameId}/elements/{elementId}")
    public ElementDef read(@PathVariable("gameId") Integer gameId,
                           @PathVariable("elementId") String elementId,
                           @RequestParam(name = "withData", defaultValue = "false") Boolean withData) throws OasisApiException {
        return elementService.readElement(gameId, elementId, withData);
    }

    @Operation(
            summary = "Adds a new game element",
            tags = {"admin", "curator"}
    )
    @ForCurator
    @PostMapping(path = "/games/{gameId}/elements", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ElementDef add(@PathVariable("gameId") Integer gameId,
                          @Valid @RequestBody ElementCreateRequest request) {
        return elementService.addElement(gameId, request);
    }

    @Operation(
            summary = "Gets all active elements of a game",
            tags = {"player"}
    )
    @ForCurator
    @GetMapping(path = "/games/{gameId}/elements")
    public List<ElementDef> getElementsOfGame(@PathVariable("gameId") Integer gameId,
                                              @RequestParam(name = "types", required = false) Set<String> types) {
        return elementService.listElementsByTypes(gameId, types);
    }

    @Operation(
            summary = "Updates metadata of an existing game element",
            tags = {"admin", "curator"}
    )
    @ForCurator
    @PatchMapping(path = "/games/{gameId}/elements/{elementId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ElementDef update(@PathVariable("gameId") Integer gameId,
                             @PathVariable("elementId") String elementId,
                             @Valid @RequestBody ElementUpdateRequest updateRequest) {
        return elementService.updateElement(gameId, elementId, updateRequest);
    }

    @Operation(
            summary = "Deletes an existing game element",
            tags = {"admin", "curator"}
    )
    @ForCurator
    @DeleteMapping(path = "/games/{gameId}/elements/{elementId}")
    public ElementDef delete(@PathVariable("gameId") Integer gameId,
                             @PathVariable("elementId") String elementId) {
        return elementService.deleteElement(gameId, elementId);
    }
}
