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

import io.github.oasis.game.factory.PointsOperator;
import io.github.oasis.game.utils.Constants;
import io.github.oasis.game.utils.Utils;
import io.github.oasis.model.DefinitionUpdateEvent;
import io.github.oasis.model.DefinitionUpdateType;
import io.github.oasis.model.Event;
import io.github.oasis.model.collect.Pair;
import io.github.oasis.model.defs.BaseDef;
import io.github.oasis.model.events.ErrorPointEvent;
import io.github.oasis.model.events.PointEvent;
import io.github.oasis.model.rules.PointRule;
import lombok.Builder;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.common.state.MapState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.streaming.api.functions.co.KeyedBroadcastProcessFunction;
import org.apache.flink.util.Collector;
import org.mvel2.MVEL;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

public class PointProcessor extends KeyedBroadcastProcessFunction<Long, Event, DefinitionUpdateEvent, PointEvent> {

    public static final MapStateDescriptor<Long, BaseDef> BROADCAST_POINT_RULES_DESCRIPTOR =
            new MapStateDescriptor<>(
                    "oasis.states.broadcast.points",
                    Types.LONG,
                    Types.GENERIC(BaseDef.class)
            );

    @Override
    public void processElement(Event event, ReadOnlyContext ctx, Collector<PointEvent> out) throws Exception {
        Iterable<Map.Entry<Long, BaseDef>> entries = ctx.getBroadcastState(BROADCAST_POINT_RULES_DESCRIPTOR).immutableEntries();
        Map<String, Pair<Double, PointRule>> scoredPoints = new HashMap<>();
        List<PointRule> selfAggregatedRules = new ArrayList<>();
        double totalPoints = 0;

        for (Map.Entry<Long, BaseDef> entry : entries) {
            PointRule rule = (PointRule) entry.getValue();
            if (rule.isSelfAggregatedRule()) {
                selfAggregatedRules.add(rule);
                continue;
            }

            if (rule.canApplyForEvent(event)) {
                try {
                    Optional<Double> amountResult = executeRuleConditionAndValue(rule, event.getAllFieldValues());
                    if (amountResult.isPresent()) {
                        double d = amountResult.get();
                        scoredPoints.put(rule.getName(), Pair.of(d, rule));

                        // calculate points for other users other than main user of this event belongs to
                        calculateRedirectedPoints(event, rule, out);
                        totalPoints += d;
                    }
                } catch (Throwable t) {
                    out.collect(new ErrorPointEvent(t.getMessage(), t, event, rule));
                }
            }
        }

        evalSelfRules(event, selfAggregatedRules, scoredPoints, totalPoints, out);

        PointEvent pe = PointEvent.create(event, scoredPoints);
        out.collect(pe);
    }

    private void evalSelfRules(Event event,
                               List<PointRule> selfAggregatedRules,
                               Map<String, Pair<Double, PointRule>> points,
                               double totalPoints,
                               Collector<PointEvent> out) {
        if (Utils.isNonEmpty(selfAggregatedRules)) {
            Map<String, Object> vars = new HashMap<>(event.getAllFieldValues());
            vars.put(Constants.VARIABLE_TOTAL_POINTS, totalPoints);

            selfAggregatedRules.stream()
                    .filter(pointRule -> Utils.eventEquals(event, pointRule.getForEvent()))
                    .forEach(rule -> {
                        try {
                            executeRuleConditionAndValue(rule, vars)
                                    .ifPresent(p -> points.put(rule.getName(), Pair.of(p, rule)));

                        } catch (Throwable t) {
                            out.collect(new ErrorPointEvent(t.getMessage(), t, event, rule));
                        }
                    });
        }
    }

    private synchronized void calculateRedirectedPoints(Event event, PointRule rule, Collector<PointEvent> out) {
        if (Utils.isNonEmpty(rule.getAdditionalPoints())) {
            for (PointRule.AdditionalPointReward reward : rule.getAdditionalPoints()) {
                Long userId = event.getUserId(reward.getToUser());
                if (Utils.isValidId(userId)) {
                    Double amount = evaluateAmount(reward.getAmount(), event.getAllFieldValues());

                    PointEvent pointEvent = new RedirectedPointEvent(event, reward.getToUser());
                    Map<String, Pair<Double, PointRule>> scoredPoints = new HashMap<>();
                    scoredPoints.put(reward.getName(), Pair.of(amount, rule));
                    pointEvent.setPointEvents(scoredPoints);
                    out.collect(pointEvent);
                }
            }
        }
    }

    private Optional<Double> executeRuleConditionAndValue(PointRule rule, Map<String, Object> variables) throws IOException {
        boolean status = Utils.evaluateCondition(rule.getConditionExpression(), variables);
        if (status) {
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
                ctx.getBroadcastState(BROADCAST_POINT_RULES_DESCRIPTOR).remove(rule.getId());
            } else {
                ctx.getBroadcastState(BROADCAST_POINT_RULES_DESCRIPTOR).put(rule.getId(), rule);
            }
        }
    }

    public static class RedirectedPointEvent extends PointEvent {

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
