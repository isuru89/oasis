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

package io.github.oasis.elements.badges;

import io.github.oasis.core.Event;
import io.github.oasis.core.EventJson;
import io.github.oasis.core.EventScope;
import io.github.oasis.core.events.BasePointEvent;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Awards points for badges.
 *
 * @author Isuru Weerarathna
 */
public class BadgePointsEvent extends BasePointEvent {
    public BadgePointsEvent(String pointId, String pointStoredKey, BigDecimal points, Event eventRef) {
        super(pointId, pointStoredKey, points, eventRef);
    }

    public BadgePointsEvent(String pointId, BigDecimal points, Event eventRef) {
        super(pointId, BadgePointsEvent.DEFAULT_POINTS_KEY, points, eventRef);
    }

    public static class BadgeEventRef extends EventJson {

        public BadgeEventRef(Map<String, Object> ref) {
            super(ref);
        }

        public static BadgeEventRef from(String eventId, long timestamp, EventScope scope) {
            Map<String, Object> json = new HashMap<>();
            json.put(Event.ID, eventId);
            json.put(Event.USER_ID, scope.getUserId());
            json.put(Event.TEAM_ID, scope.getTeamId());
            return new BadgeEventRef(json);
        }
    }

}
