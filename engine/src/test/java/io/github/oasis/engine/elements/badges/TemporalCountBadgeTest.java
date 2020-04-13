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

import io.github.oasis.engine.elements.AbstractRuleTest;
import io.github.oasis.engine.elements.Signal;
import io.github.oasis.engine.elements.badges.rules.BadgeTemporalCountRule;
import io.github.oasis.engine.elements.badges.rules.BadgeTemporalRule;
import io.github.oasis.engine.elements.badges.signals.TemporalBadgeSignal;
import io.github.oasis.engine.model.RuleContext;
import io.github.oasis.engine.model.TEvent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author Isuru Weerarathna
 */
@DisplayName("Temporal Count Badges")
public class TemporalCountBadgeTest extends AbstractRuleTest {

    private static final int ATTR_1 = 1;
    private static final int ATTR_2 = 2;
    private static final int ATTR_4 = 4;

    private static final long T_3 = 3;
    private static final long T_5 = 5;
    private static final long T_10 = 10;

    private static final long FIFTY = 50;
    public static final String EVENT_TYPE = "event.a";
    public static final String EVENT_TYPE_B = "event.b";

    @DisplayName("Rule should be able to create")
    @Test
    public void shouldNotBeAbleToSetValueResolver() {
        BadgeTemporalCountRule rule = new BadgeTemporalCountRule("test.histogram.count");
        rule.setForEvent(EVENT_TYPE);
        rule.setTimeUnit(FIFTY);
        rule.setThresholds(Arrays.asList(aT(ATTR_1, T_3), aT(ATTR_2, T_5)));
        rule.setCriteria((e,r,c) -> (long) e.getFieldValue("value") >= 50);

        Assertions.assertNotNull(rule.getCriteria());
        Assertions.assertThrows(IllegalStateException.class, () -> rule.setValueResolver((e,c) -> BigDecimal.ONE));
    }

    @DisplayName("No Thresholds")
    @Test
    public void testNoThresholds() {
        TEvent e1 = TEvent.createKeyValue(110, EVENT_TYPE, 112);
        TEvent e2 = TEvent.createKeyValue(115, EVENT_TYPE, 14);
        TEvent e3 = TEvent.createKeyValue(120, EVENT_TYPE, 4);
        TEvent e4 = TEvent.createKeyValue(155, EVENT_TYPE, 5);
        TEvent e5 = TEvent.createKeyValue(160, EVENT_TYPE, 84);
        TEvent e6 = TEvent.createKeyValue(165, EVENT_TYPE, 39);

        List<Signal> signals = new ArrayList<>();
        RuleContext<BadgeTemporalRule> ruleContext = createRule(FIFTY, signals);
        BadgeTemporalRule rule = ruleContext.getRule();
        Assertions.assertEquals(0, rule.getThresholds().size());
        BadgeTemporalProcessor processor = new BadgeTemporalProcessor(pool, ruleContext);
        submitOrder(processor, e1, e2, e3, e4, e5, e6);

        Assertions.assertEquals(0, signals.size());
    }

    @DisplayName("Single Threshold: no threshold passed")
    @Test
    public void testSingleT() {
        TEvent e1 = TEvent.createKeyValue(110, EVENT_TYPE, 52);
        TEvent e2 = TEvent.createKeyValue(115, EVENT_TYPE, 54);
        TEvent e3 = TEvent.createKeyValue(120, EVENT_TYPE, 4);
        TEvent e4 = TEvent.createKeyValue(155, EVENT_TYPE, 5);
        TEvent e5 = TEvent.createKeyValue(160, EVENT_TYPE, 84);
        TEvent e6 = TEvent.createKeyValue(165, EVENT_TYPE, 39);

        List<Signal> signals = new ArrayList<>();
        RuleContext<BadgeTemporalRule> ruleContext = createRule(FIFTY, signals, aT(ATTR_1, T_3));
        BadgeTemporalRule rule = ruleContext.getRule();
        Assertions.assertEquals(1, rule.getThresholds().size());
        BadgeTemporalProcessor processor = new BadgeTemporalProcessor(pool, ruleContext);
        submitOrder(processor, e1, e2, e3, e4, e5, e6);

        Assertions.assertEquals(0, signals.size());
    }

