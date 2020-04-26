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

package io.github.oasis.services.events.model;

import io.github.oasis.core.Event;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

import java.util.Map;

/**
 * @author Isuru Weerarathna
 */
@DataObject
public class EventProxy implements Event {

    private JsonObject ref;
    private JsonObject data;

    public JsonObject toJson() {
        return ref;
    }

    public EventProxy(JsonObject ref) {
        this.ref = ref;
        data = ref.getJsonObject("payload");
    }

    @Override
    public Map<String, Object> getAllFieldValues() {
        return data.getMap();
    }

    @Override
    public void setFieldValue(String fieldName, Object value) {
        data.put(fieldName, value);
    }

    @Override
    public Object getFieldValue(String fieldName) {
        return data.getValue(fieldName);
    }

    @Override
    public String getEventType() {
        return ref.getString("type");
    }

    @Override
    public long getTimestamp() {
        return ref.getLong("ts");
    }

    @Override
    public long getUser() {
        return ref.getLong("user");
    }

    @Override
    public String getExternalId() {
        return ref.getString("id");
    }

    @Override
    public Long getUserId(String fieldName) {
        return null;
    }

    @Override
    public Long getTeam() {
        return ref.getLong("team");
    }

    @Override
    public Integer getSource() {
        return ref.getInteger("source");
    }

    @Override
    public Integer getGameId() {
        return ref.getInteger("game");
    }

    public String getUserEmail() {
        return ref.getString("email");
    }

    public EventProxy copyForGame(int gameId, int sourceId, long userId, long teamId) {
        JsonObject event = toJson().copy()
                .put("source", sourceId)
                .put("team", teamId)
                .put("user", userId)
                .put("game", gameId);
        return new EventProxy(event);
    }
}
