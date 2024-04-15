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

import io.github.oasis.core.collect.Pair;
import io.github.oasis.services.events.utils.TestUtils;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.codec.BodyCodec;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.util.Set;
import java.util.UUID;

/**
 * @author Isuru Weerarathna
 */
@DisplayName("Successful Event Publish")
public class PublishTest extends AbstractEventPushTest {

    @Test
    @DisplayName("Publish once success")
    void successPublish(Vertx vertx, VertxTestContext testContext) throws NoSuchAlgorithmException {
        KeyPair keyPair = TestUtils.createKeys();
        String token = UUID.randomUUID().toString();
        setSourceExists(token, createEventSource(token, 1, Set.of(1), keyPair.getPublic()));
        setPlayerExists(KNOWN_USER, createPlayerWithTeam(KNOWN_USER, 500, Pair.of(200, 1)));

        String hash = TestUtils.signPayload(VALID_PAYLOAD, keyPair.getPrivate());

        callForEvent(vertx, token + ":" + hash)
                .sendJson(
                        VALID_PAYLOAD,
                        testContext.succeeding(res -> assertSuccessWithInvocations(res, testContext, 1))
                );
    }

    @Test
    @DisplayName("Server: Publish only for user assigned subset of games")
    void successPublishForOnlyGames(Vertx vertx, VertxTestContext testContext) throws NoSuchAlgorithmException {
        KeyPair keyPair = TestUtils.createKeys();
        String token = UUID.randomUUID().toString();
        setSourceExists(token, createEventSource(token, 1, Set.of(1,2,3), keyPair.getPublic()));
        setPlayerExists("isuru@oasis.com", createPlayerWith2Teams("isuru@oasis.com", 500, Pair.of(200,1), Pair.of(201, 2)));

        JsonObject event = TestUtils.aEvent("isuru@oasis.com", System.currentTimeMillis(), "test.a", 100);
        JsonObject payload = new JsonObject().put("data", event);
        String hash = TestUtils.signPayload(payload, keyPair.getPrivate());

        callForEvent(vertx, token + ":" + hash)
                .sendJson(
                        payload,
                        testContext.succeeding(res -> {
                            assertSuccessWithInvocations(res, testContext, 2);
                        })
                );
    }

    @Test
    @DisplayName("Server: Publish only for existing games")
    void successPublishForOnlyExistingGames(Vertx vertx, VertxTestContext testContext) throws NoSuchAlgorithmException {
        KeyPair keyPair = TestUtils.createKeys();
        String sourceToken = UUID.randomUUID().toString();
        String userEmail = "success.publish@oasis.io";
        setSourceExists(sourceToken, createEventSource(sourceToken, 1, Set.of(1,2), keyPair.getPublic()));
        setPlayerExists(userEmail, createPlayerWith2Teams(userEmail, 500, Pair.of(200,1), Pair.of(201, 3)));

        JsonObject event = TestUtils.aEvent(userEmail, System.currentTimeMillis(), "test.a", 100);
        JsonObject payload = new JsonObject().put("data", event);
        String hash = TestUtils.signPayload(payload, keyPair.getPrivate());

        callForEvent(vertx, sourceToken + ":" + hash)
                .sendJson(payload, testContext.succeeding(res -> assertSuccessWithInvocations(res, testContext, 1)));
    }

    @Test
    @DisplayName("Dispatching failure handling")
    void dispatcherFailed(Vertx vertx, VertxTestContext testContext) throws NoSuchAlgorithmException {
        KeyPair keyPair = TestUtils.createKeys();
        dispatcherService.setReturnSuccess(false);
        String token = UUID.randomUUID().toString();
        setSourceExists(token, createEventSource(token, 1, Set.of(1,2), keyPair.getPublic()));
        setPlayerExists(KNOWN_USER, createPlayerWithTeam(KNOWN_USER, 500, Pair.of(200,1)));


        String hash = TestUtils.signPayload(VALID_PAYLOAD, keyPair.getPrivate());

        callForEvent(vertx, token + ":" + hash)
                .sendJson(
                        VALID_PAYLOAD,
                        testContext.succeeding(res -> assertSuccessWithInvocations(res, testContext, 1))
                );
    }


    @Test
    @DisplayName("Cache clear for event source reference")
    void sourceCacheClearSuccess(Vertx vertx, VertxTestContext testContext) throws NoSuchAlgorithmException {
        KeyPair keyPair = TestUtils.createKeys();
        String token = UUID.randomUUID().toString();
        String userId = "temp.source.cache@oasis.io";
        setSourceExists(token, createEventSource(token, 1, Set.of(1), keyPair.getPublic()));
        setPlayerExists(userId, createPlayerWithTeam(userId, 500, Pair.of(200, 1)));

        JsonObject validPayload = new JsonObject()
                .put("data", TestUtils.aEvent(userId, System.currentTimeMillis(), "event.a", 100));
        String hash = TestUtils.signPayload(validPayload, keyPair.getPrivate());

        Config redisConfigs = new Config();
        redisConfigs.useSingleServer()
                .setAddress(redis.getRedisURI());
        RedissonClient redissonClient = Redisson.create(redisConfigs);

        callForEvent(vertx, token + ":" + hash)
                .sendJson(validPayload,
                        testContext.succeeding(res -> {
                            assertSourceExistsInCache(redissonClient, token, true);
                            callToDeleteEndPoint("/_cache/sources/" + token, vertx)
                                    .as(BodyCodec.jsonObject())
                                    .send(testContext.succeeding(resDel -> {
                                        testContext.verify(() -> {
                                            Assertions.assertEquals(200, resDel.statusCode());
                                            assertSourceExistsInCache(redissonClient, token, false);
                                            testContext.completeNow();
                                        });
                                    }));
                        })
                );
    }


}
