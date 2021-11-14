package io.github.oasis.services.events;

import io.github.oasis.core.collect.Pair;
import io.github.oasis.services.events.utils.TestUtils;
import io.vertx.core.Vertx;
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
@DisplayName("Payload Integrity")
public class PayloadIntegrityTest extends AbstractEventPushTest {

    @Test
    @DisplayName("Payload changed after hash is generated")
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
                        testContext.succeeding(res -> assert403Response(res, testContext))
                );
    }

    @Test
    @DisplayName("Payload with correct hash")
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
