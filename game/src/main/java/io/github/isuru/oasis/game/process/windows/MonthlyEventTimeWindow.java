package io.github.isuru.oasis.game.process.windows;

import io.github.isuru.oasis.model.collect.Pair;
import io.github.isuru.oasis.model.utils.TimeUtils;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;

import java.time.ZoneId;

/**
 * @author iweerarathna
 */
public class MonthlyEventTimeWindow extends OasisTimeWindowAssigner {

    private static final ZoneId UTC_ZONE = ZoneId.of("UTC");

    @Override
    protected TimeWindow findWindow(long timestamp, Object element) {
        Pair<Long, Long> monthRange = TimeUtils.getMonthRange(timestamp, UTC_ZONE);
        return new TimeWindow(monthRange.getValue0(), monthRange.getValue1());
    }
}
