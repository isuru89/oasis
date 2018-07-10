package io.github.isuru.oasis.process.windows;

import org.apache.flink.streaming.api.windowing.windows.TimeWindow;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * @author iweerarathna
 */
public class WeeklyEventTimeWindow extends OasisTimeWindowAssigner {

    private static final long DAY_ONE = 86400000L;

    @Override
    protected TimeWindow findWindow(long timestamp, Object element) {
        long start = TimeWindow.getWindowStartWithOffset(timestamp, 0, DAY_ONE);
        ZonedDateTime startT = ZonedDateTime.ofInstant(Instant.ofEpochMilli(start), ZoneId.systemDefault())
                .with(DayOfWeek.MONDAY);
//        System.out.println("Weekly Window created " + startT.toInstant() + " to "
//                + startT.plusDays(7).toInstant());
        return new TimeWindow(startT.toInstant().toEpochMilli(),
                startT.plusDays(7).toInstant().toEpochMilli());
    }
}
