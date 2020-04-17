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

package io.github.oasis.engine.elements.badges;

import io.github.oasis.engine.elements.AbstractRuleTest;
import io.github.oasis.core.elements.Signal;
import io.github.oasis.engine.elements.badges.rules.BadgeHistogramStreakNRule;
import io.github.oasis.engine.elements.badges.signals.HistogramBadgeRemovalSignal;
import io.github.oasis.engine.elements.badges.signals.HistogramBadgeSignal;
import io.github.oasis.engine.factory.Scripting;
import io.github.oasis.core.elements.RuleContext;
import io.github.oasis.engine.model.SingleEventTypeMatcher;
import io.github.oasis.engine.model.TEvent;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * @author Isuru Weerarathna
 */
@DisplayName("Histogram Streaks")
public class HistogramStreakTest extends AbstractRuleTest {

    public static final String EVT_A = "a";
    public static final String EVT_B = "b";
    private static long FIFTY = 50;

    private static final int ATTR_SILVER = 10;
    private static final int ATTR_GOLD = 20;

    private final Map<Integer, Integer> singleStreak = Map.of(3, ATTR_SILVER);
    private final Map<Integer, Integer> multiStreaks = Map.of(3, ATTR_SILVER, 5, ATTR_GOLD);

    @DisplayName("Single streak: No matching event types")
    @Test
    public void testHistogramStreakNNoMatchEvents() {
        TEvent e1 = TEvent.createKeyValue(100, EVT_B, 75);
        TEvent e2 = TEvent.createKeyValue(144, EVT_B, 63);
        TEvent e3 = TEvent.createKeyValue(156, EVT_B, 57);
        TEvent e4 = TEvent.createKeyValue(187, EVT_B, 88);
        TEvent e6 = TEvent.createKeyValue(205, EVT_B, 26);
        TEvent e7 = TEvent.createKeyValue(235, EVT_B, 96);
        TEvent e8 = TEvent.createKeyValue(265, EVT_B, 11);

        List<Signal> signalsRef = new ArrayList<>();
        RuleContext<BadgeHistogramStreakNRule> ruleContext = createOptions(singleStreak, FIFTY, 80, signalsRef::add);
        BadgeHistogramStreakN streakN = new BadgeHistogramStreakN(pool, ruleContext);
        submitOrder(streakN, e1, e2, e3, e4, e6, e7, e8);

        Set<Signal> signals = mergeSignals(signalsRef);
        System.out.println(signals);
        Assertions.assertEquals(0, signals.size());
    }

    @DisplayName("Single streak")
    @Test
    public void testHistogramStreakN() {
        TEvent e1 = TEvent.createKeyValue(100, EVT_A, 75);
        TEvent e2 = TEvent.createKeyValue(144, EVT_A, 63);
        TEvent e3 = TEvent.createKeyValue(156, EVT_A, 57);
        TEvent e4 = TEvent.createKeyValue(187, EVT_A, 88);
        TEvent e6 = TEvent.createKeyValue(205, EVT_A, 26);
        TEvent e7 = TEvent.createKeyValue(235, EVT_A, 96);
        TEvent e8 = TEvent.createKeyValue(265, EVT_A, 11);

        List<Signal> signalsRef = new ArrayList<>();
        RuleContext<BadgeHistogramStreakNRule> ruleContext = createOptions(singleStreak, FIFTY, 80, signalsRef::add);
        BadgeHistogramStreakN streakN = new BadgeHistogramStreakN(pool, ruleContext);
        submitOrder(streakN, e1, e2, e3, e4, e6, e7, e8);

        Set<Signal> signals = mergeSignals(signalsRef);
        System.out.println(signals);
        assertStrict(signals,
                new HistogramBadgeSignal(ruleContext.getRule().getId(), e7, 3, ATTR_SILVER, 100, 200, e7.getExternalId()));
    }

