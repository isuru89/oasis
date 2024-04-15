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

package io.github.oasis.services.events.dispatcher;

import io.github.oasis.core.external.EventAsyncDispatcher;
import io.github.oasis.core.external.EventDispatcher;
import io.github.oasis.core.external.EventStreamFactory;
import io.vertx.core.Promise;
import io.vertx.core.Verticle;
import io.vertx.core.spi.VerticleFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.ServiceLoader;
import java.util.concurrent.Callable;

/**
 * @author Isuru Weerarathna
 */
public class DispatcherFactory implements VerticleFactory {

    private static final Logger LOG = LoggerFactory.getLogger(DispatcherFactory.class);

    public static final String OASIS_VERTICLE = "oasis";
    private static final String OASIS_PREFIX = OASIS_VERTICLE + ":";

    @Override
    public String prefix() {
        return OASIS_VERTICLE;
    }

    @Override
    public void createVerticle(String type, ClassLoader classLoader, Promise<Callable<Verticle>> promise) {
        String impl = StringUtils.substringAfter(type, OASIS_PREFIX);
        LOG.info("Creating dispatcher of type: {}", impl);
        try {
            Optional<EventStreamFactory> eventStreamFactory = ServiceLoader.load(EventStreamFactory.class, classLoader)
                    .stream()
                    .filter(eventStreamFactoryProvider -> impl.equals(eventStreamFactoryProvider.type().getName()))
                    .map(ServiceLoader.Provider::get)
                    .findFirst();

            Object instance;
            if (eventStreamFactory.isPresent()) {
                instance = eventStreamFactory.get().getDispatcher();
            } else {
                instance = classLoader.loadClass(impl).getDeclaredConstructor().newInstance();
            }

            if (instance instanceof EventDispatcher) {
                EventDispatcher dispatchSupport = (EventDispatcher) instance;
                if (instance instanceof EventAsyncDispatcher) {
                    promise.complete(() -> new DispatcherAsyncVerticle((EventAsyncDispatcher) instance));
                } else {
                    promise.complete(() -> new DispatcherVerticle(dispatchSupport));
                }
            } else if (instance instanceof Verticle) {
                promise.complete(() -> (Verticle) instance);
            } else {
                promise.fail(new IllegalArgumentException("Unknown dispatcher type provided! " + impl));
            }

        } catch (ReflectiveOperationException e) {
            LOG.error("Cannot load provided dispatcher implementation!", e);
            promise.fail(e);
        }
    }
}
