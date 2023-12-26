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

package io.github.oasis.services.events.utils;

import io.github.oasis.core.ID;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.redis.client.Redis;
import io.vertx.redis.client.RedisAPI;
import io.vertx.redis.client.RedisOptions;
import io.vertx.redis.client.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Isuru Weerarathna
 */
public class TestRedisDeployVerticle extends AbstractVerticle {

    private static final Logger LOG = LoggerFactory.getLogger(TestRedisDeployVerticle.class);

    private Map<String, JsonObject> sources = new HashMap<>();
    private Map<String, JsonObject> users = new HashMap<>();
    private RedisAPI api;

    private final String redisUrl;

    public TestRedisDeployVerticle(String redisUrl) {
        this.redisUrl = redisUrl;
    }

    public TestRedisDeployVerticle addUser(String email, long id, Map<String, Object> gameTeamIds) {
        users.put(email, new JsonObject()
                .put("email", email)
                .put("id", id)
                .put("games", new JsonObject(gameTeamIds))
        );
        return this;
    }

    public TestRedisDeployVerticle addUser(String email, JsonObject userWithTeams) {
        users.put(email, userWithTeams);
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
                .setConnectionString(redisUrl)
                .setMaxPoolSize(4)
                .setMaxWaitingHandlers(16);

        Redis redisClient = Redis.createClient(vertx, configs);
        api = RedisAPI.api(redisClient);
        redisClient.connect(onConnect -> {
            if (onConnect.succeeded()) {
                cleanAll(api, cleanRes -> {
                    List<Future> futures = new ArrayList<>();
                    for (Map.Entry<String, JsonObject> entry : sources.entrySet()) {
                        futures.add(Future.<Response>future(p ->
                                api.hset(List.of(ID.ALL_SOURCES_INDEX, entry.getKey(), entry.getValue().encode()), p)));
                    }
                    for (Map.Entry<String, JsonObject> entry : users.entrySet()) {
                        futures.add(Future.<Response>future(p ->
                                api.hset(List.of(ID.ALL_USERS_TEAMS, entry.getKey(), entry.getValue().encode()), p)));
                    }
                    CompositeFuture.all(futures).onComplete(r -> {
                        if (r.succeeded()) {
                            LOG.info("Redis initialization completed.");
                            promise.complete();
                        } else {
                            LOG.warn("Redis initialization failed! {}", r.cause().getMessage());
                            promise.fail(r.cause());
                        }
                    });
                });
            } else {
                promise.fail(onConnect.cause());
            }
        });
    }

    private void cleanAll(RedisAPI api, Handler<AsyncResult<Void>> handler) {
        api.del(List.of(ID.ALL_SOURCES_INDEX, ID.ALL_USERS_TEAMS), res -> {
            handler.handle(Future.succeededFuture());
        });
    }

    @Override
    public void stop(Promise<Void> stopPromise) {
        if (api != null) {
            CompositeFuture.all(
                List.of(
                    Future.<Response>future(p -> api.del(List.of(ID.ALL_SOURCES_INDEX), p)),
                    Future.<Response>future(p -> api.del(List.of(ID.ALL_USERS_TEAMS), p))
                )
            ).onComplete(res -> {
                stopPromise.complete();
            });
        }
    }
}
