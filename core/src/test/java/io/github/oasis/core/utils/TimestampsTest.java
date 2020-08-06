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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;

/**
 * @author Isuru Weerarathna
 */
public class TimestampsTest {

    @Test
    public void testTimeUnitStr() {
        Assertions.assertEquals(0L, Timestamps.parseTimeUnit(null));
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

}
