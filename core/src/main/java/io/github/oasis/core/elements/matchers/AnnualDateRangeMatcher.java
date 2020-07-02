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

import java.time.MonthDay;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Compares timestamp for two date ranges in every year.
 * No time (hh:mm) is considered. Only month and date.
 *
 * @author Isuru Weerarathna
 */
public class AnnualDateRangeMatcher implements TimeRangeMatcher {

    private final MonthDay from;
    private final MonthDay to;
    private final Predicate<MonthDay> checkStrategy;

    AnnualDateRangeMatcher(MonthDay from, MonthDay to) {
        this.from = from;
        this.to = to;

        if (from.isAfter(to)) {
            checkStrategy = this::checkAmongAcrossYears;
        } else {
            checkStrategy = this::checkWithinSameYear;
        }
    }

    public static AnnualDateRangeMatcher create(String from, String to) {
        Optional<MonthDay> startMdRef = Timestamps.toMonthDay(from);
        if (startMdRef.isEmpty()) {
            throw new IllegalArgumentException("Cannot parse from date for range! ['" + from + "']");
        }
        MonthDay beginning = startMdRef.get();
        Optional<MonthDay> toMd = Timestamps.toMonthDay(to);
        return new AnnualDateRangeMatcher(beginning, toMd.orElse(beginning));
    }

    @Override
    public boolean isBetween(long timeMs, String timeZone) {
        ZonedDateTime userTime = Timestamps.getUserSpecificTime(timeMs, timeZone);
        MonthDay currentTime = MonthDay.from(userTime);
        return checkStrategy.test(currentTime);
    }

    private boolean checkAmongAcrossYears(MonthDay check) {
        return check.equals(from)
                || check.equals(to)
                || check.isAfter(from)
                || check.isBefore(to);
    }

    private boolean checkWithinSameYear(MonthDay check) {
        return check.equals(from)
                || check.equals(to)
                || (check.isAfter(from) && check.isBefore(to));
    }

}