    @DisplayName("Multiple streaks")
    @Test
    public void testHistogramMultiStreakN() {
        TEvent e1 = TEvent.createKeyValue(100, EVT_A, 75);
        TEvent e2 = TEvent.createKeyValue(144, EVT_A, 63);
        TEvent e3 = TEvent.createKeyValue(156, EVT_A, 57);
        TEvent e4 = TEvent.createKeyValue(187, EVT_A, 88);
        TEvent e5 = TEvent.createKeyValue(205, EVT_A, 26);
        TEvent e6 = TEvent.createKeyValue(235, EVT_A, 96);
        TEvent e7 = TEvent.createKeyValue(265, EVT_A, 91);
        TEvent e8 = TEvent.createKeyValue(312, EVT_A, 80);

        List<Signal> signalsRef = new ArrayList<>();
        RuleContext<BadgeHistogramStreakNRule> ruleContext = createOptions(multiStreaks, FIFTY, 80, signalsRef::add);
        BadgeHistogramStreakN streakN = new BadgeHistogramStreakN(pool, ruleContext);
        submitOrder(streakN, e1, e2, e3, e4, e5, e6, e7, e8);

        Set<Signal> signals = mergeSignals(signalsRef);
        System.out.println(signals);
        BadgeHistogramStreakNRule rule = ruleContext.getRule();
        assertStrict(signals,
                new HistogramBadgeSignal(rule.getId(), e6, 3, ATTR_SILVER, 100, 200, e6.getExternalId()),
                new HistogramBadgeSignal(rule.getId(), e8, 5, ATTR_GOLD, 100, 300, e8.getExternalId()));
    }

    @DisplayName("Multiple streaks: Breaks all in multiple streaks and creates a new streak/badge")
    @Test
    public void testBreakHistogramMultiStreakNOutOfOrder() {
        TEvent e1 = TEvent.createKeyValue(100, EVT_A, 75);
        TEvent e2 = TEvent.createKeyValue(144, EVT_A, 63);
        TEvent e3 = TEvent.createKeyValue(156, EVT_A, 57);
        TEvent e4 = TEvent.createKeyValue(187, EVT_A, 88);
        TEvent e5 = TEvent.createKeyValue(205, EVT_A, 26);
        TEvent e6 = TEvent.createKeyValue(235, EVT_A, 96);
        TEvent e7 = TEvent.createKeyValue(265, EVT_A, 91);
        TEvent e8 = TEvent.createKeyValue(312, EVT_A, 80);
        TEvent e9 = TEvent.createKeyValue(170, EVT_A, -88);

        List<Signal> signalsRef = new ArrayList<>();
        RuleContext<BadgeHistogramStreakNRule> ruleContext = createOptions(multiStreaks, FIFTY, 80, signalsRef::add);
        BadgeHistogramStreakN streakN = new BadgeHistogramStreakN(pool, ruleContext);
        submitOrder(streakN, e1, e2, e3, e4, e5, e6, e7, e8, e9);

        Set<Signal> signals = mergeSignals(signalsRef);
        System.out.println(signals);
        BadgeHistogramStreakNRule rule = ruleContext.getRule();
        assertStrict(signals,
                new HistogramBadgeSignal(rule.getId(), e6, 3, ATTR_SILVER, 100, 200, e6.getExternalId()),
                new HistogramBadgeSignal(rule.getId(), e8, 5, ATTR_GOLD, 100, 300, e8.getExternalId()),
                new HistogramBadgeRemovalSignal(rule.getId(), e9.asEventScope(), ATTR_SILVER, 100, 200),
                new HistogramBadgeRemovalSignal(rule.getId(), e9.asEventScope(), ATTR_GOLD, 100, 300),
                new HistogramBadgeSignal(rule.getId(), e8, 3, ATTR_SILVER, 200, 300, e8.getExternalId()));
    }

    @DisplayName("Multiple streaks: Breaks the latest streak in multiple streaks")
    @Test
    public void testBreakHistogramMultiStreakNOutOfOrderLower() {
        TEvent e1 = TEvent.createKeyValue(100, EVT_A, 75);
        TEvent e2 = TEvent.createKeyValue(144, EVT_A, 63);
        TEvent e3 = TEvent.createKeyValue(156, EVT_A, 57);
        TEvent e4 = TEvent.createKeyValue(187, EVT_A, 88);
        TEvent e5 = TEvent.createKeyValue(205, EVT_A, 26);
        TEvent e6 = TEvent.createKeyValue(235, EVT_A, 96);
        TEvent e7 = TEvent.createKeyValue(265, EVT_A, 91);
        TEvent e8 = TEvent.createKeyValue(312, EVT_A, 80);
        TEvent e9 = TEvent.createKeyValue(275, EVT_A, -88);

        List<Signal> signalsRef = new ArrayList<>();
        RuleContext<BadgeHistogramStreakNRule> ruleContext = createOptions(multiStreaks, FIFTY, 80, signalsRef::add);
        BadgeHistogramStreakN streakN = new BadgeHistogramStreakN(pool, ruleContext);
        submitOrder(streakN, e1, e2, e3, e4, e5, e6, e7, e8, e9);

        Set<Signal> signals = mergeSignals(signalsRef);
        System.out.println(signals);
        BadgeHistogramStreakNRule rule = ruleContext.getRule();
        assertStrict(signals,
                new HistogramBadgeSignal(rule.getId(), e6, 3, ATTR_SILVER, 100, 200, e6.getExternalId()),
                new HistogramBadgeSignal(rule.getId(), e8, 5, ATTR_GOLD, 100, 300, e8.getExternalId()),
                new HistogramBadgeRemovalSignal(rule.getId(), e9.asEventScope(), ATTR_GOLD, 100, 300));
    }

