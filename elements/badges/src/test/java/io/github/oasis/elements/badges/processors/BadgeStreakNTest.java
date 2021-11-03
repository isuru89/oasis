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
import io.github.oasis.elements.badges.TEvent;
import io.github.oasis.elements.badges.rules.StreakNBadgeRule;
import io.github.oasis.elements.badges.signals.BadgeRemoveSignal;
import io.github.oasis.elements.badges.signals.StreakBadgeSignal;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * @author Isuru Weerarathna
 */
@DisplayName("Streaks")
public class BadgeStreakNTest extends AbstractRuleTest {

    public static final String EVT_A = "question.voted";
    public static final String EVT_B = "unknown.event";

    private static final int ATTR_SILVER = 10;
    private static final int ATTR_GOLD = 20;

    public static final String STREAK_N_YML = "kinds/badgeStreakN.yml";
    public static final String SINGLE_STREAK = "SINGLE_STREAK";
    public static final String SINGLE_STREAK_WITH_POINTS = "SINGLE_STREAK_WITH_POINTS";
    public static final String MULTI_STREAK = "MULTI_STREAK";
    public static final String MULTI_STREAK_WITH_POINTS = "MULTI_STREAK_WITH_POINTS";
    public static final String DEF_POINTS_ID = "reputation";


    @DisplayName("Single streak: not enough elements")
    @Test
    public void testNotEnoughStreakN() {
        TEvent e1 = TEvent.createKeyValue(100, EVT_A, 75);
        TEvent e2 = TEvent.createKeyValue(104, EVT_A, 63);

        List<Signal> signals = new ArrayList<>();
        StreakNBadgeRule rule = loadRule(STREAK_N_YML, SINGLE_STREAK);
        RuleContext<StreakNBadgeRule> ruleContext = createRule(rule, signals::add);
        Assertions.assertEquals(3, ruleContext.getRule().getMaxStreak());
        StreakNBadgeProcessor streakN = new StreakNBadgeProcessor(pool, ruleContext);
        submitOrder(streakN, e1, e2);

        System.out.println(signals);
        Assertions.assertEquals(0, signals.size());
    }

    @DisplayName("Single streak: not satisfied elements")
    @Test
    public void testNoStreakN() {
        TEvent e1 = TEvent.createKeyValue(100, EVT_A, 75);
        TEvent e2 = TEvent.createKeyValue(104, EVT_A, 63);
        TEvent e3 = TEvent.createKeyValue(105, EVT_A, 47);
        TEvent e4 = TEvent.createKeyValue(106, EVT_A, 88);

        List<Signal> signals = new ArrayList<>();
        StreakNBadgeRule rule = loadRule(STREAK_N_YML, SINGLE_STREAK);
        RuleContext<StreakNBadgeRule> ruleContext = createRule(rule, signals::add);
        StreakNBadgeProcessor streakN = new StreakNBadgeProcessor(pool, ruleContext);
        submitOrder(streakN, e1, e2, e3, e4);

        System.out.println(signals);
        Assertions.assertEquals(0, signals.size());
    }

    @DisplayName("Single streak: No satisfied event types")
    @Test
    public void testOrderedStreakNNoEventTypes() {
        TEvent e1 = TEvent.createKeyValue(100, EVT_B, 75);
        TEvent e2 = TEvent.createKeyValue(104, EVT_B, 63);
        TEvent e3 = TEvent.createKeyValue(105, EVT_B, 50);
        TEvent e4 = TEvent.createKeyValue(106, EVT_B, 21);

        List<Signal> signals = new ArrayList<>();
        StreakNBadgeRule rule = loadRule(STREAK_N_YML, SINGLE_STREAK);
        RuleContext<StreakNBadgeRule> ruleContext = createRule(rule, signals::add);
        StreakNBadgeProcessor streakN = new StreakNBadgeProcessor(pool, ruleContext);
        submitOrder(streakN, e1, e2, e3, e4);

        Assertions.assertEquals(0, signals.size());
    }

