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

package io.github.oasis.services.events.dispatcher;

import io.github.oasis.services.events.model.EventProxy;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * @author Isuru Weerarathna
 */
@ProxyGen
public interface EventDispatcherService {

    String DISPATCHER_SERVICE_QUEUE = "event.dispatcher.queue";

    @GenIgnore
    static EventDispatcherService createProxy(Vertx vertx, String address) {
        return new EventDispatcherServiceVertxEBProxy(vertx, address);
    }

    @Fluent
    EventDispatcherService pushEvent(EventProxy event, Handler<AsyncResult<JsonObject>> handler);

    @Fluent
    EventDispatcherService push(JsonObject message, Handler<AsyncResult<JsonObject>> handler);

    @Fluent
    EventDispatcherService broadcast(JsonObject obj, Handler<AsyncResult<JsonObject>> handler);
}
