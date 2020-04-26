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
import io.github.oasis.services.events.db.RedisService;
import io.github.oasis.services.events.dispatcher.EventDispatcherService;
import io.github.oasis.services.events.model.EventProxy;
import io.github.oasis.services.events.model.EventSource;
import io.github.oasis.services.events.model.UserInfo;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.AuthHandler;
import io.vertx.ext.web.handler.BodyHandler;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.TimeZone;
import java.util.stream.Collectors;

/**
 * @author Isuru Weerarathna
 */
public class HttpServiceVerticle extends AbstractVerticle {

    private static final String APPLICATION_JSON = HttpHeaderValues.APPLICATION_JSON.toString();
    private static final String DATA = "data";
    private HttpServer server;

    private AuthService authService;
    private RedisService redisService;
    private EventDispatcherService dispatcherService;

    @Override
    public void start(Promise<Void> promise) {
        authService = AuthService.createProxy(vertx, AuthService.AUTH_SERVICE_QUEUE);
        redisService = RedisService.createProxy(vertx, RedisService.DB_SERVICE_QUEUE);
        dispatcherService = EventDispatcherService.createProxy(vertx, EventDispatcherService.DISPATCHER_SERVICE_QUEUE);

        AuthHandler authHandler = new EventAuthHandler(new EventAuthProvider(authService));

        HttpServerOptions serverOptions = new HttpServerOptions();
        server = vertx.createHttpServer(serverOptions);

        Router router = Router.router(vertx);
        router.get("/ping")
                .produces(APPLICATION_JSON)
                .handler(this::ping);
        Route eventsRouter = router.route("/api/event*");
        eventsRouter
                .consumes(APPLICATION_JSON)
                .produces(APPLICATION_JSON)
                .handler(BodyHandler.create())
                .handler(authHandler)
                .handler(this::verifyIntegrity);

        router.put("/api/events").handler(this::putEvents);
        router.put("/api/event").handler(this::putEvent);
        server.requestHandler(router);
        server.listen(8090, onListen -> {
            if (onListen.succeeded()) {
                promise.complete();
            } else {
                promise.fail(onListen.cause());
            }
        });
    }

    private void verifyIntegrity(RoutingContext ctx) {
        EventSource eventSource = (EventSource) ctx.user();
        Optional<String> optHeader = Optional.ofNullable(ctx.get("__oasisdigest"));
        if (optHeader.isPresent() && eventSource.verifyEvent(ctx.getBody(), optHeader.get())) {
            ctx.next();
        } else {
            ctx.fail(403, new IllegalArgumentException("xxx"));
        }
    }

    private void putEvents(RoutingContext context) {
        Optional<JsonArray> payloadArray = getEventPayloadAsArray(context.getBody());
        if (payloadArray.isEmpty()) {
            context.fail(400);
            return;
        }
        JsonArray eventArray = payloadArray.get();
        Iterator<Object> it = eventArray.iterator();
        EventSource source = asEventSource(context.user());
        while (it.hasNext()) {
            Object eventPayloadObj = it.next();
            Optional<EventProxy> eventProxy = asEvent(eventPayloadObj);
            if (eventProxy.isEmpty()) {
                context.fail(400);
                return;
            }

            // @TODO
        }
    }

    private void putEvent(RoutingContext context) {
        Optional<EventProxy> eventPayload = getEventPayloadAsObject(context.getBody());
        if (eventPayload.isEmpty()) {
            context.fail(400);
            return;
        }
        EventProxy event = eventPayload.get();
        EventSource source = asEventSource(context.user());
        String userEmail = event.getUserEmail();
        System.out.println(userEmail);
        redisService.readUserInfo(userEmail, res -> {
            if (res.succeeded()) {
                UserInfo user = res.result();
                List<Integer> gameIds = source.getGameIds().stream()
                        .filter(gId -> user.getTeamId(gId).isPresent())
                        .collect(Collectors.toList());
                for (int gameId : gameIds) {
                    user.getTeamId(gameId).ifPresent(teamId -> {
                        EventProxy gameEvent = event.copyForGame(gameId, source.getSourceId(), user.getId(), teamId);
                        dispatcherService.push(gameEvent, dispatcherRes -> {
                            System.out.println(dispatcherRes.succeeded());
                        });
                    });
                }
                context.response().end(new JsonObject().put("eventId", event.getExternalId()).toBuffer());
            } else {
                System.out.println(res.cause().getMessage());
                context.fail(400, res.cause());
            }
        });
    }

    private void ping(RoutingContext context) {
        context.response().end(Json.encodeToBuffer(new JsonObject()
            .put("tz", TimeZone.getDefault().getID())
            .put("offset", TimeZone.getDefault().getOffset(System.currentTimeMillis()) / 1000)
            .put("health", "OK")
        ));
    }

    private EventSource asEventSource(User user) {
        return (EventSource) user;
    }

    private Optional<EventProxy> asEvent(Object obj) {
        if (obj instanceof JsonObject) {
            return Optional.of(new EventProxy((JsonObject) obj));
        }
        return Optional.empty();
    }

    private Optional<JsonArray> getEventPayloadAsArray(Buffer body) {
        Object o = Json.decodeValue(body);
        if (!(o instanceof JsonObject)) {
            return Optional.empty();
        }
        JsonObject payload = (JsonObject) o;
        if (!payload.containsKey(DATA) || !(payload.getValue(DATA) instanceof JsonArray)) {
            return Optional.empty();
        }
        return Optional.of(payload.getJsonArray(DATA));
    }

    private Optional<EventProxy> getEventPayloadAsObject(Buffer body) {
        if (Objects.isNull(body)) {
            return Optional.empty();
        }

        Object o = Json.decodeValue(body);
        if (!(o instanceof JsonObject)) {
            return Optional.empty();
        }
        JsonObject payload = (JsonObject) o;
        if (!payload.containsKey(DATA) || !(payload.getValue(DATA) instanceof JsonObject)) {
            return Optional.empty();
        }
        return Optional.of(new EventProxy(payload.getJsonObject(DATA)));
    }

    @Override
    public void stop() {
        if (server != null) {
            System.out.println("Http server closing..." + context.deploymentID());
            server.close();
        }
    }
}
