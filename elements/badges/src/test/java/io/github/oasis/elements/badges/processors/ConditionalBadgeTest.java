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

import io.github.oasis.core.Event;
import io.github.oasis.core.context.ExecutionContext;
import io.github.oasis.core.elements.AbstractRule;
import io.github.oasis.core.elements.EventExecutionFilter;
import io.github.oasis.core.elements.RuleContext;
import io.github.oasis.core.elements.Signal;
import io.github.oasis.core.elements.matchers.SingleEventTypeMatcher;
import io.github.oasis.elements.badges.TEvent;
import io.github.oasis.elements.badges.rules.ConditionalBadgeRule;
import io.github.oasis.elements.badges.signals.ConditionalBadgeSignal;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author Isuru Weerarathna
 */
@DisplayName("Conditional Badges")
public class ConditionalBadgeTest extends AbstractRuleTest {

    private static final String EVT_1 = "event.a";
    private static final String EVT_2 = "event.b";

    private static final int ATTR_50 = 50;
    private static final int ATTR_65 = 65;
    private static final int ATTR_75 = 75;
    private static final int ATTR_85 = 85;
    public static final String RULE_ID = "test.cond.badge";

    @DisplayName("No conditions")
    @Test
    public void testNoConditions() {
        TEvent e1 = TEvent.createKeyValue(110, EVT_1, 14);
        TEvent e2 = TEvent.createKeyValue(144, EVT_1, 32);
        TEvent e3 = TEvent.createKeyValue(125, EVT_1, 11);

        List<Signal> signals = new ArrayList<>();
        RuleContext<ConditionalBadgeRule> ruleContext = createRule(signals);
        Assertions.assertTrue(ruleContext.getRule().getConditions().isEmpty());
        ConditionalBadgeProcessor processor = new ConditionalBadgeProcessor(pool, ruleContext);
        submitOrder(processor, e1, e2, e3);

        System.out.println(signals);
        Assertions.assertEquals(0, signals.size());
    }

    @DisplayName("Single condition: No event condition met")
    @Test
    public void testSingleConditionBadgeNoEventCondMet() {
        TEvent e1 = TEvent.createKeyValue(110, EVT_1, 14);
        TEvent e2 = TEvent.createKeyValue(144, EVT_1, 62);
        TEvent e3 = TEvent.createKeyValue(125, EVT_1, 11);

        List<Signal> signals = new ArrayList<>();
        RuleContext<ConditionalBadgeRule> ruleContext = createRule(signals, aCond(1, ATTR_50, this::greater50));
        ruleContext.getRule().setEventFilter(((event, rule1, ctx) -> (long)event.getFieldValue("value") >= 75));
        Assertions.assertEquals(1, ruleContext.getRule().getConditions().size());
        ConditionalBadgeProcessor processor = new ConditionalBadgeProcessor(pool, ruleContext);
        submitOrder(processor, e1, e2, e3);

        System.out.println(signals);
        Assertions.assertEquals(0, signals.size());
    }

    @DisplayName("Single condition: no satisfying events")
    @Test
    public void testSingleConditionNoEvents() {
        TEvent e1 = TEvent.createKeyValue(110, EVT_2, 14);
        TEvent e2 = TEvent.createKeyValue(144, EVT_2, 52);
        TEvent e3 = TEvent.createKeyValue(125, EVT_2, 11);

        List<Signal> signals = new ArrayList<>();
        RuleContext<ConditionalBadgeRule> ruleContext = createRule(signals, aCond(1, ATTR_50, this::greater50));
        ruleContext.getRule().setEventTypeMatcher(new SingleEventTypeMatcher(EVT_1));
        Assertions.assertEquals(1, ruleContext.getRule().getConditions().size());
        ConditionalBadgeProcessor processor = new ConditionalBadgeProcessor(pool, ruleContext);
        submitOrder(processor, e1, e2, e3);

        System.out.println(signals);
        Assertions.assertEquals(0, signals.size());
    }

