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

import io.github.oasis.engine.model.ID;
import io.github.oasis.engine.model.RuleContext;
import io.github.oasis.engine.rules.BadgeTemporalRule;
import io.github.oasis.engine.rules.signals.BadgeRemoveSignal;
import io.github.oasis.engine.rules.signals.BadgeSignal;
import io.github.oasis.engine.rules.signals.TemporalBadge;
import io.github.oasis.engine.storage.Db;
import io.github.oasis.engine.storage.DbContext;
import io.github.oasis.engine.storage.Mapped;
import io.github.oasis.model.Event;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.github.oasis.engine.utils.Constants.SCALE;
import static io.github.oasis.engine.utils.Numbers.isIncreasedOrEqual;
import static io.github.oasis.engine.utils.Numbers.isThresholdCrossedDown;
import static io.github.oasis.engine.utils.Numbers.isThresholdCrossedUp;

/**
 * Satisfy condition N times within a tumbling time unit. (daily, weekly, monthly)
 *
 * @author Isuru Weerarathna
 */
public class BadgeTemporalProcessor extends BadgeProcessor<BadgeTemporalRule> {

    public BadgeTemporalProcessor(Db pool, RuleContext<BadgeTemporalRule> ruleContext) {
        super(pool, ruleContext);
    }

    @Override
    public boolean isDenied(Event event) {
        return super.isDenied(event) || !isCriteriaSatisfied(event, rule);
    }

    @Override
    public List<BadgeSignal> process(Event event, BadgeTemporalRule rule, DbContext db) {
        BigDecimal value = resolveValueOfEvent(event, rule);
        String badgeKey = ID.getUserTemporalBadgeKey(event.getGameId(), event.getUser(), rule.getId());
        Mapped map = db.MAP(badgeKey);
        long ts = event.getTimestamp();
        long tsUnit = ts - (ts % rule.getTimeUnit());
        String subKey = String.valueOf(tsUnit);
        BigDecimal updatedVal = map.incrementByDecimal(subKey, value);
        BigDecimal prevValue = updatedVal.subtract(value).setScale(SCALE, BigDecimal.ROUND_HALF_UP);
        boolean increased = isIncreasedOrEqual(prevValue, updatedVal);
        Optional<List<BadgeTemporalRule.Threshold>> crossedThreshold = getCrossedThreshold(prevValue, updatedVal, rule);
        return crossedThreshold.map(thresholds -> thresholds.stream()
                .map(threshold -> increased
                        ? badgeCreation(rule, threshold, event, tsUnit)
                        : badgeRemoval(rule, threshold, event, tsUnit))
                .collect(Collectors.toList())).orElse(null);
    }

    private BadgeSignal badgeCreation(BadgeTemporalRule rule, BadgeTemporalRule.Threshold threshold, Event event, long tsUnit) {
        return new TemporalBadge(rule.getId(),
                event,
                threshold.getAttribute(),
                tsUnit,
                tsUnit + rule.getTimeUnit(),
                event.getTimestamp(),
                event.getExternalId());
    }

    private BadgeSignal badgeRemoval(BadgeTemporalRule rule, BadgeTemporalRule.Threshold threshold, Event event, long tsUnit) {
        return new BadgeRemoveSignal(rule.getId(),
                event.asEventScope(),
                threshold.getAttribute(),
                tsUnit,
                tsUnit + rule.getTimeUnit(),
                event.getExternalId(),
                event.getExternalId());
    }

    private Optional<List<BadgeTemporalRule.Threshold>> getCrossedThreshold(BigDecimal prev, BigDecimal now, BadgeTemporalRule rule) {
        List<BadgeTemporalRule.Threshold> thresholds = new LinkedList<>();
        for (BadgeTemporalRule.Threshold threshold : rule.getThresholds()) {
            if (isThresholdCrossedUp(prev, now, threshold.getValue())) {
                thresholds.add(threshold);
            } else if (isThresholdCrossedDown(prev, now, threshold.getValue())) {
                thresholds.add(threshold);
            }
        }
        return thresholds.isEmpty() ? Optional.empty() : Optional.of(thresholds);
    }

    private BigDecimal resolveValueOfEvent(Event event, BadgeTemporalRule rule) {
        return rule.getValueResolver().apply(event).setScale(SCALE, BigDecimal.ROUND_HALF_UP);
    }

    private boolean isCriteriaSatisfied(Event event, BadgeTemporalRule rule) {
        return rule.getCriteria() == null || rule.getCriteria().test(event);
    }
}
