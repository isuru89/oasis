/*
 *
 *  * Licensed to the Apache Software Foundation (ASF) under one
 *  * or more contributor license agreements.  See the NOTICE file
 *  * distributed with this work for additional information
 *  * regarding copyright ownership.  The ASF licenses this file
 *  * to you under the Apache License, Version 2.0 (the
 *  * "License"); you may not use this file except in compliance
 *  * with the License.  You may obtain a copy of the License at
 *  *
 *  *    http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing,
 *  * software distributed under the License is distributed on an
 *  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  * KIND, either express or implied.  See the License for the
 *  * specific language governing permissions and limitations
 *  * under the License.
 *
 *
 */

package io.github.oasis.services.events.client;

import io.github.oasis.services.events.auth.AuthService;
import io.github.oasis.services.events.db.DataService;
import io.github.oasis.services.events.db.RedisService;
import io.github.oasis.services.events.model.EventSource;
import io.github.oasis.services.events.model.UserInfo;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Isuru Weerarathna
 */
public class CachedApiClient implements DataService, AuthService {

    private static final Logger LOG = LoggerFactory.getLogger(CachedApiClient.class);

    private final RedisService cacheService;
    private final AdminApiClient apiClient;

    public CachedApiClient(RedisService cacheService, AdminApiClient apiClient) {
        this.cacheService = cacheService;
        this.apiClient = apiClient;
    }

    @Override
    public DataService readUserInfo(String email, Handler<AsyncResult<UserInfo>> resultHandler) {
        cacheService.readUserInfo(email, res -> {
            if (res.succeeded()) {
                UserInfo user = res.result();
                if (user != null) {
                    // cache hit
                    resultHandler.handle(Future.succeededFuture(user));
                    return;
                }

                LOG.debug("User details for user '{}' not found in the cache!", email);
                Promise<UserInfo> promise = Promise.promise();
                loadUserFromApi(email, promise);
                persistBackToCache(email, promise, resultHandler);

            } else {
                resultHandler.handle(Future.failedFuture(res.cause()));
            }
        });
        return this;
    }

    @Override
    public DataService readSourceInfo(String sourceToken, Handler<AsyncResult<EventSource>> resultHandler) {
        cacheService.readSourceInfo(sourceToken, res -> {
            if (res.succeeded()) {
                EventSource resultInCache = res.result();
                if (resultInCache == null) {
                    LOG.debug("Source by token '{}' not found in cache!", sourceToken);
                    Promise<EventSource> apiPromise = Promise.promise();
                    loadEventSourceFromApi(sourceToken, apiPromise);
                    persistEventSourceBackToCache(sourceToken, apiPromise, resultHandler);
                } else {
                    resultHandler.handle(Future.succeededFuture(resultInCache));
                }
            } else {
                resultHandler.handle(Future.failedFuture(res.cause()));
            }
        });
        return this;
    }

    private void persistEventSourceBackToCache(String sourceToken, Promise<EventSource> apiPromise, Handler<AsyncResult<EventSource>> handler) {
        apiPromise.future().onSuccess(eventSource -> cacheService.persistSourceInfo(sourceToken, eventSource, res -> {
            if (res.succeeded()) {
                handler.handle(Future.succeededFuture(eventSource));
            } else {
                handler.handle(Future.failedFuture(res.cause()));
            }
        })).onFailure(err -> {
            LOG.error("Unable to persist source details to the cache!", err);
            handler.handle(Future.failedFuture(err));
        });
    }

    private void loadEventSourceFromApi(String sourceId, Promise<EventSource> promise) {
        apiClient.readSourceInfo(sourceId, res -> {
            if (res.succeeded()) {
                promise.complete(res.result());
            } else {
                promise.fail(res.cause());
            }
        });
    }

    private void persistBackToCache(String email, Promise<UserInfo> promise, Handler<AsyncResult<UserInfo>> finalHandler) {
        promise.future().onSuccess(userInfo -> cacheService.persistUserInfo(email, userInfo, resultHandler -> {
            if (resultHandler.succeeded()) {
                finalHandler.handle(Future.succeededFuture(userInfo));
            } else {
                finalHandler.handle(Future.failedFuture(resultHandler.cause()));
            }
        })).onFailure(err -> {
            LOG.error("Unable to persist user details into the cache!", err);
            finalHandler.handle(Future.failedFuture(err));
        });
    }

    private void loadUserFromApi(String email, Promise<UserInfo> promise) {
        apiClient.readUserInfo(email, res -> {
            if (res.succeeded()) {
                promise.complete(res.result());
            } else {
                promise.fail(res.cause());
            }
        });
    }

    @Override
    public AuthService readSource(String sourceId, Handler<AsyncResult<EventSource>> handler) {
        this.readSourceInfo(sourceId, handler);
        return this;
    }
}
