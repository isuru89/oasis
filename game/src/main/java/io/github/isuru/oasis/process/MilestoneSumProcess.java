package io.github.isuru.oasis.process;

import io.github.isuru.oasis.model.Event;
import io.github.isuru.oasis.utils.Utils;
import io.github.isuru.oasis.model.Milestone;
import io.github.isuru.oasis.model.events.MilestoneEvent;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

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

    private ValueState<Long> accSum;
    private ValueState<Integer> currentLevel;

    public MilestoneSumProcess(List<Long> levels, FilterFunction<Event> filter,
                               Serializable expression, Milestone milestone) {
        this.levels = levels;
        this.filter = filter;
        this.expression = expression;
        this.milestone = milestone;
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

            // @TODO update sum in db
        }
    }

    @Override
    public void open(Configuration parameters) {
        ValueStateDescriptor<Integer> currLevelStateDesc =
                new ValueStateDescriptor<>(String.format("milestone-%s-curr-level", milestone.getId()),
                        Integer.class, 0);
        ValueStateDescriptor<Long> stateDesc =
                new ValueStateDescriptor<>(String.format("milestone-%s-sum", milestone.getId()),
                        Long.class, 0L);
        currentLevel = getRuntimeContext().getState(currLevelStateDesc);
        accSum = getRuntimeContext().getState(stateDesc);
    }

}
