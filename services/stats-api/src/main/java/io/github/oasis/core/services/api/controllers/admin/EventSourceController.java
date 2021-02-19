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
import io.github.oasis.core.services.api.services.EventSourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
        consumes = MediaType.APPLICATION_JSON_VALUE,
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
    @PostMapping("/admin/event-sources")
    public EventSource registerEventSource(@RequestBody EventSource eventSource) throws OasisException {
        return eventSourceService.registerEventSource(eventSource);
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
        return new ResponseEntity<>(
                "OK",
                HttpStatus.CREATED
        );
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
        return new ResponseEntity<>(
                "OK",
                HttpStatus.OK
        );
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
            summary = "Download the keys of an existing event source",
            tags = {"admin"}
    )
    @ForAdmin
    @DeleteMapping("/admin/event-sources/{eventSourceId}/download")
    public void downloadEventSourceKeys(@PathVariable("eventSourceId") Integer eventSourceId) {
        eventSourceService.deleteEventSource(eventSourceId);
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
