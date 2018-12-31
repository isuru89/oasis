package io.github.isuru.oasis.game.factory.badges;

import io.github.isuru.oasis.game.utils.HistogramCounter;
import io.github.isuru.oasis.game.utils.Utils;
import io.github.isuru.oasis.model.Event;
import io.github.isuru.oasis.model.events.BadgeEvent;
import io.github.isuru.oasis.model.rules.BadgeFromEvents;
import org.apache.flink.api.common.state.MapState;
import org.apache.flink.streaming.api.windowing.windows.Window;
import org.apache.flink.util.Collector;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

public class HistogramSumProcessor<E extends Event, W extends Window> extends HistogramCountProcessor<E, W> {

    HistogramSumProcessor(BadgeFromEvents badgeRule, Function<Long, String> timeConverter) {
        super(badgeRule, timeConverter);
    }

    @Override
    public void process(Long userId, Context context, Iterable<E> elements, Collector<BadgeEvent> out) throws Exception {
        initDefaultState();

        BadgeFromEvents badgeRule = getBadge();

        Predicate<LocalDate> holidayPredicate = createHolidayPredictor();
        String timeKey = getTimeConverter().apply(context.window().maxTimestamp());

        // ignore when the current date is holiday and specified so to ignore.
        if (Utils.isDurationBusinessDaysOnly(badgeRule.getDuration())) {
            LocalDate currDate = LocalDate.parse(timeKey);
            if (holidayPredicate.test(currDate)) {
                return;
            }
        }

        Iterator<E> it = elements.iterator();
        double sum = 0;
        Event lastE = null;

        while (it.hasNext()) {
            Event tmp = it.next();

            Object o = Utils.executeExpression(badgeRule.getContinuousAggregator(), tmp.getAllFieldValues());
            if (o instanceof Number) {
                sum += ((Number) o).doubleValue();
            }
            lastE = tmp;
        }

        MapState<String, Integer> countMap = getCountMap();

        if (isConditionFulfil(sum, badgeRule)) {
            countMap.put(timeKey, 1);

            int streakLength = HistogramCounter.processContinuous(timeKey, countMap, holidayPredicate);
            if (streakLength < 2) {
                countMap.clear();
                countMap.put(timeKey, 1);
                clearCurrentStreak();

            } else {
                long cStreak = 0;
                for (long t : getStreaks()) {
                    if (streakLength >= t) {
                        cStreak = t;
                    } else {
                        break;
                    }
                }

                if (getCurrentStreak().value() < cStreak
                        && (badgeRule.getMaxBadges() != 1 || cStreak > getMaxAchieved().value())) {
                    // creating a badge
                    BadgeEvent badgeEvent = new BadgeEvent(userId,
                            getStreakBadges().get(cStreak),
                            badgeRule,
                            Collections.singletonList(lastE),
                            lastE);
                    out.collect(badgeEvent);
                    getCurrentStreak().update(cStreak);
                    getMaxAchieved().update(Math.max(getMaxAchieved().value(), cStreak));
                }
            }

        } else {
            // clear map because consecutive is dropped.
            HistogramCounter.clearLessThan(timeKey, countMap);
        }
    }

    private boolean isConditionFulfil(double sum, BadgeFromEvents badgeRule) throws IOException {
        Map<String, Object> vars = new HashMap<>();
        vars.put("sum", sum);
        vars.put("value", sum);
        return Utils.evaluateCondition(badgeRule.getContinuousCondition(), vars);
    }
}
