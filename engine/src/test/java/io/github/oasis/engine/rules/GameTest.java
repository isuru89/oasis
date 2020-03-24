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

import io.github.oasis.engine.rules.signals.BadgeRemoveSignal;
import io.github.oasis.engine.rules.signals.BadgeSignal;
import io.github.oasis.engine.rules.signals.Signal;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * @author Isuru Weerarathna
 */
public class GameTest extends AbstractRuleTest {

    @Test
    public void testNotEnoughStreakN() {
        TEvent e1 = TEvent.createKeyValue(100, "a", 75);
        TEvent e2 = TEvent.createKeyValue(104, "a", 63);

        List<Signal> signals = new ArrayList<>();
        StreakNRule options = new StreakNRule();
        options.setId("abc");
        options.setStreaks(Collections.singletonList(3));
        options.setCondition(val -> val >= 50);
        options.setRetainTime(10);
        options.setCollector(signals::add);
        Assert.assertEquals(3, options.getMaxStreak());
        StreakN streakN = new StreakN(pool, options);
        submitOrder(streakN, e1, e2);

        System.out.println(signals);
        Assert.assertEquals(0, signals.size());
    }

    @Test
    public void testNoStreakN() {
        TEvent e1 = TEvent.createKeyValue(100, "a", 75);
        TEvent e2 = TEvent.createKeyValue(104, "a", 63);
        TEvent e3 = TEvent.createKeyValue(105, "a", 47);
        TEvent e4 = TEvent.createKeyValue(106, "a", 88);

        List<Signal> signals = new ArrayList<>();
        StreakNRule options = new StreakNRule();
        options.setId("abc");
        options.setStreaks(Collections.singletonList(3));
        options.setCondition(val -> val >= 50);
        options.setRetainTime(10);
        options.setCollector(signals::add);
        Assert.assertEquals(3, options.getMaxStreak());
        StreakN streakN = new StreakN(pool, options);
        submitOrder(streakN, e1, e2, e3, e4);

        System.out.println(signals);
        Assert.assertEquals(0, signals.size());
    }

    @Test
    public void testOrderedStreakN() {
        TEvent e1 = TEvent.createKeyValue(100, "a", 75);
        TEvent e2 = TEvent.createKeyValue(104, "a", 63);
        TEvent e3 = TEvent.createKeyValue(105, "a", 50);
        TEvent e4 = TEvent.createKeyValue(106, "a", 21);

        List<Signal> signals = new ArrayList<>();
        StreakNRule options = new StreakNRule();
        options.setId("abc");
        options.setStreaks(Collections.singletonList(3));
        options.setCondition(val -> val >= 50);
        options.setRetainTime(10);
        options.setCollector(signals::add);
        Assert.assertEquals(3, options.getMaxStreak());
        StreakN streakN = new StreakN(pool, options);
        submitOrder(streakN, e1, e2, e3, e4);

        Assert.assertEquals(1, signals.size());

        assertSignal(signals, new BadgeSignal(options.getId(), 3, 100, 105, e1.getExternalId(), e3.getExternalId()));
    }

    @Test
    public void testOutOfOrderMisMatchStreakN() {
        TEvent e1 = TEvent.createKeyValue(100, "a", 75);
        TEvent e2 = TEvent.createKeyValue(104, "a", 63);
        TEvent e3 = TEvent.createKeyValue(105, "a", 55);
        TEvent e4 = TEvent.createKeyValue(101, "a", 11);
        TEvent e5 = TEvent.createKeyValue(106, "a", 21);

        List<Signal> signals = new ArrayList<>();
        StreakNRule options = new StreakNRule();
        options.setId("abc");
        options.setStreaks(Collections.singletonList(3));
        options.setCondition(val -> val >= 50);
        options.setRetainTime(10);
        options.setCollector(signals::add);
        Assert.assertEquals(3, options.getMaxStreak());
        StreakN streakN = new StreakN(pool, options);
        submitOrder(streakN, e1, e2, e3, e4, e5);

        System.out.println(signals);
        Assert.assertEquals(2, signals.size());

        assertSignal(signals, new BadgeSignal(options.getId(), 3, 100, 105, e1.getExternalId(), e3.getExternalId()));
        assertSignal(signals, new BadgeRemoveSignal(options.getId(), 3, 100, 105, e1.getExternalId(), e3.getExternalId()));
    }

