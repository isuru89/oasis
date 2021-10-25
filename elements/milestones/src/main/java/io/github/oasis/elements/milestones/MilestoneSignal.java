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

package io.github.oasis.elements.milestones;

import io.github.oasis.core.Event;
import io.github.oasis.core.elements.AbstractSink;
import io.github.oasis.core.elements.FeedEntry;
import io.github.oasis.core.elements.Signal;
import io.github.oasis.elements.milestones.spec.MilestoneFeedData;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Isuru Weerarathna
 */
@ToString
public class MilestoneSignal extends Signal {

    private int currentLevel;
    private int previousLevel;
    private BigDecimal currentScore;
    private String causedEvent;

    public MilestoneSignal(String ruleId, int previousLevel, int currentLevel, BigDecimal currentScore,
                           Event causedEvent) {
        super(ruleId, causedEvent.asEventScope(), causedEvent.getTimestamp());

        this.currentLevel = currentLevel;
        this.previousLevel = previousLevel;
        this.currentScore = currentScore;
        this.causedEvent = causedEvent.getExternalId();
    }

    public int getCurrentLevel() {
        return currentLevel;
    }

    public int getPreviousLevel() {
        return previousLevel;
    }

    public BigDecimal getCurrentScore() {
        return currentScore;
    }

    public String getCausedEvent() {
        return causedEvent;
    }

    @Override
    public Class<? extends AbstractSink> sinkHandler() {
        return MilestonesSink.class;
    }

    @Override
    public Optional<FeedEntry> generateFeedEntry() {
        if (currentLevel != previousLevel) {
            return Optional.of(FeedEntry.builder()
                    .byPlugin(MilestonesModule.ID)
                    .eventTimestamp(getOccurredTimestamp())
                    .eventType("MILESTONE_REACHED")
                    .scope(getEventScope())
                    .data(MilestoneFeedData.builder()
                            .ruleId(getRuleId())
                            .currentLevel(currentLevel)
                            .previousLevel(previousLevel)
                            .build()
                    ).build()
            );
        }
        return Optional.empty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MilestoneSignal signal = (MilestoneSignal) o;
        return getRuleId().equals(signal.getRuleId()) &&
                currentLevel == signal.currentLevel &&
                previousLevel == signal.previousLevel &&
                currentScore.equals(signal.currentScore) &&
                causedEvent.equals(signal.causedEvent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getRuleId(), currentLevel, previousLevel, currentScore, causedEvent);
    }

    @Override
    public int compareTo(Signal o) {
        return Comparator.comparing(MilestoneSignal::getRuleId)
                .thenComparing(Signal::getEventScope)
                .thenComparing(MilestoneSignal::getCurrentLevel)
                .thenComparing(MilestoneSignal::getPreviousLevel)
                .thenComparing(MilestoneSignal::getCausedEvent)
                .thenComparing(MilestoneSignal::getCurrentScore)
                .compare(this, (MilestoneSignal) o);
    }
}
