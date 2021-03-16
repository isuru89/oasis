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

package io.github.oasis.elements.milestones;

import io.github.oasis.core.elements.AbstractDef;
import io.github.oasis.core.elements.matchers.AnyOfEventTypeMatcher;
import io.github.oasis.core.elements.matchers.SingleEventTypeMatcher;
import io.github.oasis.core.elements.spec.BaseSpecification;
import io.github.oasis.core.elements.spec.MatchEventsDef;
import io.github.oasis.core.elements.spec.SelectorDef;
import io.github.oasis.core.events.BasePointEvent;
import io.github.oasis.core.external.messages.EngineMessage;
import io.github.oasis.elements.milestones.spec.MilestoneLevel;
import io.github.oasis.elements.milestones.spec.MilestoneSpecification;
import io.github.oasis.elements.milestones.spec.ValueExtractorDef;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.github.oasis.elements.milestones.Utils.findByName;
import static io.github.oasis.elements.milestones.Utils.isNonEmptyString;
import static io.github.oasis.elements.milestones.Utils.isNumber;
import static io.github.oasis.elements.milestones.Utils.parseAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Isuru Weerarathna
 */
class MilestoneParserTest {

    private MilestoneParser parser;

    @BeforeEach
    void setUp() {
        parser = new MilestoneParser();
    }

    @Test
    void parse() {
        MilestoneDef def = createMilestone();

        EngineMessage engineMessage = new EngineMessage();
        engineMessage.setType(EngineMessage.GAME_RULE_ADDED);
        engineMessage.setImpl(MilestoneDef.class.getName());
        engineMessage.setData(toMap(def));

        MilestoneDef parsed = parser.parse(engineMessage);

        assertEquals(def.getSpec().getValueExtractor(), parsed.getSpec().getValueExtractor());
        assertEquals(def.getSpec().getLevels().size(), parsed.getSpec().getLevels().size());
    }

    @Test
    void testParsing() {
        List<MilestoneDef> defs = parseAll("milestones.yml", parser);
        findByName(defs, "Challenge-Win-Points").ifPresent(def -> {
            assertTrue(isNonEmptyString(def.getDescription()));
            assertNotNull(def.getSpec().getSelector().getMatchEvents());
            assertNull(def.getSpec().getSelector().getMatchEvent());
            assertNotNull(def.getSpec().getValueExtractor());
            assertEquals(2, def.getSpec().getLevels().size());

            MilestoneRule rule = parser.convert(def);
            assertNotNull(rule.getValueExtractor());
            assertNotNull(rule.getEventTypeMatcher());
            assertTrue(rule.getEventTypeMatcher() instanceof AnyOfEventTypeMatcher);
            assertEquals(1, rule.getLevelFor(BigDecimal.valueOf(51)).orElseThrow().getLevel());
            assertEquals(2, rule.getLevelFor(BigDecimal.valueOf(100)).orElseThrow().getLevel());
        });
        findByName(defs, "Star-Points").ifPresent(def -> {
            assertTrue(isNonEmptyString(def.getDescription()));
            assertNull(def.getSpec().getSelector().getMatchEvent());
            assertNull(def.getSpec().getSelector().getMatchEvents());
            assertNotNull(def.getSpec().getSelector().getMatchPointIds());
            assertNotNull(def.getSpec().getLevels());

            TEvent event = TEvent.createKeyValue(Instant.now().toEpochMilli(), "event.a", 55);
            BasePointEvent basePointEvent = new BasePointEvent("star.points", BasePointEvent.DEFAULT_POINTS_KEY, BigDecimal.valueOf(20), event) {
                @Override
                public String getPointStoredKey() {
                    return super.getPointStoredKey();
                }
            };
            MilestoneRule rule = parser.convert(def);
            assertNotNull(rule.getValueExtractor());
            assertNotNull(rule.getEventTypeMatcher());
            assertEquals(3, rule.getLevels().size());
            assertTrue(rule.getEventTypeMatcher() instanceof AnyOfEventTypeMatcher);
            assertEquals(BigDecimal.valueOf(20), rule.getValueExtractor().resolve(basePointEvent, rule, null));
            assertEquals(BigDecimal.ZERO, rule.getValueExtractor().resolve(event, rule, null));
            assertTrue(rule.getEventTypeMatcher().matches("star.points"));
            assertTrue(rule.getEventTypeMatcher().matches("coupan.points"));
            assertFalse(rule.getEventTypeMatcher().matches("unknown.points"));
        });
        findByName(defs, "Total-Reputations").ifPresent(def -> {
            assertTrue(isNonEmptyString(def.getDescription()));
            assertNull(def.getSpec().getSelector().getMatchEvent());
            assertNull(def.getSpec().getSelector().getMatchEvents());
            assertNotNull(def.getSpec().getSelector().getMatchPointIds());
            assertNotNull(def.getSpec().getLevels());

            MilestoneRule rule = parser.convert(def);
            assertNotNull(rule.getValueExtractor());
            assertNotNull(rule.getEventTypeMatcher());
            assertEquals(5, rule.getLevels().size());
            assertTrue(rule.getEventTypeMatcher() instanceof SingleEventTypeMatcher);
            assertTrue(rule.getEventTypeMatcher().matches("stackoverflow.reputation"));
            assertFalse(rule.getEventTypeMatcher().matches("unknown.reputation"));
        });
        findByName(defs, "Milestone-with-Event-Count").ifPresent(def -> {
            assertTrue(isNonEmptyString(def.getDescription()));
            assertNotNull(def.getSpec().getSelector().getMatchEvent());
            assertNull(def.getSpec().getSelector().getMatchEvents());
            assertNull(def.getSpec().getSelector().getMatchPointIds());
            assertNotNull(def.getSpec().getLevels());
            assertNotNull(def.getSpec().getSelector().getFilter());
            assertTrue(isNumber(def.getSpec().getValueExtractor().getAmount()));

            MilestoneRule rule = parser.convert(def);
            assertNotNull(rule.getValueExtractor());
            assertNotNull(rule.getEventTypeMatcher());
            assertEquals(3, rule.getLevels().size());
            assertTrue(rule.getEventTypeMatcher() instanceof SingleEventTypeMatcher);
            assertTrue(rule.getEventTypeMatcher().matches("stackoverflow.question.answered"));
            assertFalse(rule.getEventTypeMatcher().matches("unknown.reputation"));
            assertEquals(BigDecimal.ONE, rule.getValueExtractor().resolve(null, rule, null));
            assertNotNull(rule.getEventFilter());
        });
    }

