package io.github.oasis.services.events;

import io.github.oasis.core.utils.CacheUtils;
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
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;

import java.security.KeyPair;
import java.util.List;
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
        return client.get(TEST_PORT, "localhost", API_EVENT)
                .method(HttpMethod.PUT);
    }

    protected HttpRequest<String> callForEvent(Vertx vertx, String bearerHeader) {
        if (StringUtils.isBlank(bearerHeader)) {
            return callPushEvent(vertx)
                    .as(BodyCodec.string());
        }

        return callPushEvent(vertx)
                .bearerTokenAuthentication(bearerHeader)
                .as(BodyCodec.string());
    }

    protected HttpRequest<String> pushBulkEvents(Vertx vertx, String bearerHeader) {
        WebClient client = WebClient.create(vertx);
        return client.get(TEST_PORT, "localhost", API_EVENTS)
                .method(HttpMethod.PUT)
                .bearerTokenAuthentication(bearerHeader)
                .as(BodyCodec.string());
    }

    protected TestRedisDeployVerticle createKnownSource(KeyPair keyPair) {
        return new TestRedisDeployVerticle(redis.getRedisURI())
                .addSource(KNOWN_SOURCE, 1, keyPair.getPublic(), List.of(1));
    }

    protected void assertSuccessWithInvocations(HttpResponse<String> response, VertxTestContext ctx, int invocations) {
        ctx.verify(() -> {
            assertThat(response.statusCode()).isEqualTo(202);
            verifyPushTimes(invocations);
            ctx.completeNow();
        });
    }

    protected void assertSourceExistsInCache(RedissonClient redissonClient, String token, boolean checkExists) {
        String sourceCacheKey = CacheUtils.getSourceCacheKey(token);
        RBucket<String> bucket = redissonClient.getBucket(sourceCacheKey, StringCodec.INSTANCE);
        if (checkExists) {
            Assertions.assertTrue(bucket.isExists());
        } else {
            Assertions.assertFalse(bucket.isExists());
        }
    }

    protected void assertSuccess(HttpResponse<String> response, VertxTestContext ctx) {
        ctx.verify(() -> {
            assertThat(response.statusCode()).isEqualTo(202);
            ctx.completeNow();
        });
    }

    protected void verifyPushTimes(int invocations) {
        sleepWell();
        ArgumentCaptor<EventProxy> eventCapture = ArgumentCaptor.forClass(EventProxy.class);
        Mockito.verify(dispatcherService, Mockito.times(invocations)).pushEvent(eventCapture.capture(), Mockito.any());
        if (invocations > 0) {
            for (EventProxy eventProxy : eventCapture.getAllValues()) {
                assertTrue(Objects.nonNull(eventProxy.getExternalId()));
                assertTrue(Objects.nonNull(eventProxy.getSource()));
                assertTrue(Objects.nonNull(eventProxy.getGameId()));
                assertTrue(Objects.nonNull(eventProxy.getTeam()));
            }
        }
    }

    protected void assert401Response(HttpResponse<String> response, VertxTestContext ctx) {
        ctx.verify(() -> {
            assertThat(response.statusCode()).isEqualTo(401);
            ctx.completeNow();
        });
    }
}
