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

package io.github.oasis.engine.elements.challenges;

import io.github.oasis.core.Event;
import io.github.oasis.engine.elements.AbstractRuleTest;
import io.github.oasis.core.elements.AbstractRule;
import io.github.oasis.core.elements.Signal;
import io.github.oasis.core.context.ExecutionContext;
import io.github.oasis.core.elements.RuleContext;
import io.github.oasis.engine.model.SingleEventTypeMatcher;
import io.github.oasis.engine.model.TEvent;
import io.github.oasis.engine.utils.Constants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author Isuru Weerarathna
 */
@DisplayName("Challenges")
public class ChallengeTest extends AbstractRuleTest {

    private static final String EVT_A = "a";
    private static final String EVT_B = "b";

    static final BigDecimal AWARD = BigDecimal.valueOf(100).setScale(Constants.SCALE, RoundingMode.HALF_UP);

    static final long START = 0;

    static final long U1 = 1;
    static final long U2 = 2;
    static final long U3 = 3;
    static final long U4 = 4;
    static final long U5 = 5;

    static final int WIN_3 = 3;

    static final String POINT_ID = "challenge.points";

    @DisplayName("No relevant events, no winners")
    @Test
    public void testWithoutWinner() {
        TEvent e1 = TEvent.createKeyValue(1,100, EVT_B, 87);
        TEvent e2 = TEvent.createKeyValue(2,105, EVT_B, 53);
        TEvent e3 = TEvent.createKeyValue(3,110, EVT_B, 34);

        List<Signal> signals = new ArrayList<>();
        RuleContext<ChallengeRule> ruleContext = createRule(AWARD, WIN_3, START, 150, signals);
        ChallengeRule rule = ruleContext.getRule();
        Assertions.assertEquals(WIN_3, rule.getWinnerCount());
        Assertions.assertTrue(rule.hasFlag(ChallengeRule.REPEATABLE_WINNERS));
        ChallengeProcessor processor = new ChallengeProcessor(pool, ruleContext);
        submitOrder(processor, e1, e2, e3);

        System.out.println(signals);
        Assertions.assertEquals(0, signals.size());
    }

    @DisplayName("No criteria satisfied, no winners")
    @Test
    public void testNoWinnerUnsatisfiedCriteria() {
        TEvent e1 = TEvent.createKeyValue(U1,100, EVT_A, 17);
        TEvent e2 = TEvent.createKeyValue(U2,105, EVT_A, 23);
        TEvent e3 = TEvent.createKeyValue(U3,110, EVT_A, 34);
        TEvent e4 = TEvent.createKeyValue(U4,115, EVT_A, 45);

        List<Signal> signals = new ArrayList<>();
        RuleContext<ChallengeRule> ruleContext = createRule(AWARD, WIN_3, START, 150, signals);
        ChallengeRule rule = ruleContext.getRule();
        Assertions.assertEquals(WIN_3, rule.getWinnerCount());
        ChallengeProcessor processor = new ChallengeProcessor(pool, ruleContext);
        submitOrder(processor, e1, e2, e3, e4);

        System.out.println(signals);
        Assertions.assertEquals(0, signals.size());
    }

    @DisplayName("Same points for each winner")
    @Test
    public void testSamePointsWinners() {
        TEvent e1 = TEvent.createKeyValue(U1,100, EVT_A, 57);
        TEvent e2 = TEvent.createKeyValue(U2,105, EVT_A, 83);
        TEvent e3 = TEvent.createKeyValue(U3,110, EVT_A, 34);
        TEvent e4 = TEvent.createKeyValue(U4,115, EVT_A, 25);

        List<Signal> signals = new ArrayList<>();
        RuleContext<ChallengeRule> ruleContext = createRule(AWARD, WIN_3, START, 150, signals);
        ChallengeRule rule = ruleContext.getRule();
        Assertions.assertEquals(WIN_3, rule.getWinnerCount());
        ChallengeProcessor processor = new ChallengeProcessor(pool, ruleContext);
        submitOrder(processor, e1, e2, e3, e4);

        System.out.println(signals);
        assertStrict(signals,
                new ChallengeWinSignal(rule.getId(), e1, 1, U1, e1.getTimestamp(), e1.getExternalId()),
                new ChallengeWinSignal(rule.getId(), e2, 2, U2, e2.getTimestamp(), e2.getExternalId()),
                new ChallengePointsAwardedSignal(rule.getId(), POINT_ID, AWARD, e1),
                new ChallengePointsAwardedSignal(rule.getId(), POINT_ID, AWARD, e2)
                );
    }

