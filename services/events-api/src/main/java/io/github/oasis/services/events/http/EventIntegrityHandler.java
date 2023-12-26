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

import io.github.oasis.services.events.auth.AuthService;
import io.github.oasis.services.events.model.EventApiConfigs;
import io.github.oasis.services.events.model.EventSource;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Verify the integrity of the payload by comparing the payload hash
 * against client's private key.
 *
 * @author Isuru Weerarathna
 */
class EventIntegrityHandler implements Handler<RoutingContext> {

    private static final Logger LOG = LoggerFactory.getLogger(EventIntegrityHandler.class);

    private static final String EVENTS_PAYLOAD_VERIFICATION_FAILED = "Event payload verification failed!";

    static Handler<RoutingContext> create(EventApiConfigs configs) {
        if (configs.isSkipEventIntegrityCheck()) {
            LOG.warn(">>>>> WARN: Event API Integrity check has been disabled! <<<<<");
            return new NoIntegrityCheckHandler();
        }
        return new EventIntegrityHandler();
    }

    @Override
    public void handle(RoutingContext ctx) {
        EventSource eventSource = (EventSource) ctx.user();
        Optional<String> optHeader = Optional.ofNullable(ctx.get(AuthService.REQ_DIGEST));
        if (optHeader.isPresent() && eventSource.verifyEvent(ctx.body().buffer(), optHeader.get())) {
            ctx.next();
        } else {
            LOG.warn("[Source={}] Payload verification failed!", eventSource.getSourceId());
            ctx.fail(HttpResponseStatus.FORBIDDEN.code(), new IllegalArgumentException(EVENTS_PAYLOAD_VERIFICATION_FAILED));
        }
    }

    static class NoIntegrityCheckHandler implements Handler<RoutingContext> {
        @Override
        public void handle(RoutingContext ctx) {
            ctx.next();
        }
    }
}
