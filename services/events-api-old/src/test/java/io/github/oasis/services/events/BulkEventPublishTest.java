package io.github.oasis.services.events;

import io.github.oasis.core.collect.Pair;
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
import java.util.Set;
import java.util.UUID;

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
        String sourceToken = UUID.randomUUID().toString();
        setSourceExists(sourceToken, createEventSource(sourceToken, 1, Set.of(1), keyPair.getPublic()));
        String player1 = "player.1000@oasis.io";
        String player2 = "player.1001@oasis.io";
        String player3 = "player.1002@oasis.io";
        setPlayerExists(player1, createPlayerWithTeam(player1, 501, Pair.of(200,1)));
        setPlayerExists(player2, createPlayerWithTeam(player2, 502, Pair.of(200,1)));
        setPlayerExists(player3, createPlayerWithTeam(player3, 503, Pair.of(200,1)));

        JsonObject multiPayload = new JsonObject()
                .put("data", new JsonArray()
                        .add(aEvent(player1, System.currentTimeMillis(), "event.a", 100))
                        .add(aEvent(player2, System.currentTimeMillis() - 100, "event.b", 150))
                        .add(aEvent(player3, System.currentTimeMillis() - 80, "event.a", 176))
                );
        String hash = TestUtils.signPayload(multiPayload, keyPair.getPrivate());

        pushBulkEvents(vertx, sourceToken + ":" + hash)
                .sendJson(
                        multiPayload,
                        testContext.succeeding(res -> assertSuccess(res, testContext))
                );
    }

    @Test
    @DisplayName("Failure: Publish bulk in invalid format")
    void bulkInvalid(Vertx vertx, VertxTestContext testContext) throws NoSuchAlgorithmException {
        KeyPair keyPair = TestUtils.createKeys();
        String sourceToken = UUID.randomUUID().toString();
        setSourceExists(sourceToken, createEventSource(sourceToken, 1, Set.of(1), keyPair.getPublic()));
        String player1 = "player.2000@oasis.io";
        String player2 = "player.2001@oasis.io";
        String player3 = "player.2002@oasis.io";
        setPlayerExists(player1, createPlayerWithTeam(player1, 501, Pair.of(200,1)));
        setPlayerExists(player2, createPlayerWithTeam(player2, 502, Pair.of(200,1)));
        setPlayerExists(player3, createPlayerWithTeam(player3, 503, Pair.of(200,1)));

        JsonObject multiPayload = new JsonObject()
                .put("data", new JsonArray()
                        .add(aEvent(player1, System.currentTimeMillis(), "event.a", 100))
                        .add(aEvent(player2, System.currentTimeMillis() - 100, "event.b", 150))
                        .add(aEvent(player3, System.currentTimeMillis() - 80, "event.a", 176))
                );
        JsonArray payload = new JsonArray().add(multiPayload);
        String hash = TestUtils.signPayload(payload, keyPair.getPrivate());

        pushBulkEvents(vertx, sourceToken + ":" + hash)
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
