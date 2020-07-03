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

package io.github.oasis.engine.model;

import io.github.oasis.core.Event;
import io.github.oasis.core.EventJson;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * @author Isuru Weerarathna
 */
public class TEvent extends EventJson {

    public static int SOURCE_ID = 1;
    public static int GAME_ID = 1;
    public static long TEAM_ID = 1;
    public static long USER_ID = 0L;

    private Map<String, Object> values = new HashMap<>();

    public static EventJson createKeyValue(long ts, String eventType, long value) {
        Map<String, Object> values = new HashMap<>();
        values.put("value", value);
        values.put(Event.EVENT_TYPE, eventType);
        values.put(Event.TIMESTAMP, ts);
        values.put(Event.GAME_ID, GAME_ID);
        values.put(Event.USER_ID, USER_ID);
        values.put(Event.TEAM_ID, TEAM_ID);
        values.put(Event.SOURCE_ID, SOURCE_ID);
        values.put(Event.ID, UUID.randomUUID().toString());
        return new EventJson(values);
    }

    public static EventJson createKeyValue(long user, long ts, String eventType, long value) {
        Map<String, Object> values = createKeyValue(ts, eventType, value).getAllFieldValues();
        values.put(Event.USER_ID, user);
        values.put(Event.USER_NAME, String.valueOf(user));
        return new EventJson(values);
    }

    public static EventJson createKeyValueTz(long user, long ts, String eventType, long value, String tz) {
        Map<String, Object> values = createKeyValue(ts, eventType, value).getAllFieldValues();
        values.put(Event.USER_ID, user);
        values.put(Event.USER_NAME, String.valueOf(user));
        values.put(Event.TIMEZONE, tz);
        return new EventJson(values);
    }

    public static EventJson createWithTeam(long user, long team, long ts, String eventType, long value) {
        Map<String, Object> values = createKeyValue(ts, eventType, value).getAllFieldValues();
        values.put(Event.USER_ID, user);
        values.put(Event.TEAM_ID, team);
        return new EventJson(values);
    }

    @Override
    public Map<String, Object> getAllFieldValues() {
        return values;
    }

    @Override
    public void setFieldValue(String fieldName, Object value) {

    }

    @Override
    public Object getFieldValue(String fieldName) {
        return values.get(fieldName);
    }

    @Override
    public String getEventType() {
        return (String) values.get(Event.EVENT_TYPE);
    }

    @Override
    public long getTimestamp() {
        return (Long) values.get(Event.TIMESTAMP);
    }

    @Override
    public String getUserName() {
        return (String) values.get(Event.USER_NAME);
    }

    @Override
    public long getUser() {
        return (Long) values.get(Event.USER_ID);
    }

    @Override
    public String getExternalId() {
        return (String) values.get(Event.ID);
    }

    @Override
    public Long getTeam() {
        return (Long) values.get(Event.TEAM_ID);
    }

    @Override
    public Integer getSource() {
        return (Integer) values.get(Event.SOURCE_ID);
    }

    @Override
    public Integer getGameId() {
        return (Integer) values.get(Event.GAME_ID);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getExternalId());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        TEvent that = (TEvent) obj;
        return Objects.equals(getExternalId(), that.getExternalId());
    }

    @Override
    public String toString() {
        return "TEvent{" +
                "type=" + getEventType() + ", " +
                "ts=" + getTimestamp() + ", " +
                "id=" + getExternalId() +
                '}';
    }
}
