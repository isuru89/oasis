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

package io.github.oasis.elements.ratings;

import io.github.oasis.core.Event;
import io.github.oasis.core.elements.FeedEntry;
import io.github.oasis.core.elements.Signal;
import io.github.oasis.elements.ratings.spec.RatingFeedData;
import lombok.ToString;

import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Isuru Weerarathna
 */
@ToString(callSuper = true)
public class RatingChangedSignal extends AbstractRatingSignal {

    private int previousRating;
    private long changedAt;
    private String changedEvent;

    public RatingChangedSignal(String ruleId, int previousRating, int rating, long changedAt, Event changedEvent) {
        super(ruleId, changedEvent.asEventScope(), changedAt, rating);
        this.previousRating = previousRating;
        this.changedAt = changedAt;
        this.changedEvent = changedEvent.getExternalId();
    }

    public int getPreviousRating() {
        return previousRating;
    }

    public long getChangedAt() {
        return changedAt;
    }

    public String getChangedEvent() {
        return changedEvent;
    }

    public static RatingChangedSignal create(RatingRule rule, Event causedEvent, int previousRating, int newRating) {
        return new RatingChangedSignal(rule.getId(), previousRating, newRating, causedEvent.getTimestamp(), causedEvent);
    }

    @Override
    public Optional<FeedEntry> generateFeedEntry() {
        if (getPreviousRating() != getCurrentRating()) {
            return Optional.of(FeedEntry.builder()
                    .byPlugin(RatingsModule.ID)
                    .scope(getEventScope())
                    .eventType("RATING_CHANGED")
                    .eventTimestamp(getOccurredTimestamp())
                    .data(RatingFeedData.builder()
                            .ruleId(getRuleId())
                            .previousRating(getPreviousRating())
                            .currentRating(getCurrentRating())
                            .build()
                    ).build());
        }
        return Optional.empty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RatingChangedSignal that = (RatingChangedSignal) o;
        return getRuleId().equals(that.getRuleId()) &&
                getCurrentRating() == that.getCurrentRating() &&
                getPreviousRating() == that.getPreviousRating() &&
                changedAt == that.changedAt &&
                changedEvent.equals(that.changedEvent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getRuleId(), getPreviousRating(), getCurrentRating(), changedAt, changedEvent);
    }

    @Override
    public int compareTo(Signal o) {
        if (o instanceof RatingChangedSignal) {
            return Comparator.comparing(RatingChangedSignal::getRuleId)
                    .thenComparing(Signal::getEventScope)
                    .thenComparing(RatingChangedSignal::getCurrentRating)
                    .thenComparing(RatingChangedSignal::getPreviousRating)
                    .thenComparing(RatingChangedSignal::getChangedAt)
                    .thenComparing(RatingChangedSignal::getChangedEvent)
                    .compare(this, (RatingChangedSignal) o);
        } else {
            return -1;
        }
    }
}
