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

package io.github.oasis.engine.elements.points;

import io.github.oasis.engine.elements.AbstractRule;
import io.github.oasis.engine.elements.AbstractRuleTest;
import io.github.oasis.engine.elements.Signal;
import io.github.oasis.engine.model.EventExecutionFilter;
import io.github.oasis.engine.model.EventValueResolver;
import io.github.oasis.engine.model.ExecutionContext;
import io.github.oasis.engine.model.RuleContext;
import io.github.oasis.engine.model.TEvent;
import io.github.oasis.model.Event;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Isuru Weerarathna
 */
@DisplayName("Points Calculation")
public class PointsTest extends AbstractRuleTest {

    private static final String EVT_A = "event.a";
    private static final String EVT_B = "event.b";
    private static final double AMOUNT_10 = 10.0;
    private static final double AMOUNT_50 = 50.0;

    @DisplayName("No matching event types")
    @Test
    public void testNoMatchingEventTypes() {
        TEvent e1 = TEvent.createKeyValue(110, EVT_B, 15);
        TEvent e2 = TEvent.createKeyValue(144, EVT_B, 83);
        TEvent e3 = TEvent.createKeyValue(157, EVT_B, 14);

        List<Signal> signals = new ArrayList<>();
        RuleContext<PointRule> ruleContext = createRule(AMOUNT_10, this::greaterThan50,signals);
        Assertions.assertEquals(AMOUNT_10, ruleContext.getRule().getAmountToAward().doubleValue());
        PointsProcessor processor = new PointsProcessor(pool, ruleContext);
        submitOrder(processor, e1, e2, e3);

        System.out.println(signals);
        Assertions.assertEquals(0, signals.size());
    }

    @DisplayName("Const Award: no criteria satisfied")
    @Test
    public void testNoCriteria() {
        TEvent e1 = TEvent.createKeyValue(110, EVT_A, 15);
        TEvent e2 = TEvent.createKeyValue(144, EVT_A, 23);
        TEvent e3 = TEvent.createKeyValue(157, EVT_A, 14);

        List<Signal> signals = new ArrayList<>();
        RuleContext<PointRule> ruleContext = createRule(AMOUNT_10, this::greaterThan50, signals);
        PointRule rule = ruleContext.getRule();
        Assertions.assertEquals(AMOUNT_10, rule.getAmountToAward().doubleValue());
        PointsProcessor processor = new PointsProcessor(pool, ruleContext);
        submitOrder(processor, e1, e2, e3);

        System.out.println(signals);
        Assertions.assertEquals(0, signals.size());
    }

    @DisplayName("Const Award: a point awarded")
    @Test
    public void testSinglePoint() {
        TEvent e1 = TEvent.createKeyValue(110, EVT_A, 15);
        TEvent e2 = TEvent.createKeyValue(144, EVT_A, 83);
        TEvent e3 = TEvent.createKeyValue(157, EVT_A, 14);

        List<Signal> signals = new ArrayList<>();
        RuleContext<PointRule> ruleContext = createRule(AMOUNT_10, this::greaterThan50, signals);
        PointRule rule = ruleContext.getRule();
        Assertions.assertEquals(AMOUNT_10, rule.getAmountToAward().doubleValue());
        PointsProcessor processor = new PointsProcessor(pool, ruleContext);
        submitOrder(processor, e1, e2, e3);

        System.out.println(signals);
        assertStrict(signals,
                new PointSignal(rule.getId(), BigDecimal.valueOf(AMOUNT_10), e2));
    }

    @DisplayName("Const Award: multiple point awarded")
    @Test
    public void testMultiplePoints() {
        TEvent e1 = TEvent.createKeyValue(110, EVT_A, 65);
        TEvent e2 = TEvent.createKeyValue(144, EVT_A, 14);
        TEvent e3 = TEvent.createKeyValue(157, EVT_A, 83);

        List<Signal> signals = new ArrayList<>();
        RuleContext<PointRule> ruleContext = createRule(AMOUNT_50, this::greaterThan50, signals);
        PointRule rule = ruleContext.getRule();
        Assertions.assertEquals(AMOUNT_50, rule.getAmountToAward().doubleValue());
        PointsProcessor processor = new PointsProcessor(pool, ruleContext);
        submitOrder(processor, e1, e2, e3);

        System.out.println(signals);
        assertStrict(signals,
                new PointSignal(rule.getId(), BigDecimal.valueOf(AMOUNT_50), e1),
                new PointSignal(rule.getId(), BigDecimal.valueOf(AMOUNT_50), e3));
    }

    @DisplayName("Expression Award: a point awarded")
    @Test
    public void testExpressionSinglePoint() {
        TEvent e1 = TEvent.createKeyValue(110, EVT_A, 15);
        TEvent e2 = TEvent.createKeyValue(144, EVT_A, 83);
        TEvent e3 = TEvent.createKeyValue(157, EVT_A, 14);

        List<Signal> signals = new ArrayList<>();
        RuleContext<PointRule> ruleContext = createRule(this::awards, this::greaterThan50, signals);
        PointsProcessor processor = new PointsProcessor(pool, ruleContext);
        submitOrder(processor, e1, e2, e3);

        System.out.println(signals);
        assertStrict(signals,
                new PointSignal(ruleContext.getRule().getId(), BigDecimal.valueOf(16), e2));
    }

    @DisplayName("Expression Award: multiple points awarded")
    @Test
    public void testExpressionMultiplePoint() {
        TEvent e1 = TEvent.createKeyValue(110, EVT_A, 55);
        TEvent e2 = TEvent.createKeyValue(144, EVT_A, 14);
        TEvent e3 = TEvent.createKeyValue(157, EVT_A, 67);

        List<Signal> signals = new ArrayList<>();
        RuleContext<PointRule> ruleContext = createRule(this::awards, this::greaterThan50, signals);
        PointsProcessor processor = new PointsProcessor(pool, ruleContext);
        submitOrder(processor, e1, e2, e3);

        System.out.println(signals);
        assertStrict(signals,
                new PointSignal(ruleContext.getRule().getId(), BigDecimal.valueOf(11), e1),
                new PointSignal(ruleContext.getRule().getId(), BigDecimal.valueOf(13), e3));
    }

    private BigDecimal awards(Event event, ExecutionContext context) {
        return BigDecimal.valueOf((long)event.getFieldValue("value") / 5);
    }

    private boolean greaterThan50(Event event, AbstractRule rule, ExecutionContext context) {
        return (long) event.getFieldValue("value") >= 50;
    }

    private RuleContext<PointRule> createRule(double amount, EventExecutionFilter criteria, Collection<Signal> collection) {
        PointRule rule = new PointRule("test.point.rule");
        rule.setForEvent(EVT_A);
        rule.setAmountToAward(BigDecimal.valueOf(amount));
        rule.setCriteria(criteria);

        return new RuleContext<>(rule, fromConsumer(collection::add));
    }

    private RuleContext<PointRule> createRule(EventValueResolver<ExecutionContext> amount, EventExecutionFilter criteria, Collection<Signal> collection) {
        PointRule rule = new PointRule("test.point.rule");
        rule.setForEvent(EVT_A);
        rule.setAmountExpression(amount);
        rule.setCriteria(criteria);

        return new RuleContext<>(rule, fromConsumer(collection::add));
    }
}
