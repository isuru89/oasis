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

import io.github.oasis.engine.elements.AbstractRule;
import io.github.oasis.engine.elements.AbstractRuleTest;
import io.github.oasis.engine.elements.Signal;
import io.github.oasis.engine.model.ExecutionContext;
import io.github.oasis.engine.model.RuleContext;
import io.github.oasis.engine.model.TEvent;
import io.github.oasis.engine.utils.Constants;
import io.github.oasis.model.Event;
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
@DisplayName("Challenges - Out of order winners")
public class ChallengeOutOfOrderTest extends AbstractRuleTest {

    private static final String EVT_A = "a";

    static final BigDecimal AWARD = BigDecimal.valueOf(100).setScale(Constants.SCALE, RoundingMode.HALF_UP);

    static final long START = 0;

    static final long U1 = 1;
    static final long U2 = 2;
    static final long U3 = 3;
    static final long U4 = 4;
    static final long U5 = 5;

    static final int WIN_3 = 3;

    static final String POINT_ID = "challenge.points";

    @DisplayName("Team Scope: non-repeatable")
    @Test
    public void testTeamNonRepeatable() {
        TEvent e1 = TEvent.createWithTeam(U1, 1,100, EVT_A, 57);
        TEvent e2 = TEvent.createWithTeam(U1, 2,105, EVT_A, 83);
        TEvent e3 = TEvent.createWithTeam(U3, 2,120, EVT_A, 98);
        TEvent e4 = TEvent.createWithTeam(U1, 2,160, EVT_A, 75);
        TEvent e5 = TEvent.createWithTeam(U1, 1,160, EVT_A, 88);
        TEvent e6 = TEvent.createWithTeam(U1, 1,165, EVT_A, 71);
        TEvent e7 = TEvent.createWithTeam(U5, 2,135, EVT_A, 64);
        TEvent e8 = TEvent.createWithTeam(U4, 2,180, EVT_A, 50);

        List<Signal> signals = new ArrayList<>();
        RuleContext<ChallengeRule> ruleContext = createRule(AWARD, WIN_3, START, 200, signals);
        ChallengeRule rule = ruleContext.getRule();
        rule.setFlags(Set.of(ChallengeRule.OUT_OF_ORDER_WINNERS));
        rule.setScope(ChallengeRule.ChallengeScope.TEAM);
        rule.setScopeId(2);
        Assertions.assertEquals(WIN_3, rule.getWinnerCount());
        Assertions.assertEquals(2, rule.getScopeId());
        Assertions.assertEquals(ChallengeRule.ChallengeScope.TEAM, rule.getScope());
        Assertions.assertTrue(rule.doesNotHaveFlag(ChallengeRule.REPEATABLE_WINNERS));
        Assertions.assertTrue(rule.hasFlag(ChallengeRule.OUT_OF_ORDER_WINNERS));
        ChallengeProcessor processor = new ChallengeProcessor(pool, eventReadWrite, ruleContext);
        String ruleId = rule.getId();
        ChallengeOverEvent overEvent = ChallengeOverEvent.createFor(e1.getGameId(), ruleId);
        submitOrder(processor, e1, e2, e3, e4, e5, e6, e7, e8, overEvent);

        System.out.println(signals);
        assertStrict(signals,
                new ChallengeWinSignal(ruleId, e2, 1, U1, e2.getTimestamp(), e2.getExternalId()),
                new ChallengeWinSignal(ruleId, e3, 2, U3, e3.getTimestamp(), e3.getExternalId()),
                new ChallengeWinSignal(ruleId, e7, 3, U5, e7.getTimestamp(), e7.getExternalId()),
                new ChallengePointsAwardedSignal(ruleId, POINT_ID, AWARD, e2),
                new ChallengePointsAwardedSignal(ruleId, POINT_ID, AWARD, e3),
                new ChallengePointsAwardedSignal(ruleId, POINT_ID, AWARD, e7)
        );
    }

