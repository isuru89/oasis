package io.github.isuru.oasis.game.process;

import io.github.isuru.oasis.game.utils.HistogramCounter;
import io.github.isuru.oasis.game.utils.Utils;
import io.github.isuru.oasis.model.Badge;
import io.github.isuru.oasis.model.Event;
import io.github.isuru.oasis.model.events.BadgeEvent;
import io.github.isuru.oasis.model.rules.BadgeFromEvents;
import org.apache.flink.api.common.state.MapState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.Window;
import org.apache.flink.util.Collector;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;

public class CountProcessor<E extends Event, W extends Window> extends ProcessWindowFunction<E, BadgeEvent, Long, W> {

    private final ValueStateDescriptor<Long> currStreakDesc;
    private final ValueStateDescriptor<Long> maxAchDesc;
    private MapState<String, Integer> countMap;
    private ValueState<Long> currentStreak;
    private ValueState<Long> maxAchieved;

    private BadgeFromEvents badge;
    private Map<Long, Badge> streakBadges = new HashMap<>();
    private List<Long> streaks = new LinkedList<>();
    private Function<Long, String> timeConverter;

    public CountProcessor(BadgeFromEvents badgeRule, Function<Long, String> timeConverter) {
        this.badge = badgeRule;
        this.timeConverter = timeConverter;
        currStreakDesc = new ValueStateDescriptor<>(
                String.format("badge-%d-curr-streak", badge.getBadge().getId()), Long.class);
        maxAchDesc = new ValueStateDescriptor<>(
                String.format("badge-%d-max-achieve", badge.getBadge().getId()), Long.class);

        Time time = Utils.fromStr(badgeRule.getDuration());
        streaks.add(time.getSize());
        streakBadges.put(time.getSize(), badgeRule.getBadge());
        if (badgeRule.getSubBadges() != null) {
            for (Badge subBadge : badgeRule.getSubBadges()) {
                if (subBadge instanceof BadgeFromEvents.ContinuousSubBadge) {
                    long sz = Utils.fromStr(((BadgeFromEvents.ContinuousSubBadge) subBadge).getWithin()).getSize();
                    streaks.add(sz);
                    streakBadges.put(sz, subBadge);
                }
            }
        }
    }

    @Override
    public void process(Long userId, Context context, Iterable<E> elements, Collector<BadgeEvent> out) throws Exception {
        initDefaultState();

        String timeKey = timeConverter.apply(context.window().maxTimestamp());

        Iterator<E> it = elements.iterator();
        int count = 0;
        Event lastE = null;
        while (it.hasNext()) {
            lastE = it.next();
            count++;
        }

        // @TODO handle holidays
        if (count > 0) {
            countMap.put(timeKey, count);
            int streakLength = HistogramCounter.processContinuous(timeKey, countMap);
            if (streakLength < 2) {
                countMap.clear();
                countMap.put(timeKey, count);
                clearCurrentStreak();
            } else {
                long cStreak = 0;
                for (long t : streaks) {
                    if (streakLength >= t) {
                        cStreak = t;
                    } else {
                        break;
                    }
                }

                if (currentStreak.value() < cStreak
                        && (badge.getMaxBadges() != 1 || cStreak > maxAchieved.value())) {
                    // creating a badge
                    BadgeEvent badgeEvent = new BadgeEvent(userId,
                            streakBadges.get(cStreak),
                            badge,
                            Collections.singletonList(lastE),
                            lastE);
                    out.collect(badgeEvent);
                    currentStreak.update(cStreak);
                    maxAchieved.update(Math.max(maxAchieved.value(), cStreak));
                }
            }

        } else {
            HistogramCounter.clearLessThan(timeKey, countMap);
        }
    }

    private void initDefaultState() throws IOException {
        if (Objects.equals(currentStreak.value(), currStreakDesc.getDefaultValue())) {
            currentStreak.update(0L);
        }
        if (Objects.equals(maxAchieved.value(), maxAchDesc.getDefaultValue())) {
            maxAchieved.update(0L);
        }
    }

    private void clearCurrentStreak() throws IOException {
        currentStreak.update(0L);
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        MapStateDescriptor<String, Integer> countMapDesc = new MapStateDescriptor<>(
                String.format("badge-%d-histogram", badge.getBadge().getId()), String.class, Integer.class);

        countMap = getRuntimeContext().getMapState(countMapDesc);
        currentStreak = getRuntimeContext().getState(currStreakDesc);
        maxAchieved = getRuntimeContext().getState(maxAchDesc);
    }
}
