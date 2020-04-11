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
import akka.routing.DefaultResizer;
import akka.routing.Routee;
import akka.routing.Router;
import io.github.oasis.engine.OasisConfigs;
import io.github.oasis.engine.actors.cmds.EventMessage;
import io.github.oasis.engine.actors.cmds.OasisRuleMessage;
import io.github.oasis.engine.actors.cmds.StartRuleExecutionCommand;
import io.github.oasis.engine.actors.routers.UserRouting;
import io.github.oasis.engine.factory.InjectedActorSupport;
import io.github.oasis.engine.model.ExecutionContext;
import io.github.oasis.engine.model.Rules;
import io.github.oasis.engine.model.ActorSignalCollector;
import io.github.oasis.model.Event;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Isuru Weerarathna
 */
public class RuleSupervisor extends OasisBaseActor implements InjectedActorSupport {

    private static final DefaultResizer ELASTICITY = new DefaultResizer(5, 10);

    private static final AtomicInteger counter = new AtomicInteger(0);

    private static final int EXECUTORS = 3;

    private Rules rules;
    private ActorRef signalExchanger;
    private ActorSignalCollector collector;
    private final int id;
    private Router executor;

    @Inject
    public RuleSupervisor(OasisConfigs configs) {
        super(configs);

        id = counter.incrementAndGet();

        signalExchanger = createSignalExchanger();
        collector = new ActorSignalCollector(signalExchanger);
        rules = Rules.get(collector);
    }

    @Override
    public void preStart() {
        createExecutors();

        beginAllChildren();
    }

    @Override
    public void postRestart(Throwable reason) throws Exception {
        super.postRestart(reason);

        beginAllChildren();
    }

    private void beginAllChildren() {
        signalExchanger.tell(new StartRuleExecutionCommand(id, rules), getSelf());
        executor.route(new StartRuleExecutionCommand(id, rules), getSelf());
    }

    private ActorRef createSignalExchanger() {
        ActorRef actorRef = getContext().actorOf(Props.create(SignalSupervisor.class, () -> injectActor(SignalSupervisor.class)), "signal-exchanger");
        getContext().watch(actorRef);
        return actorRef;
    }

    private void createExecutors() {
        int executors = configs.getInt(OasisConfigs.RULE_EXECUTOR_COUNT, EXECUTORS);
        List<Routee> routees = new ArrayList<>();
        for (int i = 0; i < executors; i++) {
            ActorRef actorRef = getContext().actorOf(Props.create(RuleExecutor.class, () -> injectActor(RuleExecutor.class)));
            getContext().watch(actorRef);
            routees.add(new ActorRefRoutee(actorRef));
        }
        this.executor = new Router(new UserRouting(), routees);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Event.class, this::processEvent)
                .match(OasisRuleMessage.class, this::forwardRuleModifiedEvent)
                .build();
    }

    private void processEvent(Event event) {
        executor.route(new EventMessage(event, new ExecutionContext()), getSelf());
    }

    private void forwardRuleModifiedEvent(OasisRuleMessage ruleMessage) {
        executor.route(ruleMessage, getSelf());
    }

}
