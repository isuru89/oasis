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

package io.github.oasis.engine.actors;

import akka.actor.AbstractActor;
import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.OneForOneStrategy;
import akka.actor.Props;
import akka.actor.SupervisorStrategy;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.DeciderBuilder;
import akka.routing.ActorRefRoutee;
import akka.routing.Routee;
import io.github.oasis.engine.EngineContext;
import io.github.oasis.core.configs.OasisConfigs;
import io.github.oasis.engine.ext.Rules;
import io.github.oasis.engine.ext.RulesImpl;
import scala.Option;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * @author Isuru Weerarathna
 */
public abstract class OasisBaseActor extends AbstractActor {

    private static final int MAX_NR_OF_RETRIES = 10;

    private static final SupervisorStrategy RESTART_STRATEGY = new OneForOneStrategy(
            MAX_NR_OF_RETRIES,
            Duration.ofMinutes(1),
            DeciderBuilder.matchAny(e -> SupervisorStrategy.restart())
                    .build()
    );

    protected LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    protected EngineContext engineContext;
    protected OasisConfigs configs;
    protected String parentId;
    protected String myId;

    OasisBaseActor(EngineContext context) {
        this.engineContext = context;
        this.configs = engineContext.getConfigs();
    }

    protected <T extends Actor> List<Routee> createChildRouteActorsOfType(Class<T> actorClz, Function<Integer, String> actorNameSupplier, int count) {
        log.info("[{}] Creating {} actors of type {}", myId, count, actorClz.getSimpleName());
        List<Routee> allRoutes = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            ActorRef ruleActor = getContext().actorOf(Props.create(actorClz, engineContext), actorNameSupplier.apply(i));
            getContext().watch(ruleActor);
            allRoutes.add(new ActorRefRoutee(ruleActor));
        }
        return allRoutes;
    }

    @Override
    public void preRestart(Throwable reason, Option<Object> message) throws Exception {
        super.preRestart(reason, message);

        log.warning("[{}#{}] Actor restarting due to {}", parentId, myId, reason);
    }

    @Override
    public SupervisorStrategy supervisorStrategy() {
        return RESTART_STRATEGY;
    }

    RulesImpl.GameRules getGameRuleRef(int gameId) {
        return Rules.RULES_PROVIDER.get(getContext().getSystem()).forGame(gameId);
    }

    void createGameRuleRefNx(int gameId) {
        Rules.RULES_PROVIDER.get(getContext().getSystem()).createIfNotExists(gameId);
    }
}
