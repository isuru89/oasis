package io.github.oasis.services.events;

import io.github.oasis.services.events.utils.TestDispatcherFactory;
import io.github.oasis.services.events.utils.TestDispatcherService;
import io.github.oasis.services.events.utils.TestDispatcherVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

/**
 * @author Isuru Weerarathna
 */
@ExtendWith(VertxExtension.class)
public abstract class AbstractTest {

    protected TestDispatcherService dispatcherService;

    @BeforeEach
    void beforeEach(Vertx vertx, VertxTestContext testContext) {
        JsonObject testConfigs = new JsonObject().put("oasis.dispatcher", "test:any");
        dispatcherService = Mockito.spy(new TestDispatcherService());
        TestDispatcherVerticle dispatcherVerticle = new TestDispatcherVerticle(dispatcherService);
        DeploymentOptions options = new DeploymentOptions().setConfig(testConfigs);
        vertx.registerVerticleFactory(new TestDispatcherFactory(dispatcherVerticle));
        vertx.deployVerticle(new EventsApi(), options, testContext.completing());
    }

    protected HttpRequest<Buffer> callToEndPoint(String endPoint, Vertx vertx) {
        WebClient client = WebClient.create(vertx);
        return client.get(8090, "localhost", endPoint);
    }

}
