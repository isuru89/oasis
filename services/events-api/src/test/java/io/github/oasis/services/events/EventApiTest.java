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

import io.github.oasis.services.events.model.EventProxy;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpHeaders;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Isuru Weerarathna
 */
@ExtendWith(VertxExtension.class)
public class EventApiTest {

    private TestDispatcherService dispatcherService;

    @BeforeEach
    void beforeEach(Vertx vertx, VertxTestContext testContext) {
        JsonObject testConfigs = new JsonObject().put("oasis.dispatcher", "test:any");
        dispatcherService = Mockito.spy(new TestDispatcherService());
        TestDispatcherVerticle dispatcherVerticle = new TestDispatcherVerticle(dispatcherService);
        DeploymentOptions options = new DeploymentOptions().setConfig(testConfigs);
        vertx.registerVerticleFactory(new TestDispatcherFactory(dispatcherVerticle));
        vertx.deployVerticle(new EventsApi(), options, testContext.completing());
    }

    @Test
    @DisplayName("Server: Health check")
    void healthCheck(Vertx vertx, VertxTestContext testContext) {
        WebClient client = WebClient.create(vertx);
        client.get(8090, "localhost", "/ping")
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
    @DisplayName("Server: Empty auth header")
    void emptyAuthHeader(Vertx vertx, VertxTestContext testContext) {
        WebClient client = WebClient.create(vertx);
        client.get(8090, "localhost", "/api/event")
                .method(HttpMethod.PUT)
                .putHeader(HttpHeaders.AUTHORIZATION.toString(), "")
                .as(BodyCodec.string())
                .sendJson(new JsonObject().put("name", "isuru"), testContext.succeeding(res -> {
                    testContext.verify(() -> {
                        Assertions.assertEquals(401, res.statusCode());
                        testContext.completeNow();
                    });
                }));
    }

    @Test
    @DisplayName("Server: Only bearer type should grant")
    void invalidAuthType(Vertx vertx, VertxTestContext testContext) {
        WebClient client = WebClient.create(vertx);
        client.get(8090, "localhost", "/api/event")
                .method(HttpMethod.PUT)
                .basicAuthentication("user", "pass123")
                .as(BodyCodec.string())
                .sendJson(new JsonObject().put("name", "isuru"), testContext.succeeding(res -> {
                    testContext.verify(() -> {
                        Assertions.assertEquals(400, res.statusCode());
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
    @DisplayName("Server: Bad Auth Header having more values")
    void badAuthHeaderMoreValues(Vertx vertx, VertxTestContext testContext) {
        WebClient client = WebClient.create(vertx);
        client.get(8090, "localhost", "/api/event")
                .method(HttpMethod.PUT)
                .bearerTokenAuthentication("abcd efgh")
                .as(BodyCodec.string())
                .sendJson(new JsonObject().put("name", "isuru"), testContext.succeeding(res -> {
                    testContext.verify(() -> {
                        Assertions.assertEquals(400, res.statusCode());
                        testContext.completeNow();
                    });
                }));
    }

    @Test
    @DisplayName("Server: Event source does not exist")
    void sourceDoeNotExist(Vertx vertx, VertxTestContext testContext) throws NoSuchAlgorithmException {
        KeyPair keyPair = TestUtils.createKeys();
        vertx.deployVerticle(new TestRedisDeployVerticle()
                .addSource("abc", 1, keyPair.getPublic(), List.of(1)), testContext.succeeding());

        JsonObject payload = new JsonObject().put("name", "isuru");
        String hash = TestUtils.signPayload(payload, keyPair.getPrivate());

        WebClient client = WebClient.create(vertx);
        client.get(8090, "localhost", "/api/event")
                .method(HttpMethod.PUT)
                .bearerTokenAuthentication("abcd:" + hash)
                .as(BodyCodec.string())
                .sendJson(new JsonObject().put("name", "isuru2"), testContext.succeeding(res -> {
                    testContext.verify(() -> {
                        Assertions.assertEquals(401, res.statusCode());
                        testContext.completeNow();
                    });
                }));
    }

    @Test
    @DisplayName("Server: Auth Success but integrity violated")
    void authSuccessIntegrityFailed(Vertx vertx, VertxTestContext testContext) throws NoSuchAlgorithmException {
        KeyPair keyPair = TestUtils.createKeys();
        vertx.deployVerticle(new TestRedisDeployVerticle()
                .addSource("abc", 1, keyPair.getPublic(), List.of(1)), testContext.succeeding());

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
    @DisplayName("Server: Payload format incorrect")
    void payloadFormatIncorrect(Vertx vertx, VertxTestContext testContext) throws NoSuchAlgorithmException {
        KeyPair keyPair = TestUtils.createKeys();
        vertx.deployVerticle(new TestRedisDeployVerticle()
                .addSource("abc", 1, keyPair.getPublic(), List.of(1)), testContext.succeeding());

        JsonObject payload = new JsonObject().put("name", "isuru");
        String hash = TestUtils.signPayload(payload, keyPair.getPrivate());

        WebClient client = WebClient.create(vertx);
        client.get(8090, "localhost", "/api/event")
                .method(HttpMethod.PUT)
                .bearerTokenAuthentication("abc:" + hash)
                .as(BodyCodec.string())
                .sendJson(payload, testContext.succeeding(res -> {
                    testContext.verify(() -> {
                        Assertions.assertEquals(400, res.statusCode());
                        testContext.completeNow();
                    });
                }));
    }

    @Test
    @DisplayName("Server: Payload content type incorrect")
    void payloadContentTypeIncorrect(Vertx vertx, VertxTestContext testContext) throws NoSuchAlgorithmException {
        KeyPair keyPair = TestUtils.createKeys();
        vertx.deployVerticle(new TestRedisDeployVerticle().addSource("abc", 1, keyPair.getPublic(), List.of(1)), testContext.succeeding());

        String payload = "isuru";
        String hash = TestUtils.signPayload(payload, keyPair.getPrivate());

        WebClient client = WebClient.create(vertx);
        client.get(8090, "localhost", "/api/event")
                .method(HttpMethod.PUT)
                .bearerTokenAuthentication("abc:" + hash)
                .as(BodyCodec.string())
                .sendBuffer(Buffer.buffer(payload), testContext.succeeding(res -> {
                    testContext.verify(() -> {
                        Assertions.assertEquals(400, res.statusCode());
                        testContext.completeNow();
                    });
                }));
    }

    @Test
    @DisplayName("Server: No such user exists")
    void authSuccess(Vertx vertx, VertxTestContext testContext) throws NoSuchAlgorithmException {
        KeyPair keyPair = TestUtils.createKeys();
        vertx.deployVerticle(new TestRedisDeployVerticle()
                .addSource("abc", 1, keyPair.getPublic(), List.of(1)),
                testContext.succeeding());

        JsonObject event = TestUtils.aEvent("isuru@oasis.com", System.currentTimeMillis(), "test.a", 100);
        JsonObject payload = new JsonObject().put("data", event);
        String hash = TestUtils.signPayload(payload, keyPair.getPrivate());

        WebClient client = WebClient.create(vertx);
        client.get(8090, "localhost", "/api/event")
                .method(HttpMethod.PUT)
                .bearerTokenAuthentication("abc:" + hash)
                .as(BodyCodec.string())
                .sendJson(payload, testContext.succeeding(res -> {
                    testContext.verify(() -> {
                        System.out.println(res.body());
                        Assertions.assertEquals(400, res.statusCode());
                        verifyPushTimes(0);
                        testContext.completeNow();
                    });
                }));
    }

    @Test
    @DisplayName("Server: Publish once success")
    void successPublish(Vertx vertx, VertxTestContext testContext) throws NoSuchAlgorithmException {
        KeyPair keyPair = TestUtils.createKeys();
        vertx.deployVerticle(new TestRedisDeployVerticle()
                        .addSource("abc", 1, keyPair.getPublic(), List.of(1))
                        .addUser("isuru@oasis.com", 500, Map.of("1", new JsonObject().put("team", 200))),
                testContext.succeeding());

        JsonObject event = TestUtils.aEvent("isuru@oasis.com", System.currentTimeMillis(), "test.a", 100);
        JsonObject payload = new JsonObject().put("data", event);
        String hash = TestUtils.signPayload(payload, keyPair.getPrivate());

        WebClient client = WebClient.create(vertx);
        client.get(8090, "localhost", "/api/event")
                .method(HttpMethod.PUT)
                .bearerTokenAuthentication("abc:" + hash)
                .as(BodyCodec.string())
                .sendJson(payload, testContext.succeeding(res -> {
                    testContext.verify(() -> {
                        System.out.println(res.body());
                        Assertions.assertEquals(200, res.statusCode());
                        verifyPushTimes(1);
                        testContext.completeNow();
                    });
                }));
    }

    @Test
    @DisplayName("Server: Publish only for user assigned subset of games")
    void successPublishForOnlyGames(Vertx vertx, VertxTestContext testContext) throws NoSuchAlgorithmException {
        KeyPair keyPair = TestUtils.createKeys();
        vertx.deployVerticle(new TestRedisDeployVerticle()
                        .addSource("abc", 1, keyPair.getPublic(), List.of(1, 2, 3))
                        .addUser("isuru@oasis.com", 500,
                                Map.of("1", new JsonObject().put("team", 200), "2", new JsonObject().put("team", 201))
                        ),
                testContext.succeeding());

        JsonObject event = TestUtils.aEvent("isuru@oasis.com", System.currentTimeMillis(), "test.a", 100);
        JsonObject payload = new JsonObject().put("data", event);
        String hash = TestUtils.signPayload(payload, keyPair.getPrivate());

        WebClient client = WebClient.create(vertx);
        client.get(8090, "localhost", "/api/event")
                .method(HttpMethod.PUT)
                .bearerTokenAuthentication("abc:" + hash)
                .as(BodyCodec.string())
                .sendJson(payload, testContext.succeeding(res -> {
                    testContext.verify(() -> {
                        System.out.println(res.body());
                        Assertions.assertEquals(200, res.statusCode());
                        verifyPushTimes(2);
                        testContext.completeNow();
                    });
                }));
    }

    @Test
    @DisplayName("Server: Publish only for existing games")
    void successPublishForOnlyExistingGames(Vertx vertx, VertxTestContext testContext) throws NoSuchAlgorithmException {
        KeyPair keyPair = TestUtils.createKeys();
        vertx.deployVerticle(new TestRedisDeployVerticle()
                        .addSource("abc", 1, keyPair.getPublic(), List.of(1, 2))
                        .addUser("isuru@oasis.com", 500,
                                Map.of("1", new JsonObject().put("team", 200), "3", new JsonObject().put("team", 201))
                        ),
                testContext.succeeding());

        JsonObject event = TestUtils.aEvent("isuru@oasis.com", System.currentTimeMillis(), "test.a", 100);
        JsonObject payload = new JsonObject().put("data", event);
        String hash = TestUtils.signPayload(payload, keyPair.getPrivate());

        WebClient client = WebClient.create(vertx);
        client.get(8090, "localhost", "/api/event")
                .method(HttpMethod.PUT)
                .bearerTokenAuthentication("abc:" + hash)
                .as(BodyCodec.string())
                .sendJson(payload, testContext.succeeding(res -> {
                    testContext.verify(() -> {
                        System.out.println(res.body());
                        Assertions.assertEquals(200, res.statusCode());
                        verifyPushTimes(1);
                        testContext.completeNow();
                    });
                }));
    }

    private void verifyPushTimes(int invocations) {
        ArgumentCaptor<EventProxy> eventCapture = ArgumentCaptor.forClass(EventProxy.class);
        Mockito.verify(dispatcherService, Mockito.times(invocations)).push(eventCapture.capture(), Mockito.any());
        if (invocations > 0) {
            for (EventProxy eventProxy : eventCapture.getAllValues()) {
                Assertions.assertTrue(Objects.nonNull(eventProxy.getExternalId()));
                Assertions.assertTrue(Objects.nonNull(eventProxy.getSource()));
                Assertions.assertTrue(Objects.nonNull(eventProxy.getGameId()));
                Assertions.assertTrue(Objects.nonNull(eventProxy.getTeam()));
            }
        }
    }
}
