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

import io.github.oasis.engine.actors.cmds.SignalMessage;
import io.github.oasis.engine.external.Db;
import io.github.oasis.engine.rules.AbstractRule;
import io.github.oasis.engine.rules.signals.PointSignal;
import io.github.oasis.engine.rules.signals.Signal;
import io.github.oasis.engine.sinks.PointsSink;

import javax.inject.Inject;

/**
 * @author Isuru Weerarathna
 */
public class SignalConsumer extends OasisBaseActor {

    @Inject
    private Db db;

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(SignalMessage.class, this::processSignal)
                .build();
    }

    private void processSignal(SignalMessage signalMessage) {
        Signal signal = signalMessage.getSignal();
        AbstractRule rule = signalMessage.getRule();

        System.out.println("Processing signal " + signalMessage + " with rule " + rule);
        if (signal instanceof PointSignal) {
            new PointsSink(db).consume(signal, rule);
        }
    }
}
