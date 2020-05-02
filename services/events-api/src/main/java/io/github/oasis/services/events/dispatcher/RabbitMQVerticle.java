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

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.rabbitmq.RabbitMQClient;
import io.vertx.rabbitmq.RabbitMQOptions;
import io.vertx.serviceproxy.ServiceBinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Isuru Weerarathna
 */
public class RabbitMQVerticle extends AbstractVerticle {

    private static final Logger LOG = LoggerFactory.getLogger(RabbitMQVerticle.class);

    private RabbitMQClient mqClient;

    @Override
    public void start(Promise<Void> promise) {
        LOG.info("Starting RabbitMQ connection...");
        JsonObject rabbitConfigs = config();
        LOG.debug("RabbitMQ configs: {}",
                printableRabbitConfigs(rabbitConfigs).encodePrettily());
        RabbitMQOptions options = new RabbitMQOptions(rabbitConfigs);

        mqClient = RabbitMQClient.create(vertx, options);
        initService(mqClient, rabbitConfigs, promise);
    }

    void initService(RabbitMQClient client, JsonObject rabbitConfigs, Promise<Void> promise) {
        RabbitMQDispatcherService.create(vertx, client, rabbitConfigs, ready -> {
            if (ready.succeeded()) {
                ServiceBinder binder = new ServiceBinder(vertx);
                binder.setAddress(EventDispatcherService.DISPATCHER_SERVICE_QUEUE)
                        .register(EventDispatcherService.class, ready.result());
                LOG.info("RabbitMQ connection successful!");
                promise.complete();
            } else {
                LOG.error("Failed to connect to RabbitMQ!", ready.cause());
                promise.fail(ready.cause());
            }
        });
    }

    JsonObject printableRabbitConfigs(JsonObject configs) {
        return configs.copy()
                .put("password", "******");
    }

    @Override
    public void stop(Promise<Void> promise) {
        if (mqClient != null) {
            LOG.warn("Stopping RabbitMQ...");
            mqClient.stop(promise.future());
        }
    }
}
