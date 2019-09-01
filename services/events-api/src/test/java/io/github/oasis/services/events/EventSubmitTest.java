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

import com.google.common.collect.Sets;
import io.github.oasis.services.common.OasisServiceException;
import io.github.oasis.services.events.domain.EventSourceRef;
import io.github.oasis.services.events.domain.UserId;
import io.github.oasis.services.events.internal.ErrorCodes;
import io.github.oasis.services.events.internal.IUserMapper;
import io.github.oasis.services.events.json.EventResponse;
import io.github.oasis.services.events.json.NewEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author Isuru Weerarathna
 */
@DisplayName("Submitting Events")
class EventSubmitTest extends AbstractTest {

    private static final String USER_ALICE = "alice@oasis.io";
    private static final String USER_JOHN = "john@oasis.io";
    private static final String APP_TEST_EVENT = "app.test.event";

    @Autowired private EventsAggregate events;

    @MockBean private IUserMapper userMapper;

    private EventSourceRef appTestSourceRef;

    @BeforeEach
    void setupEventSources() {
        MockitoAnnotations.initMocks(this);

        EventSourceRef sourceRef = new EventSourceRef();
        sourceRef.setId(100);
        sourceRef.setAllowedEventTypes(Sets.newHashSet(APP_TEST_EVENT));
        sourceRef.setMappedGames(Sets.newHashSet(101, 102));
        appTestSourceRef = sourceRef;

        Mockito.when(userMapper.map(Mockito.any(NewEvent.class)))
                .then((Answer<Optional<UserId>>) invocation -> Optional.of(new UserId(500)));
    }

    @DisplayName("should not be able to submit events other than the app registered for")
    @Test
    void testOnlyApplicationSpecificAllowedEvents() {

    }

    @DisplayName("should not be able to submit events from unregistered apps")
    @Test
    void testForRegisteredAppsOnly() {

    }

    @DisplayName("should be able to submit events without id")
    @Test
    void testCanSubmitWithoutId() {
        NewEvent event = create(APP_TEST_EVENT, USER_JOHN);
        event.setId(null);
        EventResponse eventResponse = events.submitEvent(event, appTestSourceRef);
        assertNotNull(eventResponse.getEventId());
    }

    @DisplayName("should not be able to submit events without a valid timestamp")
    @Test
    void testNoSubmitWithoutTs() {
        assertSubmissionFail(() -> {
            NewEvent event = create(APP_TEST_EVENT, USER_JOHN);
            event.setTs(0);
            events.submitEvent(event, appTestSourceRef);
        }, ErrorCodes.MISSING_MANDATORY_FIELDS);

        assertSubmissionFail(() -> {
            NewEvent event = create(APP_TEST_EVENT, USER_JOHN);
            event.setTs(-1);
            events.submitEvent(event, appTestSourceRef);
        }, ErrorCodes.MISSING_MANDATORY_FIELDS);
    }

    @DisplayName("should not be able to submit events without mentioning user")
    @Test
    void testNoSubmitWithoutUser() {
        assertSubmissionFail(() -> {
            NewEvent event = create(APP_TEST_EVENT, null);
            events.submitEvent(event, appTestSourceRef);
        }, ErrorCodes.MISSING_MANDATORY_FIELDS);

        assertSubmissionFail(() -> {
            NewEvent event = create(APP_TEST_EVENT, "");
            events.submitEvent(event, appTestSourceRef);
        }, ErrorCodes.MISSING_MANDATORY_FIELDS);

        assertSubmissionFail(() -> {
            NewEvent event = create(APP_TEST_EVENT, "  ");
            events.submitEvent(event, appTestSourceRef);
        }, ErrorCodes.MISSING_MANDATORY_FIELDS);
    }

    @DisplayName("should not be able to submit events without mentioning event type")
    @Test
    void testNoSubmitWithoutType() {
        assertSubmissionFail(() -> {
            NewEvent event = create(null, USER_ALICE);
            events.submitEvent(event, appTestSourceRef);
        }, ErrorCodes.MISSING_MANDATORY_FIELDS);

        assertSubmissionFail(() -> {
            NewEvent event = create("", USER_ALICE);
            events.submitEvent(event, appTestSourceRef);
        }, ErrorCodes.MISSING_MANDATORY_FIELDS);

        assertSubmissionFail(() -> {
            NewEvent event = create("  ", USER_ALICE);
            events.submitEvent(event, appTestSourceRef);
        }, ErrorCodes.MISSING_MANDATORY_FIELDS);
    }

    @DisplayName("should not be able to submit external event types matching 'oasis.*'")
    @Test
    void testRestrictedEventTypes() {
        assertSubmissionFail(() -> {
            NewEvent event = create("oasis.hack.event", USER_ALICE);
            events.submitEvent(event, appTestSourceRef);
        }, ErrorCodes.ILLEGAL_EVENT_TYPE);

        assertSubmissionFail(() -> {
            NewEvent event = create("oasis.event", USER_JOHN);
            events.submitEvent(event, appTestSourceRef);
        }, ErrorCodes.ILLEGAL_EVENT_TYPE);
    }

    private void assertSubmissionFail(Executable executable, int expectedErrorCode) {
        try {
            executable.execute();
            fail("expected to fail this operation, but success!");
        } catch (Throwable ex) {
            assertTrue(ex instanceof OasisServiceException, "must have thrown oasis exception!");
            OasisServiceException e = (OasisServiceException) ex;
            assertEquals(expectedErrorCode, e.getErrorCode());
        }
    }

    private NewEvent create(String eventType, String forUser) {
        NewEvent newEvent = new NewEvent();
        newEvent.setId(UUID.randomUUID().toString());
        newEvent.setEventType(eventType);
        newEvent.setUserEmail(forUser);
        newEvent.setTs(Instant.now().toEpochMilli());
        return newEvent;
    }
}
