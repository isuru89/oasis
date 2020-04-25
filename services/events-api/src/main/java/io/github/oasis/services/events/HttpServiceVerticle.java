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

package io.github.oasis.services.events;

import io.github.oasis.services.events.auth.AuthService;
import io.github.oasis.services.events.auth.EventAuthHandler;
import io.github.oasis.services.events.auth.EventAuthProvider;
import io.github.oasis.services.events.model.EventProxy;
import io.github.oasis.services.events.model.EventSource;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.AuthHandler;
import io.vertx.ext.web.handler.BodyHandler;

import java.util.Optional;
import java.util.TimeZone;

/**
 * @author Isuru Weerarathna
 */
public class HttpServiceVerticle extends AbstractVerticle {

    private HttpServer server;

    private AuthService authService;

    @Override
    public void start(Promise<Void> promise) {
        authService = AuthService.createProxy(vertx, AuthService.AUTH_SERVICE_QUEUE);

        AuthHandler authHandler = new EventAuthHandler(new EventAuthProvider(authService));

        HttpServerOptions serverOptions = new HttpServerOptions();
        server = vertx.createHttpServer(serverOptions);

        Router router = Router.router(vertx);
        router.get("/health").handler(this::healthCheck);
        router.route("/api/event*")
                .handler(BodyHandler.create())
                .handler(authHandler)
                .handler(ctx -> {
                    EventSource eventSource = (EventSource) ctx.user();
                    Optional<String> optHeader = EventAuthHandler.parseDigest(ctx.request().getHeader(HttpHeaders.AUTHORIZATION));
                    if (optHeader.isPresent() && eventSource.verifyEvent(ctx.getBody(), optHeader.get())) {
                        ctx.next();
                    } else {
                        ctx.fail(403);
                    }
                })
                .handler(ctx -> {
                    ctx.response().putHeader("X-Oasis-EventId", "sdsfdfdfd");
                    ctx.next();
                });
        router.put("/api/event").handler(this::putEvent);
        router.put("/api/events").handler(this::putEvents);
        server.requestHandler(router);
        server.listen(8090, onListen -> {
            if (onListen.succeeded()) {
                promise.complete();
            } else {
                promise.fail(onListen.cause());
            }
        });
    }

    private void putEvents(RoutingContext context) {
        context.response().end("OK");
    }

    private void putEvent(RoutingContext context) {
        Object o = Json.decodeValue(context.getBody());
        if (!(o instanceof JsonObject)) {
            context.fail(400);
            return;
        }
        EventProxy event = new EventProxy((JsonObject) o);
        User user = context.user();
        context.response().end("OK");
    }

    private void healthCheck(RoutingContext context) {
        context.response().end(Json.encodeToBuffer(new JsonObject()
            .put("tz", TimeZone.getDefault().getID())
            .put("offset", TimeZone.getDefault().getOffset(System.currentTimeMillis()) / 1000)
            .put("health", "OK")
        ));
    }

    @Override
    public void stop() {
        if (server != null) {
            System.out.println("Http server closing..." + context.deploymentID());
            server.close();
        }
    }
}
