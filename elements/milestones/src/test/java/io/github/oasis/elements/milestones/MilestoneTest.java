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

package io.github.oasis.elements.milestones;

import io.github.oasis.core.Event;
import io.github.oasis.core.context.ExecutionContext;
import io.github.oasis.core.elements.EventBiValueResolver;
import io.github.oasis.core.elements.RuleContext;
import io.github.oasis.core.elements.Signal;
import io.github.oasis.core.elements.matchers.SingleEventTypeMatcher;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static io.github.oasis.elements.milestones.MilestoneRule.SKIP_NEGATIVE_VALUES;

/**
 * @author Isuru Weerarathna
 */
@DisplayName("Milestones")
public class MilestoneTest extends AbstractRuleTest {

    private static final String EVT_SP = "star.points";
    private static final String EVT_CP = "coupan.points";
    private static final String EVT_SOBA = "stackoverflow.bounty.awarded";
    private static final String EVT_SOAA = "stackoverflow.answer.accepted";
    private static final String EVT_A = "event.a";
    private static final String EVT_B = "unknown.event";

    private static final int L_0 = 0;
    private static final int L_1 = 1;
    private static final int L_2 = 2;
    private static final int L_3 = 3;

    private static final String MILESTONES_YML = "milestones-testrun.yml";
    private static final String TOTAL_REPUTATIONS = "Total-Reputations";
    private static final String TEST_SINGLE_LEVEL = "Test-Single-Level";
    private static final String TEST_STAR_POINTS = "Test-Star-Points";
    private static final String TEST_CHALLENGE_WIN_POINTS = "Test-Challenge-Win-Points";
    private static final String TEST_CHALLENGE_WIN_POINTS_WITHOUT_FILTER = "Test-Challenge-Win-Points-Without-Filter";
    private static final String TEST_CHALLENGE_WIN_POINTS_TRACK_PENALTIES = "Test-Challenge-Win-Points-Track-Penalties";

    @DisplayName("No levels")
    @Test
    public void testNoLevels() {
        TEvent e1 = TEvent.createKeyValue(100, EVT_A, 87);
        TEvent e2 = TEvent.createKeyValue(105, EVT_A, 53);
        TEvent e3 = TEvent.createKeyValue(105, EVT_A, 34);

        List<Signal> signals = new ArrayList<>();
        RuleContext<MilestoneRule> ruleContext = createRule(signals, this::extractValue);
        MilestoneProcessor milestoneProcessor = new MilestoneProcessor(pool, ruleContext);
        submitOrder(milestoneProcessor, e1, e2, e3);

        System.out.println(signals);
        Assertions.assertEquals(0, signals.size());
    }

    @DisplayName("No value extractor")
    @Test
    public void testWhenValueExtractorNotGiven() {
        TEvent e1 = TEvent.createKeyValue(100, EVT_A, 87);
        TEvent e2 = TEvent.createKeyValue(105, EVT_A, 53);
        TEvent e3 = TEvent.createKeyValue(105, EVT_A, 34);

        List<Signal> signals = new ArrayList<>();
        MilestoneRule rule = loadRule(MILESTONES_YML, TOTAL_REPUTATIONS);
        RuleContext<MilestoneRule> ruleContext = createRule(rule, signals);
        MilestoneProcessor milestoneProcessor = new MilestoneProcessor(pool, ruleContext);
        submitOrder(milestoneProcessor, e1, e2, e3);

        System.out.println(signals);
        Assertions.assertEquals(0, signals.size());
    }

    @DisplayName("Mismatched event types")
    @Test
    public void testMismatchedEventTypes() {
        TEvent e1 = TEvent.createKeyValue(100, EVT_B, 87);
        TEvent e2 = TEvent.createKeyValue(105, EVT_B, 53);
        TEvent e3 = TEvent.createKeyValue(105, EVT_B, 34);

        List<Signal> signals = new ArrayList<>();
        MilestoneRule rule = loadRule(MILESTONES_YML, TOTAL_REPUTATIONS);
        RuleContext<MilestoneRule> ruleContext = createRule(rule, signals);
        MilestoneProcessor milestoneProcessor = new MilestoneProcessor(pool, ruleContext);
        submitOrder(milestoneProcessor, e1, e2, e3);

        System.out.println(signals);
        Assertions.assertEquals(0, signals.size());
    }

