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

import io.github.oasis.game.utils.Utils;
import io.github.oasis.model.Badge;
import io.github.oasis.model.Event;
import io.github.oasis.model.events.BadgeEvent;
import io.github.oasis.model.rules.BadgeFromEvents;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.Window;
import org.apache.flink.util.Collector;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

/**
 * @author iweerarathna
 */
class ConditionBadgeHandler<E extends Event, W extends Window>
        extends ProcessWindowFunction<E, BadgeEvent, Long, W> {

    private BadgeFromEvents badgeRule;

    private final ValueStateDescriptor<Boolean> activeState;
    private final ValueStateDescriptor<Integer> maxBadgesState;

    ConditionBadgeHandler(BadgeFromEvents badgeRule) {
        this.badgeRule = badgeRule;
        this.activeState = new ValueStateDescriptor<>("badges-activated", Boolean.class);
        this.maxBadgesState = new ValueStateDescriptor<>("max-badges-count", Integer.class);
    }

    @Override
    public void process(Long userId, Context context, Iterable<E> elements, Collector<BadgeEvent> out) throws Exception {
        ValueState<Boolean> state = context.globalState().getState(activeState);
        if (state.value() != null && !state.value()) {
            return;
        }

        ValueState<Integer> currMaxBadges = context.globalState().getState(maxBadgesState);
        if (currMaxBadges.value() == null) {
            currMaxBadges.update(0);
        }
        int curr = currMaxBadges.value();

        E value = elements.iterator().next();
        if (Utils.eventEquals(value, badgeRule.getEventType())) {
            Map<String, Object> vars = value.getAllFieldValues();
            if (Utils.evaluateCondition(badgeRule.getCondition(), vars)) {
                curr++;
                out.collect(new BadgeEvent(userId, badgeRule.getBadge(), badgeRule, Collections.singletonList(value), value));

                if (curr >= badgeRule.getMaxBadges()) {
                    state.update(false);
                }
            }

            if (badgeRule.getSubBadges() != null && !badgeRule.getSubBadges().isEmpty()) {
                for (Badge badge : badgeRule.getSubBadges()) {
                    if (badge instanceof BadgeFromEvents.ConditionalSubBadge) {
                        Serializable condition = ((BadgeFromEvents.ConditionalSubBadge) badge).getCondition();
                        if (Utils.evaluateCondition(condition, vars)) {
                            out.collect(new BadgeEvent(userId, badge, badgeRule, Collections.singletonList(value), value));
                        }
                    }
                }
            }
        }
    }
}