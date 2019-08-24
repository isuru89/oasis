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

package io.github.oasis.game.factory.badges;

import io.github.oasis.game.utils.Utils;
import io.github.oasis.model.Badge;
import io.github.oasis.model.collect.Pair;
import io.github.oasis.model.events.BadgeEvent;
import io.github.oasis.model.events.PointEvent;
import io.github.oasis.model.rules.BadgeFromEvents;
import io.github.oasis.model.rules.BadgeFromPoints;
import io.github.oasis.model.rules.PointRule;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.common.functions.FilterFunction;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class SumAggregator implements AggregateFunction<PointEvent, BadgeAggregator, BadgeEvent> {

    private FilterFunction<PointEvent> filterFunction;
    private BadgeFromPoints badgeRule;

    SumAggregator(FilterFunction<PointEvent> filterFunction, BadgeFromPoints rule) {
        this.filterFunction = filterFunction;
        this.badgeRule = rule;
    }

    @Override
    public BadgeAggregator createAccumulator() {
        return new BadgeAggregator();
    }

    @Override
    public BadgeAggregator add(PointEvent value, BadgeAggregator accumulator) {
        if (accumulator.getUserId() == null) {
            accumulator.setUserId(value.getUser());
        }
        try {
            if (filterFunction == null || filterFunction.filter(value)) {
                Pair<Double, PointRule> pointScore = value.getPointScore(badgeRule.getPointsId());
                if (pointScore != null) {
                    accumulator.setFirstRefEvent(Utils.firstNonNull(accumulator.getFirstRefEvent(), value.getRefEvent()));
                    accumulator.setValue(accumulator.getValue() + pointScore.getValue0());
                    accumulator.setLastRefEvent(value.getRefEvent());
                }
            }
        } catch (Exception e) {
            return accumulator;
        }
        return accumulator;
    }

    @Override
    public BadgeEvent getResult(BadgeAggregator accumulator) {
        Map<String, Object> vars = new HashMap<>();
        vars.put(badgeRule.getAggregator(), accumulator.getValue());
        vars.put("value", accumulator.getValue());
        try {
            if (Utils.evaluateCondition(
                    Utils.compileExpression(badgeRule.getCondition()), vars)) {
                //System.out.println(accumulator.userId + " = " + accumulator.value);
                BadgeEvent badgeEvent = new BadgeEvent(accumulator.getUserId(),
                        badgeRule.getBadge(), badgeRule,
                        Arrays.asList(accumulator.getFirstRefEvent(), accumulator.getLastRefEvent()),
                        accumulator.getLastRefEvent());
                badgeEvent.setTag(String.valueOf(accumulator.getValue()));
                return badgeEvent;
            } else {
                List<? extends Badge> subBadges = badgeRule.getSubBadges();
                if (subBadges != null) {
                    for (Badge badge : subBadges) {
                        if (badge instanceof BadgeFromEvents.ConditionalSubBadge
                                && Utils.evaluateCondition(((BadgeFromEvents.ConditionalSubBadge) badge).getCondition(), vars)) {
                            System.out.println(accumulator.getUserId() + " = " + accumulator.getValue());
                            BadgeEvent badgeEvent = new BadgeEvent(accumulator.getUserId(),
                                    badge, badgeRule,
                                    Arrays.asList(accumulator.getFirstRefEvent(), accumulator.getLastRefEvent()),
                                    accumulator.getLastRefEvent());
                            badgeEvent.setTag(String.valueOf(accumulator.getValue()));
                            return badgeEvent;
                        }
                    }
                }
                return new BadgeEvent(-1L, null, null, null, null);
            }
        } catch (IOException e) {
            // @TODO handle error
            return new BadgeEvent(-1L, null, null, null, null);
        }
    }

    @Override
    public BadgeAggregator merge(BadgeAggregator a, BadgeAggregator b) {
        BadgeAggregator aggregator = new BadgeAggregator();
        aggregator.setUserId(a.getUserId());
        aggregator.setValue(a.getValue() + b.getValue());
        aggregator.setLastRefEvent(
                a.getLastRefEvent().getTimestamp() > b.getLastRefEvent().getTimestamp() ? a.getLastRefEvent() : b.getLastRefEvent());
        aggregator.setFirstRefEvent(
                a.getFirstRefEvent().getTimestamp() > b.getFirstRefEvent().getTimestamp() ? b.getFirstRefEvent() : a.getFirstRefEvent());
        return aggregator;
    }
}

