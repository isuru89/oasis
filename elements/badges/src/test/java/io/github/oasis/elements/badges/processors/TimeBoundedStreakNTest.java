/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.github.oasis.elements.badges.processors;

import io.github.oasis.core.elements.RuleContext;
import io.github.oasis.core.elements.Signal;
import io.github.oasis.core.elements.matchers.SingleEventTypeMatcher;
import io.github.oasis.elements.badges.TEvent;
import io.github.oasis.elements.badges.rules.StreakNBadgeRule;
import io.github.oasis.elements.badges.rules.TimeBoundedStreakNRule;
import io.github.oasis.elements.badges.signals.BadgeRemoveSignal;
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
@DisplayName("Time bounded Streaks")
public class TimeBoundedStreakNTest extends AbstractRuleTest {

    public static final String EVENT_TYPE = "event.a";
    public static final String EVENT_B = "event.b";

    private static final int ATTR_SILVER = 10;
    private static final int ATTR_GOLD = 20;

    private final Map<Integer, Integer> multiStreak = Map.of(3, ATTR_SILVER, 5, ATTR_GOLD);
    private final Map<Integer, Integer> singleStreak = Map.of(3, ATTR_SILVER);

    @DisplayName("Multi streaks: No matching event types")
    @Test
    public void testMultiStreakNWithinTUnitNoEventTypes() {
        TEvent e1 = TEvent.createKeyValue(100, EVENT_B, 75);
        TEvent e2 = TEvent.createKeyValue(110, EVENT_B, 63);
        TEvent e3 = TEvent.createKeyValue(120, EVENT_B, 50);
        TEvent e4 = TEvent.createKeyValue(130, EVENT_B, 81);
        TEvent e5 = TEvent.createKeyValue(150, EVENT_B, 77);
        TEvent e6 = TEvent.createKeyValue(160, EVENT_B, 87);

        List<Signal> signalsRef = new ArrayList<>();
        RuleContext<StreakNBadgeRule> ruleContext = createRule(multiStreak, 60, signalsRef::add);
        Assertions.assertEquals(5, ruleContext.getRule().getMaxStreak());
        StreakNBadgeProcessor streakN = new TimeBoundedStreakNBadge(pool, ruleContext);
        submitOrder(streakN, e1, e2, e3, e4, e5, e6);

        Set<Signal> signals = mergeSignals(signalsRef);
        System.out.println(signals);
        Assertions.assertEquals(0, signals.size());
    }

    @DisplayName("Single streak: multiple consecutive badges")
    @Test
    public void testStreakNWithinTUnit() {
        TEvent e1 = TEvent.createKeyValue(100, EVENT_TYPE, 75);
        TEvent e2 = TEvent.createKeyValue(110, EVENT_TYPE, 63);
        TEvent e3 = TEvent.createKeyValue(120, EVENT_TYPE, 50);
        TEvent e4 = TEvent.createKeyValue(130, EVENT_TYPE, 81);
        TEvent e5 = TEvent.createKeyValue(150, EVENT_TYPE, 77);
        TEvent e6 = TEvent.createKeyValue(160, EVENT_TYPE, 87);
        TEvent e7 = TEvent.createKeyValue(170, EVENT_TYPE, 11);

        List<Signal> signalsRef = new ArrayList<>();
        RuleContext<StreakNBadgeRule> ruleContext = createRule(singleStreak, 30, signalsRef::add);
        StreakNBadgeRule rule = ruleContext.getRule();
        Assertions.assertEquals(3, rule.getMaxStreak());
        StreakNBadgeProcessor streakN = new TimeBoundedStreakNBadge(pool, ruleContext);
        submitOrder(streakN, e1, e2, e3, e4, e5, e6, e7);

        Set<Signal> signals = mergeSignals(signalsRef);
        System.out.println(signals);
        assertStrict(signals,
                new StreakBadgeSignal(rule.getId(), e3, 3, ATTR_SILVER, 100, 120, e1.getExternalId(), e3.getExternalId()),
                new StreakBadgeSignal(rule.getId(), e6, 3, ATTR_SILVER, 130, 160, e4.getExternalId(), e6.getExternalId()));
    }

    @DisplayName("Multi streaks: multiple badges")
    @Test
    public void testMultiStreakNWithinTUnit() {
        TEvent e1 = TEvent.createKeyValue(100, EVENT_TYPE, 75);
        TEvent e2 = TEvent.createKeyValue(110, EVENT_TYPE, 63);
        TEvent e3 = TEvent.createKeyValue(120, EVENT_TYPE, 50);
        TEvent e4 = TEvent.createKeyValue(130, EVENT_TYPE, 81);
        TEvent e5 = TEvent.createKeyValue(150, EVENT_TYPE, 77);
        TEvent e6 = TEvent.createKeyValue(160, EVENT_TYPE, 87);

        List<Signal> signalsRef = new ArrayList<>();
        RuleContext<StreakNBadgeRule> ruleContext = createRule(multiStreak, 60, signalsRef::add);
        StreakNBadgeRule rule = ruleContext.getRule();
        Assertions.assertEquals(5, rule.getMaxStreak());
        StreakNBadgeProcessor streakN = new TimeBoundedStreakNBadge(pool, ruleContext);
        submitOrder(streakN, e1, e2, e3, e4, e5, e6);

        Set<Signal> signals = mergeSignals(signalsRef);
        System.out.println(signals);
        assertStrict(signals,
                new StreakBadgeSignal(rule.getId(), e3, 3, ATTR_SILVER, 100, 120, e1.getExternalId(), e3.getExternalId()),
                new StreakBadgeSignal(rule.getId(), e5, 5, ATTR_GOLD, 100, 150, e1.getExternalId(), e5.getExternalId()));
    }

