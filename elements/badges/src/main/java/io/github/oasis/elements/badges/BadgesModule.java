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

package io.github.oasis.elements.badges;

import io.github.oasis.core.context.RuleExecutionContextSupport;
import io.github.oasis.core.context.RuntimeContextSupport;
import io.github.oasis.core.elements.*;
import io.github.oasis.core.external.Db;
import io.github.oasis.elements.badges.processors.*;
import io.github.oasis.elements.badges.rules.*;

import java.util.List;

/**
 * @author Isuru Weerarathna
 */
public class BadgesModule extends ElementModule {

    private static final String BADGES = "core:badge";

    private final List<String> keysSupported = List.of(BADGES);
    private final List<Class<? extends AbstractSink>> sinks = List.of(BadgeSink.class);
    private final ElementParser parser = new BadgeParser();

    @Override
    public List<Class<? extends AbstractDef>> getSupportedDefinitions() {
        return List.of(BadgeDef.class);
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
        return new BadgeSink(context.getDb());
    }

    @Override
    public AbstractProcessor<? extends AbstractRule, ? extends Signal> createProcessor(AbstractRule rule, RuleExecutionContextSupport ruleExecutionContext) {
        if (rule instanceof BadgeRule) {
            return createBadgeProcessor(ruleExecutionContext.getDb(),
                    (BadgeRule) rule,
                    ruleExecutionContext.getSignalCollector());
        }
        return null;
    }

    private AbstractProcessor<? extends AbstractRule, ? extends Signal> createBadgeProcessor(Db db, BadgeRule rule, SignalCollector collector) {
        if (rule instanceof FirstEventBadgeRule) {
            var ruleContext = new RuleContext<>((FirstEventBadgeRule) rule, collector);
            return new BadgeFirstEvent(db, ruleContext);
        } else if (rule instanceof ConditionalBadgeRule) {
            var ruleContext = new RuleContext<>((ConditionalBadgeRule) rule, collector);
            return new ConditionalBadgeProcessor(db, ruleContext);
        } else if (rule instanceof PeriodicStreakNRule) {
            var ruleContext = new RuleContext<>((PeriodicStreakNRule) rule, collector);
            return new PeriodicStreakNBadge(db, ruleContext);
        } else if (rule instanceof TimeBoundedStreakNRule) {
            var ruleContext = new RuleContext<>((StreakNBadgeRule) rule, collector);
            return new TimeBoundedStreakNBadge(db, ruleContext);
        } else if (rule instanceof PeriodicBadgeRule) {
            var ruleContext = new RuleContext<>((PeriodicBadgeRule) rule, collector);
            return new PeriodicBadgeProcessor(db, ruleContext);
        } else if (rule instanceof StreakNBadgeRule) {
            var ruleContext = new RuleContext<>((StreakNBadgeRule) rule, collector);
            return new StreakNBadgeProcessor(db, ruleContext);
        }
        return null;
    }
}
