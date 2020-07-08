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

package io.github.oasis.elements.badges;

import io.github.oasis.core.EventJson;
import io.github.oasis.core.context.ExecutionContext;
import io.github.oasis.core.elements.AbstractDef;
import io.github.oasis.core.elements.AbstractRule;
import io.github.oasis.core.external.messages.PersistedDef;
import io.github.oasis.elements.badges.rules.ConditionalBadgeRule;
import io.github.oasis.elements.badges.rules.FirstEventBadgeRule;
import io.github.oasis.elements.badges.rules.PeriodicBadgeRule;
import io.github.oasis.elements.badges.rules.PeriodicOccurrencesRule;
import io.github.oasis.elements.badges.rules.PeriodicOccurrencesStreakNRule;
import io.github.oasis.elements.badges.rules.PeriodicStreakNRule;
import io.github.oasis.elements.badges.rules.StreakNBadgeRule;
import io.github.oasis.elements.badges.rules.TimeBoundedStreakNRule;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Isuru Weerarathna
 */
class BadgeParserTest {

    private BadgeParser parser;

    @BeforeEach
    void beforeEach() {
        parser = new BadgeParser();
    }

    @Test
    void parse() {
        {
            BadgeDef def = createFirstEvent();
            PersistedDef persistedDef = new PersistedDef();
            persistedDef.setType(PersistedDef.GAME_RULE_ADDED);
            persistedDef.setImpl(BadgeDef.class.getName());
            persistedDef.setData(toMap(def));

            AbstractDef abstractDef = parser.parse(persistedDef);
            Assertions.assertTrue(abstractDef instanceof BadgeDef);

            BadgeDef parsed = (BadgeDef) abstractDef;
            Assertions.assertEquals(def.getEvent(), parsed.getEvent());
        }

        {
            BadgeDef def = createStreak();
            PersistedDef persistedDef = new PersistedDef();
            persistedDef.setType(PersistedDef.GAME_RULE_ADDED);
            persistedDef.setImpl(BadgeDef.class.getName());
            persistedDef.setData(toMap(def));

            AbstractDef abstractDef = parser.parse(persistedDef);
            Assertions.assertTrue(abstractDef instanceof BadgeDef);

            BadgeDef parsed = (BadgeDef) abstractDef;
            Assertions.assertEquals(def.getStreaks().size(), parsed.getStreaks().size());
        }
    }

    @Test
    void convertStreak() {
        BadgeDef def = createStreak();

        AbstractRule abstractRule = parser.convert(def);
        Assertions.assertNotNull(abstractRule);
        Assertions.assertTrue(abstractRule instanceof StreakNBadgeRule);

        StreakNBadgeRule rule = (StreakNBadgeRule) abstractRule;
        Assertions.assertEquals(10, rule.getMaxStreak());
        Assertions.assertEquals(3, rule.getMinStreak());
        Assertions.assertNotNull(rule.getCriteria());
        Assertions.assertEquals(def.getStreaks().size(), rule.getStreaks().size());
        // streaks must be ordered by priority
        Assertions.assertEquals(10, rule.getStreaks().stream()
                .reduce(0, (val1, val2) -> {
                    Assertions.assertTrue(val1 < val2);
                    return val2;
                })
                .intValue());
    }

    @Test
    void convertFirstEvent() {
        BadgeDef def = createFirstEvent();

        AbstractRule abstractRule = parser.convert(def);
        Assertions.assertNotNull(abstractRule);
        Assertions.assertTrue(abstractRule instanceof FirstEventBadgeRule);

        FirstEventBadgeRule rule = (FirstEventBadgeRule) abstractRule;
        Assertions.assertEquals("event.a", rule.getEventName());
        Assertions.assertEquals(1, rule.getAttributeId());
    }

    @Test
    void convertConditional() {
        BadgeDef def = createBase();
        def.setKind(BadgeDef.CONDITIONAL_KIND);
        def.setMaxAwardTimes(5);
        def.setConditions(List.of(
                new BadgeDef.Condition(1, "e.data.value > 100", 1),
                new BadgeDef.Condition(4, "e.data.value > 300", 3),
                new BadgeDef.Condition(2, "e.data.value > 200", 2)
        ));

        AbstractRule abstractRule = parser.convert(def);
        Assertions.assertNotNull(abstractRule);
        Assertions.assertTrue(abstractRule instanceof ConditionalBadgeRule);

        ConditionalBadgeRule rule = (ConditionalBadgeRule) abstractRule;
        Assertions.assertEquals(def.getMaxAwardTimes(), rule.getMaxAwardTimes());
        Assertions.assertEquals(def.getConditions().size(), rule.getConditions().size());
        // conditions must be ordered by priority
        Assertions.assertEquals(4, rule.getConditions().stream()
                .map(ConditionalBadgeRule.Condition::getPriority)
                .reduce(0, (val1, val2) -> {
                    Assertions.assertTrue(val1 < val2);
                    return val2;
                })
                .intValue());
    }