    @DisplayName("Multi streaks: Out-of-order event breaks latest streak")
    @Test
    public void testOutOfOrderBreakMultiStreakNWithinTUnit() {
        TEvent e1 = TEvent.createKeyValue(100, EVENT_TYPE, 75);
        TEvent e2 = TEvent.createKeyValue(110, EVENT_TYPE, 63);
        TEvent e3 = TEvent.createKeyValue(120, EVENT_TYPE, 50);
        TEvent e4 = TEvent.createKeyValue(130, EVENT_TYPE, 81);
        TEvent e5 = TEvent.createKeyValue(150, EVENT_TYPE, 77);
        TEvent e6 = TEvent.createKeyValue(125, EVENT_TYPE, 12);

        List<Signal> signalsRef = new ArrayList<>();
        RuleContext<StreakNBadgeRule> ruleContext = createRule(multiStreak, 60, signalsRef::add);
        StreakNBadgeRule rule = ruleContext.getRule();
        Assertions.assertEquals(5, rule.getMaxStreak());
        StreakNBadgeProcessor streakN = new TimeBoundedStreakNBadge(pool, ruleContext);
        submitOrder(streakN, e1, e2, e3, e4, e5, e6);

        Set<Signal> signals = mergeSignals(signalsRef);
        System.out.println(signals);
        assertStrict(signals,
                new StreakBadgeSignal(rule.getId(), e3, 3, ATTR_SILVER, 100, 120, e1.getExternalId(), e3.getExternalId()),
                new StreakBadgeSignal(rule.getId(), e5, 5, ATTR_GOLD, 100, 150, e1.getExternalId(), e5.getExternalId()),
                new BadgeRemoveSignal(rule.getId(), e5.asEventScope(), ATTR_GOLD, 100, 150, e1.getExternalId(), e5.getExternalId()));
    }

    @DisplayName("Single streak: not within given time unit")
    @Test
    public void testStreakNNotWithinTUnit() {
        TEvent e1 = TEvent.createKeyValue(100, EVENT_TYPE, 75);
        TEvent e2 = TEvent.createKeyValue(110, EVENT_TYPE, 63);
        TEvent e3 = TEvent.createKeyValue(122, EVENT_TYPE, 50);
        TEvent e4 = TEvent.createKeyValue(135, EVENT_TYPE, 81);
        TEvent e5 = TEvent.createKeyValue(140, EVENT_TYPE, 21);

        List<Signal> signalsRef = new ArrayList<>();
        RuleContext<StreakNBadgeRule> ruleContext = createRule(singleStreak, 20, signalsRef::add);
        Assertions.assertEquals(3, ruleContext.getRule().getMaxStreak());
        StreakNBadgeProcessor streakN = new TimeBoundedStreakNBadge(pool, ruleContext);
        submitOrder(streakN, e1, e2, e3, e4, e5);

        Set<Signal> signals = mergeSignals(signalsRef);
        System.out.println(signals);
        Assertions.assertEquals(0, signals.size());
    }

    @DisplayName("Single streak: Out-of-order event creates a new streak")
    @Test
    public void testOutOfOrderStreakNWithinTUnit() {
        TEvent e1 = TEvent.createKeyValue(100, EVENT_TYPE, 35);
        TEvent e2 = TEvent.createKeyValue(110, EVENT_TYPE, 63);
        TEvent e3 = TEvent.createKeyValue(120, EVENT_TYPE, 50);
        TEvent e4 = TEvent.createKeyValue(115, EVENT_TYPE, 88);
        TEvent e5 = TEvent.createKeyValue(140, EVENT_TYPE, 21);

        List<Signal> signalsRef = new ArrayList<>();
        RuleContext<StreakNBadgeRule> ruleContext = createRule(singleStreak, 30, signalsRef::add);
        Assertions.assertEquals(3, ruleContext.getRule().getMaxStreak());
        StreakNBadgeProcessor streakN = new TimeBoundedStreakNBadge(pool, ruleContext);
        submitOrder(streakN, e1, e2, e3, e4, e5);

        Set<Signal> signals = mergeSignals(signalsRef);
        System.out.println(signals);
        Assertions.assertEquals(1, signals.size());
        assertStrict(signals,
                new StreakBadgeSignal(ruleContext.getRule().getId(), e3, 3, ATTR_SILVER, 110, 120, e2.getExternalId(), e3.getExternalId()));
    }

