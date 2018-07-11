package io.github.isuru.oasis.factory.badges;

import io.github.isuru.oasis.model.Badge;
import io.github.isuru.oasis.model.events.BadgeEvent;
import io.github.isuru.oasis.model.events.PointEvent;
import io.github.isuru.oasis.model.rules.BadgeFromPoints;
import io.github.isuru.oasis.model.rules.BadgeRule;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.Window;
import org.apache.flink.util.Collector;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;

/**
 * @author iweerarathna
 */
class StreakBadgeHandler<W extends Window> extends ProcessWindowFunction<PointEvent, BadgeEvent, Long, W>
        implements Serializable {

    private BadgeFromPoints rule;

    StreakBadgeHandler(BadgeRule rule) {
        this.rule = (BadgeFromPoints) rule;
    }

    @Override
    public void process(Long userId, Context context, Iterable<PointEvent> elements, Collector<BadgeEvent> out) {
        Iterator<PointEvent> iterator = elements.iterator();
        PointEvent first = null, last = null;
        int count = 0;
        while (iterator.hasNext()) {
            PointEvent next = iterator.next();
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