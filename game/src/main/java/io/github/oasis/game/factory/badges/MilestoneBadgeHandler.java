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
import io.github.oasis.model.events.BadgeEvent;
import io.github.oasis.model.events.MilestoneEvent;
import io.github.oasis.model.rules.BadgeFromMilestone;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.GlobalWindow;
import org.apache.flink.util.Collector;

import java.util.Collections;

/**
 * @author iweerarathna
 */
class MilestoneBadgeHandler<E extends MilestoneEvent>
        extends ProcessWindowFunction<E, BadgeEvent, Long, GlobalWindow> {

    private final BadgeFromMilestone badgeRule;

    MilestoneBadgeHandler(BadgeFromMilestone rule) {
        this.badgeRule = rule;
    }

    @Override
    public void process(Long userId, Context context, Iterable<E> elements, Collector<BadgeEvent> out) throws Exception {
        E next = elements.iterator().next();
        Badge badge = null;
        if (badgeRule.getLevel() == next.getLevel()) {
            badge = badgeRule.getBadge();
        } else if (badgeRule.getSubBadges() != null) {
            for (Badge subBadge : badgeRule.getSubBadges()) {
                if (subBadge instanceof BadgeFromMilestone.LevelSubBadge
                        && ((BadgeFromMilestone.LevelSubBadge) subBadge).getLevel() == next.getLevel()) {
                    badge = subBadge;
                    break;
                }
            }
        }

        if (badge != null) {
            out.collect(new BadgeEvent(userId, badge, badgeRule, Collections.singletonList(next), next));
        }
    }
}