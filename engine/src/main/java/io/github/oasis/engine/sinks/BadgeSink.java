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

package io.github.oasis.engine.sinks;

import io.github.oasis.engine.external.Db;
import io.github.oasis.engine.external.DbContext;
import io.github.oasis.engine.external.Mapped;
import io.github.oasis.engine.external.Sorted;
import io.github.oasis.engine.model.ExecutionContext;
import io.github.oasis.engine.model.ID;
import io.github.oasis.engine.model.TimeContext;
import io.github.oasis.engine.rules.AbstractRule;
import io.github.oasis.engine.rules.signals.BadgeRemoveSignal;
import io.github.oasis.engine.rules.signals.BadgeSignal;
import io.github.oasis.engine.rules.signals.Signal;
import io.github.oasis.engine.rules.signals.StreakBadgeSignal;
import io.github.oasis.engine.rules.signals.TemporalBadge;

import javax.inject.Inject;
import java.io.IOException;

/**
 * @author Isuru Weerarathna
 */
public class BadgeSink extends AbstractSink {

    @Inject
    public BadgeSink(Db db) {
        super(db);
    }

    @Override
    public void consume(Signal badgeSignal, AbstractRule rule, ExecutionContext context) {
        try (DbContext db = dbPool.createContext()) {
            BadgeSignal signal = (BadgeSignal) badgeSignal;

            long userId = signal.getEventScope().getUserId();
            int gameId = signal.getEventScope().getGameId();
            long ts = signal.getOccurredTimestamp();
            String ruleId = signal.getRuleId();
            int addition = signal instanceof BadgeRemoveSignal ? -1 : 1;

            // badge log
            Sorted badgeLog = db.SORTED(ID.getGameUserBadgesLog(gameId, userId));
            String logMember = getBadgeKey(signal);
            boolean added = badgeLog.add(logMember, signal.getOccurredTimestamp());

            if (!added) {
                return;
            }

            Mapped badgesMap = db.MAP(ID.getGameUserBadgesSummary(gameId, userId));

            TimeContext tcx = new TimeContext(ts, context.getUserTimeOffset());

            badgesMap.incrementByInt("all", addition);
            badgesMap.incrementByInt("all:" + tcx.getYear(), addition);
            badgesMap.incrementByInt("all:" + tcx.getMonth(), addition);
            badgesMap.incrementByInt("all:" + tcx.getDay(), addition);
            badgesMap.incrementByInt("all:" + tcx.getWeek(), addition);
            badgesMap.incrementByInt("all:" + tcx.getQuarter(), addition);

            // by type + attr
            String rulePfx = "rule:" + ruleId + ":" + signal.getAttribute();
            badgesMap.incrementByInt(rulePfx, addition);
            badgesMap.incrementByInt(rulePfx + ":" + tcx.getYear(), addition);
            badgesMap.incrementByInt(rulePfx + ":" + tcx.getMonth(), addition);
            badgesMap.incrementByInt(rulePfx + ":" + tcx.getDay(), addition);
            badgesMap.incrementByInt(rulePfx + ":" + tcx.getWeek(), addition);
            badgesMap.incrementByInt(rulePfx + ":" + tcx.getQuarter(), addition);

            // by attr
            String attrPfx = "attr:" + signal.getAttribute();
            badgesMap.incrementByInt(rulePfx, addition);
            badgesMap.incrementByInt(attrPfx + ":" + tcx.getYear(), addition);
            badgesMap.incrementByInt(attrPfx + ":" + tcx.getMonth(), addition);
            badgesMap.incrementByInt(attrPfx + ":" + tcx.getDay(), addition);
            badgesMap.incrementByInt(attrPfx + ":" + tcx.getWeek(), addition);
            badgesMap.incrementByInt(attrPfx + ":" + tcx.getQuarter(), addition);



        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getBadgeKey(BadgeSignal signal) {
        if (signal instanceof TemporalBadge || signal instanceof StreakBadgeSignal) {
            return String.format("%s:%d:%d", signal.getRuleId(), signal.getAttribute(), signal.getStartTime());
        } else {
            return String.format("%s:%d:%s", signal.getRuleId(), signal.getAttribute(), signal.getEndId());
        }
    }
}
