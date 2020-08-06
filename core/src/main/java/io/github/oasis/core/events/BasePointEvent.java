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

package io.github.oasis.core.events;

import io.github.oasis.core.Event;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

/**
 * @author Isuru Weerarathna
 */
public abstract class BasePointEvent implements Event {

    public static final String DEFAULT_POINTS_KEY = "points";

    private String eventId;
    private String pointId;
    private String pointStoredKey;
    private BigDecimal points;
    private Event eventRef;

    public BasePointEvent(String pointId, String pointStoredKey, BigDecimal points, Event eventRef) {
        this.pointId = pointId;
        this.points = points;
        this.eventRef = eventRef;
        this.pointStoredKey = pointStoredKey;
        eventRef.setFieldValue(pointStoredKey, points);
        eventId = UUID.randomUUID().toString();
    }

    public String getPointStoredKey() {
        return pointStoredKey;
    }

    public BigDecimal getPoints() {
        return points;
    }

    public String getPointId() {
        return pointId;
    }

    @Override
    public Map<String, Object> getAllFieldValues() {
        return eventRef.getAllFieldValues();
    }

    @Override
    public void setFieldValue(String fieldName, Object value) {

    }

    @Override
    public Object getFieldValue(String fieldName) {
        return eventRef.getFieldValue(fieldName);
    }

    @Override
    public String getEventType() {
        return pointId;
    }

    @Override
    public long getTimestamp() {
        return eventRef.getTimestamp();
    }

    @Override
    public long getUser() {
        return eventRef.getUser();
    }

    @Override
    public String getExternalId() {
        return eventId;
    }

    @Override
    public Long getTeam() {
        return eventRef.getTeam();
    }

    @Override
    public Integer getSource() {
        return eventRef.getSource();
    }

    @Override
    public Integer getGameId() {
        return eventRef.getGameId();
    }

    @Override
    public String getUserName() {
        return eventRef.getUserName();
    }

    @Override
    public String getTimeZone() {
        return eventRef.getTimeZone();
    }

    @Override
    public String toString() {
        return "BasePointEvent{" +
                "eventId='" + eventId + '\'' +
                ", pointId='" + pointId + '\'' +
                ", points=" + points +
                ", eventRef=" + eventRef +
                ", pointStoredKey='" + pointStoredKey + '\'' +
                '}';
    }
}
