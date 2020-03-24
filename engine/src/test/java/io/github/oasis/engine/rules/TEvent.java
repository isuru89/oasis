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

package io.github.oasis.engine.rules;

import io.github.oasis.model.Event;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Isuru Weerarathna
 */
public class TEvent implements Event {

    private Map<String, Object> values = new HashMap<>();

    public static TEvent createKeyValue(long ts, String eventType, long value) {
        TEvent event = new TEvent();
        event.values.put("value", value);
        event.values.put("type", eventType);
        event.values.put("ts", ts);
        event.values.put("game", 1);
        event.values.put("user", 0);
        event.values.put("id", UUID.randomUUID().toString());
        return event;
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
        return (String) values.get("type");
    }

    @Override
    public long getTimestamp() {
        return (Long) values.get("ts");
    }

    @Override
    public long getUser() {
        return 0;
    }

    @Override
    public String getExternalId() {
        return (String) values.get("id");
    }

    @Override
    public Long getUserId(String fieldName) {
        return (Long) values.get("user");
    }

    @Override
    public Long getTeam() {
        return null;
    }

    @Override
    public Long getTeamScope() {
        return null;
    }

    @Override
    public Integer getSource() {
        return null;
    }

    @Override
    public Integer getGameId() {
        return (Integer) values.get("game");
    }

}
