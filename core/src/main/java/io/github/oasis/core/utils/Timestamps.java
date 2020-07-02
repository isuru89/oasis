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

import java.time.*;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Isuru Weerarathna
 */
public class Timestamps {

    static final long DAILY = Duration.ofDays(1).toMillis();
    static final long HOURLY = Duration.ofHours(1).toMillis();
    static final long WEEKLY = Duration.ofDays(7).toMillis();

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
}
