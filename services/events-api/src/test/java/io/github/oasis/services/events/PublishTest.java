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

import io.github.oasis.services.events.utils.TestRedisDeployVerticle;
import io.github.oasis.services.events.utils.TestUtils;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

/**
 * @author Isuru Weerarathna
 */
@DisplayName("Successful Event Publish")
public class PublishTest extends AbstractEventPushTest {

    @Test
    @DisplayName("Publish once success")
    void successPublish(Vertx vertx, VertxTestContext testContext) throws NoSuchAlgorithmException {
        KeyPair keyPair = TestUtils.createKeys();
        awaitRedisInitialization(vertx, testContext, createKnownUser(createKnownSource(keyPair)));

        String hash = TestUtils.signPayload(VALID_PAYLOAD, keyPair.getPrivate());

        callForEvent(vertx, KNOWN_SOURCE + ":" + hash)
                .sendJson(
                        VALID_PAYLOAD,
                        testContext.succeeding(res -> assertSuccessWithInvocations(res, testContext, 1))
                );
    }

    @Test
    @DisplayName("Server: Publish only for user assigned subset of games")
    void successPublishForOnlyGames(Vertx vertx, VertxTestContext testContext) throws NoSuchAlgorithmException {
        KeyPair keyPair = TestUtils.createKeys();
        awaitRedisInitialization(vertx, testContext, new TestRedisDeployVerticle()
                        .addSource("abc", 1, keyPair.getPublic(), List.of(1, 2, 3))
                        .addUser("isuru@oasis.com", 500,
                                Map.of("1", new JsonObject().put("team", 200), "2", new JsonObject().put("team", 201))
                        ));

        JsonObject event = TestUtils.aEvent("isuru@oasis.com", System.currentTimeMillis(), "test.a", 100);
        JsonObject payload = new JsonObject().put("data", event);
        String hash = TestUtils.signPayload(payload, keyPair.getPrivate());

        callForEvent(vertx, KNOWN_SOURCE + ":" + hash)
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
        awaitRedisInitialization(vertx, testContext, new TestRedisDeployVerticle()
                        .addSource("abc", 1, keyPair.getPublic(), List.of(1, 2))
                        .addUser("isuru@oasis.com", 500,
                                Map.of("1", new JsonObject().put("team", 200), "3", new JsonObject().put("team", 201))
                        ));

        JsonObject event = TestUtils.aEvent("isuru@oasis.com", System.currentTimeMillis(), "test.a", 100);
        JsonObject payload = new JsonObject().put("data", event);
        String hash = TestUtils.signPayload(payload, keyPair.getPrivate());

        callForEvent(vertx, KNOWN_SOURCE + ":" + hash)
                .sendJson(payload, testContext.succeeding(res -> assertSuccessWithInvocations(res, testContext, 1)));
    }

    @Test
    @DisplayName("Dispatching failure handling")
    void dispatcherFailed(Vertx vertx, VertxTestContext testContext) throws NoSuchAlgorithmException {
        KeyPair keyPair = TestUtils.createKeys();
        dispatcherService.setReturnSuccess(false);
        awaitRedisInitialization(vertx, testContext, createKnownUser(createKnownSource(keyPair)));

        String hash = TestUtils.signPayload(VALID_PAYLOAD, keyPair.getPrivate());

        callForEvent(vertx, KNOWN_SOURCE + ":" + hash)
                .sendJson(
                        VALID_PAYLOAD,
                        testContext.succeeding(res -> assertSuccessWithInvocations(res, testContext, 1))
                );
    }

}
