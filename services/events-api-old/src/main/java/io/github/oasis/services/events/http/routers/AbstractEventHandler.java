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

package io.github.oasis.services.events.http.routers;

import io.github.oasis.services.events.db.DataService;
import io.github.oasis.services.events.dispatcher.EventDispatcherService;
import io.github.oasis.services.events.model.EventProxy;
import io.github.oasis.services.events.model.EventSource;
import io.github.oasis.services.events.model.UserInfo;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Isuru Weerarathna
 */
public abstract class AbstractEventHandler {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractEventHandler.class);

    static final String EVENTS_PAYLOAD_FORMAT_IS_INCORRECT = "Events payload format is incorrect!";

    private final DataService clientService;
    private final EventDispatcherService dispatcherService;

    protected AbstractEventHandler(DataService dataService, EventDispatcherService dispatcherService) {
        this.clientService = dataService;
        this.dispatcherService = dispatcherService;
    }

    protected void putEvent(EventProxy event, EventSource source, Handler<AsyncResult<Boolean>> handler) {
        String userEmail = event.getUserEmail();

        clientService.readUserInfo(userEmail, res -> {
            if (res.succeeded()) {
                LOG.info("[{}] User {} exists in Oasis", event.getExternalId(), userEmail);
                UserInfo user = res.result();
                if (user == null) {
                    LOG.warn("[{}] User {} does not exist in Oasis!", event.getExternalId(), userEmail);
                    handler.handle(Future.failedFuture(new IllegalArgumentException("No user exists by email " + userEmail + "!")));
                    return;
                }

                List<Integer> gameIds = source.getGameIds().stream()
                        .filter(gId -> user.getTeamId(gId).isPresent())
                        .collect(Collectors.toList());

                for (int gameId : gameIds) {
                    user.getTeamId(gameId).ifPresent(teamId -> {
                        EventProxy gameEvent = event.copyForGame(gameId, source.getSourceId(), user.getId(), teamId);
                        dispatcherService.pushEvent(gameEvent, dispatcherRes -> {
                            if (dispatcherRes.succeeded()) {
                                LOG.info("[{}] Event published.", event.getExternalId());
                            } else {
                                LOG.error("[{}] Unable to publish event!", event.getExternalId(), dispatcherRes.cause());
                            }
                        });
                    });
                }

                handler.handle(Future.succeededFuture(true));

                if (gameIds.isEmpty()) {
                    LOG.warn("[{}] No related games for this user {}!", event.getExternalId(), userEmail);
                }

            } else {
                LOG.warn("[{}] Cannot access user info for user {} from cache or service!", event.getExternalId(), userEmail);
                LOG.error("Error:", res.cause());
                handler.handle(Future.failedFuture(new IllegalArgumentException("Cannot accept this event at this moment " + userEmail + "!")));
            }
        });
    }

    protected void failWithInvalidPayloadFormat(RoutingContext context) {
        context.fail(HttpResponseStatus.BAD_REQUEST.code(),
                new IllegalArgumentException(EVENTS_PAYLOAD_FORMAT_IS_INCORRECT));
    }
}
