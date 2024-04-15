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

import io.github.oasis.services.events.Constants;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.redis.client.Redis;
import io.vertx.redis.client.RedisAPI;
import io.vertx.redis.client.RedisOptions;
import io.vertx.serviceproxy.ServiceBinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

/**
 * @author Isuru Weerarathna
 */
public class RedisVerticle extends AbstractVerticle {

    private static final Logger LOG = LoggerFactory.getLogger(RedisVerticle.class);

    private static final int DEFAULT_REDIS_MAX_RETRIES = 1;
    private static final int DEFAULT_REDIS_RETRY_DELAY = 5000;

    private Redis redisClient;

    private int maxRetries = DEFAULT_REDIS_MAX_RETRIES;
    private int retryDelay = DEFAULT_REDIS_RETRY_DELAY;

    @Override
    public void start(Promise<Void> promise) {
        LOG.info("Starting Redis connection...");
        JsonObject redisConfigs = config();
        LOG.info("Redis Configs: {}", redisConfigs.encodePrettily());
        RedisOptions configs = new RedisOptions(redisConfigs);

        redisClient = Redis.createClient(vertx, configs);
        maxRetries = redisConfigs.getInteger("connectionRetries", DEFAULT_REDIS_MAX_RETRIES);
        retryDelay = redisConfigs.getInteger("connectionRetryDelay", DEFAULT_REDIS_RETRY_DELAY);

        RedisSettings settings = new RedisSettings();
        settings.setEventSourceTTL(redisConfigs.getInteger("eventSourcesTTL", Constants.DEFAULT_TTL_EVENT_SOURCE));

        redisClient.connect(onConnect -> {
            if (onConnect.succeeded()) {
                checkConnectionHealth(promise, settings);
            } else {
                LOG.error("Redis connection establishment failed!", onConnect.cause());
                retryConnection(1, settings, promise, null);
            }
        });
    }

    private void retryConnection(int retry, RedisSettings redisSettings, Promise<Void> promise, Throwable error) {
        if (retry > maxRetries) {
            LOG.error("Redis connection establishment exhausted after {} failures! No more tries!", retry);
            promise.fail(error);
        } else {
            vertx.setTimer(retryDelay, timer -> redisClient.connect(onConnect -> {
                if (onConnect.succeeded()) {
                    LOG.info("Redis connection successful. Initializing...");
                    checkConnectionHealth(promise, redisSettings);
                } else {
                    LOG.error("Redis connection establishment failed! [Retry: {}]", retry, onConnect.cause());
                    retryConnection(retry + 1, redisSettings, promise, onConnect.cause());
                }
            }));
        }
    }

    private void checkConnectionHealth(Promise<Void> promise, RedisSettings redisSettings) {
        RedisAPI.api(redisClient).ping(Collections.singletonList("oasis test"), res -> {
            if (res.succeeded()) {
                bindRedisService(promise, redisSettings);
            } else {
                promise.fail(res.cause());
            }
        });

    }

    private void bindRedisService(Promise<Void> promise, RedisSettings redisSettings) {
        Future.future(dbServicePromise -> RedisServiceImpl.create(redisClient, redisSettings, res -> {
            if (res.succeeded()) {
                new ServiceBinder(vertx)
                        .setAddress(RedisService.DB_SERVICE_QUEUE)
                        .register(RedisService.class, res.result());
                dbServicePromise.complete();
            } else {
                dbServicePromise.fail(res.cause());
            }
        })).onComplete(result -> {
            if (result.succeeded()) {
                LOG.info("Redis connection successfully established.");
                promise.complete();
            } else {
                promise.fail(result.cause());
            }
        }).onFailure(promise::fail);
    }

    @Override
    public void stop() {
        if (redisClient != null) {
            redisClient.close();
            LOG.warn("Redis shutdown completed!");
        }
    }
}