    @DisplayName("Single Threshold: no matching events")
    @Test
    public void testSingleTNoMatchEvents() {
        TEvent e1 = TEvent.createKeyValue(110, EVENT_TYPE_B, 75);
        TEvent e2 = TEvent.createKeyValue(115, EVENT_TYPE_B, 63);
        TEvent e3 = TEvent.createKeyValue(120, EVENT_TYPE_B, 99);
        TEvent e4 = TEvent.createKeyValue(155, EVENT_TYPE_B, 26);
        TEvent e5 = TEvent.createKeyValue(160, EVENT_TYPE_B, 96);
        TEvent e6 = TEvent.createKeyValue(165, EVENT_TYPE_B, 71);

        List<Signal> signals = new ArrayList<>();
        RuleContext<BadgeTemporalRule> ruleContext = createRule(FIFTY, signals, aT(ATTR_1, T_3));
        BadgeTemporalRule rule = ruleContext.getRule();
        Assertions.assertEquals(1, rule.getThresholds().size());
        BadgeTemporalProcessor processor = new BadgeTemporalProcessor(pool, ruleContext);
        submitOrder(processor, e1, e2, e3, e4, e5, e6);

        Assertions.assertEquals(0, signals.size());
    }

    @DisplayName("Single Threshold: badge creation")
    @Test
    public void testSingleTBadge() {
        TEvent e1 = TEvent.createKeyValue(110, EVENT_TYPE, 99);
        TEvent e2 = TEvent.createKeyValue(115, EVENT_TYPE, 54);
        TEvent e3 = TEvent.createKeyValue(120, EVENT_TYPE, 63);
        TEvent e4 = TEvent.createKeyValue(155, EVENT_TYPE, 5);
        TEvent e5 = TEvent.createKeyValue(160, EVENT_TYPE, 34);
        TEvent e6 = TEvent.createKeyValue(165, EVENT_TYPE, 39);

        List<Signal> signals = new ArrayList<>();
        RuleContext<BadgeTemporalRule> ruleContext = createRule(FIFTY, signals, aT(ATTR_1, T_3));
        BadgeTemporalRule rule = ruleContext.getRule();
        Assertions.assertEquals(1, rule.getThresholds().size());
        BadgeTemporalProcessor processor = new BadgeTemporalProcessor(pool, ruleContext);
        submitOrder(processor, e1, e2, e3, e4, e5, e6);

        System.out.println(signals);
        assertStrict(signals,
                new TemporalBadgeSignal(rule.getId(), e3, ATTR_1, 100, 150, e3.getTimestamp(), e3.getExternalId()));
    }

    @DisplayName("Single Threshold: badge creation sparse condition")
    @Test
    public void testSingleTBadgeWithC() {
        TEvent e1 = TEvent.createKeyValue(110, EVENT_TYPE, 99);
        TEvent e2 = TEvent.createKeyValue(115, EVENT_TYPE, 23);
        TEvent e3 = TEvent.createKeyValue(120, EVENT_TYPE, 53);
        TEvent e4 = TEvent.createKeyValue(125, EVENT_TYPE, 66);
        TEvent e5 = TEvent.createKeyValue(160, EVENT_TYPE, 34);
        TEvent e6 = TEvent.createKeyValue(165, EVENT_TYPE, 39);

        List<Signal> signals = new ArrayList<>();
        RuleContext<BadgeTemporalRule> ruleContext = createRule(FIFTY, signals, aT(ATTR_1, T_3));
        BadgeTemporalRule rule = ruleContext.getRule();
        Assertions.assertEquals(1, rule.getThresholds().size());
        BadgeTemporalProcessor processor = new BadgeTemporalProcessor(pool, ruleContext);
        submitOrder(processor, e1, e2, e3, e4, e5, e6);

        System.out.println(signals);
        assertStrict(signals,
                new TemporalBadgeSignal(rule.getId(), e4, ATTR_1, 100, 150, e4.getTimestamp(), e4.getExternalId()));
    }

    @DisplayName("Single Threshold: badges in different time units")
    @Test
    public void testSingleTBadgeInDiffT() {
        TEvent e1 = TEvent.createKeyValue(110, EVENT_TYPE, 99);
        TEvent e2 = TEvent.createKeyValue(115, EVENT_TYPE, 54);
        TEvent e3 = TEvent.createKeyValue(120, EVENT_TYPE, 83);
        TEvent e4 = TEvent.createKeyValue(155, EVENT_TYPE, 55);
        TEvent e5 = TEvent.createKeyValue(160, EVENT_TYPE, 64);
        TEvent e6 = TEvent.createKeyValue(165, EVENT_TYPE, 79);

        List<Signal> signals = new ArrayList<>();
        RuleContext<BadgeTemporalRule> ruleContext = createRule(FIFTY, signals, aT(ATTR_1, T_3));
        BadgeTemporalRule rule = ruleContext.getRule();
        Assertions.assertEquals(1, rule.getThresholds().size());
        BadgeTemporalProcessor processor = new BadgeTemporalProcessor(pool, ruleContext);
        submitOrder(processor, e1, e2, e3, e4, e5, e6);

        System.out.println(signals);
        assertStrict(signals,
                new TemporalBadgeSignal(rule.getId(), e6, ATTR_1, 150, 200, e6.getTimestamp(), e6.getExternalId()),
                new TemporalBadgeSignal(rule.getId(), e3, ATTR_1, 100, 150, e3.getTimestamp(), e3.getExternalId()));
    }

