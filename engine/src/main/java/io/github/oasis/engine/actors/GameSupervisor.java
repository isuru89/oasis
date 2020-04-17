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

import akka.routing.Routee;
import akka.routing.Router;
import io.github.oasis.engine.EngineContext;
import io.github.oasis.core.configs.OasisConfigs;
import io.github.oasis.engine.actors.cmds.GameEventMessage;
import io.github.oasis.engine.actors.cmds.OasisCommand;
import io.github.oasis.engine.actors.routers.UserRouting;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Isuru Weerarathna
 */
public class GameSupervisor extends OasisBaseActor {

    private static final AtomicInteger COUNTER = new AtomicInteger(0);

    private static final int RULE_SUPERVISORS = 2;

    private Router ruleRouters;

    public GameSupervisor(EngineContext context) {
        super(context);

        myId = "G" + COUNTER.incrementAndGet();
        createRuleRouters();
    }

    private void createRuleRouters() {
        int supervisorCount = configs.getInt(OasisConfigs.RULE_SUPERVISOR_COUNT, RULE_SUPERVISORS);
        List<Routee> allRoutes = createChildRouteActorsOfType(RuleSupervisor.class,
                index -> ActorNames.RULE_SUPERVISOR_PREFIX + index,
                supervisorCount);
        ruleRouters = new Router(new UserRouting(), allRoutes);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(GameEventMessage.class, event -> ruleRouters.route(event, getSender()))
                .match(OasisCommand.class, this::processOasisCommand)
                .build();
    }

    private void processOasisCommand(OasisCommand command) {
        ruleRouters.route(command, getSelf());
    }

}
