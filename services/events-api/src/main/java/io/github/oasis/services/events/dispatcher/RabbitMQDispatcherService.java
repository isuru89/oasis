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
import io.vertx.core.AsyncResult;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.rabbitmq.RabbitMQClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Isuru Weerarathna
 */
public class RabbitMQDispatcherService implements EventDispatcherService {

    private static final Logger LOG = LoggerFactory.getLogger(RabbitMQDispatcherService.class);

    private static final String DEF_EVENT_EXCHANGE = "oasis.event.exchange";
    private static final boolean DEF_EVENT_EXCHANGE_DURABLE = true;
    private static final boolean DEF_EVENT_EXCHANGE_AUTO_DEL = false;
    private static final String DEF_EVENT_EXCHANGE_TYPE = "direct";

    private static final String DEF_BC_EXCHANGE = "oasis.event.bc.exchange";
    private static final boolean DEF_BC_EXCHANGE_DURABLE = true;
    private static final boolean DEF_BC_EXCHANGE_AUTO_DEL = false;
    private static final String DEF_BC_EXCHANGE_TYPE = "fanout";

    private Vertx vertx;
    private RabbitMQClient client;

    static EventDispatcherService create(Vertx vertx, RabbitMQClient mqClient,
                                         JsonObject configs,
                                         Handler<AsyncResult<EventDispatcherService>> readyHandler) {
        return new RabbitMQDispatcherService(vertx, mqClient, configs, readyHandler);
    }

    public RabbitMQDispatcherService(Vertx vertx, RabbitMQClient mqClient, JsonObject configs,
                                     Handler<AsyncResult<EventDispatcherService>> readyHandler) {
        this.vertx = vertx;
        this.client = mqClient;

        LOG.info("Initializing RabbitMQ client...");
        mqClient.start(res -> {
            if (res.succeeded()) {
                initializeExchanges(mqClient, configs)
                        .onComplete(initRes -> {
                            if (initRes.succeeded()) {
                                LOG.info("RabbitMQ initialization completed.");
                                readyHandler.handle(Future.succeededFuture(this));
                            } else {
                                LOG.error("RabbitMQ initialization failed!", initRes.cause());
                                readyHandler.handle(Future.failedFuture(initRes.cause()));
                            }
                        });
            } else {
                LOG.error("Failed to connect to RabbitMQ!", res.cause());
                readyHandler.handle(Future.failedFuture(res.cause()));
            }
        });
    }

    private CompositeFuture initializeExchanges(RabbitMQClient mqClient, JsonObject configs) {
        Future<Object> eventExchange = Future.future(p -> {
            JsonObject eventExchangeOptions = configs.getJsonObject("eventExchange");
            LOG.debug("Event Exchange Options: {}", eventExchangeOptions.encodePrettily());
            JsonObject exchangeConfigs = new JsonObject();
            mqClient.exchangeDeclare(eventExchangeOptions.getString("name", DEF_EVENT_EXCHANGE),
                    eventExchangeOptions.getString("type", DEF_EVENT_EXCHANGE_TYPE),
                    eventExchangeOptions.getBoolean("durable", DEF_EVENT_EXCHANGE_DURABLE),
                    eventExchangeOptions.getBoolean("autoDelete", DEF_EVENT_EXCHANGE_AUTO_DEL),
                    exchangeConfigs,
                    res -> {
                        if (res.succeeded()) {
                            p.complete();
                        } else {
                            p.fail(res.cause());
                        }
                    });
        });
        Future<Object> broadcastExchange = Future.future(p -> {
            JsonObject eventExchangeOptions = configs.getJsonObject("broadcastExchange");
            LOG.debug("Broadcast Exchange Options: {}", eventExchangeOptions.encodePrettily());
            JsonObject exchangeConfigs = new JsonObject();
            mqClient.exchangeDeclare(eventExchangeOptions.getString("name", DEF_BC_EXCHANGE),
                    DEF_BC_EXCHANGE_TYPE,
                    eventExchangeOptions.getBoolean("durable", DEF_BC_EXCHANGE_DURABLE),
                    eventExchangeOptions.getBoolean("autoDelete", DEF_BC_EXCHANGE_AUTO_DEL),
                    exchangeConfigs,
                    res -> {
                        if (res.succeeded()) {
                            p.complete();
                        } else {
                            p.fail(res.cause());
                        }
                    });
        });
        return CompositeFuture.all(List.of(eventExchange, broadcastExchange));
    }

    @Override
    public RabbitMQDispatcherService push(EventProxy event, Handler<AsyncResult<JsonObject>> result) {
        return this;
    }

    @Override
    public void close() {

    }
}