    @DisplayName("Multiple streaks: Out-of-order breaks the latest streak in multiple streaks")
    @Test
    public void testBreakHistogramMultiStreakNWithHoles() {
        TEvent e1 = TEvent.createKeyValue(100, EVT_A, 75);
        TEvent e2 = TEvent.createKeyValue(144, EVT_A, 63); // --
        TEvent e3 = TEvent.createKeyValue(156, EVT_A, 57);
        TEvent e4 = TEvent.createKeyValue(187, EVT_A, 88); // --
        TEvent e5 = TEvent.createKeyValue(205, EVT_A, 26);
        TEvent e6 = TEvent.createKeyValue(235, EVT_A, 96); // --
        TEvent e7 = TEvent.createKeyValue(265, EVT_A, 91); // --
        TEvent e9 = TEvent.createKeyValue(170, EVT_A, -88);

        List<Signal> signalsRef = new ArrayList<>();
        RuleContext<BadgeHistogramStreakNRule> ruleContext = createOptions(multiStreaks, FIFTY, 80, signalsRef::add);
        BadgeHistogramStreakN streakN = new BadgeHistogramStreakN(pool, ruleContext);
        submitOrder(streakN, e1, e2, e3, e4, e5, e6, e7, e9);

        Set<Signal> signals = mergeSignals(signalsRef);
        System.out.println(signals);
        BadgeHistogramStreakNRule rule = ruleContext.getRule();
        assertStrict(signals,
                new HistogramBadgeSignal(rule.getId(), e6, 3, ATTR_SILVER, 100, 200, e6.getExternalId()),
                new HistogramBadgeRemovalSignal(rule.getId(), e9.asEventScope(), ATTR_SILVER, 100, 200));
    }

    @DisplayName("Single streak: No streaks available yet")
    @Test
    public void testNoHistogramStreakN() {
        TEvent e1 = TEvent.createKeyValue(100, EVT_A, 75);
        TEvent e2 = TEvent.createKeyValue(144, EVT_A, 63);
        TEvent e3 = TEvent.createKeyValue(156, EVT_A, 7);
        TEvent e4 = TEvent.createKeyValue(187, EVT_A, 18);
        TEvent e6 = TEvent.createKeyValue(205, EVT_A, 26);
        TEvent e7 = TEvent.createKeyValue(235, EVT_A, 96);
        TEvent e8 = TEvent.createKeyValue(265, EVT_A, 71);
        TEvent e9 = TEvent.createKeyValue(285, EVT_A, 21);

        List<Signal> signalsRef = new ArrayList<>();
        RuleContext<BadgeHistogramStreakNRule> ruleContext = createOptions(singleStreak, FIFTY, 80, signalsRef::add);
        BadgeHistogramStreakN streakN = new BadgeHistogramStreakN(pool, ruleContext);
        submitOrder(streakN, e1, e2, e3, e4, e6, e7, e8, e9);

        Set<Signal> signals = mergeSignals(signalsRef);
        System.out.println(signals);
        Assert.assertEquals(0, signals.size());
    }

