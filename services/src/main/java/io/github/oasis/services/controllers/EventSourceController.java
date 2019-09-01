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

import io.github.oasis.services.dto.DeleteResponse;
import io.github.oasis.services.dto.events.EventSourceDto;
import io.github.oasis.services.model.EventSourceToken;
import io.github.oasis.services.model.UserRole;
import io.github.oasis.services.services.IEventsService;
import io.github.oasis.services.utils.Checks;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@SuppressWarnings("unused")
@RequestMapping("/event")
public class EventSourceController {

    private static final Logger LOG = LoggerFactory.getLogger(EventSourceController.class);

    @Autowired
    private IEventsService eventsService;

    @Secured(UserRole.ROLE_ADMIN)
    @PostMapping("/source")
    public EventSourceDto addEventSource(@RequestBody EventSourceToken eventSourceToken) throws Exception {
        // duplicate events source names are ignored.
        if (eventsService.listAllEventSources().stream()
                .anyMatch(e -> e.getDisplayName().equalsIgnoreCase(eventSourceToken.getDisplayName()))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "There is already an event token exist with name '" + eventSourceToken.getDisplayName() + "'!");
        }

        EventSourceToken insertedToken = eventsService.addEventSource(eventSourceToken);
        return EventSourceDto.from(insertedToken);
    }

    @GetMapping("/source/list")
    public List<EventSourceDto> listEventSource() throws Exception {
        return eventsService.listAllEventSources()
                .stream()
                .map(EventSourceDto::from)
                .collect(Collectors.toList());
    }

    @PostMapping(value = "/source/{id}/downloadKey", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void downloadEventSourceKey(@PathVariable("id") int sourceId, HttpServletResponse response) throws Exception {
        Checks.greaterThanZero(sourceId, "id");

        try {
            Optional<EventSourceToken> optionalToken = eventsService.makeDownloadSourceKey(sourceId);
            if (optionalToken.isPresent()) {
                EventSourceToken eventSourceToken = optionalToken.get();
                String sourceName = String.format("key-%s", eventSourceToken.getSourceName());

                response.addHeader("Content-Disposition", String.format("attachment; filename=key-%s.key", sourceName));
                response.addHeader("Access-Control-Expose-Headers", "Content-Disposition");

                // get your file as InputStream
                try (ByteArrayInputStream is = new ByteArrayInputStream(eventSourceToken.getSecretKey())) {
                    // copy it to response's OutputStream
                    IOUtils.copy(is, response.getOutputStream());
                    response.flushBuffer();
                }
            } else {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "The key has been already downloaded! Cannot download again.");
            }

        } catch (IOException ex) {
            LOG.info("Error writing buffer to output stream. Event source id was '{}'", sourceId, ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "IOError writing file to output buffer!");
        }
    }

    @Secured(UserRole.ROLE_ADMIN)
    @DeleteMapping("/source/{id}")
    public DeleteResponse deleteEventSource(@PathVariable("id") int sourceId) throws Exception {
        boolean success = eventsService.disableEventSource(sourceId);
        return new DeleteResponse("eventSource", success);
    }

}