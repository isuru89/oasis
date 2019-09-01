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

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.oasis.model.Constants;
import io.github.oasis.model.collect.Pair;
import io.github.oasis.services.dto.StatusResponse;
import io.github.oasis.services.dto.events.EventPushDto;
import io.github.oasis.services.model.EventSourceToken;
import io.github.oasis.services.services.IEventsService;
import io.github.oasis.services.utils.BodyRequestWrapper;
import io.github.oasis.services.utils.HmacUtils;
import io.github.oasis.services.utils.SecurityUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

@RestController
@SuppressWarnings("unused")
@RequestMapping("/event")
public class EventsController {

    @Autowired
    private IEventsService eventsService;

    @Autowired
    private ObjectMapper jsonMapper;

    @PostMapping("/submit")
    public StatusResponse submitEvent(HttpServletRequest request,
                                      HttpServletResponse response) throws Exception {

        // verify event submission
        // header format: <algo> <token>:<nonce>:<digest>
        Pair<String, Triple<String, String, String>> authHeader = HmacUtils.getAuthHeader(request);
        if (authHeader == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Event source authentication failed! No authorization header or header is invalid!");
        }


        String algorithm = authHeader.getValue0();
        String token = authHeader.getValue1().getLeft();
        String nonce = authHeader.getValue1().getMiddle();
        String digest = authHeader.getValue1().getRight();

        Optional<EventSourceToken> appIdOpt = eventsService.readSourceByToken(token);
        EventSourceToken sourceToken = appIdOpt.orElseThrow((Supplier<Exception>)
                () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Event source token is not recognized by the Oasis!"));

        BodyRequestWrapper requestWrapper = new BodyRequestWrapper(request);
        byte[] payload = requestWrapper.getPayload();
        SecurityUtils.verifyIntegrity(sourceToken, algorithm, digest, payload);

        EventPushDto eventData = jsonMapper.readValue(payload, EventPushDto.class);
        Map<String, Object> meta = eventData.getMeta() == null ? new HashMap<>() : eventData.getMeta();
        Long gid = meta.containsKey("gameId") ? null : Long.parseLong(meta.get("gameId").toString());

        if (eventData.getEvent() != null) {
            Map<String, Object> event = eventData.getEvent();
            event.put(Constants.FIELD_GAME_ID, gid);
            event.putAll(meta);
            eventsService.submitEvent(token, event);
        } else if (eventData.getEvents() != null) {
            eventData.getEvents().forEach(et -> {
                et.put(Constants.FIELD_GAME_ID, gid);
                et.putAll(meta);
            });
            eventsService.submitEvents(token, eventData.getEvents());
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No events have been defined in this call!");
        }

        return new StatusResponse(true);
    }

}
