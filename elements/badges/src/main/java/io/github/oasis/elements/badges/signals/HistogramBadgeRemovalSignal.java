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

import io.github.oasis.core.EventScope;
import io.github.oasis.core.elements.Signal;

import java.util.Comparator;

/**
 * @author Isuru Weerarathna
 */
public class HistogramBadgeRemovalSignal extends BadgeRemoveSignal {
    public HistogramBadgeRemovalSignal(BadgeSignal prevBadge) {
        this(prevBadge.getRuleId(), prevBadge.getEventScope(), prevBadge.getAttribute(), prevBadge.getStartTime(), prevBadge.getEndTime());
    }

    public HistogramBadgeRemovalSignal(String ruleId, EventScope eventScope, int streak, long st, long et) {
        super(ruleId, eventScope, streak, st, et, null, null);
    }

    @Override
    public int compareTo(Signal o) {
        return Comparator
                .comparingLong(BadgeSignal::getStartTime)
                .thenComparing(BadgeSignal::getEndTime)
                .thenComparing(BadgeSignal::getAttribute)
                .thenComparing(BadgeSignal::getRuleId)
                .thenComparing(Signal::getEventScope)
                .compare(this, (BadgeSignal) o);
    }
}
