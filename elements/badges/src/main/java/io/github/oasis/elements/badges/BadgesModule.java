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
import io.github.oasis.core.elements.AbstractProcessor;
import io.github.oasis.core.elements.AbstractRule;
import io.github.oasis.core.elements.AbstractSink;
import io.github.oasis.core.elements.RuleContext;
import io.github.oasis.core.elements.Signal;
import io.github.oasis.core.elements.SignalCollector;
import io.github.oasis.core.external.Db;
import io.github.oasis.core.elements.ElementModule;
import io.github.oasis.elements.badges.rules.BadgeConditionalRule;
import io.github.oasis.elements.badges.rules.BadgeFirstEventRule;
import io.github.oasis.elements.badges.rules.BadgeHistogramStreakNRule;
import io.github.oasis.elements.badges.rules.BadgeRule;
import io.github.oasis.elements.badges.rules.BadgeStreakNRule;
import io.github.oasis.elements.badges.rules.BadgeTemporalRule;
import io.github.oasis.elements.badges.rules.BadgeTemporalStreakNRule;

import java.util.List;

/**
 * @author Isuru Weerarathna
 */
public class BadgesModule extends ElementModule {

    private final List<Class<? extends AbstractSink>> sinks = List.of(BadgeSink.class);

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
        if (rule instanceof BadgeFirstEventRule) {
            RuleContext<BadgeFirstEventRule> ruleContext = new RuleContext<>((BadgeFirstEventRule) rule, collector);
            return new BadgeFirstEvent(db, ruleContext);
        } else if (rule instanceof BadgeConditionalRule) {
            RuleContext<BadgeConditionalRule> ruleContext = new RuleContext<>((BadgeConditionalRule) rule, collector);
            return new BadgeConditionalProcessor(db, ruleContext);
        } else if (rule instanceof BadgeHistogramStreakNRule) {
            RuleContext<BadgeHistogramStreakNRule> ruleContext = new RuleContext<>((BadgeHistogramStreakNRule) rule, collector);
            return new BadgeHistogramStreakN(db, ruleContext);
        } else if (rule instanceof BadgeTemporalStreakNRule) {
            RuleContext<BadgeStreakNRule> ruleContext = new RuleContext<>((BadgeStreakNRule) rule, collector);
            return new BadgeTemporalStreakN(db, ruleContext);
        } else if (rule instanceof BadgeTemporalRule) {
            RuleContext<BadgeTemporalRule> ruleContext = new RuleContext<>((BadgeTemporalRule) rule, collector);
            return new BadgeTemporalProcessor(db, ruleContext);
        } else if (rule instanceof BadgeStreakNRule) {
            RuleContext<BadgeStreakNRule> ruleContext = new RuleContext<>((BadgeStreakNRule) rule, collector);
            return new BadgeStreakN(db, ruleContext);
        }
        return null;
    }
}