    @DisplayName("Single streak: No streaks due to non-existence buckets")
    @Test
    public void testNoHistogramStreakNWithHole() {
        TEvent e1 = TEvent.createKeyValue(100, EVT_A, 75);
        TEvent e2 = TEvent.createKeyValue(144, EVT_A, 63);
        TEvent e6 = TEvent.createKeyValue(205, EVT_A, 26);
        TEvent e7 = TEvent.createKeyValue(235, EVT_A, 96);
        TEvent e8 = TEvent.createKeyValue(265, EVT_A, 71);
        TEvent e9 = TEvent.createKeyValue(285, EVT_A, 21);

        List<Signal> signalsRef = new ArrayList<>();
        RuleContext<BadgeHistogramStreakNRule> ruleContext = createOptions(singleStreak, FIFTY, 80, signalsRef::add);
        BadgeHistogramStreakN streakN = new BadgeHistogramStreakN(pool, ruleContext);
        submitOrder(streakN, e1, e2, e6, e7, e8, e9);

        Set<Signal> signals = mergeSignals(signalsRef);
        System.out.println(signals);
        Assert.assertEquals(0, signals.size());
    }

    @DisplayName("Multiple streaks: Out-of-order breaks the latest streak in multiple streaks")
    @Test
    public void testHistogramStreakNOutOfOrder() {
        TEvent e1 = TEvent.createKeyValue(110, EVT_A, 75);
        TEvent e2 = TEvent.createKeyValue(144, EVT_A, 63);
        TEvent e3 = TEvent.createKeyValue(156, EVT_A, 57);
        TEvent e4 = TEvent.createKeyValue(205, EVT_A, 26);
        TEvent e5 = TEvent.createKeyValue(235, EVT_A, 96);
        TEvent e6 = TEvent.createKeyValue(265, EVT_A, 11);
        TEvent e7 = TEvent.createKeyValue(187, EVT_A, 88);

        List<Signal> signalsRef = new ArrayList<>();
        RuleContext<BadgeHistogramStreakNRule> ruleContext = createOptions(singleStreak, FIFTY, 80, signalsRef::add);
        BadgeHistogramStreakN streakN = new BadgeHistogramStreakN(pool, ruleContext);
        submitOrder(streakN, e1, e2, e3, e4, e5, e6, e7);

        Set<Signal> signals = mergeSignals(signalsRef);
        System.out.println(signals);
        Assert.assertEquals(1, signals.size());
        assertStrict(signals,
                new HistogramBadgeSignal(ruleContext.getRule().getId(), e7, 3, ATTR_SILVER, 100, 200, e7.getExternalId()));
    }

    @DisplayName("Single streak: Out-of-order breaks the only single streak")
    @Test
    public void testBreakHistogramStreakNOutOfOrder() {
        TEvent e1 = TEvent.createKeyValue(110, EVT_A, 75);
        TEvent e2 = TEvent.createKeyValue(144, EVT_A, 63);
        TEvent e3 = TEvent.createKeyValue(156, EVT_A, 99);
        TEvent e4 = TEvent.createKeyValue(205, EVT_A, 26);
        TEvent e5 = TEvent.createKeyValue(235, EVT_A, 96);
        TEvent e6 = TEvent.createKeyValue(265, EVT_A, 11);
        TEvent e7 = TEvent.createKeyValue(187, EVT_A, -50);

        List<Signal> signalsRef = new ArrayList<>();
        RuleContext<BadgeHistogramStreakNRule> ruleContext = createOptions(singleStreak, FIFTY, 80, signalsRef::add);
        BadgeHistogramStreakN streakN = new BadgeHistogramStreakN(pool, ruleContext);
        submitOrder(streakN, e1, e2, e3, e4, e5, e6, e7);

        BadgeHistogramStreakNRule rule = ruleContext.getRule();
        Set<Signal> signals = mergeSignals(signalsRef);
        System.out.println(signals);
        assertStrict(signals,
                new HistogramBadgeSignal(rule.getId(), e5, 3, ATTR_SILVER, 100, 200, e5.getExternalId()),
                new HistogramBadgeRemovalSignal(rule.getId(), e7.asEventScope(), ATTR_SILVER, 100, 200));
    }

    private RuleContext<BadgeHistogramStreakNRule> createOptions(Map<Integer, Integer> streaks, long timeunit, long threshold, Consumer<Signal> consumer) {
        BadgeHistogramStreakNRule rule = new BadgeHistogramStreakNRule("test.histogram.streak");
        rule.setEventTypeMatcher(new SingleEventTypeMatcher(EVT_A));
        rule.setStreaks(streaks);
        rule.setConsecutive(true);
        rule.setThreshold(BigDecimal.valueOf(threshold));
        rule.setTimeUnit(timeunit);
        rule.setValueResolver(Scripting.create("e.value", Scripting.CONTEXT_VAR));
        return new RuleContext<>(rule, fromConsumer(consumer));
    }
}
