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
import io.github.oasis.services.events.model.EventSource;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.redis.client.Redis;
import io.vertx.redis.client.RedisAPI;
import io.vertx.redis.client.Response;

import java.util.Objects;

/**
 * @author Isuru Weerarathna
 */
public class RedisAuthService implements AuthService {

    private static final String SOURCE_KEY = "oasis.sources";

    private RedisAPI redis;

    public static RedisAuthService create(Redis client, Handler<AsyncResult<AuthService>> handler) {
        return new RedisAuthService(client, handler);
    }

    public RedisAuthService(Redis redisClient,Handler<AsyncResult<AuthService>> handler) {
        this.redis = RedisAPI.api(redisClient);
        handler.handle(Future.succeededFuture(this));
    }

    @Override
    public AuthService readSource(String sourceId, Handler<AsyncResult<EventSource>> handler) {
        redis.hget(SOURCE_KEY,
                sourceId,
                res -> {
                    if (res.succeeded()) {
                        Response result = res.result();
                        if (Objects.isNull(result)) {
                            handler.handle(Future.failedFuture(""));
                            return;
                        }
                        try {
                            JsonObject sourceJson = (JsonObject) Json.decodeValue(result.toBuffer());
                            EventSource eventSource = EventSource.create(sourceId, sourceJson);
                            handler.handle(Future.succeededFuture(eventSource));
                        } catch (RuntimeException e) {
                            handler.handle(Future.failedFuture(e));
                        }
                    } else {
                        handler.handle(Future.failedFuture(""));
                    }
        });
        return this;
    }
}
