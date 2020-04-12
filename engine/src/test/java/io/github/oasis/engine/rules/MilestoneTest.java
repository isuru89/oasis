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

import io.github.oasis.engine.model.EventBiValueResolver;
import io.github.oasis.engine.model.ExecutionContext;
import io.github.oasis.engine.model.ID;
import io.github.oasis.engine.model.RuleContext;
import io.github.oasis.engine.model.TEvent;
import io.github.oasis.engine.processors.MilestoneProcessor;
import io.github.oasis.engine.rules.signals.MilestoneSignal;
import io.github.oasis.engine.rules.signals.Signal;
import io.github.oasis.model.Event;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static io.github.oasis.engine.rules.MilestoneRule.MilestoneFlag.SKIP_NEGATIVE_VALUES;
import static io.github.oasis.engine.rules.MilestoneRule.MilestoneFlag.TRACK_PENALTIES;

/**
 * @author Isuru Weerarathna
 */
@DisplayName("Milestones")
public class MilestoneTest extends AbstractRuleTest {

    private static final String EVT_A = "a";
    private static final String EVT_B = "b";

    private static final int L_0 = 0;
    private static final int L_1 = 1;
    private static final int L_2 = 2;
    private static final int L_3 = 3;

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
        RuleContext<MilestoneRule> ruleContext = createRule(signals, null, aLevel(1, 100));
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
        RuleContext<MilestoneRule> ruleContext = createRule(signals, this::extractValue, aLevel(1, 100));
        MilestoneProcessor milestoneProcessor = new MilestoneProcessor(pool, ruleContext);
        submitOrder(milestoneProcessor, e1, e2, e3);