    @DisplayName("Different points for each winner")
    @Test
    public void testDifferentPointsWinners() {
        TEvent e1 = TEvent.createKeyValue(U1,100, EVT_A, 57);
        TEvent e2 = TEvent.createKeyValue(U2,105, EVT_A, 83);
        TEvent e3 = TEvent.createKeyValue(U3,110, EVT_A, 34);
        TEvent e4 = TEvent.createKeyValue(U4,115, EVT_A, 25);

        List<Signal> signals = new ArrayList<>();
        RuleContext<ChallengeRule> ruleContext = createRule(AWARD, WIN_3, START, 150, signals);
        ChallengeRule rule = ruleContext.getRule();
        rule.setCustomAwardPoints(this::award);
        Assertions.assertEquals(WIN_3, rule.getWinnerCount());
        ChallengeProcessor processor = new ChallengeProcessor(pool, ruleContext);
        submitOrder(processor, e1, e2, e3, e4);

        System.out.println(signals);
        assertStrict(signals,
                new ChallengeWinSignal(rule.getId(), e1, 1, U1, e1.getTimestamp(), e1.getExternalId()),
                new ChallengeWinSignal(rule.getId(), e2, 2, U2, e2.getTimestamp(), e2.getExternalId()),
                new ChallengePointsAwardedSignal(rule.getId(), POINT_ID, asDecimal(7), e1),
                new ChallengePointsAwardedSignal(rule.getId(), POINT_ID, asDecimal(33), e2)
                );
    }

    @DisplayName("Winner limit exceeded")
    @Test
    public void testWinnerLimitExceeded() {
        TEvent e1 = TEvent.createKeyValue(U1,100, EVT_A, 57);
        TEvent e2 = TEvent.createKeyValue(U2,105, EVT_A, 83);
        TEvent e3 = TEvent.createKeyValue(U3,110, EVT_A, 34);
        TEvent e4 = TEvent.createKeyValue(U4,155, EVT_A, 75);
        TEvent e5 = TEvent.createKeyValue(U4,160, EVT_A, 99);

        List<Signal> signals = new ArrayList<>();
        RuleContext<ChallengeRule> ruleContext = createRule(AWARD, WIN_3, START, 200, signals);
        ChallengeRule rule = ruleContext.getRule();
        Assertions.assertEquals(WIN_3, rule.getWinnerCount());
        ChallengeProcessor processor = new ChallengeProcessor(pool, ruleContext);
        submitOrder(processor, e1, e2, e3, e4, e5);

        System.out.println(signals);
        assertStrict(signals,
                new ChallengeWinSignal(rule.getId(), e1, 1, U1, e1.getTimestamp(), e1.getExternalId()),
                new ChallengeWinSignal(rule.getId(), e2, 2, U2, e2.getTimestamp(), e2.getExternalId()),
                new ChallengeWinSignal(rule.getId(), e4, 3, U4, e4.getTimestamp(), e4.getExternalId()),
                new ChallengePointsAwardedSignal(rule.getId(), POINT_ID, AWARD, e1),
                new ChallengePointsAwardedSignal(rule.getId(), POINT_ID, AWARD, e1),
                new ChallengePointsAwardedSignal(rule.getId(), POINT_ID, AWARD, e4),
                new ChallengeOverSignal(rule.getId(), e5.asEventScope(), e5.getTimestamp(), ChallengeOverSignal.CompletionType.ALL_WINNERS_FOUND)
        );
    }

