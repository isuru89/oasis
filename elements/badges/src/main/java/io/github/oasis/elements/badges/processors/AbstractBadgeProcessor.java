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

package io.github.oasis.elements.badges.processors;

import io.github.oasis.core.Event;
import io.github.oasis.core.context.ExecutionContext;
import io.github.oasis.core.elements.AbstractProcessor;
import io.github.oasis.core.elements.AbstractRule;
import io.github.oasis.core.elements.RuleContext;
import io.github.oasis.core.elements.Signal;
import io.github.oasis.core.external.Db;
import io.github.oasis.core.external.DbContext;
import io.github.oasis.core.external.Mapped;
import io.github.oasis.elements.badges.BadgeIDs;
import io.github.oasis.elements.badges.StreakSupport;
import io.github.oasis.elements.badges.rules.BadgeRule;
import io.github.oasis.elements.badges.signals.BadgeRemoveSignal;
import io.github.oasis.elements.badges.signals.BadgeSignal;


/**
 * @author Isuru Weerarathna
 */
public abstract class AbstractBadgeProcessor<R extends BadgeRule> extends AbstractProcessor<R, BadgeSignal> {

    private static final String BADGE_HISTORY_FORMAT = "%d:%s:%d:%d";

    public AbstractBadgeProcessor(Db pool, RuleContext<R> ruleContext) {
        super(pool, ruleContext);
    }

    protected String getMetaStreakKey(AbstractRule rule) {
        return rule.getId() + ".streak";
    }

    protected String getMetaEndTimeKey(AbstractRule rule) {
        return rule.getId();
    }

    @Override
    protected void beforeEmit(BadgeSignal signal, Event event, R rule, ExecutionContext context, DbContext db) {
        rule.derivePointsInTo(signal);
        negatePointsIfBadgeRemoval(signal);

        db.addToSorted(BadgeIDs.getUserBadgeSpecKey(event.getGameId(), event.getUser(), rule.getId()),
                String.format(BADGE_HISTORY_FORMAT, signal.getEndTime(), rule.getId(), signal.getStartTime(), signal.getRank()),
                signal.getStartTime());
        String userBadgesMeta = BadgeIDs.getUserBadgesMetaKey(event.getGameId(), event.getUser());
        Mapped map = db.MAP(userBadgesMeta);
        String value = map.getValue(rule.getId());
        int streak = 0;
        boolean supportStreak = false;

        if (signal instanceof StreakSupport streakSupport) {
            streak = streakSupport.getStreak();
            supportStreak = true;
        }

        String streakKey = getMetaStreakKey(rule);
        String endTimeKey = getMetaEndTimeKey(rule);
        if (value == null) {
            map.setValue(endTimeKey, signal.getEndTime());
            if (supportStreak) {
                map.setValue(streakKey, streak);
            }
        } else {
            long val = Long.parseLong(value);
            if (signal.getEndTime() >= val) {
                map.setValue(streakKey, streak);
            }
            map.setValue(endTimeKey, Math.max(signal.getEndTime(), val));
        }
    }

    private void negatePointsIfBadgeRemoval(Signal signalRef) {
        if (signalRef instanceof BadgeRemoveSignal signal) {
            if (signal.getPointId() != null) {
                signal.setPointAwards(signal.getPointId(), signal.getPoints().negate());
            }
        }
    }
}
