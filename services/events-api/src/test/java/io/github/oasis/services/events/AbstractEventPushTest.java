package io.github.oasis.services.events;

import io.github.oasis.services.events.model.EventProxy;
import io.github.oasis.services.events.utils.TestRedisDeployVerticle;
import io.github.oasis.services.events.utils.TestUtils;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Assertions;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.security.KeyPair;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Isuru Weerarathna
 */
public abstract class AbstractEventPushTest extends AbstractTest {

    protected static final String KNOWN_SOURCE = "abc";
    protected static final String KNOWN_USER = "player@oasis.com";

    protected static final JsonObject VALID_PAYLOAD = new JsonObject()
            .put("data", TestUtils.aEvent(KNOWN_USER, System.currentTimeMillis(), "event.a", 100));

    protected static final String API_EVENT = "/api/event";
    protected static final String API_EVENTS = "/api/events";

    protected HttpRequest<Buffer> callPushEvent(Vertx vertx) {
        WebClient client = WebClient.create(vertx);
        return client.get(8090, "localhost", API_EVENT)
                .method(HttpMethod.PUT);
    }

    protected HttpRequest<String> callForEvent(Vertx vertx, String bearerHeader) {
        return callPushEvent(vertx)
                .bearerTokenAuthentication(bearerHeader)
                .as(BodyCodec.string());
    }

    protected HttpRequest<String> pushBulkEvents(Vertx vertx, String bearerHeader) {
        WebClient client = WebClient.create(vertx);
        return client.get(8090, "localhost", API_EVENTS)
                .method(HttpMethod.PUT)
                .bearerTokenAuthentication(bearerHeader)
                .as(BodyCodec.string());
    }

    protected TestRedisDeployVerticle createKnownSource(KeyPair keyPair) {
        return new TestRedisDeployVerticle()
                .addSource(KNOWN_SOURCE, 1, keyPair.getPublic(), List.of(1));
    }

    protected TestRedisDeployVerticle createKnownUser(TestRedisDeployVerticle verticle) {
        return verticle
                .addUser(KNOWN_USER,
                        500,
                        Map.of("1", new JsonObject().put("team", 200))
                );
    }

    protected void assertSuccessWithInvocations(HttpResponse<String> response, VertxTestContext ctx, int invocations) {
        ctx.verify(() -> {
            assertThat(response.statusCode()).isEqualTo(202);
            verifyPushTimes(invocations);
            ctx.completeNow();
        });
    }

    protected void verifyPushTimes(int invocations) {
        sleepWell();
        ArgumentCaptor<EventProxy> eventCapture = ArgumentCaptor.forClass(EventProxy.class);
        Mockito.verify(dispatcherService, Mockito.times(invocations)).push(eventCapture.capture(), Mockito.any());
        if (invocations > 0) {
            for (EventProxy eventProxy : eventCapture.getAllValues()) {
                assertTrue(Objects.nonNull(eventProxy.getExternalId()));
                assertTrue(Objects.nonNull(eventProxy.getSource()));
                assertTrue(Objects.nonNull(eventProxy.getGameId()));
                assertTrue(Objects.nonNull(eventProxy.getTeam()));
            }
        }
    }

}