    @DisplayName("Single streak")
    @Test
    public void testOrderedStreakN() {
        TEvent e1 = TEvent.createKeyValue(100, EVT_A, 75);
        TEvent e2 = TEvent.createKeyValue(104, EVT_A, 63);
        TEvent e3 = TEvent.createKeyValue(105, EVT_A, 50);
        TEvent e4 = TEvent.createKeyValue(106, EVT_A, 21);

        List<Signal> signals = new ArrayList<>();
        StreakNBadgeRule rule = loadRule(STREAK_N_YML, SINGLE_STREAK);
        RuleContext<StreakNBadgeRule> ruleContext = createRule(rule, signals::add);
        StreakNBadgeProcessor streakN = new StreakNBadgeProcessor(pool, ruleContext);
        submitOrder(streakN, e1, e2, e3, e4);

        System.out.println(signals);
        assertStrict(signals,
                new StreakBadgeSignal(ruleContext.getRule().getId(), e3, 3, ATTR_SILVER, 100, 105, e1.getExternalId(), e3.getExternalId()));
    }

    @DisplayName("Single streak: with points")
    @Test
    public void testOrderedStreakNWithPoints() {
        TEvent e1 = TEvent.createKeyValue(100, EVT_A, 75);
        TEvent e2 = TEvent.createKeyValue(104, EVT_A, 63);
        TEvent e3 = TEvent.createKeyValue(105, EVT_A, 50);
        TEvent e4 = TEvent.createKeyValue(106, EVT_A, 21);

        List<Signal> signals = new ArrayList<>();
        StreakNBadgeRule rule = loadRule(STREAK_N_YML, SINGLE_STREAK_WITH_POINTS);
        RuleContext<StreakNBadgeRule> ruleContext = createRule(rule, signals::add);
        StreakNBadgeProcessor streakN = new StreakNBadgeProcessor(pool, ruleContext);
        submitOrder(streakN, e1, e2, e3, e4);

        System.out.println(signals);
        assertStrict(signals,
                new StreakBadgeSignal(ruleContext.getRule().getId(), e3, 3, ATTR_SILVER, 100, 105, e1.getExternalId(), e3.getExternalId())
                    .setPointAwards(DEF_POINTS_ID, BigDecimal.valueOf(10))
        );
    }

    @DisplayName("Single streak: Out-of-order break")
    @Test
    public void testOutOfOrderMisMatchStreakN() {
        TEvent e1 = TEvent.createKeyValue(100, EVT_A, 75);
        TEvent e2 = TEvent.createKeyValue(104, EVT_A, 63);
        TEvent e3 = TEvent.createKeyValue(105, EVT_A, 55);
        TEvent e4 = TEvent.createKeyValue(101, EVT_A, 11);
        TEvent e5 = TEvent.createKeyValue(106, EVT_A, 21);

        List<Signal> signals = new ArrayList<>();
        StreakNBadgeRule rule = loadRule(STREAK_N_YML, SINGLE_STREAK);
        RuleContext<StreakNBadgeRule> ruleContext = createRule(rule, signals::add);
        StreakNBadgeProcessor streakN = new StreakNBadgeProcessor(pool, ruleContext);
        submitOrder(streakN, e1, e2, e3, e4, e5);

        System.out.println(signals);
        assertStrict(signals,
                new StreakBadgeSignal(ruleContext.getRule().getId(), e3, 3, ATTR_SILVER, 100, 105, e1.getExternalId(), e3.getExternalId()),
                new BadgeRemoveSignal(ruleContext.getRule().getId(), e3.asEventScope(), ATTR_SILVER, 100, 105, e1.getExternalId(), e3.getExternalId()));
    }

