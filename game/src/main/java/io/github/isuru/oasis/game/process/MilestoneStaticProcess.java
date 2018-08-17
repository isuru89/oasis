package io.github.isuru.oasis.game.process;

import io.github.isuru.oasis.game.utils.Utils;
import io.github.isuru.oasis.model.Event;
import io.github.isuru.oasis.model.Milestone;
import io.github.isuru.oasis.model.events.MilestoneEvent;
import io.github.isuru.oasis.model.events.MilestoneStateEvent;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

/**
 * @author iweerarathna
 */
public class MilestoneStaticProcess extends KeyedProcessFunction<Long, Event, MilestoneEvent> {

    private Milestone milestone;
    private FilterFunction<Event> filter;
    private Serializable expression;
    private OutputTag<MilestoneStateEvent> outputTag;
    private List<Milestone.Level> orderedLevels;

    private ValueState<Boolean> started;
    private ValueState<Integer> currentLevel;
    private ValueState<Double> previousValue;

    @Override
    public void processElement(Event value, Context ctx, Collector<MilestoneEvent> out) throws Exception {
        if (filter == null || filter.filter(value)) {
            boolean hasStarted = started.value();
            int currLevel = currentLevel.value();
            double currValue = Double.parseDouble(
                    Utils.executeExpression(expression, value.getAllFieldValues()).toString());
            double prevValue = previousValue.value();

            if (!hasStarted) {
                currLevel = findLevel(currValue);

                currentLevel.update(currLevel);
                previousValue.update(currValue);
                started.update(Boolean.TRUE);
                sendMilestoneStateEvent(ctx, value, currValue, currLevel);
                out.collect(new MilestoneEvent(value.getUser(), milestone, currentLevel.value(), value));
                return;
            }

            Milestone.Level nextLevel;
            if (prevValue < currLevel) { // increment
                nextLevel = milestone.getLevel(currLevel + 1);
            } else if (currLevel < prevValue) { // decrement
                nextLevel = milestone.getLevel(currLevel - 1);
            } else {
                return;
            }

            if (nextLevel == null) {
                return;
            }

            int level = findLevel(currValue);
            if (currLevel != level) {
                currentLevel.update(level);
                out.collect(new MilestoneEvent(value.getUser(), milestone, level, value));
            }
            previousValue.update(currValue);
            sendMilestoneStateEvent(ctx, value, currValue, level);
        }
    }

    private void sendMilestoneStateEvent(Context ctx, Event event, double currValue, int currLevel) throws IOException {
        Milestone.Level level = milestone.getLevel(currLevel + 1);
        Double nextVal = null;
        if (level != null) {
            nextVal = level.getNumber().doubleValue();
        }
        ctx.output(outputTag, new MilestoneStateEvent(event.getUser(), milestone, currValue, nextVal));
    }

    private int findLevel(double currValue) throws IOException {
        if (currValue <= orderedLevels.get(0).getNumber().doubleValue()) {
            // less than least level
            return 0;
        }

        Milestone.Level level = orderedLevels.get(0);
        for (int i = 1; i < orderedLevels.size(); i++) {
            if (level.getNumber().doubleValue() < currValue &&
                    currValue <= orderedLevels.get(i).getNumber().doubleValue()) {
                // in between this range
                return level.getLevel();
            }
            level = orderedLevels.get(i);
        }
        return orderedLevels.size();
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        ValueStateDescriptor<Integer> currLevelStateDesc =
                new ValueStateDescriptor<>(
                        String.format("milestone-static-%d-curr-level", milestone.getId()),
                        Integer.class);
        ValueStateDescriptor<Boolean> startedStateDesc =
                new ValueStateDescriptor<>(
                        String.format("milestone-static-%d-started", milestone.getId()),
                        Boolean.class);
        ValueStateDescriptor<Double> prevValueStateDesc =
                new ValueStateDescriptor<>(
                        String.format("milestone-static-%d-prev-value", milestone.getId()),
                        Double.class);

        currentLevel = getRuntimeContext().getState(currLevelStateDesc);
        if (currentLevel.value() == currLevelStateDesc.getDefaultValue()) {
            currentLevel.update(milestone.getStartingLevel().intValue());
        }
        started = getRuntimeContext().getState(startedStateDesc);
        if (started.value() == startedStateDesc.getDefaultValue()) {
            started.update(Boolean.FALSE);
        }
        previousValue = getRuntimeContext().getState(prevValueStateDesc);
        if (previousValue.value() == prevValueStateDesc.getDefaultValue()) {
            previousValue.update(0.0);
        }
    }
}
