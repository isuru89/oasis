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

package io.github.oasis.game.factory.badges;

import io.github.oasis.model.Badge;
import io.github.oasis.model.Event;
import io.github.oasis.model.events.BadgeEvent;
import io.github.oasis.model.rules.BadgeFromEvents;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.Window;
import org.apache.flink.util.Collector;

import java.util.Arrays;
import java.util.Iterator;

/**
 * @author iweerarathna
 */
class StreakBadgeHandlerEvents<E extends Event, W extends Window>
        extends ProcessWindowFunction<E, BadgeEvent, Long, W> {

    private BadgeFromEvents rule;

    StreakBadgeHandlerEvents(BadgeFromEvents rule) {
        this.rule = rule;
    }

    @Override
    public void process(Long userId, Context context, Iterable<E> elements, Collector<BadgeEvent> out) {
        Iterator<? extends Event> iterator = elements.iterator();
        Event first = null, last = null;
        int count = 0;
        while (iterator.hasNext()) {
            Event next = iterator.next();
            if (first == null) first = next;
            last = next;
            count++;
        }

        Badge subBadge = rule.getSubBadge(count);
        if (subBadge != null) {
            out.collect(new BadgeEvent(userId, subBadge, rule, Arrays.asList(first, last), last));
        }

    }
}