    @Test
    public void testOutOfOrderMisMatchNotAffectedStreakN() {
        TEvent e1 = TEvent.createKeyValue(100, "a", 75);
        TEvent e2 = TEvent.createKeyValue(104, "a", 63);
        TEvent e3 = TEvent.createKeyValue(105, "a", 55);
        TEvent e4 = TEvent.createKeyValue(107, "a", 11);
        TEvent e5 = TEvent.createKeyValue(106, "a", 21);

        List<Signal> signals = new ArrayList<>();
        StreakNRule options = new StreakNRule();
        options.setId("abc");
        options.setStreaks(Collections.singletonList(3));
        options.setCondition(val -> val >= 50);
        options.setRetainTime(10);
        options.setCollector(signals::add);
        Assert.assertEquals(3, options.getMaxStreak());
        StreakN streakN = new StreakN(pool, options);
        submitOrder(streakN, e1, e2, e3, e4, e5);

        System.out.println(signals);
        Assert.assertEquals(1, signals.size());
        assertSignal(signals, new BadgeSignal(options.getId(), 3, 100, 105, e1.getExternalId(), e3.getExternalId()));
    }

    @Test
    public void testOutOfOrderMisMatchNotAffectedBeforeAllStreakN() {
        TEvent e1 = TEvent.createKeyValue(100, "a", 75);
        TEvent e2 = TEvent.createKeyValue(104, "a", 63);
        TEvent e3 = TEvent.createKeyValue(105, "a", 55);
        TEvent e4 = TEvent.createKeyValue(99, "a", 11);
        TEvent e5 = TEvent.createKeyValue(106, "a", 21);

        List<Signal> signals = new ArrayList<>();
        StreakNRule options = createStreakNOptions(Collections.singletonList(3), signals::add);
        Assert.assertEquals(3, options.getMaxStreak());
        StreakN streakN = new StreakN(pool, options);
        submitOrder(streakN, e1, e2, e3, e4, e5);

        System.out.println(signals);
        Assert.assertEquals(1, signals.size());
        assertSignal(signals, new BadgeSignal(options.getId(), 3, 100, 105, e1.getExternalId(), e3.getExternalId()));
    }

    @Test
    public void testOutOfOrderMatchStreakN() {
        TEvent e1 = TEvent.createKeyValue(100, "a", 75);
        TEvent e2 = TEvent.createKeyValue(104, "a", 63);
        TEvent e3 = TEvent.createKeyValue(105, "a", 20);
        TEvent e4 = TEvent.createKeyValue(101, "a", 81);
        TEvent e5 = TEvent.createKeyValue(106, "a", 21);

        List<Signal> signals = new ArrayList<>();
        StreakNRule options = createStreakNOptions(Collections.singletonList(3), signals::add);
        Assert.assertEquals(3, options.getMaxStreak());
        StreakN streakN = new StreakN(pool, options);
        submitOrder(streakN, e1, e2, e3, e4, e5);

        Assert.assertEquals(1, signals.size());
        assertSignal(signals, new BadgeSignal(options.getId(), 3, 100, 104, e1.getExternalId(), e2.getExternalId()));
    }

    @Test
    public void testStreakN() {
        TEvent e1 = TEvent.createKeyValue(100, "a", 75);
        TEvent e2 = TEvent.createKeyValue(104, "a", 63);
        TEvent e3 = TEvent.createKeyValue(105, "a", 50);
        TEvent e4 = TEvent.createKeyValue(101, "a", 81);
        TEvent e5 = TEvent.createKeyValue(106, "a", 21);

        List<Signal> signals = new ArrayList<>();
        StreakNRule options = createStreakNOptions(Collections.singletonList(3), signals::add);
        Assert.assertEquals(3, options.getMaxStreak());
        StreakN streakN = new StreakN(pool, options);
        submitOrder(streakN, e1, e2, e3, e4, e5);

        System.out.println(signals);
        Assert.assertEquals(2, signals.size());

        assertSignal(signals, new BadgeSignal(options.getId(), 3, 100, 105, e1.getExternalId(), e3.getExternalId()));
        assertSignal(signals, new BadgeSignal(options.getId(), 3, 100, 104, e1.getExternalId(), e2.getExternalId()));
    }

    // ---------------------------------------
    // MULTI STREAK TESTS
    // ---------------------------------------

    @Test
    public void testMultiStreakN() {
        TEvent e1 = TEvent.createKeyValue(100, "a", 75);
        TEvent e2 = TEvent.createKeyValue(104, "a", 63);
        TEvent e3 = TEvent.createKeyValue(105, "a", 57);
        TEvent e4 = TEvent.createKeyValue(106, "a", 88);
        TEvent e5 = TEvent.createKeyValue(107, "a", 76);

        List<Signal> signalsRef = new ArrayList<>();
        StreakNRule options = createStreakNOptions(Arrays.asList(3, 5), signalsRef::add);
        Assert.assertEquals(5, options.getMaxStreak());
        StreakN streakN = new StreakN(pool, options);
        submitOrder(streakN, e1, e2, e3, e4, e5);

        Set<Signal> signals = mergeSignals(signalsRef);
        System.out.println(signals);
        Assert.assertEquals(2, signals.size());

        assertSignal(signals, new BadgeSignal(options.getId(), 3, 100, 105, e1.getExternalId(), e3.getExternalId()));
        assertSignal(signals, new BadgeSignal(options.getId(), 5, 100, 107, e1.getExternalId(), e5.getExternalId()));
    }

