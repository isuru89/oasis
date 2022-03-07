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
import io.github.oasis.core.elements.RuleContext;
import io.github.oasis.core.external.Db;
import io.github.oasis.core.external.DbContext;
import io.github.oasis.core.utils.Utils;
import io.github.oasis.elements.badges.BadgeIDs;
import io.github.oasis.elements.badges.rules.FirstEventBadgeRule;
import io.github.oasis.elements.badges.signals.BadgeSignal;

import java.util.List;

import static io.github.oasis.core.utils.Texts.COLON;

/**
 * Awards a badge when something occurs for the very first time.
 *
 * @author Isuru Weerarathna
 */
public class BadgeFirstEvent extends AbstractBadgeProcessor<FirstEventBadgeRule> {

    public BadgeFirstEvent(Db pool, RuleContext<FirstEventBadgeRule> ruleContext) {
        super(pool, ruleContext);
    }

    @Override
    public List<BadgeSignal> process(Event event, FirstEventBadgeRule rule, ExecutionContext context, DbContext db) {
        String key = BadgeIDs.getUserFirstEventsKey(event.getGameId(), event.getUser());
        long ts = event.getTimestamp();
        String id = event.getExternalId();
        String subKey = rule.getEventName();
        String value = ts + COLON + id + COLON + System.currentTimeMillis();

        if (isFirstOne(db.setIfNotExistsInMap(key, subKey, value))) {
            return List.of(
                    BadgeSignal.firstEvent(rule.getId(),
                            Utils.firstNonNull(rule.getBadgeId(), rule.getId()),
                            event,
                            rule.getAttributeId())
            );
        }
        return null;
    }

    private boolean isFirstOne(boolean status) {
        return status;
    }

}
