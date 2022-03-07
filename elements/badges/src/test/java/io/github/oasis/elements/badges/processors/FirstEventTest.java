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
import io.github.oasis.elements.badges.rules.FirstEventBadgeRule;
import io.github.oasis.elements.badges.signals.BadgeSignal;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Isuru Weerarathna
 */
@DisplayName("First Event Badges")
public class FirstEventTest extends AbstractRuleTest {

    private static final String EVT_1 = "app.user.registered";
    private static final String EVT_2 = "app.evt";
    private static final String DEF_FILE = "kinds/firstEvents.yml";

    @DisplayName("No Condition: no event types")
    @Test
    public void testFirstWithoutConditionNoEvents() {
        TEvent e1 = TEvent.createKeyValue(110, EVT_2, 75);
        TEvent e2 = TEvent.createKeyValue(144, EVT_2, 63);
        TEvent e3 = TEvent.createKeyValue(157, EVT_2, 14);

        List<Signal> signals = new ArrayList<>();
        FirstEventBadgeRule rule = loadRule(DEF_FILE, "USER_REGISTRATION");
        RuleContext<FirstEventBadgeRule> ruleContext = createRule(rule, signals);
        Assertions.assertEquals(EVT_1, rule.getEventName());
        BadgeFirstEvent firstEvent = new BadgeFirstEvent(pool, ruleContext);
        submitOrder(firstEvent, e1, e2, e3);

        System.out.println(signals);
        Assertions.assertEquals(0, signals.size());
    }


    @DisplayName("No Condition: same event types")
    @Test
    public void testFirstWithoutConditionSameEvents() {
        TEvent e1 = TEvent.createKeyValue(110, EVT_1, 75);
        TEvent e2 = TEvent.createKeyValue(144, EVT_1, 63);
        TEvent e3 = TEvent.createKeyValue(157, EVT_1, 14);

        List<Signal> signals = new ArrayList<>();
        FirstEventBadgeRule rule = loadRule(DEF_FILE, "USER_REGISTRATION");
        RuleContext<FirstEventBadgeRule> ruleContext = createRule(rule, signals);
        Assertions.assertEquals(EVT_1, rule.getEventName());
        BadgeFirstEvent firstEvent = new BadgeFirstEvent(pool, ruleContext);
        submitOrder(firstEvent, e1, e2, e3);

        System.out.println(signals);
        Assertions.assertEquals(1, signals.size());

        assertSignal(signals, new BadgeSignal(rule.getId(), rule.getId(), e1, e1.getTimestamp(), rule.getAttributeId(), 110, 110, e1.getExternalId(), e1.getExternalId()));
    }

    @DisplayName("No Condition: with point awards")
    @Test
    public void testFirstWithPoints() {
        TEvent e1 = TEvent.createKeyValue(110, EVT_1, 75);
        TEvent e2 = TEvent.createKeyValue(144, EVT_1, 63);
        TEvent e3 = TEvent.createKeyValue(157, EVT_1, 14);

        List<Signal> signals = new ArrayList<>();
        FirstEventBadgeRule rule = loadRule(DEF_FILE, "BADGE_WITH_POINTS");
        RuleContext<FirstEventBadgeRule> ruleContext = createRule(rule, signals);
        Assertions.assertEquals(EVT_1, rule.getEventName());
        BadgeFirstEvent firstEvent = new BadgeFirstEvent(pool, ruleContext);
        submitOrder(firstEvent, e1, e2, e3);

        System.out.println(signals);
        Assertions.assertEquals(1, signals.size());

        assertSignal(signals,
                new BadgeSignal(rule.getId(), rule.getId(), e1, e1.getTimestamp(), rule.getAttributeId(), 110, 110, e1.getExternalId(), e1.getExternalId())
                    .setPointAwards(rule.getPointId(), rule.getPointAwards()));
    }

