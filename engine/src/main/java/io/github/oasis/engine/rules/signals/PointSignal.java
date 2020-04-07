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

import io.github.oasis.model.Event;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Objects;

/**
 * @author Isuru Weerarathna
 */
@ToString
public class PointSignal extends Signal {

    private BigDecimal score;
    private Event eventRef;

    public PointSignal(String ruleId, BigDecimal score, Event eventRef) {
        super(ruleId, eventRef.asEventScope());

        this.score = score;
        this.eventRef = eventRef;
    }

    public BigDecimal getScore() {
        return score;
    }

    public Event getEventRef() {
        return eventRef;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PointSignal that = (PointSignal) o;
        return super.equals(o) &&
                score.equals(that.score) &&
                eventRef.equals(that.eventRef);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getRuleId(), score, eventRef);
    }

    @Override
    public int compareTo(Signal o) {
        return Comparator
                .comparing(PointSignal::getRuleId)
                .thenComparing(PointSignal::getEventScope)
                .thenComparing(PointSignal::getScore)
                .thenComparing(o2 -> o2.getEventRef().getExternalId())
                .thenComparing(o2 -> o2.getEventRef().getUser())
                .compare(this, (PointSignal) o);
    }
}
