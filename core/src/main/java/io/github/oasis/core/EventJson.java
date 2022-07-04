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

package io.github.oasis.core;

import java.util.Map;

/**
 * Json based event.
 *
 * @author Isuru Weerarathna
 */
public class EventJson implements Event {

    private String id;
    private String type;
    private long ts;
    private String userName;
    private long userId;
    private long teamId;
    private int gameId;
    private int sourceId;
    private String tz;

    private Map<String, Object> ref;

    public EventJson() {
    }

    public EventJson(Map<String, Object> ref) {
        this.id = (String) ref.get(Event.ID);
        this.type = (String) ref.get(Event.EVENT_TYPE);
        this.userName = (String) ref.get(Event.USER_NAME);
        this.ts = ((Number)ref.get(Event.TIMESTAMP)).longValue();
        this.userId = ((Number)ref.get(Event.USER_ID)).longValue();
        this.teamId = ((Number)ref.get(Event.TEAM_ID)).longValue();
        this.sourceId = ((Number)ref.get(Event.SOURCE_ID)).intValue();
        this.gameId = ((Number)ref.get(Event.GAME_ID)).intValue();
        this.tz = (String) ref.get((Event.TIMEZONE));
        this.ref = ref;
    }

    @Override
    public Map<String, Object> getAllFieldValues() {
        return ref;
    }

    @Override
    public void setFieldValue(String fieldName, Object value) {
        ref.put(fieldName, value);
    }

    @Override
    public Object getFieldValue(String fieldName) {
        return ref.get(fieldName);
    }

    @Override
    public String getEventType() {
        return type;
    }

    @Override
    public long getTimestamp() {
        return ts;
    }

    @Override
    public String getUserName() {
        return userName;
    }

    @Override
    public long getUser() {
        return userId;
    }

    @Override
    public String getExternalId() {
        return id;
    }

    @Override
    public Long getTeam() {
        return teamId;
    }

    @Override
    public Integer getSource() {
        return sourceId;
    }

    @Override
    public Integer getGameId() {
        return gameId;
    }

    @Override
    public String getTimeZone() {
        return tz;
    }

    @Override
    public String toString() {
        return "EventJson{" +
                "id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", ts=" + ts +
                ", user=" + userId +
                ", team=" + teamId +
                '}';
    }
}
