package io.github.oasis.services.events;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.junit.jupiter.MockServerExtension;
import org.mockserver.model.MediaType;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.redisson.config.Config;

import java.security.PublicKey;
import java.util.Base64;
import java.util.Objects;
import java.util.Set;

/**
 * @author Isuru Weerarathna
 */
@ExtendWith({ VertxExtension.class, MockServerExtension.class})
public abstract class AbstractTest {

    public static final int TEST_PORT = 8010;

    protected TestDispatcherService dispatcherService;

    protected ClientAndServer clientAndServer;

    @BeforeEach
    void beforeEach(Vertx vertx, VertxTestContext testContext, ClientAndServer clientAndServer) {
        this.clientAndServer = clientAndServer;

        cleanRedis();

        JsonObject adminConfigs = getAdminApiConfigs(clientAndServer);
        JsonObject cacheConfigs = new JsonObject().put("impl", RedisVerticle.class.getName());
        JsonObject dispatcherConf = new JsonObject().put("impl", "test:any").put("configs", new JsonObject());
        JsonObject testConfigs = new JsonObject()
                .put("http", new JsonObject().put("instances", 1).put("port", TEST_PORT))
                .put("oasis", new JsonObject().put("dispatcher", dispatcherConf)
                        .put("cache", cacheConfigs).put("adminApi", adminConfigs));
        dispatcherService = Mockito.spy(new TestDispatcherService());
        TestDispatcherVerticle dispatcherVerticle = new TestDispatcherVerticle(dispatcherService);
        DeploymentOptions options = new DeploymentOptions().setConfig(testConfigs);
        vertx.registerVerticleFactory(new TestDispatcherFactory(dispatcherVerticle));
        vertx.deployVerticle(new EventsApi(), options, testContext.completing());
    }

    private void cleanRedis() {
        Config redisConfigs = new Config();
        redisConfigs.useSingleServer()
                        .setAddress("redis://localhost:6379");
        RedissonClient redissonClient = Redisson.create(redisConfigs);
        redissonClient.getMap(ID.EVENT_API_CACHE_USERS_KEY, StringCodec.INSTANCE).delete();
        redissonClient.getMap(ID.EVENT_API_CACHE_SOURCES_KEY, StringCodec.INSTANCE).delete();
        redissonClient.shutdown();
    }

    private JsonObject getAdminApiConfigs(ClientAndServer clientAndServer) {
        return new JsonObject()
                .put("baseUrl", "http://localhost:" + clientAndServer.getLocalPort() + "/api")
                .put("eventSourceGet", "/admin/event-sources")
                .put("playerGet", "/players")
                .put("apiKey", "eventapi")
                .put("secretKey", "eventapi");
    }

    protected void setPlayerDoesNotExists(String email) {
        new MockServerClient("localhost", clientAndServer.getLocalPort())
                .when(org.mockserver.model.HttpRequest.request("/api/players")
                        .withMethod("GET")
                        .withQueryStringParameter("email", email)
                        .withQueryStringParameter("verbose", "true")
                ).respond(org.mockserver.model.HttpResponse.response().withStatusCode(404));
    }

    protected void setSourceDoesNotExists(String token) {
        new MockServerClient("localhost", clientAndServer.getLocalPort())
                .when(org.mockserver.model.HttpRequest.request("/api/admin/event-sources")
                        .withMethod("GET")
                        .withQueryStringParameter("token", token)
                        .withQueryStringParameter("withKey", "true")
                ).respond(org.mockserver.model.HttpResponse.response().withStatusCode(404));
    }

    protected void setPlayerExists(String email, JsonObject playerWithTeams) {
        new MockServerClient("localhost", clientAndServer.getLocalPort())
                .when(org.mockserver.model.HttpRequest.request("/api/players")
                        .withMethod("GET")
                        .withQueryStringParameter("email", email)
                        .withQueryStringParameter("verbose", "true")
                ).respond(org.mockserver.model.HttpResponse.response()
                        .withStatusCode(200)
                        .withBody(playerWithTeams.encode(), MediaType.APPLICATION_JSON)
                );
    }

    protected void setSourceExists(String token, JsonObject eventSource) {
        new MockServerClient("localhost", clientAndServer.getLocalPort())
                .when(org.mockserver.model.HttpRequest.request("/api/admin/event-sources")
                        .withMethod("GET")
                        .withQueryStringParameter("token", token)
                        .withQueryStringParameter("withKey", "true")
                ).respond(org.mockserver.model.HttpResponse.response()
                        .withStatusCode(200)
                        .withBody(eventSource.encode(), MediaType.APPLICATION_JSON)
                );
    }

    protected JsonObject createEventSource(String token, Integer id, Set<Integer> games, PublicKey publicKey) {
        return new JsonObject()
                .put("token", token)
                .put("games", games)
                .put("id", id)
                .put("secrets", new JsonObject().put("publicKey", Base64.getEncoder().encodeToString(publicKey.getEncoded())));
    }

    protected JsonObject createPlayerWithTeams(String email, long id, Pair<Integer, Integer>... teamToGameMapping) {
        JsonArray array = new JsonArray();
        for (Pair<Integer, Integer> pair : teamToGameMapping) {
            array.add(new JsonObject().put("id", pair.getLeft()).put("gameId", pair.getRight()));
        }

        return new JsonObject()
                .put("email", email)
                .put("id", id)
                .put("teams", array);
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
        vertx.deployVerticle(verticle, testContext.succeeding());
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
