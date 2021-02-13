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
import io.github.oasis.elements.badges.rules.*;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

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
    void parseFirstEventsFromYml() {
        List<BadgeDef> badgeDefs = TestUtils.parseAll("badge-first-event.yml", parser);
        TestUtils.findByName(badgeDefs, "Initial-Registration").ifPresent(def -> {
            assertEquals("BDG00001", def.getId());
            assertTrue(StringUtils.isNotBlank(def.getDescription()));
            assertNotNull(def.getEvent());
            assertNull(def.getEvents());
            assertEquals("firstEvent", def.getKind());
        });
        TestUtils.findByName(badgeDefs, "Initial-Registration-With-Points").ifPresent(def -> {
            assertEquals("BDG00003", def.getId());
            assertEquals("firstEvent", def.getKind());
            assertNotNull(def.getPointId());
            assertEquals(50, def.getPointAwards());
        });
    }

    @Test
    void parseConditionalFromYml() {
        List<BadgeDef> badgeDefs = TestUtils.parseAll("badge-conditional.yml", parser);
        TestUtils.findByName(badgeDefs, "Question-Quality").ifPresent(def -> {
            assertEquals("BDG-C0001", def.getId());
            assertTrue(StringUtils.isNotBlank(def.getDescription()));
            assertNotNull(def.getEvent());
            assertNull(def.getEvents());
            assertEquals("conditional", def.getKind());
            assertEquals(3, def.getConditions().size());
            def.getConditions()
                    .forEach(cond -> {
                        assertTrue(cond.getPriority() > 0);
                        assertTrue(cond.getAttribute() > 0);
                        assertNotNull(cond.getCondition());
                        assertNull(cond.getPointAwards());
                    });

        });
        TestUtils.findByName(badgeDefs, "Question-Views-With-Points").ifPresent(def -> {
            assertEquals("BDG-C0002", def.getId());
            assertEquals("conditional", def.getKind());
            assertNotNull(def.getPointId());
            assertNull(def.getPointAwards());
            assertEquals(3, def.getConditions().size());
            def.getConditions()
                    .forEach(cond -> {
                        assertTrue(cond.getPriority() > 0);
                        assertTrue(cond.getAttribute() > 0);
                        assertNotNull(cond.getCondition());
                        assertNotNull(cond.getPointAwards());
                        assertTrue(cond.getPointAwards() instanceof Number);
                    });
        });
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
            assertTrue(abstractDef instanceof BadgeDef);

            BadgeDef parsed = (BadgeDef) abstractDef;
            assertEquals(def.getEvent(), parsed.getEvent());
        }

        {
            BadgeDef def = createStreak();
            PersistedDef persistedDef = new PersistedDef();
            persistedDef.setType(PersistedDef.GAME_RULE_ADDED);
            persistedDef.setImpl(BadgeDef.class.getName());
            persistedDef.setData(toMap(def));

            AbstractDef abstractDef = parser.parse(persistedDef);
            assertTrue(abstractDef instanceof BadgeDef);

            BadgeDef parsed = (BadgeDef) abstractDef;
            assertEquals(def.getStreaks().size(), parsed.getStreaks().size());
        }
    }

    @Test
    void convertStreak() {
        BadgeDef def = createStreak();

        AbstractRule abstractRule = parser.convert(def);
        assertNotNull(abstractRule);
        assertTrue(abstractRule instanceof StreakNBadgeRule);

        StreakNBadgeRule rule = (StreakNBadgeRule) abstractRule;
        assertEquals(10, rule.getMaxStreak());
        assertEquals(3, rule.getMinStreak());
        assertNotNull(rule.getCriteria());
        assertEquals(def.getStreaks().size(), rule.getStreaks().size());
        // streaks must be ordered by priority
        assertEquals(10, rule.getStreaks().stream()
                .reduce(0, (val1, val2) -> {
                    assertTrue(val1 < val2);
                    return val2;
                })
                .intValue());
    }

    @Test
    void convertFirstEvent() {
        BadgeDef def = createFirstEvent();

        AbstractRule abstractRule = parser.convert(def);
        assertNotNull(abstractRule);
        assertTrue(abstractRule instanceof FirstEventBadgeRule);

        FirstEventBadgeRule rule = (FirstEventBadgeRule) abstractRule;
        assertEquals("event.a", rule.getEventName());
        assertEquals(1, rule.getAttributeId());
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
        assertNotNull(abstractRule);
        assertTrue(abstractRule instanceof ConditionalBadgeRule);

        ConditionalBadgeRule rule = (ConditionalBadgeRule) abstractRule;
        assertEquals(def.getMaxAwardTimes(), rule.getMaxAwardTimes());
        assertEquals(def.getConditions().size(), rule.getConditions().size());
        // conditions must be ordered by priority
        assertEquals(4, rule.getConditions().stream()
                .map(ConditionalBadgeRule.Condition::getPriority)
                .reduce(0, (val1, val2) -> {
                    assertTrue(val1 < val2);
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
        def.setEventFilter("e.data.value > 100");
        def.setStreaks(List.of(
                new BadgeDef.Streak(3, 1),
                new BadgeDef.Streak(5, 2),
                new BadgeDef.Streak(10, 3)
        ));

        AbstractRule abstractRule = parser.convert(def);
        assertNotNull(abstractRule);
        assertTrue(abstractRule instanceof TimeBoundedStreakNRule);

        TimeBoundedStreakNRule rule = (TimeBoundedStreakNRule) abstractRule;
        assertEquals(86400000, rule.getTimeUnit());
        assertEquals(10, rule.getMaxStreak());
        assertEquals(3, rule.getMinStreak());
        assertEquals(2, rule.getAttributeForStreak(5));
        assertEquals(0, rule.getAttributeForStreak(0));
        assertEquals(3, rule.getAttributeForStreak(10));
        assertEquals(0, rule.getAttributeForStreak(11));
        assertEquals(def.getStreaks().size(), rule.getStreaks().size());

        // streaks must be ordered by priority
        assertEquals(10, rule.getStreaks().stream()
                .reduce(0, (val1, val2) -> {
                    assertTrue(val1 < val2);
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
        def.setAggregatorExtractor("e.data.score");
        def.setStreaks(List.of(
                new BadgeDef.Streak(3, 1),
                new BadgeDef.Streak(5, 2),
                new BadgeDef.Streak(10, 3)
        ));

        AbstractRule abstractRule = parser.convert(def);
        assertNotNull(abstractRule);
        assertTrue(abstractRule instanceof PeriodicStreakNRule);

        PeriodicStreakNRule rule = (PeriodicStreakNRule) abstractRule;
        assertEquals(3600, rule.getTimeUnit());
        assertEquals(10, rule.getMaxStreak());
        assertEquals(3, rule.getMinStreak());
        assertEquals(new BigDecimal("100.0"), rule.getThreshold());
        assertNotNull(rule.getValueResolver());

        assertEquals(def.getStreaks().size(), rule.getStreaks().size());

        // streaks must be ordered by priority
        assertEquals(10, rule.getStreaks().stream()
                .reduce(0, (val1, val2) -> {
                    assertTrue(val1 < val2);
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
        def.setEventFilter("e.value > 50");
        def.setStreaks(List.of(
                new BadgeDef.Streak(3, 1),
                new BadgeDef.Streak(5, 2),
                new BadgeDef.Streak(10, 3)
        ));

        AbstractRule abstractRule = parser.convert(def);
        assertNotNull(abstractRule);
        assertTrue(abstractRule instanceof PeriodicOccurrencesStreakNRule);

        PeriodicOccurrencesStreakNRule rule = (PeriodicOccurrencesStreakNRule) abstractRule;
        assertEquals(3600, rule.getTimeUnit());
        assertEquals(10, rule.getMaxStreak());
        assertEquals(3, rule.getMinStreak());
        assertEquals(new BigDecimal("100.0"), rule.getThreshold());
        assertNotNull(rule.getValueResolver());
        ExecutionContext context = new ExecutionContext();
        assertEquals(BigDecimal.ONE, rule.getValueResolver().resolve(TEvent.createKeyValue(100, "event.a", 60), context));
        assertEquals(BigDecimal.ZERO, rule.getValueResolver().resolve(TEvent.createKeyValue(100, "event.a", 40), context));

        assertEquals(def.getStreaks().size(), rule.getStreaks().size());

        // streaks must be ordered by priority
        assertEquals(10, rule.getStreaks().stream()
                .reduce(0, (val1, val2) -> {
                    assertTrue(val1 < val2);
                    return val2;
                }).intValue());
    }

    @Test
    void convertPeriodicAccumulation() {
        BadgeDef def = createBase();
        def.setKind(BadgeDef.PERIODIC_ACCUMULATIONS_KIND);
        def.setTimeUnit(1234);
        def.setAggregatorExtractor("e.data.value");
        def.setThresholds(List.of(
                new BadgeDef.Threshold(BigDecimal.valueOf(100.0), 1),
                new BadgeDef.Threshold(BigDecimal.valueOf(300.0), 3),
                new BadgeDef.Threshold(BigDecimal.valueOf(200.0), 2)
        ));

        AbstractRule abstractRule = parser.convert(def);
        assertNotNull(abstractRule);
        assertTrue(abstractRule instanceof PeriodicBadgeRule);

        PeriodicBadgeRule rule = (PeriodicBadgeRule) abstractRule;
        assertEquals(1234, rule.getTimeUnit());
        assertNotNull(rule.getValueResolver());
        assertEquals(def.getThresholds().size(), rule.getThresholds().size());

        // thresholds must be in reverse order
        assertEquals(new BigDecimal("100.0"), rule.getThresholds().stream()
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
        def.setEventFilter("e.data.value > 100");
        def.setThresholds(List.of(
                new BadgeDef.Threshold(BigDecimal.valueOf(100.0), 1),
                new BadgeDef.Threshold(BigDecimal.valueOf(300.0), 3),
                new BadgeDef.Threshold(BigDecimal.valueOf(200.0), 2)
        ));

        AbstractRule abstractRule = parser.convert(def);
        assertNotNull(abstractRule);
        assertTrue(abstractRule instanceof PeriodicOccurrencesRule);

        PeriodicOccurrencesRule rule = (PeriodicOccurrencesRule) abstractRule;
        assertEquals(1234, rule.getTimeUnit());
        assertNotNull(rule.getValueResolver());
        assertNotNull(rule.getCriteria());
        assertEquals(def.getThresholds().size(), rule.getThresholds().size());
        assertEquals(BigDecimal.ONE, rule.getValueResolver().resolve(new EventJson(), new ExecutionContext()));

        // thresholds must be in reverse order
        assertEquals(new BigDecimal("100.0"), rule.getThresholds().stream()
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
        def.setEventFilter("e.data.value > 100");
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