    @DisplayName("Single Threshold: Out-of-order badge creation")
    @Test
    public void testSingleTOOOBadge() {
        TEvent e1 = TEvent.createKeyValue(110, EVENT_TYPE, 54);
        TEvent e2 = TEvent.createKeyValue(115, EVENT_TYPE, 84);
        TEvent e3 = TEvent.createKeyValue(155, EVENT_TYPE, 55);
        TEvent e4 = TEvent.createKeyValue(160, EVENT_TYPE, 34);
        TEvent e5 = TEvent.createKeyValue(165, EVENT_TYPE, 39);
        TEvent e6 = TEvent.createKeyValue(120, EVENT_TYPE, 63);

        List<Signal> signals = new ArrayList<>();
        RuleContext<BadgeTemporalRule> ruleContext = createRule(FIFTY, signals, aT(ATTR_1, T_3));
        BadgeTemporalRule rule = ruleContext.getRule();
        Assertions.assertEquals(1, rule.getThresholds().size());
        BadgeTemporalProcessor processor = new BadgeTemporalProcessor(pool, ruleContext);
        submitOrder(processor, e1, e2, e3, e4, e5, e6);

        System.out.println(signals);
        assertStrict(signals,
                new TemporalBadgeSignal(rule.getId(), e6, ATTR_1, 100, 150, e6.getTimestamp(), e6.getExternalId()));
    }

    // ------------------------------------------------------
    // MULTIPLE THRESHOLDS
    // ------------------------------------------------------

    @DisplayName("Multi Threshold: no threshold passed")
    @Test
    public void testMultiT() {
        TEvent e1 = TEvent.createKeyValue(110, EVENT_TYPE, 52);
        TEvent e2 = TEvent.createKeyValue(115, EVENT_TYPE, 74);
        TEvent e3 = TEvent.createKeyValue(120, EVENT_TYPE, 4);
        TEvent e4 = TEvent.createKeyValue(155, EVENT_TYPE, 5);
        TEvent e5 = TEvent.createKeyValue(160, EVENT_TYPE, 64);
        TEvent e6 = TEvent.createKeyValue(165, EVENT_TYPE, 69);

        List<Signal> signals = new ArrayList<>();
        RuleContext<BadgeTemporalRule> ruleContext = createRule(FIFTY, signals, aT(ATTR_1, T_3), aT(ATTR_2, T_5), aT(ATTR_4, T_10));
        BadgeTemporalRule rule = ruleContext.getRule();
        Assertions.assertEquals(3, rule.getThresholds().size());
        BadgeTemporalProcessor processor = new BadgeTemporalProcessor(pool, ruleContext);
        submitOrder(processor, e1, e2, e3, e4, e5, e6);

        Assertions.assertEquals(0, signals.size());
    }

    @DisplayName("Multi Threshold: no matching passed")
    @Test
    public void testMultiTNoMatches() {
        TEvent e1 = TEvent.createKeyValue(110, EVENT_TYPE_B, 75);
        TEvent e2 = TEvent.createKeyValue(115, EVENT_TYPE_B, 63);
        TEvent e3 = TEvent.createKeyValue(120, EVENT_TYPE_B, 99);
        TEvent e4 = TEvent.createKeyValue(155, EVENT_TYPE_B, 86);
        TEvent e5 = TEvent.createKeyValue(160, EVENT_TYPE_B, 96);
        TEvent e6 = TEvent.createKeyValue(165, EVENT_TYPE_B, 71);

        List<Signal> signals = new ArrayList<>();
        RuleContext<BadgeTemporalRule> ruleContext = createRule(FIFTY, signals, aT(ATTR_1, T_3), aT(ATTR_2, T_5), aT(ATTR_4, T_10));
        BadgeTemporalRule rule = ruleContext.getRule();
        Assertions.assertEquals(3, rule.getThresholds().size());
        BadgeTemporalProcessor processor = new BadgeTemporalProcessor(pool, ruleContext);
        submitOrder(processor, e1, e2, e3, e4, e5, e6);

        Assertions.assertEquals(0, signals.size());
    }

