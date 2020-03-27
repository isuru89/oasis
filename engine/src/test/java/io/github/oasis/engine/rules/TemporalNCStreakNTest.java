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

package io.github.oasis.engine.rules;

import io.github.oasis.engine.rules.signals.BadgeSignal;
import io.github.oasis.engine.rules.signals.Signal;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * @author Isuru Weerarathna
 */
@DisplayName("Time based non-consecutive Single Streak")
public class TemporalNCStreakNTest extends AbstractRuleTest {

    @DisplayName("Single streak: badge due to consecutive events")
    @Test
    public void testStreakNInTUnit() {
        TEvent e1 = TEvent.createKeyValue(100, "a", 75);
        TEvent e2 = TEvent.createKeyValue(110, "a", 63);
        TEvent e3 = TEvent.createKeyValue(120, "a", 50); // --
        TEvent e4 = TEvent.createKeyValue(130, "a", 81);

        List<Signal> signalsRef = new ArrayList<>();
        TemporalStreakNRule options = createStreakNOptions(Collections.singletonList(3), 30, signalsRef::add);
        Assertions.assertEquals(3, options.getMaxStreak());
        StreakN streakN = new TemporalStreakN(pool, options);
        submitOrder(streakN, e1, e2, e3, e4);

        Set<Signal> signals = mergeSignals(signalsRef);
        System.out.println(signals);
        Assertions.assertEquals(1, signals.size());

        assertSignal(signals, new BadgeSignal(options.getId(), 3, 100, 120, e1.getExternalId(), e3.getExternalId()));
    }

    @DisplayName("Single streak: Out-of-order unsatisfying event does not affect")
    @Test
    public void testOutOfOrderNInTNoBreak() {
        TEvent e1 = TEvent.createKeyValue(100, "a", 75);
        TEvent e2 = TEvent.createKeyValue(110, "a", 63);
        TEvent e3 = TEvent.createKeyValue(120, "a", 50); // --
        TEvent e4 = TEvent.createKeyValue(111, "a", 1);

        List<Signal> signalsRef = new ArrayList<>();
        TemporalStreakNRule options = createStreakNOptions(Collections.singletonList(3), 30, signalsRef::add);
        Assertions.assertEquals(3, options.getMaxStreak());
        StreakN streakN = new TemporalStreakN(pool, options);
        submitOrder(streakN, e1, e2, e3, e4);

        Set<Signal> signals = mergeSignals(signalsRef);
        System.out.println(signals);
        Assertions.assertEquals(1, signals.size());

        assertSignal(signals, new BadgeSignal(options.getId(), 3, 100, 120, e1.getExternalId(), e3.getExternalId()));
    }

    @DisplayName("Single streak: Out-of-order satisfying event creates a badge")
    @Test
    public void testOutOfOrderNInTCreate() {
        TEvent e1 = TEvent.createKeyValue(100, "a", 75);
        TEvent e2 = TEvent.createKeyValue(110, "a", 63);
        TEvent e3 = TEvent.createKeyValue(120, "a", 10);
        TEvent e4 = TEvent.createKeyValue(111, "a", 91); // --

        List<Signal> signalsRef = new ArrayList<>();
        TemporalStreakNRule options = createStreakNOptions(Collections.singletonList(3), 30, signalsRef::add);
        Assertions.assertEquals(3, options.getMaxStreak());
        StreakN streakN = new TemporalStreakN(pool, options);
        submitOrder(streakN, e1, e2, e3, e4);

        Set<Signal> signals = mergeSignals(signalsRef);
        System.out.println(signals);
        Assertions.assertEquals(1, signals.size());

        assertSignal(signals, new BadgeSignal(options.getId(), 3, 100, 111, e1.getExternalId(), e4.getExternalId()));
    }

    @DisplayName("Single streak: badge due to non-consecutive events")
    @Test
    public void testNInT() {
        TEvent e1 = TEvent.createKeyValue(100, "a", 75);
        TEvent e2 = TEvent.createKeyValue(110, "a", 63);
        TEvent e3 = TEvent.createKeyValue(120, "a", 11);
        TEvent e4 = TEvent.createKeyValue(130, "a", 50); // --

        List<Signal> signalsRef = new ArrayList<>();
        TemporalStreakNRule options = createStreakNOptions(Collections.singletonList(3), 30, signalsRef::add);
        Assertions.assertEquals(3, options.getMaxStreak());
        StreakN streakN = new TemporalStreakN(pool, options);
        submitOrder(streakN, e1, e2, e3, e4);

        Set<Signal> signals = mergeSignals(signalsRef);
        System.out.println(signals);
        Assertions.assertEquals(1, signals.size());

        assertSignal(signals, new BadgeSignal(options.getId(), 3, 100, 130, e1.getExternalId(), e4.getExternalId()));
    }

    @DisplayName("Single streak: not within time unit")
    @Test
    public void testNNotInT() {
        TEvent e1 = TEvent.createKeyValue(100, "a", 75);
        TEvent e2 = TEvent.createKeyValue(110, "a", 63);
        TEvent e3 = TEvent.createKeyValue(120, "a", 11);
        TEvent e4 = TEvent.createKeyValue(131, "a", 50);

        List<Signal> signalsRef = new ArrayList<>();
        TemporalStreakNRule options = createStreakNOptions(Collections.singletonList(3), 30, signalsRef::add);
        Assertions.assertEquals(3, options.getMaxStreak());
        StreakN streakN = new TemporalStreakN(pool, options);
        submitOrder(streakN, e1, e2, e3, e4);

        Set<Signal> signals = mergeSignals(signalsRef);
        System.out.println(signals);
        Assertions.assertEquals(0, signals.size());
    }

    @DisplayName("Single streak: multiple consecutive badges")
    @Test
    public void testNInTManyConsecutive() {
        TEvent e1 = TEvent.createKeyValue(100, "a", 75);
        TEvent e2 = TEvent.createKeyValue(110, "a", 63);
        TEvent e3 = TEvent.createKeyValue(120, "a", 50); // --
        TEvent e4 = TEvent.createKeyValue(130, "a", 81);
        TEvent e5 = TEvent.createKeyValue(150, "a", 77);
        TEvent e6 = TEvent.createKeyValue(160, "a", 87); // --
        TEvent e7 = TEvent.createKeyValue(170, "a", 11);

        List<Signal> signalsRef = new ArrayList<>();
        TemporalStreakNRule options = createStreakNOptions(Collections.singletonList(3), 30, signalsRef::add);
        Assertions.assertEquals(3, options.getMaxStreak());
        StreakN streakN = new TemporalStreakN(pool, options);
        submitOrder(streakN, e1, e2, e3, e4, e5, e6, e7);

        Set<Signal> signals = mergeSignals(signalsRef);
        System.out.println(signals);
        Assertions.assertEquals(2, signals.size());

        assertSignal(signals, new BadgeSignal(options.getId(), 3, 100, 120, e1.getExternalId(), e3.getExternalId()));
        assertSignal(signals, new BadgeSignal(options.getId(), 3, 130, 160, e4.getExternalId(), e6.getExternalId()));
    }

    private TemporalStreakNRule createStreakNOptions(List<Integer> streaks, long timeUnit, Consumer<Signal> consumer) {
        TemporalStreakNRule options = new TemporalStreakNRule();
        options.setId("abc");
        options.setStreaks(streaks);
        options.setConsecutive(false);
        options.setCondition(val -> val >= 50);
        options.setRetainTime(100);
        options.setCollector(consumer);
        options.setTimeUnit(timeUnit);
        return options;
    }

}
