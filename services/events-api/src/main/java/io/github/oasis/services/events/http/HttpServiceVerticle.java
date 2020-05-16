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

import io.github.oasis.core.external.messages.PersistedDef;
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
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.TimeZone;
import java.util.stream.Collectors;

import static io.github.oasis.services.events.http.Constants.CONF_PORT;
import static io.github.oasis.services.events.http.Constants.ROUTE_BULK_EVENT_PUSH;
import static io.github.oasis.services.events.http.Constants.ROUTE_EVENT_PUSH;

/**
 * @author Isuru Weerarathna
 */
public class HttpServiceVerticle extends AbstractVerticle {

    private static final Logger LOG = LoggerFactory.getLogger(HttpServiceVerticle.class);

    private static final int DEF_PORT = 8050;

    private static final Throwable NO_SUCH_USER_EXIST = new IllegalArgumentException("User does not exist!");
    private static final String APPLICATION_JSON = HttpHeaderValues.APPLICATION_JSON.toString();
    private static final String DATA = "data";
    private HttpServer server;

    private AuthService authService;
    private RedisService redisService;
    private EventDispatcherService dispatcherService;

    @Override
    public void start(Promise<Void> promise) {
        LOG.info("Starting event api web service...");
        JsonObject httpConf = config();
        LOG.info("HTTP configs {}", httpConf.encodePrettily());
        authService = AuthService.createProxy(vertx, AuthService.AUTH_SERVICE_QUEUE);
        redisService = RedisService.createProxy(vertx, RedisService.DB_SERVICE_QUEUE);
        dispatcherService = EventDispatcherService.createProxy(vertx, EventDispatcherService.DISPATCHER_SERVICE_QUEUE);

        AuthHandler authHandler = new EventAuthHandler(new EventAuthProvider(authService));

        HttpServerOptions serverOptions = new HttpServerOptions(httpConf);
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

        router.put(ROUTE_BULK_EVENT_PUSH).handler(this::putEvents);
        router.put(ROUTE_EVENT_PUSH).handler(this::putEventHandler);
        server.requestHandler(router);

        int port = httpConf.getInteger(CONF_PORT, DEF_PORT);
        server.listen(port, onListen -> {
            if (onListen.succeeded()) {
                LOG.info("Listening for events on port {}", port);
                promise.complete();
            } else {
                LOG.error("Events API initialization failed!", onListen.cause());
                promise.fail(onListen.cause());
            }
        });
    }

    private void verifyIntegrity(RoutingContext ctx) {
        EventSource eventSource = (EventSource) ctx.user();
        Optional<String> optHeader = Optional.ofNullable(ctx.get(AuthService.REQ_DIGEST));
        if (optHeader.isPresent() && eventSource.verifyEvent(ctx.getBody(), optHeader.get())) {
            ctx.next();
        } else {
            LOG.warn("Payload verification failed!");
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
        JsonArray submittedEvents = new JsonArray();
        while (it.hasNext()) {
            Object eventPayloadObj = it.next();
            Optional<EventProxy> eventProxy = asEvent(eventPayloadObj);
            if (eventProxy.isEmpty()) {
                context.fail(400);
                return;
            }

            EventProxy event = eventProxy.get();
            putEvent(event, source, res -> {
                if (res.failed()) {
                    LOG.warn("Unable to publish event! {}", event);
                }
            });
            submittedEvents.add(event.getExternalId());
        }
        context.response().setStatusCode(202).end(new JsonObject().put("events", submittedEvents).toBuffer());
    }

    private void putEventHandler(RoutingContext context) {
        Optional<EventProxy> eventPayload = getEventPayloadAsObject(context.getBody());
        if (eventPayload.isEmpty()) {
            LOG.warn("Event payload does not comply to the accepted format!");
            context.fail(400);
            return;
        }
        EventSource source = asEventSource(context.user());
        EventProxy event = eventPayload.get();
        putEvent(event, source, res -> {
            if (res.succeeded()) {
                context.response().setStatusCode(202).end(new JsonObject().put("eventId", event.getExternalId()).toBuffer());
            } else {
                context.fail(400, res.cause());
            }
        });
    }

    private void putEvent(EventProxy event, EventSource source, Handler<AsyncResult<Boolean>> handler) {
        String userEmail = event.getUserEmail();
        redisService.readUserInfo(userEmail, res -> {
            if (res.succeeded()) {
                LOG.info("User {} exists in Oasis", userEmail);
                UserInfo user = res.result();
                List<Integer> gameIds = source.getGameIds().stream()
                        .filter(gId -> user.getTeamId(gId).isPresent())
                        .collect(Collectors.toList());
                for (int gameId : gameIds) {
                    user.getTeamId(gameId).ifPresent(teamId -> {
                        EventProxy gameEvent = event.copyForGame(gameId, source.getSourceId(), user.getId(), teamId);
                        dispatcherService.pushEvent(gameEvent, dispatcherRes -> {
                            if (dispatcherRes.succeeded()) {
                                LOG.info("Event published {}", dispatcherRes.result());
                            } else {
                                LOG.error("Unable to publish event! {}", gameEvent, dispatcherRes.cause());
                            }
                        });
                    });
                }
                handler.handle(Future.succeededFuture(true));
            } else {
                LOG.warn("User {} does not exist in Oasis!", userEmail);
                handler.handle(Future.failedFuture(NO_SUCH_USER_EXIST));
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
            LOG.debug("Event API shutting down...");
            server.close();
        }
    }
}
