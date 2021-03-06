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
import io.github.oasis.core.external.SignalSubscriptionSupport;
import io.github.oasis.engine.factory.Parsers;
import io.github.oasis.engine.factory.Processors;
import io.github.oasis.engine.factory.Sinks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Isuru Weerarathna
 */
public class EngineContext implements RuntimeContextSupport, Registrar {

    private static final Logger LOG = LoggerFactory.getLogger(EngineContext.class);

    private OasisConfigs configs;
    private Db db;
    private EventReadWrite eventStore;

    private Parsers parsers;
    private Processors processors;
    private Sinks sinks;

    private List<Class<? extends ElementModuleFactory>> moduleFactoryList = new ArrayList<>();
    private final transient List<ElementModule> moduleList = new ArrayList<>();

    private SignalSubscriptionSupport signalSubscription;

    public void init() throws OasisException {
        processors = new Processors();
        sinks = new Sinks();

        try {
            for (Class<? extends ElementModuleFactory> moduleFactory : moduleFactoryList) {
                LOG.info("Initializing element module [{}]", moduleFactory.getName());
                moduleFactory.getDeclaredConstructor().newInstance().init(this, configs);
            }
        } catch (ReflectiveOperationException e) {
            throw new OasisException(e.getMessage(), e);
        }

        // initialize modules before anything
        for (ElementModule module : moduleList) {
            module.init(this);
        }

        parsers = Parsers.from(this);
        processors.init(this);
        sinks.init(this);
    }

    public Parsers getParsers() {
        return parsers;
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

    @Override
    public Db getDb() {
        return db;
    }

    @Override
    public EventReadWrite getEventStore() {
        return eventStore;
    }

    @Override
    public void registerModule(ElementModule module) {
        moduleList.add(module);
    }

    public static Builder builder() {
        return new Builder();
    }

    public SignalSubscriptionSupport getSignalSubscription() {
        return signalSubscription;
    }

    private void setConfigs(OasisConfigs configs) {
        this.configs = configs;
    }

    private void setDb(Db db) {
        this.db = db;
    }

    private void setEventStore(EventReadWrite eventStore) {
        this.eventStore = eventStore;
    }

    private void setModuleFactoryList(List<Class<? extends ElementModuleFactory>> moduleFactoryList) {
        this.moduleFactoryList = moduleFactoryList;
    }

    private void setSignalSubscription(SignalSubscriptionSupport signalSubscription) {
        this.signalSubscription = signalSubscription;
    }

    public static class Builder {
        private final EngineContext ctx = new EngineContext();
        private final List<Class<? extends ElementModuleFactory>> factories = new ArrayList<>();

        private Builder() {}

        public Builder withConfigs(OasisConfigs configs) {
            ctx.setConfigs(configs);
            return this;
        }

        public Builder withDb(Db dbRef) {
            ctx.setDb(dbRef);
            return this;
        }

        public Builder withEventStore(EventReadWrite eventStore) {
            ctx.setEventStore(eventStore);
            return this;
        }

        public Builder withSignalSubscription(SignalSubscriptionSupport signalSubscription) {
            ctx.setSignalSubscription(signalSubscription);
            return this;
        }

        public Builder installModules(List<Class<? extends ElementModuleFactory>> factoriesList) {
            factories.addAll(factoriesList);
            return this;
        }

        public Builder installModule(Class<? extends ElementModuleFactory> moduleFactory) {
            factories.add(moduleFactory);
            return this;
        }

        public EngineContext build() {
            ctx.setModuleFactoryList(factories);
            return ctx;
        }

    }
}
