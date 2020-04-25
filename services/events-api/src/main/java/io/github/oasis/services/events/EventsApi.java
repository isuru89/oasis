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

package io.github.oasis.services.events;

import io.github.oasis.services.events.db.RedisVerticle;
import io.github.oasis.services.events.dispatcher.DispatcherFactory;
import io.github.oasis.services.events.dispatcher.RabbitMQVerticle;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * @author Isuru Weerarathna
 */
public class EventsApi extends AbstractVerticle {

    @Override
    public void start(Promise<Void> promise) {
        Promise<String> redisDeployment = Promise.promise();
        vertx.deployVerticle(new RedisVerticle(), redisDeployment);

        redisDeployment.future()
            .compose(id -> {
                Promise<String> dispatcherDeployment = Promise.promise();
                vertx.deployVerticle(config().getString("oasis.dispatcher"), dispatcherDeployment);
                return dispatcherDeployment.future();
            })
            .compose(id -> {
                Promise<String> httpDeployment = Promise.promise();
                vertx.deployVerticle(HttpServiceVerticle.class,
                        new DeploymentOptions().setInstances(2),
                        httpDeployment);
                return httpDeployment.future();
            }).onComplete(res -> {
                if (res.succeeded()) {
                    promise.complete();
                } else {
                    promise.fail(res.cause());
                }
            });
    }

    public static void main(String[] args) {
        deploy();
    }

    public static Vertx deploy() {
        Vertx vertx = Vertx.vertx();
        vertx.registerVerticleFactory(new DispatcherFactory());

        JsonObject configs = new JsonObject().put("oasis.dispatcher", "oasis:" + RabbitMQVerticle.class.getName());
        DeploymentOptions options = new DeploymentOptions().setConfig(configs);
        vertx.deployVerticle(new EventsApi(), options);
        return vertx;
    }

}
