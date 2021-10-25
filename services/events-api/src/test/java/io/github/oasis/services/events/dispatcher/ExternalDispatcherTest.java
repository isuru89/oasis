/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.github.oasis.services.events.dispatcher;

import io.github.oasis.core.external.EventAsyncDispatcher;
import io.github.oasis.core.external.EventDispatcher;
import io.github.oasis.core.external.messages.EngineMessage;
import io.github.oasis.services.events.EventsApi;
import io.github.oasis.services.events.db.RedisVerticle;
import io.github.oasis.services.events.model.EventProxy;
import io.github.oasis.services.events.utils.TestDispatcherVerticle;
import io.github.oasis.services.events.utils.TestUtils;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

import java.io.IOException;

import static io.github.oasis.services.events.AbstractTest.TEST_PORT;

/**
 * @author Isuru Weerarathna
 */
@DisplayName("External Dispatcher Test")
@ExtendWith(VertxExtension.class)
public class ExternalDispatcherTest {

    public static final int SLEEP_MS = 2000;

    @Test
    @DisplayName("Loading synchronous dispatcher")
    void testLoadSyncDispatcher(Vertx vertx, VertxTestContext testContext) {
        String impl = SyncDispatcherSupport.class.getName();
        JsonObject dispatcherConf = new JsonObject().put("impl", "oasis:" + impl).put("configs", new JsonObject());
        JsonObject testConfigs = new JsonObject()
                .put("http", new JsonObject().put("instances", 1).put("port", TEST_PORT))
                .put("oasis", new JsonObject().put("dispatcher", dispatcherConf).put("cache", getDefaultRedisCacheConfigs()));
        DeploymentOptions options = new DeploymentOptions().setConfig(testConfigs);
        vertx.registerVerticleFactory(new DispatcherFactory());
        vertx.deployVerticle(new EventsApi(), options, testContext.succeedingThenComplete());
        sleepFor(SLEEP_MS);
        testContext.completeNow();
    }

    @Test
    @DisplayName("Loading asynchronous dispatcher")
    void testLoadASyncDispatcher(Vertx vertx, VertxTestContext testContext) {
        String impl = AsyncDispatcher.class.getName();
        JsonObject dispatcherConf = new JsonObject().put("impl", "oasis:" + impl).put("configs", new JsonObject());
        JsonObject testConfigs = new JsonObject()
                .put("http", new JsonObject().put("instances", 1).put("port", TEST_PORT))
                .put("oasis", new JsonObject().put("dispatcher", dispatcherConf).put("cache", getDefaultRedisCacheConfigs()));
        DeploymentOptions options = new DeploymentOptions().setConfig(testConfigs);
        vertx.registerVerticleFactory(new DispatcherFactory());
        vertx.deployVerticle(new EventsApi(), options, testContext.succeedingThenComplete());
        sleepFor(SLEEP_MS);
        testContext.completeNow();
    }

    @Test
    @DisplayName("Loading verticle reference dispatcher")
    void testLoadVerticleDispatcher(Vertx vertx, VertxTestContext testContext) {
        String impl = VerticleRefDispatcher.class.getName();
        JsonObject dispatcherConf = new JsonObject().put("impl", "oasis:" + impl).put("configs", new JsonObject());
        JsonObject testConfigs = new JsonObject()
                .put("http", new JsonObject().put("instances", 1).put("port", TEST_PORT))
                .put("oasis", new JsonObject().put("dispatcher", dispatcherConf).put("cache", getDefaultRedisCacheConfigs()));
        DeploymentOptions options = new DeploymentOptions().setConfig(testConfigs);
        vertx.registerVerticleFactory(new DispatcherFactory());
        vertx.deployVerticle(new EventsApi(), options, testContext.succeedingThenComplete());
        sleepFor(SLEEP_MS);
        testContext.completeNow();
    }

    @Test
    @DisplayName("Loading verticle reference failure without no-arg constructor")
    void testLoadVerticleDispatcherNoConstructor(Vertx vertx, VertxTestContext testContext) {
        String impl = TestDispatcherVerticle.class.getName();
        JsonObject dispatcherConf = new JsonObject().put("impl", "oasis:" + impl).put("configs", new JsonObject());
        JsonObject testConfigs = new JsonObject()
                .put("http", new JsonObject().put("instances", 1).put("port", TEST_PORT))
                .put("oasis", new JsonObject().put("dispatcher", dispatcherConf));
        DeploymentOptions options = new DeploymentOptions().setConfig(testConfigs);
        vertx.registerVerticleFactory(new DispatcherFactory());
        vertx.deployVerticle(new EventsApi(), options, testContext.failing(e -> {}));
        sleepFor(1000);
        testContext.completeNow();
    }