    @DisplayName("Single level")
    @Test
    public void testSingleLevel() {
        TEvent e1 = TEvent.createKeyValue(100, EVT_SOBA, 87);
        TEvent e2 = TEvent.createKeyValue(105, EVT_SOBA, 53);
        TEvent e3 = TEvent.createKeyValue(105, EVT_SOBA, 34);

        List<Signal> signals = new ArrayList<>();
        MilestoneRule rule = loadRule(MILESTONES_YML, TEST_SINGLE_LEVEL);
        RuleContext<MilestoneRule> ruleContext = createRule(rule, signals);
        MilestoneProcessor milestoneProcessor = new MilestoneProcessor(pool, ruleContext);
        submitOrder(milestoneProcessor, e1, e2, e3);

        System.out.println(signals);
        assertStrict(signals,
                new MilestoneSignal(ruleContext.getRule().getId(), L_0, L_1, BigDecimal.valueOf(140.0), e2));
    }

    @DisplayName("Multiple Levels")
    @Test
    public void testMultipleLevels() {
        TEvent e1 = TEvent.createKeyValue(100, EVT_SP, 87);
        TEvent e2 = TEvent.createKeyValue(105, EVT_CP, 53);
        TEvent e3 = TEvent.createKeyValue(110, EVT_SP, 34);
        TEvent e4 = TEvent.createKeyValue(115, EVT_SP, 11);
        TEvent e5 = TEvent.createKeyValue(120, EVT_CP, 84);
        TEvent e6 = TEvent.createKeyValue(125, EVT_SP, 92);

        List<Signal> signals = new ArrayList<>();
        MilestoneRule rule = loadRule(MILESTONES_YML, TEST_STAR_POINTS);
        RuleContext<MilestoneRule> ruleContext = createRule(rule, signals);
        MilestoneProcessor milestoneProcessor = new MilestoneProcessor(pool, ruleContext);
        submitOrder(milestoneProcessor, e1, e2, e3, e4, e5, e6);

        System.out.println(signals);
        assertStrict(signals,
                new MilestoneSignal(ruleContext.getRule().getId(), L_0, L_1, BigDecimal.valueOf(140.0), e2),
                new MilestoneSignal(ruleContext.getRule().getId(), L_1, L_2, BigDecimal.valueOf(269.0), e5),
                new MilestoneSignal(ruleContext.getRule().getId(), L_2, L_3, BigDecimal.valueOf(361.0), e6));
    }

    @DisplayName("Multiple Levels: No fluctuations")
    @Test
    public void testMultipleLevelsNoFluctuations() {
        TEvent e1 = TEvent.createKeyValue(100, EVT_CP, 87);
        TEvent e2 = TEvent.createKeyValue(105, EVT_SP, 53);
        TEvent e3 = TEvent.createKeyValue(110, EVT_CP, -74);
        TEvent e4 = TEvent.createKeyValue(115, EVT_SP, 11);
        TEvent e5 = TEvent.createKeyValue(120, EVT_CP, 84);
        TEvent e6 = TEvent.createKeyValue(125, EVT_SP, 92);

        List<Signal> signals = new ArrayList<>();
        MilestoneRule rule = loadRule(MILESTONES_YML, TEST_STAR_POINTS);
        RuleContext<MilestoneRule> ruleContext = createRule(rule, signals);
        MilestoneProcessor milestoneProcessor = new MilestoneProcessor(pool, ruleContext);
        submitOrder(milestoneProcessor, e1, e2, e3, e4, e5, e6);

        System.out.println(signals);
        assertStrict(signals,
                new MilestoneSignal(ruleContext.getRule().getId(), L_0, L_1, BigDecimal.valueOf(140.0), e2),
                new MilestoneSignal(ruleContext.getRule().getId(), L_1, L_2, BigDecimal.valueOf(235.0), e5),
                new MilestoneSignal(ruleContext.getRule().getId(), L_2, L_3, BigDecimal.valueOf(327.0), e6));
    }

