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

package io.github.oasis.services.events.auth;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.impl.AuthHandlerImpl;
import io.vertx.ext.web.handler.impl.HttpStatusException;

import java.util.Optional;

/**
 * @author Isuru Weerarathna
 */
public class EventAuthHandler extends AuthHandlerImpl {

    private static final HttpStatusException UNAUTHORIZED = new HttpStatusException(401);
    private static final HttpStatusException BAD_HEADER = new HttpStatusException(401, "Bad Header provided");

    private static final String BEARER = "Bearer";

    public EventAuthHandler(AuthProvider authProvider) {
        super(authProvider);
    }

    @Override
    protected String authenticateHeader(RoutingContext context) {
        return BEARER;
    }

    @Override
    public void authorize(User user, Handler<AsyncResult<Void>> handler) {
        handler.handle(Future.succeededFuture());
    }

    @Override
    public void parseCredentials(RoutingContext context, Handler<AsyncResult<JsonObject>> handler) {
        String authorization = context.request().headers().get(HttpHeaders.AUTHORIZATION);
        if (authorization == null || authorization.isEmpty()) {
            handler.handle(Future.failedFuture(UNAUTHORIZED));
            return;
        }

        String[] parts = authorization.split(" ");
        if (parts.length != 2 || !parts[0].equals(BEARER)) {
            handler.handle(Future.failedFuture(BAD_HEADER));
            return;
        }
        String[] dataParts = parts[1].split(":");
        if (dataParts.length != 2) {
            handler.handle(Future.failedFuture(BAD_HEADER));
            return;
        }
        context.put("__oasisdigest", dataParts[1]);
        handler.handle(Future.succeededFuture(new JsonObject().put("id", dataParts[0]).put("digest", dataParts[1])));
    }
}
