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
import io.github.oasis.core.elements.ElementModule;
import io.github.oasis.core.elements.ElementModuleFactory;
import io.github.oasis.core.elements.Registrar;
import io.github.oasis.core.exception.OasisException;
import io.github.oasis.core.external.Db;
import io.github.oasis.core.external.EventReadWrite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

/**
 * @author Isuru Weerarathna
 */
@Component
public class StatsApiContext implements RuntimeContextSupport, Registrar {

    private static final Logger LOG = LoggerFactory.getLogger(StatsApiContext.class);

    private final Db db;
    private final OasisConfigs configs;

    private final List<ElementModule> elementModules = new ArrayList<>();

    public StatsApiContext(Db dbPool, OasisConfigs oasisConfigs) {
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
    }

    public List<ElementModule> getElementModules() {
        return elementModules;
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
    public EventReadWrite getEventStore() {
        return null;
    }

    @Override
    public void registerModule(ElementModule module) {
        elementModules.add(module);
    }
}
