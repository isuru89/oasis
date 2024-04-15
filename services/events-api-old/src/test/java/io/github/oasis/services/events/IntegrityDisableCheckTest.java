/*
 *
 *  * Licensed to the Apache Software Foundation (ASF) under one
 *  * or more contributor license agreements.  See the NOTICE file
 *  * distributed with this work for additional information
 *  * regarding copyright ownership.  The ASF licenses this file
 *  * to you under the Apache License, Version 2.0 (the
 *  * "License"); you may not use this file except in compliance
 *  * with the License.  You may obtain a copy of the License at
 *  *
 *  *    http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing,
 *  * software distributed under the License is distributed on an
 *  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  * KIND, either express or implied.  See the License for the
 *  * specific language governing permissions and limitations
 *  * under the License.
 *
 *
 */

package io.github.oasis.services.events;

import io.github.oasis.core.collect.Pair;
import io.github.oasis.services.events.utils.TestUtils;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.util.Set;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Isuru Weerarathna
 */
@DisplayName("Payload Integrity Disable")
public class IntegrityDisableCheckTest extends AbstractEventPushTest {

    @Override
    protected void modifyConfigs(JsonObject jsonObject) {
        JsonObject oasis = jsonObject.getJsonObject("http");
        oasis.put("skipEventIntegrityCheck", true);
    }

    @Test
    @DisplayName("Integrity Disabled: Event source does not exist should still fail")
    void sourceDoeNotExist(Vertx vertx, VertxTestContext testContext) throws NoSuchAlgorithmException {
        stubFor(get(urlPathEqualTo("/api/admin/event-source"))
            .withQueryParam("token", equalTo("pqrs"))
            .willReturn(notFound()));

        KeyPair keyPair = TestUtils.createKeys();
        awaitRedisInitialization(vertx, testContext, createKnownSource(keyPair));

        String hash = TestUtils.signPayload(VALID_PAYLOAD, keyPair.getPrivate());
        String bearer = "abcd:" + hash;

        callForEvent(vertx, bearer)
                .sendJson(
                        VALID_PAYLOAD,
                        testContext.succeeding(res -> assert401Response(res, testContext))
                );
    }

    @Test
    @DisplayName("Integrity Disabled: Payload changed after hash is generated should be successful")
    void hashIncorrect(Vertx vertx, VertxTestContext testContext) throws NoSuchAlgorithmException {
        KeyPair keyPair = TestUtils.createKeys();
        String sourceToken = UUID.randomUUID().toString();
        String userEmail = "mom.attack@oasis.io";
        setSourceExists(sourceToken, createEventSource(sourceToken, 1, Set.of(1), keyPair.getPublic()));
        setPlayerExists(userEmail, createPlayerWithTeam(userEmail, 500, Pair.of(200,1)));

        JsonObject validPayload = new JsonObject()
                .put("data", TestUtils.aEvent(userEmail, System.currentTimeMillis(), "event.a", 100));
        String hash = TestUtils.signPayload(validPayload, keyPair.getPrivate());
        JsonObject modified = validPayload.copy();
        modified.getJsonObject("data").put("email", "hacked.user@oasis.io");
        assertThat(modified.getJsonObject("data").getString("email"))
                .isNotEqualTo(validPayload.getJsonObject("data").getString("email"));

        callForEvent(vertx, sourceToken + ":" + hash)
                .sendJson(
                        modified,
                        testContext.succeeding(res -> assertSuccessWithInvocations(res, testContext, 1))
                );
    }

    @Test
    @DisplayName("Integrity Disabled: Payload with correct hash")
    void correctHash(Vertx vertx, VertxTestContext testContext) throws NoSuchAlgorithmException {
        KeyPair keyPair = TestUtils.createKeys();
        String sourceToken = UUID.randomUUID().toString();
        String userEmail = "mom.attack@oasis.io";
        setSourceExists(sourceToken, createEventSource(sourceToken, 1, Set.of(1), keyPair.getPublic()));
        setPlayerExists(userEmail, createPlayerWithTeam(userEmail, 500, Pair.of(200,1)));

        JsonObject validPayload = new JsonObject()
                .put("data", TestUtils.aEvent(userEmail, System.currentTimeMillis(), "event.a", 100));
        String hash = TestUtils.signPayload(validPayload, keyPair.getPrivate());

        callForEvent(vertx, sourceToken + ":" + hash)
                .sendJson(
                        validPayload,
                        testContext.succeeding(res -> assertSuccessWithInvocations(res, testContext, 1))
                );
    }
}
