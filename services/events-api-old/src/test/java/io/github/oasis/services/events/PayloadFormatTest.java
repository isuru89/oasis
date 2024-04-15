package io.github.oasis.services.events;

import io.github.oasis.services.events.utils.TestUtils;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Isuru Weerarathna
 */
@DisplayName("Payload Format")
public class PayloadFormatTest extends AbstractEventPushTest {

    @Test
    @DisplayName("Payload content type incorrect")
    void payloadContentTypeIncorrect(Vertx vertx, VertxTestContext testContext) throws NoSuchAlgorithmException {
        KeyPair keyPair = TestUtils.createKeys();
        String sourceToken = UUID.randomUUID().toString();
        setSourceExists(sourceToken, createEventSource(sourceToken, 1, Set.of(1), keyPair.getPublic()));

        String payload = "isuru";
        String hash = TestUtils.signPayload(payload, keyPair.getPrivate());

        callForEvent(vertx, sourceToken + ":" + hash)
                .sendBuffer(
                        Buffer.buffer(payload),
                        testContext.succeeding(res -> assert400Response(res, testContext))
                );
    }

    @Test
    @DisplayName("Payload as json array not accepted")
    void payloadContentTypeArrayIncorrect(Vertx vertx, VertxTestContext testContext) throws NoSuchAlgorithmException {
        KeyPair keyPair = TestUtils.createKeys();
        String sourceToken = UUID.randomUUID().toString();
        setSourceExists(sourceToken, createEventSource(sourceToken, 1, Set.of(1), keyPair.getPublic()));

        JsonArray payload = new JsonArray().add(new JsonObject().put("data", KNOWN_USER));
        String hash = TestUtils.signPayload(payload, keyPair.getPrivate());

        callForEvent(vertx, sourceToken + ":" + hash)
                .sendJson(
                        payload,
                        testContext.succeeding(res -> assert400Response(res, testContext))
                );
    }

    @Test
    @DisplayName("Payload does not have a data field")
    void payloadFormatIncorrect(Vertx vertx, VertxTestContext testContext) throws NoSuchAlgorithmException {
        KeyPair keyPair = TestUtils.createKeys();
        String sourceToken = UUID.randomUUID().toString();
        setSourceExists(sourceToken, createEventSource(sourceToken, 1, Set.of(1), keyPair.getPublic()));

        JsonObject payload = new JsonObject().put("name", "isuru");
        String hash = TestUtils.signPayload(payload, keyPair.getPrivate());

        callForEvent(vertx, sourceToken + ":" + hash)
                .sendJson(
                        payload,
                        testContext.succeeding(res -> assert400Response(res, testContext))
                );
    }

    @Test
    @DisplayName("No such user exists")
    void noSuchUserExists(Vertx vertx, VertxTestContext testContext) throws NoSuchAlgorithmException {
        KeyPair keyPair = TestUtils.createKeys();
        String sourceToken = UUID.randomUUID().toString();
        setSourceExists(sourceToken, createEventSource(sourceToken, 1, Set.of(1), keyPair.getPublic()));

        JsonObject event = TestUtils.aEvent("unknown@oasis.com", System.currentTimeMillis(), "test.a", 100);
        JsonObject payload = new JsonObject().put("data", event);
        String hash = TestUtils.signPayload(payload, keyPair.getPrivate());

        callForEvent(vertx, sourceToken + ":" + hash)
                .sendJson(
                        payload,
                        testContext.succeeding(res -> {
                            testContext.verify(() -> {
                                assertThat(res.statusCode()).isEqualTo(400);
                                verifyPushTimes(0);
                                testContext.completeNow();
                            });
                        }));
    }

    private void assert400Response(HttpResponse<String> response, VertxTestContext ctx) {
        ctx.verify(() -> {
            assertThat(response.statusCode()).isEqualTo(400);
            ctx.completeNow();
        });
    }

}
