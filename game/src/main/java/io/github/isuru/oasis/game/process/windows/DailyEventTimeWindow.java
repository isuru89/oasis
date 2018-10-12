package io.github.isuru.oasis.game.process.windows;

import io.github.isuru.oasis.model.collect.Pair;
import io.github.isuru.oasis.model.utils.TimeUtils;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;

import java.time.ZoneId;

public class DailyEventTimeWindow extends OasisTimeWindowAssigner {
    @Override
    protected TimeWindow findWindow(long timestamp, Object element) {
        Pair<Long, Long> dayRange = TimeUtils.getDayRange(timestamp, ZoneId.systemDefault());
        return new TimeWindow(dayRange.getValue0(), dayRange.getValue1());
    }
}