    @DisplayName("No Condition: different event types")
    @Test
    public void testFirstWithoutConditionDifferentEvents() {
        TEvent e1 = TEvent.createKeyValue(110, EVT_1, 75);
        TEvent e2 = TEvent.createKeyValue(144, EVT_2, 63);
        TEvent e3 = TEvent.createKeyValue(157, EVT_2, 14);

        List<Signal> signals = new ArrayList<>();
        FirstEventBadgeRule rule = loadRule(DEF_FILE, "USER_REGISTRATION");
        RuleContext<FirstEventBadgeRule> ruleContext = createRule(rule, signals);
        Assertions.assertEquals(EVT_1, rule.getEventName());
        BadgeFirstEvent firstEvent = new BadgeFirstEvent(pool, ruleContext);
        submitOrder(firstEvent, e1, e2, e3);

        System.out.println(signals);
        Assertions.assertEquals(1, signals.size());

        assertSignal(signals, new BadgeSignal(rule.getId(), rule.getId(), e1, e1.getTimestamp(), rule.getAttributeId(), 110, 110, e1.getExternalId(), e1.getExternalId()));
    }

    @DisplayName("No Condition: mixed event types")
    @Test
    public void testFirstWithoutConditionMixedEvents() {
        TEvent e1 = TEvent.createKeyValue(110, EVT_1, 75);
        TEvent e2 = TEvent.createKeyValue(144, EVT_2, 63);
        TEvent e3 = TEvent.createKeyValue(157, EVT_1, 76);

        List<Signal> signals = new ArrayList<>();
        FirstEventBadgeRule rule = loadRule(DEF_FILE, "USER_REGISTRATION");
        RuleContext<FirstEventBadgeRule> ruleContext = createRule(rule, signals);
        Assertions.assertEquals(EVT_1, rule.getEventName());
        BadgeFirstEvent firstEvent = new BadgeFirstEvent(pool, ruleContext);
        submitOrder(firstEvent, e1, e2, e3);

        System.out.println(signals);
        Assertions.assertEquals(1, signals.size());

        assertSignal(signals, new BadgeSignal(rule.getId(), rule.getId(), e1, e1.getTimestamp(), rule.getAttributeId(), 110, 110, e1.getExternalId(), e1.getExternalId()));
    }

    @DisplayName("With Condition: same event types")
    @Test
    public void testFirstWithConditionSameEvents() {
        TEvent e1 = TEvent.createKeyValue(110, EVT_1, 14);
        TEvent e2 = TEvent.createKeyValue(144, EVT_1, 63);
        TEvent e3 = TEvent.createKeyValue(157, EVT_1, 76);

        List<Signal> signals = new ArrayList<>();
        FirstEventBadgeRule rule = loadRule(DEF_FILE, "USER_REGISTRATION_WITH_CONDITION");
        RuleContext<FirstEventBadgeRule> ruleContext = createRule(rule, signals);
        Assertions.assertEquals(EVT_1, rule.getEventName());
        BadgeFirstEvent firstEvent = new BadgeFirstEvent(pool, ruleContext);
        submitOrder(firstEvent, e1, e2, e3);

        System.out.println(signals);
        Assertions.assertEquals(1, signals.size());

        assertSignal(signals, new BadgeSignal(rule.getId(), rule.getId(), e2, e2.getTimestamp(), rule.getAttributeId(), 144, 144, e2.getExternalId(), e2.getExternalId()));
    }

    @DisplayName("With Condition: different event types")
    @Test
    public void testFirstWithConditionDifferentEvents() {
        TEvent e1 = TEvent.createKeyValue(110, EVT_2, 14);
        TEvent e2 = TEvent.createKeyValue(144, EVT_1, 63);
        TEvent e3 = TEvent.createKeyValue(157, EVT_2, 76);

        List<Signal> signals = new ArrayList<>();
        FirstEventBadgeRule rule = loadRule(DEF_FILE, "USER_REGISTRATION_WITH_CONDITION");
        RuleContext<FirstEventBadgeRule> ruleContext = createRule(rule, signals);
        Assertions.assertEquals(EVT_1, rule.getEventName());
        BadgeFirstEvent firstEvent = new BadgeFirstEvent(pool, ruleContext);
        submitOrder(firstEvent, e1, e2, e3);

        System.out.println(signals);
        Assertions.assertEquals(1, signals.size());

        assertSignal(signals, new BadgeSignal(rule.getId(), rule.getId(), e2, e2.getTimestamp(), rule.getAttributeId(), 144, 144, e2.getExternalId(), e2.getExternalId()));
    }

