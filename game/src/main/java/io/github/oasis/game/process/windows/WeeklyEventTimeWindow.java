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

package io.github.oasis.game.process.windows;

import io.github.oasis.model.collect.Pair;
import io.github.oasis.model.utils.TimeUtils;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;

import java.time.ZoneOffset;

/**
 * @author iweerarathna
 */
public class WeeklyEventTimeWindow extends OasisTimeWindowAssigner {

    private static final long DAY_ONE = 86400000L;

    @Override
    protected TimeWindow findWindow(long timestamp, Object element) {
        long start = TimeWindow.getWindowStartWithOffset(timestamp, 0, DAY_ONE);
        Pair<Long, Long> weekRange = TimeUtils.getWeekRange(start, ZoneOffset.UTC);
        return new TimeWindow(weekRange.getValue0(), weekRange.getValue1());
    }
}
