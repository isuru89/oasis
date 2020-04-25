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

package io.github.oasis.engine;

import io.github.oasis.core.configs.OasisConfigs;
import io.github.oasis.core.context.RuntimeContextSupport;
import io.github.oasis.core.elements.ElementModule;
import io.github.oasis.core.elements.ElementModuleFactory;
import io.github.oasis.core.elements.Registrar;
import io.github.oasis.core.exception.OasisException;
import io.github.oasis.core.external.Db;
import io.github.oasis.core.external.EventReadWrite;
import io.github.oasis.engine.factory.Parsers;
import io.github.oasis.engine.factory.Processors;
import io.github.oasis.engine.factory.Sinks;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Isuru Weerarathna
 */
public class EngineContext implements RuntimeContextSupport, Registrar {

    private OasisConfigs configs;
    private Db db;
    private EventReadWrite eventStore;

    private Parsers parsers;
    private Processors processors;
    private Sinks sinks;

    private List<Class<? extends ElementModuleFactory>> moduleFactoryList = new ArrayList<>();
    private transient List<ElementModule> moduleList = new ArrayList<>();

    public void init() throws OasisException {
        processors = new Processors();
        sinks = new Sinks();

        try {
            for (Class<? extends ElementModuleFactory> moduleFactory : moduleFactoryList) {
                moduleFactory.getDeclaredConstructor().newInstance().init(this, configs);
            }
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            throw new OasisException(e.getMessage(), e);
        }

        parsers = Parsers.from(this);
        processors.init(this);
        sinks.init(this);
    }

    public List<ElementModule> getModuleList() {
        return moduleList;
    }

    public Sinks getSinks() {
        return sinks;
    }

    public Processors getProcessors() {
        return processors;
    }

    @Override
    public OasisConfigs getConfigs() {
        return configs;
    }

    public void setConfigs(OasisConfigs configs) {
        this.configs = configs;
    }

    @Override
    public Db getDb() {
        return db;
    }

    public void setDb(Db db) {
        this.db = db;
    }

    @Override
    public EventReadWrite getEventStore() {
        return eventStore;
    }

    public void setEventStore(EventReadWrite eventStore) {
        this.eventStore = eventStore;
    }

    public void setModuleFactoryList(List<Class<? extends ElementModuleFactory>> moduleFactoryList) {
        this.moduleFactoryList = moduleFactoryList;
    }

    @Override
    public void registerModule(ElementModule module) {
        moduleList.add(module);
    }
}