    @Test
    void convert() {
        MilestoneDef def = createMilestone();

        MilestoneRule rule = parser.convert(def);

        {
            Optional<MilestoneRule.Level> levelFor = rule.getLevelFor(new BigDecimal("100.0"));
            assertTrue(levelFor.isPresent());
            assertEquals(1, levelFor.get().getLevel());
        }
        {
            Optional<MilestoneRule.Level> levelFor = rule.getLevelFor(new BigDecimal("45100.0"));
            assertTrue(levelFor.isPresent());
            assertEquals(4, levelFor.get().getLevel());
        }
        {
            Optional<MilestoneRule.Level> levelFor = rule.getLevelFor(new BigDecimal("0.0"));
            assertTrue(levelFor.isEmpty());
        }

        {
            Optional<MilestoneRule.Level> nextLevel = rule.getNextLevel(new BigDecimal("100.0"));
            assertTrue(nextLevel.isPresent());
            assertEquals(2, nextLevel.get().getLevel());
        }
        {
            Optional<MilestoneRule.Level> nextLevel = rule.getNextLevel(new BigDecimal("123100.0"));
            assertTrue(nextLevel.isEmpty());
        }
        {
            Optional<MilestoneRule.Level> nextLevel = rule.getNextLevel(new BigDecimal("0.0"));
            assertTrue(nextLevel.isPresent());
            assertEquals(1, nextLevel.get().getLevel());
        }
    }

    private MilestoneDef createMilestone() {
        MilestoneDef def = new MilestoneDef();
        def.setId("MILE00001");
        def.setName("milestone-1");
        def.setType("core:milestone");
        SelectorDef selectorDef = new SelectorDef();
        MatchEventsDef eventsDef = new MatchEventsDef();
        eventsDef.setAnyOf(Arrays.asList("point.a", "point.b"));
        selectorDef.setMatchPointIds(eventsDef);
        MilestoneSpecification spec = new MilestoneSpecification();
        spec.setSelector(selectorDef);

        ValueExtractorDef valueExtractorDef = new ValueExtractorDef();
        valueExtractorDef.setExpression("e.data.value");
        spec.setValueExtractor(valueExtractorDef);
        spec.setLevels(List.of(
            aLevel(1, 100),
            aLevel(2, 200),
            aLevel(3, 500),
            aLevel(4, 1000)
        ));

        def.setSpec(spec);
        return def;
    }

    private MilestoneLevel aLevel(int levelId, double milestone) {
        MilestoneLevel level = new MilestoneLevel();
        level.setLevel(levelId);
        level.setMilestone(BigDecimal.valueOf(milestone));
        return level;
    }

    private Map<String, Object> toMap(AbstractDef<? extends BaseSpecification> def) {
        Yaml yaml = new Yaml();
        System.out.println(yaml.dumpAsMap(def));
        return yaml.load(yaml.dumpAsMap(def));
    }

}