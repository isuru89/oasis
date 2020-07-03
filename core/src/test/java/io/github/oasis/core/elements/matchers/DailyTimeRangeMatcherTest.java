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
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Isuru Weerarathna
 */
public class DailyTimeRangeMatcherTest {

    @Test
    public void testSameDayTimes() {
        TimeRangeMatcher matcher = DailyTimeRangeMatcher.create("09:00:00", "13:00:00");

        assertTrue(matcher.isBetween(1593574200000L, "Asia/Colombo")); // 03:30 UTC
        assertTrue(matcher.isBetween(1593577800000L, "Asia/Colombo")); // 04:30 UTC
        assertFalse(matcher.isBetween(1593577800000L, "Pacific/Auckland")); // 04:30 UTC
        assertFalse(matcher.isBetween(1593561600000L, "Asia/Colombo")); // 00:00 UTC
        assertFalse(matcher.isBetween(1593592200000L, "Asia/Colombo")); // 08:30 UTC
        assertTrue(matcher.isBetween(1593588600000L, "Asia/Colombo")); // 07:30 UTC
    }

    @Test
    public void testAcrossDayTimes() {
        TimeRangeMatcher matcher = DailyTimeRangeMatcher.create("22:00:00", "02:00:00");

        assertTrue(matcher.isBetween(1593621000000L, "Asia/Colombo")); // 16:30 UTC
        assertTrue(matcher.isBetween(1593624600000L, "Asia/Colombo")); // 17:30 UTC
        assertTrue(matcher.isBetween(1593648000000L, "UTC")); // 00:00 UTC
        assertTrue(matcher.isBetween(1593612000000L, "Pacific/Auckland")); // 04:30 UTC
        assertFalse(matcher.isBetween(1593639000000L, "Asia/Colombo")); // 08:30 UTC
        assertTrue(matcher.isBetween(1593635400000L, "Asia/Colombo")); // 20:30 UTC
    }
}
