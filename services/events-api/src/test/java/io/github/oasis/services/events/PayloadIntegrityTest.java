package io.github.oasis.services.events;

import io.github.oasis.services.events.utils.TestUtils;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Isuru Weerarathna
 */
@DisplayName("Payload Integrity")
public class PayloadIntegrityTest extends AbstractEventPushTest {

    @Test
    @DisplayName("Payload changed after hash is generated")
    void hashIncorrect(Vertx vertx, VertxTestContext testContext) throws NoSuchAlgorithmException {
        KeyPair keyPair = TestUtils.createKeys();
        awaitRedisInitialization(vertx, testContext, createKnownUser(createKnownSource(keyPair)));

        String hash = TestUtils.signPayload(VALID_PAYLOAD, keyPair.getPrivate());
        JsonObject modified = VALID_PAYLOAD.copy();
        modified.getJsonObject("data").put("email", "player2@oasis.com");
        assertThat(modified.getJsonObject("data").getString("email"))
                .isNotEqualTo(VALID_PAYLOAD.getJsonObject("data").getString("email"));

        callForEvent(vertx, KNOWN_SOURCE + ":" + hash)
                .sendJson(
                        modified,
                        testContext.succeeding(res -> assert403Response(res, testContext))
                );
    }

    @Test
    @DisplayName("Payload with correct hash")
    void correctHash(Vertx vertx, VertxTestContext testContext) throws NoSuchAlgorithmException {
        KeyPair keyPair = TestUtils.createKeys();
        awaitRedisInitialization(vertx, testContext, createKnownUser(createKnownSource(keyPair)));

        String hash = TestUtils.signPayload(VALID_PAYLOAD, keyPair.getPrivate());

        callForEvent(vertx, KNOWN_SOURCE + ":" + hash)
                .sendJson(
                        VALID_PAYLOAD,
                        testContext.succeeding(res -> assertAcceptedResponse(res, testContext))
                );
    }

    private void assert403Response(HttpResponse<String> response, VertxTestContext ctx) {
        ctx.verify(() -> {
            assertThat(response.statusCode()).isEqualTo(403);
            ctx.completeNow();
        });
    }

    private void assertAcceptedResponse(HttpResponse<String> response, VertxTestContext ctx) {
        ctx.verify(() -> {
            assertThat(response.statusCode()).isEqualTo(202);
            ctx.completeNow();
        });
    }

}
