package io.github.oasis.services.events;

import io.github.oasis.services.events.utils.TestUtils;
import io.vertx.core.Vertx;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.codec.BodyCodec;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Isuru Weerarathna
 */
@DisplayName("Unauthorized 401 Checks")
public class UnauthorizedTest extends AbstractEventPushTest {

    @Test
    @DisplayName("No Authorization Header")
    void unauthorizedEventSend(Vertx vertx, VertxTestContext testContext) {
        callForEvent(vertx)
                .sendJson(
                    VALID_PAYLOAD,
                    testContext.succeeding(res -> assert401Response(res, testContext))
                );
    }

    @Test
    @DisplayName("Empty Authorization header")
    void emptyAuthHeader(Vertx vertx, VertxTestContext testContext) {
        callForEvent(vertx, "")
                .sendJson(
                        VALID_PAYLOAD,
                        testContext.succeeding(res -> assert401Response(res, testContext))
                );
    }

    @Test
    @DisplayName("Only Bearer type should accept")
    void invalidAuthType(Vertx vertx, VertxTestContext testContext) {
        callForEvent(vertx)
                .basicAuthentication("user", "pass123")
                .sendJson(
                        VALID_PAYLOAD,
                        testContext.succeeding(res -> assert401Response(res, testContext))
                );
    }

    @Test
    @DisplayName("Event source does not exist")
    void sourceDoeNotExist(Vertx vertx, VertxTestContext testContext) throws NoSuchAlgorithmException {
        KeyPair keyPair = TestUtils.createKeys();
        vertx.deployVerticle(createKnownSource(keyPair), testContext.succeeding());

        String hash = TestUtils.signPayload(VALID_PAYLOAD, keyPair.getPrivate());
        String bearer = "abcd:" + hash;

        callForEvent(vertx, bearer)
                .sendJson(
                        VALID_PAYLOAD,
                        testContext.succeeding(res -> assert401Response(res, testContext))
                );
    }

    @Test
    @DisplayName("Insufficient data on Authorization header")
    void badAuthHeader(Vertx vertx, VertxTestContext testContext) {
        callForEvent(vertx, "abcd")
                .sendJson(
                        VALID_PAYLOAD,
                        testContext.succeeding(res -> assert401Response(res, testContext))
                );
    }

    @Test
    @DisplayName("Extra unnecessary data on Authorization header")
    void badAuthHeaderMoreValues(Vertx vertx, VertxTestContext testContext) {
        callForEvent(vertx, "abcd efgh")
                .sendJson(
                        VALID_PAYLOAD,
                        testContext.succeeding(res -> assert401Response(res, testContext))
                );
    }

    private HttpRequest<String> callForEvent(Vertx vertx) {
        return super.callPushEvent(vertx)
                .as(BodyCodec.string());
    }

    private void assert401Response(HttpResponse<String> response, VertxTestContext ctx) {
        ctx.verify(() -> {
            assertThat(response.statusCode()).isEqualTo(401);
            ctx.completeNow();
        });
    }

}