    @DisplayName("Multiple Levels: Penalties with fluctuations")
    @Test
    public void testMultipleLevelsAffectPenalties() {
        TEvent e1 = TEvent.createKeyValue(100, EVT_CP, 87);
        TEvent e2 = TEvent.createKeyValue(105, EVT_CP, 53);
        TEvent e3 = TEvent.createKeyValue(110, EVT_SP, -74);
        TEvent e4 = TEvent.createKeyValue(115, EVT_SP, 11);
        TEvent e5 = TEvent.createKeyValue(120, EVT_CP, 84);
        TEvent e6 = TEvent.createKeyValue(125, EVT_SP, 92);

        List<Signal> signals = new ArrayList<>();
        MilestoneRule rule = loadRule(MILESTONES_YML, TEST_STAR_POINTS);
        RuleContext<MilestoneRule> ruleContext = createRule(rule, signals);
        rule.setFlags(new HashSet<>());
        MilestoneProcessor milestoneProcessor = new MilestoneProcessor(pool, ruleContext);
        submitOrder(milestoneProcessor, e1, e2, e3, e4, e5, e6);

        System.out.println(signals);
        assertStrict(signals,
                new MilestoneSignal(rule.getId(), L_0, L_1, BigDecimal.valueOf(140.0), e2),
                new MilestoneSignal(rule.getId(), L_1, L_0, BigDecimal.valueOf(66.0), e3),
                new MilestoneSignal(rule.getId(), L_0, L_1, BigDecimal.valueOf(161.0), e5),
                new MilestoneSignal(rule.getId(), L_1, L_2, BigDecimal.valueOf(253.0), e6));
    }

    @DisplayName("Multiple Levels: Single penalty reset all levels")
    @Test
    public void testMultipleLevelsPenaltyResetAll() {
        TEvent e1 = TEvent.createKeyValue(100, EVT_CP, 87);
        TEvent e2 = TEvent.createKeyValue(105, EVT_SP, 53);
        TEvent e3 = TEvent.createKeyValue(110, EVT_CP, 34);
        TEvent e4 = TEvent.createKeyValue(115, EVT_SP, 11);
        TEvent e5 = TEvent.createKeyValue(120, EVT_CP, 84);
        TEvent e6 = TEvent.createKeyValue(125, EVT_SP, 92);
        TEvent e7 = TEvent.createKeyValue(125, EVT_SP, -400);

        List<Signal> signals = new ArrayList<>();
        MilestoneRule rule = loadRule(MILESTONES_YML, TEST_STAR_POINTS);
        RuleContext<MilestoneRule> ruleContext = createRule(rule, signals);
        rule.setFlags(new HashSet<>());
        MilestoneProcessor milestoneProcessor = new MilestoneProcessor(pool, ruleContext);
        submitOrder(milestoneProcessor, e1, e2, e3, e4, e5, e6, e7);

        System.out.println(signals);
        assertStrict(signals,
                new MilestoneSignal(rule.getId(), L_0, L_1, BigDecimal.valueOf(140.0), e2),
                new MilestoneSignal(rule.getId(), L_1, L_2, BigDecimal.valueOf(269.0), e5),
                new MilestoneSignal(rule.getId(), L_2, L_3, BigDecimal.valueOf(361.0), e6),
                new MilestoneSignal(rule.getId(), L_3, L_0, BigDecimal.valueOf(-39.0), e7));
    }

    @DisplayName("With condition")
    @Test
    public void testWithCondition() {
        TEvent e1 = TEvent.createKeyValue(100, EVT_SOBA, 87);
        TEvent e2 = TEvent.createKeyValue(105, EVT_SOBA, 53);
        TEvent e3 = TEvent.createKeyValue(110, EVT_SOBA, 34);
        TEvent e4 = TEvent.createKeyValue(115, EVT_SOAA, 11);
        TEvent e5 = TEvent.createKeyValue(120, EVT_SOAA, 84);
        TEvent e6 = TEvent.createKeyValue(125, EVT_SOAA, 92);
        TEvent e7 = TEvent.createKeyValue(126, EVT_B, 99);

        List<Signal> signals = new ArrayList<>();
        MilestoneRule rule = loadRule(MILESTONES_YML, TEST_CHALLENGE_WIN_POINTS);
        RuleContext<MilestoneRule> ruleContext = createRule(rule, signals);
        MilestoneProcessor milestoneProcessor = new MilestoneProcessor(pool, ruleContext);
        submitOrder(milestoneProcessor, e1, e2, e3, e4, e5, e6, e7);

        System.out.println(signals);
        assertStrict(signals,
                new MilestoneSignal(rule.getId(), L_0, L_1, BigDecimal.valueOf(171.0), e5),
                new MilestoneSignal(rule.getId(), L_1, L_2, BigDecimal.valueOf(171.0 + 92.0), e6));
    }

