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

package io.github.oasis.services.events.http.routers;

import io.github.oasis.services.events.model.EventProxy;
import io.github.oasis.services.events.model.EventSource;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;

import java.util.Objects;
import java.util.Optional;

/**
 * @author Isuru Weerarathna
 */
final class PayloadUtils {

    static final String DATA = "data";

    static EventSource asEventSource(User user) {
        return (EventSource) user;
    }

    static Optional<JsonArray> getEventPayloadAsArray(Buffer body) {
        Object o = Json.decodeValue(body);
        if (!(o instanceof JsonObject)) {
            return Optional.empty();
        }
        JsonObject payload = (JsonObject) o;
        if (!payload.containsKey(DATA) || !(payload.getValue(DATA) instanceof JsonArray)) {
            return Optional.empty();
        }
        return Optional.of(payload.getJsonArray(DATA));
    }

    static Optional<EventProxy> getEventPayloadAsObject(Buffer body) {
        if (Objects.isNull(body)) {
            return Optional.empty();
        }

        Object o = Json.decodeValue(body);
        if (!(o instanceof JsonObject)) {
            return Optional.empty();
        }
        JsonObject payload = (JsonObject) o;
        if (!payload.containsKey(DATA) || !(payload.getValue(DATA) instanceof JsonObject)) {
            return Optional.empty();
        }
        return Optional.of(new EventProxy(payload.getJsonObject(DATA)));
    }

    static Optional<EventProxy> asEvent(Object obj) {
        if (obj instanceof JsonObject) {
            return Optional.of(new EventProxy((JsonObject) obj));
        }
        return Optional.empty();
    }

    private PayloadUtils() {}

}
