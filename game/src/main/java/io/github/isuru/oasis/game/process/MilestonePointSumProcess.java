package io.github.isuru.oasis.game.process;

import io.github.isuru.oasis.game.utils.Utils;
import io.github.isuru.oasis.model.Milestone;
import io.github.isuru.oasis.model.events.MilestoneEvent;
import io.github.isuru.oasis.model.events.MilestoneStateEvent;
import io.github.isuru.oasis.model.events.PointEvent;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeutils.base.DoubleSerializer;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * @author iweerarathna
 */
public class MilestonePointSumProcess extends KeyedProcessFunction<Long, PointEvent, MilestoneEvent> {

    private final ValueStateDescriptor<Integer> currLevelStateDesc;
    private final ValueStateDescriptor<Double> stateDesc;
    private final ValueStateDescriptor<Double> accNegSumDesc;

    private List<Double> levels;
    private Milestone milestone;
    private OutputTag<MilestoneStateEvent> outputTag;

    private boolean atEnd = false;
    private Double nextLevelValue = null;

    private ValueState<Double> accSum;
    private ValueState<Double> accNegSum;
    private ValueState<Integer> currentLevel;

    public MilestonePointSumProcess(List<Double> levels, Milestone milestone,
                                    OutputTag<MilestoneStateEvent> outputTag) {
        this.levels = levels;
        this.milestone = milestone;
        this.outputTag = outputTag;

        currLevelStateDesc =
                new ValueStateDescriptor<>(String.format("milestone-psd-%d-curr-level", milestone.getId()),
                        Integer.class);
        stateDesc =
                new ValueStateDescriptor<>(String.format("milestone-psd-%d-sum", milestone.getId()),
                        DoubleSerializer.INSTANCE);
        accNegSumDesc = new ValueStateDescriptor<>(
                String.format("milestone-psd-%d-negsum", milestone.getId()),
                DoubleSerializer.INSTANCE);
    }

    @Override
    public void processElement(PointEvent value, Context ctx, Collector<MilestoneEvent> out) throws Exception {
        double acc;
        if (Utils.isNullOrEmpty(milestone.getPointIds())) {
            acc = value.getTotalScore();
        } else {
            acc = 0;
            for (String pid : milestone.getPointIds()) {
                if (value.containsPoint(pid)) {
                    acc += value.getPointScore(pid).getValue0();
                }
            }
        }

        initDefaultState();

        if (milestone.isOnlyPositive() && acc < 0) {
            accNegSum.update(accNegSum.value() + acc);
            ctx.output(outputTag, new MilestoneStateEvent(value.getUser(), milestone, accNegSum.value()));
            return;
        }

        Integer currLevel = currentLevel.value();
        if (currLevel < levels.size()) {
            double margin = levels.get(currLevel);
            double currSum = accSum.value();
            if (currSum < margin && margin <= currSum + acc) {
                // level changed
                int nextLevel = currLevel + 1;
                currentLevel.update(nextLevel);
                out.collect(new MilestoneEvent(value.getUser(), milestone, nextLevel, value));

                double total = currSum + acc;
                if (nextLevel < levels.size()) {
                    margin = levels.get(nextLevel);

                    // check for subsequent levels
                    while (nextLevel < levels.size() && margin < total) {
                        margin = levels.get(nextLevel);
                        if (margin < total) {
                            nextLevel = nextLevel + 1;
                            currentLevel.update(nextLevel);
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
                    milestone, accSum.value(), nextLevelValue));
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
}
