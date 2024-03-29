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

package io.github.oasis.elements.badges.signals;

import io.github.oasis.core.Event;
import io.github.oasis.core.EventScope;
import io.github.oasis.core.elements.FeedEntry;
import io.github.oasis.core.elements.Signal;
import io.github.oasis.core.utils.Texts;
import io.github.oasis.elements.badges.BadgeIDs;
import io.github.oasis.elements.badges.BadgesModule;
import io.github.oasis.elements.badges.spec.BadgeFeedData;

import java.util.Optional;

/**
 * @author Isuru Weerarathna
 */
public class BadgeRemoveSignal extends BadgeSignal {

    public BadgeRemoveSignal(String ruleId, EventScope event, int rank, long st, long et, String sid, String eid) {
        super(ruleId, event, st, rank, st, et, sid, eid);
    }

    public BadgeRemoveSignal(String ruleId, EventScope eventScope, int rank, long st) {
        super(ruleId, eventScope, st, rank, st, -1, null, null);
    }

    public BadgeRemoveSignal(BadgeSignal prevBadge) {
        super(prevBadge.getRuleId(),
                prevBadge.getEventScope(),
                prevBadge.getStartTime(),
                prevBadge.getRank(),
                prevBadge.getStartTime(),
                prevBadge.getEndTime(),
                prevBadge.getStartId(),
                prevBadge.getEndId());
    }

    @Override
    public Optional<FeedEntry> generateFeedEntry() {
        return Optional.of(FeedEntry.builder()
                .byPlugin(BadgesModule.ID)
                .eventTimestamp(getOccurredTimestamp())
                .type(BadgeIDs.FEED_TYPE_BADGE_REMOVED)
                .scope(FeedEntry.FeedScope.fromEventScope(getEventScope(), getRuleId()))
                .data(BadgeFeedData.builder()
                        .ruleId(getRuleId())
                        .rank(getRank())
                        .build()
                ).build());
    }

    @Override
    public Optional<Signal> createSignal(Event causedEvent) {
        if (Texts.isNotEmpty(getPointId()) && getPoints() != null) {
            return Optional.of(new BadgePointSignal(getPointId(), getPoints().negate(), causedEvent));
        }
        return Optional.empty();
    }

    @Override
    public String toString() {
        return "BadgeRemoveSignal{" +
                "startTime=" + getStartTime() + ", " +
                "endTime=" + getEndTime() + ", " +
                "startId=" + getStartId() + ", " +
                "endId=" + getEndId() + ", " +
                "rank=" + getRank() +
                "}";
    }
}