    @Test
    void convertTimeBoundedStreak() {
        BadgeDef def = createBase();
        def.setKind(BadgeDef.TIME_BOUNDED_STREAK_KIND);
        def.setConsecutive(true);
        def.setTimeUnit("daily");
        def.setCondition("e.data.value > 100");
        def.setStreaks(List.of(
                new BadgeDef.Streak(3, 1),
                new BadgeDef.Streak(5, 2),
                new BadgeDef.Streak(10, 3)
        ));

        AbstractRule abstractRule = parser.convert(def);
        Assertions.assertNotNull(abstractRule);
        Assertions.assertTrue(abstractRule instanceof TimeBoundedStreakNRule);

        TimeBoundedStreakNRule rule = (TimeBoundedStreakNRule) abstractRule;
        Assertions.assertEquals(86400000, rule.getTimeUnit());
        Assertions.assertEquals(10, rule.getMaxStreak());
        Assertions.assertEquals(3, rule.getMinStreak());
        Assertions.assertEquals(2, rule.getAttributeForStreak(5));
        Assertions.assertEquals(0, rule.getAttributeForStreak(0));
        Assertions.assertEquals(3, rule.getAttributeForStreak(10));
        Assertions.assertEquals(0, rule.getAttributeForStreak(11));
        Assertions.assertEquals(def.getStreaks().size(), rule.getStreaks().size());

        // streaks must be ordered by priority
        Assertions.assertEquals(10, rule.getStreaks().stream()
                .reduce(0, (val1, val2) -> {
                    Assertions.assertTrue(val1 < val2);
                    return val2;
                }).intValue());
    }

    @Test
    void convertPeriodicAccumulatorStreak() {
        BadgeDef def = createBase();
        def.setKind(BadgeDef.PERIODIC_ACCUMULATIONS_STREAK_KIND);
        def.setConsecutive(true);
        def.setTimeUnit(3600);
        def.setThreshold(BigDecimal.valueOf(100.0));
        def.setValueExtractorExpression("e.data.score");
        def.setStreaks(List.of(
                new BadgeDef.Streak(3, 1),
                new BadgeDef.Streak(5, 2),
                new BadgeDef.Streak(10, 3)
        ));

        AbstractRule abstractRule = parser.convert(def);
        Assertions.assertNotNull(abstractRule);
        Assertions.assertTrue(abstractRule instanceof PeriodicStreakNRule);

        PeriodicStreakNRule rule = (PeriodicStreakNRule) abstractRule;
        Assertions.assertEquals(3600, rule.getTimeUnit());
        Assertions.assertEquals(10, rule.getMaxStreak());
        Assertions.assertEquals(3, rule.getMinStreak());
        Assertions.assertEquals(new BigDecimal("100.0"), rule.getThreshold());
        Assertions.assertNotNull(rule.getValueResolver());

        Assertions.assertEquals(def.getStreaks().size(), rule.getStreaks().size());

        // streaks must be ordered by priority
        Assertions.assertEquals(10, rule.getStreaks().stream()
                .reduce(0, (val1, val2) -> {
                    Assertions.assertTrue(val1 < val2);
                    return val2;
                }).intValue());
    }

    @Test
    void convertPeriodicCountStreak() {
        BadgeDef def = createBase();
        def.setKind(BadgeDef.PERIODIC_OCCURRENCES_STREAK_KIND);
        def.setConsecutive(true);
        def.setTimeUnit(3600);
        def.setThreshold(BigDecimal.valueOf(100.0));
        def.setCondition("e.value > 50");
        def.setStreaks(List.of(
                new BadgeDef.Streak(3, 1),
                new BadgeDef.Streak(5, 2),
                new BadgeDef.Streak(10, 3)
        ));

        AbstractRule abstractRule = parser.convert(def);
        Assertions.assertNotNull(abstractRule);
        Assertions.assertTrue(abstractRule instanceof PeriodicOccurrencesStreakNRule);

        PeriodicOccurrencesStreakNRule rule = (PeriodicOccurrencesStreakNRule) abstractRule;
        Assertions.assertEquals(3600, rule.getTimeUnit());
        Assertions.assertEquals(10, rule.getMaxStreak());
        Assertions.assertEquals(3, rule.getMinStreak());
        Assertions.assertEquals(new BigDecimal("100.0"), rule.getThreshold());
        Assertions.assertNotNull(rule.getValueResolver());
        ExecutionContext context = new ExecutionContext();
        Assertions.assertEquals(BigDecimal.ONE, rule.getValueResolver().resolve(TEvent.createKeyValue(100, "event.a", 60), context));
        Assertions.assertEquals(BigDecimal.ZERO, rule.getValueResolver().resolve(TEvent.createKeyValue(100, "event.a", 40), context));

        Assertions.assertEquals(def.getStreaks().size(), rule.getStreaks().size());

        // streaks must be ordered by priority
        Assertions.assertEquals(10, rule.getStreaks().stream()
                .reduce(0, (val1, val2) -> {
                    Assertions.assertTrue(val1 < val2);
                    return val2;
                }).intValue());
    }