    @DisplayName("Single condition: no condition satisfied")
    @Test
    public void testSingleConditionNoCondition() {
        TEvent e1 = TEvent.createKeyValue(110, EVT_1, 14);
        TEvent e2 = TEvent.createKeyValue(144, EVT_1, 32);
        TEvent e3 = TEvent.createKeyValue(125, EVT_1, 11);

        List<Signal> signals = new ArrayList<>();
        RuleContext<ConditionalBadgeRule> ruleContext = createRule(signals, aCond(1, ATTR_50, this::greater50));
        Assertions.assertEquals(1, ruleContext.getRule().getConditions().size());
        ConditionalBadgeProcessor processor = new ConditionalBadgeProcessor(pool, ruleContext);
        submitOrder(processor, e1, e2, e3);

        System.out.println(signals);
        Assertions.assertEquals(0, signals.size());
    }

    @DisplayName("Single condition: badge creation")
    @Test
    public void testSingleConditionBadge() {
        TEvent e1 = TEvent.createKeyValue(110, EVT_1, 14);
        TEvent e2 = TEvent.createKeyValue(144, EVT_1, 62);
        TEvent e3 = TEvent.createKeyValue(125, EVT_1, 11);

        List<Signal> signals = new ArrayList<>();
        RuleContext<ConditionalBadgeRule> ruleContext = createRule(signals, aCond(1, ATTR_50, this::greater50));
        Assertions.assertEquals(1, ruleContext.getRule().getConditions().size());
        ConditionalBadgeProcessor processor = new ConditionalBadgeProcessor(pool, ruleContext);
        submitOrder(processor, e1, e2, e3);

        System.out.println(signals);
        assertStrict(signals,
                new ConditionalBadgeSignal(ruleContext.getRule().getId(), e2, ATTR_50, 144, e2.getExternalId()));
    }

    @DisplayName("Single condition: multiple same badge creation")
    @Test
    public void testSingleConditionMultiSameBadge() {
        TEvent e1 = TEvent.createKeyValue(110, EVT_1, 14);
        TEvent e2 = TEvent.createKeyValue(144, EVT_1, 62);
        TEvent e3 = TEvent.createKeyValue(125, EVT_1, 64);

        List<Signal> signals = new ArrayList<>();
        RuleContext<ConditionalBadgeRule> ruleContext = createRule(signals, aCond(1, ATTR_50, this::greater50));
        ConditionalBadgeRule rule = ruleContext.getRule();
        Assertions.assertEquals(1, rule.getConditions().size());
        ConditionalBadgeProcessor processor = new ConditionalBadgeProcessor(pool, ruleContext);
        submitOrder(processor, e1, e2, e3);

        System.out.println(signals);
        assertStrict(signals,
                new ConditionalBadgeSignal(rule.getId(), e2, ATTR_50, 144, e2.getExternalId()),
                new ConditionalBadgeSignal(rule.getId(), e3, ATTR_50, 125, e3.getExternalId()));
    }

    @DisplayName("Single condition: only once award")
    @Test
    public void testSingleConditionOnceBadge() {
        TEvent e1 = TEvent.createKeyValue(110, EVT_1, 14);
        TEvent e2 = TEvent.createKeyValue(144, EVT_1, 62);
        TEvent e3 = TEvent.createKeyValue(147, EVT_1, 57);

        List<Signal> signals = new ArrayList<>();
        RuleContext<ConditionalBadgeRule> ruleContext = createRule(signals, 1, aCond(1, ATTR_50, this::greater50));
        ConditionalBadgeRule rule = ruleContext.getRule();
        Assertions.assertEquals(1, rule.getConditions().size());
        ConditionalBadgeProcessor processor = new ConditionalBadgeProcessor(pool, ruleContext);
        submitOrder(processor, e1, e2, e3);

        System.out.println(signals);
        assertStrict(signals,
                new ConditionalBadgeSignal(rule.getId(), e2, ATTR_50, 144, e2.getExternalId()));
    }

