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

package io.github.oasis.engine;

import io.github.oasis.core.Event;

import java.util.Map;

/**
 * @author Isuru Weerarathna
 */
public class EventJson implements Event {

    private Map<String, Object> ref;

    public EventJson(Map<String, Object> ref) {
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
        return (String) ref.get(Event.EVENT_TYPE);
    }

    @Override
    public long getTimestamp() {
        return (Long) ref.get(Event.TIMESTAMP);
    }

    @Override
    public String getUserName() {
        return (String) ref.get(Event.USER_NAME);
    }

    @Override
    public long getUser() {
        return (Long) ref.get(Event.USER_ID);
    }

    @Override
    public String getExternalId() {
        return (String) ref.get(Event.ID);
    }

    @Override
    public Long getTeam() {
        return (Long) ref.get(Event.TEAM_ID);
    }

    @Override
    public Integer getSource() {
        return (Integer) ref.get(Event.SOURCE_ID);
    }

    @Override
    public Integer getGameId() {
        return (Integer) ref.get(Event.GAME_ID);
    }
}
