package io.github.isuru.oasis.process;

import io.github.isuru.oasis.model.Event;
import io.github.isuru.oasis.model.Milestone;
import io.github.isuru.oasis.model.events.MilestoneEvent;
import io.github.isuru.oasis.model.events.MilestoneStateEvent;
import io.github.isuru.oasis.utils.Utils;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

import java.io.Serializable;
import java.util.List;

/**
 * @author iweerarathna
 */
public class MilestoneSumProcess extends KeyedProcessFunction<Long, Event, MilestoneEvent> {

    private List<Long> levels;
    private FilterFunction<Event> filter;
    private Serializable expression;
    private Milestone milestone;
    private OutputTag<MilestoneStateEvent> outputTag;

    private ValueState<Long> accSum;
    private ValueState<Integer> currentLevel;

    public MilestoneSumProcess(List<Long> levels, FilterFunction<Event> filter,
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
                long acc = Long.parseLong(Utils.executeExpression(expression, value.getAllFieldValues()).toString());
                long margin = levels.get(currLevel);
                long currSum = accSum.value();
                if (currSum < margin && margin <= currSum + acc) {
                    // level changed
                    int nextLevel = currLevel + 1;
                    currentLevel.update(nextLevel);
                    out.collect(new MilestoneEvent(value.getUser(), milestone, nextLevel, value));

                    long total = currSum + acc;
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

            // update sum in db;
            ctx.output(outputTag, new MilestoneStateEvent(value.getUser(), milestone, accSum.value()));
        }
    }

    @Override
    public void open(Configuration parameters) {
        ValueStateDescriptor<Integer> currLevelStateDesc =
                new ValueStateDescriptor<>(String.format("milestone-%d-curr-level", milestone.getId()),
                        Integer.class, 0);
        ValueStateDescriptor<Long> stateDesc =
                new ValueStateDescriptor<>(String.format("milestone-%d-sum", milestone.getId()),
                        Long.class, 0L);
        currentLevel = getRuntimeContext().getState(currLevelStateDesc);
        accSum = getRuntimeContext().getState(stateDesc);
    }

}