    @DisplayName("Non-repeatable winners")
    @Test
    public void testNonRepeatableWinners() {
        TEvent e1 = TEvent.createKeyValue(U1,100, EVT_A, 57);
        TEvent e2 = TEvent.createKeyValue(U2,105, EVT_A, 83);
        TEvent e3 = TEvent.createKeyValue(U3,110, EVT_A, 34);
        TEvent e4 = TEvent.createKeyValue(U1,155, EVT_A, 75);
        TEvent e5 = TEvent.createKeyValue(U4,160, EVT_A, 99);

        List<Signal> signals = new ArrayList<>();
        RuleContext<ChallengeRule> ruleContext = createRule(AWARD, WIN_3, START, 200, signals);
        ChallengeRule rule = ruleContext.getRule();
        rule.setFlags(Set.of());
        Assertions.assertEquals(WIN_3, rule.getWinnerCount());
        Assertions.assertTrue(rule.doesNotHaveFlag(ChallengeRule.REPEATABLE_WINNERS));
        ChallengeProcessor processor = new ChallengeProcessor(pool, ruleContext);
        submitOrder(processor, e1, e2, e3, e4, e5);

        System.out.println(signals);
        assertStrict(signals,
                new ChallengeWinSignal(rule.getId(), e1, 1, U1, e1.getTimestamp(), e1.getExternalId()),
                new ChallengeWinSignal(rule.getId(), e2, 2, U2, e2.getTimestamp(), e2.getExternalId()),
                new ChallengeWinSignal(rule.getId(), e5, 3, U4, e5.getTimestamp(), e5.getExternalId()),
                new ChallengePointsAwardedSignal(rule.getId(), POINT_ID, AWARD, e1),
                new ChallengePointsAwardedSignal(rule.getId(), POINT_ID, AWARD, e1),
                new ChallengePointsAwardedSignal(rule.getId(), POINT_ID, AWARD, e5)
        );
    }

    @DisplayName("Repeatable winners")
    @Test
    public void testRepeatableWinners() {
        TEvent e1 = TEvent.createKeyValue(U1,100, EVT_A, 57);
        TEvent e2 = TEvent.createKeyValue(U1,105, EVT_A, 83);
        TEvent e3 = TEvent.createKeyValue(U3,110, EVT_A, 34);
        TEvent e4 = TEvent.createKeyValue(U1,155, EVT_A, 75);
        TEvent e5 = TEvent.createKeyValue(U4,160, EVT_A, 99);

        List<Signal> signals = new ArrayList<>();
        RuleContext<ChallengeRule> ruleContext = createRule(AWARD, WIN_3, START, 200, signals);
        ChallengeRule rule = ruleContext.getRule();
        Assertions.assertEquals(WIN_3, rule.getWinnerCount());
        Assertions.assertTrue(rule.hasFlag(ChallengeRule.REPEATABLE_WINNERS));
        ChallengeProcessor processor = new ChallengeProcessor(pool, ruleContext);
        submitOrder(processor, e1, e2, e3, e4, e5);

        System.out.println(signals);
        assertStrict(signals,
                new ChallengeWinSignal(rule.getId(), e1, 1, U1, e1.getTimestamp(), e1.getExternalId()),
                new ChallengeWinSignal(rule.getId(), e2, 2, U1, e2.getTimestamp(), e2.getExternalId()),
                new ChallengeWinSignal(rule.getId(), e4, 3, U1, e4.getTimestamp(), e4.getExternalId()),
                new ChallengePointsAwardedSignal(rule.getId(), POINT_ID, AWARD, e1),
                new ChallengePointsAwardedSignal(rule.getId(), POINT_ID, AWARD, e1),
                new ChallengePointsAwardedSignal(rule.getId(), POINT_ID, AWARD, e4),
                new ChallengeOverSignal(rule.getId(), e5.asEventScope(), e5.getTimestamp(), ChallengeOverSignal.CompletionType.ALL_WINNERS_FOUND)
        );
    }

