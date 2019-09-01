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
import io.github.oasis.services.events.internal.ErrorCodes;
import io.github.oasis.services.events.internal.InvalidEventTypeMatcher;
import io.github.oasis.services.events.internal.exceptions.EventSubmissionException;
import io.github.oasis.services.events.json.EventResponse;
import io.github.oasis.services.events.json.NewEvent;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * @author Isuru Weerarathna
 */
@Component
public class EventsAggregate {

    private InvalidEventTypeMatcher invalidEventTypeMatcher;

    public EventsAggregate(InvalidEventTypeMatcher invalidEventTypeMatcher) {
        this.invalidEventTypeMatcher = invalidEventTypeMatcher;
    }

    public EventResponse submitEvent(NewEvent event) throws EventSubmissionException {
        checkEvent(event);
        checkForInvalidEventTypes(event);

        assignIdIfNotExist(event);

        return new EventResponse(event.getId());
    }

    private void assignIdIfNotExist(NewEvent event) {
        if (Validation.isEmpty(event.getId())) {
            event.setId(UUID.randomUUID().toString());
        }
    }

    private void checkEvent(NewEvent event) throws EventSubmissionException {
        if (Validation.isEmpty(event.getEventType())) {
            throw new EventSubmissionException(ErrorCodes.MISSING_MANDATORY_FIELDS,
                    "Missing mandatory 'eventType' field!");
        } else if (event.getTs() <= 0) {
            throw new EventSubmissionException(ErrorCodes.MISSING_MANDATORY_FIELDS,
                    "Invalid 'timestamp' field! Must be a valid epoch milliseconds timestamp!");
        } else if (Validation.isEmpty(event.getUserEmail())) {
            throw new EventSubmissionException(ErrorCodes.MISSING_MANDATORY_FIELDS,
                    "Missing mandatory 'userEmail' field!");
        }
    }

    private void checkForInvalidEventTypes(NewEvent event) throws EventSubmissionException {
        if (!invalidEventTypeMatcher.valid(event)) {
            throw new EventSubmissionException(ErrorCodes.ILLEGAL_EVENT_TYPE,
                    "Event type '%s' is not allowed in Oasis from external applications!");
        }
    }
}
