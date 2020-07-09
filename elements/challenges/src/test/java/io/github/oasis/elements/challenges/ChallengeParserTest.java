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

package io.github.oasis.elements.challenges;

import io.github.oasis.core.EventJson;
import io.github.oasis.core.elements.AbstractDef;
import io.github.oasis.core.elements.AbstractRule;
import io.github.oasis.core.external.messages.PersistedDef;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;

/**
 * @author Isuru Weerarathna
 */
class ChallengeParserTest {

    private ChallengeParser parser;

    @BeforeEach
    void beforeEach() {
        parser = new ChallengeParser();
    }

    @Test
    void parse() {
        ChallengeDef def = createTestChallenge();

        PersistedDef persistedDef = new PersistedDef();
        persistedDef.setType(PersistedDef.GAME_RULE_ADDED);
        persistedDef.setImpl(ChallengeDef.class.getName());
        persistedDef.setData(toMap(def));

        AbstractDef abstractDef = parser.parse(persistedDef);

        Assertions.assertTrue(abstractDef instanceof ChallengeDef);
        ChallengeDef parsed = (ChallengeDef) abstractDef;
        Assertions.assertEquals(def.getStartAt(), parsed.getStartAt());
        Assertions.assertEquals(def.getExpireAt(), parsed.getExpireAt());
        Assertions.assertEquals(def.getWinnerCount(), parsed.getWinnerCount());
        Assertions.assertEquals(def.getPointId(), parsed.getPointId());
        Assertions.assertEquals(def.getPointAwards(), parsed.getPointAwards());
        Assertions.assertEquals(def.getCriteria(), parsed.getCriteria());
        Assertions.assertEquals(ChallengeRule.ChallengeScope.TEAM.name(), parsed.getScope().get(Constants.DEF_SCOPE_TYPE));
        Assertions.assertEquals(1000, parsed.getScope().get(Constants.DEF_SCOPE_ID));
    }

    @Test
    void convert() {
        ChallengeDef def = createTestChallenge();

        AbstractRule abstractRule = parser.convert(def);
        Assertions.assertTrue(abstractRule instanceof ChallengeRule);

        ChallengeRule rule = (ChallengeRule) abstractRule;
        Assertions.assertEquals(def.getWinnerCount(), rule.getWinnerCount());
        Assertions.assertEquals(ChallengeRule.ChallengeScope.TEAM, rule.getScope());
        Assertions.assertEquals(1000L, rule.getScopeId());
        Assertions.assertEquals(def.getPointId(), rule.getPointId());
        Assertions.assertEquals(def.getExpireAt(), rule.getExpireAt());
        Assertions.assertEquals(def.getStartAt(), rule.getStartAt());
        Assertions.assertEquals(BigDecimal.ZERO, rule.getAwardPoints());
        Assertions.assertNotNull(rule.getCustomAwardPoints());

        EventJson eventJson = new EventJson();
        Assertions.assertEquals(new BigDecimal("5000.0"), rule.getCustomAwardPoints().resolve(eventJson, 1, rule));
        Assertions.assertEquals(new BigDecimal("3100.0"), rule.getCustomAwardPoints().resolve(eventJson, 20, rule));
        Assertions.assertEquals(new BigDecimal("100.0"), rule.getCustomAwardPoints().resolve(eventJson, 50, rule));
    }

    @Test
    void convertNullScope() {
        ChallengeDef def = createTestChallenge();
        def.setScope(null);

        AbstractRule abstractRule = parser.convert(def);
        Assertions.assertTrue(abstractRule instanceof ChallengeRule);

        ChallengeRule rule = (ChallengeRule) abstractRule;
        Assertions.assertEquals(ChallengeRule.ChallengeScope.GAME, rule.getScope());
        Assertions.assertEquals(0, rule.getScopeId());
    }

    @Test
    void convertConstAward() {
        ChallengeDef def = createTestChallenge();
        def.setPointAwards(200.0);

        AbstractRule abstractRule = parser.convert(def);
        Assertions.assertTrue(abstractRule instanceof ChallengeRule);

        ChallengeRule rule = (ChallengeRule) abstractRule;
        Assertions.assertEquals(new BigDecimal("200.0"), rule.getAwardPoints());
        Assertions.assertNull(rule.getCustomAwardPoints());
    }

    @Test
    void convertNoAward() {
        ChallengeDef def = createTestChallenge();
        def.setPointAwards(null);

        AbstractRule abstractRule = parser.convert(def);
        Assertions.assertTrue(abstractRule instanceof ChallengeRule);

        ChallengeRule rule = (ChallengeRule) abstractRule;
        Assertions.assertEquals(BigDecimal.ZERO, rule.getAwardPoints());
        Assertions.assertNull(rule.getCustomAwardPoints());
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

    private ChallengeDef createTestChallenge() {
        ChallengeDef def = new ChallengeDef();
        def.setId("CHAL00003");
        def.setName("challenge-1");
        def.setStartAt(System.currentTimeMillis());
        def.setExpireAt(LocalDate.of(2020, 12, 31).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli());
        def.setWinnerCount(20);
        def.setPointId("challenge-winner-points");
        def.setCriteria("e.data.reputations > 200");
        def.setScope(Map.of(Constants.DEF_SCOPE_TYPE, ChallengeRule.ChallengeScope.TEAM.toString(),
                Constants.DEF_SCOPE_ID, 1000L));
        def.setPointAwards("100 * (50 - rank + 1)");

        return def;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> toMap(AbstractDef def) {
        Yaml yaml = new Yaml();
        System.out.println(yaml.dumpAsMap(def));
        return (Map<String, Object>) yaml.load(yaml.dumpAsMap(def));
    }
}