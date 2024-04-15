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

package io.github.oasis.services.events.dispatcher;

import io.github.oasis.core.external.EventDispatcher;
import io.github.oasis.core.external.messages.EngineMessage;
import io.github.oasis.services.events.model.EventProxy;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * @author Isuru Weerarathna
 */
public class WrappedDispatcherService extends AbstractDispatcherService {

    private final JsonObject EMPTY = new JsonObject();

    private final Vertx vertx;
    private final EventDispatcher dispatcher;

    public WrappedDispatcherService(Vertx vertx, EventDispatcher dispatcher) {
        this.vertx = vertx;
        this.dispatcher = dispatcher;
    }

    @Override
    public EventDispatcherService pushEvent(EventProxy event, Handler<AsyncResult<JsonObject>> handler) {
        handle(toEngineMessage(event), handler);
        return this;
    }

    @Override
    public EventDispatcherService push(JsonObject message, Handler<AsyncResult<JsonObject>> handler) {
        handle(toEngineMessage(message), handler);
        return this;
    }

    @Override
    public EventDispatcherService broadcast(JsonObject message, Handler<AsyncResult<JsonObject>> handler) {
        broadcastSync(toEngineMessage(message), handler);
        return this;
    }

    private void broadcastSync(EngineMessage engineMessage, Handler<AsyncResult<JsonObject>> handler) {
        vertx.executeBlocking(future -> {
            try {
                dispatcher.broadcast(engineMessage);
                future.complete();
            } catch (Exception e) {
                future.fail(e);
            }
        }, res -> {
            if (res.succeeded()) {
                handler.handle(Future.succeededFuture(EMPTY));
            } else {
                handler.handle(Future.failedFuture(res.cause()));
            }
        });
    }

    private void handle(EngineMessage engineMessage, Handler<AsyncResult<JsonObject>> handler) {
        vertx.executeBlocking(future -> {
            try {
                dispatcher.push(engineMessage);
                future.complete();
            } catch (Exception e) {
                future.fail(e);
            }
        }, res -> {
            if (res.succeeded()) {
                handler.handle(Future.succeededFuture(EMPTY));
            } else {
                handler.handle(Future.failedFuture(res.cause()));
            }
        });
    }
}
