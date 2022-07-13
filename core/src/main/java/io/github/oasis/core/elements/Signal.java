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

package io.github.oasis.core.elements;

import io.github.oasis.core.Event;
import io.github.oasis.core.EventScope;

import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Isuru Weerarathna
 */
public abstract class Signal implements Comparable<Signal>  {

    private final String ruleId;
    private final EventScope eventScope;
    private long occurredTimestamp;

    protected Signal(String ruleId, Event event, long occurredTimestamp) {
        this(ruleId, event.asEventScope(), occurredTimestamp);
    }

    protected Signal(String ruleId, EventScope eventScope, long occurredTimestamp) {
        this.ruleId = ruleId;
        this.eventScope = eventScope;
        this.occurredTimestamp = occurredTimestamp;
    }

    public abstract Class<? extends AbstractSink> sinkHandler();

    /**
     * Optionally generates a feed event which will be notified to external parties,
     * and not for internal execution.
     *
     * @return feed entry instance or empty reference if such cannot be generated.
     */
    public Optional<FeedEntry> generateFeedEntry() {
        return Optional.empty();
    }

    public String getRuleId() {
        return ruleId;
    }

    public EventScope getEventScope() {
        return eventScope;
    }

    public long getOccurredTimestamp() {
        return occurredTimestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Signal signal = (Signal) o;
        return ruleId.equals(signal.ruleId) &&
                Objects.equals(eventScope, signal.eventScope);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ruleId, eventScope);
    }

    @Override
    public int compareTo(Signal o) {
        return Comparator.comparing(Signal::getRuleId)
                .thenComparing(Signal::getEventScope)
                .compare(this, o);
    }
}
