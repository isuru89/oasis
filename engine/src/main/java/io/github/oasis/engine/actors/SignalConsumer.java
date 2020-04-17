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

import io.github.oasis.engine.EngineContext;
import io.github.oasis.engine.actors.cmds.SignalMessage;
import io.github.oasis.engine.actors.cmds.StartRuleExecutionCommand;
import io.github.oasis.core.elements.AbstractRule;
import io.github.oasis.core.elements.AbstractSink;
import io.github.oasis.core.elements.Signal;
import io.github.oasis.engine.factory.Sinks;
import io.github.oasis.core.context.ExecutionContext;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Isuru Weerarathna
 */
public class SignalConsumer extends OasisBaseActor {

    private static final AtomicLong COUNTER = new AtomicLong(0L);

    private String logId;
    private Sinks sinks;

    public SignalConsumer(EngineContext context) {
        super(context);

        this.sinks = context.getSinks();
        myId = "C" + COUNTER.incrementAndGet();
        logId = myId;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(StartRuleExecutionCommand.class, this::initializeMe)
                .match(SignalMessage.class, this::processSignal)
                .build();
    }

    private void initializeMe(StartRuleExecutionCommand message) {
        parentId = message.getParentId();
        log.info("[{}] Initialization from {}", myId, parentId);
        logId = parentId + "#" + myId;
    }

    private void processSignal(SignalMessage signalMessage) {
        Signal signal = signalMessage.getSignal();
        AbstractRule rule = signalMessage.getRule();
        ExecutionContext context = signalMessage.getContext();

        AbstractSink sink = sinks.create(signal.sinkHandler());
        log.info("[{}] {} processing {} of rule {}", logId, sink, signal, rule);
        sink.consume(signal, rule, context);
    }
}
