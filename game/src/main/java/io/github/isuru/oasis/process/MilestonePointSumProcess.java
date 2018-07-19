package io.github.isuru.oasis.process;

import io.github.isuru.oasis.model.Milestone;
import io.github.isuru.oasis.model.events.MilestoneEvent;
import io.github.isuru.oasis.model.events.MilestoneStateEvent;
import io.github.isuru.oasis.model.events.PointEvent;
import io.github.isuru.oasis.utils.Utils;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeutils.base.DoubleSerializer;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

import java.util.List;

/**
 * @author iweerarathna
 */
public class MilestonePointSumProcess extends KeyedProcessFunction<Long, PointEvent, MilestoneEvent> {

    private List<Double> levels;
    private Milestone milestone;
    private OutputTag<MilestoneStateEvent> outputTag;

    private transient ValueState<Double> accSum;
    private transient ValueState<Integer> currentLevel;

    public MilestonePointSumProcess(List<Double> levels, Milestone milestone,
                                    OutputTag<MilestoneStateEvent> outputTag) {
        this.levels = levels;
        this.milestone = milestone;
        this.outputTag = outputTag;
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

            } else {
                accSum.update(currSum + acc);
            }
        }

        // update sum in db
        ctx.output(outputTag, new MilestoneStateEvent(value.getUser(), milestone, accSum.value()));
    }

    @Override
    public void open(Configuration parameters) {
        ValueStateDescriptor<Integer> currLevelStateDesc =
                new ValueStateDescriptor<>(String.format("milestone-sd-%d-curr-level", milestone.getId()),
                        Integer.class,
                        0);
        ValueStateDescriptor<Double> stateDesc =
                new ValueStateDescriptor<>(String.format("milestone-sd-%d-sum", milestone.getId()),
                        DoubleSerializer.INSTANCE,
                        0.0);
        currentLevel = getRuntimeContext().getState(currLevelStateDesc);
        accSum = getRuntimeContext().getState(stateDesc);
    }
}