    @DisplayName("No winners after expired")
    @Test
    public void testNoWinnersAfterExpired() {
        TEvent e1 = TEvent.createKeyValue(U1,100, EVT_A, 57);
        TEvent e2 = TEvent.createKeyValue(U2,105, EVT_A, 83);
        TEvent e3 = TEvent.createKeyValue(U3,110, EVT_A, 34);
        TEvent e4 = TEvent.createKeyValue(U4,155, EVT_A, 75);

        List<Signal> signals = new ArrayList<>();
        RuleContext<ChallengeRule> ruleContext = createRule(AWARD, WIN_3, START, 150, signals);
        ChallengeRule rule = ruleContext.getRule();
        Assertions.assertEquals(WIN_3, rule.getWinnerCount());
        ChallengeProcessor processor = new ChallengeProcessor(pool, ruleContext);
        submitOrder(processor, e1, e2, e3, e4);

        System.out.println(signals);
        assertStrict(signals,
                new ChallengeWinSignal(rule.getId(), e1, 1, U1, e1.getTimestamp(), e1.getExternalId()),
                new ChallengeWinSignal(rule.getId(), e2, 2, U2, e2.getTimestamp(), e2.getExternalId()),
                new ChallengePointsAwardedSignal(rule.getId(), POINT_ID, AWARD, e1),
                new ChallengePointsAwardedSignal(rule.getId(), POINT_ID, AWARD, e2)
        );
    }

    @DisplayName("No winners before the start")
    @Test
    public void testNoWinnersBeforeTheStart() {
        TEvent e1 = TEvent.createKeyValue(U1,50, EVT_A, 57);
        TEvent e2 = TEvent.createKeyValue(U2,105, EVT_A, 83);
        TEvent e3 = TEvent.createKeyValue(U3,110, EVT_A, 34);
        TEvent e4 = TEvent.createKeyValue(U4,155, EVT_A, 75);

        List<Signal> signals = new ArrayList<>();
        RuleContext<ChallengeRule> ruleContext = createRule(AWARD, WIN_3, 100, 200, signals);
        ChallengeRule rule = ruleContext.getRule();
        Assertions.assertEquals(WIN_3, rule.getWinnerCount());
        ChallengeProcessor processor = new ChallengeProcessor(pool, ruleContext);
        submitOrder(processor, e1, e2, e3, e4);

        System.out.println(signals);
        assertStrict(signals,
                new ChallengeWinSignal(rule.getId(), e2, 1, U2, e2.getTimestamp(), e2.getExternalId()),
                new ChallengeWinSignal(rule.getId(), e4, 2, U4, e4.getTimestamp(), e4.getExternalId()),
                new ChallengePointsAwardedSignal(rule.getId(), POINT_ID, AWARD, e2),
                new ChallengePointsAwardedSignal(rule.getId(), POINT_ID, AWARD, e4)
        );
    }

    @DisplayName("User Scoped: single challenge")
    @Test
    public void testUserScopedChallenge() {
        TEvent e1 = TEvent.createKeyValue(U1,100, EVT_A, 57);
        TEvent e2 = TEvent.createKeyValue(U2,105, EVT_A, 83);
        TEvent e3 = TEvent.createKeyValue(U3,110, EVT_A, 34);
        TEvent e4 = TEvent.createKeyValue(U4,155, EVT_A, 75);
        TEvent e5 = TEvent.createKeyValue(U2,160, EVT_A, 64);

        List<Signal> signals = new ArrayList<>();
        RuleContext<ChallengeRule> ruleContext = createRule(AWARD, 1, START, 200, signals);
        ChallengeRule rule = ruleContext.getRule();
        rule.setScope(ChallengeRule.ChallengeScope.USER);
        rule.setScopeId(U2);
        Assertions.assertEquals(1, rule.getWinnerCount());
        Assertions.assertEquals(U2, rule.getScopeId());
        Assertions.assertEquals(ChallengeRule.ChallengeScope.USER, rule.getScope());
        ChallengeProcessor processor = new ChallengeProcessor(pool, ruleContext);
        submitOrder(processor, e1, e2, e3, e4, e5);

        System.out.println(signals);
        assertStrict(signals,
                new ChallengeWinSignal(rule.getId(), e2, 1, U2, e2.getTimestamp(), e2.getExternalId()),
                new ChallengePointsAwardedSignal(rule.getId(), POINT_ID, AWARD, e2),
                new ChallengeOverSignal(rule.getId(), e5.asEventScope(), e5.getTimestamp(), ChallengeOverSignal.CompletionType.ALL_WINNERS_FOUND)
        );
    }

