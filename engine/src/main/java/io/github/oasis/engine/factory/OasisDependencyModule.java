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

package io.github.oasis.engine.factory;

import akka.actor.ActorSystem;
import com.google.inject.Guice;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.github.oasis.engine.EngineContext;
import io.github.oasis.engine.OasisConfigs;
import io.github.oasis.engine.actors.OasisSupervisor;
import io.github.oasis.engine.actors.RuleExecutor;
import io.github.oasis.engine.actors.SignalSupervisor;
import io.github.oasis.engine.external.Db;
import io.github.oasis.engine.external.redis.RedisDb;

/**
 * @author Isuru Weerarathna
 */
public class OasisDependencyModule extends AbstractActorProviderModule {

    private final ActorSystem actorSystem;
    private EngineContext context;

    public OasisDependencyModule(ActorSystem actorSystem, EngineContext context) {
        this.actorSystem = actorSystem;
        this.context = context;
        injector = Guice.createInjector(this);
    }

    @Override
    protected void configure() {
        bindActor(actorSystem, RuleExecutor.class, "rule-executor-actor");
        bindSingletonActor(actorSystem, OasisSupervisor.class, "oasis-supervisor");
        bindActor(actorSystem, SignalSupervisor.class, "signal-exchanger");
        bind(OasisConfigs.class).toProvider(context.getConfigsProvider());
    }

    @Provides @Singleton
    Db createDb(OasisConfigs configs) {
        RedisDb redisDb = RedisDb.create(configs);
        redisDb.init();
        return redisDb;
    }

}
