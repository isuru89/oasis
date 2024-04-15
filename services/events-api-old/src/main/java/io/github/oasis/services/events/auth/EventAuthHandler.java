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

import io.github.oasis.services.events.model.ApiKeyCredentials;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.HttpException;
import io.vertx.ext.web.handler.impl.HTTPAuthorizationHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Isuru Weerarathna
 */
public class EventAuthHandler extends HTTPAuthorizationHandler<EventAuthProvider> {

    private static final Logger LOG = LoggerFactory.getLogger(EventAuthHandler.class);
    private static final String COLON = ":";

    private static final HttpException BAD_HEADER = new HttpException(401, "Bad Header provided");

    public EventAuthHandler(EventAuthProvider authProvider) {
        super(authProvider, Type.BEARER, null);
    }

    @Override
    public void authenticate(RoutingContext context, Handler<AsyncResult<User>> handler) {
        parseAuthorization(context, result -> {
            if (result.succeeded()) {
                String[] dataParts = result.result().split(COLON);
                if (dataParts.length != 2) {
                    LOG.warn("Authorization header is invalid!");
                    handler.handle(Future.failedFuture(BAD_HEADER));
                    return;
                }

                String digest = dataParts[1];
                context.put(AuthService.REQ_DIGEST, digest);
                this.authProvider.authenticate(new ApiKeyCredentials(dataParts[0], digest), handler);
            } else {
                handler.handle(Future.failedFuture(result.cause()));
            }
        });
    }
}
