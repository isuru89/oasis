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

package io.github.oasis.core.services.api.configs;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigValue;
import io.github.oasis.core.configs.OasisConfigs;
import io.github.oasis.core.external.EngineManagerSubscription;
import io.github.oasis.core.external.EventDispatcher;
import io.github.oasis.core.external.EventStreamFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * @author Isuru Weerarathna
 */
@Configuration
public class StreamConfigs {

    private static final Logger LOG = LoggerFactory.getLogger(StreamConfigs.class);

    @Bean
    public EventStreamFactory createStreamFactory(OasisConfigs oasisConfigs) {
        String dispatcherImpl = oasisConfigs.get("oasis.dispatcher.impl", null);
        if (StringUtils.isBlank(dispatcherImpl)) {
            throw new IllegalStateException("Mandatory dispatcher implementation has not specified!");
        }

        LOG.info("Initializing dispatcher implementation {}...", dispatcherImpl);

        if (StringUtils.startsWith(dispatcherImpl, "classpath:")) {
            String dispatcherClz = StringUtils.substringAfter(dispatcherImpl, "classpath:");
            return loadFromClasspathReflection(dispatcherClz);
        } else {
            return loadFromServiceLoader(dispatcherImpl);
        }
    }

    @Bean
    public EventDispatcher createStreamDispatcher(OasisConfigs oasisConfigs, EventStreamFactory eventStreamFactory) throws Exception {
        String dispatcherImpl = oasisConfigs.get("oasis.dispatcher.impl", null);

        LOG.info("Dispatcher loaded from {}", dispatcherImpl);
        EventDispatcher dispatcher = eventStreamFactory.getDispatcher();
        Map<String, Object> config = toMap(oasisConfigs.getConfigRef().getConfig("oasis.dispatcher.configs"));
        EventDispatcher.DispatcherContext context = () -> config;
        dispatcher.init(context);
        LOG.info("Dispatcher {} successfully loaded!", dispatcherImpl);

        return dispatcher;
    }

    @Bean
    public EngineManagerSubscription createEngineStatusSubscription(OasisConfigs oasisConfigs, EventStreamFactory eventStreamFactory) {
        EngineManagerSubscription subscription = eventStreamFactory.getEngineManagerSubscription();
        subscription.init(oasisConfigs);
        return subscription;
    }

    private EventStreamFactory loadFromClasspathReflection(String dispatcherImpl) {
        try {
            Class<?> referredClz = Thread.currentThread().getContextClassLoader().loadClass(dispatcherImpl);
            return (EventStreamFactory) referredClz.getDeclaredConstructor().newInstance();

        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Dispatcher implementation cannot be found in classpath! " + dispatcherImpl);
        }
    }

    private EventStreamFactory loadFromServiceLoader(String dispatcherImpl) {
        return ServiceLoader.load(EventStreamFactory.class)
                .stream()
                .peek(eventStreamFactoryProvider -> LOG.info("Found dispatcher implementation: {}", eventStreamFactoryProvider.type().getName()))
                .filter(eventStreamFactoryProvider -> dispatcherImpl.equals(eventStreamFactoryProvider.type().getName()))
                .map(ServiceLoader.Provider::get)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Unknown dispatcher implementation provided! " + dispatcherImpl));
    }

    private Map<String, Object> toMap(Config config) {
        Map<String, Object> destination = new HashMap<>();
        for (Map.Entry<String, ConfigValue> entry : config.entrySet()) {
            destination.put(entry.getKey(), entry.getValue().unwrapped());
        }
        return destination;
    }


}
