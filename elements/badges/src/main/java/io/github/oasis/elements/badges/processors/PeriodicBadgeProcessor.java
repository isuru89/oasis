/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.github.oasis.elements.badges.processors;

import io.github.oasis.core.Event;
import io.github.oasis.core.context.ExecutionContext;
import io.github.oasis.core.elements.RuleContext;
import io.github.oasis.core.external.Db;
import io.github.oasis.core.external.DbContext;
import io.github.oasis.core.external.Mapped;
import io.github.oasis.core.utils.Utils;
import io.github.oasis.elements.badges.BadgeIDs;
import io.github.oasis.elements.badges.rules.PeriodicBadgeRule;
import io.github.oasis.elements.badges.signals.BadgeRemoveSignal;
import io.github.oasis.elements.badges.signals.BadgeSignal;
import io.github.oasis.elements.badges.signals.TemporalBadgeSignal;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.github.oasis.core.utils.Constants.SCALE;
import static io.github.oasis.core.utils.Numbers.isIncreasedOrEqual;
import static io.github.oasis.core.utils.Numbers.isThresholdCrossedDown;
import static io.github.oasis.core.utils.Numbers.isThresholdCrossedUp;

/**
 * Satisfy condition N times within a tumbling time unit. (daily, weekly, monthly)
 * Based on different thresholds, user will be awarded different badges.
 *
 * For e.g: Award,
 *            - gold badge if user scores more than 1000+ points in a day
 *            - silver badge if user scores more than 500+ points in a day
 *            - bronze badge if user scores more than 200+ points in a day
 *
 * @author Isuru Weerarathna
 */
public class PeriodicBadgeProcessor extends AbstractBadgeProcessor<PeriodicBadgeRule> {

    public PeriodicBadgeProcessor(Db pool, RuleContext<PeriodicBadgeRule> ruleContext) {
        super(pool, ruleContext);
    }

    @Override
    public boolean isDenied(Event event, ExecutionContext context) {
        return super.isDenied(event, context) || !isCriteriaSatisfied(event, rule, context);
    }

    @Override
    public List<BadgeSignal> process(Event event, PeriodicBadgeRule rule, ExecutionContext context, DbContext db) {
        BigDecimal value = resolveValueOfEvent(event, rule, context);
        String badgeKey = BadgeIDs.getUserTemporalBadgeKey(event.getGameId(), event.getUser(), rule.getId());
        Mapped map = db.MAP(badgeKey);
        long ts = event.getTimestamp() + context.getUserTimeOffset();
        long tsUnit = ts - (ts % rule.getTimeUnit());
        String subKey = String.valueOf(tsUnit);
        BigDecimal updatedVal = map.incrementByDecimal(subKey, value);
        BigDecimal prevValue = updatedVal.subtract(value).setScale(SCALE, RoundingMode.HALF_UP);
        boolean increased = isIncreasedOrEqual(prevValue, updatedVal);
        Optional<List<PeriodicBadgeRule.Threshold>> crossedThreshold = getCrossedThreshold(prevValue, updatedVal, rule);
        return crossedThreshold.map(thresholds ->
                thresholds.stream()
                        .map(threshold -> increased
                        ? badgeCreation(rule, threshold, event, tsUnit)
                        : badgeRemoval(rule, threshold, event, tsUnit))
                .collect(Collectors.toList()))
                .orElse(null);
    }

    private BadgeSignal badgeCreation(PeriodicBadgeRule rule, PeriodicBadgeRule.Threshold threshold, Event event, long tsUnit) {
        return new TemporalBadgeSignal(rule.getId(),
                Utils.firstNonNull(threshold.getBadgeId(), rule.getId()),
                event,
                threshold.getAttribute(),
                tsUnit,
                tsUnit + rule.getTimeUnit(),
                event.getTimestamp(),
                event.getExternalId());
    }

    private BadgeSignal badgeRemoval(PeriodicBadgeRule rule, PeriodicBadgeRule.Threshold threshold, Event event, long tsUnit) {
        return new BadgeRemoveSignal(rule.getId(),
                Utils.firstNonNull(threshold.getBadgeId(), rule.getId()),
                event.asEventScope(),
                threshold.getAttribute(),
                tsUnit,
                tsUnit + rule.getTimeUnit(),
                event.getExternalId(),
                event.getExternalId());
    }

    private Optional<List<PeriodicBadgeRule.Threshold>> getCrossedThreshold(BigDecimal prev, BigDecimal now, PeriodicBadgeRule rule) {
        List<PeriodicBadgeRule.Threshold> thresholds = new LinkedList<>();
        for (PeriodicBadgeRule.Threshold threshold : rule.getThresholds()) {
            if (isThresholdCrossedUp(prev, now, threshold.getValue())) {
                thresholds.add(threshold);
            } else if (isThresholdCrossedDown(prev, now, threshold.getValue())) {
                thresholds.add(threshold);
            }
        }
        return thresholds.isEmpty() ? Optional.empty() : Optional.of(thresholds);
    }

    private BigDecimal resolveValueOfEvent(Event event, PeriodicBadgeRule rule, ExecutionContext context) {
        return rule.getValueResolver().resolve(event, context).setScale(SCALE, RoundingMode.HALF_UP);
    }

    private boolean isCriteriaSatisfied(Event event, PeriodicBadgeRule rule, ExecutionContext context) {
        return rule.getCriteria() == null || rule.getCriteria().matches(event, rule, context);
    }
}
