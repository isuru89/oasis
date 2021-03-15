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

import java.time.ZonedDateTime;
import java.time.temporal.IsoFields;

/**
 * @author Isuru Weerarathna
 */
public class TimeOffset {

    static final String DAY_PATTERN = "D%d%02d%02d";
    static final String MONTH_PATTERN = "M%s%02d";
    static final String WEEK_PATTERN = "W%d%02d";
    static final String QUARTER_PATTERN = "Q%d%02d";

    private String year;
    private String month;
    private String day;
    private String week;
    private String quarter;

    public TimeOffset(long ts, int offset) {
        ZonedDateTime userTime = Timestamps.getUserSpecificTime(ts, offset);
        int y = userTime.getYear();
        year = "Y" + y;
        month = String.format(MONTH_PATTERN, y, userTime.getMonth().getValue());
        day = String.format(DAY_PATTERN, y, userTime.getMonth().getValue(), userTime.getDayOfMonth());
        week = String.format(WEEK_PATTERN, y, userTime.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR));
        quarter = String.format(QUARTER_PATTERN, y, userTime.get(IsoFields.QUARTER_OF_YEAR));
    }

    public TimeOffset(long ts, String timeZone) {
        ZonedDateTime userTime = Timestamps.getUserSpecificTime(ts, timeZone);
        int y = userTime.getYear();
        year = "Y" + y;
        month = String.format(MONTH_PATTERN, y, userTime.getMonth().getValue());
        day = String.format(DAY_PATTERN, y, userTime.getMonth().getValue(), userTime.getDayOfMonth());
        week = String.format(WEEK_PATTERN, y, userTime.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR));
        quarter = String.format(QUARTER_PATTERN, y, userTime.get(IsoFields.QUARTER_OF_YEAR));
    }

    public String getByType(String timeType) {
        char firstChar = Character.toUpperCase(timeType.charAt(0));
        switch (firstChar) {
            case 'D': return getDay();
            case 'M': return getMonth();
            case 'Q': return getQuarter();
            case 'Y':
            case 'A': return getYear();
            default: return getWeek();
        }
    }

    public String getYear() {
        return year;
    }

    public String getMonth() {
        return month;
    }

    public String getDay() {
        return day;
    }

    public String getWeek() {
        return week;
    }

    public String getQuarter() {
        return quarter;
    }
}