    @DisplayName("User Scoped: single challenge")
    @Test
    public void testUserScopedMultipleChallenge() {
        TEvent e1 = TEvent.createKeyValue(U1,100, EVT_A, 57);
        TEvent e2 = TEvent.createKeyValue(U2,105, EVT_A, 83);
        TEvent e3 = TEvent.createKeyValue(U3,110, EVT_A, 34);
        TEvent e4 = TEvent.createKeyValue(U2,155, EVT_A, 75);
        TEvent e5 = TEvent.createKeyValue(U2,160, EVT_A, 64);

        List<Signal> signals = new ArrayList<>();
        RuleContext<ChallengeRule> ruleContext = createRule(AWARD, WIN_3, START, 200, signals);
        ChallengeRule rule = ruleContext.getRule();
        rule.setScope(ChallengeRule.ChallengeScope.USER);
        rule.setScopeId(U2);
        Assertions.assertEquals(WIN_3, rule.getWinnerCount());
        Assertions.assertEquals(U2, rule.getScopeId());
        Assertions.assertEquals(ChallengeRule.ChallengeScope.USER, rule.getScope());
        Assertions.assertTrue(rule.hasFlag(ChallengeRule.REPEATABLE_WINNERS));
        ChallengeProcessor processor = new ChallengeProcessor(pool, ruleContext);
        submitOrder(processor, e1, e2, e3, e4, e5);

        System.out.println(signals);
        assertStrict(signals,
                new ChallengeWinSignal(rule.getId(), e2, 1, U2, e2.getTimestamp(), e2.getExternalId()),
                new ChallengeWinSignal(rule.getId(), e4, 2, U2, e4.getTimestamp(), e4.getExternalId()),
                new ChallengeWinSignal(rule.getId(), e5, 3, U2, e5.getTimestamp(), e5.getExternalId()),
                new ChallengePointsAwardedSignal(rule.getId(), POINT_ID, AWARD, e2),
                new ChallengePointsAwardedSignal(rule.getId(), POINT_ID, AWARD, e4),
                new ChallengePointsAwardedSignal(rule.getId(), POINT_ID, AWARD, e5)
        );
    }

    @DisplayName("User Scoped: non repeatable challenge")
    @Test
    public void testUserScopedNRMultipleChallenge() {
        TEvent e1 = TEvent.createKeyValue(U1,100, EVT_A, 57);
        TEvent e2 = TEvent.createKeyValue(U2,105, EVT_A, 83);
        TEvent e3 = TEvent.createKeyValue(U3,110, EVT_A, 34);
        TEvent e4 = TEvent.createKeyValue(U2,155, EVT_A, 75);
        TEvent e5 = TEvent.createKeyValue(U2,160, EVT_A, 64);

        List<Signal> signals = new ArrayList<>();
        RuleContext<ChallengeRule> ruleContext = createRule(AWARD, WIN_3, START, 200, signals);
        ChallengeRule rule = ruleContext.getRule();
        rule.setScope(ChallengeRule.ChallengeScope.USER);
        rule.setScopeId(U2);
        rule.setFlags(Set.of());
        Assertions.assertEquals(WIN_3, rule.getWinnerCount());
        Assertions.assertEquals(U2, rule.getScopeId());
        Assertions.assertEquals(ChallengeRule.ChallengeScope.USER, rule.getScope());
        Assertions.assertTrue(rule.doesNotHaveFlag(ChallengeRule.REPEATABLE_WINNERS));
        ChallengeProcessor processor = new ChallengeProcessor(pool, ruleContext);
        submitOrder(processor, e1, e2, e3, e4, e5);

        System.out.println(signals);
        assertStrict(signals,
                new ChallengeWinSignal(rule.getId(), e2, 1, U2, e2.getTimestamp(), e2.getExternalId()),
                new ChallengePointsAwardedSignal(rule.getId(), POINT_ID, AWARD, e2)
        );
    }

