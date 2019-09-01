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

package io.github.oasis.model;

import java.io.Serializable;
import java.util.Map;

public interface Event extends Serializable {

    String ID = "_id";
    String USER = "_user";
    String TIMESTAMP = "_ts";
    String EVENT_TYPE = "_type";
    String SOURCE = "_source";
    String GAME = "_gameId";

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
     * Return user id indicated by any other field. This is useful
     * when an event is associated with several users, and will be called when
     * framework needs to assign point(s) to this other user as well.
     *
     * @param fieldName user field name.
     * @return other user id.
     */
    Long getUserId(String fieldName);

    /**
     * Returns the current team of the associated user of this event.
     *
     * @return team id.
     */
    Long getTeam();

    /**
     * Returns the scope id of the current team.
     * Operators does not care about this field for its computation.
     *
     * @return the scope id of the current team.
     */
    Long getTeamScope();

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
}
