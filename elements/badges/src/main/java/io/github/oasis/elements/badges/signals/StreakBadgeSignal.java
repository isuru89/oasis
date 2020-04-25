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
import io.github.oasis.elements.badges.StreakSupport;

/**
 * @author Isuru Weerarathna
 */
public class StreakBadgeSignal extends BadgeSignal implements StreakSupport {

    private final int streak;

    public StreakBadgeSignal(String ruleId, Event causedEvent, int streak, int attribute, long st, long et, String sid, String eid) {
        super(ruleId, causedEvent, causedEvent.getTimestamp(), attribute, st, et, sid, eid);
        this.streak = streak;
    }

    @Override
    public int getStreak() {
        return streak;
    }
}
