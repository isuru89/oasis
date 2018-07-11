package io.github.isuru.oasis.factory.badges;

import io.github.isuru.oasis.model.Event;
import io.github.isuru.oasis.model.Badge;
import io.github.isuru.oasis.model.events.BadgeEvent;
import io.github.isuru.oasis.model.rules.BadgeFromEvents;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.Window;
import org.apache.flink.util.Collector;

import java.util.Arrays;
import java.util.Iterator;

/**
 * @author iweerarathna
 */
class StreakBadgeHandlerEvents<E extends Event, W extends Window>
        extends ProcessWindowFunction<E, BadgeEvent, Long, W> {

    private BadgeFromEvents rule;

    StreakBadgeHandlerEvents(BadgeFromEvents rule) {
        this.rule = rule;
    }

    @Override
    public void process(Long userId, Context context, Iterable<E> elements, Collector<BadgeEvent> out) {
        Iterator<? extends Event> iterator = elements.iterator();
        Event first = null, last = null;
        int count = 0;
        while (iterator.hasNext()) {
            Event next = iterator.next();
            if (first == null) first = next;
            last = next;
            count++;
        }

        Badge subBadge = rule.getSubBadge(count);
        if (subBadge != null) {
            out.collect(new BadgeEvent(userId, subBadge, rule, Arrays.asList(first, last), last));
        }

    }
}