    @Test
    public void testOutOfOrderMultiStreakN() {
        TEvent e1 = TEvent.createKeyValue(100, "a", 75);
        TEvent e2 = TEvent.createKeyValue(104, "a", 63);
        TEvent e3 = TEvent.createKeyValue(105, "a", 57);
        TEvent e4 = TEvent.createKeyValue(106, "a", 88);
        TEvent e5 = TEvent.createKeyValue(101, "a", 76);
        TEvent e6 = TEvent.createKeyValue(107, "a", 26);

        List<Signal> signalsRef = new ArrayList<>();
        StreakNRule options = createStreakNOptions(Arrays.asList(3, 5), signalsRef::add);
        Assert.assertEquals(5, options.getMaxStreak());
        StreakN streakN = new StreakN(pool, options);
        submitOrder(streakN, e1, e2, e3, e4, e5, e6);

        Set<Signal> signals = mergeSignals(signalsRef);
        System.out.println(signals);
        Assert.assertEquals(2, signals.size());

        assertSignal(signals, new BadgeSignal(options.getId(), 3, 100, 104, e1.getExternalId(), e2.getExternalId()));
        assertSignal(signals, new BadgeSignal(options.getId(), 5, 100, 106, e1.getExternalId(), e4.getExternalId()));
    }

    @Test
    public void testOutOfOrderBreakMultiStreakN() {
        TEvent e1 = TEvent.createKeyValue(100, "a", 75);
        TEvent e2 = TEvent.createKeyValue(104, "a", 63);
        TEvent e3 = TEvent.createKeyValue(105, "a", 57);
        TEvent e4 = TEvent.createKeyValue(106, "a", 88);
        TEvent e5 = TEvent.createKeyValue(107, "a", 76);
        TEvent e6 = TEvent.createKeyValue(101, "a", 26);

        List<Signal> signalsRef = new ArrayList<>();
        StreakNRule options = createStreakNOptions(Arrays.asList(3, 5), signalsRef::add);
        Assert.assertEquals(5, options.getMaxStreak());
        StreakN streakN = new StreakN(pool, options);
        submitOrder(streakN, e1, e2, e3, e4, e5, e6);

        Set<Signal> signals = mergeSignals(signalsRef);
        System.out.println(signals);
        Assert.assertEquals(5, signals.size());

        assertSignal(signals, new BadgeSignal(options.getId(), 3, 100, 105, e1.getExternalId(), e3.getExternalId()));
        assertSignal(signals, new BadgeSignal(options.getId(), 5, 100, 107, e1.getExternalId(), e5.getExternalId()));
        assertSignal(signals, new BadgeRemoveSignal(options.getId(), 3, 100, 105, e1.getExternalId(), e3.getExternalId()));
        assertSignal(signals, new BadgeRemoveSignal(options.getId(), 5, 100, 107, e1.getExternalId(), e5.getExternalId()));
        assertSignal(signals, new BadgeSignal(options.getId(), 3, 104, 106, e2.getExternalId(), e4.getExternalId()));
    }

    @Test
    public void testOutOfOrderBreakAllMultiStreakN() {
        TEvent e1 = TEvent.createKeyValue(100, "a", 75);
        TEvent e2 = TEvent.createKeyValue(104, "a", 63);
        TEvent e3 = TEvent.createKeyValue(106, "a", 57);
        TEvent e4 = TEvent.createKeyValue(107, "a", 88);
        TEvent e6 = TEvent.createKeyValue(105, "a", 26);

        List<Signal> signalsRef = new ArrayList<>();
        StreakNRule options = createStreakNOptions(Arrays.asList(3, 5), signalsRef::add);
        Assert.assertEquals(5, options.getMaxStreak());
        StreakN streakN = new StreakN(pool, options);
        submitOrder(streakN, e1, e2, e3, e4, e6);

        Set<Signal> signals = mergeSignals(signalsRef);
        System.out.println(signals);
        Assert.assertEquals(2, signals.size());

        assertSignal(signals, new BadgeSignal(options.getId(), 3, 100, 106, e1.getExternalId(), e3.getExternalId()));
        assertSignal(signals, new BadgeRemoveSignal(options.getId(), 3, 100, 106, e1.getExternalId(), e3.getExternalId()));
    }

    private StreakNRule createStreakNOptions(List<Integer> streaks, Consumer<Signal> consumer) {
        StreakNRule options = new StreakNRule();
        options.setId("abc");
        options.setStreaks(streaks);
        options.setCondition(val -> val >= 50);
        options.setRetainTime(10);
        options.setCollector(consumer);
        return options;
    }
}
