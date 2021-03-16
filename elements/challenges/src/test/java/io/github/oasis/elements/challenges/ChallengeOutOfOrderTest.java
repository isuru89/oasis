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

package io.github.oasis.elements.challenges;

import io.github.oasis.core.elements.RuleContext;
import io.github.oasis.core.elements.Signal;
import io.github.oasis.core.utils.Constants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Isuru Weerarathna
 */
@DisplayName("Challenges - Out of order winners")
public class ChallengeOutOfOrderTest extends AbstractRuleTest {

    private static final String EVT_A = "user.scored";

    static final BigDecimal AWARD = BigDecimal.valueOf(100).setScale(Constants.SCALE, RoundingMode.HALF_UP);

    static final long START = 0;

    static final long U1 = 1;
    static final long U2 = 2;
    static final long U3 = 3;
    static final long U4 = 4;
    static final long U5 = 5;

    static final int WIN_3 = 3;

    static final String POINT_ID = "challenge.points";
    public static final String CHALLENGES_OUT_OF_ORDER_YML = "challenges-out-of-order.yml";

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
        ChallengeRule rule = loadRule(CHALLENGES_OUT_OF_ORDER_YML, "TEAM_SCOPED_MULTI_WINNER_OOO");
        RuleContext<ChallengeRule> ruleContext = createRule(rule, signals);
        Assertions.assertEquals(WIN_3, rule.getWinnerCount());
        Assertions.assertEquals(2, rule.getScopeId());
        Assertions.assertEquals(ChallengeRule.ChallengeScope.TEAM, rule.getScope());
        Assertions.assertTrue(rule.doesNotHaveFlag(ChallengeRule.REPEATABLE_WINNERS));
        Assertions.assertTrue(rule.hasFlag(ChallengeRule.OUT_OF_ORDER_WINNERS));
        ChallengeProcessor processor = new ChallengeProcessor(pool, eventReadWriteHandler, ruleContext);
        String ruleId = rule.getId();
        ChallengeOverEvent overEvent = ChallengeOverEvent.createFor(e1.getGameId(), ruleId);
        submitOrder(processor, e1, e2, e3, e4, e5, e6, e7, e8, overEvent);

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
        ChallengeRule rule = loadRule(CHALLENGES_OUT_OF_ORDER_YML, "TEAM_SCOPED_MULTI_WINNER_OOO_REPEATABLE");
        RuleContext<ChallengeRule> ruleContext = createRule(rule, signals);
        Assertions.assertTrue(rule.hasFlag(ChallengeRule.REPEATABLE_WINNERS));
        Assertions.assertTrue(rule.hasFlag(ChallengeRule.OUT_OF_ORDER_WINNERS));
        ChallengeProcessor processor = new ChallengeProcessor(pool, eventReadWriteHandler, ruleContext);
        String ruleId = rule.getId();
        ChallengeOverEvent overEvent = ChallengeOverEvent.createFor(e1.getGameId(), ruleId);
        submitOrder(processor, e1, e2, e3, e4, e5, e6, e7, e8, overEvent);

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
        ChallengeRule rule = loadRule(CHALLENGES_OUT_OF_ORDER_YML, "TEAM_SCOPED_MULTI_WINNER_OOO_REPEATABLE");
        RuleContext<ChallengeRule> ruleContext = createRule(rule, signals);
        ChallengeProcessor processor = new ChallengeProcessor(pool, eventReadWriteHandler, ruleContext);
        submitOrder(processor, e1, e2, e3, e4, e5, e6, e7, e8);

        System.out.println(signals);
        Assertions.assertTrue(signals.isEmpty());
    }

    private BigDecimal asDecimal(long val) {
        return BigDecimal.valueOf(val).setScale(Constants.SCALE, RoundingMode.HALF_UP);
    }

    private RuleContext<ChallengeRule> createRule(ChallengeRule rule, Collection<Signal> signals) {
        return new RuleContext<>(rule, fromConsumer(signals::add));
    }
}
