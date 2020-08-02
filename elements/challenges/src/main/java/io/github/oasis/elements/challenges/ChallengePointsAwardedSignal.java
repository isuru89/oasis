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

package io.github.oasis.elements.challenges;

import io.github.oasis.core.Event;
import io.github.oasis.core.elements.EventCreatable;
import io.github.oasis.engine.element.points.PointSignal;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Isuru Weerarathna
 */
public class ChallengePointsAwardedSignal extends PointSignal implements EventCreatable {

    public ChallengePointsAwardedSignal(String ruleId, String pointId, BigDecimal points, Event causedEvent) {
        super(ruleId, pointId, points, causedEvent);
    }

    @Override
    public String toString() {
        return "ChallengePointsAwardedSignal{" +
                "pointId='" + getPointId() + '\'' +
                ", score=" + getScore() +
                ", eventRef=" + getEventRef() +
                '}';
    }

    @Override
    public Optional<Event> generateEvent() {
        if (Objects.nonNull(getPointId())) {
            return Optional.of(new ChallengePointEvent(getPointId(), getScore(), getEventRef()));
        } else {
            return Optional.empty();
        }
    }
}
