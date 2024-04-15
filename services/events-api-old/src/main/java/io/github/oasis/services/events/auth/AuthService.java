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

import io.github.oasis.services.events.model.EventSource;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;

/**
 * @author Isuru Weerarathna
 */
@ProxyGen
public interface AuthService {

    String REQ_DIGEST = "__oasisdigest";

    String AUTH_SERVICE_QUEUE = "auth.service.queue";

    @GenIgnore
    static AuthService createProxy(Vertx vertx, String address) {
        return new AuthServiceVertxEBProxy(vertx, address);
    }

    @Fluent
    AuthService readSource(String sourceId, Handler<AsyncResult<EventSource>> handler);

}
