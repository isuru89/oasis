package io.github.oasis.services.events.dispatcher;

import io.github.oasis.services.events.model.EventProxy;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rabbitmq.RabbitMQClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static io.github.oasis.services.events.dispatcher.RabbitMQDispatcherService.DEF_EVENT_EXCHANGE;
import static io.github.oasis.services.events.dispatcher.RabbitMQDispatcherService.generateRoutingKey;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Isuru Weerarathna
 */
@DisplayName("RabbitMQ Dispatcher Test")
@ExtendWith(VertxExtension.class)
public class RabbitMQTest {

    @Test
    @DisplayName("Routing key generation")
    void testRoutingKey() {
        assertThat(generateRoutingKey(createForGame(1))).isEqualTo("oasis.game.1");
        assertThat(generateRoutingKey(createForGame(89887))).isEqualTo("oasis.game.89887");
    }

    @Test
    @DisplayName("No exchange initialization when connection failed")
    void noInitializationWhenNoConnection(Vertx vertx, VertxTestContext context) {
        JsonObject configs = new JsonObject();
        RabbitMQClient mqClient = Mockito.spy(RabbitMQClient.create(vertx, configs));
        RabbitMQDispatcherService service = Mockito.spy(new RabbitMQDispatcherService(vertx, mqClient));

        {
            AsyncResult<Void> startResult = Mockito.mock(AsyncResult.class);
            Mockito.when(startResult.succeeded()).thenReturn(false);
            Mockito.doAnswer(new Answer() {
                @Override
                public Object answer(InvocationOnMock invocation) {
                    ((Handler<AsyncResult<Void>>) invocation.getArguments()[0]).handle(startResult);
                    return null;
                }
            }).when(mqClient).start(Mockito.any());
        }

        service.init(configs, context.failing());
        Mockito.verify(service, Mockito.never()).initializeExchanges(Mockito.any(), Mockito.any());
        context.completeNow();
    }

    @Test
    @DisplayName("Exchange Initialization with default values")
    void testInitialization(Vertx vertx, VertxTestContext context) {
        JsonObject configs = new JsonObject();
        RabbitMQClient mqClient = Mockito.spy(RabbitMQClient.create(vertx, configs));
        RabbitMQDispatcherService service = new RabbitMQDispatcherService(vertx, mqClient);

        {
            AsyncResult<Void> startResult = Mockito.mock(AsyncResult.class);
            Mockito.when(startResult.succeeded()).thenReturn(true);
            Mockito.doAnswer(new Answer() {
                @Override
                public Object answer(InvocationOnMock invocation) {
                    ((Handler<AsyncResult<Void>>) invocation.getArguments()[0]).handle(startResult);
                    return null;
                }
            }).when(mqClient).start(Mockito.any());
        }

        AsyncResult<Void> asyncResult = Mockito.mock(AsyncResult.class);
        Mockito.when(asyncResult.succeeded()).thenReturn(true);
        Mockito.doAnswer(new Answer() {
            int count = 0;
            @Override
            public Object answer(InvocationOnMock invocation) {
                count++;
                if (count == 1) {
                    assertThat((String) invocation.getArgument(0)).isEqualTo(RabbitMQDispatcherService.DEF_EVENT_EXCHANGE);
                    assertThat((String) invocation.getArgument(1)).isEqualTo(RabbitMQDispatcherService.DEF_EVENT_EXCHANGE_TYPE);
                    assertThat((boolean) invocation.getArgument(2)).isEqualTo(RabbitMQDispatcherService.DEF_EVENT_EXCHANGE_DURABLE);
                    assertThat((boolean) invocation.getArgument(3)).isEqualTo(RabbitMQDispatcherService.DEF_EVENT_EXCHANGE_AUTO_DEL);
                } else {
                    assertThat((String) invocation.getArgument(0)).isEqualTo(RabbitMQDispatcherService.DEF_BC_EXCHANGE);
                    assertThat((String) invocation.getArgument(1)).isEqualTo(RabbitMQDispatcherService.DEF_BC_EXCHANGE_TYPE);
                    assertThat((boolean) invocation.getArgument(2)).isEqualTo(RabbitMQDispatcherService.DEF_BC_EXCHANGE_DURABLE);
                    assertThat((boolean) invocation.getArgument(3)).isEqualTo(RabbitMQDispatcherService.DEF_BC_EXCHANGE_AUTO_DEL);
                }
                ((Handler<AsyncResult<Void>>) invocation.getArguments()[5]).handle(asyncResult);
                return null;
            }
        }).when(mqClient).exchangeDeclare(
                Mockito.anyString(),
                Mockito.anyString(),
                Mockito.anyBoolean(),
                Mockito.anyBoolean(),
                Mockito.any(JsonObject.class),
                Mockito.any());
        service.init(configs, context.succeeding());
        context.completeNow();
    }

