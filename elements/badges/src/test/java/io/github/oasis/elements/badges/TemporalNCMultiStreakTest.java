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

package io.github.oasis.elements.badges;

import io.github.oasis.core.elements.RuleContext;
import io.github.oasis.core.elements.Signal;
import io.github.oasis.core.elements.matchers.SingleEventTypeMatcher;
import io.github.oasis.elements.badges.rules.BadgeStreakNRule;
import io.github.oasis.elements.badges.rules.BadgeTemporalStreakNRule;
import io.github.oasis.elements.badges.signals.StreakBadgeSignal;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * @author Isuru Weerarathna
 */
@DisplayName("Time based non-consecutive Multi Streaks")
public class TemporalNCMultiStreakTest extends AbstractRuleTest {

    public static final String EVT_A = "a";
    public static final String EVT_B = "b";

    private static final int ATTR_SILVER = 10;
    private static final int ATTR_GOLD = 20;

    private final Map<Integer, Integer> streakMap = Map.of(3, ATTR_SILVER, 5, ATTR_GOLD);

    @DisplayName("Multi streak: No matching event types")
    @Test
    public void testMultiStreakNInTUnitNoEventType() {
        TEvent e1 = TEvent.createKeyValue(100, EVT_B, 75);
        TEvent e2 = TEvent.createKeyValue(110, EVT_B, 63);
        TEvent e3 = TEvent.createKeyValue(120, EVT_B, 50); // --
        TEvent e4 = TEvent.createKeyValue(130, EVT_B, 81);

        List<Signal> signalsRef = new ArrayList<>();
        RuleContext<BadgeStreakNRule> ruleContext = createRule(streakMap, 30, signalsRef::add);
        BadgeStreakNRule rule = ruleContext.getRule();
        Assertions.assertEquals(5, rule.getMaxStreak());
        BadgeStreakN streakN = new BadgeTemporalStreakN(pool, ruleContext);
        submitOrder(streakN, e1, e2, e3, e4);

        Set<Signal> signals = mergeSignals(signalsRef);
        System.out.println(signals);
        Assertions.assertEquals(0, signals.size());
    }

    @DisplayName("Multi streak: only first streak yet")
    @Test
    public void testMultiStreakNInTUnit() {
        TEvent e1 = TEvent.createKeyValue(100, EVT_A, 75);
        TEvent e2 = TEvent.createKeyValue(110, EVT_A, 63);
        TEvent e3 = TEvent.createKeyValue(120, EVT_A, 50); // --
        TEvent e4 = TEvent.createKeyValue(130, EVT_A, 81);

        List<Signal> signalsRef = new ArrayList<>();
        RuleContext<BadgeStreakNRule> ruleContext = createRule(streakMap, 30, signalsRef::add);
        BadgeStreakNRule rule = ruleContext.getRule();
        Assertions.assertEquals(5, rule.getMaxStreak());
        BadgeStreakN streakN = new BadgeTemporalStreakN(pool, ruleContext);
        submitOrder(streakN, e1, e2, e3, e4);

        Set<Signal> signals = mergeSignals(signalsRef);
        System.out.println(signals);
        Assertions.assertEquals(1, signals.size());

        assertSignal(signals, new StreakBadgeSignal(rule.getId(), e3, 3, ATTR_SILVER, 100, 120, e1.getExternalId(), e3.getExternalId()));
    }

    @DisplayName("Multi streak: all streaks found")
    @Test
    public void testMultiStreakAllNInTUnit() {
        TEvent e1 = TEvent.createKeyValue(100, EVT_A, 75);
        TEvent e2 = TEvent.createKeyValue(110, EVT_A, 63);
        TEvent e3 = TEvent.createKeyValue(120, EVT_A, 50); // --
        TEvent e4 = TEvent.createKeyValue(125, EVT_A, 81);
        TEvent e5 = TEvent.createKeyValue(130, EVT_A, 77); // --

        List<Signal> signalsRef = new ArrayList<>();
        RuleContext<BadgeStreakNRule> ruleContext = createRule(streakMap, 30, signalsRef::add);
        BadgeStreakNRule rule = ruleContext.getRule();
        Assertions.assertEquals(5, rule.getMaxStreak());
        BadgeStreakN streakN = new BadgeTemporalStreakN(pool, ruleContext);
        submitOrder(streakN, e1, e2, e3, e4, e5);

        Set<Signal> signals = mergeSignals(signalsRef);
        System.out.println(signals);
        Assertions.assertEquals(2, signals.size());

        assertSignal(signals, new StreakBadgeSignal(rule.getId(), e3, 3, ATTR_SILVER, 100, 120, e1.getExternalId(), e3.getExternalId()));
        assertSignal(signals, new StreakBadgeSignal(rule.getId(), e5, 5, ATTR_GOLD, 100, 130, e1.getExternalId(), e5.getExternalId()));
    }

