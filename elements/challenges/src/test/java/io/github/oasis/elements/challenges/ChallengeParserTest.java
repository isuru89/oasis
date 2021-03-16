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
import io.github.oasis.core.elements.spec.BaseSpecification;
import io.github.oasis.core.elements.spec.PointAwardDef;
import io.github.oasis.core.elements.spec.SelectorDef;
import io.github.oasis.core.external.messages.EngineMessage;
import io.github.oasis.elements.challenges.spec.ChallengeRewardDef;
import io.github.oasis.elements.challenges.spec.ChallengeSpecification;
import io.github.oasis.elements.challenges.spec.ScopeDef;
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

        EngineMessage engineMessage = new EngineMessage();
        engineMessage.setType(EngineMessage.GAME_RULE_ADDED);
        engineMessage.setImpl(ChallengeDef.class.getName());
        engineMessage.setData(toMap(def));

        AbstractDef<? extends BaseSpecification> abstractDef = parser.parse(engineMessage);

        Assertions.assertTrue(abstractDef instanceof ChallengeDef);
        ChallengeDef parsed = (ChallengeDef) abstractDef;
        Assertions.assertEquals(def.getSpec().getStartAt(), parsed.getSpec().getStartAt());
        Assertions.assertEquals(def.getSpec().getExpireAt(), parsed.getSpec().getExpireAt());
        Assertions.assertEquals(def.getSpec().getWinnerCount(), parsed.getSpec().getWinnerCount());
        Assertions.assertEquals(def.getSpec().getRewards().getPoints().getId(), parsed.getSpec().getRewards().getPoints().getId());
        Assertions.assertEquals(def.getSpec().getRewards().getPoints().getExpression(), parsed.getSpec().getRewards().getPoints().getExpression());
        Assertions.assertEquals(ChallengeRule.ChallengeScope.TEAM.name(), parsed.getSpec().getScopeTo().getType());
        Assertions.assertEquals(1000, parsed.getSpec().getScopeTo().getTargetId());
    }

    @Test
    void convert() {
        ChallengeDef def = createTestChallenge();

        AbstractRule abstractRule = parser.convert(def);
        Assertions.assertTrue(abstractRule instanceof ChallengeRule);

        ChallengeRule rule = (ChallengeRule) abstractRule;
        Assertions.assertEquals(def.getSpec().getWinnerCount(), rule.getWinnerCount());
        Assertions.assertEquals(ChallengeRule.ChallengeScope.TEAM, rule.getScope());
        Assertions.assertEquals(1000L, rule.getScopeId());
        Assertions.assertEquals(def.getSpec().getRewards().getPoints().getId(), rule.getPointId());
        Assertions.assertEquals(def.getSpec().getExpireAt(), rule.getExpireAt());
        Assertions.assertEquals(def.getSpec().getStartAt(), rule.getStartAt());
        Assertions.assertEquals(BigDecimal.ZERO, rule.getAwardPoints());
        Assertions.assertNotNull(rule.getCustomAwardPoints());

        EventJson eventJson = new EventJson();
        Assertions.assertEquals(new BigDecimal("5000.0"), rule.getCustomAwardPoints().resolve(eventJson, 1, rule));
        Assertions.assertEquals(new BigDecimal("3100.0"), rule.getCustomAwardPoints().resolve(eventJson, 20, rule));
        Assertions.assertEquals(new BigDecimal("100.0"), rule.getCustomAwardPoints().resolve(eventJson, 50, rule));
    }

    @Test
    void convertConstAward() {
        ChallengeDef def = createTestChallenge();
        def.getSpec().getRewards().getPoints().setAmount(BigDecimal.valueOf(200.0));
        def.getSpec().getRewards().getPoints().setExpression(null);

        AbstractRule abstractRule = parser.convert(def);
        Assertions.assertTrue(abstractRule instanceof ChallengeRule);

        ChallengeRule rule = (ChallengeRule) abstractRule;
        Assertions.assertEquals(new BigDecimal("200.0"), rule.getAwardPoints());
        Assertions.assertNull(rule.getCustomAwardPoints());
    }

    @Test
    void unknownDef() {
        AbstractDef unknownDef = new AbstractDef() {
        };

        Assertions.assertThrows(IllegalArgumentException.class, () -> parser.convert(unknownDef));
    }

    private ChallengeDef createTestChallenge() {
        ChallengeDef def = new ChallengeDef();
        def.setId("CHAL00003");
        def.setName("challenge-1");
        def.setType("core:challenge");
        ChallengeSpecification spec = new ChallengeSpecification();
        def.setSpec(spec);

        spec.setSelector(new SelectorDef());
        spec.getSelector().setMatchEvent("event.a");
        spec.setStartAt(System.currentTimeMillis());
        spec.setExpireAt(LocalDate.of(2020, 12, 31).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli());
        spec.setWinnerCount(20);
        spec.setRewards(new ChallengeRewardDef());
        spec.getRewards().setPoints(new PointAwardDef());
        spec.getRewards().getPoints().setId("challenge-winner-points");
        spec.getRewards().getPoints().setExpression("100 * (50 - rank + 1)");
        spec.setScopeTo(new ScopeDef());
        spec.getScopeTo().setType(ChallengeRule.ChallengeScope.TEAM.name());
        spec.getScopeTo().setTargetId(1000L);

        return def;
    }

    private Map<String, Object> toMap(AbstractDef<? extends BaseSpecification> def) {
        Yaml yaml = new Yaml();
        System.out.println(yaml.dumpAsMap(def));
        return yaml.load(yaml.dumpAsMap(def));
    }
}