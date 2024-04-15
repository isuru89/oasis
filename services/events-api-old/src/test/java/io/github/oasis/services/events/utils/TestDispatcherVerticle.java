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

package io.github.oasis.services.events.utils;

import io.github.oasis.services.events.dispatcher.EventDispatcherService;
import io.vertx.core.AbstractVerticle;
import io.vertx.serviceproxy.ServiceBinder;

/**
 * @author Isuru Weerarathna
 */
public class TestDispatcherVerticle extends AbstractVerticle {

    private EventDispatcherService dispatcherService;

    public TestDispatcherVerticle(EventDispatcherService dispatcherService) {
        this.dispatcherService = dispatcherService;
    }

    @Override
    public void start() {
        new ServiceBinder(vertx)
                .setAddress(EventDispatcherService.DISPATCHER_SERVICE_QUEUE)
                .register(EventDispatcherService.class, dispatcherService);
    }
}
