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

package io.github.oasis.core.elements.matchers;

import io.github.oasis.core.elements.TimeRangeMatcher;
import io.github.oasis.core.utils.Timestamps;

import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.function.Predicate;

/**
 * Compares timestamp for two time ranges within every day.
 * No date (dd/mm) is considered. Only time portion.
 *
 * @author Isuru Weerarathna
 */
public class DailyTimeRangeMatcher implements TimeRangeMatcher {

    private final LocalTime from;
    private final LocalTime to;
    private final Predicate<LocalTime> checkStrategy;

    private DailyTimeRangeMatcher(LocalTime from, LocalTime to) {
        this.from = from;
        this.to = to;

        if (from.isAfter(to)) {
            checkStrategy = this::checkAcrossDays;
        } else {
            checkStrategy = this::checkSameDay;
        }
    }

    public static DailyTimeRangeMatcher create(String start, String end) {
        return new DailyTimeRangeMatcher(LocalTime.parse(start), LocalTime.parse(end));
    }

    @Override
    public boolean isBetween(long timeMs, String timeZone) {
        ZonedDateTime userTime = Timestamps.getUserSpecificTime(timeMs, timeZone);
        LocalTime currentTime = userTime.toLocalTime();
        return checkStrategy.test(currentTime);
    }

    private boolean checkAcrossDays(LocalTime check) {
        return check.equals(from)
                || check.equals(to)
                || check.isAfter(from)
                || check.isBefore(to);
    }

    private boolean checkSameDay(LocalTime check) {
        return check.equals(from)
                || check.equals(to)
                || (check.isAfter(from) && check.isBefore(to));
    }
}
