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
import io.github.oasis.engine.actors.cmds.OasisCommand;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Isuru Weerarathna
 */
public class GameSupervisor extends OasisBaseActor {

    private static final int RULE_PROCESSORS = 10;

    private Router ruleRouters;

    public GameSupervisor() {
        createRuleRouters();
    }

    private void createRuleRouters() {
        List<Routee> routees = new ArrayList<>();
        for (int i = 0; i < RULE_PROCESSORS; i++) {
            ActorRef ruleActor = getContext().actorOf(Props.create(RuleSupervisor.class, RuleSupervisor::new), "rule-supervisor-" + i);
            getContext().watch(ruleActor);
            routees.add(new ActorRefRoutee(ruleActor));
        }
        ruleRouters = new Router(new UserRouting(), routees);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(OasisCommand.class, this::processOasisCommand)
                .build();
    }

    private void processOasisCommand(OasisCommand command) {
        ruleRouters.route(command, getSelf());
    }
}
