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

    private Map<String, PublicKey> sources = new HashMap<>();
    private RedisAPI api;

    public TestRedisDeployVerticle addSource(String id, PublicKey publicKey) {
        sources.put(id, publicKey);
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
                for (Map.Entry<String, PublicKey> entry : sources.entrySet()) {
                    String key = Base64.getEncoder().encodeToString(entry.getValue().getEncoded());
                    futures.add(Future.<Response>future(p -> api.hset(Arrays.asList("oasis.sources", entry.getKey(), key), p)));
                }
                CompositeFuture.all(futures).onComplete(res -> {
                    System.out.println("Redis added completed!");
                    promise.complete();
                });
            } else {
                promise.fail(onConnect.cause());
            }
        });
    }

    @Override
    public void stop(Promise<Void> stopPromise) throws Exception {
        if (api != null) {
            api.del(Collections.singletonList("oasis.sources"), res -> {
               stopPromise.complete();
            });
        }
    }
}
