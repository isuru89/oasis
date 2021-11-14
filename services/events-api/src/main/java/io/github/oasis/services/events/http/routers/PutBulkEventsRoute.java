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

package io.github.oasis.services.events.http.routers;

import io.github.oasis.services.events.db.DataService;
import io.github.oasis.services.events.dispatcher.EventDispatcherService;
import io.github.oasis.services.events.model.EventProxy;
import io.github.oasis.services.events.model.EventSource;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Optional;

import static io.github.oasis.services.events.http.routers.PayloadUtils.asEvent;
import static io.github.oasis.services.events.http.routers.PayloadUtils.asEventSource;
import static io.github.oasis.services.events.http.routers.PayloadUtils.getEventPayloadAsArray;

/**
 * Submits multiple events at once.
 *
 * @author Isuru Weerarathna
 */
public class PutBulkEventsRoute extends AbstractEventHandler implements Handler<RoutingContext> {

    private static final Logger LOG = LoggerFactory.getLogger(PutBulkEventsRoute.class);

    public PutBulkEventsRoute(DataService dataService, EventDispatcherService dispatcherService) {
        super(dataService, dispatcherService);
    }

    @Override
    public void handle(RoutingContext context) {
        Optional<JsonArray> payloadArray = getEventPayloadAsArray(context.getBody());
        if (payloadArray.isEmpty()) {
            failWithInvalidPayloadFormat(context);
            return;
        }

        JsonArray eventArray = payloadArray.get();
        Iterator<Object> it = eventArray.iterator();
        EventSource source = asEventSource(context.user());
        JsonArray submittedEvents = new JsonArray();
        while (it.hasNext()) {
            Object eventPayloadObj = it.next();
            Optional<EventProxy> eventProxy = asEvent(eventPayloadObj);
            if (eventProxy.isEmpty()) {
                failWithInvalidPayloadFormat(context);
                return;
            }

            EventProxy event = eventProxy.get();
            putEvent(event, source, res -> {
                if (res.failed()) {
                    LOG.warn("Unable to publish event! {}", event);
                }
            });
            submittedEvents.add(event.getExternalId());
        }
        context.response()
                .setStatusCode(HttpResponseStatus.ACCEPTED.code())
                .end(new JsonObject().put("events", submittedEvents).toBuffer());
    }
}