    @DisplayName("Single streak: Out-of-order break with points")
    @Test
    public void testOutOfOrderMisMatchStreakNWithPoints() {
        TEvent e1 = TEvent.createKeyValue(100, EVT_A, 75);
        TEvent e2 = TEvent.createKeyValue(104, EVT_A, 63);
        TEvent e3 = TEvent.createKeyValue(105, EVT_A, 55);
        TEvent e4 = TEvent.createKeyValue(101, EVT_A, 11);
        TEvent e5 = TEvent.createKeyValue(106, EVT_A, 21);

        List<Signal> signals = new ArrayList<>();
        StreakNBadgeRule rule = loadRule(STREAK_N_YML, SINGLE_STREAK_WITH_POINTS);
        RuleContext<StreakNBadgeRule> ruleContext = createRule(rule, signals::add);
        StreakNBadgeProcessor streakN = new StreakNBadgeProcessor(pool, ruleContext);
        submitOrder(streakN, e1, e2, e3, e4, e5);

        System.out.println(signals);
        assertStrict(signals,
                new StreakBadgeSignal(rule.getId(), e3, 3, ATTR_SILVER, 100, 105, e1.getExternalId(), e3.getExternalId())
                    .setPointAwards(DEF_POINTS_ID, BigDecimal.valueOf(10)),
                new BadgeRemoveSignal(rule.getId(), e3.asEventScope(), ATTR_SILVER, 100, 105, e1.getExternalId(), e3.getExternalId())
                    .setPointAwards(DEF_POINTS_ID, BigDecimal.valueOf(10).negate())
        );
    }

    @DisplayName("Single streak: Out-of-order falls after outside streak")
    @Test
    public void testOutOfOrderMisMatchNotAffectedStreakN() {
        TEvent e1 = TEvent.createKeyValue(100, EVT_A, 75);
        TEvent e2 = TEvent.createKeyValue(104, EVT_A, 63);
        TEvent e3 = TEvent.createKeyValue(105, EVT_A, 55);
        TEvent e4 = TEvent.createKeyValue(107, EVT_A, 11);
        TEvent e5 = TEvent.createKeyValue(106, EVT_A, 21);

        List<Signal> signals = new ArrayList<>();
        StreakNBadgeRule rule = loadRule(STREAK_N_YML, SINGLE_STREAK);
        RuleContext<StreakNBadgeRule> ruleContext = createRule(rule, signals::add);
        StreakNBadgeProcessor streakN = new StreakNBadgeProcessor(pool, ruleContext);
        submitOrder(streakN, e1, e2, e3, e4, e5);

        System.out.println(signals);
        assertStrict(signals,
                new StreakBadgeSignal(rule.getId(), e3, 3, ATTR_SILVER, 100, 105, e1.getExternalId(), e3.getExternalId()) );
    }

    @DisplayName("Single streak: Out-of-order falls before outside streak")
    @Test
    public void testOutOfOrderMisMatchNotAffectedBeforeAllStreakN() {
        TEvent e1 = TEvent.createKeyValue(100, EVT_A, 75);
        TEvent e2 = TEvent.createKeyValue(104, EVT_A, 63);
        TEvent e3 = TEvent.createKeyValue(105, EVT_A, 55);
        TEvent e4 = TEvent.createKeyValue(99, EVT_A, 11);
        TEvent e5 = TEvent.createKeyValue(106, EVT_A, 21);

        List<Signal> signals = new ArrayList<>();
        StreakNBadgeRule rule = loadRule(STREAK_N_YML, SINGLE_STREAK);
        RuleContext<StreakNBadgeRule> ruleContext = createRule(rule, signals::add);
        StreakNBadgeProcessor streakN = new StreakNBadgeProcessor(pool, ruleContext);
        submitOrder(streakN, e1, e2, e3, e4, e5);

        System.out.println(signals);
        assertStrict(signals,
                new StreakBadgeSignal(rule.getId(), e3, 3, ATTR_SILVER, 100, 105, e1.getExternalId(), e3.getExternalId()));
    }