    @DisplayName("No point event when empty point id")
    @Test
    public void noPointEventIfEmptyPointId() {
        TEvent e1 = TEvent.createKeyValue(U1,100, EVT_A, 57);
        RuleContext<ChallengeRule> ruleContext = createRule(BigDecimal.valueOf(20), 3, 100, 200, new ArrayList<>());
        ChallengeRule rule = ruleContext.getRule();
        ChallengePointsAwardedSignal withPointId = new ChallengePointsAwardedSignal(rule.getId(), POINT_ID, BigDecimal.valueOf(20), e1);
        Assertions.assertTrue(withPointId.generateEvent().isPresent());
        ChallengePointsAwardedSignal noPointId = new ChallengePointsAwardedSignal(rule.getId(), null, BigDecimal.valueOf(20), e1);
        Assertions.assertFalse(noPointId.generateEvent().isPresent());
    }

    @DisplayName("Team Scoped: multiple non-repeatable winners")
    @Test
    public void teamScopedSingleChallenge() {
        TEvent e1 = TEvent.createWithTeam(U1, 1,100, EVT_A, 57);
        TEvent e2 = TEvent.createWithTeam(U2, 2,105, EVT_A, 83);
        TEvent e3 = TEvent.createWithTeam(U3, 2, 110, EVT_A, 98);
        TEvent e4 = TEvent.createWithTeam(U2, 2,155, EVT_A, 75);
        TEvent e5 = TEvent.createWithTeam(U1, 1,160, EVT_A, 88);
        TEvent e6 = TEvent.createWithTeam(U1, 1,165, EVT_A, 71);
        TEvent e7 = TEvent.createWithTeam(U4, 2,170, EVT_A, 64);
        TEvent e8 = TEvent.createWithTeam(U5, 2,175, EVT_A, 50);

        List<Signal> signals = new ArrayList<>();
        RuleContext<ChallengeRule> ruleContext = createRule(AWARD, WIN_3, START, 200, signals);
        ChallengeRule rule = ruleContext.getRule();
        rule.setFlags(Set.of());
        rule.setScope(ChallengeRule.ChallengeScope.TEAM);
        rule.setScopeId(2);
        Assertions.assertEquals(WIN_3, rule.getWinnerCount());
        Assertions.assertEquals(2, rule.getScopeId());
        Assertions.assertEquals(ChallengeRule.ChallengeScope.TEAM, rule.getScope());
        Assertions.assertTrue(rule.doesNotHaveFlag(ChallengeRule.REPEATABLE_WINNERS));
        ChallengeProcessor processor = new ChallengeProcessor(pool, ruleContext);
        submitOrder(processor, e1, e2, e3, e4, e5, e6, e7, e8);

        System.out.println(signals);
        assertStrict(signals,
                new ChallengeWinSignal(rule.getId(), e2, 1, U2, e2.getTimestamp(), e2.getExternalId()),
                new ChallengeWinSignal(rule.getId(), e3, 2, U3, e3.getTimestamp(), e3.getExternalId()),
                new ChallengeWinSignal(rule.getId(), e7, 3, U4, e7.getTimestamp(), e7.getExternalId()),
                new ChallengePointsAwardedSignal(rule.getId(), POINT_ID, AWARD, e2),
                new ChallengePointsAwardedSignal(rule.getId(), POINT_ID, AWARD, e3),
                new ChallengePointsAwardedSignal(rule.getId(), POINT_ID, AWARD, e7),
                new ChallengeOverSignal(rule.getId(), e8.asEventScope(), e8.getTimestamp(), ChallengeOverSignal.CompletionType.ALL_WINNERS_FOUND)
        );
    }

