package io.github.isuru.oasis.injector.scheduler;

import io.github.isuru.oasis.model.collect.Pair;

import java.time.*;

public class MonthlyScheduler extends BaseScheduler {

    @Override
    protected Pair<Long, Long> deriveTimeRange(long ms, ZoneId zoneId) {
        ZonedDateTime zonedDateTime = Instant.ofEpochMilli(ms).atZone(zoneId);
        YearMonth from = YearMonth.of(zonedDateTime.getYear(), zonedDateTime.getMonth());
        long start = from.atDay(1).atStartOfDay()
                .toInstant(ZoneOffset.UTC).toEpochMilli();
        long end = from.atEndOfMonth().plusDays(1).atStartOfDay()
                .toInstant(ZoneOffset.UTC).toEpochMilli();
        return Pair.of(start, end);
    }
}
