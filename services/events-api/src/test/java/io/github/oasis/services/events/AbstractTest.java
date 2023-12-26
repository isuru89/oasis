package io.github.oasis.services.events;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.http.Body;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.redis.testcontainers.RedisContainer;
import io.github.oasis.core.ID;
import io.github.oasis.core.collect.Pair;
import io.github.oasis.services.events.db.RedisVerticle;
import io.github.oasis.services.events.utils.TestDispatcherFactory;
import io.github.oasis.services.events.utils.TestDispatcherService;
import io.github.oasis.services.events.utils.TestDispatcherVerticle;
import io.github.oasis.services.events.utils.TestRedisDeployVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.redisson.config.Config;
import org.springframework.http.MediaType;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.security.PublicKey;
import java.util.Base64;
import java.util.Objects;
import java.util.Set;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

/**
 * @author Isuru Weerarathna
 */
@ExtendWith({ VertxExtension.class })
@Testcontainers
@WireMockTest
public abstract class AbstractTest {

    public static final int TEST_PORT = 8010;

    @Container
    protected static final RedisContainer redis = new RedisContainer(DockerImageName.parse("redis:5"));

    @Rule
    public WireMockRule wireMock = new WireMockRule(WireMockConfiguration.wireMockConfig().dynamicPort());

    protected TestDispatcherService dispatcherService;

    @BeforeEach
    void beforeEach(Vertx vertx, VertxTestContext testContext, WireMockRuntimeInfo runtimeInfo) {
        cleanRedis();

        // clear wiremock mockings
        var wiremock = runtimeInfo.getWireMock();
        wiremock.resetMappings();
        wiremock.resetRequests();
        wiremock.resetScenarios();

        JsonObject adminConfigs = getAdminApiConfigs(runtimeInfo);
        JsonObject cacheConfigs = new JsonObject().put("impl", RedisVerticle.class.getName())
            .put("configs", new JsonObject().put("connectionString", redis.getRedisURI()));
        JsonObject dispatcherConf = new JsonObject().put("impl", "test:any").put("configs", new JsonObject());
        JsonObject testConfigs = new JsonObject()
                .put("http", new JsonObject().put("instances", 1).put("port", TEST_PORT))
                .put("oasis", new JsonObject().put("dispatcher", dispatcherConf)
                .put("cache", cacheConfigs).put("adminApi", adminConfigs));
        modifyConfigs(testConfigs);
        dispatcherService = Mockito.spy(new TestDispatcherService());
        TestDispatcherVerticle dispatcherVerticle = new TestDispatcherVerticle(dispatcherService);
        DeploymentOptions options = new DeploymentOptions().setConfig(testConfigs);
        vertx.registerVerticleFactory(new TestDispatcherFactory(dispatcherVerticle));
        vertx.deployVerticle(new EventsApi(), options, testContext.succeedingThenComplete());
    }

    protected void modifyConfigs(JsonObject jsonObject) {
        // do nothing
    }

    private void cleanRedis() {
        Config redisConfigs = new Config();
        redisConfigs.useSingleServer()
            .setAddress(redis.getRedisURI())
            .setConnectionMinimumIdleSize(1)
            .setConnectionPoolSize(2);
        RedissonClient redissonClient = Redisson.create(redisConfigs);
        redissonClient.getMap(ID.EVENT_API_CACHE_USERS_KEY, StringCodec.INSTANCE).delete();
        redissonClient.getMap(ID.EVENT_API_CACHE_SOURCES_KEY, StringCodec.INSTANCE).delete();
        redissonClient.shutdown();
    }

    private JsonObject getAdminApiConfigs(WireMockRuntimeInfo wireMockRuntime) {
        return new JsonObject()
                .put("baseUrl", "http://localhost:" + wireMockRuntime.getHttpPort() + "/api")
                .put("eventSourceGet", "/admin/event-source")
                .put("playerGet", "/players")
                .put("apiKey", "eventapi")
                .put("secretKey", "eventapi");
    }

    protected void setPlayerDoesNotExists(String email) {
        stubFor(get(urlPathEqualTo("/api/players"))
            .withQueryParam("email", equalTo(email))
            .withQueryParam("verbose", equalTo("true"))
            .willReturn(notFound()));
    }

    protected void setPlayerExists(String email, JsonObject playerWithTeams) {
        stubFor(get(urlPathEqualTo("/api/players"))
            .withQueryParam("email", equalTo(email))
            .withQueryParam("verbose", equalTo("true"))
            .willReturn(
                ok()
                    .withResponseBody(new Body(playerWithTeams.encode()))
                    .withHeader("content-type", MediaType.APPLICATION_JSON.toString())
            ));
    }

    protected void setSourceExists(String token, JsonObject eventSource) {
        stubFor(get(urlPathEqualTo("/api/admin/event-source"))
            .withQueryParam("token", equalTo(token))
            .withQueryParam("withKey", equalTo("true"))
            .willReturn(
                ok()
                    .withResponseBody(new Body(eventSource.encode()))
                    .withHeader("content-type", MediaType.APPLICATION_JSON.toString())
            ));
    }

    protected JsonObject createEventSource(String token, Integer id, Set<Integer> games, PublicKey publicKey) {
        return new JsonObject()
                .put("token", token)
                .put("games", games)
                .put("id", id)
                .put("secrets", new JsonObject().put("publicKey", Base64.getEncoder().encodeToString(publicKey.getEncoded())));
    }

    protected JsonObject createPlayerWith2Teams(String email, long id, Pair<Integer, Integer> pair1, Pair<Integer, Integer> pair2) {
        JsonArray array = new JsonArray();
        array.add(new JsonObject().put("id", pair1.getLeft()).put("gameId", pair1.getRight()));
        array.add(new JsonObject().put("id", pair2.getLeft()).put("gameId", pair2.getRight()));

        return new JsonObject()
                .put("email", email)
                .put("id", id)
                .put("teams", array);
    }

    protected JsonObject createPlayerWithTeam(String email, long id, Pair<Integer, Integer> pair) {
        JsonArray array = new JsonArray();
        array.add(new JsonObject().put("id", pair.getLeft()).put("gameId", pair.getRight()));

        return new JsonObject()
                .put("email", email)
                .put("id", id)
                .put("teams", array);
    }

    protected void sleepWell() {
        String relaxTime = System.getenv("OASIS_RELAX_TIME");
        if (Objects.nonNull(relaxTime)) {
            long slp = Long.parseLong(relaxTime);
            if (slp > 0) {
                try {
                    Thread.sleep(slp);
                } catch (InterruptedException e) {
                    // do nothing
                }
            }
        }
    }

    protected void awaitRedisInitialization(Vertx vertx, VertxTestContext testContext, TestRedisDeployVerticle verticle) {
        System.out.println(redis.getRedisURI());
        vertx.deployVerticle(verticle, testContext.succeedingThenComplete());
        sleepWell();
    }

    protected HttpRequest<Buffer> callToEndPoint(String endPoint, Vertx vertx) {
        WebClient client = WebClient.create(vertx);
        return client.get(TEST_PORT, "localhost", endPoint);
    }

    protected HttpRequest<Buffer> callToDeleteEndPoint(String endPoint, Vertx vertx) {
        WebClient client = WebClient.create(vertx);
        return client.delete(TEST_PORT, "localhost", endPoint);
    }
}
