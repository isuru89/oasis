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
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.authentication.AuthenticationProvider;
import io.vertx.ext.web.handler.HttpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.github.oasis.services.events.model.ApiKeyCredentials.SOURCE_ID;

/**
 * @author Isuru Weerarathna
 */
public class EventAuthProvider implements AuthenticationProvider {

    private static final Logger LOG = LoggerFactory.getLogger(EventAuthProvider.class);

    private static final HttpException NO_SOURCE = new HttpException(401);

    private final AuthService authService;

    public EventAuthProvider(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public void authenticate(JsonObject authInfo, Handler<AsyncResult<User>> handler) {
        String sourceId = authInfo.getString(SOURCE_ID);
        authService.readSource(sourceId, res -> {
            if (res.succeeded()) {
                handler.handle(Future.succeededFuture(res.result()));
            } else {
                LOG.warn("Given event source does not exist! {}", sourceId);
                handler.handle(Future.failedFuture(NO_SOURCE));
            }
        });
    }
}
