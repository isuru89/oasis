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

package io.github.oasis.core.utils;

import io.github.oasis.core.model.TimeScope;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

/**
 * @author Isuru Weerarathna
 */
public class TimestampsTest {

    @Test
    public void testTimeUnitStr() {
        Assertions.assertEquals(0L, Timestamps.parseTimeUnit((String) null));
        Assertions.assertEquals(0L, Timestamps.parseTimeUnit(""));
        Assertions.assertEquals(0L, Timestamps.parseTimeUnit(" "));

        Assertions.assertEquals(Timestamps.DAILY, Timestamps.parseTimeUnit("daily"));
        Assertions.assertEquals(Timestamps.DAILY, Timestamps.parseTimeUnit("Daily"));
        Assertions.assertEquals(Timestamps.WEEKLY, Timestamps.parseTimeUnit("weekly"));
        Assertions.assertEquals(Timestamps.WEEKLY, Timestamps.parseTimeUnit("Weekly"));
        Assertions.assertEquals(Timestamps.HOURLY, Timestamps.parseTimeUnit("hourly"));
        Assertions.assertEquals(Timestamps.HOURLY, Timestamps.parseTimeUnit("Hourly"));

        Assertions.assertEquals(Duration.ofDays(30).toMillis(), Timestamps.parseTimeUnit("30 days"));
        Assertions.assertEquals(Duration.ofDays(1).toMillis(), Timestamps.parseTimeUnit("1d"));
        Assertions.assertEquals(Duration.ofDays(14).toMillis(), Timestamps.parseTimeUnit("2 weeks"));
        Assertions.assertEquals(Duration.ofHours(5).toMillis(), Timestamps.parseTimeUnit("5hours"));
        Assertions.assertEquals(Duration.ofMinutes(30).toMillis(), Timestamps.parseTimeUnit("30mins"));

        Assertions.assertThrows(IllegalArgumentException.class, () -> Timestamps.parseTimeUnit("abc"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> Timestamps.parseTimeUnit("30sec"));
    }

    @Test
    public void testRangeDays() {
        List<String> dates = Timestamps.timeUnitsWithinRange(LocalDate.parse("2020-06-25"), LocalDate.parse("2020-07-10"), TimeScope.DAILY);
        Assertions.assertEquals(16, dates.size());
        Assertions.assertTrue(dates.contains("D20200625"));
        Assertions.assertTrue(dates.contains("D20200710"));

        // same day
        dates = Timestamps.timeUnitsWithinRange(LocalDate.parse("2020-07-30"), LocalDate.parse("2020-07-30"), TimeScope.DAILY);
        Assertions.assertEquals(1, dates.size());
        Assertions.assertTrue(dates.contains("D20200730"));

        // across years
        dates = Timestamps.timeUnitsWithinRange(LocalDate.parse("2019-12-30"), LocalDate.parse("2020-01-07"), TimeScope.DAILY);
        Assertions.assertEquals(9, dates.size());
        Assertions.assertTrue(dates.contains("D20191230"));
        Assertions.assertTrue(dates.contains("D20200107"));
    }

    @Test
    public void testRangeWeeks() {
        List<String> weeks = Timestamps.timeUnitsWithinRange(LocalDate.parse("2020-05-25"), LocalDate.parse("2020-07-10"), TimeScope.WEEKLY);
        Assertions.assertEquals(7, weeks.size());
        Assertions.assertTrue(weeks.contains("W202022"));
        Assertions.assertTrue(weeks.contains("W202028"));

        // within same week
        weeks = Timestamps.timeUnitsWithinRange(LocalDate.parse("2020-07-07"), LocalDate.parse("2020-07-10"), TimeScope.WEEKLY);
        Assertions.assertEquals(1, weeks.size());
        Assertions.assertTrue(weeks.contains("W202028"));

        // across years
        weeks = Timestamps.timeUnitsWithinRange(LocalDate.parse("2019-12-01"), LocalDate.parse("2020-01-31"), TimeScope.WEEKLY);
        System.out.println(weeks);
        Assertions.assertEquals(9, weeks.size());
        Assertions.assertTrue(weeks.contains("W201948"));
        Assertions.assertTrue(weeks.contains("W202004"));
    }

    @Test
    public void testRangeMonths() {
        List<String> months = Timestamps.timeUnitsWithinRange(LocalDate.parse("2020-05-25"), LocalDate.parse("2020-07-10"), TimeScope.MONTHLY);
        System.out.println(months);
        Assertions.assertEquals(3, months.size());
        Assertions.assertTrue(months.contains("M202005"));
        Assertions.assertTrue(months.contains("M202006"));
        Assertions.assertTrue(months.contains("M202007"));

        // within same month
        months = Timestamps.timeUnitsWithinRange(LocalDate.parse("2020-07-07"), LocalDate.parse("2020-07-10"), TimeScope.MONTHLY);
        Assertions.assertEquals(1, months.size());
        Assertions.assertTrue(months.contains("M202007"));

        // within same month range
        months = Timestamps.timeUnitsWithinRange(LocalDate.parse("2020-07-01"), LocalDate.parse("2020-07-31"), TimeScope.MONTHLY);
        Assertions.assertEquals(1, months.size());
        Assertions.assertTrue(months.contains("M202007"));

        // across years
        months = Timestamps.timeUnitsWithinRange(LocalDate.parse("2019-12-01"), LocalDate.parse("2020-01-31"), TimeScope.MONTHLY);
        Assertions.assertEquals(2, months.size());
        Assertions.assertTrue(months.contains("M201912"));
        Assertions.assertTrue(months.contains("M202001"));
    }

    @Test
    public void testRangeYears() {
        List<String> years = Timestamps.timeUnitsWithinRange(LocalDate.parse("2019-05-25"), LocalDate.parse("2020-07-10"), TimeScope.YEARLY);
        System.out.println(years);
        Assertions.assertEquals(2, years.size());
        Assertions.assertTrue(years.contains("Y2019"));
        Assertions.assertTrue(years.contains("Y2020"));

        // within same year
        years = Timestamps.timeUnitsWithinRange(LocalDate.parse("2020-07-07"), LocalDate.parse("2020-07-10"), TimeScope.YEARLY);
        Assertions.assertEquals(1, years.size());
        Assertions.assertTrue(years.contains("Y2020"));

        // within same year range
        years = Timestamps.timeUnitsWithinRange(LocalDate.parse("2020-01-01"), LocalDate.parse("2020-12-31"), TimeScope.YEARLY);
        Assertions.assertEquals(1, years.size());
        Assertions.assertTrue(years.contains("Y2020"));

        // across several years
        years = Timestamps.timeUnitsWithinRange(LocalDate.parse("2019-12-01"), LocalDate.parse("2020-01-31"), TimeScope.YEARLY);
        Assertions.assertEquals(2, years.size());
        Assertions.assertTrue(years.contains("Y2019"));
        Assertions.assertTrue(years.contains("Y2020"));
    }

    @Test
    public void testRangeQuarters() {
        List<String> quarters = Timestamps.timeUnitsWithinRange(LocalDate.parse("2020-03-25"), LocalDate.parse("2020-07-10"), TimeScope.QUARTERLY);
        System.out.println(quarters);
        Assertions.assertEquals(3, quarters.size());
        Assertions.assertTrue(quarters.contains("Q202001"));
        Assertions.assertTrue(quarters.contains("Q202002"));
        Assertions.assertTrue(quarters.contains("Q202003"));

        // within same year
        quarters = Timestamps.timeUnitsWithinRange(LocalDate.parse("2020-07-07"), LocalDate.parse("2020-07-10"), TimeScope.QUARTERLY);
        Assertions.assertEquals(1, quarters.size());
        Assertions.assertTrue(quarters.contains("Q202003"));

        // within same quarter range
        quarters = Timestamps.timeUnitsWithinRange(LocalDate.parse("2020-01-01"), LocalDate.parse("2020-03-31"), TimeScope.QUARTERLY);
        Assertions.assertEquals(1, quarters.size());
        Assertions.assertTrue(quarters.contains("Q202001"));

        // across several years
        quarters = Timestamps.timeUnitsWithinRange(LocalDate.parse("2019-12-01"), LocalDate.parse("2020-01-31"), TimeScope.QUARTERLY);
        Assertions.assertEquals(2, quarters.size());
        Assertions.assertTrue(quarters.contains("Q201904"));
        Assertions.assertTrue(quarters.contains("Q202001"));
    }

    @Test
    public void testFormatKey() {
        Assertions.assertEquals("D20200731", Timestamps.formatKey(LocalDate.parse("2020-07-31"), TimeScope.DAILY));
        Assertions.assertEquals("M202007", Timestamps.formatKey(LocalDate.parse("2020-07-31"), TimeScope.MONTHLY));
        Assertions.assertEquals("Q202003", Timestamps.formatKey(LocalDate.parse("2020-07-31"), TimeScope.QUARTERLY));
        Assertions.assertEquals("W202031", Timestamps.formatKey(LocalDate.parse("2020-07-31"), TimeScope.WEEKLY));
        Assertions.assertEquals("Y2020", Timestamps.formatKey(LocalDate.parse("2020-07-31"), TimeScope.YEARLY));
        Assertions.assertNull(Timestamps.formatKey(LocalDate.parse("2020-07-31"), TimeScope.ALL));
    }
}
