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

package io.github.oasis.engine.model;

import io.github.oasis.core.configs.OasisConfigs;
import io.github.oasis.core.context.RuleExecutionContextSupport;
import io.github.oasis.core.context.RuntimeContextSupport;
import io.github.oasis.core.elements.SignalCollector;
import io.github.oasis.core.external.Db;
import io.github.oasis.core.external.EventReadWrite;
import io.github.oasis.engine.EngineContext;

import java.io.Serializable;

/**
 * @author Isuru Weerarathna
 */
public class RuleExecutionContext implements RuleExecutionContextSupport, Serializable {

    private SignalCollector signalCollector;
    private RuntimeContextSupport engineContext;

    private RuleExecutionContext(SignalCollector signalCollector, EngineContext engineContext) {
        this.signalCollector = signalCollector;
        this.engineContext = engineContext;
    }

    @Override
    public SignalCollector getSignalCollector() {
        return signalCollector;
    }

    public static RuleExecutionContext from(EngineContext engineContext, RuleExecutionContext other) {
        return new RuleExecutionContext(other.getSignalCollector(), engineContext);
    }

    public static RuleExecutionContext from(SignalCollector collector) {
        return new RuleExecutionContext(collector, null);
    }

    @Override
    public OasisConfigs getConfigs() {
        return engineContext.getConfigs();
    }

    @Override
    public Db getDb() {
        return engineContext.getDb();
    }

    @Override
    public EventReadWrite getEventStore() {
        return engineContext.getEventStore();
    }
}
