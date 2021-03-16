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
import io.github.oasis.core.elements.spec.BaseSpecification;
import io.github.oasis.core.elements.spec.EventFilterDef;
import io.github.oasis.core.elements.spec.SelectorDef;
import io.github.oasis.core.elements.spec.TimeUnitDef;
import io.github.oasis.core.external.messages.EngineMessage;
import io.github.oasis.core.utils.Texts;
import io.github.oasis.elements.badges.rules.ConditionalBadgeRule;
import io.github.oasis.elements.badges.rules.FirstEventBadgeRule;
import io.github.oasis.elements.badges.rules.PeriodicBadgeRule;
import io.github.oasis.elements.badges.rules.PeriodicOccurrencesRule;
import io.github.oasis.elements.badges.rules.PeriodicOccurrencesStreakNRule;
import io.github.oasis.elements.badges.rules.PeriodicStreakNRule;
import io.github.oasis.elements.badges.rules.StreakNBadgeRule;
import io.github.oasis.elements.badges.rules.TimeBoundedStreakNRule;
import io.github.oasis.elements.badges.spec.BadgeSpecification;
import io.github.oasis.elements.badges.spec.Condition;
import io.github.oasis.elements.badges.spec.RewardDef;
import io.github.oasis.elements.badges.spec.Streak;
import io.github.oasis.elements.badges.spec.Threshold;
import io.github.oasis.elements.badges.spec.ValueExtractorDef;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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
    void parseFirstEventsFromYml() {
        List<BadgeDef> badgeDefs = TestUtils.parseAll("badge-first-event.yml", parser);
        TestUtils.findByName(badgeDefs, "Initial-Registration").ifPresent(def -> {
            assertEquals("BDG00001", def.getId());
            assertTrue(StringUtils.isNotBlank(def.getDescription()));
            assertNotNull(def.getSpec());
            assertNotNull(def.getSpec().getSelector().getMatchEvent());
            assertNull(def.getSpec().getSelector().getMatchEvents());
            assertEquals("firstEvent", def.getSpec().getKind());
        });
        TestUtils.findByName(badgeDefs, "Initial-Registration-With-Points").ifPresent(def -> {
            assertEquals("BDG00003", def.getId());
            assertEquals("firstEvent", def.getSpec().getKind());
            assertNotNull(def.getSpec().getRewards().getPoints().getId());
            assertEquals(BigDecimal.valueOf(50), def.getSpec().getRewards().getPoints().getAmount());
        });
    }

    @Test
    void parseConditionalFromYml() {
        List<BadgeDef> badgeDefs = TestUtils.parseAll("badge-conditional.yml", parser);
        TestUtils.findByName(badgeDefs, "Question-Quality").ifPresent(def -> {
            assertEquals("BDG-C0001", def.getId());
            assertTrue(StringUtils.isNotBlank(def.getDescription()));
            assertNotNull(def.getSpec().getSelector().getMatchEvent());
            assertNull(def.getSpec().getSelector().getMatchEvents());
            assertEquals("conditional", def.getSpec().getKind());
            assertEquals(3, def.getSpec().getConditions().size());
            def.getSpec().getConditions()
                    .forEach(cond -> {
                        assertTrue(cond.getPriority() > 0);
                        assertNotNull(cond.getRewards());
                        assertTrue(cond.getRewards().getBadge().getAttribute() > 0);
                        assertTrue(Texts.isNotEmpty(cond.getCondition()));
                        assertNull(cond.getRewards().getPoints());
                    });

        });
        TestUtils.findByName(badgeDefs, "Question-Views-With-Points").ifPresent(def -> {
            assertEquals("BDG-C0002", def.getId());
            assertEquals("conditional", def.getSpec().getKind());
            assertEquals(3, def.getSpec().getConditions().size());
            def.getSpec().getConditions()
                    .forEach(cond -> {
                        assertTrue(cond.getPriority() > 0);
                        assertTrue(cond.getRewards().getBadge().getAttribute() > 0);
                        assertTrue(Texts.isNotEmpty(cond.getCondition()));
                        assertNotNull(cond.getRewards().getPoints());
                        assertNotNull(cond.getRewards().getPoints().getAmount());
                    });
        });
    }

    @Test
    void parse() {
        {
            BadgeDef def = createFirstEvent();
            EngineMessage engineMessage = new EngineMessage();
            engineMessage.setType(EngineMessage.GAME_RULE_ADDED);
            engineMessage.setImpl(BadgeDef.class.getName());
            engineMessage.setData(toMap(def));

            BadgeDef parsed = parser.parse(engineMessage);
            assertEquals(def.getSpec().getSelector().getMatchEvent(), parsed.getSpec().getSelector().getMatchEvent());
        }

        {
            BadgeDef def = createStreak();
            EngineMessage engineMessage = new EngineMessage();
            engineMessage.setType(EngineMessage.GAME_RULE_ADDED);
            engineMessage.setImpl(BadgeDef.class.getName());
            engineMessage.setData(toMap(def));

            BadgeDef parsed = parser.parse(engineMessage);
            assertEquals(def.getSpec().getStreaks().size(), parsed.getSpec().getStreaks().size());
        }
    }

    @Test
    void convertStreak() {
        BadgeDef def = createStreak();
        def.getSpec().setRetainTime(TimeUnitDef.of(7L, "days"));

        System.out.println(def);
        AbstractRule abstractRule = parser.convert(def);
        assertNotNull(abstractRule);
        assertTrue(abstractRule instanceof StreakNBadgeRule);

        StreakNBadgeRule rule = (StreakNBadgeRule) abstractRule;
        assertEquals(10, rule.getMaxStreak());
        assertEquals(3, rule.getMinStreak());
        assertNotNull(rule.getCriteria());
        assertEquals(def.getSpec().getStreaks().size(), rule.getStreaks().size());
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
        def.getSpec().setKind(BadgeDef.CONDITIONAL_KIND);
        def.getSpec().setConditions(List.of(
                new Condition(1, "e.data.value > 100", RewardDef.attributeWithMax(1, 5)),
                new Condition(4, "e.data.value > 300", RewardDef.attributeWithMax(3, 5)),
                new Condition(2, "e.data.value > 200", RewardDef.attributeWithMax(2, 5))
        ));

        AbstractRule abstractRule = parser.convert(def);
        assertNotNull(abstractRule);
        assertTrue(abstractRule instanceof ConditionalBadgeRule);

        ConditionalBadgeRule rule = (ConditionalBadgeRule) abstractRule;
        assertEquals(def.getSpec().getConditions().size(), rule.getConditions().size());
        // conditions must be ordered by priority
        assertEquals(4, rule.getConditions().stream()
                .peek(cond -> assertEquals(5, cond.getMaxBadgesAllowed()))
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
        def.getSpec().setKind(BadgeDef.TIME_BOUNDED_STREAK_KIND);
        def.getSpec().setConsecutive(true);
        def.getSpec().setTimeRange(TimeUnitDef.of(1L, "days"));
        def.getSpec().setRetainTime(TimeUnitDef.of(7L, "days"));
        def.getSpec().getSelector().setFilter(EventFilterDef.withExpression("e.data.value > 100"));
        def.getSpec().setStreaks(List.of(
                new Streak(3, RewardDef.withAttribute(1)),
                new Streak(5, RewardDef.withAttribute(2)),
                new Streak(10, RewardDef.withAttribute(3))
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
        assertEquals(def.getSpec() .getStreaks().size(), rule.getStreaks().size());

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
        def.getSpec().setKind(BadgeDef.PERIODIC_ACCUMULATIONS_STREAK_KIND);
        def.getSpec().setConsecutive(true);
        def.getSpec().setPeriod(TimeUnitDef.of(3600L, "ms"));
        def.getSpec().setThreshold(BigDecimal.valueOf(100.0));
        def.getSpec().setAggregatorExtractor(new ValueExtractorDef("e.data.score"));
        def.getSpec().setStreaks(List.of(
                new Streak(3, RewardDef.withAttribute(1)),
                new Streak(5, RewardDef.withAttribute(2)),
                new Streak(10, RewardDef.withAttribute(3))
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

        assertEquals(def.getSpec().getStreaks().size(), rule.getStreaks().size());

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
        def.getSpec().setKind(BadgeDef.PERIODIC_OCCURRENCES_STREAK_KIND);
        def.getSpec().setConsecutive(true);
        def.getSpec().setPeriod(TimeUnitDef.of(3600L, "milli"));
        def.getSpec().setThreshold(BigDecimal.valueOf(100.0));
        def.getSpec().getSelector().setFilter(EventFilterDef.withExpression("e.value > 50"));
        def.getSpec().setStreaks(List.of(
                new Streak(3, RewardDef.withAttribute(1)),
                new Streak(5, RewardDef.withAttribute(2)),
                new Streak(10, RewardDef.withAttribute(3))
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

        assertEquals(def.getSpec().getStreaks().size(), rule.getStreaks().size());

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
        def.getSpec().setKind(BadgeDef.PERIODIC_ACCUMULATIONS_KIND);
        def.getSpec().setPeriod(TimeUnitDef.of(1234L, "ms"));
        def.getSpec().setAggregatorExtractor(new ValueExtractorDef("e.data.value"));
        def.getSpec().setThresholds(List.of(
                new Threshold(BigDecimal.valueOf(100.0), RewardDef.withAttribute(1)),
                new Threshold(BigDecimal.valueOf(300.0), RewardDef.withAttribute(3)),
                new Threshold(BigDecimal.valueOf(200.0), RewardDef.withAttribute(2))
        ));

        AbstractRule abstractRule = parser.convert(def);
        assertNotNull(abstractRule);
        assertTrue(abstractRule instanceof PeriodicBadgeRule);

        PeriodicBadgeRule rule = (PeriodicBadgeRule) abstractRule;
        assertEquals(1234, rule.getTimeUnit());
        assertNotNull(rule.getValueResolver());
        assertEquals(def.getSpec().getThresholds().size(), rule.getThresholds().size());

        // thresholds must be in reverse order
        assertEquals(new BigDecimal("100.0"), rule.getThresholds().stream()
                .map(PeriodicBadgeRule.Threshold::getValue)
                .reduce((val1, val2) -> {
                    System.out.println(val1 + ", " + val2);
                    assertTrue(val1.compareTo(val2) > 0);
                    return val2;
                }).orElseThrow());
    }

    @Test
    void convertPeriodicOccurences() {
        BadgeDef def = createBase();
        def.getSpec().setKind(BadgeDef.PERIODIC_OCCURRENCES_KIND);
        def.getSpec().setPeriod(TimeUnitDef.of(1234L, "ms"));
        def.getSpec().getSelector().setFilter(EventFilterDef.withExpression("e.data.value > 100"));
        def.getSpec().setThresholds(List.of(
                new Threshold(BigDecimal.valueOf(100.0), RewardDef.withAttribute(1)),
                new Threshold(BigDecimal.valueOf(300.0), RewardDef.withAttribute(3)),
                new Threshold(BigDecimal.valueOf(200.0), RewardDef.withAttribute(2))
        ));

        AbstractRule abstractRule = parser.convert(def);
        assertNotNull(abstractRule);
        assertTrue(abstractRule instanceof PeriodicOccurrencesRule);

        PeriodicOccurrencesRule rule = (PeriodicOccurrencesRule) abstractRule;
        assertEquals(1234, rule.getTimeUnit());
        assertNotNull(rule.getValueResolver());
        assertNotNull(rule.getCriteria());
        assertEquals(def.getSpec().getThresholds().size(), rule.getThresholds().size());
        assertEquals(BigDecimal.ONE, rule.getValueResolver().resolve(new EventJson(), new ExecutionContext()));

        // thresholds must be in reverse order
        assertEquals(new BigDecimal("100.0"), rule.getThresholds().stream()
                .map(PeriodicBadgeRule.Threshold::getValue)
                .reduce((val1, val2) -> {
                    System.out.println(val1 + ", " + val2);
                    assertTrue(val1.compareTo(val2) > 0);
                    return val2;
                }).orElseThrow());
    }


    private BadgeDef createBase() {
        BadgeDef def = new BadgeDef();
        def.setId("BADGE00001");
        def.setName("badge-1");
        def.setType("core:badge");
        BadgeSpecification spec = new BadgeSpecification();
        spec.setSelector(SelectorDef.singleEvent("event.a"));
        def.setSpec(spec);
        return def;
    }

    private BadgeDef createFirstEvent() {
        BadgeDef def = new BadgeDef();
        def.setId("BADGE00002");
        def.setName("badge-1");
        def.setType("core:badge");
        BadgeSpecification spec = new BadgeSpecification();
        spec.setKind(BadgeDef.FIRST_EVENT_KIND);
        SelectorDef selectorDef = SelectorDef.singleEvent("event.a");
        spec.setSelector(selectorDef);
        spec.setRewards(RewardDef.withAttribute(1));
        def.setSpec(spec);
        return def;
    }

    private BadgeDef createStreak() {
        BadgeDef def = new BadgeDef();
        def.setId("BADGE00003");
        def.setName("badge-2");
        def.setType("core:badge");
        BadgeSpecification spec = new BadgeSpecification();
        spec.setKind(BadgeDef.STREAK_N_KIND);
        spec.setSelector(SelectorDef.singleEvent("event.a"));
        spec.getSelector().setFilter(EventFilterDef.withExpression("e.data.value > 100"));
        spec.setConsecutive(true);
        spec.setStreaks(List.of(
                new Streak(3, RewardDef.withAttribute(1)),
                new Streak(5, RewardDef.withAttribute(2)),
                new Streak(10, RewardDef.withAttribute(3))
        ));
        def.setSpec(spec);
        return def;
    }

    private Map<String, Object> toMap(AbstractDef<? extends BaseSpecification> def) {
        Yaml yaml = new Yaml();
        System.out.println(yaml.dumpAsMap(def));
        return yaml.load(yaml.dumpAsMap(def));
    }
}