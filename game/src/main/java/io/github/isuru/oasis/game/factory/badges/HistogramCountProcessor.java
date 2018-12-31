package io.github.isuru.oasis.game.factory.badges;

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
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

class HistogramCountProcessor<E extends Event, W extends Window> extends ProcessWindowFunction<E, BadgeEvent, Long, W> {

    private final ValueStateDescriptor<Long> currStreakDesc;
    private final ValueStateDescriptor<Long> maxAchDesc;
    private MapState<String, Integer> countMap;
    private ValueState<Long> currentStreak;
    private ValueState<Long> maxAchieved;

    private BadgeFromEvents badge;
    private Map<Long, Badge> streakBadges = new HashMap<>();
    private List<Long> streaks = new LinkedList<>();
    private Function<Long, String> timeConverter;

    HistogramCountProcessor(BadgeFromEvents badgeRule, Function<Long, String> timeConverter) {
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

        Predicate<LocalDate> holidayPredicate = createHolidayPredictor();
        String timeKey = timeConverter.apply(context.window().maxTimestamp());

        Iterator<E> it = elements.iterator();
        int count = 0;
        Event lastE = null;
        while (it.hasNext()) {
            lastE = it.next();
            count++;
        }

        if (count > 0) {
            countMap.put(timeKey, count);

            // if current date is a holiday and should count only for business days,
            // then we ignore the today count.
            if (Utils.isDurationBusinessDaysOnly(badge.getDuration())) {
                LocalDate currDate = LocalDate.parse(timeKey);
                if (holidayPredicate.test(currDate)) {
                    return;
                }
            }

            int streakLength = HistogramCounter.processContinuous(timeKey, countMap, holidayPredicate);

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

    void initDefaultState() throws IOException {
        if (Objects.equals(currentStreak.value(), currStreakDesc.getDefaultValue())) {
            currentStreak.update(0L);
        }
        if (Objects.equals(maxAchieved.value(), maxAchDesc.getDefaultValue())) {
            maxAchieved.update(0L);
        }
    }

    Predicate<LocalDate> createHolidayPredictor() {
        return Utils.isDurationBusinessDaysOnly(badge.getDuration())
                ? DefaultHolidayPredictor.INSTANCE : AllDaysPredictor.INSTANCE;
    }

    void clearCurrentStreak() throws IOException {
        currentStreak.update(0L);
    }

    @Override
    public void open(Configuration parameters) {
        MapStateDescriptor<String, Integer> countMapDesc = new MapStateDescriptor<>(
                String.format("badge-%d-histogram", badge.getBadge().getId()), String.class, Integer.class);

        countMap = getRuntimeContext().getMapState(countMapDesc);
        currentStreak = getRuntimeContext().getState(currStreakDesc);
        maxAchieved = getRuntimeContext().getState(maxAchDesc);
    }

    Function<Long, String> getTimeConverter() {
        return timeConverter;
    }

    BadgeFromEvents getBadge() {
        return badge;
    }

    MapState<String, Integer> getCountMap() {
        return countMap;
    }

    ValueState<Long> getCurrentStreak() {
        return currentStreak;
    }

    ValueState<Long> getMaxAchieved() {
        return maxAchieved;
    }

    Map<Long, Badge> getStreakBadges() {
        return streakBadges;
    }

    List<Long> getStreaks() {
        return streaks;
    }

    /**
     * Returns as holiday true if the given date is a Saturday or Sunday.
     */
    private static class DefaultHolidayPredictor implements Predicate<LocalDate> {

        private static final DefaultHolidayPredictor INSTANCE = new DefaultHolidayPredictor();

        private DefaultHolidayPredictor() {}

        @Override
        public boolean test(LocalDate localDate) {
            return localDate.getDayOfWeek() == DayOfWeek.SATURDAY || localDate.getDayOfWeek() == DayOfWeek.SUNDAY;
        }
    }

    /**
     * No holiday predictor. Every day will be counted.
     */
    private static class AllDaysPredictor implements Predicate<LocalDate> {
        private static final AllDaysPredictor INSTANCE = new AllDaysPredictor();

        private AllDaysPredictor() {}

        @Override
        public boolean test(LocalDate localDate) {
            return false;
        }
    }
}