        System.out.println(signals);
        Assertions.assertEquals(0, signals.size());
    }

    @DisplayName("Single level")
    @Test
    public void testSingleLevel() {
        TEvent e1 = TEvent.createKeyValue(100, EVT_A, 87);
        TEvent e2 = TEvent.createKeyValue(105, EVT_A, 53);
        TEvent e3 = TEvent.createKeyValue(105, EVT_A, 34);

        List<Signal> signals = new ArrayList<>();
        RuleContext<MilestoneRule> ruleContext = createRule(signals, this::extractValue, aLevel(1, 100));
        MilestoneProcessor milestoneProcessor = new MilestoneProcessor(pool, ruleContext);
        submitOrder(milestoneProcessor, e1, e2, e3);

        System.out.println(signals);
        assertStrict(signals,
                new MilestoneSignal(ruleContext.getRule().getId(), L_0, L_1, BigDecimal.valueOf(140.0), e2));
    }

    @DisplayName("Multiple Levels")
    @Test
    public void testMultipleLevels() {
        TEvent e1 = TEvent.createKeyValue(100, EVT_A, 87);
        TEvent e2 = TEvent.createKeyValue(105, EVT_A, 53);
        TEvent e3 = TEvent.createKeyValue(110, EVT_A, 34);
        TEvent e4 = TEvent.createKeyValue(115, EVT_A, 11);
        TEvent e5 = TEvent.createKeyValue(120, EVT_A, 84);
        TEvent e6 = TEvent.createKeyValue(125, EVT_A, 92);

        List<Signal> signals = new ArrayList<>();
        RuleContext<MilestoneRule> ruleContext = createRule(signals, this::extractValue,
                aLevel(1, 100),
                aLevel(2, 200),
                aLevel(3, 300));
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
        TEvent e1 = TEvent.createKeyValue(100, EVT_A, 87);
        TEvent e2 = TEvent.createKeyValue(105, EVT_A, 53);
        TEvent e3 = TEvent.createKeyValue(110, EVT_A, -74);
        TEvent e4 = TEvent.createKeyValue(115, EVT_A, 11);
        TEvent e5 = TEvent.createKeyValue(120, EVT_A, 84);
        TEvent e6 = TEvent.createKeyValue(125, EVT_A, 92);

        List<Signal> signals = new ArrayList<>();
        RuleContext<MilestoneRule> ruleContext = createRule(signals, this::extractValue,
                aLevel(1, 100),
                aLevel(2, 200),
                aLevel(3, 300));
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
        TEvent e1 = TEvent.createKeyValue(100, EVT_A, 87);
        TEvent e2 = TEvent.createKeyValue(105, EVT_A, 53);
        TEvent e3 = TEvent.createKeyValue(110, EVT_A, -74);
        TEvent e4 = TEvent.createKeyValue(115, EVT_A, 11);
        TEvent e5 = TEvent.createKeyValue(120, EVT_A, 84);
        TEvent e6 = TEvent.createKeyValue(125, EVT_A, 92);

        List<Signal> signals = new ArrayList<>();
        RuleContext<MilestoneRule> ruleContext = createRule(signals, this::extractValue,
                aLevel(1, 100),
                aLevel(2, 200),
                aLevel(3, 300));
        MilestoneRule rule = ruleContext.getRule();
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
        TEvent e1 = TEvent.createKeyValue(100, EVT_A, 87);
        TEvent e2 = TEvent.createKeyValue(105, EVT_A, 53);
        TEvent e3 = TEvent.createKeyValue(110, EVT_A, 34);
        TEvent e4 = TEvent.createKeyValue(115, EVT_A, 11);
        TEvent e5 = TEvent.createKeyValue(120, EVT_A, 84);
        TEvent e6 = TEvent.createKeyValue(125, EVT_A, 92);
        TEvent e7 = TEvent.createKeyValue(125, EVT_A, -400);

        List<Signal> signals = new ArrayList<>();
        RuleContext<MilestoneRule> ruleContext = createRule(signals, this::extractValue,
                aLevel(1, 100),
                aLevel(2, 200),
                aLevel(3, 300));
        MilestoneRule rule = ruleContext.getRule();
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
        TEvent e1 = TEvent.createKeyValue(100, EVT_A, 87);
        TEvent e2 = TEvent.createKeyValue(105, EVT_A, 53);
        TEvent e3 = TEvent.createKeyValue(110, EVT_A, 34);
        TEvent e4 = TEvent.createKeyValue(115, EVT_A, 11);
        TEvent e5 = TEvent.createKeyValue(120, EVT_A, 84);
        TEvent e6 = TEvent.createKeyValue(125, EVT_A, 92);

        List<Signal> signals = new ArrayList<>();
        RuleContext<MilestoneRule> ruleContext = createRule(signals, this::extractValue,
                aLevel(1, 100),
                aLevel(2, 200));
        ruleContext.getRule().setCondition((event, rule, ctx) -> (long) event.getFieldValue("value") >= 75);
        MilestoneProcessor milestoneProcessor = new MilestoneProcessor(pool, ruleContext);
        submitOrder(milestoneProcessor, e1, e2, e3, e4, e5, e6);

        System.out.println(signals);
        assertStrict(signals,
                new MilestoneSignal(ruleContext.getRule().getId(), L_0, L_1, BigDecimal.valueOf(171.0), e5),
                new MilestoneSignal(ruleContext.getRule().getId(), L_1, L_2, BigDecimal.valueOf(171.0 + 92.0), e6));
    }

    @DisplayName("Single event passes multiple levels from first level")
    @Test
    public void testEventPassesMultipleLevels() {
        TEvent e1 = TEvent.createKeyValue(100, EVT_A, 87);
        TEvent e2 = TEvent.createKeyValue(105, EVT_A, 253);
        TEvent e3 = TEvent.createKeyValue(110, EVT_A, 34);
        TEvent e4 = TEvent.createKeyValue(115, EVT_A, 11);
        TEvent e5 = TEvent.createKeyValue(120, EVT_A, 84);
        TEvent e6 = TEvent.createKeyValue(125, EVT_A, 92);

        List<Signal> signals = new ArrayList<>();
        RuleContext<MilestoneRule> ruleContext = createRule(signals, this::extractValue,
                aLevel(1, 100),
                aLevel(2, 200),
                aLevel(3, 300));
        MilestoneProcessor milestoneProcessor = new MilestoneProcessor(pool, ruleContext);
        submitOrder(milestoneProcessor, e1, e2, e3, e4, e5, e6);

        System.out.println(signals);
        assertStrict(signals,
                new MilestoneSignal(ruleContext.getRule().getId(), L_0, L_3, BigDecimal.valueOf(340.0), e2));
    }

    @DisplayName("Single event passes multiple levels from a middle level")
    @Test
    public void testEventPassesMiddleMultipleLevels() {
        TEvent e1 = TEvent.createKeyValue(100, EVT_A, 187);
        TEvent e2 = TEvent.createKeyValue(105, EVT_A, 12);
        TEvent e3 = TEvent.createKeyValue(110, EVT_A, 341);
        TEvent e4 = TEvent.createKeyValue(115, EVT_A, 11);
        TEvent e5 = TEvent.createKeyValue(120, EVT_A, 84);
        TEvent e6 = TEvent.createKeyValue(125, EVT_A, 92);

        List<Signal> signals = new ArrayList<>();
        RuleContext<MilestoneRule> ruleContext = createRule(signals, this::extractValue,
                aLevel(1, 100),
                aLevel(2, 200),
                aLevel(3, 300));
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
        TEvent e1 = TEvent.createKeyValue(100, EVT_A, 187);
        TEvent e2 = TEvent.createKeyValue(105, EVT_A, 12);
        TEvent e3 = TEvent.createKeyValue(110, EVT_A, -98);
        TEvent e4 = TEvent.createKeyValue(115, EVT_A, 11);

        List<Signal> signals = new ArrayList<>();
        RuleContext<MilestoneRule> ruleContext = createRule(signals, this::extractValue,
                aLevel(1, 100),
                aLevel(2, 200),
                aLevel(3, 300));
        MilestoneRule rule = ruleContext.getRule();
        rule.setFlags(new HashSet<>(Collections.singletonList(TRACK_PENALTIES)));
        MilestoneProcessor milestoneProcessor = new MilestoneProcessor(pool, ruleContext);
        submitOrder(milestoneProcessor, e1, e2, e3, e4);

        System.out.println(signals);
        assertStrict(signals,
                new MilestoneSignal(rule.getId(), L_0, L_1, BigDecimal.valueOf(187.0), e1));

        assertRedisHashMapValue(ID.getGameMilestoneKey(TEvent.GAME_ID, rule.getId()),
                ID.getPenaltiesUserKeyUnderGameMilestone(TEvent.USER_ID),
                "-98");
    }

    private BigDecimal extractValue(Event event, MilestoneRule rule, ExecutionContext context) {
        return BigDecimal.valueOf((long)event.getFieldValue("value"));
    }

    private MilestoneRule.Level aLevel(int level, long milestone) {
        return new MilestoneRule.Level(level, BigDecimal.valueOf(milestone));
    }

    private RuleContext<MilestoneRule> createRule(Collection<Signal> collector, EventBiValueResolver<MilestoneRule, ExecutionContext> extractor, MilestoneRule.Level... levels) {
        MilestoneRule rule = new MilestoneRule("test.milestone.rule");
        rule.setForEvent(EVT_A);
        rule.setValueExtractor(extractor);
        rule.setLevels(Arrays.asList(levels));
        rule.setFlags(new HashSet<>(Collections.singletonList(SKIP_NEGATIVE_VALUES)));
        return new RuleContext<>(rule, fromConsumer(collector::add));
    }
}
