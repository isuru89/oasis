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

package io.github.oasis.elements.badges.signals;

import io.github.oasis.core.Event;

/**
 * @author Isuru Weerarathna
 */
public class ConditionalBadgeSignal extends BadgeSignal {
    public ConditionalBadgeSignal(String ruleId, Event event, int rankId, long timestamp, String eventId) {
        super(ruleId, event, event.getTimestamp(), rankId, timestamp, timestamp, eventId, eventId);
    }
    public static ConditionalBadgeSignal create(String ruleId, Event causedEvent, int rankId) {
        return new ConditionalBadgeSignal(ruleId, causedEvent, rankId, causedEvent.getTimestamp(), causedEvent.getExternalId());
    }
}