    @DisplayName("Team Scoped: multiple repeatable winners")
    @Test
    public void teamScopedRepeatableWinners() {
        TEvent e1 = TEvent.createWithTeam(U1, 1,100, EVT_A, 57);
        TEvent e2 = TEvent.createWithTeam(U2, 2,105, EVT_A, 83);
        TEvent e3 = TEvent.createWithTeam(U3, 2,110, EVT_A, 98);
        TEvent e4 = TEvent.createWithTeam(U2, 2,155, EVT_A, 75);
        TEvent e5 = TEvent.createWithTeam(U1, 1,160, EVT_A, 88);
        TEvent e6 = TEvent.createWithTeam(U1, 1,165, EVT_A, 71);
        TEvent e7 = TEvent.createWithTeam(U4, 2,170, EVT_A, 64);
        TEvent e8 = TEvent.createWithTeam(U5, 2,175, EVT_A, 50);

        List<Signal> signals = new ArrayList<>();
        RuleContext<ChallengeRule> ruleContext = createRule(AWARD, WIN_3, START, 200, signals);
        ChallengeRule rule = ruleContext.getRule();
        rule.setScope(ChallengeRule.ChallengeScope.TEAM);
        rule.setScopeId(2);
        Assertions.assertEquals(WIN_3, rule.getWinnerCount());
        Assertions.assertEquals(2, rule.getScopeId());
        Assertions.assertEquals(ChallengeRule.ChallengeScope.TEAM, rule.getScope());
        Assertions.assertTrue(rule.hasFlag(ChallengeRule.REPEATABLE_WINNERS));
        ChallengeProcessor processor = new ChallengeProcessor(pool, ruleContext);
        submitOrder(processor, e1, e2, e3, e4, e5, e6, e7, e8);

        System.out.println(signals);
        assertStrict(signals,
                new ChallengeWinSignal(rule.getId(), e2, 1, U2, e2.getTimestamp(), e2.getExternalId()),
                new ChallengeWinSignal(rule.getId(), e3, 2, U3, e3.getTimestamp(), e3.getExternalId()),
                new ChallengeWinSignal(rule.getId(), e4, 3, U2, e4.getTimestamp(), e4.getExternalId()),
                new ChallengePointsAwardedSignal(rule.getId(), POINT_ID, AWARD, e2),
                new ChallengePointsAwardedSignal(rule.getId(), POINT_ID, AWARD, e3),
                new ChallengePointsAwardedSignal(rule.getId(), POINT_ID, AWARD, e4),
                new ChallengeOverSignal(rule.getId(), e7.asEventScope(), e7.getTimestamp(), ChallengeOverSignal.CompletionType.ALL_WINNERS_FOUND),
                new ChallengeOverSignal(rule.getId(), e8.asEventScope(), e8.getTimestamp(), ChallengeOverSignal.CompletionType.ALL_WINNERS_FOUND)
        );
    }

    private BigDecimal asDecimal(long val) {
        return BigDecimal.valueOf(val).setScale(Constants.SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal award(Event event, int position, ChallengeRule rule) {
        return BigDecimal.valueOf((long)event.getFieldValue("value") - 50);
    }

    private boolean check(Event event, AbstractRule rule, ExecutionContext context) {
        return (long)event.getFieldValue("value") >= 50;
    }

    private RuleContext<ChallengeRule> createRule(BigDecimal points, int winners, long start, long end, Collection<Signal> signals) {
        ChallengeRule rule = new ChallengeRule("test.challenge.rule");
        rule.setEventTypeMatcher(new SingleEventTypeMatcher(EVT_A));
        rule.setScope(ChallengeRule.ChallengeScope.GAME);
        rule.setAwardPoints(points);
        rule.setStartAt(start);
        rule.setExpireAt(end);
        rule.setCriteria(this::check);
        rule.setWinnerCount(winners);
        rule.setPointId(POINT_ID);
        rule.setFlags(Set.of(ChallengeRule.REPEATABLE_WINNERS));
        return new RuleContext<>(rule, fromConsumer(signals::add));
    }

}
