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
import io.github.oasis.core.elements.AbstractRule;
import io.github.oasis.core.elements.matchers.AnyOfEventTypeMatcher;
import io.github.oasis.core.elements.matchers.SingleEventTypeMatcher;
import io.github.oasis.core.events.BasePointEvent;
import io.github.oasis.core.external.messages.PersistedDef;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.github.oasis.elements.milestones.Utils.findByName;
import static io.github.oasis.elements.milestones.Utils.isNonEmptyString;
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

        PersistedDef persistedDef = new PersistedDef();
        persistedDef.setType(PersistedDef.GAME_RULE_ADDED);
        persistedDef.setImpl(MilestoneDef.class.getName());
        persistedDef.setData(toMap(def));

        AbstractDef abstractDef = parser.parse(persistedDef);

        assertTrue(abstractDef instanceof MilestoneDef);
        MilestoneDef parsed = (MilestoneDef) abstractDef;
        assertEquals(def.getValueExtractor(), parsed.getValueExtractor());
        assertEquals(def.getLevels().size(), parsed.getLevels().size());
    }

    @Test
    void testParsing() {
        List<MilestoneDef> defs = parseAll("milestones.yml", parser);
        findByName(defs, "Challenge-Win-Points").ifPresent(def -> {
            assertTrue(isNonEmptyString(def.getDescription()));
            assertNotNull(def.getEvents());
            assertNull(def.getEvent());
            assertNotNull(def.getValueExtractor());
            assertEquals(2, def.getLevels().size());

            MilestoneRule rule = parser.convert(def);
            assertNotNull(rule.getValueExtractor());
            assertNotNull(rule.getEventTypeMatcher());
            assertTrue(rule.getEventTypeMatcher() instanceof AnyOfEventTypeMatcher);
            assertEquals(1, rule.getLevelFor(BigDecimal.valueOf(51)).orElseThrow().getLevel());
            assertEquals(2, rule.getLevelFor(BigDecimal.valueOf(100)).orElseThrow().getLevel());
        });
        findByName(defs, "Star-Points").ifPresent(def -> {
            assertTrue(isNonEmptyString(def.getDescription()));
            assertNull(def.getEvent());
            assertNull(def.getEvents());
            assertNotNull(def.getPointIds());
            assertNotNull(def.getLevels());

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
            assertNull(def.getEvent());
            assertNull(def.getEvents());
            assertNotNull(def.getPointIds());
            assertNotNull(def.getLevels());

            MilestoneRule rule = parser.convert(def);
            assertNotNull(rule.getValueExtractor());
            assertNotNull(rule.getEventTypeMatcher());
            assertEquals(5, rule.getLevels().size());
            assertTrue(rule.getEventTypeMatcher() instanceof SingleEventTypeMatcher);
            assertTrue(rule.getEventTypeMatcher().matches("stackoverflow.reputation"));
            assertFalse(rule.getEventTypeMatcher().matches("unknown.reputation"));
        });
    }

    @Test
    void convert() {
        MilestoneDef def = createMilestone();

        AbstractRule abstractRule = parser.convert(def);
        assertTrue(abstractRule instanceof MilestoneRule);

        MilestoneRule rule = (MilestoneRule) abstractRule;
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

    @Test
    void unknownDef() {
        AbstractDef unknownDef = new AbstractDef() {
            @Override
            public Object getEvent() {
                return super.getEvent();
            }
        };

        Assertions.assertThrows(IllegalArgumentException.class, () -> parser.convert(unknownDef));
    }

    private MilestoneDef createMilestone() {
        MilestoneDef def = new MilestoneDef();
        def.setId(1);
        def.setName("milestone-1");
        def.setValueExtractor("e.data.value");
        def.setLevels(List.of(
            aLevel(1, 100),
            aLevel(2, 200),
            aLevel(3, 500),
            aLevel(4, 1000)
        ));
        return def;
    }

    private MilestoneDef.MilestoneLevel aLevel(int levelId, double milestone) {
        MilestoneDef.MilestoneLevel level = new MilestoneDef.MilestoneLevel();
        level.setLevel(levelId);
        level.setMilestone(BigDecimal.valueOf(milestone));
        return level;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> toMap(AbstractDef def) {
        Yaml yaml = new Yaml();
        System.out.println(yaml.dumpAsMap(def));
        return (Map<String, Object>) yaml.load(yaml.dumpAsMap(def));
    }

}