    @DisplayName("Single condition: Out-of-order only once award not affected")
    @Test
    public void testSingleConditionOOOOnceBadge() {
        TEvent e1 = TEvent.createKeyValue(110, EVT_1, 14);
        TEvent e2 = TEvent.createKeyValue(144, EVT_1, 62);
        TEvent e3 = TEvent.createKeyValue(125, EVT_1, 57);

        List<Signal> signals = new ArrayList<>();
        RuleContext<ConditionalBadgeRule> ruleContext = createRule(signals, 1, aCond(1, ATTR_50, this::greater50));
        ConditionalBadgeRule rule = ruleContext.getRule();
        Assertions.assertEquals(1, rule.getConditions().size());
        ConditionalBadgeProcessor processor = new ConditionalBadgeProcessor(pool, ruleContext);
        submitOrder(processor, e1, e2, e3);

        System.out.println(signals);
        assertStrict(signals,
                new ConditionalBadgeSignal(rule.getId(), e2, ATTR_50, 144, e2.getExternalId()));
    }

    // ------------------------------------------------------
    // MULTIPLE CONDITIONS
    // ------------------------------------------------------

    @DisplayName("Multi conditions: no satisfying events")
    @Test
    public void testMultiConditionNoEvents() {
        TEvent e1 = TEvent.createKeyValue(110, EVT_2, 14);
        TEvent e2 = TEvent.createKeyValue(144, EVT_2, 52);
        TEvent e3 = TEvent.createKeyValue(125, EVT_2, 11);

        List<Signal> signals = new ArrayList<>();
        RuleContext<ConditionalBadgeRule> ruleContext = createRule(signals,
                aCond(2, ATTR_50, this::greater50),
                aCond(1, ATTR_75, this::greater75));
        ConditionalBadgeRule rule = ruleContext.getRule();
        rule.setEventTypeMatcher(new SingleEventTypeMatcher(EVT_1));
        Assertions.assertEquals(2, rule.getConditions().size());
        ConditionalBadgeProcessor processor = new ConditionalBadgeProcessor(pool, ruleContext);
        submitOrder(processor, e1, e2, e3);

        System.out.println(signals);
        Assertions.assertEquals(0, signals.size());
    }

    @DisplayName("Multi condition: no condition satisfied")
    @Test
    public void testMultiConditionNoCondition() {
        TEvent e1 = TEvent.createKeyValue(110, EVT_1, 14);
        TEvent e2 = TEvent.createKeyValue(144, EVT_1, 32);
        TEvent e3 = TEvent.createKeyValue(125, EVT_1, 11);

        List<Signal> signals = new ArrayList<>();
        RuleContext<ConditionalBadgeRule> ruleContext = createRule(signals,
                aCond(2, ATTR_50, this::greater50),
                aCond(1, ATTR_65, this::greater65));
        Assertions.assertEquals(2, ruleContext.getRule().getConditions().size());
        ConditionalBadgeProcessor processor = new ConditionalBadgeProcessor(pool, ruleContext);
        submitOrder(processor, e1, e2, e3);

        System.out.println(signals);
        Assertions.assertEquals(0, signals.size());
    }

    @DisplayName("Multi condition: badge creation")
    @Test
    public void testMultiConditionBadge() {
        TEvent e1 = TEvent.createKeyValue(110, EVT_1, 14);
        TEvent e2 = TEvent.createKeyValue(144, EVT_1, 62);
        TEvent e3 = TEvent.createKeyValue(125, EVT_1, 11);

        List<Signal> signals = new ArrayList<>();
        RuleContext<ConditionalBadgeRule> ruleContext = createRule(signals,
                aCond(2, ATTR_75, this::greater75),
                aCond(1, ATTR_50, this::greater50));
        Assertions.assertEquals(2, ruleContext.getRule().getConditions().size());
        ConditionalBadgeProcessor processor = new ConditionalBadgeProcessor(pool, ruleContext);
        submitOrder(processor, e1, e2, e3);

        System.out.println(signals);
        assertStrict(signals,
                new ConditionalBadgeSignal(ruleContext.getRule().getId(), e2, ATTR_50, 144, e2.getExternalId()));
    }

