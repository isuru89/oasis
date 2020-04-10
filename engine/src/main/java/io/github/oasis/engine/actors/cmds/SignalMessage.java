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

package io.github.oasis.engine.actors.cmds;

import io.github.oasis.engine.model.ExecutionContext;
import io.github.oasis.engine.rules.AbstractRule;
import io.github.oasis.engine.rules.signals.Signal;

/**
 * @author Isuru Weerarathna
 */
public class SignalMessage implements OasisCommand {

    private Signal signal;
    private AbstractRule rule;
    private ExecutionContext context;

    public SignalMessage(Signal signal, ExecutionContext context, AbstractRule rule) {
        this.signal = signal;
        this.rule = rule;
    }

    public ExecutionContext getContext() {
        return context;
    }

    public Signal getSignal() {
        return signal;
    }

    public AbstractRule getRule() {
        return rule;
    }
}