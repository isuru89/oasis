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

import io.github.oasis.services.events.client.ClientVerticle;
import io.github.oasis.services.events.http.HttpServiceVerticle;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.github.oasis.services.events.Constants.KEY_DISPATCHER;
import static io.github.oasis.services.events.Constants.KEY_DISPATCHER_CONFIGS;
import static io.github.oasis.services.events.Constants.KEY_DISPATCHER_IMPL;

/**
 * @author Isuru Weerarathna
 */
public class EventsApi extends AbstractVerticle {

    private static final Logger LOG = LoggerFactory.getLogger(EventsApi.class);

    @Override
    public void start(Promise<Void> promise) {
        JsonObject oasisConfigs = config().getJsonObject("oasis", new JsonObject());
        JsonObject httpConfigs = config().getJsonObject("http", new JsonObject());

        JsonObject cacheConfigs = oasisConfigs.getJsonObject("cache", new JsonObject());
        Promise<String> cacheDeployment = Promise.promise();
        DeploymentOptions cacheOptions = new DeploymentOptions().setConfig(cacheConfigs.getJsonObject("configs"));
        vertx.deployVerticle(cacheConfigs.getString("impl"), cacheOptions, cacheDeployment);

        cacheDeployment.future()
            .compose(id -> {
                Promise<String> clientDeployment = Promise.promise();
                DeploymentOptions clientOptions = new DeploymentOptions().setConfig(oasisConfigs);
                vertx.deployVerticle(ClientVerticle.class, clientOptions, clientDeployment);
                return clientDeployment.future();
            })
            .compose(id -> {
                JsonObject dispatcherConf = oasisConfigs.getJsonObject(KEY_DISPATCHER);
                Promise<String> dispatcherDeployment = Promise.promise();
                DeploymentOptions dispatcherConfigs = new DeploymentOptions()
                        .setConfig(dispatcherConf.getJsonObject(KEY_DISPATCHER_CONFIGS));
                vertx.deployVerticle(dispatcherConf.getString(KEY_DISPATCHER_IMPL), dispatcherConfigs, dispatcherDeployment);
                return dispatcherDeployment.future();
            })
            .compose(id -> {
                Promise<String> httpDeployment = Promise.promise();
                vertx.deployVerticle(HttpServiceVerticle.class,
                        createHttpDeploymentOptions(httpConfigs),
                        httpDeployment);
                return httpDeployment.future();
            }).onComplete(res -> {
                if (res.succeeded()) {
                    LOG.info("Ready to accept events.");
                    promise.complete();
                } else {
                    LOG.info("Oops! Something went wrong starting events API!", res.cause());
                    promise.fail(res.cause());
                }
            });
    }

    private DeploymentOptions createHttpDeploymentOptions(JsonObject http) {
        return new DeploymentOptions()
                .setInstances(http.getInteger("instances", 2))
                .setHa(http.getBoolean("highAvailability", false))
                .setConfig(http);
    }
}
