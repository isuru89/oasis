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

package io.github.oasis.engine.processors;

import io.github.oasis.engine.elements.AbstractProcessor;
import io.github.oasis.engine.elements.badges.BadgeConditionalProcessor;
import io.github.oasis.engine.elements.badges.BadgeFirstEvent;
import io.github.oasis.engine.elements.badges.BadgeHistogramStreakN;
import io.github.oasis.engine.elements.badges.BadgeStreakN;
import io.github.oasis.engine.elements.badges.BadgeTemporalProcessor;
import io.github.oasis.engine.elements.badges.BadgeTemporalStreakN;
import io.github.oasis.engine.elements.challenges.ChallengeProcessor;
import io.github.oasis.engine.elements.milestones.MilestoneProcessor;
import io.github.oasis.engine.elements.points.PointsProcessor;
import io.github.oasis.engine.elements.ratings.RatingProcessor;
import io.github.oasis.engine.external.Db;
import io.github.oasis.engine.model.RuleContext;
import io.github.oasis.engine.model.SignalCollector;
import io.github.oasis.engine.elements.AbstractRule;
import io.github.oasis.engine.elements.badges.rules.BadgeConditionalRule;
import io.github.oasis.engine.elements.badges.rules.BadgeFirstEventRule;
import io.github.oasis.engine.elements.badges.rules.BadgeHistogramStreakNRule;
import io.github.oasis.engine.elements.badges.rules.BadgeRule;
import io.github.oasis.engine.elements.badges.rules.BadgeStreakNRule;
import io.github.oasis.engine.elements.badges.rules.BadgeTemporalRule;
import io.github.oasis.engine.elements.badges.rules.BadgeTemporalStreakNRule;
import io.github.oasis.engine.elements.challenges.ChallengeRule;
import io.github.oasis.engine.elements.milestones.MilestoneRule;
import io.github.oasis.engine.elements.points.PointRule;
import io.github.oasis.engine.elements.ratings.RatingRule;
import io.github.oasis.engine.elements.Signal;

import javax.inject.Inject;

/**
 * @author Isuru Weerarathna
 */
public class Processors {

    private final Db db;

    @Inject
    private Processors(Db db) {
        this.db = db;
    }

    public AbstractProcessor<? extends AbstractRule, ? extends Signal> createProcessor(AbstractRule rule, SignalCollector collector) {
        if (rule instanceof ChallengeRule) {
            RuleContext<ChallengeRule> ruleContext = new RuleContext<>((ChallengeRule) rule, collector);
            return new ChallengeProcessor(db, ruleContext);
        } else if (rule instanceof PointRule) {
            RuleContext<PointRule> ruleContext = new RuleContext<>((PointRule) rule, collector);
            return new PointsProcessor(db, ruleContext);
        } else if (rule instanceof RatingRule) {
            RuleContext<RatingRule> ruleContext = new RuleContext<>((RatingRule) rule, collector);
            return new RatingProcessor(db, ruleContext);
        } else if (rule instanceof MilestoneRule) {
            RuleContext<MilestoneRule> ruleContext = new RuleContext<>((MilestoneRule) rule, collector);
            return new MilestoneProcessor(db, ruleContext);
        } else if (rule instanceof BadgeRule) {
            return createBadgeProcessor((BadgeRule) rule, collector);
        }
        return null;
    }

    private AbstractProcessor<? extends AbstractRule, ? extends Signal> createBadgeProcessor(BadgeRule rule, SignalCollector collector) {
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
