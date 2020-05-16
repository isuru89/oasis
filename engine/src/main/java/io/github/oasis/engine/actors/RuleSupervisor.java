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
import akka.routing.Routee;
import akka.routing.Router;
import io.github.oasis.engine.EngineContext;
import io.github.oasis.core.configs.OasisConfigs;
import io.github.oasis.engine.actors.cmds.EventMessage;
import io.github.oasis.engine.actors.cmds.GameEventMessage;
import io.github.oasis.engine.actors.cmds.OasisRuleMessage;
import io.github.oasis.engine.actors.cmds.StartRuleExecutionCommand;
import io.github.oasis.engine.actors.routers.UserRouting;
import io.github.oasis.engine.model.ActorSignalCollector;
import io.github.oasis.core.context.ExecutionContext;
import io.github.oasis.engine.model.RuleExecutionContext;
import io.github.oasis.engine.model.Rules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Isuru Weerarathna
 */
public class RuleSupervisor extends OasisBaseActor {

    private static final Logger LOG = LoggerFactory.getLogger(RuleSupervisor.class);

    private static final AtomicInteger counter = new AtomicInteger(0);

    private static final int EXECUTORS = 3;

    private Rules rules;
    private ActorRef signalExchanger;
    private ActorSignalCollector collector;
    private RuleExecutionContext ruleExecutionContext;
    private Router executor;

    public RuleSupervisor(EngineContext context) {
        super(context);

        myId = "R" + counter.incrementAndGet();

        signalExchanger = createSignalExchanger();
        collector = new ActorSignalCollector(signalExchanger);
        rules = Rules.create();
        ruleExecutionContext = RuleExecutionContext.from(collector);
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
        signalExchanger.tell(new StartRuleExecutionCommand(myId, rules, ruleExecutionContext), getSelf());
        executor.route(new StartRuleExecutionCommand(myId, rules,ruleExecutionContext), getSelf());
    }

    private ActorRef createSignalExchanger() {
        ActorRef actorRef = getContext().actorOf(
                Props.create(SignalSupervisor.class, this.engineContext),
                ActorNames.SIGNAL_SUPERVISOR_PREFIX + myId);
        getContext().watch(actorRef);
        return actorRef;
    }

    private void createExecutors() {
        int executors = configs.getInt(OasisConfigs.RULE_EXECUTOR_COUNT, EXECUTORS);
        LOG.info("[{}] Rule Executor count {}", myId, executors);
        List<Routee> allRoutes = createChildRouteActorsOfType(RuleExecutor.class,
                index -> ActorNames.RULE_EXECUTOR_PREFIX + index,
                executors);
        this.executor = new Router(new UserRouting(), allRoutes);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(StartRuleExecutionCommand.class, this::initializeMe)
                .match(GameEventMessage.class, this::processEvent)
                .match(EventMessage.class, this::forwardEventMessage)
                .match(OasisRuleMessage.class, this::forwardRuleModifiedEvent)
                .build();
    }

    private void initializeMe(StartRuleExecutionCommand message) {

    }

    private void forwardEventMessage(EventMessage eventMessage) {
        executor.route(eventMessage, getSelf());
    }

    private void processEvent(GameEventMessage eventMessage) {
        executor.route(new EventMessage(eventMessage.getEvent(),
                ExecutionContext.from(eventMessage.getGameContext()).withUserTz(0).build()),
                getSelf());
    }

    private void forwardRuleModifiedEvent(OasisRuleMessage ruleMessage) {
        LOG.info("[{}] Rule message received. {}", myId, ruleMessage);
        executor.route(ruleMessage, getSelf());
    }

}
