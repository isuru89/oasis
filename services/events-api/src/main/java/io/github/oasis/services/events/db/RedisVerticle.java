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

package io.github.oasis.services.events.db;

import io.github.oasis.services.events.auth.AuthService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.redis.client.Redis;
import io.vertx.redis.client.RedisOptions;
import io.vertx.serviceproxy.ServiceBinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Isuru Weerarathna
 */
public class RedisVerticle extends AbstractVerticle {

    private static final Logger LOG = LoggerFactory.getLogger(RedisVerticle.class);

    private Redis redisClient;

    private int maxRetries = 1;
    private int retryDelay = 5000;

    @Override
    public void start(Promise<Void> promise) {
        LOG.info("Starting Redis connection...");
        JsonObject redisConfigs = config();
        LOG.debug("Redis Configs: {}", redisConfigs.encodePrettily());
        RedisOptions configs = new RedisOptions(redisConfigs);

        redisClient = Redis.createClient(vertx, configs);
        maxRetries = redisConfigs.getInteger("connectionRetries", 1);
        retryDelay = redisConfigs.getInteger("connectionRetryDelay", 2000);

        redisClient.connect(onConnect -> {
            if (onConnect.succeeded()) {
                bindAuthService(promise);
            } else {
                LOG.error("Redis connection establishment failed!", onConnect.cause());
                retryConnection(1, promise, null);
            }
        });
    }

    private void retryConnection(int retry, Promise<Void> promise, Throwable error) {
        if (retry > maxRetries) {
            LOG.error("Redis connection establishment exhausted after {} failures! No more tries!", retry);
            promise.fail(error);
        } else {
            vertx.setTimer(retryDelay, timer -> redisClient.connect(onConnect -> {
                if (onConnect.succeeded()) {
                    LOG.info("Redis connection successful. Initializing...");
                    bindAuthService(promise);
                } else {
                    LOG.error("Redis connection establishment failed! [Retry: {}]", retry, onConnect.cause());
                    retryConnection(retry + 1, promise, onConnect.cause());
                }
            }));
        }
    }

    private void bindAuthService(Promise<Void> promise) {
        Future<Object> authFuture = Future.future(authServicePromise -> {
            RedisAuthService.create(redisClient, res -> {
                if (res.succeeded()) {
                    new ServiceBinder(vertx)
                            .setAddress(AuthService.AUTH_SERVICE_QUEUE)
                            .register(AuthService.class, res.result());
                    authServicePromise.complete();
                } else {
                    authServicePromise.fail(res.cause());
                }
            });
        });

        Future<Object> dbFuture = Future.future(dbServicePromise -> {
            RedisServiceImpl.create(redisClient, res -> {
                if (res.succeeded()) {
                    new ServiceBinder(vertx)
                            .setAddress(RedisService.DB_SERVICE_QUEUE)
                            .register(RedisService.class, res.result());
                    dbServicePromise.complete();
                } else {
                    dbServicePromise.fail(res.cause());
                }
            });
        });
        CompositeFuture.all(authFuture, dbFuture).onComplete(result -> {
            if (result.succeeded()) {
                LOG.info("Redis connection successfully established.");
                promise.complete();
            } else {
                LOG.error("Redis connection establishment failed!", result.cause());
                promise.fail(result.cause());
            }
        });
    }

    @Override
    public void stop() {
        if (redisClient != null) {
            redisClient.close();
            LOG.warn("Redis shutdown completed!");
        }
    }
}