    @Test
    @DisplayName("Unknown dispatcher type")
    void testUnknownDispatcher(Vertx vertx, VertxTestContext testContext) {
        String impl = UnknownDispatcher.class.getName();
        JsonObject dispatcherConf = new JsonObject().put("impl", "oasis:" + impl).put("configs", new JsonObject());
        JsonObject testConfigs = new JsonObject()
                .put("http", new JsonObject().put("instances", 1).put("port", TEST_PORT))
                .put("oasis", new JsonObject().put("dispatcher", dispatcherConf));
        DeploymentOptions options = new DeploymentOptions().setConfig(testConfigs);
        vertx.registerVerticleFactory(new DispatcherFactory());
        vertx.deployVerticle(new EventsApi(), options, testContext.failing(e -> {}));
        sleepFor(500);
        testContext.completeNow();
    }

    @Test
    @DisplayName("Sync push")
    void testSyncPushDispatcher(Vertx vertx, VertxTestContext testContext) {
        SyncDispatcherSupport dispatcher = Mockito.spy(new SyncDispatcherSupport());
        WrappedDispatcherService service = new WrappedDispatcherService(vertx, dispatcher);
        EventProxy eventProxy = new EventProxy(
                TestUtils.aEvent("admin@oasis.com", System.currentTimeMillis(), "event.a", 100).put(EventProxy.USER_ID, 1)
        );
        service.pushEvent(eventProxy, res -> {
            try {
                Assertions.assertThat(res.succeeded()).isTrue();
                Mockito.verify(dispatcher, Mockito.times(1)).push(Mockito.any(EngineMessage.class));
            } catch (Exception e) {
                Assertions.fail(e.getMessage());
            } finally {
                testContext.completeNow();
            }
        });
    }

    @Test
    @DisplayName("Sync push Fail")
    void testSyncPushFailDispatcher(Vertx vertx, VertxTestContext testContext) {
        SyncDispatcherSupport dispatcher = Mockito.spy(new SyncDispatcherSupport(true));
        WrappedDispatcherService service = new WrappedDispatcherService(vertx, dispatcher);
        EventProxy eventProxy = new EventProxy(
                TestUtils.aEvent("admin@oasis.com", System.currentTimeMillis(), "event.a", 100).put(EventProxy.USER_ID, 1)
        );
        service.pushEvent(eventProxy, res -> {
            try {
                Assertions.assertThat(res.succeeded()).isFalse();
                Mockito.verify(dispatcher, Mockito.times(1)).push(Mockito.any(EngineMessage.class));
            } catch (Exception e) {
                Assertions.fail(e.getMessage());
            } finally {
                testContext.completeNow();
            }
        });
    }

    @Test
    @DisplayName("ASync push")
    void testASyncPushDispatcher() {
        AsyncDispatcher dispatcher = Mockito.spy(new AsyncDispatcher());
        WrappedAsyncDispatcherService service = new WrappedAsyncDispatcherService(dispatcher);
        EventProxy eventProxy = new EventProxy(
                TestUtils.aEvent("admin@oasis.com", System.currentTimeMillis(), "event.a", 100).put(EventProxy.USER_ID, 1)
        );
        service.pushEvent(eventProxy, res -> {
            try {
                Assertions.assertThat(res.succeeded()).isTrue();
                Mockito.verify(dispatcher, Mockito.times(1))
                        .pushAsync(Mockito.any(EngineMessage.class), Mockito.any(EventAsyncDispatcher.Handler.class));
            } catch (Exception e) {
                Assertions.fail(e.getMessage());
            }
        });
    }

    @Test
    @DisplayName("ASync push Fail")
    void testASyncPushFailDispatcher() {
        AsyncDispatcher dispatcher = Mockito.spy(new AsyncDispatcher(true));
        WrappedAsyncDispatcherService service = new WrappedAsyncDispatcherService(dispatcher);
        EventProxy eventProxy = new EventProxy(
                TestUtils.aEvent("admin@oasis.com", System.currentTimeMillis(), "event.a", 100).put(EventProxy.USER_ID, 1)
        );
        service.pushEvent(eventProxy, res -> {
            try {
                Assertions.assertThat(res.succeeded()).isFalse();
                Mockito.verify(dispatcher, Mockito.times(1))
                        .pushAsync(Mockito.any(EngineMessage.class), Mockito.any(EventAsyncDispatcher.Handler.class));
            } catch (Exception e) {
                Assertions.fail(e.getMessage());
            }
        });
    }

    @Test
    @DisplayName("ASync broadcast")
    void testASyncBroadcastDispatcher() {
        AsyncDispatcher dispatcher = Mockito.spy(new AsyncDispatcher());
        WrappedAsyncDispatcherService service = new WrappedAsyncDispatcherService(dispatcher);
        JsonObject jsonObject = TestUtils.aEvent("admin@oasis.com", System.currentTimeMillis(), "event.a", 100);
        service.broadcast(jsonObject, res -> {
            try {
                Assertions.assertThat(res.succeeded()).isTrue();
                Mockito.verify(dispatcher, Mockito.times(1))
                        .broadcastAsync(Mockito.any(), Mockito.any(EventAsyncDispatcher.Handler.class));
            } catch (Throwable e) {
                Assertions.fail(e.getMessage());
            }
        });
    }