    @DisplayName("Single streak: Out-of-order creates new streak")
    @Test
    public void testOutOfOrderMatchStreakN() {
        TEvent e1 = TEvent.createKeyValue(100, EVT_A, 75);
        TEvent e2 = TEvent.createKeyValue(104, EVT_A, 63);
        TEvent e3 = TEvent.createKeyValue(105, EVT_A, 20);
        TEvent e4 = TEvent.createKeyValue(101, EVT_A, 81);
        TEvent e5 = TEvent.createKeyValue(106, EVT_A, 21);

        List<Signal> signals = new ArrayList<>();
        StreakNBadgeRule rule = loadRule(STREAK_N_YML, SINGLE_STREAK);
        RuleContext<StreakNBadgeRule> ruleContext = createRule(rule, signals::add);
        StreakNBadgeProcessor streakN = new StreakNBadgeProcessor(pool, ruleContext);
        submitOrder(streakN, e1, e2, e3, e4, e5);

        assertStrict(signals,
                new StreakBadgeSignal(rule.getId(), e2, 3, ATTR_SILVER, 100, 104, e1.getExternalId(), e2.getExternalId()));
    }

    @DisplayName("Single streak: Out-of-order does not modifies existing streak end time")
    @Test
    public void testStreakN() {
        TEvent e1 = TEvent.createKeyValue(100, EVT_A, 75);
        TEvent e2 = TEvent.createKeyValue(104, EVT_A, 63);
        TEvent e3 = TEvent.createKeyValue(105, EVT_A, 50);
        TEvent e4 = TEvent.createKeyValue(101, EVT_A, 81);
        TEvent e5 = TEvent.createKeyValue(106, EVT_A, 21);

        List<Signal> signals = new ArrayList<>();
        StreakNBadgeRule rule = loadRule(STREAK_N_YML, SINGLE_STREAK);
        RuleContext<StreakNBadgeRule> ruleContext = createRule(rule, signals::add);
        StreakNBadgeProcessor streakN = new StreakNBadgeProcessor(pool, ruleContext);
        submitOrder(streakN, e1, e2, e3, e4, e5);

        printSignals(signals);
        assertStrict(signals,
                new StreakBadgeSignal(rule.getId(), e3, 3, ATTR_SILVER, 100, 105, e1.getExternalId(), e3.getExternalId()));
    }

    // ---------------------------------------
    // MULTI STREAK TESTS
    // ---------------------------------------

    @DisplayName("Multi streaks")
    @Test
    public void testMultiStreakN() {
        TEvent e1 = TEvent.createKeyValue(100, EVT_A, 75);
        TEvent e2 = TEvent.createKeyValue(104, EVT_A, 63);
        TEvent e3 = TEvent.createKeyValue(105, EVT_A, 57);
        TEvent e4 = TEvent.createKeyValue(106, EVT_A, 88);
        TEvent e5 = TEvent.createKeyValue(107, EVT_A, 76);

        List<Signal> signalsRef = new ArrayList<>();
        StreakNBadgeRule rule = loadRule(STREAK_N_YML, MULTI_STREAK);
        RuleContext<StreakNBadgeRule> ruleContext = createRule(rule, signalsRef::add);
        Assertions.assertEquals(5, rule.getMaxStreak());
        StreakNBadgeProcessor streakN = new StreakNBadgeProcessor(pool, ruleContext);
        submitOrder(streakN, e1, e2, e3, e4, e5);

        Set<Signal> signals = mergeSignals(signalsRef);
        System.out.println(signals);
        assertStrict(signals,
                new StreakBadgeSignal(rule.getId(), e3, 3, ATTR_SILVER, 100, 105, e1.getExternalId(), e3.getExternalId()),
                new StreakBadgeSignal(rule.getId(), e5, 5, ATTR_GOLD, 100, 107, e1.getExternalId(), e5.getExternalId()));
    }

