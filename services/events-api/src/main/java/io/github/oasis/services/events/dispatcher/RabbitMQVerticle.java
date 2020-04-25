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
import io.vertx.rabbitmq.RabbitMQClient;
import io.vertx.rabbitmq.RabbitMQOptions;
import io.vertx.serviceproxy.ServiceBinder;

/**
 * @author Isuru Weerarathna
 */
public class RabbitMQVerticle extends AbstractVerticle {

    public static final String DISPATCHER_QUEUE = "event.dispatcher.queue";

    private RabbitMQClient mqClient;

    @Override
    public void start(Promise<Void> promise) {
        RabbitMQOptions options = new RabbitMQOptions()
                .setHost("localhost")
                .setPort(5672)
                .setAutomaticRecoveryEnabled(true);

        mqClient = RabbitMQClient.create(vertx, options);
        RabbitMQDispatcherService.create(vertx, mqClient, ready -> {
            if (ready.succeeded()) {
                ServiceBinder binder = new ServiceBinder(vertx);
                binder.setAddress(DISPATCHER_QUEUE)
                        .register(RabbitMQDispatcherService.class, ready.result());
                promise.complete();
            } else {
                promise.fail(ready.cause());
            }
        });
    }

    @Override
    public void stop(Promise<Void> promise) {
        if (mqClient != null) {
            System.out.println("RabbitMQ stopping...");
            mqClient.stop(promise.future());
        }
    }
}