    @DisplayName("Team Scope: repeatable winners")
    @Test
    public void testTeamRepeatable() {
        TEvent e1 = TEvent.createWithTeam(U1, 1,100, EVT_A, 57);
        TEvent e2 = TEvent.createWithTeam(U1, 2,105, EVT_A, 83);
        TEvent e3 = TEvent.createWithTeam(U3, 2,120, EVT_A, 98);
        TEvent e4 = TEvent.createWithTeam(U1, 2,110, EVT_A, 75);
        TEvent e5 = TEvent.createWithTeam(U1, 1,160, EVT_A, 88);
        TEvent e6 = TEvent.createWithTeam(U1, 1,165, EVT_A, 71);
        TEvent e7 = TEvent.createWithTeam(U5, 2,107, EVT_A, 64);
        TEvent e8 = TEvent.createWithTeam(U4, 2,180, EVT_A, 50);

        List<Signal> signals = new ArrayList<>();
        RuleContext<ChallengeRule> ruleContext = createRule(AWARD, WIN_3, START, 200, signals);
        ChallengeRule rule = ruleContext.getRule();
        rule.setCustomAwardPoints(this::award);
        rule.setFlags(Set.of(ChallengeRule.OUT_OF_ORDER_WINNERS, ChallengeRule.REPEATABLE_WINNERS));
        rule.setScope(ChallengeRule.ChallengeScope.TEAM);
        rule.setScopeId(2);
        Assertions.assertTrue(rule.hasFlag(ChallengeRule.REPEATABLE_WINNERS));
        Assertions.assertTrue(rule.hasFlag(ChallengeRule.OUT_OF_ORDER_WINNERS));
        ChallengeProcessor processor = new ChallengeProcessor(pool, eventReadWrite, ruleContext);
        String ruleId = rule.getId();
        ChallengeOverEvent overEvent = ChallengeOverEvent.createFor(e1.getGameId(), ruleId);
        submitOrder(processor, e1, e2, e3, e4, e5, e6, e7, e8, overEvent);

        System.out.println(signals);
        assertStrict(signals,
                new ChallengeWinSignal(ruleId, e2, 1, U1, e2.getTimestamp(), e2.getExternalId()),
                new ChallengeWinSignal(ruleId, e7, 2, U5, e7.getTimestamp(), e7.getExternalId()),
                new ChallengeWinSignal(ruleId, e4, 3, U1, e4.getTimestamp(), e4.getExternalId()),
                new ChallengePointsAwardedSignal(ruleId, POINT_ID, asDecimal(33), e2),
                new ChallengePointsAwardedSignal(ruleId, POINT_ID, asDecimal(14), e7),
                new ChallengePointsAwardedSignal(ruleId, POINT_ID, asDecimal(25), e4)
        );
    }

    @DisplayName("Team Scope: no winners until challenge over event")
    @Test
    public void testTeamNoWinnersUntilChallengeIsOver() {
        TEvent e1 = TEvent.createWithTeam(U1, 1,100, EVT_A, 57);
        TEvent e2 = TEvent.createWithTeam(U1, 2,105, EVT_A, 83);
        TEvent e3 = TEvent.createWithTeam(U3, 2,120, EVT_A, 98);
        TEvent e4 = TEvent.createWithTeam(U1, 2,110, EVT_A, 75);
        TEvent e5 = TEvent.createWithTeam(U1, 1,160, EVT_A, 88);
        TEvent e6 = TEvent.createWithTeam(U1, 1,165, EVT_A, 71);
        TEvent e7 = TEvent.createWithTeam(U5, 2,107, EVT_A, 64);
        TEvent e8 = TEvent.createWithTeam(U4, 2,180, EVT_A, 50);

        List<Signal> signals = new ArrayList<>();
        RuleContext<ChallengeRule> ruleContext = createRule(AWARD, WIN_3, START, 200, signals);
        ChallengeRule rule = ruleContext.getRule();
        rule.setCustomAwardPoints(this::award);
        rule.setFlags(Set.of(ChallengeRule.OUT_OF_ORDER_WINNERS, ChallengeRule.REPEATABLE_WINNERS));
        rule.setScope(ChallengeRule.ChallengeScope.TEAM);
        rule.setScopeId(2);
        ChallengeProcessor processor = new ChallengeProcessor(pool, eventReadWrite, ruleContext);
        submitOrder(processor, e1, e2, e3, e4, e5, e6, e7, e8);

        System.out.println(signals);
        Assertions.assertTrue(signals.isEmpty());
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
        rule.setForEvent(EVT_A);
        rule.setScope(ChallengeRule.ChallengeScope.GAME);
        rule.setAwardPoints(points);
        rule.setStartAt(start);
        rule.setExpireAt(end);
        rule.setCriteria(this::check);
        rule.setWinnerCount(winners);
        rule.setPointId(POINT_ID);
        rule.setFlags(Set.of(ChallengeRule.OUT_OF_ORDER_WINNERS));
        return new RuleContext<>(rule, fromConsumer(signals::add));
    }

}
