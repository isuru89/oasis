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
import io.github.oasis.core.elements.Signal;
import lombok.ToString;

import java.util.Comparator;

/**
 * @author Isuru Weerarathna
 */
@ToString(callSuper = true)
public class HistogramBadgeSignal extends StreakBadgeSignal {
    public HistogramBadgeSignal(String ruleId, Event causedEvent, int streak, int attribute, long st, long et, String eid) {
        super(ruleId, ruleId, causedEvent, streak, attribute, st, et, null, eid);
    }

    public HistogramBadgeSignal(String ruleId, String badgeId, Event causedEvent, int streak, int attribute, long st, long et, String eid) {
        super(ruleId, badgeId, causedEvent, streak, attribute, st, et, null, eid);
    }

    @Override
    public int compareTo(Signal o) {
        return Comparator
                .comparingLong(BadgeSignal::getStartTime)
                .thenComparing(BadgeSignal::getAttribute)
                .thenComparing(BadgeSignal::getRuleId)
                .thenComparing(BadgeSignal::getBadgeId)
                .thenComparing(Signal::getEventScope)
                .thenComparingLong(BadgeSignal::getEndTime)
                .compare(this, (BadgeSignal) o);
    }
}
