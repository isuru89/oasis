package io.github.isuru.oasis.game.process.triggers;

import io.github.isuru.oasis.model.Event;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeutils.base.LongSerializer;
import org.apache.flink.streaming.api.windowing.triggers.Trigger;
import org.apache.flink.streaming.api.windowing.triggers.TriggerResult;
import org.apache.flink.streaming.api.windowing.windows.Window;

import java.io.IOException;

/**
 * @author iweerarathna
 */
public class FrequencyTimeTrigger<E extends Event, W extends Window> extends Trigger<E, W> {
    private static final long serialVersionUID = 1L;

    private final long maxCount;
    private final long timeoutMs;

    private final ValueStateDescriptor<Long> countDesc = new ValueStateDescriptor<>("count", LongSerializer.INSTANCE, 0L);
    private final ValueStateDescriptor<Long> deadlineDesc = new ValueStateDescriptor<>("deadline", LongSerializer.INSTANCE, Long.MAX_VALUE);

    public FrequencyTimeTrigger(long maxCount, long timeoutMs) {
        this.maxCount = maxCount;
        this.timeoutMs = timeoutMs;
    }

    @Override
    public TriggerResult onElement(E element, long timestamp, W window, Trigger.TriggerContext ctx) throws IOException {
        final ValueState<Long> deadline = ctx.getPartitionedState(deadlineDesc);
        final ValueState<Long> count = ctx.getPartitionedState(countDesc);

        final long currentDeadline = deadline.value();
        final long currentTimeMs = System.currentTimeMillis();

        final long newCount = count.value() + 1;

        if (currentTimeMs >= currentDeadline || newCount >= maxCount) {
            return fire(deadline, count);
        }

        if (currentDeadline == deadlineDesc.getDefaultValue()) {
            final long nextDeadline = currentTimeMs + timeoutMs;
            deadline.update(nextDeadline);
            ctx.registerEventTimeTimer(nextDeadline);
        }

        count.update(newCount);

        return TriggerResult.CONTINUE;
    }

    @Override
    public TriggerResult onEventTime(long time, W window, Trigger.TriggerContext ctx) throws Exception {
        final ValueState<Long> deadline = ctx.getPartitionedState(deadlineDesc);
        // fire only if the deadline hasn't changed since registering this timer
        if (deadline.value() == time) {
            return fire(deadline, ctx.getPartitionedState(countDesc));
        }
        return TriggerResult.CONTINUE;
    }

    @Override
    public TriggerResult onProcessingTime(long time, W window, Trigger.TriggerContext ctx) throws Exception {
        final ValueState<Long> deadline = ctx.getPartitionedState(deadlineDesc);
        // fire only if the deadline hasn't changed since registering this timer
        if (deadline.value() == time) {
            return fire(deadline, ctx.getPartitionedState(countDesc));
        }
        return TriggerResult.CONTINUE;
    }

    @Override
    public void clear(W window, TriggerContext ctx) throws Exception {
        final ValueState<Long> deadline = ctx.getPartitionedState(deadlineDesc);
        final long deadlineValue = deadline.value();
        if (deadlineValue != deadlineDesc.getDefaultValue()) {
            ctx.deleteProcessingTimeTimer(deadlineValue);
        }
        deadline.clear();
    }

    private TriggerResult fire(ValueState<Long> deadline, ValueState<Long> count) throws IOException {
        deadline.update(Long.MAX_VALUE);
        count.update(0L);
        return TriggerResult.FIRE;
    }

    @Override
    public String toString() {
        return "CountWithTimeoutTrigger(" + maxCount + "," + timeoutMs + ")";
    }
}
