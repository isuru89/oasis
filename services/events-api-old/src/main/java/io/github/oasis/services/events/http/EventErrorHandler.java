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

package io.github.oasis.services.events.http;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles any errors occurred while processing events and send
 * meaningful payload back to clients.
 *
 * @author Isuru Weerarathna
 */
class EventErrorHandler implements Handler<RoutingContext> {

    private static final Logger LOG = LoggerFactory.getLogger(EventErrorHandler.class);

    @Override
    public void handle(RoutingContext ctx) {
        JsonObject errorJson = new JsonObject();
        Throwable failure = ctx.failure();
        if (failure != null) {
            LOG.error("Error occurred while processing {}", ctx.request().path());
            LOG.error("Error: ", failure);
            errorJson.put("error", failure.getMessage());
        }

        errorJson.put("timestamp", System.currentTimeMillis())
                .put("status", ctx.statusCode())
                .put("path", ctx.request().path());

        ctx.response().setStatusCode(ctx.statusCode()).end(errorJson.encodePrettily());
    }
}
