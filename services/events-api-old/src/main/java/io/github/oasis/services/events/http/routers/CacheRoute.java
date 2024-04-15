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

package io.github.oasis.services.events.http.routers;

import io.github.oasis.core.utils.CacheUtils;
import io.github.oasis.services.events.db.RedisService;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

/**
 * @author Isuru Weerarathna
 */
public class CacheRoute implements Handler<RoutingContext> {

    private static final String PARAM_CACHE_TYPE = "cacheType";
    private static final String PARAM_CACHE_ID = "cacheId";

    private static final String CACHE_TYPE_SOURCE = "sources";

    private final RedisService redisService;

    public CacheRoute(RedisService redisService) {
        this.redisService = redisService;
    }

    @Override
    public void handle(RoutingContext ctx) {
        String cacheType = ctx.pathParam(PARAM_CACHE_TYPE);
        String cacheId = ctx.pathParam(PARAM_CACHE_ID);

        if (CACHE_TYPE_SOURCE.equals(cacheType)) {
            String sourceCacheKey = CacheUtils.getSourceCacheKey(cacheId);
            redisService.deleteKey(sourceCacheKey, res -> {
               if (res.succeeded()) {
                   ctx.response().setStatusCode(HttpResponseStatus.OK.code())
                           .end(new JsonObject().put("success", true).toBuffer());
               } else {
                   ctx.response().setStatusCode(HttpResponseStatus.BAD_REQUEST.code())
                           .end(new JsonObject().put("message", "Invalid cache id!").toBuffer());
               }
            });
        } else {
            ctx.response().setStatusCode(HttpResponseStatus.BAD_REQUEST.code())
                    .end(new JsonObject().put("message", "Invalid cache type!").toBuffer());
        }

    }
}