    @Test
    @DisplayName("ASync broadcast Fail")
    void testASyncBroadcastFailDispatcher() {
        AsyncDispatcher dispatcher = Mockito.spy(new AsyncDispatcher(true));
        WrappedAsyncDispatcherService service = new WrappedAsyncDispatcherService(dispatcher);
        JsonObject jsonObject = TestUtils.aEvent("admin@oasis.com", System.currentTimeMillis(), "event.a", 100);
        service.broadcast(jsonObject, res -> {
            try {
                Assertions.assertThat(res.succeeded()).isFalse();
                Mockito.verify(dispatcher, Mockito.times(1))
                        .broadcastAsync(Mockito.any(), Mockito.any(EventAsyncDispatcher.Handler.class));
            } catch (Exception e) {
                Assertions.fail(e.getMessage());
            }
        });
    }

    @Test
    @DisplayName("Sync broadcast")
    void testSyncBroadcastDispatcher(Vertx vertx, VertxTestContext testContext) {
        SyncDispatcherSupport dispatcher = Mockito.spy(new SyncDispatcherSupport());
        WrappedDispatcherService service = new WrappedDispatcherService(vertx, dispatcher);
        JsonObject jsonObject = TestUtils.aEvent("admin@oasis.com", System.currentTimeMillis(), "event.a", 100);
        service.broadcast(jsonObject, res -> {
            try {
                Assertions.assertThat(res.succeeded()).isTrue();
                Mockito.verify(dispatcher, Mockito.times(1)).broadcast(Mockito.any());
            } catch (Exception e) {
                Assertions.fail(e.getMessage());
            } finally {
                testContext.completeNow();
            }
        });
    }

    @Test
    @DisplayName("Sync broadcast Fail")
    void testSyncBroadcastFailDispatcher(Vertx vertx, VertxTestContext testContext) {
        SyncDispatcherSupport dispatcher = Mockito.spy(new SyncDispatcherSupport(true));
        WrappedDispatcherService service = new WrappedDispatcherService(vertx, dispatcher);
        JsonObject jsonObject = TestUtils.aEvent("admin@oasis.com", System.currentTimeMillis(), "event.a", 100);
        service.broadcast(jsonObject, res -> {
            try {
                Assertions.assertThat(res.succeeded()).isFalse();
                Mockito.verify(dispatcher, Mockito.times(1)).broadcast(Mockito.any());
            } catch (Exception e) {
                Assertions.fail(e.getMessage());
            } finally {
                testContext.completeNow();
            }
        });
    }

    @AfterEach
    void afterEach(Vertx vertx, VertxTestContext testContext) {
        testContext.completeNow();
    }

    private JsonObject getDefaultRedisCacheConfigs() {
        return new JsonObject().put("impl", RedisVerticle.class.getName());
    }

    private void sleepFor(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static class UnknownDispatcher {

    }

    public static class VerticleRefDispatcher extends AbstractVerticle {

    }

    public static class SyncDispatcherSupport implements EventDispatcher {
        private static final RuntimeException NO_IMPL = new RuntimeException();
        private boolean throwError = false;

        public SyncDispatcherSupport() {
        }

        public SyncDispatcherSupport(boolean throwError) {
            this.throwError = throwError;
        }

        @Override
        public void init(DispatcherContext context) throws Exception {

        }

        @Override
        public void push(EngineMessage event) throws Exception {
            if (throwError) {
                throw new Exception();
            }
        }

        @Override
        public void broadcast(EngineMessage message) throws Exception {
            if (throwError) {
                throw new Exception();
            }
        }

        @Override
        public void close() throws IOException {

        }
    }

    public static class AsyncDispatcher implements EventAsyncDispatcher {
        private boolean throwError = false;

        public AsyncDispatcher() {
        }

        public AsyncDispatcher(boolean throwError) {
            this.throwError = throwError;
        }

        @Override
        public void pushAsync(EngineMessage event, Handler handler) {
            if (throwError) {
                handler.onFail(new Exception());
            } else {
                handler.onSuccess(null);
            }
        }

        @Override
        public void init(DispatcherContext context, Handler handler) {
            handler.onSuccess(this);
        }

        @Override
        public void broadcastAsync(EngineMessage message, Handler handler) {
            if (throwError) {
                handler.onFail(new Exception());
            } else {
                handler.onSuccess(null);
            }
        }

        @Override
        public void close() throws IOException {

        }
    }
}
