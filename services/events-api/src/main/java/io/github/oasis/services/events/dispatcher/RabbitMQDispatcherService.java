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
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.rabbitmq.RabbitMQClient;

/**
 * @author Isuru Weerarathna
 */
public class RabbitMQDispatcherService implements EventDispatcherService {

    private Vertx vertx;
    private RabbitMQClient client;

    static EventDispatcherService create(Vertx vertx, RabbitMQClient mqClient,
                                         Handler<AsyncResult<EventDispatcherService>> readyHandler) {
        return new RabbitMQDispatcherService(vertx, mqClient, readyHandler);
    }

    public RabbitMQDispatcherService(Vertx vertx, RabbitMQClient mqClient,
                                     Handler<AsyncResult<EventDispatcherService>> readyHandler) {
        this.vertx = vertx;
        this.client = mqClient;

        mqClient.start(res -> {
            if (res.succeeded()) {
                System.out.println("Rabbit started");
                JsonObject exchangeConfigs = new JsonObject();
                mqClient.exchangeDeclare("oasis.event.exchange",
                        "direct",
                        true,
                        false,
                        exchangeConfigs,
                        resx -> {
                            if (resx.succeeded()) {
                                System.out.println(">>> RabbitMQ success");
                                readyHandler.handle(Future.succeededFuture(this));
                            } else {
                                System.out.println(">>>> RabbitMQ failed");
                                res.cause().printStackTrace();;
                                readyHandler.handle(Future.failedFuture(resx.cause()));

                            }
                        });
            } else {
                System.out.println("Rabbit failed");
                readyHandler.handle(Future.failedFuture(res.cause()));
            }
        });
    }

    @Override
    public RabbitMQDispatcherService push(EventProxy event, Handler<AsyncResult<JsonObject>> result) {
        return this;
    }

    @Override
    public void close() {

    }
}
