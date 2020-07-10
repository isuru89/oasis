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
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Isuru Weerarathna
 */
public class WeeklyTimeMatcherTest {

    @Test
    public void testSingleDay() {
        TimeRangeMatcher matcher = WeeklyTimeMatcher.create("Friday");

        assertTrue(matcher.isBetween(1593747000000L, "Asia/Colombo")); // 03:30 UTC
        assertFalse(matcher.isBetween(1593797400000L, "Pacific/Auckland")); // 17:30 UTC
        assertFalse(matcher.isBetween(1593574200000L, "Asia/Colombo")); // 03:30 UTC
    }

    @Test
    public void testMultipleDays() {
        TimeRangeMatcher matcher = WeeklyTimeMatcher.create("Friday,Saturday,Sunday");

        assertTrue(matcher.isBetween(1593747000000L, "Asia/Colombo")); // 03:30 UTC
        assertFalse(matcher.isBetween(1593747000000L, "America/Los_Angeles")); // 03:30 UTC
        assertTrue(matcher.isBetween(1593797400000L, "Pacific/Auckland")); // 17:30 UTC
        assertFalse(matcher.isBetween(1593574200000L, "Asia/Colombo")); // 03:30 UTC
    }

    @Test
    public void testUnknownDayFail() {
        assertThrows(IllegalArgumentException.class, () -> WeeklyTimeMatcher.create("Fridays"));
        assertThrows(IllegalArgumentException.class, () -> WeeklyTimeMatcher.create(""));
    }

}
