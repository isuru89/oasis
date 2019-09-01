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

package io.github.oasis.services.events;

import io.github.oasis.services.common.Validation;
import io.github.oasis.services.events.domain.EventSourceRef;
import io.github.oasis.services.events.domain.UserId;
import io.github.oasis.services.events.internal.ErrorCodes;
import io.github.oasis.services.events.internal.IUserMapper;
import io.github.oasis.services.events.internal.InvalidEventTypeMatcher;
import io.github.oasis.services.events.internal.dto.GameEventDto;
import io.github.oasis.services.events.internal.exceptions.EventSubmissionException;
import io.github.oasis.services.events.json.BatchEventResponse;
import io.github.oasis.services.events.json.EventResponse;
import io.github.oasis.services.events.json.NewEvent;
import io.github.oasis.services.events.json.NewEvents;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author Isuru Weerarathna
 */
@Component
public class EventsAggregate {

    private InvalidEventTypeMatcher invalidEventTypeMatcher;
    private IUserMapper userMapper;

    public EventsAggregate(InvalidEventTypeMatcher invalidEventTypeMatcher, IUserMapper userMapper) {
        this.invalidEventTypeMatcher = invalidEventTypeMatcher;
        this.userMapper = userMapper;
    }

    public BatchEventResponse submitEvents(NewEvents events, EventSourceRef eventSource) throws EventSubmissionException {
        BatchEventResponse batchResponse = new BatchEventResponse();

        List<GameEventDto> gameEvents = new ArrayList<>();
        for (NewEvent event : events.getEvents()) {
            String eventId = event.getId();

            try {
                if (Validation.isEmpty(eventId)) {
                    throw createMissingMandatoryFieldEx("Event 'id' is mandatory for batch submissions!");
                }
                List<GameEventDto> gameEventDTOs = createEvents(event, eventSource);
                gameEvents.addAll(gameEventDTOs);
                batchResponse.addSuccessResponse(eventId, createResponseForSingleEvent(gameEventDTOs));

            } catch (EventSubmissionException e) {
                batchResponse.addFailureResponse(eventId, e);
            }
        }
        sendEvents(gameEvents);
        return batchResponse;
    }

    public EventResponse submitEvent(NewEvent event, EventSourceRef eventSource) throws EventSubmissionException {
        List<GameEventDto> gameEventDTOs = createEvents(event, eventSource);
        sendEvents(gameEventDTOs);

        return createResponseForSingleEvent(gameEventDTOs);
    }

    private List<GameEventDto> createEvents(NewEvent event, EventSourceRef eventSource) throws EventSubmissionException {
        checkEvent(event);
        checkForInvalidEventTypes(event, eventSource);

        GameEventDto gameEvent = GameEventDto.from(event);
        gameEvent.addEventSource(eventSource.getId());

        // find user id
        UserId userId = UserId.mapFor(event, userMapper);
        gameEvent.addUser(userId);


        assignIdIfNotExist(gameEvent);


        // create one event per mapped game
        return createOneEventPerMappedGame(gameEvent, eventSource);
    }

    private void sendEvents(List<GameEventDto> gameEvents) {
        // @TODO send all events
        System.out.println(gameEvents);
    }

    private List<GameEventDto> createOneEventPerMappedGame(GameEventDto gameEvent,
                                                           EventSourceRef eventSource) {
        return eventSource.getMappedGames().stream()
                .map(gameEvent::cloneEventForGame)
                .collect(Collectors.toList());
    }

    private void assignIdIfNotExist(GameEventDto event) {
        if (!event.hasId()) {
            event.addId(UUID.randomUUID().toString());
        }
    }

    private EventResponse createResponseForSingleEvent(List<GameEventDto> gameEvents) {
        GameEventDto dto = gameEvents.get(0);
        EventResponse response = new EventResponse(dto.getId());
        response.setUserId(dto.getUser());
        response.setMappedGameCount(gameEvents.size());
        return response;
    }

    private void checkEvent(NewEvent event) throws EventSubmissionException {
        if (Validation.isEmpty(event.getEventType())) {
            throw createMissingMandatoryFieldEx("Missing mandatory 'eventType' field!");
        } else if (event.getTs() <= 0) {
            throw createMissingMandatoryFieldEx(
                    "Field 'timestamp' must be a valid epoch milliseconds timestamp!");
        } else if (Validation.isEmpty(event.getUserEmail())) {
            throw createMissingMandatoryFieldEx("Missing mandatory 'userEmail' field!");
        }
    }

    private void checkForInvalidEventTypes(NewEvent event,
                                           EventSourceRef eventSource) throws EventSubmissionException {
        if (!invalidEventTypeMatcher.valid(event)) {
            throw new EventSubmissionException(ErrorCodes.ILLEGAL_EVENT_TYPE,
                    "Event type '%s' is reserved for internal use!");
        }

        if (!eventSource.isEventAllowed(event.getEventType())) {
            throw new EventSubmissionException(ErrorCodes.UNMAPPED_EVENT_TYPE,
                    "Event type '%s' has not been registered with this external application!");
        }
    }

    private EventSubmissionException createMissingMandatoryFieldEx(String message) {
        return new EventSubmissionException(ErrorCodes.MISSING_MANDATORY_FIELDS, message);
    }
}
