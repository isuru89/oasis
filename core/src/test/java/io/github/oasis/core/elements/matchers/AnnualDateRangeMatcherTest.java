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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Isuru Weerarathna
 */
public class AnnualDateRangeMatcherTest {

    @Test
    public void testRangeWithinSameYear() {
        AnnualDateRangeMatcher matcher = AnnualDateRangeMatcher.create("03-01", "03-31");
        assertTrue(matcher.isBetween(1583087400000L, "Asia/Colombo"));

        // 03-01 in different time zones
        assertTrue(matcher.isBetween(1583020800000L, "Asia/Colombo"));
        assertTrue(matcher.isBetween(1583020800000L, "UTC"));
        assertFalse(matcher.isBetween(1583020800000L, "America/Los_Angeles"));

        // 03-31 in different time zones
        assertTrue(matcher.isBetween(1585612800000L, "Asia/Colombo"));
        assertFalse(matcher.isBetween(1585699199999L, "Asia/Colombo"));
        assertTrue(matcher.isBetween(1585699199000L, "America/Los_Angeles"));
    }

    @Test
    public void testRangeWithinAcrossYear() {
        AnnualDateRangeMatcher matcher = AnnualDateRangeMatcher.create("12-01", "01-31");

        // 12-31
        assertTrue(matcher.isBetween(1609372800000L, "Asia/Colombo"));
        // 01-01
        assertTrue(matcher.isBetween(1609459200000L, "Asia/Colombo"));

        // 12-01 in different time zones
        assertTrue(matcher.isBetween(1638316800000L, "Asia/Colombo"));
        assertTrue(matcher.isBetween(1638316800000L, "UTC"));
        assertFalse(matcher.isBetween(1638316800000L, "America/Los_Angeles"));

        // 01-31 in different time zones
        assertTrue(matcher.isBetween(1612051200000L, "Asia/Colombo"));
        assertFalse(matcher.isBetween(1612137599999L, "Asia/Colombo"));
        assertTrue(matcher.isBetween(1612051200000L, "America/Los_Angeles"));
    }

}
