package io.github.isuru.oasis.injector.scheduler;

import io.github.isuru.oasis.model.collect.Pair;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;

public class DailyScheduler extends BaseScheduler {

    @Override
    protected Pair<Long, Long> deriveTimeRange(long ms, ZoneId zoneId) {
        LocalDate localDate = Instant.ofEpochMilli(ms).atZone(zoneId).toLocalDate();
        return Pair.of(localDate.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli(),
                localDate.plusDays(1L).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli());
    }
}