    @DisplayName("Multi streaks: consecutive badges")
    @Test
    public void testMultiStreakNConsecutive() {
        TEvent e1 = TEvent.createKeyValue(100, EVT_A, 75);
        TEvent e2 = TEvent.createKeyValue(104, EVT_A, 63);
        TEvent e3 = TEvent.createKeyValue(105, EVT_A, 57); //
        TEvent e4 = TEvent.createKeyValue(106, EVT_A, 88);
        TEvent e5 = TEvent.createKeyValue(107, EVT_A, 76); //
        TEvent e6 = TEvent.createKeyValue(108, EVT_A, 81);
        TEvent e7 = TEvent.createKeyValue(109, EVT_A, 97);
        TEvent e8 = TEvent.createKeyValue(110, EVT_A, 77); //

        List<Signal> signalsRef = new ArrayList<>();
        StreakNBadgeRule rule = loadRule(STREAK_N_YML, MULTI_STREAK);
        RuleContext<StreakNBadgeRule> ruleContext = createRule(rule, signalsRef::add);
        Assertions.assertEquals(5, rule.getMaxStreak());
        StreakNBadgeProcessor streakN = new StreakNBadgeProcessor(pool, ruleContext);
        submitOrder(streakN, e1, e2, e3, e4, e5, e6, e7, e8);

        Set<Signal> signals = mergeSignals(signalsRef);
        printSignals(signals);
        assertStrict(signals,
                new StreakBadgeSignal(rule.getId(), e3, 3, ATTR_SILVER, 100, 105, e1.getExternalId(), e3.getExternalId()),
                new StreakBadgeSignal(rule.getId(), e5, 5, ATTR_GOLD, 100, 107, e1.getExternalId(), e5.getExternalId()),
                new StreakBadgeSignal(rule.getId(), e8, 3, ATTR_SILVER, 108, 110, e6.getExternalId(), e8.getExternalId()));
    }

    @DisplayName("Multi streaks: Non consecutive badges")
    @Test
    public void testMultiStreakNNonConsecutive() {
        TEvent e1 = TEvent.createKeyValue(100, EVT_A, 75);
        TEvent e2 = TEvent.createKeyValue(104, EVT_A, 63);
        TEvent e3 = TEvent.createKeyValue(105, EVT_A, 57); //
        TEvent e4 = TEvent.createKeyValue(106, EVT_A, 88);
        TEvent e5 = TEvent.createKeyValue(107, EVT_A, 76); //
        TEvent e6 = TEvent.createKeyValue(108, EVT_A, 3);
        TEvent e7 = TEvent.createKeyValue(109, EVT_A, 97);
        TEvent e8 = TEvent.createKeyValue(110, EVT_A, 77);
        TEvent e9 = TEvent.createKeyValue(111, EVT_A, 77); //

        List<Signal> signalsRef = new ArrayList<>();
        StreakNBadgeRule rule = loadRule(STREAK_N_YML, MULTI_STREAK);
        RuleContext<StreakNBadgeRule> ruleContext = createRule(rule, signalsRef::add);
        Assertions.assertEquals(5, rule.getMaxStreak());
        StreakNBadgeProcessor streakN = new StreakNBadgeProcessor(pool, ruleContext);
        submitOrder(streakN, e1, e2, e3, e4, e5, e6, e7, e8, e9);

        Set<Signal> signals = mergeSignals(signalsRef);
        printSignals(signals);
        assertStrict(signals,
                new StreakBadgeSignal(rule.getId(), e3, 3, ATTR_SILVER, 100, 105, e1.getExternalId(), e3.getExternalId()),
                new StreakBadgeSignal(rule.getId(), e5, 5, ATTR_GOLD, 100, 107, e1.getExternalId(), e5.getExternalId()),
                new StreakBadgeSignal(rule.getId(), e9, 3, ATTR_SILVER, 109, 111, e7.getExternalId(), e9.getExternalId()));
    }

