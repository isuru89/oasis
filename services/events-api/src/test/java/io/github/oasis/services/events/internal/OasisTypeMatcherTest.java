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

package io.github.oasis.services.events.internal;

import io.github.oasis.services.events.json.NewEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Isuru Weerarathna
 */
@DisplayName("Internal - Event Type Matcher")
class OasisTypeMatcherTest {

    @DisplayName("should return true when event type does not match with oasis.**")
    @Test
    void testPathMatcher() {
        OasisTypeMatcher typeMatcher = new OasisTypeMatcher();
        assertTrue(typeMatcher.valid(createEvent("app.oasis")));
        assertTrue(typeMatcher.valid(createEvent("oasisx")));
        assertTrue(typeMatcher.valid(createEvent("oasisfx")));
        assertTrue(typeMatcher.valid(createEvent("a.oasis.internal")));
        assertTrue(typeMatcher.valid(createEvent("app.its.own")));
        assertTrue(typeMatcher.valid(createEvent("app")));
        assertTrue(typeMatcher.valid(createEvent("app.own")));
    }

    @DisplayName("should return false when event type does match with oasis.**")
    @Test
    void testPathMatcherInvalid() {
        OasisTypeMatcher typeMatcher = new OasisTypeMatcher();
        assertFalse(typeMatcher.valid(createEvent("oasis")));
        assertFalse(typeMatcher.valid(createEvent("oasis.")));
        assertFalse(typeMatcher.valid(createEvent("oasis.internal")));
        assertFalse(typeMatcher.valid(createEvent("oasis.internal.any.depth")));
        assertFalse(typeMatcher.valid(createEvent("OASIS")));
        assertFalse(typeMatcher.valid(createEvent("OaSiS.internal")));
        assertFalse(typeMatcher.valid(createEvent("oAsIs")));
    }

    private NewEvent createEvent(String eventType) {
        NewEvent event = new NewEvent();
        event.setEventType(eventType);
        return event;
    }
}
