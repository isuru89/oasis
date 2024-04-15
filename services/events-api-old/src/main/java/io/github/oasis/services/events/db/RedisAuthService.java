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

import io.github.oasis.core.ID;
import io.github.oasis.services.events.auth.AuthService;
import io.github.oasis.services.events.model.EventSource;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.redis.client.Redis;
import io.vertx.redis.client.RedisAPI;
import io.vertx.redis.client.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * @author Isuru Weerarathna
 */
public class RedisAuthService implements AuthService {

    private static final Logger LOG = LoggerFactory.getLogger(RedisAuthService.class);

    private static final String SOURCE_KEY = ID.ALL_SOURCES_INDEX;
    private static final String EMPTY = "";

    private final RedisAPI redis;

    public static RedisAuthService create(Redis client, Handler<AsyncResult<AuthService>> handler) {
        return new RedisAuthService(client, handler);
    }

    public RedisAuthService(Redis redisClient, Handler<AsyncResult<AuthService>> handler) {
        this.redis = RedisAPI.api(redisClient);
        redis.hgetall(SOURCE_KEY, res -> {
           if (res.succeeded()) {
               LOG.info("Redis authentication handler verified.");
               handler.handle(Future.succeededFuture(this));
           } else {
               handler.handle(Future.failedFuture(res.cause()));
           }
        });
    }

    @Override
    public AuthService readSource(String sourceId, Handler<AsyncResult<EventSource>> handler) {
        redis.hget(SOURCE_KEY,
                sourceId,
                res -> {
                    if (res.succeeded()) {
                        Response result = res.result();
                        if (Objects.isNull(result)) {
                            handler.handle(Future.failedFuture(EMPTY));
                            return;
                        }
                        try {
                            JsonObject sourceJson = (JsonObject) Json.decodeValue(result.toBuffer());
                            EventSource eventSource = EventSource.create(sourceId, sourceJson);
                            LOG.info("Found event source {}", eventSource);
                            handler.handle(Future.succeededFuture(eventSource));
                        } catch (RuntimeException e) {
                            LOG.error("Failed to retrieve source info {}!", sourceId, e);
                            handler.handle(Future.failedFuture(e));
                        }
                    } else {
                        handler.handle(Future.failedFuture(res.cause()));
                    }
        });
        return this;
    }
}
