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

package io.github.oasis.game.process;

import io.github.oasis.model.Event;
import io.github.oasis.model.Milestone;
import io.github.oasis.model.events.MilestoneEvent;
import io.github.oasis.model.events.MilestoneStateEvent;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeutils.base.DoubleSerializer;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;
import org.mvel2.MVEL;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * @author iweerarathna
 */
public class MilestoneSumDoubleProcess extends KeyedProcessFunction<Long, Event, MilestoneEvent> {

    private final ValueStateDescriptor<Integer> currLevelStateDesc;
    private final ValueStateDescriptor<Double> stateDesc;
    private final ValueStateDescriptor<Double> accNegSumDesc;

    private List<Double> levels;
    private Serializable expression;
    private Milestone milestone;
    private OutputTag<MilestoneStateEvent> outputTag;

    private boolean atEnd = false;
    private Double nextLevelValue = null;

    private ValueState<Double> accSum;
    private ValueState<Double> accNegSum;
    private ValueState<Integer> currentLevel;

    public MilestoneSumDoubleProcess(List<Double> levels,
                                     Serializable expression, Milestone milestone,
                                     OutputTag<MilestoneStateEvent> outputTag) {
        this.levels = levels;
        this.expression = expression;
        this.milestone = milestone;
        this.outputTag = outputTag;

        currLevelStateDesc =
                new ValueStateDescriptor<>(String.format("milestone-sd-%d-curr-level", milestone.getId()),
                        Integer.class);
        stateDesc =
                new ValueStateDescriptor<>(String.format("milestone-sd-%d-sum", milestone.getId()),
                        DoubleSerializer.INSTANCE);
        accNegSumDesc = new ValueStateDescriptor<>(
                String.format("milestone-sd-%d-negsum", milestone.getId()),
                DoubleSerializer.INSTANCE);
    }

    @Override
    public void processElement(Event value, Context ctx, Collector<MilestoneEvent> out) throws Exception {
        initDefaultState();

        Integer currLevel = currentLevel.value();
        double currLevelMargin;
        if (currLevel < levels.size()) {
            double margin = levels.get(currLevel);
            currLevelMargin = margin;
            double acc = asDouble(expression, value);

            if (milestone.isOnlyPositive() && acc < 0) {
                accNegSum.update(accNegSum.value() + acc);
                ctx.output(outputTag, new MilestoneStateEvent(value.getUser(), value.getGameId(), milestone, accNegSum.value()));
                return;
            }

            double currSum = accSum.value();
            if (currSum < margin && margin <= currSum + acc) {
                // level changed
                int nextLevel = currLevel + 1;
                currentLevel.update(nextLevel);
                out.collect(new MilestoneEvent(value.getUser(), milestone, nextLevel, value));

                double total = currSum + acc;
                if (nextLevel < levels.size()) {
                    margin = levels.get(nextLevel);
                    currLevelMargin = margin;

                    // check for subsequent levels
                    while (nextLevel < levels.size() && margin < total) {
                        margin = levels.get(nextLevel);
                        if (margin < total) {
                            nextLevel = nextLevel + 1;
                            currentLevel.update(nextLevel);
                            currLevelMargin = margin;
                            out.collect(new MilestoneEvent(value.getUser(), milestone, nextLevel, value));
                        }
                    }
                }
                accSum.update(total);
                nextLevelValue = levels.size() > nextLevel ? levels.get(nextLevel) : null;
                atEnd = levels.size() >= nextLevel;

            } else {
                accSum.update(currSum + acc);
            }
        } else {
            currLevelMargin = levels.get(levels.size() - 1); // max level
        }

        if (!atEnd && nextLevelValue == null) {
            if (levels.size() > currentLevel.value()) {
                nextLevelValue = levels.get(Integer.parseInt(currentLevel.value().toString()));
            } else {
                nextLevelValue = null;
                atEnd = true;
            }
        }

        // update sum in db
        if (!atEnd) {
            ctx.output(outputTag, new MilestoneStateEvent(value.getUser(),
                    value.getGameId(),
                    milestone,
                    accSum.value(),
                    nextLevelValue,
                    currLevelMargin));
        }
    }

    private void initDefaultState() throws IOException {
        if (Objects.equals(accNegSum.value(), accNegSumDesc.getDefaultValue())) {
            accNegSum.update(0.0);
        }
        if (Objects.equals(currentLevel.value(), currLevelStateDesc.getDefaultValue())) {
            currentLevel.update(0);
        }
        if (Objects.equals(accSum.value(), stateDesc.getDefaultValue())) {
            accSum.update(0.0);
        }
    }

    @Override
    public void open(Configuration parameters) {
        accNegSum = getRuntimeContext().getState(accNegSumDesc);
        currentLevel = getRuntimeContext().getState(currLevelStateDesc);
        accSum = getRuntimeContext().getState(stateDesc);
    }

    private static double asDouble(Serializable expr, Event event) {
        Object o = MVEL.executeExpression(expr, event.getAllFieldValues());
        if (o instanceof Number) {
            return ((Number) o).doubleValue();
        }
        return 0.0;
    }

}
