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
import io.github.oasis.engine.actors.cmds.OasisRuleMessage;
import io.github.oasis.engine.actors.cmds.StartRuleExecutionCommand;
import io.github.oasis.engine.model.Rules;
import io.github.oasis.engine.processors.AbstractProcessor;
import io.github.oasis.engine.processors.Processors;
import io.github.oasis.engine.rules.AbstractRule;
import io.github.oasis.engine.rules.signals.Signal;
import io.github.oasis.model.Event;

import javax.inject.Inject;
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

    private int parentId;
    private long myId;
    private Rules rules;

    private AbstractActor.Receive executing;
    private AbstractActor.Receive starting;

    @Inject private Processors processors;

    public RuleExecutor() {
        starting = receiveBuilder()
                .match(StartRuleExecutionCommand.class, this::assignRules)
                .build();
        executing = receiveBuilder()
                .match(Event.class, this::processEvent)
                .match(OasisRuleMessage.class, this::ruleModified)
                .build();
    }

    @Override
    public Receive createReceive() {
        return starting;
    }

    private void ruleModified(OasisRuleMessage message) {
        //System.out.println("Rule modified received in " + myId + " IN " + this);
    }

    private void assignRules(StartRuleExecutionCommand startRuleExecutionCommand) {
        this.rules = startRuleExecutionCommand.getRules();
        this.parentId = startRuleExecutionCommand.getParentId();
        this.myId = COUNTER.incrementAndGet();
        System.out.println("Initializing msg recieved " + rules + " -- " + this.myId + " @" + System.currentTimeMillis() + " : " + this);
        getContext().become(executing);
    }

    private void processEvent(Event event) {
        System.out.println("Processing event for user #" + event.getUser() + " in " + myId + " p " + processors + " in this " + this);
        Iterator<AbstractRule> allRulesForEvent = rules.getAllRulesForEvent(event);
        while (allRulesForEvent.hasNext()) {
            AbstractRule rule = allRulesForEvent.next();

            // create processor
            AbstractProcessor<? extends AbstractRule, ? extends Signal> processor = cache.computeIfAbsent(rule.getId(),
                    s -> processors.createProcessor(rule, rules.getCollector()));

            // execute processor using event
            processor.accept(event);
        }
    }

}