    @Test
    void convertPeriodicAccumulation() {
        BadgeDef def = createBase();
        def.setKind(BadgeDef.PERIODIC_ACCUMULATIONS_KIND);
        def.setTimeUnit(1234);
        def.setValueExtractorExpression("e.data.value");
        def.setThresholds(List.of(
                new BadgeDef.Threshold(BigDecimal.valueOf(100.0), 1),
                new BadgeDef.Threshold(BigDecimal.valueOf(300.0), 3),
                new BadgeDef.Threshold(BigDecimal.valueOf(200.0), 2)
        ));

        AbstractRule abstractRule = parser.convert(def);
        Assertions.assertNotNull(abstractRule);
        Assertions.assertTrue(abstractRule instanceof PeriodicBadgeRule);

        PeriodicBadgeRule rule = (PeriodicBadgeRule) abstractRule;
        Assertions.assertEquals(1234, rule.getTimeUnit());
        Assertions.assertNotNull(rule.getValueResolver());
        Assertions.assertEquals(def.getThresholds().size(), rule.getThresholds().size());

        // thresholds must be in reverse order
        Assertions.assertEquals(new BigDecimal("100.0"), rule.getThresholds().stream()
                .map(PeriodicBadgeRule.Threshold::getValue)
                .reduce((val1, val2) -> {
                    System.out.println(val1 + ", " + val2);
                    assertTrue(val1.compareTo(val2) > 0);
                    return val2;
                }).get());
    }

    @Test
    void convertPeriodicOccurences() {
        BadgeDef def = createBase();
        def.setKind(BadgeDef.PERIODIC_OCCURRENCES_KIND);
        def.setTimeUnit(1234);
        def.setCondition("e.data.value > 100");
        def.setThresholds(List.of(
                new BadgeDef.Threshold(BigDecimal.valueOf(100.0), 1),
                new BadgeDef.Threshold(BigDecimal.valueOf(300.0), 3),
                new BadgeDef.Threshold(BigDecimal.valueOf(200.0), 2)
        ));

        AbstractRule abstractRule = parser.convert(def);
        Assertions.assertNotNull(abstractRule);
        Assertions.assertTrue(abstractRule instanceof PeriodicOccurrencesRule);

        PeriodicOccurrencesRule rule = (PeriodicOccurrencesRule) abstractRule;
        Assertions.assertEquals(1234, rule.getTimeUnit());
        Assertions.assertNotNull(rule.getValueResolver());
        Assertions.assertNotNull(rule.getCriteria());
        Assertions.assertEquals(def.getThresholds().size(), rule.getThresholds().size());
        Assertions.assertEquals(BigDecimal.ONE, rule.getValueResolver().resolve(new EventJson(), new ExecutionContext()));

        // thresholds must be in reverse order
        Assertions.assertEquals(new BigDecimal("100.0"), rule.getThresholds().stream()
                .map(PeriodicBadgeRule.Threshold::getValue)
                .reduce((val1, val2) -> {
                    System.out.println(val1 + ", " + val2);
                    assertTrue(val1.compareTo(val2) > 0);
                    return val2;
                }).get());
    }


    private BadgeDef createBase() {
        BadgeDef def = new BadgeDef();
        def.setId("BADGE00001");
        def.setName("badge-1");
        def.setEvent("event.a");
        return def;
    }

    private BadgeDef createFirstEvent() {
        BadgeDef def = new BadgeDef();
        def.setId("BADGE00002");
        def.setKind(BadgeDef.FIRST_EVENT_KIND);
        def.setName("badge-1");
        def.setEvent("event.a");
        def.setAttribute(1);
        return def;
    }

    private BadgeDef createStreak() {
        BadgeDef def = new BadgeDef();
        def.setId("BADGE00003");
        def.setKind(BadgeDef.STREAK_N_KIND);
        def.setName("badge-2");
        def.setEvent("event.a");
        def.setCondition("e.data.value > 100");
        def.setConsecutive(true);
        def.setStreaks(List.of(
                new BadgeDef.Streak(3, 1),
                new BadgeDef.Streak(5, 2),
                new BadgeDef.Streak(10, 3)
        ));

        return def;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> toMap(AbstractDef def) {
        Yaml yaml = new Yaml();
        System.out.println(yaml.dumpAsMap(def));
        return (Map<String, Object>) yaml.load(yaml.dumpAsMap(def));
    }
}