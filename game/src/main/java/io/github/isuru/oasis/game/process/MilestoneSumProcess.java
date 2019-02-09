package io.github.isuru.oasis.game.process;

import io.github.isuru.oasis.game.utils.Utils;
import io.github.isuru.oasis.model.Event;
import io.github.isuru.oasis.model.Milestone;
import io.github.isuru.oasis.model.events.MilestoneEvent;
import io.github.isuru.oasis.model.events.MilestoneStateEvent;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * @author iweerarathna
 */
public class MilestoneSumProcess extends KeyedProcessFunction<Long, Event, MilestoneEvent> {

    private final ValueStateDescriptor<Integer> currLevelStateDesc;
    private final ValueStateDescriptor<Long> stateDesc;
    private final ValueStateDescriptor<Long> accNegSumDesc;

    private List<Long> levels;
    private Serializable expression;
    private Milestone milestone;
    private OutputTag<MilestoneStateEvent> outputTag;

    private boolean atEnd = false;
    private Long nextLevelValue = null;

    private ValueState<Long> accSum;
    private ValueState<Long> accNegSum;
    private ValueState<Integer> currentLevel;

    public MilestoneSumProcess(List<Long> levels,
                               Serializable expression, Milestone milestone,
                               OutputTag<MilestoneStateEvent> outputTag) {
        this.levels = levels;
        this.expression = expression;
        this.milestone = milestone;
        this.outputTag = outputTag;

        currLevelStateDesc =
                new ValueStateDescriptor<>(String.format("milestone-%d-curr-level", milestone.getId()),
                        Integer.class);
        stateDesc =
                new ValueStateDescriptor<>(String.format("milestone-%d-sum", milestone.getId()),
                        Long.class);
        accNegSumDesc = new ValueStateDescriptor<>(
                String.format("milestone-%d-negsum", milestone.getId()), Long.class);
    }

    @Override
    public void processElement(Event value, Context ctx, Collector<MilestoneEvent> out) throws Exception {
        initDefaultState();

        Integer currLevel = currentLevel.value();
        Long currLevelMargin;
        if (currLevel < levels.size()) {
            long acc = Long.parseLong(Utils.executeExpression(expression, value.getAllFieldValues()).toString());

            if (milestone.isOnlyPositive() && acc < 0) {
                accNegSum.update(accNegSum.value() + acc);
                ctx.output(outputTag, new MilestoneStateEvent(value.getUser(), value.getGameId(), milestone, accNegSum.value()));
                return;
            }

            long margin = levels.get(currLevel);
            currLevelMargin = margin;
            long currSum = accSum.value();
            if (currSum < margin && margin <= currSum + acc) {
                // level changed
                int nextLevel = currLevel + 1;
                currentLevel.update(nextLevel);
                out.collect(new MilestoneEvent(value.getUser(), milestone, nextLevel, value));

                long total = currSum + acc;
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
                atEnd = levels.size() <= nextLevel;

            } else {
                accSum.update(currSum + acc);
            }
        } else {
            currLevelMargin = levels.get(levels.size() - 1);
        }

        if (!atEnd && nextLevelValue == null) {
            if (levels.size() > currentLevel.value()) {
                nextLevelValue = levels.get(Integer.parseInt(currentLevel.value().toString()));
            } else {
                nextLevelValue = null;
                atEnd = true;
            }
        }

        if (!atEnd) {
            // update sum in db;
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
            accNegSum.update(0L);
        }
        if (Objects.equals(currentLevel.value(), currLevelStateDesc.getDefaultValue())) {
            currentLevel.update(0);
        }
        if (Objects.equals(accSum.value(), stateDesc.getDefaultValue())) {
            accSum.update(0L);
        }
    }

    @Override
    public void open(Configuration parameters) {
        accNegSum = getRuntimeContext().getState(accNegSumDesc);
        currentLevel = getRuntimeContext().getState(currLevelStateDesc);
        accSum = getRuntimeContext().getState(stateDesc);
    }

}
