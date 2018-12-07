package io.github.isuru.oasis.injector.scheduler;

import io.github.isuru.oasis.model.collect.Pair;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class WeeklyScheduler extends BaseScheduler  {

    @Override
    protected Pair<Long, Long> deriveTimeRange(long ms, ZoneId zoneId) {
        ZonedDateTime startT = ZonedDateTime
                .ofInstant(Instant.ofEpochMilli(ms), ZoneId.systemDefault())
                .minusDays(1)
                .with(DayOfWeek.MONDAY);
        long rangeStart = startT.toInstant().toEpochMilli();
        long rangeEnd = startT.plusDays(7).toInstant().toEpochMilli();
        return Pair.of(rangeStart, rangeEnd);
    }

}
