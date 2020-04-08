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
import io.github.oasis.engine.model.RuleContext;
import io.github.oasis.engine.rules.BadgeFirstEventRule;
import io.github.oasis.engine.rules.signals.BadgeSignal;
import io.github.oasis.engine.external.Db;
import io.github.oasis.engine.external.DbContext;
import io.github.oasis.model.Event;

import java.util.Collections;
import java.util.List;

/**
 * @author Isuru Weerarathna
 */
public class BadgeFirstEvent extends BadgeProcessor<BadgeFirstEventRule> {

    public BadgeFirstEvent(Db pool, RuleContext<BadgeFirstEventRule> ruleContext) {
        super(pool, ruleContext);
    }

    @Override
    public List<BadgeSignal> process(Event event, BadgeFirstEventRule rule, DbContext db) {
        String key = ID.getUserFirstEventsKey(event.getGameId(), event.getUser());
        long ts = event.getTimestamp();
        String id = event.getExternalId();
        String subKey = rule.getEventName();
        String value = ts + ":" + id + ":" + System.currentTimeMillis();
        if (isFirstOne(db.setIfNotExistsInMap(key, subKey, value))) {
            return Collections.singletonList(BadgeSignal.firstEvent(rule.getId(), event, 1));
        }
        return null;
    }

    private boolean isFirstOne(boolean status) {
        return status;
    }

}