    @DisplayName("Multi condition: multi badge creation")
    @Test
    public void testMultiConditionMultiBadge() {
        TEvent e1 = TEvent.createKeyValue(110, EVT_1, 14);
        TEvent e2 = TEvent.createKeyValue(144, EVT_1, 62);
        TEvent e3 = TEvent.createKeyValue(145, EVT_1, 87);

        List<Signal> signals = new ArrayList<>();
        RuleContext<ConditionalBadgeRule> ruleContext = createRule(signals,
                aCond(1, ATTR_75, this::greater75),
                aCond(2, ATTR_50, this::greater50));
        ConditionalBadgeRule rule = ruleContext.getRule();
        Assertions.assertEquals(2, rule.getConditions().size());
        ConditionalBadgeProcessor processor = new ConditionalBadgeProcessor(pool, ruleContext);
        submitOrder(processor, e1, e2, e3);

        System.out.println(signals);
        assertStrict(signals,
                new ConditionalBadgeSignal(rule.getId(), e2, ATTR_50, 144, e2.getExternalId()),
                new ConditionalBadgeSignal(rule.getId(), e3, ATTR_75, 145, e3.getExternalId()));
    }

    @DisplayName("Multi condition: when order is incorrect no multiple different badges")
    @Test
    public void testMultiConditionUnOrderedMultiBadge() {
        TEvent e1 = TEvent.createKeyValue(110, EVT_1, 14);
        TEvent e2 = TEvent.createKeyValue(144, EVT_1, 62);
        TEvent e3 = TEvent.createKeyValue(145, EVT_1, 87);

        List<Signal> signals = new ArrayList<>();
        RuleContext<ConditionalBadgeRule> ruleContext = createRule(signals,
                aCond(2, ATTR_75, this::greater75),
                aCond(1, ATTR_50, this::greater50));
        ConditionalBadgeRule rule = ruleContext.getRule();
        Assertions.assertEquals(2, rule.getConditions().size());
        ConditionalBadgeProcessor processor = new ConditionalBadgeProcessor(pool, ruleContext);
        submitOrder(processor, e1, e2, e3);

        System.out.println(signals);
        assertStrict(signals,
                new ConditionalBadgeSignal(rule.getId(), e2, ATTR_50, 144, e2.getExternalId()),
                new ConditionalBadgeSignal(rule.getId(), e3, ATTR_50, 145, e3.getExternalId()));
    }

    @DisplayName("Multi condition: multiple many badges")
    @Test
    public void testMultiConditionMultiBadgeMany() {
        TEvent e1 = TEvent.createKeyValue(110, EVT_1, 14);
        TEvent e2 = TEvent.createKeyValue(144, EVT_1, 62);
        TEvent e3 = TEvent.createKeyValue(145, EVT_1, 87);
        TEvent e4 = TEvent.createKeyValue(150, EVT_1, 76);
        TEvent e5 = TEvent.createKeyValue(155, EVT_1, 80);
        TEvent e6 = TEvent.createKeyValue(185, EVT_1, 50);
        TEvent e7 = TEvent.createKeyValue(160, EVT_1, 1);
        TEvent e8 = TEvent.createKeyValue(165, EVT_1, 85);

        List<Signal> signals = new ArrayList<>();
        RuleContext<ConditionalBadgeRule> ruleContext = createRule(signals,
                aCond(1, ATTR_85, this::greater85),
                aCond(2, ATTR_75, this::greater75),
                aCond(3, ATTR_50, this::greater50));
        ConditionalBadgeRule rule = ruleContext.getRule();
        Assertions.assertEquals(3, rule.getConditions().size());
        ConditionalBadgeProcessor processor = new ConditionalBadgeProcessor(pool, ruleContext);
        submitOrder(processor, e1, e2, e3, e4, e5, e6, e7, e8);

        System.out.println(signals);
        assertStrict(signals,
                new ConditionalBadgeSignal(rule.getId(), e2, ATTR_50, 144, e2.getExternalId()),
                new ConditionalBadgeSignal(rule.getId(), e6, ATTR_50, 185, e6.getExternalId()),
                new ConditionalBadgeSignal(rule.getId(), e4, ATTR_75, 150, e4.getExternalId()),
                new ConditionalBadgeSignal(rule.getId(), e5, ATTR_75, 155, e5.getExternalId()),
                new ConditionalBadgeSignal(rule.getId(), e3, ATTR_85, 145, e3.getExternalId()),
                new ConditionalBadgeSignal(rule.getId(), e8, ATTR_85, 165, e8.getExternalId()));
    }

