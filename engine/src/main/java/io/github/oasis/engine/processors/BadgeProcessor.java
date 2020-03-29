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

package io.github.oasis.engine.processors;

import io.github.oasis.engine.model.ID;
import io.github.oasis.engine.rules.AbstractRule;
import io.github.oasis.engine.rules.BadgeRule;
import io.github.oasis.engine.rules.signals.BadgeSignal;
import io.github.oasis.engine.storage.Db;
import io.github.oasis.engine.storage.DbContext;
import io.github.oasis.engine.storage.Mapped;
import io.github.oasis.model.Event;

/**
 * @author Isuru Weerarathna
 */
public abstract class BadgeProcessor<R extends BadgeRule> extends AbstractProcessor<R, BadgeSignal> {
    public BadgeProcessor(Db pool, R rule) {
        super(pool, rule);
    }

    protected String getMetaStreakKey(AbstractRule rule) {
        return rule.getId() + ".streak";
    }

    protected String getMetaEndTimeKey(AbstractRule rule) {
        return rule.getId();
    }

    @Override
    protected void beforeEmit(BadgeSignal signal, Event event, R rule, DbContext db) {
        db.addToSorted(ID.getUserBadgeSpecKey(event.getGameId(), event.getUser(), rule.getId()),
                String.format("%d:%s:%d:%d", signal.getEndTime(), rule.getId(), signal.getStartTime(), signal.getAttribute()),
                signal.getStartTime());
        String userBadgesMeta = ID.getUserBadgesMetaKey(event.getGameId(), event.getUser());
        Mapped map = db.MAP(userBadgesMeta);
        String value = map.getValue(rule.getId());
        String streakKey = getMetaStreakKey(rule);
        String endTimeKey = getMetaEndTimeKey(rule);
        if (value == null) {
            map.setValue(endTimeKey, signal.getEndTime());
            map.setValue(streakKey, signal.getAttribute());
        } else {
            long val = Long.parseLong(value);
            if (signal.getEndTime() >= val) {
                map.setValue(streakKey, signal.getAttribute());
            }
            map.setValue(endTimeKey, Math.max(signal.getEndTime(), val));
        }
    }

}