    @DisplayName("Multi streak: Out-of-order unsatisfying event does not affect first streak")
    @Test
    public void testOutOfOrderNInTNoBreak() {
        TEvent e1 = TEvent.createKeyValue(100, EVT_A, 75);
        TEvent e2 = TEvent.createKeyValue(110, EVT_A, 63);
        TEvent e3 = TEvent.createKeyValue(120, EVT_A, 50); // --
        TEvent e4 = TEvent.createKeyValue(111, EVT_A, 1);

        List<Signal> signalsRef = new ArrayList<>();
        RuleContext<BadgeStreakNRule> ruleContext = createRule(streakMap, 30, signalsRef::add);
        BadgeStreakNRule rule = ruleContext.getRule();
        Assertions.assertEquals(5, rule.getMaxStreak());
        BadgeStreakN streakN = new BadgeTemporalStreakN(pool, ruleContext);
        submitOrder(streakN, e1, e2, e3, e4);

        Set<Signal> signals = mergeSignals(signalsRef);
        System.out.println(signals);
        Assertions.assertEquals(1, signals.size());

        assertSignal(signals, new StreakBadgeSignal(rule.getId(), e3, 3, ATTR_SILVER, 100, 120, e1.getExternalId(), e3.getExternalId()));
    }

    @DisplayName("Multi streak: Out-of-order unsatisfying event does not affect second streak")
    @Test
    public void testOutOfOrderNInTNoBreakSecond() {
        TEvent e1 = TEvent.createKeyValue(100, EVT_A, 75);
        TEvent e2 = TEvent.createKeyValue(110, EVT_A, 63);
        TEvent e3 = TEvent.createKeyValue(120, EVT_A, 50); // --
        TEvent e4 = TEvent.createKeyValue(122, EVT_A, 87);
        TEvent e5 = TEvent.createKeyValue(124, EVT_A, 79); // --
        TEvent e6 = TEvent.createKeyValue(123, EVT_A, 1);

        List<Signal> signalsRef = new ArrayList<>();
        RuleContext<BadgeStreakNRule> ruleContext = createRule(streakMap, 30, signalsRef::add);
        BadgeStreakNRule rule = ruleContext.getRule();
        Assertions.assertEquals(5, rule.getMaxStreak());
        BadgeStreakN streakN = new BadgeTemporalStreakN(pool, ruleContext);
        submitOrder(streakN, e1, e2, e3, e4, e5, e6);

        Set<Signal> signals = mergeSignals(signalsRef);
        System.out.println(signals);
        Assertions.assertEquals(2, signals.size());

        assertSignal(signals, new StreakBadgeSignal(rule.getId(), e3, 3, ATTR_SILVER, 100, 120, e1.getExternalId(), e3.getExternalId()));
        assertSignal(signals, new StreakBadgeSignal(rule.getId(), e5, 5, ATTR_GOLD, 100, 124, e1.getExternalId(), e5.getExternalId()));
    }

    @DisplayName("Multi streak: Out-of-order satisfying event creates a badge")
    @Test
    public void testOutOfOrderNInTCreate() {
        TEvent e1 = TEvent.createKeyValue(100, EVT_A, 75);
        TEvent e2 = TEvent.createKeyValue(110, EVT_A, 63);
        TEvent e3 = TEvent.createKeyValue(120, EVT_A, 10);
        TEvent e4 = TEvent.createKeyValue(111, EVT_A, 91); // --

        List<Signal> signalsRef = new ArrayList<>();
        RuleContext<BadgeStreakNRule> ruleContext = createRule(streakMap, 30, signalsRef::add);
        BadgeStreakNRule rule = ruleContext.getRule();
        Assertions.assertEquals(5, rule.getMaxStreak());
        BadgeStreakN streakN = new BadgeTemporalStreakN(pool, ruleContext);
        submitOrder(streakN, e1, e2, e3, e4);

        Set<Signal> signals = mergeSignals(signalsRef);
        System.out.println(signals);
        Assertions.assertEquals(1, signals.size());

        assertSignal(signals, new StreakBadgeSignal(rule.getId(), e4, 3, ATTR_SILVER, 100, 111, e1.getExternalId(), e4.getExternalId()));
    }

