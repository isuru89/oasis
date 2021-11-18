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
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.serviceproxy.ServiceBinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Isuru Weerarathna
 */
public class ClientVerticle extends AbstractVerticle {

    private static final Logger LOG = LoggerFactory.getLogger(ClientVerticle.class);

    @Override
    public void start(Promise<Void> promise) {
        LOG.info("Starting client connections...");
        JsonObject configs = config();

        RedisService cacheService = RedisService.createProxy(vertx, RedisService.DB_SERVICE_QUEUE);
        WebClientOptions clientOptions = new WebClientOptions();
        clientOptions.setUserAgent(AdminConstants.EVENT_API_USER_AGENT);

        AdminApiClient apiClient = new AdminApiClient(WebClient.create(vertx, clientOptions), configs);

        CachedApiClient cachedApiClient = new CachedApiClient(cacheService, apiClient);

        // register data service (adminApi with cached)
        new ServiceBinder(vertx)
                .setAddress(DataService.DATA_SERVICE_QUEUE)
                .register(DataService.class, cachedApiClient);

        // register auth service
        new ServiceBinder(vertx)
                .setAddress(AuthService.AUTH_SERVICE_QUEUE)
                .register(AuthService.class, cachedApiClient);
        promise.complete();
    }

}
