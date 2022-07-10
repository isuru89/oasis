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
import io.github.oasis.core.EventJson;
import io.github.oasis.core.EventScope;
import io.github.oasis.core.elements.AbstractSink;
import io.github.oasis.core.elements.EventCreatable;
import io.github.oasis.core.elements.FeedEntry;
import io.github.oasis.core.elements.Signal;
import io.github.oasis.core.elements.SignalCreatable;
import io.github.oasis.core.utils.Texts;
import io.github.oasis.elements.badges.BadgeIDs;
import io.github.oasis.elements.badges.BadgePointsEvent;
import io.github.oasis.elements.badges.BadgeSink;
import io.github.oasis.elements.badges.BadgesModule;
import io.github.oasis.elements.badges.spec.BadgeFeedData;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * @author Isuru Weerarathna
 */
@ToString
public class BadgeSignal extends Signal implements EventCreatable, SignalCreatable {

    private final long startTime;
    private final long endTime;
    private final String startId;
    private final String endId;
    private final int attribute;

    private String pointId;
    private BigDecimal points;

    public BadgeSignal(String ruleId,
                EventScope eventScope,
                long occurredTs,
                int attributeId,
                long st, long et,
                String sid, String eid) {
        super(ruleId, eventScope, occurredTs);

        this.attribute = attributeId;
        startTime = st;
        endTime = et;
        startId = sid;
        endId = eid;
    }

    public BadgeSignal(String ruleId,
                       Event event,
                       long occurredTs,
                       int attributeId,
                       long st, long et,
                       String sid, String eid) {
        super(ruleId, event, occurredTs);

        this.attribute = attributeId;
        this.startTime = st;
        this.endTime = et;
        this.startId = sid;
        this.endId = eid;
    }

    public static BadgeSignal firstEvent(String ruleId, Event causedEvent, int attributeId) {
        return new BadgeSignal(ruleId, causedEvent, causedEvent.getTimestamp(), attributeId,
                causedEvent.getTimestamp(), causedEvent.getTimestamp(),
                causedEvent.getExternalId(), causedEvent.getExternalId());
    }

    @Override
    public Optional<Event> generateEvent() {
        if (Texts.isNotEmpty(pointId) && points != null) {
            return Optional.of(new BadgePointsEvent(pointId, points, BadgeRefEvent.create(this)));
        }
        return Optional.empty();
    }

    @Override
    public Optional<FeedEntry> generateFeedEntry() {
        return Optional.of(FeedEntry.builder()
                .byPlugin(BadgesModule.ID)
                .eventTimestamp(getOccurredTimestamp())
                .type(BadgeIDs.FEED_TYPE_BADGE_EARNED)
                .scope(FeedEntry.FeedScope.fromEventScope(getEventScope(), getRuleId()))
                .data(BadgeFeedData.builder()
                    .ruleId(getRuleId())
                    .attribute(attribute)
                    .build()
                ).build());
    }

    @Override
    public Optional<Signal> createSignal(Event causedEvent) {
        if (Texts.isNotEmpty(pointId) && points != null) {
            return Optional.of(new BadgePointSignal(pointId, points, causedEvent));
        }
        return Optional.empty();
    }

    public BadgeSignal setPointAwards(String pointId, BigDecimal value) {
        this.pointId = pointId;
        this.points = value;
        return this;
    }

    public String getPointId() {
        return pointId;
    }

    public BigDecimal getPoints() {
        return points;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public int getAttribute() {
        return attribute;
    }

    public String getStartId() {
        return startId;
    }

    public String getEndId() {
        return endId;
    }

    public String getUniqueId() {
        return getRuleId() + ":" + attribute + ":" + startTime;
    }

    @Override
    public Class<? extends AbstractSink> sinkHandler() {
        return BadgeSink.class;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BadgeSignal signal = (BadgeSignal) o;
        return getUniqueId().equals(signal.getUniqueId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getRuleId(), startTime, attribute);
    }

    @Override
    public int compareTo(Signal o) {
        if (o instanceof BadgeSignal) {
            Comparator<BadgeSignal> comparator = Comparator
                    .comparingLong(BadgeSignal::getStartTime)
                    .thenComparing(Signal::getEventScope)
                    .thenComparing(BadgeSignal::getAttribute)
                    .thenComparing(BadgeSignal::getRuleId)
                    .thenComparingLong(BadgeSignal::getEndTime)
                    .thenComparing(BadgeSignal::getStartId)
                    .thenComparing(BadgeSignal::getEndId);
            if (getPointId() != null) {
                comparator = comparator.thenComparing(BadgeSignal::getPointId);
            }
            if (getPoints() != null) {
                comparator = comparator.thenComparing(BadgeSignal::getPoints);
            }
            return comparator.compare(this, (BadgeSignal) o);
        } else {
            return -1;
        }
    }

    public static class BadgeRefEvent extends EventJson {

        public BadgeRefEvent(Map<String, Object> ref) {
            super(ref);
        }

        public static BadgeRefEvent create(BadgeSignal signal) {
            Map<String, Object> data = new HashMap<>();
            data.put(Event.ID, UUID.randomUUID().toString());
            data.put(Event.TEAM_ID, signal.getEventScope().getTeamId());
            data.put(Event.USER_ID, signal.getEventScope().getUserId());
            data.put(Event.EVENT_TYPE, signal.getPointId());
            data.put(Event.GAME_ID, signal.getEventScope().getGameId());
            data.put(Event.SOURCE_ID, signal.getEventScope().getSourceId());
            data.put(Event.TIMESTAMP, signal.getEndTime());
            return new BadgeRefEvent(data);
        }
    }
}
