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

import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.redis.client.Redis;
import io.vertx.redis.client.RedisAPI;
import io.vertx.redis.client.RedisOptions;
import io.vertx.redis.client.Response;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Isuru Weerarathna
 */
public class TestRedisDeployVerticle extends AbstractVerticle {

    private Map<String, JsonObject> sources = new HashMap<>();
    private Map<String, JsonObject> users = new HashMap<>();
    private RedisAPI api;

    public TestRedisDeployVerticle addUser(String email, long id, Map<String, Object> gameTeamIds) {
        users.put(email, new JsonObject()
                .put("email", email)
                .put("id", id)
                .put("games", new JsonObject(gameTeamIds))
        );
        return this;
    }

    public TestRedisDeployVerticle addSource(String token, int id, PublicKey publicKey, List<Integer> gameIds) {
        sources.put(token, new JsonObject()
            .put("token", token)
            .put("id", id)
            .put("key", Base64.getEncoder().encodeToString(publicKey.getEncoded()))
            .put("games", gameIds));
        return this;
    }

    @Override
    public void start(Promise<Void> promise) {
        RedisOptions configs = new RedisOptions()
                .setConnectionString("redis://localhost:6379")
                .setMaxPoolSize(4)
                .setMaxWaitingHandlers(16);

        Redis redisClient = Redis.createClient(vertx, configs);
        api = RedisAPI.api(redisClient);
        redisClient.connect(onConnect -> {
            if (onConnect.succeeded()) {
                List<Future> futures = new ArrayList<>();
                for (Map.Entry<String, JsonObject> entry : sources.entrySet()) {
                    futures.add(Future.<Response>future(p ->
                            api.hset(List.of("oasis.sources", entry.getKey(), entry.getValue().encode()), p)));
                }
                for (Map.Entry<String, JsonObject> entry : users.entrySet()) {
                    futures.add(Future.<Response>future(p ->
                            api.hset(List.of("oasis.users", entry.getKey(), entry.getValue().encode()), p)));
                }
                CompositeFuture.all(futures).onComplete(res -> {
                    System.out.println("Redis adding completed!");
                    promise.complete();
                });
            } else {
                promise.fail(onConnect.cause());
            }
        });
    }

    @Override
    public void stop(Promise<Void> stopPromise) {
        if (api != null) {
            CompositeFuture.all(
                List.of(
                    Future.<Response>future(p -> api.del(List.of("oasis.sources"), p)),
                    Future.<Response>future(p -> api.del(List.of("oasis.users"), p))
                )
            ).onComplete(res -> {
                stopPromise.complete();
            });
        }
    }
}
