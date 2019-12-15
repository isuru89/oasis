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

import io.github.oasis.game.states.MilestoneState;
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
import java.util.Objects;
import java.util.Optional;

/**
 * @author iweerarathna
 */
public class MilestoneCountProcess extends KeyedProcessFunction<Long, Event, MilestoneEvent> {

    private final ValueStateDescriptor<MilestoneState> milestoneStateValueStateDescriptor;

    private Milestone milestone;
    private OutputTag<MilestoneStateEvent> outputTag;

    private ValueState<MilestoneState> milestoneStateValueState;

    public MilestoneCountProcess(Milestone milestone, OutputTag<MilestoneStateEvent> outputTag) {
        this.milestone = milestone;
        this.outputTag = outputTag;

        milestoneStateValueStateDescriptor = new ValueStateDescriptor<>(
                "oasis.milestone.count.process", Types.GENERIC(MilestoneState.class));
    }

    @Override
    public void processElement(Event value, Context ctx, Collector<MilestoneEvent> out) throws Exception {
        MilestoneState milestoneState = initDefaultState();
        if (milestoneState.isAllLevelsReached()) {
            return;
        }

        MilestoneState state = milestoneState.incrementCountAndGet();
        Optional<Milestone.Level> currentLevel = milestone.findLevelForValue(state.getTotalCount());
        if (currentLevel.isPresent()) {
            Milestone.Level level = currentLevel.get();
            if (state.hasLevelChanged(level)) {
                state.updateLevelTo(level, milestone);
                out.collect(MilestoneEvent.reachedEvent(value, milestone, state.getCurrentLevel()));
            }
        }

        milestoneStateValueState.update(state);

        if (!milestoneState.isAllLevelsReached()) {
            ctx.output(outputTag, MilestoneStateEvent.counting(value,
                    milestone,
                    state.getTotalCount(),
                    state.getNextLevelTarget(milestone),
                    state.getCurrentLevelTarget(milestone)));
        }
    }

    private MilestoneState initDefaultState() throws IOException {
        if (Objects.isNull(milestoneStateValueState.value())) {
            milestoneStateValueState.update(MilestoneState.from(milestone));
        }
        return milestoneStateValueState.value();
    }

    @Override
    public void open(Configuration parameters) {
        milestoneStateValueState = getRuntimeContext().getState(milestoneStateValueStateDescriptor);
    }
}
