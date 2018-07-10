package io.github.isuru.oasis.process.triggers;

import io.github.isuru.oasis.Event;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.streaming.api.windowing.triggers.Trigger;
import org.apache.flink.streaming.api.windowing.triggers.TriggerResult;
import org.apache.flink.streaming.api.windowing.windows.Window;

/**
 * @author iweerarathna
 */
public class ConditionalTrigger<E extends Event, W extends Window> extends Trigger<E, W> {

    private final FilterFunction<E> filter;

    public ConditionalTrigger(FilterFunction<E> filter) {
        this.filter = filter;
    }

    @Override
    public TriggerResult onElement(E element, long timestamp, W window, TriggerContext ctx) throws Exception {
        if (filter.filter(element)) {
            return TriggerResult.FIRE_AND_PURGE;
        } else {
            return TriggerResult.PURGE;
        }
    }

    @Override
    public TriggerResult onProcessingTime(long time, W window, TriggerContext ctx) {
        return TriggerResult.CONTINUE;
    }

    @Override
    public TriggerResult onEventTime(long time, W window, TriggerContext ctx) {
        return TriggerResult.CONTINUE;
    }

    @Override
    public void clear(W window, TriggerContext ctx) {

    }
}
