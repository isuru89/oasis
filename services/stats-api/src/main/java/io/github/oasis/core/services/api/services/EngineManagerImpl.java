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

package io.github.oasis.core.services.api.services;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigValue;
import io.github.oasis.core.Game;
import io.github.oasis.core.configs.OasisConfigs;
import io.github.oasis.core.external.EventDispatchSupport;
import io.github.oasis.core.external.EventStreamFactory;
import io.github.oasis.core.external.messages.GameState;
import io.github.oasis.core.external.messages.PersistedDef;
import io.github.oasis.core.services.api.exceptions.EngineManagerException;
import io.github.oasis.core.services.api.exceptions.ErrorCodes;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * Default implementation for engine manager through an external plugin.
 *
 * @author Isuru Weerarathna
 */
@Service
public class EngineManagerImpl implements IEngineManager, Closeable {

    private static final Logger LOG = LoggerFactory.getLogger(EngineManagerImpl.class);

    private final OasisConfigs oasisConfigs;

    private EventDispatchSupport dispatchSupport;

    public EngineManagerImpl(OasisConfigs oasisConfigs) {
        this.oasisConfigs = oasisConfigs;
    }

    @PostConstruct
    public void initialize() throws Exception {
        String dispatcherImpl = oasisConfigs.get("oasis.dispatcher.impl", null);
        if (StringUtils.isBlank(dispatcherImpl)) {
            throw new IllegalStateException("Mandatory dispatcher implementation has not specified!");
        }

        String dispatcherClz = StringUtils.substringAfter(dispatcherImpl, ":");
        LOG.info("Initializing dispatcher implementation {}...", dispatcherClz);
        EventStreamFactory eventStreamFactory = ServiceLoader.load(EventStreamFactory.class)
                .stream()
                .peek(eventStreamFactoryProvider -> LOG.debug("Found dispatcher implementation: {}", eventStreamFactoryProvider.type().getName()))
                .filter(eventStreamFactoryProvider -> dispatcherClz.equals(eventStreamFactoryProvider.type().getName()))
                .map(ServiceLoader.Provider::get)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Unknown dispatcher implementation provided! " + dispatcherClz));

        LOG.info("Dispatcher loaded from {}", dispatcherClz);
        EventDispatchSupport dispatcher = eventStreamFactory.getDispatcher();
        Map<String, Object> config = toMap(oasisConfigs.getConfigRef().getConfig("oasis.dispatcher.configs"));
        EventDispatchSupport.DispatcherContext context = () -> config;
        dispatcher.init(context);
        this.dispatchSupport = dispatcher;
        LOG.info("Dispatcher {} successfully loaded!", dispatcherClz);
    }

    private Map<String, Object> toMap(Config config) {
        Map<String, Object> destination = new HashMap<>();
        for (Map.Entry<String, ConfigValue> entry : config.entrySet()) {
            destination.put(entry.getKey(), entry.getValue().unwrapped());
        }
        return destination;
    }

    @Override
    public void changeGameStatus(GameState state, Game game) throws EngineManagerException {
        PersistedDef message = PersistedDef.createGameLifecycleEvent(game.getId(), state);
        try {
            dispatchSupport.broadcast(message);
        } catch (Exception e) {
            throw new EngineManagerException(ErrorCodes.UNABLE_TO_CHANGE_GAME_STATE, e);
        }
    }

    @Override
    public void close() throws IOException {
        if (this.dispatchSupport != null) {
            LOG.info("Closing dispatcher...");
            this.dispatchSupport.close();
        }
    }
}
