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
import io.github.oasis.core.Event;
import io.github.oasis.core.elements.AbstractProcessor;
import io.github.oasis.core.elements.AbstractRule;
import io.github.oasis.core.elements.Signal;
import io.github.oasis.engine.EngineContext;
import io.github.oasis.engine.actors.cmds.EventMessage;
import io.github.oasis.engine.actors.cmds.OasisRuleMessage;
import io.github.oasis.engine.actors.cmds.StartRuleExecutionCommand;
import io.github.oasis.engine.ext.ExternalParty;
import io.github.oasis.engine.ext.ExternalPartyImpl;
import io.github.oasis.engine.ext.RulesImpl;
import io.github.oasis.engine.factory.Processors;
import io.github.oasis.engine.model.RuleExecutionContext;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Isuru Weerarathna
 */
public class RuleExecutor extends OasisBaseActor {

    private static final AtomicLong COUNTER = new AtomicLong(0L);

    private final Map<String, AbstractProcessor<? extends AbstractRule, ? extends Signal>> cache = new HashMap<>();

    private String parentId;

    private final AbstractActor.Receive executing;
    private final AbstractActor.Receive starting;
    private RuleExecutionContext ruleExecutionContext;

    private final Processors processors;

    private ExternalPartyImpl eventSource;

    public RuleExecutor(EngineContext context) {
        super(context);

        this.myId = "E" + COUNTER.incrementAndGet();
        this.processors = context.getProcessors();

        starting = receiveBuilder()
                .match(StartRuleExecutionCommand.class, this::assignRules)
                .build();
        executing = receiveBuilder()
                .match(EventMessage.class, this::processEvent)
                .match(OasisRuleMessage.class, this::ruleModified)
                .build();
    }

    @Override
    public void preStart() throws Exception {
        super.preStart();

        eventSource = ExternalParty.EXTERNAL_PARTY.get(getContext().getSystem());
    }

    @Override
    public Receive createReceive() {
        return starting;
    }

    private void ruleModified(OasisRuleMessage message) {
        RulesImpl.GameRules rules = getGameRuleRef(message.getGameId());
        message.applyTo(rules);
    }

    private void assignRules(StartRuleExecutionCommand command) {
        this.parentId = command.getParentId();
        this.ruleExecutionContext = RuleExecutionContext.from(engineContext, command.getRuleExecutionContext());
        log.info("[{}] Initialization from {}", myId, parentId);
        getContext().become(executing);
    }

    private void processEvent(EventMessage eventMessage) {
        RulesImpl.GameRules rules = getGameRuleRef(eventMessage.getEvent().getGameId());
        Event event = eventMessage.getEvent();
        log.info("[{}#{}] Processing event {}", parentId, myId, event);
        Iterator<AbstractRule> allRulesForEvent = rules.getAllRulesForEvent(event);
        try {
            while (allRulesForEvent.hasNext()) {
                AbstractRule rule = allRulesForEvent.next();

                // create processor
                AbstractProcessor<? extends AbstractRule, ? extends Signal> processor = cache.computeIfAbsent(rule.getId(),
                        s -> processors.createProcessor(rule, ruleExecutionContext));

                // execute processor using event
                processor.accept(event, eventMessage.getContext());
            }

            Object messageId = eventMessage.getExternalMessageId();
            log.debug("[{}#{}] Acknowledging successful event for message id {}", parentId, myId, messageId);
            eventSource.ackMessage(messageId);

        } catch (Exception e) {
            log.error("[{}#{}] Error occurred while processing event {}", parentId, myId, event, e);

            Object messageId = eventMessage.getExternalMessageId();
            log.debug("[{}#{}] Acknowledging failure event for message id {}", parentId, myId, messageId);
            eventSource.nackMessage(messageId);
        }
    }

}
