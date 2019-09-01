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

package io.github.oasis.game.process.triggers;

import io.github.oasis.model.Event;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.common.state.ReducingState;
import org.apache.flink.api.common.state.ReducingStateDescriptor;
import org.apache.flink.api.common.typeutils.base.LongSerializer;
import org.apache.flink.streaming.api.windowing.triggers.Trigger;
import org.apache.flink.streaming.api.windowing.triggers.TriggerResult;
import org.apache.flink.streaming.api.windowing.windows.Window;

/**
 * @author iweerarathna
 */
public class StreakTrigger<E extends Event, W extends Window> extends Trigger<E, W> {
    private static final long serialVersionUID = 1L;

    private int length;
    private FilterFunction<E> filter;
    private boolean fireWhenEventTime = false;

    private final ReducingStateDescriptor<Long> stateDesc =
            new ReducingStateDescriptor<>("streak-count", new Sum(), LongSerializer.INSTANCE);

    public StreakTrigger(int streak, FilterFunction<E> filter) {
        this.length = streak;
        this.filter = filter;
    }

    public StreakTrigger(int streak, FilterFunction<E> filter, boolean fireWhenEventTime) {
        this(streak, filter);
        this.fireWhenEventTime = fireWhenEventTime;
    }

    @Override
    public TriggerResult onElement(E element, long timestamp, W window, TriggerContext ctx) throws Exception {
        ReducingState<Long> count = ctx.getPartitionedState(stateDesc);
        if (filter.filter(element)) {
            count.add(1L);
            if (count.get() >= length) {
                count.clear();
                return TriggerResult.FIRE_AND_PURGE;
            }
            return TriggerResult.CONTINUE;
        } else {
            count.clear();
            return TriggerResult.PURGE;
        }
    }

    @Override
    public TriggerResult onProcessingTime(long time, W window, TriggerContext ctx) {
        return TriggerResult.CONTINUE;
    }

    @Override
    public TriggerResult onEventTime(long time, W window, TriggerContext ctx) {
        if (fireWhenEventTime && time == window.maxTimestamp()) {
            ReducingState<Long> count = ctx.getPartitionedState(stateDesc);
            if (count != null) {
                count.clear();
            }
            return TriggerResult.FIRE_AND_PURGE;
        }
        return TriggerResult.CONTINUE;
    }

    @Override
    public void clear(W window, TriggerContext ctx) {
    }

    public static class Sum implements ReduceFunction<Long> {
        private static final long serialVersionUID = 1L;

        @Override
        public Long reduce(Long value1, Long value2) {
            return value1 + value2;
        }

    }
}
