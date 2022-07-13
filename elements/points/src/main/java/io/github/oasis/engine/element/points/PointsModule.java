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

package io.github.oasis.engine.element.points;

import io.github.oasis.core.context.RuleExecutionContextSupport;
import io.github.oasis.core.context.RuntimeContextSupport;
import io.github.oasis.core.elements.AbstractDef;
import io.github.oasis.core.elements.AbstractProcessor;
import io.github.oasis.core.elements.AbstractRule;
import io.github.oasis.core.elements.AbstractSink;
import io.github.oasis.core.elements.ElementModule;
import io.github.oasis.core.elements.ElementParser;
import io.github.oasis.core.elements.RuleContext;
import io.github.oasis.core.elements.Signal;
import io.github.oasis.core.exception.OasisException;
import io.github.oasis.core.external.Db;
import io.github.oasis.engine.element.points.stats.PointStats;

import java.util.List;

/**
 * @author Isuru Weerarathna
 */
public class PointsModule extends ElementModule {

    public static final String ID = "core:point";

    private final List<String> keysSupported = List.of(ID);
    private final List<Class<? extends AbstractSink>> sinks = List.of(PointsSink.class);
    private final ElementParser parser = new PointParser();

    @Override
    public void init(RuntimeContextSupport context) throws OasisException {
        Db db = context.getDb();

        loadScriptsUnderPackage(db, PointStats.class, Thread.currentThread().getContextClassLoader());
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public List<Class<? extends AbstractDef>> getSupportedDefinitions() {
        return List.of(PointDef.class);
    }

    @Override
    public List<String> getSupportedDefinitionKeys() {
        return keysSupported;
    }

    @Override
    public ElementParser getParser() {
        return parser;
    }

    @Override
    public List<Class<? extends AbstractSink>> getSupportedSinks() {
        return sinks;
    }

    @Override
    public AbstractSink createSink(Class<? extends AbstractSink> sinkReq, RuntimeContextSupport context) {
        return new PointsSink(context.getDb());
    }

    @Override
    public AbstractProcessor<? extends AbstractRule, ? extends Signal> createProcessor(AbstractRule rule, RuleExecutionContextSupport ruleExecutionContext) {
        if (rule instanceof PointRule) {
            RuleContext<PointRule> ruleContext = new RuleContext<>((PointRule) rule, ruleExecutionContext.getSignalCollector());
            return new PointsProcessor(ruleExecutionContext.getDb(), ruleContext);
        }
        return null;
    }


}
