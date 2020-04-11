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
import io.github.oasis.engine.actors.cmds.EventMessage;
import io.github.oasis.engine.actors.cmds.OasisRuleMessage;
import io.github.oasis.engine.actors.cmds.RuleAddedMessage;
import io.github.oasis.engine.actors.cmds.SignalMessage;
import io.github.oasis.engine.actors.cmds.StartRuleExecutionCommand;
import io.github.oasis.engine.actors.routers.UserSignalRouting;
import io.github.oasis.engine.factory.InjectedActorSupport;
import io.github.oasis.engine.model.EventCreatable;
import io.github.oasis.engine.model.Rules;
import io.github.oasis.engine.rules.signals.Signal;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Isuru Weerarathna
 */
public class SignalSupervisor extends OasisBaseActor implements InjectedActorSupport {

    private static final int CONSUMER_COUNT = 2;

    private Router router;

    private Rules rules;

    @Inject
    SignalSupervisor(OasisConfigs configs) {
        super(configs);
    }

    @Override
    public void preStart() {
        int consumers = configs.getInt(OasisConfigs.SIGNAL_EXECUTOR_COUNT, CONSUMER_COUNT);
        List<Routee> routees = new ArrayList<>();
        for (int i = 0; i < consumers; i++) {
            ActorRef consumer = getContext().actorOf(Props.create(SignalConsumer.class, () -> injectActor(SignalConsumer.class)));
            getContext().watch(consumer);
            routees.add(new ActorRefRoutee(consumer));
        }
        router = new Router(new UserSignalRouting(), routees);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(StartRuleExecutionCommand.class, this::initializeRules)
                .match(SignalMessage.class, this::whenSignalReceived)
                .match(OasisRuleMessage.class, this::handleRuleModificationMessage)
                .build();
    }

    private void initializeRules(StartRuleExecutionCommand command) {
        this.rules = command.getRules();
    }

    private void handleRuleModificationMessage(OasisRuleMessage message) {
        if (message instanceof RuleAddedMessage) {
            rules.addRule(((RuleAddedMessage) message).getRule());
        }
    }

    private void whenSignalReceived(SignalMessage signalMessage) {
        Signal signal = signalMessage.getSignal();
        if (signal instanceof EventCreatable) {
            ((EventCreatable) signal).generateEvent()
                    .ifPresent(event -> getContext().getParent().tell(new EventMessage(event, signalMessage.getContext()), getSelf()));
        }

        System.out.println("Signal received " + signal + " rule " + signalMessage.getRule());
        router.route(signalMessage, getSelf());
    }

}