    @DisplayName("Multi streak: Out-of-order satisfying event creates a badge")
    @Test
    public void testOutOfOrderNInTCreateSecondBadge() {
        TEvent e1 = TEvent.createKeyValue(100, EVT_A, 75);
        TEvent e2 = TEvent.createKeyValue(110, EVT_A, 63);
        TEvent e3 = TEvent.createKeyValue(120, EVT_A, 50); // --
        TEvent e4 = TEvent.createKeyValue(122, EVT_A, 87);
        TEvent e5 = TEvent.createKeyValue(124, EVT_A, 1); // --
        TEvent e6 = TEvent.createKeyValue(123, EVT_A, 51);

        List<Signal> signalsRef = new ArrayList<>();
        RuleContext<BadgeStreakNRule> ruleContext = createRule(streakMap, 30, signalsRef::add);
        BadgeStreakNRule rule = ruleContext.getRule();
        Assertions.assertEquals(5, rule.getMaxStreak());
        BadgeStreakN streakN = new BadgeTemporalStreakN(pool, ruleContext);
        submitOrder(streakN, e1, e2, e3, e4, e5, e6);

        Set<Signal> signals = mergeSignals(signalsRef);
        System.out.println(signals);
        Assertions.assertEquals(2, signals.size());

        assertSignal(signals, new StreakBadgeSignal(rule.getId(), e3, 3, ATTR_SILVER, 100, 120, e1.getExternalId(), e3.getExternalId()));
        assertSignal(signals, new StreakBadgeSignal(rule.getId(), e6, 5, ATTR_GOLD, 100, 123, e1.getExternalId(), e6.getExternalId()));
    }

    @DisplayName("Multi streak: Out-of-order satisfying event creates a chained badge")
    @Test
    public void testOutOfOrderNInTCreateChainedBadge() {
        TEvent e1 = TEvent.createKeyValue(100, EVT_A, 75);
        TEvent e2 = TEvent.createKeyValue(110, EVT_A, 63);
        TEvent e3 = TEvent.createKeyValue(120, EVT_A, 1);
        TEvent e4 = TEvent.createKeyValue(122, EVT_A, 87); // --
        TEvent e5 = TEvent.createKeyValue(124, EVT_A, 64);
        TEvent e6 = TEvent.createKeyValue(123, EVT_A, 51); // --

        List<Signal> signalsRef = new ArrayList<>();
        RuleContext<BadgeStreakNRule> ruleContext = createRule(streakMap, 30, signalsRef::add);
        BadgeStreakNRule rule = ruleContext.getRule();
        Assertions.assertEquals(5, rule.getMaxStreak());
        BadgeStreakN streakN = new BadgeTemporalStreakN(pool, ruleContext);
        submitOrder(streakN, e1, e2, e3, e4, e5, e6);

        Set<Signal> signals = mergeSignals(signalsRef);
        System.out.println(signals);
        Assertions.assertEquals(2, signals.size());

        assertSignal(signals, new StreakBadgeSignal(rule.getId(), e4, 3, ATTR_SILVER, 100, 122, e1.getExternalId(), e4.getExternalId()));
        assertSignal(signals, new StreakBadgeSignal(rule.getId(), e5, 5, ATTR_GOLD, 100, 124, e1.getExternalId(), e5.getExternalId()));
    }

    @DisplayName("Multi streak: badge due to non-consecutive events")
    @Test
    public void testNInT() {
        TEvent e1 = TEvent.createKeyValue(100, EVT_A, 75);
        TEvent e2 = TEvent.createKeyValue(104, EVT_A, 63);
        TEvent e3 = TEvent.createKeyValue(108, EVT_A, 11);
        TEvent e4 = TEvent.createKeyValue(112, EVT_A, 50); // --
        TEvent e5 = TEvent.createKeyValue(116, EVT_A, 88);
        TEvent e6 = TEvent.createKeyValue(120, EVT_A, 21);
        TEvent e7 = TEvent.createKeyValue(124, EVT_A, 51); // --
        TEvent e8 = TEvent.createKeyValue(128, EVT_A, 96);

        List<Signal> signalsRef = new ArrayList<>();
        RuleContext<BadgeStreakNRule> ruleContext = createRule(streakMap, 30, signalsRef::add);
        BadgeStreakNRule rule = ruleContext.getRule();
        Assertions.assertEquals(5, rule.getMaxStreak());
        BadgeStreakN streakN = new BadgeTemporalStreakN(pool, ruleContext);
        submitOrder(streakN, e1, e2, e3, e4, e5, e6, e7, e8);

        Set<Signal> signals = mergeSignals(signalsRef);
        System.out.println(signals);
        Assertions.assertEquals(2, signals.size());

        assertSignal(signals, new StreakBadgeSignal(rule.getId(), e1, 3, ATTR_SILVER, 100, 112, e1.getExternalId(), e4.getExternalId()));
        assertSignal(signals, new StreakBadgeSignal(rule.getId(), e7, 5, ATTR_GOLD, 100, 124, e1.getExternalId(), e7.getExternalId()));
    }

