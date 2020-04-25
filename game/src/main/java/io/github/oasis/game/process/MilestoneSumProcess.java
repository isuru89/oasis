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

import io.github.oasis.game.states.MilestoneSumState;
import io.github.oasis.game.utils.Utils;
import io.github.oasis.model.Event;
import io.github.oasis.model.Milestone;
import io.github.oasis.model.events.MilestoneEvent;
import io.github.oasis.model.events.MilestoneStateEvent;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

import java.io.IOException;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;

/**
 * @author iweerarathna
 */
public class MilestoneSumProcess extends KeyedProcessFunction<Long, Event, MilestoneEvent> {
    private final ValueStateDescriptor<MilestoneSumState> milestoneValueStateDescriptor;

    private Serializable expression;
    private Milestone milestone;
    private OutputTag<MilestoneStateEvent> outputTag;

    private ValueState<MilestoneSumState> milestoneState;

    public MilestoneSumProcess(Serializable expression, Milestone milestone,
                               OutputTag<MilestoneStateEvent> outputTag) {
        this.expression = expression;
        this.milestone = milestone;
        this.outputTag = outputTag;

        milestoneValueStateDescriptor = new ValueStateDescriptor<>(
                OasisIDs.getStateId(milestone),
                Types.GENERIC(MilestoneSumState.class)
        );
    }

    @Override
    public void processElement(Event value, Context ctx, Collector<MilestoneEvent> out) throws Exception {
        MilestoneSumState sumState = initDefaultState();

        if (sumState.isAllLevelsReached()) {
            return;
        }

        double result = getEvaluatedValue(value);

        if (milestone.isOnlyPositive() && result < 0) {
            milestoneState.update(sumState.accumulateNegative(result));
            ctx.output(outputTag, MilestoneStateEvent.lossEvent(value, milestone, sumState.getTotalNegativeSum()));
            return;
        }

        int beforeLevel = sumState.getCurrentLevel();
        sumState.accumulate(result);

        Optional<Milestone.Level> currentLevelOpt = milestone.findLevelForValue(sumState.getTotalSum());
        if (currentLevelOpt.isPresent() && sumState.hasLevelChanged(currentLevelOpt.get())) {
            sumState.updateLevelTo(currentLevelOpt.get(), milestone);
            IntStream.rangeClosed(beforeLevel + 1, sumState.getCurrentLevel())
                    .forEach(level -> out.collect(MilestoneEvent.reachedEvent(value, milestone, level)));
        }

        ctx.output(outputTag, MilestoneStateEvent.summing(
                value,
                milestone,
                sumState.getTotalSum(),
                sumState.getNextLevelTarget(milestone),
                sumState.getCurrentLevelTarget(milestone)));
    }

    private double getEvaluatedValue(Event event) {
        return Double.parseDouble(Utils.executeExpression(expression, event.getAllFieldValues()).toString());
    }

    private MilestoneSumState initDefaultState() throws IOException {
        if (Objects.isNull(milestoneState.value())) {
            milestoneState.update(MilestoneSumState.from(milestone));
        }
        return milestoneState.value();
    }

    @Override
    public void open(Configuration parameters) {
        milestoneState = getRuntimeContext().getState(milestoneValueStateDescriptor);
    }

}
