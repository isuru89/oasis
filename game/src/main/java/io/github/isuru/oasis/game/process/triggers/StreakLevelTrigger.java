package io.github.isuru.oasis.game.process.triggers;

import io.github.isuru.oasis.model.Event;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.state.ReducingState;
import org.apache.flink.api.common.state.ReducingStateDescriptor;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeutils.base.LongSerializer;
import org.apache.flink.streaming.api.windowing.triggers.Trigger;
import org.apache.flink.streaming.api.windowing.triggers.TriggerResult;
import org.apache.flink.streaming.api.windowing.windows.Window;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author iweerarathna
 */
public class StreakLevelTrigger<E extends Event, W extends Window> extends Trigger<E, W> {
    private static final long serialVersionUID = 1L;

    private Set<Long> levels;
    private FilterFunction<E> filter;
    private boolean fireWhenEventTime = false;

    private final ReducingStateDescriptor<Long> stateDesc =
            new ReducingStateDescriptor<>("streak-levels", new StreakTrigger.Sum(), LongSerializer.INSTANCE);

    private final ValueStateDescriptor<Long> currLevelStateDesc =
            new ValueStateDescriptor<>("streak-curr-level", Long.class);

    public StreakLevelTrigger(List<Long> levelBreaks, FilterFunction<E> filter) {
        this.levels = new LinkedHashSet<>(levelBreaks);
        this.filter = filter;
    }

    public StreakLevelTrigger(List<Long> levelBreaks, FilterFunction<E> filter, boolean fireWhenEventTime) {
        this(levelBreaks, filter);
        this.fireWhenEventTime = fireWhenEventTime;
    }

    @Override
    public TriggerResult onElement(E element, long timestamp, W window, TriggerContext ctx) throws Exception {
        ReducingState<Long> count = ctx.getPartitionedState(stateDesc);
        ValueState<Long> currLevel = ctx.getPartitionedState(currLevelStateDesc);

        if (filter.filter(element)) {
            count.add(1L);
            if (levels.contains(count.get())) {
                Long cLevel = currLevel.value();
                if (cLevel == null) {
                    cLevel = 0L;
                }
                currLevel.update(cLevel + 1);
                return TriggerResult.FIRE;
            }
            return TriggerResult.CONTINUE;
        } else {
            count.clear();
            return TriggerResult.PURGE;
        }
    }

    @Override
    public TriggerResult onProcessingTime(long time, W window, TriggerContext ctx) {
        return TriggerResult.CONTINUE;
    }

    @Override
    public TriggerResult onEventTime(long time, W window, TriggerContext ctx) {
        if (fireWhenEventTime && time == window.maxTimestamp()) {
            ReducingState<Long> count = ctx.getPartitionedState(stateDesc);
            if (count != null) {
                count.clear();
            }
            return TriggerResult.PURGE;
        } else {
            return TriggerResult.CONTINUE;
        }
    }

    @Override
    public void clear(W window, TriggerContext ctx) {
    }
}
