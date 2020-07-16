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

import akka.pattern.CircuitBreaker;
import io.github.oasis.core.context.ExecutionContext;
import io.github.oasis.core.elements.AbstractRule;
import io.github.oasis.core.elements.AbstractSink;
import io.github.oasis.core.elements.Signal;
import io.github.oasis.core.exception.OasisRuntimeException;
import io.github.oasis.core.external.SignalSubscriptionSupport;
import io.github.oasis.engine.EngineContext;
import io.github.oasis.engine.actors.cmds.SignalMessage;
import io.github.oasis.engine.actors.cmds.StartRuleExecutionCommand;
import io.github.oasis.engine.factory.Sinks;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Isuru Weerarathna
 */
public class SignalConsumer extends OasisBaseActor {

    private static final String C = "C";
    private static final String HASH = "#";

    private static final AtomicLong COUNTER = new AtomicLong(0L);

    private String logId;
    private final Sinks sinks;
    private final SignalSubscriptionSupport signalSubscription;
    private final CircuitBreaker breaker;

    public SignalConsumer(EngineContext context) {
        super(context);

        this.sinks = context.getSinks();
        signalSubscription = context.getSignalSubscription();
        myId = C + COUNTER.incrementAndGet();
        logId = myId;

        this.breaker = new CircuitBreaker(
                getContext().getDispatcher(),
                getContext().getSystem().getScheduler(),
                5,
                Duration.ofSeconds(10),
                Duration.ofMinutes(1)
        );
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(StartRuleExecutionCommand.class, this::initializeMe)
                .match(SignalMessage.class, (msg) -> breaker.callWithSyncCircuitBreaker(() -> processSignal(msg)))
                .build();
    }

    private void initializeMe(StartRuleExecutionCommand message) {
        parentId = message.getParentId();
        log.info("[{}] Initialization from {}", myId, parentId);
        logId = parentId + HASH + myId;
    }

    private boolean processSignal(SignalMessage signalMessage) {
        Signal signal = signalMessage.getSignal();
        AbstractRule rule = signalMessage.getRule();
        ExecutionContext context = signalMessage.getContext();

        AbstractSink sink = sinks.create(signal.sinkHandler());
        log.info("[{}] {} processing {} of rule {}", logId, sink, signal, rule);
        try {
            sink.consume(signal, rule, context);

            if (Objects.nonNull(signalSubscription)) {
                log.debug("[{}] {} notifying subscriber", logId, sink);
                signalSubscription.notifyAfter(signal, rule, context);
            }
            return true;
        } catch (OasisRuntimeException e) {
            log.error("[{}] {} error while sinking signal {}", logId, sink, signal, e);
            throw e;
        }
    }
}
