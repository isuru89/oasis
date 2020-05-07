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

import io.github.oasis.core.external.EventDispatchSupport;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ServiceBinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author Isuru Weerarathna
 */
public class DispatcherVerticle extends AbstractVerticle {

    private static final Logger LOG = LoggerFactory.getLogger(RabbitMQVerticle.class);

    private final EventDispatchSupport eventDispatcher;

    public DispatcherVerticle(EventDispatchSupport eventDispatcher) {
        this.eventDispatcher = eventDispatcher;
    }

    @Override
    public void start() throws Exception {
        LOG.info("Initializing event dispatcher {}...", eventDispatcher.getClass().getName());
        JsonObject dispatcherConfigs = config().copy();
        VertxDispatcherContext ctx = new VertxDispatcherContext(dispatcherConfigs.getMap());
        try {
            eventDispatcher.init(ctx);
            WrappedDispatcherService wrappedDispatcherService = new WrappedDispatcherService(vertx, eventDispatcher);
            ServiceBinder binder = new ServiceBinder(vertx);
            binder.setAddress(EventDispatcherService.DISPATCHER_SERVICE_QUEUE)
                    .register(EventDispatcherService.class, wrappedDispatcherService);
            LOG.info("Dispatcher initialization successful!");
        } catch (Exception e){
            LOG.error("Dispatcher initialization Failed!", e);
            throw e;
        }
    }

    @Override
    public void stop() throws Exception {
        LOG.warn("Stopping event dispatcher...");
        eventDispatcher.close();
    }

    static class VertxDispatcherContext implements EventDispatchSupport.DispatcherContext {

        private Map<String, Object> configs;

        VertxDispatcherContext(Map<String, Object> configs) {
            this.configs = configs;
        }

        @Override
        public Map<String, Object> getConfigs() {
            return configs;
        }
    }
}
