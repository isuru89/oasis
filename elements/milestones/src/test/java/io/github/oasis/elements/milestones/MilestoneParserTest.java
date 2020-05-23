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
import io.github.oasis.core.external.messages.PersistedDef;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

        Assertions.assertTrue(abstractDef instanceof MilestoneDef);
        MilestoneDef parsed = (MilestoneDef) abstractDef;
        Assertions.assertEquals(def.getValueExtractor(), parsed.getValueExtractor());
        Assertions.assertEquals(def.getLevels().size(), parsed.getLevels().size());
    }

    @Test
    void convert() {
        MilestoneDef def = createMilestone();

        AbstractRule abstractRule = parser.convert(def);
        Assertions.assertTrue(abstractRule instanceof MilestoneRule);

        MilestoneRule rule = (MilestoneRule) abstractRule;
        {
            Optional<MilestoneRule.Level> levelFor = rule.getLevelFor(new BigDecimal("100.0"));
            Assertions.assertTrue(levelFor.isPresent());
            Assertions.assertEquals(1, levelFor.get().getLevel());
        }
        {
            Optional<MilestoneRule.Level> levelFor = rule.getLevelFor(new BigDecimal("45100.0"));
            Assertions.assertTrue(levelFor.isPresent());
            Assertions.assertEquals(4, levelFor.get().getLevel());
        }
        {
            Optional<MilestoneRule.Level> levelFor = rule.getLevelFor(new BigDecimal("0.0"));
            Assertions.assertTrue(levelFor.isEmpty());
        }

        {
            Optional<MilestoneRule.Level> nextLevel = rule.getNextLevel(new BigDecimal("100.0"));
            Assertions.assertTrue(nextLevel.isPresent());
            Assertions.assertEquals(2, nextLevel.get().getLevel());
        }
        {
            Optional<MilestoneRule.Level> nextLevel = rule.getNextLevel(new BigDecimal("123100.0"));
            Assertions.assertTrue(nextLevel.isEmpty());
        }
        {
            Optional<MilestoneRule.Level> nextLevel = rule.getNextLevel(new BigDecimal("0.0"));
            Assertions.assertTrue(nextLevel.isPresent());
            Assertions.assertEquals(1, nextLevel.get().getLevel());
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