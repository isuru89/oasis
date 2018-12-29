package io.github.isuru.oasis.model.utils;

import io.github.isuru.oasis.model.collect.Pair;

import java.time.*;

/**
 * @author iweerarathna
 */
public class TimeUtils {

    public static Pair<Long, Long> getDayRange(long epoch, ZoneId zoneId) {
        LocalDate localDate = Instant.ofEpochMilli(epoch).atZone(zoneId).toLocalDate();
        return Pair.of(localDate.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli(),
                localDate.plusDays(1L).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli());
    }

    public static Pair<Long, Long> getWeekRange(long epoch, ZoneId zoneId) {
        ZonedDateTime startT = ZonedDateTime
                .ofInstant(Instant.ofEpochMilli(epoch), zoneId)
                .with(DayOfWeek.MONDAY);
        return Pair.of(startT.toInstant().toEpochMilli(), startT.plusDays(7).toInstant().toEpochMilli());
    }

    public static Pair<Long, Long> getMonthRange(long epoch, ZoneId zoneId) {
        ZonedDateTime zonedDateTime = Instant.ofEpochMilli(epoch).atZone(zoneId);
        YearMonth from = YearMonth.of(zonedDateTime.getYear(), zonedDateTime.getMonth());
        long start = from.atDay(1).atStartOfDay()
                .toInstant(ZoneOffset.UTC).toEpochMilli();
        long end = from.atEndOfMonth().plusDays(1).atStartOfDay()
                .toInstant(ZoneOffset.UTC).toEpochMilli();
        return Pair.of(start, end);
    }

}
