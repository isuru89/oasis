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

import io.github.oasis.core.exception.OasisException;
import io.github.oasis.core.model.EventSource;
import io.github.oasis.core.services.annotations.ForAdmin;
import io.github.oasis.core.services.api.controllers.AbstractController;
import io.github.oasis.core.services.api.services.impl.EventSourceService;
import io.github.oasis.core.services.api.to.EventSourceCreateRequest;
import io.github.oasis.core.services.api.to.EventSourceKeysResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Isuru Weerarathna
 */
@RestController
@RequestMapping(
        produces = MediaType.APPLICATION_JSON_VALUE
)
@Tag(name = "Event-Sources", description = "Event source manipulation API")
public class EventSourceController extends AbstractController {

    private final EventSourceService eventSourceService;

    public EventSourceController(EventSourceService eventSourceService) {
        this.eventSourceService = eventSourceService;
    }

    @Operation(
            summary = "Register a new event source",
            tags = {"admin"}
    )
    @ForAdmin
    @PostMapping(path = "/admin/event-sources", consumes = MediaType.APPLICATION_JSON_VALUE)
    public EventSource registerEventSource(@Valid @RequestBody EventSourceCreateRequest request) throws OasisException {
        return eventSourceService.registerEventSource(request);
    }

    @Operation(
            summary = "Returns all registered event sources of a game",
            tags = {"admin"}
    )
    @ForAdmin
    @GetMapping("/admin/games/{gameId}/event-sources")
    public List<EventSource> getEventSourcesOfGame(@PathVariable("gameId") Integer gameId) {
        return eventSourceService.listAllEventSourcesOfGame(gameId);
    }

    @Operation(
            summary = "Add an already created event source to a game",
            tags = {"admin"}
    )
    @ForAdmin
    @PostMapping("/admin/games/{gameId}/event-sources/{eventSourceId}")
    public ResponseEntity<String> associateEventSourceToGame(@PathVariable("gameId") Integer gameId,
                                                     @PathVariable("eventSourceId") Integer eventSourceId) {
        eventSourceService.assignEventSourceToGame(eventSourceId, gameId);
        return ResponseEntity.status(HttpStatus.CREATED).body("OK");
    }

    @Operation(
            summary = "Get the key associated with a event source. Can only download once.",
            tags = {"admin"}
    )
    @ForAdmin
    @GetMapping("/admin/event-sources/{eventSourceId}/keys")
    public ResponseEntity<EventSourceKeysResponse> fetchEventSourceKeys(@PathVariable("eventSourceId") Integer eventSourceId) {
        EventSourceKeysResponse response = eventSourceService.downloadEventSourceKeys(eventSourceId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get the event source details associated with given id.",
            tags = {"admin"}
    )
    @ForAdmin
    @GetMapping("/admin/event-sources/{eventSourceId}")
    public EventSource getEventSource(@PathVariable("eventSourceId") Integer eventSourceId) {
        return eventSourceService.readEventSource(eventSourceId);
    }

    @Operation(
            summary = "Add an already created event source to a game",
            tags = {"admin"}
    )
    @ForAdmin
    @DeleteMapping("/admin/games/{gameId}/event-sources/{eventSourceId}")
    public ResponseEntity<String> removeEventSourceFromGame(@PathVariable("gameId") Integer gameId,
                                                            @PathVariable("eventSourceId") Integer eventSourceId) {
        eventSourceService.removeEventSourceFromGame(eventSourceId, gameId);
        return ResponseEntity.ok("OK");
    }

    @Operation(
            summary = "Returns all registered event sources across all games",
            tags = {"admin"}
    )
    @ForAdmin
    @GetMapping("/admin/event-sources")
    public List<EventSource> getAllEventSources() {
        return eventSourceService.listAllEventSources();
    }

    @Operation(
            summary = "Returns the event source by the given token",
            tags = {"admin"}
    )
    @ForAdmin
    @GetMapping("/admin/event-source")
    public EventSource getEventSourceByToken(@RequestParam(name = "token") String token) {
        return eventSourceService.readEventSourceByToken(token);
    }

    @Operation(
            summary = "Deactivate an existing event source",
            tags = {"admin"}
    )
    @ForAdmin
    @DeleteMapping("/admin/event-sources/{eventSourceId}")
    public void deleteEventSource(@PathVariable("eventSourceId") Integer eventSourceId) {
        eventSourceService.deleteEventSource(eventSourceId);
    }
}
