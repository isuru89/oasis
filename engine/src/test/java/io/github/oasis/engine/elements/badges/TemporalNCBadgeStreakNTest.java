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

import io.github.oasis.elements.badges.BadgeStreakN;
import io.github.oasis.elements.badges.BadgeTemporalStreakN;
import io.github.oasis.engine.elements.AbstractRuleTest;
import io.github.oasis.core.elements.Signal;
import io.github.oasis.elements.badges.rules.BadgeStreakNRule;
import io.github.oasis.elements.badges.rules.BadgeTemporalStreakNRule;
import io.github.oasis.elements.badges.signals.StreakBadgeSignal;
import io.github.oasis.core.elements.RuleContext;
import io.github.oasis.engine.model.SingleEventTypeMatcher;
import io.github.oasis.engine.model.TEvent;
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
@DisplayName("Time based non-consecutive Single Streak")
public class TemporalNCBadgeStreakNTest extends AbstractRuleTest {

    private static final String EVT_A = "a";
    private static final String EVT_B = "b";

    private static final int ATTR_SILVER = 10;

    private final Map<Integer, Integer> streakMap = Map.of(3, ATTR_SILVER);

    @DisplayName("Single streak: No matching event types")
    @Test
    public void testStreakNInTUnitNoEventType() {
        TEvent e1 = TEvent.createKeyValue(100, EVT_B, 75);
        TEvent e2 = TEvent.createKeyValue(110, EVT_B, 63);
        TEvent e3 = TEvent.createKeyValue(120, EVT_B, 50); // --
        TEvent e4 = TEvent.createKeyValue(130, EVT_B, 81);

        List<Signal> signalsRef = new ArrayList<>();
        RuleContext<BadgeStreakNRule> ruleContext = createRule(streakMap, 30, signalsRef::add);
        Assertions.assertEquals(3, ruleContext.getRule().getMaxStreak());
        BadgeStreakN streakN = new BadgeTemporalStreakN(pool, ruleContext);
        submitOrder(streakN, e1, e2, e3, e4);

        Set<Signal> signals = mergeSignals(signalsRef);
        System.out.println(signals);
        Assertions.assertEquals(0, signals.size());
    }

    @DisplayName("Single streak: badge due to consecutive events")
    @Test
    public void testStreakNInTUnit() {
        TEvent e1 = TEvent.createKeyValue(100, EVT_A, 75);
        TEvent e2 = TEvent.createKeyValue(110, EVT_A, 63);
        TEvent e3 = TEvent.createKeyValue(120, EVT_A, 50); // --
        TEvent e4 = TEvent.createKeyValue(130, EVT_A, 81);

        List<Signal> signalsRef = new ArrayList<>();
        RuleContext<BadgeStreakNRule> ruleContext = createRule(streakMap, 30, signalsRef::add);
        Assertions.assertEquals(3, ruleContext.getRule().getMaxStreak());
        BadgeStreakN streakN = new BadgeTemporalStreakN(pool, ruleContext);
        submitOrder(streakN, e1, e2, e3, e4);

        Set<Signal> signals = mergeSignals(signalsRef);
        System.out.println(signals);
        Assertions.assertEquals(1, signals.size());

        assertSignal(signals, new StreakBadgeSignal(ruleContext.getRule().getId(), e3, 3, ATTR_SILVER, 100, 120, e1.getExternalId(), e3.getExternalId()));
    }

    @DisplayName("Single streak: Out-of-order unsatisfying event does not affect")
    @Test
    public void testOutOfOrderNInTNoBreak() {
        TEvent e1 = TEvent.createKeyValue(100, EVT_A, 75);
        TEvent e2 = TEvent.createKeyValue(110, EVT_A, 63);
        TEvent e3 = TEvent.createKeyValue(120, EVT_A, 50); // --
        TEvent e4 = TEvent.createKeyValue(111, EVT_A, 1);

        List<Signal> signalsRef = new ArrayList<>();
        RuleContext<BadgeStreakNRule> ruleContext = createRule(streakMap, 30, signalsRef::add);
        Assertions.assertEquals(3, ruleContext.getRule().getMaxStreak());
        BadgeStreakN streakN = new BadgeTemporalStreakN(pool, ruleContext);
        submitOrder(streakN, e1, e2, e3, e4);

        Set<Signal> signals = mergeSignals(signalsRef);
        System.out.println(signals);
        Assertions.assertEquals(1, signals.size());

        assertSignal(signals, new StreakBadgeSignal(ruleContext.getRule().getId(), e3, 3, ATTR_SILVER, 100, 120, e1.getExternalId(), e3.getExternalId()));
    }