    @DisplayName("Multi condition: limited badges")
    @Test
    public void testMultiConditionMultiBadgeLimited() {
        TEvent e1 = TEvent.createKeyValue(110, EVT_1, 14);
        TEvent e2 = TEvent.createKeyValue(144, EVT_1, 62);
        TEvent e3 = TEvent.createKeyValue(145, EVT_1, 87);
        TEvent e4 = TEvent.createKeyValue(150, EVT_1, 76);
        TEvent e5 = TEvent.createKeyValue(155, EVT_1, 80);
        TEvent e6 = TEvent.createKeyValue(185, EVT_1, 50);
        TEvent e7 = TEvent.createKeyValue(160, EVT_1, 1);
        TEvent e8 = TEvent.createKeyValue(165, EVT_1, 85);

        List<Signal> signals = new ArrayList<>();
        RuleContext<ConditionalBadgeRule> ruleContext = createRule(signals, 1,
                aCond(1, ATTR_85, this::greater85),
                aCond(2, ATTR_75, this::greater75),
                aCond(3, ATTR_50, this::greater50));
        ConditionalBadgeRule rule = ruleContext.getRule();
        Assertions.assertEquals(3, rule.getConditions().size());
        ConditionalBadgeProcessor processor = new ConditionalBadgeProcessor(pool, ruleContext);
        submitOrder(processor, e1, e2, e3, e4, e5, e6, e7, e8);

        System.out.println(signals);
        assertStrict(signals,
                new ConditionalBadgeSignal(rule.getId(), e2, ATTR_50, 144, e2.getExternalId()),
                new ConditionalBadgeSignal(rule.getId(), e4, ATTR_75, 150, e4.getExternalId()),
                new ConditionalBadgeSignal(rule.getId(), e3, ATTR_85, 145, e3.getExternalId()));
    }

    private boolean greater50(Event event, AbstractRule rule, ExecutionContext context) {
        return (long) event.getFieldValue("value") >= 50;
    }

    private boolean greater65(Event event, AbstractRule rule, ExecutionContext context) {
        return (long) event.getFieldValue("value") >= 65;
    }

    private boolean greater75(Event event, AbstractRule rule, ExecutionContext context) {
        return (long) event.getFieldValue("value") >= 75;
    }

    private boolean greater85(Event event, AbstractRule rule, ExecutionContext context) {
        return (long) event.getFieldValue("value") >= 85;
    }

    private ConditionalBadgeRule.Condition aCond(int priority, int attr, EventExecutionFilter cond) {
        return new ConditionalBadgeRule.Condition(priority, cond, attr);
    }

    private RuleContext<ConditionalBadgeRule> createRule(Collection<Signal> collector, ConditionalBadgeRule.Condition... conditions) {
        return createRule(collector, Integer.MAX_VALUE, conditions);
    }

    private RuleContext<ConditionalBadgeRule> createRule(Collection<Signal> collector, int maxTimes, ConditionalBadgeRule.Condition... conditions) {
        ConditionalBadgeRule rule = new ConditionalBadgeRule(RULE_ID);
        rule.setEventTypeMatcher(new SingleEventTypeMatcher(EVT_1));
        rule.setMaxAwardTimes(maxTimes);
        rule.setConditions(Arrays.asList(conditions));
        return new RuleContext<>(rule, fromConsumer(collector::add));
    }

}
