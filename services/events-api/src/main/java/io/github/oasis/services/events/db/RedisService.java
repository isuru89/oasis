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

package io.github.oasis.services.events.db;

import io.github.oasis.services.events.model.EventSource;
import io.github.oasis.services.events.model.UserInfo;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;


/**
 * @author Isuru Weerarathna
 */
@ProxyGen
public interface RedisService {

    String DB_SERVICE_QUEUE = "db.service.queue";

    static RedisService createProxy(Vertx vertx, String address) {
        return new RedisServiceVertxEBProxy(vertx, address);
    }

    @Fluent
    RedisService readUserInfo(String email, Handler<AsyncResult<UserInfo>> resultHandler);

    @Fluent
    RedisService readSourceInfo(String sourceId, Handler<AsyncResult<EventSource>> resultHandler);

    @Fluent
    RedisService persistUserInfo(String email, UserInfo userInfo, Handler<AsyncResult<UserInfo>> resultHandler);

    @Fluent
    RedisService persistSourceInfo(String sourceId, EventSource eventSource, Handler<AsyncResult<EventSource>> resultHandler);

    @Fluent
    RedisService deleteKey(String key, Handler<AsyncResult<Boolean>> resultHandler);
}
