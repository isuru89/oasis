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

package io.github.oasis.core;

import java.io.Serializable;
import java.util.Map;

/**
 * Base interface for all game events. Game events ultimately consumed by
 * {@link io.github.oasis.core.elements.AbstractRule} rules.
 *
 * @author Isuru Weerarathna
 */
public interface Event extends Serializable {

    String ID = "id";
    String USER_ID = "userId";
    String USER_NAME = "userName";
    String TIMESTAMP = "ts";
    String EVENT_TYPE = "type";
    String TEAM_ID = "teamId";
    String SOURCE_ID = "sourceId";
    String GAME_ID = "gameId";
    String TIMEZONE = "tz";

    Map<String, Object> getAllFieldValues();

    void setFieldValue(String fieldName, Object value);

    /**
     * Returns the value for the given field name.
     *
     * @param fieldName field name.
     * @return field value. null if not exist.
     */
    Object getFieldValue(String fieldName);

    /**
     * Returns the event type as string. This will be used
     * to apply rules specifically targetted for the defined event types.
     *
     * @return event type
     */
    String getEventType();

    /**
     * Actual event occurred time. Must be in epoch-milliseconds.
     *
     * @return event occurred timestamp.
     */
    long getTimestamp();

    /**
     * Returns unique username of this event's user.
     * Generally this will be user's email.
     * Use this field to compare user with other user fields. {@link #getUser()} is
     * only for internal purpose.
     *
     * @return username as a string.
     */
    String getUserName();

    /**
     * Returns the owner user id of this event. There
     * can only be one user for an event. If there are multiple
     * users, you may split/duplicate this event as required.
     *
     * @return the user id of this event.
     */
    long getUser();

    /**
     * Returns the external reference id for this event.
     * This will be useful to attach this event instance to
     * domain specific record in the user application.
     *
     * @return external reference id.
     */
    String getExternalId();

    /**
     * Returns the current team of the associated user of this event.
     *
     * @return team id.
     */
    Long getTeam();

    /**
     * Return source id generated this event.
     *
     * @return source id
     */
    Integer getSource();

    /**
     * Returns associated game id of this event.
     *
     * @return game id.
     */
    Integer getGameId();

    /**
     * Returns the timezone of this event belongs to.
     *
     * @return timezone string (as in IANA)
     */
    String getTimeZone();

    /**
     * Creates scope of this event using game id, source id and user id.
     *
     * @return scope of event.
     */
    default EventScope asEventScope() {
        return new EventScope(getGameId(), getSource(), getUser(), getTeam());
    }
}
