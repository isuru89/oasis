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

package io.github.oasis.model.events;

import io.github.oasis.model.Constants;
import io.github.oasis.model.Event;

import java.util.HashMap;
import java.util.Map;

public class JsonEvent extends HashMap<String, Object> implements Event {

    @Override
    public Map<String, Object> getAllFieldValues() {
        return this;
    }

    @Override
    public void setFieldValue(String fieldName, Object value) {
        put(fieldName, value);
    }

    @Override
    public Object getFieldValue(String fieldName) {
        return get(fieldName);
    }

    @Override
    public String getEventType() {
        return (String) get(Constants.FIELD_EVENT_TYPE);
    }

    @Override
    public long getTimestamp() {
        return getLong(Constants.FIELD_TIMESTAMP);
    }

    @Override
    public long getUser() {
        return getLong(Constants.FIELD_USER);
    }

    @Override
    public String getExternalId() {
        Object o = get(Constants.FIELD_ID);
        if (o == null) {
            return null;
        } else {
            return String.valueOf(o);
        }
    }

    @Override
    public Long getUserId(String fieldName) {
        return getLong(fieldName);
    }

    @Override
    public Long getTeam() {
        return getLongOrNull(Constants.FIELD_TEAM);
    }

    @Override
    public Long getTeamScope() {
        return getLongOrNull(Constants.FIELD_SCOPE);
    }

    @Override
    public Integer getSource() {
        Object o = get(Constants.FIELD_SOURCE);
        if (o == null) {
            return null;
        } else {
            return Integer.parseInt(o.toString());
        }
    }

    @Override
    public Integer getGameId() {
        Object o = get(Constants.FIELD_GAME_ID);
        if (o == null) {
            return null;
        } else {
            return Integer.parseInt(o.toString());
        }
    }

    private Long getLongOrNull(String key) {
        Object o = get(key);
        if (o != null) {
            return Long.parseLong(o.toString());
        } else {
            return null;
        }
    }

    private long getLong(String key) {
        Object o = get(key);
        if (o != null) {
            return Long.parseLong(o.toString());
        } else {
            return 0L;
        }
    }

    @Override
    public String toString() {
        return getEventType() + "#" + getExternalId();
    }
}