    @DisplayName("Single streak: Out-of-order satisfying event creates a badge")
    @Test
    public void testOutOfOrderNInTCreate() {
        TEvent e1 = TEvent.createKeyValue(100, EVT_A, 75);
        TEvent e2 = TEvent.createKeyValue(110, EVT_A, 63);
        TEvent e3 = TEvent.createKeyValue(120, EVT_A, 10);
        TEvent e4 = TEvent.createKeyValue(111, EVT_A, 91); // --

        List<Signal> signalsRef = new ArrayList<>();
        RuleContext<BadgeStreakNRule> ruleContext = createRule(streakMap, 30, signalsRef::add);
        Assertions.assertEquals(3, ruleContext.getRule().getMaxStreak());
        BadgeStreakN streakN = new BadgeTemporalStreakN(pool, ruleContext);
        submitOrder(streakN, e1, e2, e3, e4);

        Set<Signal> signals = mergeSignals(signalsRef);
        System.out.println(signals);
        Assertions.assertEquals(1, signals.size());

        assertSignal(signals, new StreakBadgeSignal(ruleContext.getRule().getId(), e4, 3, ATTR_SILVER, 100, 111, e1.getExternalId(), e4.getExternalId()));
    }

    @DisplayName("Single streak: badge due to non-consecutive events")
    @Test
    public void testNInT() {
        TEvent e1 = TEvent.createKeyValue(100, EVT_A, 75);
        TEvent e2 = TEvent.createKeyValue(110, EVT_A, 63);
        TEvent e3 = TEvent.createKeyValue(120, EVT_A, 11);
        TEvent e4 = TEvent.createKeyValue(130, EVT_A, 50); // --

        List<Signal> signalsRef = new ArrayList<>();
        RuleContext<BadgeStreakNRule> ruleContext = createRule(streakMap, 30, signalsRef::add);
        Assertions.assertEquals(3, ruleContext.getRule().getMaxStreak());
        BadgeStreakN streakN = new BadgeTemporalStreakN(pool, ruleContext);
        submitOrder(streakN, e1, e2, e3, e4);

        Set<Signal> signals = mergeSignals(signalsRef);
        System.out.println(signals);
        Assertions.assertEquals(1, signals.size());

        assertSignal(signals, new StreakBadgeSignal(ruleContext.getRule().getId(), e4, 3, ATTR_SILVER, 100, 130, e1.getExternalId(), e4.getExternalId()));
    }

    @DisplayName("Single streak: not within time unit")
    @Test
    public void testNNotInT() {
        TEvent e1 = TEvent.createKeyValue(100, EVT_A, 75);
        TEvent e2 = TEvent.createKeyValue(110, EVT_A, 63);
        TEvent e3 = TEvent.createKeyValue(120, EVT_A, 11);
        TEvent e4 = TEvent.createKeyValue(131, EVT_A, 50);

        List<Signal> signalsRef = new ArrayList<>();
        RuleContext<BadgeStreakNRule> ruleContext = createRule(streakMap, 30, signalsRef::add);
        Assertions.assertEquals(3, ruleContext.getRule().getMaxStreak());
        BadgeStreakN streakN = new BadgeTemporalStreakN(pool, ruleContext);
        submitOrder(streakN, e1, e2, e3, e4);

        Set<Signal> signals = mergeSignals(signalsRef);
        System.out.println(signals);
        Assertions.assertEquals(0, signals.size());
    }

    @DisplayName("Single streak: multiple consecutive badges")
    @Test
    public void testNInTManyConsecutive() {
        TEvent e1 = TEvent.createKeyValue(100, EVT_A, 75);
        TEvent e2 = TEvent.createKeyValue(110, EVT_A, 63);
        TEvent e3 = TEvent.createKeyValue(120, EVT_A, 50); // --
        TEvent e4 = TEvent.createKeyValue(130, EVT_A, 81);
        TEvent e5 = TEvent.createKeyValue(150, EVT_A, 77);
        TEvent e6 = TEvent.createKeyValue(160, EVT_A, 87); // --
        TEvent e7 = TEvent.createKeyValue(170, EVT_A, 11);

        List<Signal> signalsRef = new ArrayList<>();
        RuleContext<BadgeStreakNRule> ruleContext = createRule(streakMap, 30, signalsRef::add);
        BadgeStreakNRule rule = ruleContext.getRule();
        Assertions.assertEquals(3, rule.getMaxStreak());
        BadgeStreakN streakN = new BadgeTemporalStreakN(pool, ruleContext);
        submitOrder(streakN, e1, e2, e3, e4, e5, e6, e7);

        Set<Signal> signals = mergeSignals(signalsRef);
        System.out.println(signals);
        Assertions.assertEquals(2, signals.size());

        assertStrict(signals,
                new StreakBadgeSignal(rule.getId(), e3, 3, ATTR_SILVER, 100, 120, e1.getExternalId(), e3.getExternalId()),
                new StreakBadgeSignal(rule.getId(), e6, 3, ATTR_SILVER, 130, 160, e4.getExternalId(), e6.getExternalId()));
    }

    private RuleContext<BadgeStreakNRule> createRule(Map<Integer, Integer> streaks, long timeUnit, Consumer<Signal> consumer) {
        BadgeTemporalStreakNRule rule = new BadgeTemporalStreakNRule("test.histogram.streak");
        rule.setEventTypeMatcher(new SingleEventTypeMatcher(EVT_A));
        rule.setStreaks(streaks);
        rule.setConsecutive(false);
        rule.setCriteria((e,r,c) -> (long) e.getFieldValue("value") >= 50);
        rule.setRetainTime(100);
        rule.setTimeUnit(timeUnit);
        return new RuleContext<>(rule, fromConsumer(consumer));
    }

}
