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

package io.github.oasis.services.events.dispatcher;

import io.github.oasis.core.external.EventAsyncDispatcher;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ServiceBinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Isuru Weerarathna
 */
public class DispatcherAsyncVerticle extends AbstractVerticle {

    private static final Logger LOG = LoggerFactory.getLogger(DispatcherAsyncVerticle.class);

    private final EventAsyncDispatcher eventDispatcher;

    public DispatcherAsyncVerticle(EventAsyncDispatcher eventDispatcher) {
        this.eventDispatcher = eventDispatcher;
    }

    @Override
    public void start(Promise<Void> startPromise) {
        LOG.info("Initializing event dispatcher {}...", eventDispatcher.getClass().getName());
        JsonObject dispatcherConfigs = config().copy();
        DispatcherVerticle.VertxDispatcherContext ctx = new DispatcherVerticle.VertxDispatcherContext(dispatcherConfigs.getMap());
        try {
            eventDispatcher.init(ctx, new EventAsyncDispatcher.Handler() {
                @Override
                public void onSuccess(Object result) {
                    WrappedAsyncDispatcherService wrappedDispatcherService = new WrappedAsyncDispatcherService(eventDispatcher);
                    ServiceBinder binder = new ServiceBinder(vertx);
                    binder.setAddress(EventDispatcherService.DISPATCHER_SERVICE_QUEUE)
                            .register(EventDispatcherService.class, wrappedDispatcherService);
                    LOG.info("Dispatcher initialization successful!");
                }

                @Override
                public void onFail(Throwable error) {
                    LOG.error("Dispatcher initialization Failed!", error);
                    startPromise.fail(error);
                }
            });

        } catch (Exception e){
            LOG.error("Dispatcher initialization Failed!", e);
            throw e;
        }
    }

    @Override
    public void stop(Promise<Void> stopPromise) throws Exception {
        LOG.warn("Stopping event dispatcher...");
        eventDispatcher.close();
        stopPromise.complete();
    }
}
