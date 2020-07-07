/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
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

import java.time.DayOfWeek;
import java.time.ZonedDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Checks whether given timestamp falls into any specified day of weeks.
 *
 * @author Isuru Weerarathna
 */
public class WeeklyTimeMatcher implements TimeRangeMatcher {

    private final EnumSet<DayOfWeek> weekDays;

    public WeeklyTimeMatcher(List<DayOfWeek> weekDays) {
        this.weekDays = EnumSet.copyOf(weekDays);
    }

    @Override
    public boolean isBetween(long timeMs, String timeZone) {
        ZonedDateTime userTime = Timestamps.getUserSpecificTime(timeMs, timeZone);
        return weekDays.contains(userTime.getDayOfWeek());
    }

    public static WeeklyTimeMatcher create(String weekdays) {
        List<DayOfWeek> days = Stream.of(weekdays.split(","))
                .map(day -> DayOfWeek.valueOf(day.trim().toUpperCase()))
                .collect(Collectors.toList());
        return new WeeklyTimeMatcher(days);
    }


}
