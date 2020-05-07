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

    private static final String OPT_DURABLE = "durable";
    private static final String OPT_AUTO_DELETE = "autoDelete";
    private static final String OPT_TYPE = "type";
    private static final String OPT_NAME = "name";

    static final String DEF_EVENT_EXCHANGE = "oasis.event.exchange";
    static final boolean DEF_EVENT_EXCHANGE_DURABLE = true;
    static final boolean DEF_EVENT_EXCHANGE_AUTO_DEL = false;
    static final String DEF_EVENT_EXCHANGE_TYPE = "direct";

    static final String DEF_BC_EXCHANGE = "oasis.event.bc.exchange";
    static final boolean DEF_BC_EXCHANGE_DURABLE = true;
    static final boolean DEF_BC_EXCHANGE_AUTO_DEL = false;
    static final String DEF_BC_EXCHANGE_TYPE = "fanout";

    private Vertx vertx;
    private RabbitMQClient client;

    private String eventExchangeName = DEF_EVENT_EXCHANGE;
    private String broadcastExchangeName = DEF_BC_EXCHANGE;

    static EventDispatcherService create(Vertx vertx, RabbitMQClient mqClient,
                                         JsonObject configs,
                                         Handler<AsyncResult<EventDispatcherService>> readyHandler) {
        return new RabbitMQDispatcherService(vertx, mqClient).init(configs, readyHandler);
    }

    public RabbitMQDispatcherService(Vertx vertx, RabbitMQClient mqClient) {
        this.vertx = vertx;
        this.client = mqClient;
    }

    RabbitMQDispatcherService init(JsonObject configs, Handler<AsyncResult<EventDispatcherService>> readyHandler) {
        LOG.info("Initializing RabbitMQ client...");
        client.start(res -> {
            if (res.succeeded()) {
                initializeExchanges(client, configs)
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
        return this;
    }

    CompositeFuture initializeExchanges(RabbitMQClient mqClient, JsonObject configs) {
        Future<Object> eventExchange = Future.future(p -> {
            JsonObject eventExchangeOptions = configs.getJsonObject("eventExchange", new JsonObject());
            LOG.debug("Event Exchange Options: {}", eventExchangeOptions.encodePrettily());
            JsonObject exchangeConfigs = new JsonObject();
            eventExchangeName = eventExchangeOptions.getString(OPT_NAME, DEF_EVENT_EXCHANGE);
            mqClient.exchangeDeclare(eventExchangeName,
                    eventExchangeOptions.getString(OPT_TYPE, DEF_EVENT_EXCHANGE_TYPE),
                    eventExchangeOptions.getBoolean(OPT_DURABLE, DEF_EVENT_EXCHANGE_DURABLE),
                    eventExchangeOptions.getBoolean(OPT_AUTO_DELETE, DEF_EVENT_EXCHANGE_AUTO_DEL),
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
            JsonObject eventExchangeOptions = configs.getJsonObject("broadcastExchange", new JsonObject());
            LOG.debug("Broadcast Exchange Options: {}", eventExchangeOptions.encodePrettily());
            JsonObject exchangeConfigs = new JsonObject();
            broadcastExchangeName = eventExchangeOptions.getString(OPT_NAME, DEF_BC_EXCHANGE);
            mqClient.exchangeDeclare(broadcastExchangeName,
                    DEF_BC_EXCHANGE_TYPE,
                    eventExchangeOptions.getBoolean(OPT_DURABLE, DEF_BC_EXCHANGE_DURABLE),
                    eventExchangeOptions.getBoolean(OPT_AUTO_DELETE, DEF_BC_EXCHANGE_AUTO_DEL),
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
        String routingKey = generateRoutingKey(event);
        client.basicPublish(eventExchangeName,
                routingKey,
                event.toJson(),
                res -> {
                    if (res.succeeded()) {
                        result.handle(Future.succeededFuture(new JsonObject()));
                    } else {
                        result.handle(Future.failedFuture(res.cause()));
                    }
                });
        return this;
    }

    @Override
    public EventDispatcherService broadcast(JsonObject obj, Handler<AsyncResult<JsonObject>> handler) {
        client.basicPublish(broadcastExchangeName,
                "",
                obj,
                res -> {
                    if (res.succeeded()) {
                        handler.handle(Future.succeededFuture(new JsonObject()));
                    } else {
                        handler.handle(Future.failedFuture(res.cause()));
                    }
                });
        return null;
    }

    static String generateRoutingKey(EventProxy event) {
        return "oasis.game." + event.getGameId();
    }

}