    @DisplayName("Multi Threshold: first badge creation")
    @Test
    public void testMultiTFirstBadge() {
        TEvent e1 = TEvent.createKeyValue(110, EVENT_TYPE, 79);
        TEvent e2 = TEvent.createKeyValue(115, EVENT_TYPE, 54);
        TEvent e3 = TEvent.createKeyValue(120, EVENT_TYPE, 13);
        TEvent e4 = TEvent.createKeyValue(125, EVENT_TYPE, 5);
        TEvent e5 = TEvent.createKeyValue(130, EVENT_TYPE, 64);
        TEvent e6 = TEvent.createKeyValue(135, EVENT_TYPE, 39);

        List<Signal> signals = new ArrayList<>();
        RuleContext<BadgeTemporalRule> ruleContext = createRule(FIFTY, signals, aT(ATTR_1, T_3), aT(ATTR_2, T_5), aT(ATTR_4, T_10));
        BadgeTemporalRule rule = ruleContext.getRule();
        Assertions.assertEquals(3, rule.getThresholds().size());
        BadgeTemporalProcessor processor = new BadgeTemporalProcessor(pool, ruleContext);
        submitOrder(processor, e1, e2, e3, e4, e5, e6);

        System.out.println(signals);
        assertStrict(signals,
                new TemporalBadgeSignal(rule.getId(), e5, ATTR_1, 100, 150, e5.getTimestamp(), e5.getExternalId()));
    }

    @DisplayName("Multi Threshold: many badges creation")
    @Test
    public void testMultiTMultiBadge() {
        TEvent e1 = TEvent.createKeyValue(110, EVENT_TYPE, 89);
        TEvent e2 = TEvent.createKeyValue(115, EVENT_TYPE, 54);
        TEvent e3 = TEvent.createKeyValue(120, EVENT_TYPE, 73);
        TEvent e4 = TEvent.createKeyValue(125, EVENT_TYPE, 5);
        TEvent e5 = TEvent.createKeyValue(130, EVENT_TYPE, 64);
        TEvent e6 = TEvent.createKeyValue(135, EVENT_TYPE, 69);
        TEvent e7 = TEvent.createKeyValue(140, EVENT_TYPE, 39);

        List<Signal> signals = new ArrayList<>();
        RuleContext<BadgeTemporalRule> ruleContext = createRule(FIFTY, signals, aT(ATTR_1, T_3), aT(ATTR_2, T_5), aT(ATTR_4, T_10));
        BadgeTemporalRule rule = ruleContext.getRule();
        Assertions.assertEquals(3, rule.getThresholds().size());
        BadgeTemporalProcessor processor = new BadgeTemporalProcessor(pool, ruleContext);
        submitOrder(processor, e1, e2, e3, e4, e5, e6, e7);

        System.out.println(signals);
        assertStrict(signals,
                new TemporalBadgeSignal(rule.getId(), e3, ATTR_1, 100, 150, e3.getTimestamp(), e3.getExternalId()),
                new TemporalBadgeSignal(rule.getId(), e6, ATTR_2, 100, 150, e6.getTimestamp(), e6.getExternalId()));
    }

    @DisplayName("Multi Threshold: many badges creation to exact threshold")
    @Test
    public void testMultiTMultiBadgeExactT() {
        TEvent e1 = TEvent.createKeyValue(110, EVENT_TYPE, 50);
        TEvent e2 = TEvent.createKeyValue(115, EVENT_TYPE, 50);
        TEvent e3 = TEvent.createKeyValue(120, EVENT_TYPE, 51);
        TEvent e4 = TEvent.createKeyValue(125, EVENT_TYPE, 5);
        TEvent e5 = TEvent.createKeyValue(130, EVENT_TYPE, 68);
        TEvent e6 = TEvent.createKeyValue(135, EVENT_TYPE, 95);

        List<Signal> signals = new ArrayList<>();
        RuleContext<BadgeTemporalRule> ruleContext = createRule(FIFTY, signals, aT(ATTR_1, T_3), aT(ATTR_2, T_5), aT(ATTR_4, T_10));
        BadgeTemporalRule rule = ruleContext.getRule();
        Assertions.assertEquals(3, rule.getThresholds().size());
        BadgeTemporalProcessor processor = new BadgeTemporalProcessor(pool, ruleContext);
        submitOrder(processor, e1, e2, e3, e4, e5, e6);

        System.out.println(signals);
        assertStrict(signals,
                new TemporalBadgeSignal(rule.getId(), e3, ATTR_1, 100, 150, e3.getTimestamp(), e3.getExternalId()),
                new TemporalBadgeSignal(rule.getId(), e6, ATTR_2, 100, 150, e6.getTimestamp(), e6.getExternalId()));
    }

