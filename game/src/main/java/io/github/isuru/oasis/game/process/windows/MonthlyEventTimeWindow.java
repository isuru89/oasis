package io.github.isuru.oasis.game.process.windows;

import org.apache.flink.streaming.api.windowing.windows.TimeWindow;

import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/**
 * @author iweerarathna
 */
public class MonthlyEventTimeWindow extends OasisTimeWindowAssigner {

    private static final ZoneId UTC_ZONE = ZoneId.of("UTC");

    @Override
    protected TimeWindow findWindow(long timestamp, Object element) {
        ZonedDateTime zonedDateTime = Instant.ofEpochMilli(timestamp).atZone(UTC_ZONE);
        YearMonth from = YearMonth.of(zonedDateTime.getYear(), zonedDateTime.getMonth());
        long start = from.atDay(1).atStartOfDay()
                .toInstant(ZoneOffset.UTC).toEpochMilli();
        long end = from.atEndOfMonth().plusDays(1).atStartOfDay()
                .toInstant(ZoneOffset.UTC).toEpochMilli();
        return new TimeWindow(start, end);
    }
}
