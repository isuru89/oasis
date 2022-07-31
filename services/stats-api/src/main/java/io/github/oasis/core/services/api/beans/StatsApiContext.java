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

package io.github.oasis.core.services.api.beans;

import io.github.oasis.core.configs.OasisConfigs;
import io.github.oasis.core.context.RuntimeContextSupport;
import io.github.oasis.core.elements.*;
import io.github.oasis.core.elements.spec.BaseSpecification;
import io.github.oasis.core.exception.OasisException;
import io.github.oasis.core.exception.OasisParseException;
import io.github.oasis.core.external.Db;
import io.github.oasis.core.external.EventReadWriteHandler;
import io.github.oasis.core.external.messages.EngineMessage;
import io.github.oasis.core.services.annotations.EngineDbPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Isuru Weerarathna
 */
@Component
public class StatsApiContext implements RuntimeContextSupport, Registrar {

    private static final Logger LOG = LoggerFactory.getLogger(StatsApiContext.class);

    private final Db db;
    private final OasisConfigs configs;

    private final List<ElementModule> elementModules = new ArrayList<>();

    private final Map<String, ElementParser> parserCache = new ConcurrentHashMap<>();

    public StatsApiContext(@EngineDbPool Db dbPool, OasisConfigs oasisConfigs) {
        this.db = dbPool;
        this.configs = oasisConfigs;
    }

    @PostConstruct
    public void init() throws OasisException {
        LOG.info("Finding element modules in classpath...");
        List<? extends Class<? extends ElementModuleFactory>> factoryList = ServiceLoader.load(ElementModuleFactory.class)
                .stream()
                .map(ServiceLoader.Provider::type)
                .peek(factory -> LOG.info("Found element factory: {}", factory.getName()))
                .collect(Collectors.toList());

        LOG.info("Found #{} element module factories.", factoryList.size());
        for (Class<? extends ElementModuleFactory> factory : factoryList) {
            try {
                LOG.info(" - Loading element factory {}...", factory.getName());
                factory.getDeclaredConstructor().newInstance().init(this, configs);
                LOG.info("    Success.");
            } catch (ReflectiveOperationException e) {
                throw new OasisException("Unable to load factory " + factory.getName() + "!", e);
            }
        }

        LOG.info("Initializing #{} modules...", elementModules.size());
        for (ElementModule module : elementModules) {
            module.init(this);
        }

        elementModules
            .forEach(mod -> {
                ElementParser elementParser = mod.getParser();

                elementParser.getAcceptingDefinitions().getDefinitions().stream().map(AcceptedDefinitions.DefinitionEntry::getKey)
                    .peek(key -> LOG.info("Definition {} will be parsed with {}", key, elementParser.getClass().getName()))
                    .forEach(key -> parserCache.put(key, elementParser));
            });
    }

    public List<ElementModule> getElementModules() {
        return elementModules;
    }

    public void validateElement(ElementDef def) {
        String type = def.getType();
        ElementParser elementParser = parserCache.get(type);
        if (elementParser != null) {
            EngineMessage message = new EngineMessage();
            message.setImpl(def.getType());
            message.setType(EngineMessage.GAME_RULE_ADDED);
            message.setScope(new EngineMessage.Scope(def.getGameId()));
            message.setData(def.getData());

            try {
                AbstractDef<? extends BaseSpecification> elementSpec = elementParser.parse(message);
                elementSpec.validate();
            } catch (RuntimeException e) {
                if (e instanceof OasisParseException) {
                    throw e;
                }
                throw new OasisParseException("Unable to parse element definition! [Cause: " + e.getMessage() + "]", e);
            }
        } else {
            throw new OasisParseException("Unknown element type!");
        }
    }

    @Override
    public String id() {
        return "stats_" + UUID.randomUUID().toString();
    }

    @Override
    public OasisConfigs getConfigs() {
        return configs;
    }

    @Override
    public Db getDb() {
        return db;
    }

    @Override
    public EventReadWriteHandler getEventStore() {
        return null;
    }

    @Override
    public void registerModule(ElementModule module) {
        elementModules.add(module);
    }
}