    @DisplayName("With Condition: mixed event types")
    @Test
    public void testFirstWithConditionMixedEvents() {
        TEvent e1 = TEvent.createKeyValue(110, EVT_1, 14);
        TEvent e2 = TEvent.createKeyValue(144, EVT_1, 63);
        TEvent e3 = TEvent.createKeyValue(125, EVT_2, 76);

        List<Signal> signals = new ArrayList<>();
        FirstEventBadgeRule rule = loadRule(DEF_FILE, "USER_REGISTRATION_WITH_CONDITION");
        RuleContext<FirstEventBadgeRule> ruleContext = createRule(rule, signals);
        Assertions.assertEquals(EVT_1, rule.getEventName());
        BadgeFirstEvent firstEvent = new BadgeFirstEvent(pool, ruleContext);
        submitOrder(firstEvent, e1, e2, e3);

        System.out.println(signals);
        Assertions.assertEquals(1, signals.size());

        assertSignal(signals, new BadgeSignal(rule.getId(), rule.getId(), e2, e2.getTimestamp(), rule.getAttributeId(), 144, 144, e2.getExternalId(), e2.getExternalId()));
    }

    @DisplayName("With Condition: no event types")
    @Test
    public void testFirstWithConditionNoEvents() {
        TEvent e1 = TEvent.createKeyValue(110, EVT_2, 14);
        TEvent e2 = TEvent.createKeyValue(144, EVT_2, 63);
        TEvent e3 = TEvent.createKeyValue(125, EVT_2, 76);

        List<Signal> signals = new ArrayList<>();
        FirstEventBadgeRule rule = loadRule(DEF_FILE, "USER_REGISTRATION_WITH_CONDITION");
        RuleContext<FirstEventBadgeRule> ruleContext = createRule(rule, signals);
        BadgeFirstEvent firstEvent = new BadgeFirstEvent(pool, ruleContext);
        submitOrder(firstEvent, e1, e2, e3);

        System.out.println(signals);
        Assertions.assertEquals(0, signals.size());
    }

    @DisplayName("With Condition: no satisfied event types")
    @Test
    public void testFirstWithConditionNoSatisfiedEvents() {
        TEvent e1 = TEvent.createKeyValue(110, EVT_1, 14);
        TEvent e2 = TEvent.createKeyValue(144, EVT_1, 32);
        TEvent e3 = TEvent.createKeyValue(125, EVT_1, 11);

        List<Signal> signals = new ArrayList<>();
        FirstEventBadgeRule rule = loadRule(DEF_FILE, "USER_REGISTRATION_WITH_CONDITION");
        RuleContext<FirstEventBadgeRule> ruleContext = createRule(rule, signals);
        BadgeFirstEvent firstEvent = new BadgeFirstEvent(pool, ruleContext);
        submitOrder(firstEvent, e1, e2, e3);

        System.out.println(signals);
        Assertions.assertEquals(0, signals.size());
    }

    @DisplayName("With Condition: out-of-order event does not affect")
    @Test
    public void testFirstWithConditionOOOEvents() {
        TEvent e1 = TEvent.createKeyValue(110, EVT_1, 14);
        TEvent e2 = TEvent.createKeyValue(144, EVT_1, 55);
        TEvent e3 = TEvent.createKeyValue(125, EVT_1, 98);

        List<Signal> signals = new ArrayList<>();
        FirstEventBadgeRule rule = loadRule(DEF_FILE, "USER_REGISTRATION_WITH_CONDITION");
        RuleContext<FirstEventBadgeRule> ruleContext = createRule(rule, signals);
        Assertions.assertEquals(EVT_1, rule.getEventName());
        BadgeFirstEvent firstEvent = new BadgeFirstEvent(pool, ruleContext);
        submitOrder(firstEvent, e1, e2, e3);

        System.out.println(signals);
        Assertions.assertEquals(1, signals.size());

        assertSignal(signals, new BadgeSignal(rule.getId(), rule.getId(), e2, e2.getTimestamp(), rule.getAttributeId(), 144, 144, e2.getExternalId(), e2.getExternalId()));
    }

    private RuleContext<FirstEventBadgeRule> createRule(FirstEventBadgeRule rule, List<Signal> collectTo) {
        return new RuleContext<>(rule, fromConsumer(collectTo::add));
    }
}
