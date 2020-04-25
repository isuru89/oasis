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

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;

/**
 * @author Isuru Weerarathna
 */
@ExtendWith(VertxExtension.class)
public class EventApiTest {

    private TestDispatcherVerticle dispatcherVerticle;

    @BeforeEach
    void beforeEach(Vertx vertx, VertxTestContext testContext) {
        JsonObject testConfigs = new JsonObject().put("oasis.dispatcher", "test:any");
        dispatcherVerticle = new TestDispatcherVerticle();
        DeploymentOptions options = new DeploymentOptions().setConfig(testConfigs);
        vertx.registerVerticleFactory(new TestDispatcherFactory(dispatcherVerticle));
        vertx.deployVerticle(new EventsApi(), options, testContext.completing());
    }

    @Test
    @DisplayName("Server: Health check")
    void healthCheck(Vertx vertx, VertxTestContext testContext) {
        WebClient client = WebClient.create(vertx);
        client.get(8090, "localhost", "/health")
                .as(BodyCodec.jsonObject())
                .send(testContext.succeeding(res -> {
                    testContext.verify(() -> {
                        Assertions.assertTrue(res.body().containsKey("tz"));
                        Assertions.assertTrue(res.body().containsKey("offset"));
                        Assertions.assertTrue(res.body().containsKey("health"));
                        testContext.completeNow();
                    });
                }));
    }

    @Test
    @DisplayName("Server: No Auth Header")
    void unauthorizedEventSend(Vertx vertx, VertxTestContext testContext) {
        WebClient client = WebClient.create(vertx);
        client.get(8090, "localhost", "/api/event")
                .method(HttpMethod.PUT)
                .as(BodyCodec.string())
                .sendJson(new JsonObject().put("name", "isuru"), testContext.succeeding(res -> {
                    testContext.verify(() -> {
                        Assertions.assertEquals(401, res.statusCode());
                        testContext.completeNow();
                    });
                }));
    }

    @Test
    @DisplayName("Server: Bad Auth Header")
    void badAuthHeader(Vertx vertx, VertxTestContext testContext) {
        WebClient client = WebClient.create(vertx);
        client.get(8090, "localhost", "/api/event")
                .method(HttpMethod.PUT)
                .bearerTokenAuthentication("abcd")
                .as(BodyCodec.string())
                .sendJson(new JsonObject().put("name", "isuru"), testContext.succeeding(res -> {
                    testContext.verify(() -> {
                        Assertions.assertEquals(400, res.statusCode());
                        testContext.completeNow();
                    });
                }));
    }

    @Test
    @DisplayName("Server: Auth Success but integrity violated")
    void authSuccessIntegrityFailed(Vertx vertx, VertxTestContext testContext) throws NoSuchAlgorithmException {
        KeyPair keyPair = TestUtils.createKeys();
        vertx.deployVerticle(new TestRedisDeployVerticle().addSource("abc", keyPair.getPublic()), testContext.succeeding());

        JsonObject payload = new JsonObject().put("name", "isuru");
        String hash = TestUtils.signPayload(payload, keyPair.getPrivate());

        WebClient client = WebClient.create(vertx);
        client.get(8090, "localhost", "/api/event")
                .method(HttpMethod.PUT)
                .bearerTokenAuthentication("abc:" + hash)
                .as(BodyCodec.string())
                .sendJson(new JsonObject().put("name", "isuru2"), testContext.succeeding(res -> {
                    testContext.verify(() -> {
                        Assertions.assertEquals(403, res.statusCode());
                        testContext.completeNow();
                    });
                }));
    }

    @Test
    @DisplayName("Server: Auth Success")
    void authSuccess(Vertx vertx, VertxTestContext testContext) throws NoSuchAlgorithmException {
        KeyPair keyPair = TestUtils.createKeys();
        vertx.deployVerticle(new TestRedisDeployVerticle().addSource("abc", keyPair.getPublic()), testContext.succeeding());

        JsonObject payload = new JsonObject().put("name", "isuru");
        String hash = TestUtils.signPayload(payload, keyPair.getPrivate());

        WebClient client = WebClient.create(vertx);
        client.get(8090, "localhost", "/api/event")
                .method(HttpMethod.PUT)
                .bearerTokenAuthentication("abc:" + hash)
                .as(BodyCodec.string())
                .sendJson(new JsonObject().put("name", "isuru"), testContext.succeeding(res -> {
                    testContext.verify(() -> {
                        Assertions.assertEquals(200, res.statusCode());
                        testContext.completeNow();
                    });
                }));
    }
}