    @DisplayName("Multi streaks: Out-of-order creates multiple streaks")
    @Test
    public void testOutOfOrderMultiStreakN() {
        TEvent e1 = TEvent.createKeyValue(100, EVT_A, 75);
        TEvent e2 = TEvent.createKeyValue(104, EVT_A, 63);
        TEvent e3 = TEvent.createKeyValue(105, EVT_A, 57);
        TEvent e4 = TEvent.createKeyValue(106, EVT_A, 88);
        TEvent e5 = TEvent.createKeyValue(101, EVT_A, 76);
        TEvent e6 = TEvent.createKeyValue(107, EVT_A, 26);

        List<Signal> signalsRef = new ArrayList<>();
        StreakNBadgeRule rule = loadRule(STREAK_N_YML, MULTI_STREAK);
        RuleContext<StreakNBadgeRule> ruleContext = createRule(rule, signalsRef::add);
        Assertions.assertEquals(5, rule.getMaxStreak());
        StreakNBadgeProcessor streakN = new StreakNBadgeProcessor(pool, ruleContext);
        submitOrder(streakN, e1, e2, e3, e4, e5, e6);

        Set<Signal> signals = mergeSignals(signalsRef);
        System.out.println(signals);
        assertStrict(signals,
                new StreakBadgeSignal(rule.getId(), e2, 3, ATTR_SILVER, 100, 104, e1.getExternalId(), e2.getExternalId()),
                new StreakBadgeSignal(rule.getId(), e4, 5, ATTR_GOLD, 100, 106, e1.getExternalId(), e4.getExternalId()));
    }

    @DisplayName("Multi streaks: Out-of-order breaks the latest streak")
    @Test
    public void testOutOfOrderBreakMultiStreakN() {
        TEvent e1 = TEvent.createKeyValue(100, EVT_A, 75);
        TEvent e2 = TEvent.createKeyValue(104, EVT_A, 63);
        TEvent e3 = TEvent.createKeyValue(105, EVT_A, 57);
        TEvent e4 = TEvent.createKeyValue(106, EVT_A, 88);
        TEvent e5 = TEvent.createKeyValue(107, EVT_A, 76);
        TEvent e6 = TEvent.createKeyValue(101, EVT_A, 26);

        List<Signal> signalsRef = new ArrayList<>();
        StreakNBadgeRule rule = loadRule(STREAK_N_YML, MULTI_STREAK);
        RuleContext<StreakNBadgeRule> ruleContext = createRule(rule, signalsRef::add);
        StreakNBadgeProcessor streakN = new StreakNBadgeProcessor(pool, ruleContext);
        submitOrder(streakN, e1, e2, e3, e4, e5, e6);

        Set<Signal> signals = mergeSignals(signalsRef);
        System.out.println(signals);
        assertStrict(signals,
                new StreakBadgeSignal(rule.getId(), e3, 3, ATTR_SILVER, 100, 105, e1.getExternalId(), e3.getExternalId()),
                new StreakBadgeSignal(rule.getId(), e5, 5, ATTR_GOLD, 100, 107, e1.getExternalId(), e5.getExternalId()),
                new BadgeRemoveSignal(rule.getId(), e3.asEventScope(), ATTR_SILVER, 100, 105, e1.getExternalId(), e3.getExternalId()),
                new BadgeRemoveSignal(rule.getId(), e5.asEventScope(), ATTR_GOLD, 100, 107, e1.getExternalId(), e5.getExternalId()),
                new StreakBadgeSignal(rule.getId(), e4, 3, ATTR_SILVER, 104, 106, e2.getExternalId(), e4.getExternalId()));
    }

