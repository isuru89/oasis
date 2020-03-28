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

package io.github.oasis.engine.rules;

import io.github.oasis.engine.model.ID;
import io.github.oasis.engine.rules.signals.BadgeRemoveSignal;
import io.github.oasis.engine.rules.signals.BadgeSignal;
import io.github.oasis.engine.rules.signals.TemporalBadge;
import io.github.oasis.model.Event;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static io.github.oasis.engine.utils.Constants.SCALE;
import static io.github.oasis.engine.utils.Numbers.asDecimal;
import static io.github.oasis.engine.utils.Numbers.isIncreasedOrEqual;
import static io.github.oasis.engine.utils.Numbers.isThresholdCrossedDown;
import static io.github.oasis.engine.utils.Numbers.isThresholdCrossedUp;

/**
 * Satisfy condition N times within a time unit. (daily, weekly, monthly)
 *
 * @author Isuru Weerarathna
 */
public class TemporalBadgeProcessor extends BadgeProcessor implements Consumer<Event> {

    private TemporalBadgeRule rule;

    public TemporalBadgeProcessor(JedisPool pool, TemporalBadgeRule rule) {
        super(pool);
        this.rule = rule;
    }

    @Override
    public void accept(Event event) {
        if (!isMatchEvent(event, rule)) {
            return;
        }

        if (!isConditionSatisfied(event, rule.getCondition())) {
            return;
        }

        try (Jedis jedis = pool.getResource()) {
            List<BadgeSignal> badges = createBadges(event, rule, jedis);
            if (badges != null) {
                badges.forEach(badgeSignal -> {
                    beforeBatchEmit(badgeSignal, event, rule, jedis);
                    rule.getCollector().accept(badgeSignal);
                });
            }
        }
    }

    private List<BadgeSignal> createBadges(Event event, TemporalBadgeRule rule, Jedis jedis) {
        BigDecimal value = resolveValueOfEvent(event, rule);
        String badgeKey = ID.getUserTemporalBadgeKey(event.getGameId(), event.getUser(), rule.getId());
        long ts = event.getTimestamp();
        long tsUnit = ts - (ts % rule.getTimeUnit());
        String subKey = String.valueOf(tsUnit);
        BigDecimal updatedVal = asDecimal(jedis.hincrByFloat(badgeKey, subKey, value.doubleValue()));
        BigDecimal prevValue = updatedVal.subtract(value).setScale(SCALE, BigDecimal.ROUND_HALF_UP);
        boolean increased = isIncreasedOrEqual(prevValue, updatedVal);
        Optional<List<TemporalBadgeRule.Threshold>> crossedThreshold = getCrossedThreshold(prevValue, updatedVal, rule);
        return crossedThreshold.map(thresholds -> thresholds.stream()
                .map(threshold -> increased
                        ? badgeCreation(rule, threshold, event, tsUnit)
                        : badgeRemoval(rule, threshold, event, tsUnit))
                .collect(Collectors.toList())).orElse(null);
    }

    private BadgeSignal badgeCreation(TemporalBadgeRule rule, TemporalBadgeRule.Threshold threshold, Event event, long tsUnit) {
        return new TemporalBadge(rule.getId(),
                threshold.getAttribute(),
                tsUnit,
                tsUnit + rule.getTimeUnit(),
                event.getTimestamp(),
                event.getExternalId());
    }

    private BadgeSignal badgeRemoval(TemporalBadgeRule rule, TemporalBadgeRule.Threshold threshold, Event event, long tsUnit) {
        return new BadgeRemoveSignal(rule.getId(),
                threshold.getAttribute(),
                tsUnit,
                tsUnit + rule.getTimeUnit(),
                event.getExternalId(),
                event.getExternalId());
    }

    private Optional<List<TemporalBadgeRule.Threshold>> getCrossedThreshold(BigDecimal prev, BigDecimal now, TemporalBadgeRule rule) {
        List<TemporalBadgeRule.Threshold> thresholds = new LinkedList<>();
        for (TemporalBadgeRule.Threshold threshold : rule.getThresholds()) {
            if (isThresholdCrossedUp(prev, now, threshold.getValue())) {
                thresholds.add(threshold);
            } else if (isThresholdCrossedDown(prev, now, threshold.getValue())) {
                thresholds.add(threshold);
            }
        }
        return thresholds.isEmpty() ? Optional.empty() : Optional.of(thresholds);
    }

    private BigDecimal resolveValueOfEvent(Event event, TemporalBadgeRule rule) {
        return rule.getValueResolver().apply(event).setScale(SCALE, BigDecimal.ROUND_HALF_UP);
    }

    private boolean isConditionSatisfied(Event event, Predicate<Event> condition) {
        return condition == null || condition.test(event);
    }
}