    @DisplayName("Multi Threshold: many badges in different time unit creation")
    @Test
    public void testMultiTMultiBadgeDiffT() {
        TEvent e1 = TEvent.createKeyValue(110, EVENT_TYPE, 89);
        TEvent e2 = TEvent.createKeyValue(115, EVENT_TYPE, 54);
        TEvent e3 = TEvent.createKeyValue(120, EVENT_TYPE, 63);
        TEvent e4 = TEvent.createKeyValue(125, EVENT_TYPE, 75);
        TEvent e5 = TEvent.createKeyValue(130, EVENT_TYPE, 74);
        TEvent e6 = TEvent.createKeyValue(165, EVENT_TYPE, 39);
        TEvent e7 = TEvent.createKeyValue(170, EVENT_TYPE, 69);
        TEvent e8 = TEvent.createKeyValue(175, EVENT_TYPE, 57);
        TEvent e9 = TEvent.createKeyValue(180, EVENT_TYPE, 84);

        List<Signal> signals = new ArrayList<>();
        RuleContext<BadgeTemporalRule> ruleContext = createRule(FIFTY, signals, aT(ATTR_1, T_3), aT(ATTR_2, T_5), aT(ATTR_4, T_10));
        BadgeTemporalRule rule = ruleContext.getRule();
        Assertions.assertEquals(3, rule.getThresholds().size());
        BadgeTemporalProcessor processor = new BadgeTemporalProcessor(pool, ruleContext);
        submitOrder(processor, e1, e2, e3, e4, e5, e6, e7, e8, e9);

        System.out.println(signals);
        assertStrict(signals,
                new TemporalBadgeSignal(rule.getId(), e3, ATTR_1, 100, 150, e3.getTimestamp(), e3.getExternalId()),
                new TemporalBadgeSignal(rule.getId(), e5, ATTR_2, 100, 150, e5.getTimestamp(), e5.getExternalId()),
                new TemporalBadgeSignal(rule.getId(), e9, ATTR_1, 150, 200, e9.getTimestamp(), e9.getExternalId()));
    }

    @DisplayName("Multi Threshold: Out-of-order badges creation")
    @Test
    public void testMultiTOOOMultiBadgeDiffT() {
        TEvent e1 = TEvent.createKeyValue(110, EVENT_TYPE, 89);
        TEvent e2 = TEvent.createKeyValue(115, EVENT_TYPE, 54);
        TEvent e3 = TEvent.createKeyValue(125, EVENT_TYPE, 24);
        TEvent e4 = TEvent.createKeyValue(155, EVENT_TYPE, 95);
        TEvent e5 = TEvent.createKeyValue(160, EVENT_TYPE, 84);
        TEvent e6 = TEvent.createKeyValue(165, EVENT_TYPE, 89);
        TEvent e7 = TEvent.createKeyValue(120, EVENT_TYPE, 73);

        List<Signal> signals = new ArrayList<>();
        RuleContext<BadgeTemporalRule> ruleContext = createRule(FIFTY, signals, aT(ATTR_1, T_3), aT(ATTR_2, T_5), aT(ATTR_4, T_10));
        BadgeTemporalRule rule = ruleContext.getRule();
        Assertions.assertEquals(3, rule.getThresholds().size());
        BadgeTemporalProcessor processor = new BadgeTemporalProcessor(pool, ruleContext);
        submitOrder(processor, e1, e2, e3, e4, e5, e6, e7);

        System.out.println(signals);
        assertStrict(signals,
                new TemporalBadgeSignal(rule.getId(), e7, ATTR_1, 100, 150, e7.getTimestamp(), e7.getExternalId()),
                new TemporalBadgeSignal(rule.getId(), e6, ATTR_1, 150, 200, e6.getTimestamp(), e6.getExternalId()));
    }

    private BadgeTemporalRule.Threshold aT(int attr, long threshold) {
        return new BadgeTemporalRule.Threshold(attr, BigDecimal.valueOf(threshold));
    }

    private RuleContext<BadgeTemporalRule> createRule(long timeUnit, Collection<Signal> collection, BadgeTemporalRule.Threshold... thresholds) {
        BadgeTemporalCountRule rule = new BadgeTemporalCountRule("test.temporal.badge");
        rule.setForEvent(EVENT_TYPE);
        rule.setTimeUnit(timeUnit);
        rule.setCriteria((e,r,c) -> (long) e.getFieldValue("value") >= 50);
        rule.setThresholds(Arrays.asList(thresholds));
        return new RuleContext<>(rule, fromConsumer(collection::add));
    }
}
