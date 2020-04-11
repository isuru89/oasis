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

import akka.actor.ActorSystem;
import io.github.oasis.engine.factory.AbstractActorProviderModule;

import javax.inject.Provider;
import java.util.function.Function;

/**
 * @author Isuru Weerarathna
 */
public class EngineContext {

    private Function<ActorSystem, AbstractActorProviderModule> moduleProvider;
    private Provider<OasisConfigs> configsProvider;

    public Provider<OasisConfigs> getConfigsProvider() {
        return configsProvider;
    }

    public void setConfigsProvider(Provider<OasisConfigs> configsProvider) {
        this.configsProvider = configsProvider;
    }

    public Function<ActorSystem, AbstractActorProviderModule> getModuleProvider() {
        return moduleProvider;
    }

    public void setModuleProvider(Function<ActorSystem, AbstractActorProviderModule> moduleProvider) {
        this.moduleProvider = moduleProvider;
    }
}