    @DisplayName("Single event passes multiple levels from first level")
    @Test
    public void testEventPassesMultipleLevels() {
        TEvent e1 = TEvent.createKeyValue(100, EVT_SOAA, 87);
        TEvent e2 = TEvent.createKeyValue(105, EVT_SOBA, 253);
        TEvent e3 = TEvent.createKeyValue(110, EVT_SOAA, 34);
        TEvent e4 = TEvent.createKeyValue(115, EVT_SOBA, 11);
        TEvent e5 = TEvent.createKeyValue(120, EVT_SOAA, 84);
        TEvent e6 = TEvent.createKeyValue(125, EVT_SOBA, 92);

        List<Signal> signals = new ArrayList<>();
        MilestoneRule rule = loadRule(MILESTONES_YML, TEST_CHALLENGE_WIN_POINTS_WITHOUT_FILTER);
        RuleContext<MilestoneRule> ruleContext = createRule(rule, signals);
        MilestoneProcessor milestoneProcessor = new MilestoneProcessor(pool, ruleContext);
        submitOrder(milestoneProcessor, e1, e2, e3, e4, e5, e6);

        System.out.println(signals);
        assertStrict(signals,
                new MilestoneSignal(rule.getId(), L_0, L_3, BigDecimal.valueOf(340.0), e2));
    }

    @DisplayName("Single event passes multiple levels from a middle level")
    @Test
    public void testEventPassesMiddleMultipleLevels() {
        TEvent e1 = TEvent.createKeyValue(100, EVT_SOBA, 187);
        TEvent e2 = TEvent.createKeyValue(105, EVT_SOAA, 12);
        TEvent e3 = TEvent.createKeyValue(110, EVT_SOAA, 341);
        TEvent e4 = TEvent.createKeyValue(115, EVT_SOBA, 11);
        TEvent e5 = TEvent.createKeyValue(120, EVT_SOBA, 84);
        TEvent e6 = TEvent.createKeyValue(125, EVT_SOAA, 92);

        List<Signal> signals = new ArrayList<>();
        MilestoneRule rule = loadRule(MILESTONES_YML, TEST_CHALLENGE_WIN_POINTS_WITHOUT_FILTER);
        RuleContext<MilestoneRule> ruleContext = createRule(rule, signals);
        MilestoneProcessor milestoneProcessor = new MilestoneProcessor(pool, ruleContext);
        submitOrder(milestoneProcessor, e1, e2, e3, e4, e5, e6);

        System.out.println(signals);
        assertStrict(signals,
                new MilestoneSignal(ruleContext.getRule().getId(), L_0, L_1, BigDecimal.valueOf(187.0), e1),
                new MilestoneSignal(ruleContext.getRule().getId(), L_1, L_3, BigDecimal.valueOf(540.0), e3));
    }

    @DisplayName("Track penalties")
    @Test
    public void testTrackNegatives() {
        TEvent e1 = TEvent.createKeyValue(100, EVT_SOBA, 187);
        TEvent e2 = TEvent.createKeyValue(105, EVT_SOBA, 12);
        TEvent e3 = TEvent.createKeyValue(110, EVT_SOAA, -98);
        TEvent e4 = TEvent.createKeyValue(115, EVT_SOAA, 11);

        List<Signal> signals = new ArrayList<>();
        MilestoneRule rule = loadRule(MILESTONES_YML, TEST_CHALLENGE_WIN_POINTS_TRACK_PENALTIES);
        RuleContext<MilestoneRule> ruleContext = createRule(rule, signals);
        MilestoneProcessor milestoneProcessor = new MilestoneProcessor(pool, ruleContext);
        submitOrder(milestoneProcessor, e1, e2, e3, e4);

        System.out.println(signals);
        assertStrict(signals,
                new MilestoneSignal(rule.getId(), L_0, L_1, BigDecimal.valueOf(187.0), e1));

        assertRedisHashMapValue(MilestoneIDs.getGameUserMilestonesSummary(TEvent.GAME_ID, TEvent.USER_ID),
                rule.getId() + ":penalties",
                "-98");
    }

    private BigDecimal extractValue(Event event, MilestoneRule rule, ExecutionContext context) {
        return BigDecimal.valueOf((long)event.getFieldValue("value"));
    }

    private RuleContext<MilestoneRule> createRule(Collection<Signal> collector, EventBiValueResolver<MilestoneRule, ExecutionContext> extractor, MilestoneRule.Level... levels) {
        MilestoneRule rule = new MilestoneRule("test.milestone.rule");
        rule.setEventTypeMatcher(new SingleEventTypeMatcher(EVT_A));
        rule.setValueExtractor(extractor);
        rule.setLevels(Arrays.asList(levels));
        rule.setFlags(Set.of(SKIP_NEGATIVE_VALUES));
        return new RuleContext<>(rule, fromConsumer(collector::add));
    }

    private RuleContext<MilestoneRule> createRule(MilestoneRule rule, Collection<Signal> collector) {
        return new RuleContext<>(rule, fromConsumer(collector::add));
    }
}
