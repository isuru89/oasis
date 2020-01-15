/*
 *
 *  * Licensed to the Apache Software Foundation (ASF) under one
 *  * or more contributor license agreements.  See the NOTICE file
 *  * distributed with this work for additional information
 *  * regarding copyright ownership.  The ASF licenses this file
 *  * to you under the Apache License, Version 2.0 (the
 *  * "License"); you may not use this file except in compliance
 *  * with the License.  You may obtain a copy of the License at
 *  *
 *  *   http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing,
 *  * software distributed under the License is distributed on an
 *  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  * KIND, either express or implied.  See the License for the
 *  * specific language governing permissions and limitations
 *  * under the License.
 *
 */

package io.github.oasis.game.process;

import io.github.oasis.game.utils.Constants;
import io.github.oasis.game.utils.Utils;
import io.github.oasis.model.DefinitionUpdateEvent;
import io.github.oasis.model.DefinitionUpdateType;
import io.github.oasis.model.Event;
import io.github.oasis.model.defs.BaseDef;
import io.github.oasis.model.events.ErrorPointEvent;
import io.github.oasis.model.events.PointEvent;
import io.github.oasis.model.rules.PointRule;
import io.github.oasis.model.rules.Scoring;
import org.apache.flink.streaming.api.functions.co.KeyedBroadcastProcessFunction;
import org.apache.flink.util.Collector;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static io.github.oasis.game.states.DefinitionUpdateState.BROADCAST_DEF_UPDATE_DESCRIPTOR;

public class PointProcessor extends KeyedBroadcastProcessFunction<Long, Event, DefinitionUpdateEvent, PointEvent> {

    @Override
    public void processElement(Event event, ReadOnlyContext ctx, Collector<PointEvent> out) throws Exception {
        Iterable<Map.Entry<Long, BaseDef>> entries = ctx.getBroadcastState(BROADCAST_DEF_UPDATE_DESCRIPTOR).immutableEntries();
        Map<String, Scoring> scoredPoints = new HashMap<>();
        List<PointRule> selfAggregatedRules = new ArrayList<>();
        double totalPoints = 0;
        Map<String, Object> eventVariables = Utils.getCloneOfMap(event.getAllFieldValues());

        for (Map.Entry<Long, BaseDef> entry : entries) {
            PointRule rule = (PointRule) entry.getValue();
            if (rule.isSelfAggregatedRule()) {
                selfAggregatedRules.add(rule);
                continue;
            }

            if (rule.canApplyForEvent(event)) {
                try {
                    Optional<Double> amountResult = executeRuleConditionAndValue(rule, eventVariables);
                    if (amountResult.isPresent()) {
                        double amount = amountResult.get();
                        scoredPoints.put(rule.getName(), Scoring.create(amount, rule));

                        // calculate points for other users other than main user of this event belongs to
                        calculateRedirectedPoints(event, rule, eventVariables, out);
                        totalPoints += amount;
                    }
                } catch (Throwable t) {
                    out.collect(new ErrorPointEvent(t.getMessage(), t, event, rule));
                }
            }
        }

        evaluateSelfRules(event, selfAggregatedRules, scoredPoints, totalPoints, out);

        out.collect(PointEvent.create(event, scoredPoints));
    }

    private void evaluateSelfRules(Event event,
                                   List<PointRule> selfAggregatedRules,
                                   Map<String, Scoring> scoringMap,
                                   double totalPoints,
                                   Collector<PointEvent> out) {
        if (Utils.isNonEmpty(selfAggregatedRules)) {
            Map<String, Object> eventVariables = Utils.getCloneOfMap(event.getAllFieldValues());
            eventVariables.put(Constants.VARIABLE_TOTAL_POINTS, totalPoints);

            selfAggregatedRules.stream()
                    .filter(pointRule -> Utils.eventEquals(event, pointRule.getForEvent()))
                    .forEach(rule -> {
                        try {
                            executeRuleConditionAndValue(rule, eventVariables)
                                    .ifPresent(scoredPoints -> scoringMap.put(
                                            rule.getName(),
                                            Scoring.create(scoredPoints, rule)));

                        } catch (Throwable t) {
                            out.collect(new ErrorPointEvent(t.getMessage(), t, event, rule));
                        }
                    });
        }
    }

    private synchronized void calculateRedirectedPoints(Event event, PointRule rule,
                                                        Map<String, Object> eventVariables,
                                                        Collector<PointEvent> out) {
        if (Utils.isNonEmpty(rule.getAdditionalPoints())) {
            for (PointRule.AdditionalPointReward reward : rule.getAdditionalPoints()) {
                Long userId = event.getUserId(reward.getToUser());
                if (Utils.isValidId(userId)) {
                    Double amount = evaluateAmount(reward.getAmount(), eventVariables);

                    PointEvent pointEvent = new RedirectedPointEvent(event, reward.getToUser());
                    Map<String, Scoring> scoredPoints = new HashMap<>();
                    scoredPoints.put(reward.getName(), Scoring.create(amount, rule));
                    pointEvent.replacePointScoring(scoredPoints);
                    out.collect(pointEvent);
                }
            }
        }
    }

    private Optional<Double> executeRuleConditionAndValue(PointRule rule, Map<String, Object> variables) throws IOException {
        boolean satisfiesCondition = Utils.evaluateCondition(rule.getConditionExpression(), variables);
        if (satisfiesCondition) {
            double p;
            if (Objects.nonNull(rule.getAmountExpression())) {
                p = evaluateAmount(rule.getAmountExpression(), variables);
            } else {
                p = rule.getAmount();
            }
            return Optional.of(p);
        }
        return Optional.empty();
    }

    private static Double evaluateAmount(Serializable expr, Map<String, Object> vars) {
        if (expr instanceof Number) {
            return Utils.toDouble((Number)expr);
        } else {
            Object result = Utils.executeExpression(expr, vars);
            if (result instanceof Number) {
                return Utils.toDouble((Number) result);
            } else {
                return 0.0;
            }
        }
    }

    @Override
    public void processBroadcastElement(DefinitionUpdateEvent updateEvent, Context ctx, Collector<PointEvent> out) throws Exception {
        if (updateEvent.getBaseDef() instanceof PointRule) {
            PointRule rule = (PointRule) updateEvent.getBaseDef();

            if (updateEvent.getType() == DefinitionUpdateType.DELETED) {
                ctx.getBroadcastState(BROADCAST_DEF_UPDATE_DESCRIPTOR).remove(rule.getId());
            } else {
                ctx.getBroadcastState(BROADCAST_DEF_UPDATE_DESCRIPTOR).put(rule.getId(), rule);
            }
        }
    }

    public static class RedirectedPointEvent extends PointEvent implements Serializable {

        private String ownUserField;

        RedirectedPointEvent(Event event, String userFieldId) {
            super(event);
            this.ownUserField = userFieldId;
        }

        @Override
        public long getUser() {
            return getUserId(ownUserField);
        }
    }
}
