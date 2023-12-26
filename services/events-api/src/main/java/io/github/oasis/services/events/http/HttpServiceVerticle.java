/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.github.oasis.services.events.http;

import io.github.oasis.services.events.auth.AuthService;
import io.github.oasis.services.events.auth.EventAuthHandler;
import io.github.oasis.services.events.auth.EventAuthProvider;
import io.github.oasis.services.events.db.DataService;
import io.github.oasis.services.events.db.RedisService;
import io.github.oasis.services.events.dispatcher.EventDispatcherService;
import io.github.oasis.services.events.http.routers.CacheRoute;
import io.github.oasis.services.events.http.routers.PingRoute;
import io.github.oasis.services.events.http.routers.PutBulkEventsRoute;
import io.github.oasis.services.events.http.routers.PutEventRoute;
import io.github.oasis.services.events.model.EventApiConfigs;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.github.oasis.services.events.http.Constants.CONF_PORT;
import static io.github.oasis.services.events.http.Constants.ROUTE_BULK_EVENT_PUSH;
import static io.github.oasis.services.events.http.Constants.ROUTE_CACHE_DELETE;
import static io.github.oasis.services.events.http.Constants.ROUTE_EVENT_PUSH;
import static io.github.oasis.services.events.http.Constants.ROUTE_PING;

/**
 * @author Isuru Weerarathna
 */
public class HttpServiceVerticle extends AbstractVerticle {

    private static final Logger LOG = LoggerFactory.getLogger(HttpServiceVerticle.class);

    private static final int DEF_PORT = 8050;

    private static final String APPLICATION_JSON = HttpHeaderValues.APPLICATION_JSON.toString();
    private HttpServer server;

    @Override
    public void start(Promise<Void> promise) {
        LOG.info("Starting event api web service...");
        var httpConf = config();
        LOG.info("HTTP configs {}", httpConf.encodePrettily());

        var authService = AuthService.createProxy(vertx, AuthService.AUTH_SERVICE_QUEUE);
        var clientService = DataService.createProxy(vertx, DataService.DATA_SERVICE_QUEUE);
        var redisService = RedisService.createProxy(vertx, RedisService.DB_SERVICE_QUEUE);
        var dispatcherService = EventDispatcherService.createProxy(vertx, EventDispatcherService.DISPATCHER_SERVICE_QUEUE);

        var authHandler = new EventAuthHandler(new EventAuthProvider(authService));

        var serverOptions = new HttpServerOptions(httpConf);
        server = vertx.createHttpServer(serverOptions);

        var integrityHandler = EventIntegrityHandler.create(EventApiConfigs.create(httpConf));
        var eventErrorHandler = new EventErrorHandler();

        var cacheRoute = new CacheRoute(redisService);
        var putEventRoute = new PutEventRoute(clientService, dispatcherService);
        var putBulkEventsRoute = new PutBulkEventsRoute(clientService, dispatcherService);

        var router = Router.router(vertx);
        router.get(ROUTE_PING)
                .produces(APPLICATION_JSON)
                .handler(PingRoute.create());

        router.delete(ROUTE_CACHE_DELETE)
                .produces(APPLICATION_JSON)
                .handler(cacheRoute);

        Route eventsRouter = router.route("/api/event*");
        eventsRouter
                .consumes(APPLICATION_JSON)
                .produces(APPLICATION_JSON)
                .handler(BodyHandler.create())
                .handler(authHandler)
                .handler(integrityHandler);

        router.put(ROUTE_BULK_EVENT_PUSH)
                .handler(putBulkEventsRoute)
                .failureHandler(eventErrorHandler);
        router.put(ROUTE_EVENT_PUSH)
                .handler(putEventRoute)
                .failureHandler(eventErrorHandler);
        server.requestHandler(router);

        int port = httpConf.getInteger(CONF_PORT, DEF_PORT);
        LOG.info("Starting Events API on port {}...", port);
        server.listen(port, onListen -> {
            if (onListen.succeeded()) {
                LOG.info("Started listening for events on port {}", port);
                promise.complete();
            } else {
                LOG.error("Events API initialization failed!", onListen.cause());
                promise.fail(onListen.cause());
            }
        });
    }

    @Override
    public void stop() {
        if (server != null) {
            LOG.debug("Shutting down Event API...");
            server.close();
        }
    }
}