    @DisplayName("Single streak: Out-of-order event but not within time unit")
    @Test
    public void testOutOfOrderStreakNNotWithinTUnit() {
        TEvent e1 = TEvent.createKeyValue(100, EVENT_TYPE, 35);
        TEvent e2 = TEvent.createKeyValue(110, EVENT_TYPE, 63);
        TEvent e3 = TEvent.createKeyValue(150, EVENT_TYPE, 50);
        TEvent e4 = TEvent.createKeyValue(120, EVENT_TYPE, 88);
        TEvent e5 = TEvent.createKeyValue(160, EVENT_TYPE, 21);

        List<Signal> signalsRef = new ArrayList<>();
        RuleContext<StreakNBadgeRule> ruleContext = createRule(singleStreak, 30, signalsRef::add);
        Assertions.assertEquals(3, ruleContext.getRule().getMaxStreak());
        StreakNBadgeProcessor streakN = new TimeBoundedStreakNBadge(pool, ruleContext);
        submitOrder(streakN, e1, e2, e3, e4, e5);

        Set<Signal> signals = mergeSignals(signalsRef);
        System.out.println(signals);
        Assertions.assertEquals(0, signals.size());
    }

    @DisplayName("Single streak: Out-of-order event breaks streak within time unit")
    @Test
    public void testOutOfOrderBreakStreakNWithinTUnit() {
        TEvent e1 = TEvent.createKeyValue(100, EVENT_TYPE, 35);
        TEvent e2 = TEvent.createKeyValue(110, EVENT_TYPE, 63);
        TEvent e3 = TEvent.createKeyValue(120, EVENT_TYPE, 50);
        TEvent e4 = TEvent.createKeyValue(130, EVENT_TYPE, 88);
        TEvent e5 = TEvent.createKeyValue(115, EVENT_TYPE, 21);

        List<Signal> signalsRef = new ArrayList<>();
        RuleContext<StreakNBadgeRule> ruleContext = createRule(singleStreak, 30, signalsRef::add);
        Assertions.assertEquals(3, ruleContext.getRule().getMaxStreak());
        StreakNBadgeProcessor streakN = new TimeBoundedStreakNBadge(pool, ruleContext);
        submitOrder(streakN, e1, e2, e3, e4, e5);

        Set<Signal> signals = mergeSignals(signalsRef);
        System.out.println(signals);
        assertStrict(signals,
                new StreakBadgeSignal(ruleContext.getRule().getId(), e4, 3, ATTR_SILVER, 110, 130, e2.getExternalId(), e4.getExternalId()),
                new BadgeRemoveSignal(ruleContext.getRule().getId(), e4.asEventScope(), ATTR_SILVER, 110, 130, e2.getExternalId(), e4.getExternalId()));
    }

    @DisplayName("Single streak: Out-of-order event cannot break the streak not within time unit")
    @Test
    public void testOutOfOrderBreakStreakNNotWithinTUnit() {
        TEvent e1 = TEvent.createKeyValue(100, EVENT_TYPE, 35);
        TEvent e2 = TEvent.createKeyValue(110, EVENT_TYPE, 63);
        TEvent e3 = TEvent.createKeyValue(120, EVENT_TYPE, 50);
        TEvent e4 = TEvent.createKeyValue(140, EVENT_TYPE, 88);
        TEvent e5 = TEvent.createKeyValue(105, EVENT_TYPE, 21);

        List<Signal> signalsRef = new ArrayList<>();
        RuleContext<StreakNBadgeRule> ruleContext = createRule(singleStreak, 30, signalsRef::add);
        Assertions.assertEquals(3, ruleContext.getRule().getMaxStreak());
        StreakNBadgeProcessor streakN = new TimeBoundedStreakNBadge(pool, ruleContext);
        submitOrder(streakN, e1, e2, e3, e4, e5);

        Set<Signal> signals = mergeSignals(signalsRef);
        System.out.println(signals);
        assertStrict(signals,
                new StreakBadgeSignal(ruleContext.getRule().getId(), e4, 3, ATTR_SILVER, 110, 140, e2.getExternalId(), e4.getExternalId()));
    }

    private RuleContext<StreakNBadgeRule> createRule(Map<Integer, Integer> streaks, long timeUnit, Consumer<Signal> consumer) {
        TimeBoundedStreakNRule rule = new TimeBoundedStreakNRule("test.temporal.streak");
        rule.setEventTypeMatcher(new SingleEventTypeMatcher(EVENT_TYPE));
        rule.setStreaks(toStreakMap(streaks));
        rule.setCriteria((e,r,c) -> (long) e.getFieldValue("value") >= 50);
        rule.setRetainTime(100);
        rule.setTimeUnit(timeUnit);
        return new RuleContext<>(rule, fromConsumer(consumer));
    }
}
