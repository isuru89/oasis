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

/**
 * Compares timestamp for absolute time ranges. Start time
 * inclusive, while end time is exclusive. Timezone is neglected.
 *
 * @author Isuru Weerarathna
 */
public class AbsoluteTimeMatcher implements TimeRangeMatcher {

    private final long startTime;
    private final long endTime;

    private AbsoluteTimeMatcher(long startTime, long endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public static AbsoluteTimeMatcher create(long start, long end) {
        return new AbsoluteTimeMatcher(start, end);
    }

    @Override
    public boolean isBetween(long timeMs, String timeZone) {
        return timeMs >= startTime && timeMs < endTime;
    }
}
