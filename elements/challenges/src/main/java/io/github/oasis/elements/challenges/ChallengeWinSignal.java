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
import io.github.oasis.core.elements.Signal;
import lombok.ToString;

import java.util.Comparator;
import java.util.Objects;

/**
 * @author Isuru Weerarathna
 */
@ToString
public class ChallengeWinSignal extends AbstractChallengeSignal {

    private int position;
    private long winnerUserId;
    private long wonAt;
    private String wonEventId;

    public ChallengeWinSignal(String ruleId, Event event, int position, long winnerUserId, long wonAt, String wonEventId) {
        super(ruleId, event.asEventScope(), wonAt);
        this.position = position;
        this.winnerUserId = winnerUserId;
        this.wonAt = wonAt;
        this.wonEventId = wonEventId;
    }

    public int getPosition() {
        return position;
    }

    public long getWinnerUserId() {
        return winnerUserId;
    }

    public long getWonAt() {
        return wonAt;
    }

    public String getWonEventId() {
        return wonEventId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChallengeWinSignal that = (ChallengeWinSignal) o;
        return getRuleId().equals(((ChallengeWinSignal) o).getRuleId()) &&
                position == that.position &&
                winnerUserId == that.winnerUserId &&
                wonAt == that.wonAt &&
                wonEventId.equals(that.wonEventId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getRuleId(), position, winnerUserId, wonAt, wonEventId);
    }

    @Override
    public int compareTo(Signal o) {
        if (o instanceof ChallengeWinSignal) {
            return Comparator.comparing(ChallengeWinSignal::getRuleId)
                    .thenComparing(Signal::getEventScope)
                    .thenComparing(ChallengeWinSignal::getPosition)
                    .thenComparing(ChallengeWinSignal::getWinnerUserId)
                    .thenComparing(ChallengeWinSignal::getWonAt)
                    .thenComparing(ChallengeWinSignal::getWonEventId)
                    .compare(this, (ChallengeWinSignal) o);
        } else {
            return -1;
        }
    }
}
