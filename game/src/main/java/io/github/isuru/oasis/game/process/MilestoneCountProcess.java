package io.github.isuru.oasis.game.process;

import io.github.isuru.oasis.game.process.triggers.StreakTrigger;
import io.github.isuru.oasis.model.Event;
import io.github.isuru.oasis.model.Milestone;
import io.github.isuru.oasis.model.events.MilestoneEvent;
import io.github.isuru.oasis.model.events.MilestoneStateEvent;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.state.ReducingState;
import org.apache.flink.api.common.state.ReducingStateDescriptor;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeutils.base.LongSerializer;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

import java.util.List;

/**
 * @author iweerarathna
 */
public class MilestoneCountProcess extends KeyedProcessFunction<Long, Event, MilestoneEvent> {

    private List<Long> levels;
    private FilterFunction<Event> filter;
    private Milestone milestone;
    private OutputTag<MilestoneStateEvent> outputTag;

    private boolean atEnd = false;
    private Long nextLevelValue = null;

    private ReducingState<Long> accCount;
    private ValueState<Long> currentLevel;

    public MilestoneCountProcess(List<Long> levels, FilterFunction<Event> filter,
                                 Milestone milestone, OutputTag<MilestoneStateEvent> outputTag) {
        this.levels = levels;
        this.filter = filter;
        this.milestone = milestone;
        this.outputTag = outputTag;
    }

    @Override
    public void processElement(Event value, Context ctx, Collector<MilestoneEvent> out) throws Exception {
        if (filter == null || filter.filter(value)) {
            accCount.add(1L);
            if (levels.contains(accCount.get())) {
                int nextLevel = currentLevel.value().intValue() + 1;
                currentLevel.update(currentLevel.value() + 1);
                out.collect(new MilestoneEvent(value.getUser(), milestone, nextLevel, value));
                nextLevelValue = levels.get(nextLevel);
            }

            if (!atEnd && nextLevelValue == null) {
                if (levels.size() > currentLevel.value()) {
                    nextLevelValue = levels.get(Integer.parseInt(currentLevel.value().toString()));
                } else {
                    nextLevelValue = null;
                    atEnd = true;
                }
            }

            // figure out how to detect next level threshold
            // update count in db
            if (!atEnd) {
                ctx.output(outputTag, new MilestoneStateEvent(value.getUser(),
                        milestone,
                        accCount.get(),
                        nextLevelValue));
            }
        }
    }

    @Override
    public void open(Configuration parameters) {
        ValueStateDescriptor<Long> currLevelStateDesc =
                new ValueStateDescriptor<>(String.format("milestone-%d-curr-level", milestone.getId()),
                        Long.class, 0L);
        ReducingStateDescriptor<Long> allCountStateDesc =
                new ReducingStateDescriptor<>(String.format("milestone-%d-allcount", milestone.getId()),
                        new StreakTrigger.Sum(), LongSerializer.INSTANCE);
        currentLevel = getRuntimeContext().getState(currLevelStateDesc);
        accCount = getRuntimeContext().getReducingState(allCountStateDesc);
    }
}
