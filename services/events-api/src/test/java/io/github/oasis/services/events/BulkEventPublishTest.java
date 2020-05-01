package io.github.oasis.services.events;

import io.github.oasis.services.events.utils.TestUtils;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import static io.github.oasis.services.events.utils.TestUtils.aEvent;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Isuru Weerarathna
 */
@DisplayName("Bulk Event Publish")
public class BulkEventPublishTest extends AbstractEventPushTest {

    private static final String KNOWN_USER_2 = "player2@oasis.com";
    private static final String KNOWN_USER_3 = "player3@oasis.com";

    private static final JsonObject MULTI_PAYLOAD = new JsonObject()
            .put("data", new JsonArray()
                            .add(aEvent(KNOWN_USER, System.currentTimeMillis(), "event.a", 100))
                            .add(aEvent(KNOWN_USER_2, System.currentTimeMillis() - 100, "event.b", 150))
                            .add(aEvent(KNOWN_USER_3, System.currentTimeMillis() - 80, "event.a", 176))
                    );

    @Test
    @DisplayName("Publish bulk success")
    void successPublish(Vertx vertx, VertxTestContext testContext) throws NoSuchAlgorithmException {
        KeyPair keyPair = TestUtils.createKeys();
        awaitRedisInitialization(vertx, testContext, createKnownUser(createKnownSource(keyPair))
                .addUser(KNOWN_USER_2, 502, Map.of("1", new JsonObject().put("team", 200)))
                .addUser(KNOWN_USER_3, 503, Map.of("1", new JsonObject().put("team", 200))));

        String hash = TestUtils.signPayload(MULTI_PAYLOAD, keyPair.getPrivate());

        pushBulkEvents(vertx, KNOWN_SOURCE + ":" + hash)
                .sendJson(
                        MULTI_PAYLOAD,
                        testContext.succeeding(res -> assertSuccessWithInvocations(res, testContext, 3))
                );
    }

    @Test
    @DisplayName("Failure: Publish bulk in invalid format")
    void bulkInvalid(Vertx vertx, VertxTestContext testContext) throws NoSuchAlgorithmException {
        KeyPair keyPair = TestUtils.createKeys();
        awaitRedisInitialization(vertx, testContext, createKnownUser(createKnownSource(keyPair))
                        .addUser(KNOWN_USER_2, 502, Map.of("1", new JsonObject().put("team", 200)))
                        .addUser(KNOWN_USER_3, 503, Map.of("1", new JsonObject().put("team", 200))));

        JsonArray payload = new JsonArray().add(MULTI_PAYLOAD);
        String hash = TestUtils.signPayload(payload, keyPair.getPrivate());

        pushBulkEvents(vertx, KNOWN_SOURCE + ":" + hash)
                .sendJson(
                        payload,
                        testContext.succeeding(res -> assert400Response(res, testContext))
                );
    }

    private void assert400Response(HttpResponse<String> response, VertxTestContext ctx) {
        ctx.verify(() -> {
            assertThat(response.statusCode()).isEqualTo(400);
            ctx.completeNow();
        });
    }
}