    @DisplayName("Multi streaks: Out-of-order breaks the only streak")
    @Test
    public void testOutOfOrderBreakAllMultiStreakN() {
        TEvent e1 = TEvent.createKeyValue(100, EVT_A, 75);
        TEvent e2 = TEvent.createKeyValue(104, EVT_A, 63);
        TEvent e3 = TEvent.createKeyValue(106, EVT_A, 57);
        TEvent e4 = TEvent.createKeyValue(107, EVT_A, 88);
        TEvent e6 = TEvent.createKeyValue(105, EVT_A, 26);

        List<Signal> signalsRef = new ArrayList<>();
        StreakNBadgeRule rule = loadRule(STREAK_N_YML, MULTI_STREAK);
        RuleContext<StreakNBadgeRule> ruleContext = createRule(rule, signalsRef::add);
        StreakNBadgeProcessor streakN = new StreakNBadgeProcessor(pool, ruleContext);
        submitOrder(streakN, e1, e2, e3, e4, e6);

        Set<Signal> signals = mergeSignals(signalsRef);
        System.out.println(signals);
        assertStrict(signals,
                new StreakBadgeSignal(rule.getId(), e3, 3, ATTR_SILVER, 100, 106, e1.getExternalId(), e3.getExternalId()),
                new BadgeRemoveSignal(rule.getId(), e3.asEventScope(), ATTR_SILVER, 100, 106, e1.getExternalId(), e3.getExternalId()));
    }

    @DisplayName("Multi streaks: Out-of-order breaks the with points")
    @Test
    public void testOutOfOrderBreakWithPointsMultiStreakN() {
        TEvent e1 = TEvent.createKeyValue(100, EVT_A, 75);
        TEvent e2 = TEvent.createKeyValue(104, EVT_A, 63);
        TEvent e3 = TEvent.createKeyValue(105, EVT_A, 57);
        TEvent e4 = TEvent.createKeyValue(106, EVT_A, 88);
        TEvent e5 = TEvent.createKeyValue(107, EVT_A, 76);
        TEvent e6 = TEvent.createKeyValue(101, EVT_A, 26);

        List<Signal> signalsRef = new ArrayList<>();
        StreakNBadgeRule rule = loadRule(STREAK_N_YML, MULTI_STREAK_WITH_POINTS);
        RuleContext<StreakNBadgeRule> ruleContext = createRule(rule, signalsRef::add);
        StreakNBadgeProcessor streakN = new StreakNBadgeProcessor(pool, ruleContext);
        submitOrder(streakN, e1, e2, e3, e4, e5, e6);

        Set<Signal> signals = mergeSignals(signalsRef);
        System.out.println(signals);
        assertStrict(signals,
                new StreakBadgeSignal(rule.getId(), e3, 3, ATTR_SILVER, 100, 105, e1.getExternalId(), e3.getExternalId())
                        .setPointAwards(DEF_POINTS_ID, BigDecimal.valueOf(10)),
                new StreakBadgeSignal(rule.getId(), e5, 5, ATTR_GOLD, 100, 107, e1.getExternalId(), e5.getExternalId())
                        .setPointAwards(DEF_POINTS_ID, BigDecimal.valueOf(50)),
                new BadgeRemoveSignal(rule.getId(), e3.asEventScope(), ATTR_SILVER, 100, 105, e1.getExternalId(), e3.getExternalId())
                        .setPointAwards(DEF_POINTS_ID, BigDecimal.valueOf(10).negate()),
                new BadgeRemoveSignal(rule.getId(), e5.asEventScope(), ATTR_GOLD, 100, 107, e1.getExternalId(), e5.getExternalId())
                        .setPointAwards(DEF_POINTS_ID, BigDecimal.valueOf(50).negate()),
                new StreakBadgeSignal(rule.getId(), e4, 3, ATTR_SILVER, 104, 106, e2.getExternalId(), e4.getExternalId())
                        .setPointAwards(DEF_POINTS_ID, BigDecimal.valueOf(10)));
    }

    private RuleContext<StreakNBadgeRule> createRule(StreakNBadgeRule rule, Consumer<Signal> consumer) {
        return new RuleContext<>(rule, fromConsumer(consumer));
    }
}
