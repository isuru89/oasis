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

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.routing.ActorRefRoutee;
import akka.routing.Routee;
import akka.routing.Router;
import io.github.oasis.engine.OasisConfigs;
import io.github.oasis.engine.actors.cmds.GameEventMessage;
import io.github.oasis.engine.actors.cmds.OasisCommand;
import io.github.oasis.engine.actors.routers.UserRouting;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Isuru Weerarathna
 */
public class GameSupervisor extends OasisBaseActor {

    private static final int RULE_SUPERVISORS = 2;

    private Router ruleRouters;

    @Inject
    public GameSupervisor(OasisConfigs configs) {
        super(configs);

        createRuleRouters();
    }

    private void createRuleRouters() {
        int supervisorCount = configs.getInt(OasisConfigs.RULE_SUPERVISOR_COUNT, RULE_SUPERVISORS);
        List<Routee> routees = new ArrayList<>();
        for (int i = 0; i < supervisorCount; i++) {
            ActorRef ruleActor = getContext().actorOf(Props.create(RuleSupervisor.class, () -> injectActor(RuleSupervisor.class)), "rule-supervisor-" + i);
            getContext().watch(ruleActor);
            routees.add(new ActorRefRoutee(ruleActor));
        }
        ruleRouters = new Router(new UserRouting(), routees);
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
