package io.github.isuru.oasis.game.process;

import io.github.isuru.oasis.model.Event;
import io.github.isuru.oasis.model.Milestone;
import io.github.isuru.oasis.model.events.MilestoneEvent;
import io.github.isuru.oasis.model.events.MilestoneStateEvent;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeutils.base.DoubleSerializer;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;
import org.mvel2.MVEL;

import java.io.Serializable;
import java.util.List;

/**
 * @author iweerarathna
 */
public class MilestoneSumDoubleProcess extends KeyedProcessFunction<Long, Event, MilestoneEvent> {

    private List<Double> levels;
    private FilterFunction<Event> filter;
    private Serializable expression;
    private Milestone milestone;
    private OutputTag<MilestoneStateEvent> outputTag;

    private boolean atEnd = false;
    private Double nextLevelValue = null;

    private ValueState<Double> accSum;
    private ValueState<Double> accNegSum;
    private ValueState<Integer> currentLevel;

    public MilestoneSumDoubleProcess(List<Double> levels, FilterFunction<Event> filter,
                                     Serializable expression, Milestone milestone,
                                     OutputTag<MilestoneStateEvent> outputTag) {
        this.levels = levels;
        this.filter = filter;
        this.expression = expression;
        this.milestone = milestone;
        this.outputTag = outputTag;
    }

    @Override
    public void processElement(Event value, Context ctx, Collector<MilestoneEvent> out) throws Exception {
        if (filter == null || filter.filter(value)) {
            Integer currLevel = currentLevel.value();
            if (currLevel < levels.size()) {
                double margin = levels.get(currLevel);
                double acc = asDouble(expression, value);

                if (milestone.isOnlyPositive() && acc < 0) {
                    accNegSum.update(accNegSum.value() + acc);
                    ctx.output(outputTag, new MilestoneStateEvent(value.getUser(), milestone, accNegSum.value()));
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
                ctx.output(outputTag, new MilestoneStateEvent(value.getUser(), milestone, accSum.value(), nextLevelValue));
            }
        }
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
        ValueStateDescriptor<Double> accNegSumDesc = new ValueStateDescriptor<>(
                String.format("milestone-sd-%d-negsum", milestone.getId()),
                DoubleSerializer.INSTANCE, 0.0);

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
