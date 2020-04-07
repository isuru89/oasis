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

package io.github.oasis.engine.rules.signals;

import io.github.oasis.engine.model.EventCreatable;
import io.github.oasis.model.Event;
import io.github.oasis.model.EventScope;
import io.github.oasis.model.events.ChallengePointEvent;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Isuru Weerarathna
 */
@ToString
public class ChallengePointsAwardedSignal extends AbstractChallengeSignal implements EventCreatable {

    private String pointId;
    private BigDecimal points;
    private Event causedEvent;

    public ChallengePointsAwardedSignal(String ruleId, String pointId, BigDecimal points, Event causedEvent) {
        super(ruleId, causedEvent == null ? EventScope.NO_SCOPE : causedEvent.asEventScope());
        this.points = points;
        this.causedEvent = causedEvent;
        this.pointId = pointId;
    }

    public String getPointId() {
        return pointId;
    }

    public BigDecimal getPoints() {
        return points;
    }

    public Event getCausedEvent() {
        return causedEvent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChallengePointsAwardedSignal that = (ChallengePointsAwardedSignal) o;
        return super.equals(o) &&
                Objects.equals(getPointId(), that.getPointId()) &&
                points.equals(that.points) &&
                causedEvent.equals(that.causedEvent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getPointId(), points, causedEvent);
    }

    @Override
    public int compareTo(Signal o) {
        if (o instanceof ChallengePointsAwardedSignal) {
            return Comparator.comparing(ChallengePointsAwardedSignal::getRuleId)
                        .thenComparing(Signal::getEventScope)
                        .thenComparing(ChallengePointsAwardedSignal::getPointId)
                        .thenComparing(ChallengePointsAwardedSignal::getPoints)
                        .thenComparing(o2 -> o2.getCausedEvent().getExternalId())
                        .compare(this, (ChallengePointsAwardedSignal) o);
        } else {
            return -1;
        }
    }

    @Override
    public Optional<Event> generateEvent() {
        if (Objects.nonNull(pointId)) {
            return Optional.of(new ChallengePointEvent(pointId, "points", points, causedEvent));
        } else {
            return Optional.empty();
        }
    }
}
