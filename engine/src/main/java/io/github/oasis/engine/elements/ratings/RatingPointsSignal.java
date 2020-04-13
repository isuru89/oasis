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

package io.github.oasis.engine.elements.ratings;

import io.github.oasis.engine.elements.Signal;
import io.github.oasis.engine.elements.points.PointSignal;
import io.github.oasis.engine.model.EventCreatable;
import io.github.oasis.model.Event;
import io.github.oasis.model.events.RatingPointEvent;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Isuru Weerarathna
 */
@ToString(callSuper = true)
public class RatingPointsSignal extends PointSignal implements EventCreatable {

    private int currentRating;

    public RatingPointsSignal(String ruleId, String pointId, int currentRating, BigDecimal points, Event causedEvent) {
        super(ruleId, pointId, points, causedEvent);
        this.currentRating = currentRating;
    }

    public static RatingPointsSignal create(RatingRule rule, Event causedEvent, int currentRating, String pointId, BigDecimal points) {
        return new RatingPointsSignal(rule.getId(), pointId, currentRating, points, causedEvent);
    }

    public int getCurrentRating() {
        return currentRating;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RatingPointsSignal that = (RatingPointsSignal) o;
        return super.equals(o) &&
                currentRating == that.currentRating;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getCurrentRating());
    }

    @Override
    public int compareTo(Signal o) {
        if (o instanceof RatingPointsSignal) {
            return Comparator.comparing(RatingPointsSignal::getCurrentRating)
                    .thenComparing(PointSignal::getScore)
                    .thenComparing(PointSignal::getPointId)
                    .thenComparing(o2 -> o2.getEventRef().getExternalId())
                    .compare(this, (RatingPointsSignal) o);
        }
        return -1;
    }

    @Override
    public Optional<Event> generateEvent() {
        if (Objects.nonNull(getPointId())) {
            return Optional.of(new RatingPointEvent(getPointId(), "points", getScore(), getEventRef()));
        } else {
            return Optional.empty();
        }
    }
}
