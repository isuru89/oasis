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

import io.vertx.core.Handler;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import java.util.TimeZone;

/**
 * @author Isuru Weerarathna
 */
public class PingRoute implements Handler<RoutingContext> {

    private static final String PING_TZ = "tz";
    private static final String PING_HEALTH = "health";
    private static final String PING_OFFSET = "offset";

    private static final String PING_HEALTH_OK = "OK";

    private final JsonObject pingResult;

    private PingRoute(JsonObject pingResult) {
        this.pingResult = pingResult;
    }

    public static PingRoute create() {
        JsonObject pingResult = new JsonObject()
                .put(PING_TZ, TimeZone.getDefault().getID())
                .put(PING_OFFSET, TimeZone.getDefault().getOffset(System.currentTimeMillis()) / 1000)
                .put(PING_HEALTH, PING_HEALTH_OK);
        return new PingRoute(pingResult);
    }

    @Override
    public void handle(RoutingContext context) {
        context.response().end(Json.encodeToBuffer(pingResult));
    }
}
