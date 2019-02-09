package io.github.isuru.oasis.game.process.windows;

import io.github.isuru.oasis.model.collect.Pair;
import io.github.isuru.oasis.model.utils.TimeUtils;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;

import java.time.ZoneOffset;

/**
 * @author iweerarathna
 */
public class WeeklyEventTimeWindow extends OasisTimeWindowAssigner {

    private static final long DAY_ONE = 86400000L;

    @Override
    protected TimeWindow findWindow(long timestamp, Object element) {
        long start = TimeWindow.getWindowStartWithOffset(timestamp, 0, DAY_ONE);
        Pair<Long, Long> weekRange = TimeUtils.getWeekRange(start, ZoneOffset.UTC);
        return new TimeWindow(weekRange.getValue0(), weekRange.getValue1());
    }
}