    @Test
    @DisplayName("Exchange Initialization with custom configs")
    void testInitializationWithCustomConfigs(Vertx vertx, VertxTestContext context) {
        JsonObject configs = new JsonObject()
                .put("eventExchange", new JsonObject()
                        .put("name", "test.evt.ex")
                        .put("type", "topic")
                        .put("durable", false)
                        .put("autoDelete", true))
                .put("broadcastExchange", new JsonObject()
                        .put("name", "test.bc.ex")
                        .put("type", "direct")
                        .put("durable", false)
                        .put("autoDelete", true));
        RabbitMQClient mqClient = Mockito.spy(RabbitMQClient.create(vertx, configs));
        RabbitMQDispatcherService service = new RabbitMQDispatcherService(vertx, mqClient);
        AsyncResult<Void> asyncResult = Mockito.mock(AsyncResult.class);
        Mockito.when(asyncResult.succeeded()).thenReturn(true);
        Mockito.doAnswer(new Answer() {
            int count = 0;
            @Override
            public Object answer(InvocationOnMock invocation) {
                count++;
                if (count == 1) {
                    assertThat((String) invocation.getArgument(0)).isEqualTo("test.evt.ex");
                    assertThat((String) invocation.getArgument(1)).isEqualTo("topic");
                    assertThat((boolean) invocation.getArgument(2)).isEqualTo(false);
                    assertThat((boolean) invocation.getArgument(3)).isEqualTo(true);
                } else {
                    assertThat((String) invocation.getArgument(0)).isEqualTo("test.bc.ex");
                    assertThat((String) invocation.getArgument(1)).isEqualTo(RabbitMQDispatcherService.DEF_BC_EXCHANGE_TYPE);
                    assertThat((boolean) invocation.getArgument(2)).isEqualTo(false);
                    assertThat((boolean) invocation.getArgument(3)).isEqualTo(true);
                }
                ((Handler<AsyncResult<Void>>) invocation.getArguments()[5]).handle(asyncResult);
                return null;
            }
        }).when(mqClient).exchangeDeclare(
                Mockito.anyString(),
                Mockito.anyString(),
                Mockito.anyBoolean(),
                Mockito.anyBoolean(),
                Mockito.any(JsonObject.class),
                Mockito.any());
        service.initializeExchanges(mqClient, configs).onComplete(context.succeeding());
        context.completeNow();
    }

    @Test
    @DisplayName("Push Events Success")
    void testPushEvents(Vertx vertx, VertxTestContext context) {
        EventProxy event = createForGame(1);
        String routingKey = "oasis.game.1";
        JsonObject configs = new JsonObject();
        RabbitMQClient mqClient = Mockito.spy(RabbitMQClient.create(vertx, configs));
        RabbitMQDispatcherService service = new RabbitMQDispatcherService(vertx, mqClient);

        AsyncResult<Void> asyncResult = Mockito.mock(AsyncResult.class);
        Mockito.when(asyncResult.succeeded()).thenReturn(true);
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                assertThat((String) invocation.getArgument(0)).isEqualTo(DEF_EVENT_EXCHANGE);
                assertThat((String) invocation.getArgument(1)).isEqualTo(routingKey);
                ((Handler<AsyncResult<Void>>) invocation.getArguments()[3]).handle(asyncResult);
                return null;
            }
        }).when(mqClient).basicPublish(
                Mockito.anyString(),
                Mockito.anyString(),
                Mockito.any(JsonObject.class),
                Mockito.any());

        service.push(event, context.succeeding());
        context.completeNow();
    }

    @Test
    @DisplayName("Push Events Failure")
    void testPushEventsFail(Vertx vertx, VertxTestContext context) {
        EventProxy event = createForGame(1);
        String routingKey = "oasis.game.1";
        JsonObject configs = new JsonObject();
        RabbitMQClient mqClient = Mockito.spy(RabbitMQClient.create(vertx, configs));
        RabbitMQDispatcherService service = new RabbitMQDispatcherService(vertx, mqClient);

        AsyncResult<Void> asyncResult = Mockito.mock(AsyncResult.class);
        Mockito.when(asyncResult.succeeded()).thenReturn(false);
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                assertThat((String) invocation.getArgument(0)).isEqualTo(DEF_EVENT_EXCHANGE);
                assertThat((String) invocation.getArgument(1)).isEqualTo(routingKey);
                ((Handler<AsyncResult<Void>>) invocation.getArguments()[3]).handle(asyncResult);
                return null;
            }
        }).when(mqClient).basicPublish(
                Mockito.anyString(),
                Mockito.anyString(),
                Mockito.any(JsonObject.class),
                Mockito.any());

        service.push(event, context.failing());
        context.completeNow();
    }

    @Test
    @DisplayName("Hide sensitive info on logs")
    void hideSensitiveRabbitMQ() {
        RabbitMQVerticle verticle = new RabbitMQVerticle();
        JsonObject original = new JsonObject().put("host", "localhost").put("password", "thisissecret");
        JsonObject printable = verticle.printableRabbitConfigs(original);
        assertThat(printable).isNotSameAs(original);
        assertThat(printable.getString("password")).contains("****");
        assertThat(printable.getString("host")).isEqualTo("localhost");
    }

    private EventProxy createForGame(int gameId) {
        return new EventProxy(new JsonObject().put("game", gameId));
    }
}
