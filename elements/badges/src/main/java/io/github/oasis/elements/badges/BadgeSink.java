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

package io.github.oasis.elements.badges;

import io.github.oasis.core.elements.AbstractSink;
import io.github.oasis.core.external.Db;
import io.github.oasis.core.external.DbContext;
import io.github.oasis.core.external.Mapped;
import io.github.oasis.core.external.Sorted;
import io.github.oasis.core.context.ExecutionContext;
import io.github.oasis.core.ID;
import io.github.oasis.core.utils.TimeOffset;
import io.github.oasis.core.elements.AbstractRule;
import io.github.oasis.elements.badges.signals.BadgeRemoveSignal;
import io.github.oasis.elements.badges.signals.BadgeSignal;
import io.github.oasis.core.elements.Signal;
import io.github.oasis.elements.badges.signals.StreakBadgeSignal;
import io.github.oasis.elements.badges.signals.TemporalBadgeSignal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author Isuru Weerarathna
 */
public class BadgeSink extends AbstractSink {

    private static final Logger LOG = LoggerFactory.getLogger(BadgeSink.class);

    private static final String ALL = "all";
    private static final String ALL_PFX = ALL + COLON;
    private static final String RULE_PFX = "rule" + COLON;
    private static final String ATTR_PFX = "attr" + COLON;

    private static final String STREAK_BADGE_FORMAT = "%s:%d:%d";
    private static final String GENERAL_BADGE_FORMAT = "%s:%d:%s";

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

            TimeOffset tcx = new TimeOffset(ts, context.getUserTimeOffset());

            badgesMap.incrementByInt(ALL, addition);
            badgesMap.incrementByInt(ALL_PFX + tcx.getYear(), addition);
            badgesMap.incrementByInt(ALL_PFX + tcx.getMonth(), addition);
            badgesMap.incrementByInt(ALL_PFX + tcx.getDay(), addition);
            badgesMap.incrementByInt(ALL_PFX + tcx.getWeek(), addition);
            badgesMap.incrementByInt(ALL_PFX + tcx.getQuarter(), addition);

            // by type + attr
            String rulePfx = RULE_PFX + ruleId + COLON + signal.getAttribute();
            badgesMap.incrementByInt(rulePfx, addition);
            badgesMap.incrementByInt(rulePfx + COLON + tcx.getYear(), addition);
            badgesMap.incrementByInt(rulePfx + COLON + tcx.getMonth(), addition);
            badgesMap.incrementByInt(rulePfx + COLON + tcx.getDay(), addition);
            badgesMap.incrementByInt(rulePfx + COLON + tcx.getWeek(), addition);
            badgesMap.incrementByInt(rulePfx + COLON + tcx.getQuarter(), addition);

            // by attr
            String attrPfx = ATTR_PFX + signal.getAttribute();
            badgesMap.incrementByInt(attrPfx, addition);
            badgesMap.incrementByInt(attrPfx + COLON + tcx.getYear(), addition);
            badgesMap.incrementByInt(attrPfx + COLON + tcx.getMonth(), addition);
            badgesMap.incrementByInt(attrPfx + COLON + tcx.getDay(), addition);
            badgesMap.incrementByInt(attrPfx + COLON + tcx.getWeek(), addition);
            badgesMap.incrementByInt(attrPfx + COLON + tcx.getQuarter(), addition);

        } catch (IOException e) {
            LOG.error("Error persisting badges metrics!", e);
        }
    }

    private String getBadgeKey(BadgeSignal signal) {
        if (signal instanceof TemporalBadgeSignal || signal instanceof StreakBadgeSignal) {
            return String.format(STREAK_BADGE_FORMAT, signal.getRuleId(), signal.getAttribute(), signal.getStartTime());
        } else {
            return String.format(GENERAL_BADGE_FORMAT, signal.getRuleId(), signal.getAttribute(), signal.getEndId());
        }
    }
}