    @DisplayName("Multi streak: not within time unit")
    @Test
    public void testNNotInT() {
        TEvent e1 = TEvent.createKeyValue(100, EVT_A, 75);
        TEvent e2 = TEvent.createKeyValue(110, EVT_A, 63);
        TEvent e3 = TEvent.createKeyValue(120, EVT_A, 11);
        TEvent e4 = TEvent.createKeyValue(131, EVT_A, 50);

        List<Signal> signalsRef = new ArrayList<>();
        RuleContext<BadgeStreakNRule> ruleContext = createRule(streakMap, 30, signalsRef::add);
        Assertions.assertEquals(5, ruleContext.getRule().getMaxStreak());
        BadgeStreakN streakN = new BadgeTemporalStreakN(pool, ruleContext);
        submitOrder(streakN, e1, e2, e3, e4);

        Set<Signal> signals = mergeSignals(signalsRef);
        System.out.println(signals);
        Assertions.assertEquals(0, signals.size());
    }

    @DisplayName("Multi streak: two consecutive badge streaks")
    @Test
    public void testNInTManyConsecutive() {
        TEvent e1 = TEvent.createKeyValue(100, EVT_A, 75);
        TEvent e2 = TEvent.createKeyValue(110, EVT_A, 63);
        TEvent e3 = TEvent.createKeyValue(120, EVT_A, 50); // --
        TEvent e4 = TEvent.createKeyValue(125, EVT_A, 81);
        TEvent e5 = TEvent.createKeyValue(130, EVT_A, 77); // --
        TEvent e6 = TEvent.createKeyValue(131, EVT_A, 75);
        TEvent e7 = TEvent.createKeyValue(140, EVT_A, 63);
        TEvent e8 = TEvent.createKeyValue(150, EVT_A, 50); // --
        TEvent e9 = TEvent.createKeyValue(155, EVT_A, 81);
        TEvent e10 = TEvent.createKeyValue(160, EVT_A, 77); // --

        List<Signal> signalsRef = new ArrayList<>();
        RuleContext<BadgeStreakNRule> ruleContext = createRule(streakMap, 30, signalsRef::add);
        BadgeStreakNRule rule = ruleContext.getRule();
        Assertions.assertEquals(5, rule.getMaxStreak());
        BadgeStreakN streakN = new BadgeTemporalStreakN(pool, ruleContext);
        submitOrder(streakN, e1, e2, e3, e4, e5, e6, e7, e8, e9, e10);

        Set<Signal> signals = mergeSignals(signalsRef);
        System.out.println(signals);
        Assertions.assertEquals(4, signals.size());

        assertSignal(signals, new StreakBadgeSignal(rule.getId(), e3, 3, ATTR_SILVER, 100, 120, e1.getExternalId(), e3.getExternalId()));
        assertSignal(signals, new StreakBadgeSignal(rule.getId(), e5, 5, ATTR_GOLD, 100, 130, e1.getExternalId(), e5.getExternalId()));
        assertSignal(signals, new StreakBadgeSignal(rule.getId(), e8, 3, ATTR_SILVER, 131, 150, e6.getExternalId(), e8.getExternalId()));
        assertSignal(signals, new StreakBadgeSignal(rule.getId(), e10, 5, ATTR_GOLD, 131, 160, e6.getExternalId(), e10.getExternalId()));
    }

    private RuleContext<BadgeStreakNRule> createRule(Map<Integer, Integer> streaks, long timeUnit, Consumer<Signal> consumer) {
        BadgeTemporalStreakNRule rule = new BadgeTemporalStreakNRule("test.temporal.streak");
        rule.setEventTypeMatcher(new SingleEventTypeMatcher(EVT_A));
        rule.setStreaks(streaks);
        rule.setConsecutive(false);
        rule.setCriteria((e,r,c) -> (long) e.getFieldValue("value") >= 50);
        rule.setRetainTime(100);
        rule.setTimeUnit(timeUnit);
        return new RuleContext<>(rule, fromConsumer(consumer));
    }


}
