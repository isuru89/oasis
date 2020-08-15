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

package io.github.oasis.elements.milestones;

import io.github.oasis.core.Event;
import io.github.oasis.core.ID;
import io.github.oasis.core.context.ExecutionContext;
import io.github.oasis.core.elements.AbstractProcessor;
import io.github.oasis.core.elements.RuleContext;
import io.github.oasis.core.external.Db;
import io.github.oasis.core.external.DbContext;
import io.github.oasis.core.external.Mapped;
import io.github.oasis.core.external.Sorted;
import io.github.oasis.core.utils.Constants;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

import static io.github.oasis.core.utils.Numbers.isNegative;
import static io.github.oasis.elements.milestones.MilestoneRule.SKIP_NEGATIVE_VALUES;
import static io.github.oasis.elements.milestones.MilestoneRule.TRACK_PENALTIES;

/**
 * @author Isuru Weerarathna
 */
public class MilestoneProcessor extends AbstractProcessor<MilestoneRule, MilestoneSignal> {

    private static final String LAST_UPDATED = "%s:lastupdated";
    private static final String LAST_EVENT = "%s:lastevent";

    public MilestoneProcessor(Db dbPool, RuleContext<MilestoneRule> ruleCtx) {
        super(dbPool, ruleCtx);
    }

    @Override
    protected void beforeEmit(MilestoneSignal signal, Event event, MilestoneRule rule, ExecutionContext context, DbContext db) {
        // do nothing...
    }

    @Override
    public List<MilestoneSignal> process(Event event, MilestoneRule rule, ExecutionContext context, DbContext db) {
        if (Objects.isNull(rule.getValueExtractor())) {
            return null;
        }
        BigDecimal delta = rule.getValueExtractor().resolve(event, rule, context).setScale(Constants.SCALE, RoundingMode.HALF_UP);
        if (rule.hasFlag(SKIP_NEGATIVE_VALUES) && isNegative(delta)) {
            return null;
        }
        Mapped userMilestonesMap = db.MAP(MilestoneIDs.getGameUserMilestonesSummary(event.getGameId(), event.getUser()));
        userMilestonesMap.incrementByDecimal(rule.getId(), delta);
        userMilestonesMap.setValues(String.format(LAST_UPDATED, rule.getId()),
                String.valueOf(event.getTimestamp()),
                String.format(LAST_EVENT, rule.getId()),
                event.getExternalId());

        String milestoneKey = MilestoneIDs.getGameMilestoneKey(event.getGameId(), rule.getId());
        Sorted gameMilestoneMap = db.SORTED(milestoneKey);
        BigDecimal updatedValue = gameMilestoneMap.incrementScore(ID.getUserKeyUnderGameMilestone(event.getUser()), delta);
        if (rule.hasFlag(TRACK_PENALTIES) && isNegative(delta)) {
            userMilestonesMap.incrementByDecimal(String.format("%s:penalties", rule.getId()), delta);
        }

        MilestoneRule.Level currentLevel = rule.getLevelFor(updatedValue).orElse(null);
        BigDecimal prevValue = updatedValue.subtract(delta);
        MilestoneRule.Level prevLevel = rule.getLevelFor(prevValue).orElse(null);
        if (hasLevelIncreased(prevLevel, currentLevel)) {
            return List.of(new MilestoneSignal(rule.getId(),
                    defaultLevel(prevLevel),
                    defaultLevel(currentLevel),
                    updatedValue,
                    event));
        } else if (!rule.hasFlag(SKIP_NEGATIVE_VALUES) && hasLevelDecreased(prevLevel, currentLevel)) {
            return List.of(new MilestoneSignal(rule.getId(),
                    defaultLevel(prevLevel),
                    defaultLevel(currentLevel),
                    updatedValue,
                    event));
        }
        return null;
    }

    private int defaultLevel(MilestoneRule.Level level) {
        return level == null ? 0 : level.getLevel();
    }

    private boolean hasLevelDecreased(MilestoneRule.Level prevLevel, MilestoneRule.Level currentLevel) {
        if (prevLevel != null) {
            return currentLevel == null || currentLevel.getLevel() < prevLevel.getLevel();
        }
        return false;
    }

    private boolean hasLevelIncreased(MilestoneRule.Level prevLevel, MilestoneRule.Level currentLevel) {
        if (currentLevel != null) {
            return prevLevel == null || currentLevel.getLevel() > prevLevel.getLevel();
        }
        return false;
    }
}
