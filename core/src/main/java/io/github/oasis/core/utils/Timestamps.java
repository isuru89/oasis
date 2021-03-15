/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.github.oasis.core.utils;

import io.github.oasis.core.elements.spec.TimeUnitDef;
import io.github.oasis.core.model.TimeScope;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.MonthDay;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.IsoFields;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Isuru Weerarathna
 */
public class Timestamps {

    static final long DAILY = Duration.ofDays(1).toMillis();
    static final long HOURLY = Duration.ofHours(1).toMillis();
    static final long WEEKLY = Duration.ofDays(7).toMillis();

    private static final Pattern TIMESTAMP_PATTERN = Pattern.compile("([0-9]+)\\s*([a-zA-Z]+)");
    private static final int NO_DAYS_PER_WEEK = 7;

    public static Optional<MonthDay> toMonthDay(String text) {
        if (Objects.nonNull(text)) {
            if (text.startsWith("--")) {
                return Optional.of(MonthDay.parse(text));
            } else {
                return Optional.of(MonthDay.parse("--" + text));
            }
        }
        return Optional.empty();
    }

    public static ZonedDateTime getUserSpecificTime(long ts, int userTzOffsetInSeconds) {
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(ts), ZoneOffset.ofTotalSeconds(userTzOffsetInSeconds));
    }

    public static ZonedDateTime getUserSpecificTime(long ts, String userTimezone) {
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(ts), ZoneId.of(userTimezone));
    }

    public static int getYear(long ts, int userTzOffsetInSeconds) {
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(ts), ZoneOffset.ofTotalSeconds(userTzOffsetInSeconds)).getYear();
    }

    public static long parseTimeUnit(TimeUnitDef def) {
        return Duration.of(def.getDuration(), toChronoUnit(def.getUnit())).toMillis();
    }

    private static ChronoUnit toChronoUnit(String unit) {
        String unitLower = unit.toLowerCase();
        if (unitLower.startsWith("d")) {
            return ChronoUnit.DAYS;
        } else if (unitLower.startsWith("min")) {
            return ChronoUnit.MINUTES;
        } else if (unitLower.startsWith("h")) {
            return ChronoUnit.HOURS;
        } else if (unitLower.startsWith("s")) {
            return ChronoUnit.SECONDS;
        } else if (unitLower.startsWith("w")) {
            return ChronoUnit.WEEKS;
        } else if (unitLower.startsWith("mo")) {
            return ChronoUnit.MONTHS;
        } else if (unitLower.startsWith("mi") || unitLower.startsWith("ms")) {
            return ChronoUnit.MILLIS;
        }
        throw new IllegalArgumentException("Unknown time unit [" + unit + "]!");
    }

    /**
     * Parses given time unit string to Milliseconds.
     * Eligible, time unit strings can be, (daily, weekly, hourly).
     *
     * If given input is null or empty, will return 0.
     * @param timeUnitStr time unit string.
     * @return time unit in milliseconds.
     * @throws IllegalArgumentException when given input time unit is unknown.
     */
    public static long parseTimeUnit(String timeUnitStr) {
        if (Texts.isEmpty(timeUnitStr)) {
            return 0;
        }

        Matcher matcher = TIMESTAMP_PATTERN.matcher(timeUnitStr);
        if (matcher.matches()) {
            String amount = matcher.group(1);
            String type = matcher.group(2).toLowerCase();

            int amountTotal = Texts.isEmpty(amount) ? 1 : Integer.parseInt(amount);
            if (type.startsWith("d")) {
                return Duration.ofDays(amountTotal).toMillis();
            } else if (type.startsWith("h")) {
                return Duration.ofHours(amountTotal).toMillis();
            } else if (type.startsWith("w")) {
                return Duration.ofDays(NO_DAYS_PER_WEEK * amountTotal).toMillis();
            } else if (type.startsWith("m")) {
                return Duration.ofMinutes(amountTotal).toMillis();
            }
            throw new IllegalArgumentException("Unknown time unit string! [" + timeUnitStr + "]");
        }

        String timeunit = timeUnitStr.toLowerCase();
        if (timeunit.startsWith("d")) {
            return DAILY;
        } else if (timeunit.startsWith("w")) {
            return WEEKLY;
        } else if (timeunit.startsWith("h")) {
            return HOURLY;
        }
        throw new IllegalArgumentException("Unknown time unit string! [" + timeUnitStr + "]");
    }

    public static String formatKey(LocalDate date, TimeScope scope) {
        switch (scope) {
            case DAILY: return String.format(TimeOffset.DAY_PATTERN, date.getYear(), date.getMonth().getValue(), date.getDayOfMonth());
            case WEEKLY: return String.format(TimeOffset.WEEK_PATTERN, date.getYear(), date.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR));
            case YEARLY: return "Y" + date.getYear();
            case MONTHLY: return String.format(TimeOffset.MONTH_PATTERN, date.getYear(), date.getMonth().getValue());
            case QUARTERLY: return String.format(TimeOffset.QUARTER_PATTERN, date.getYear(), date.get(IsoFields.QUARTER_OF_YEAR));
            default: return null;
        }
    }


    public static List<String> timeUnitsWithinRange(LocalDate startTime, LocalDate endTime, TimeScope timeScope) {
        if (timeScope == TimeScope.DAILY) {
            return daysBetween(startTime, endTime);
        } else if (timeScope == TimeScope.WEEKLY) {
            return weeksBetween(startTime, endTime);
        } else if (timeScope == TimeScope.MONTHLY) {
            return monthsBetween(startTime, endTime);
        } else if (timeScope == TimeScope.YEARLY) {
            return yearsBetween(startTime, endTime);
        } else if (timeScope == TimeScope.QUARTERLY) {
            return quartersBetween(startTime, endTime);
        }
        throw new IllegalArgumentException("Unknown timescope type!");
    }

    private static List<String> daysBetween(LocalDate startDate, LocalDate endDate) {
        return Stream.iterate(startDate, date -> date.plusDays(1))
                .limit(ChronoUnit.DAYS.between(startDate, endDate) + 1)
                .map(date -> String.format(TimeOffset.DAY_PATTERN, date.getYear(), date.getMonth().getValue(), date.getDayOfMonth()))
                .collect(Collectors.toList());
    }

    private static List<String> monthsBetween(LocalDate startDate, LocalDate endDate) {
        List<String> months = new ArrayList<>();
        YearMonth startRange = YearMonth.from(startDate);
        YearMonth endRange = YearMonth.from(endDate).plusMonths(1);
        for (YearMonth tmp = startRange; tmp.isBefore(endRange); tmp = tmp.plusMonths(1)) {
            months.add(String.format(TimeOffset.MONTH_PATTERN, tmp.getYear(), tmp.getMonth().getValue()));
        }
        return months;
    }

    private static List<String> yearsBetween(LocalDate startDate, LocalDate endDate) {
        return Stream.iterate(startDate, date -> date.plusYears(1))
                .limit(endDate.getYear() - startDate.getYear() + 1)
                .map(date -> "Y" + date.getYear())
                .collect(Collectors.toList());
    }

    private static List<String> weeksBetween(LocalDate startDate, LocalDate endDate) {
        return Stream.iterate(startDate, date -> date.plusWeeks(1))
                .limit(ChronoUnit.WEEKS.between(startDate, endDate) + 1)
                .map(date -> String.format(TimeOffset.WEEK_PATTERN, date.getYear(), date.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)))
                .collect(Collectors.toList());
    }

    private static List<String> quartersBetween(LocalDate startDate, LocalDate endDate) {
        Set<String> quarters = new HashSet<>();
        YearMonth startRange = YearMonth.from(startDate);
        YearMonth endRange = YearMonth.from(endDate);
        for (YearMonth tmp = startRange; tmp.isBefore(endRange) || tmp.equals(endRange); tmp = tmp.plusMonths(1)) {
            quarters.add(String.format(TimeOffset.QUARTER_PATTERN, tmp.getYear(), tmp.get(IsoFields.QUARTER_OF_YEAR)));
        }
        return new ArrayList<>(quarters);
    }

}
