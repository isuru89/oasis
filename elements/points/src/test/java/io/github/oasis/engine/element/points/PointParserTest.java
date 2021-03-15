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

package io.github.oasis.engine.element.points;

import io.github.oasis.core.elements.AbstractDef;
import io.github.oasis.core.elements.AbstractRule;
import io.github.oasis.core.elements.EventExecutionFilterFactory;
import io.github.oasis.core.elements.matchers.AnnualDateRangeMatcher;
import io.github.oasis.core.elements.matchers.ScriptedTimeMatcher;
import io.github.oasis.core.elements.matchers.SingleEventTypeMatcher;
import io.github.oasis.core.elements.spec.AcceptsWithinDef;
import io.github.oasis.core.elements.spec.SelectorDef;
import io.github.oasis.core.elements.spec.TimeRangeDef;
import io.github.oasis.core.external.messages.PersistedDef;
import io.github.oasis.engine.element.points.spec.CappedDef;
import io.github.oasis.engine.element.points.spec.PointRewardDef;
import io.github.oasis.engine.element.points.spec.PointSpecification;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Isuru Weerarathna
 */
class PointParserTest {

    private static final Yaml YAML = new Yaml();

    private PointParser parser;

    @BeforeEach
    void beforeEach() {
        parser = new PointParser();
    }

    @Test
    void parse() {
        PointDef pointDef = new PointDef();
        pointDef.setId("POINT00001");
        pointDef.setName("point-1");
        pointDef.setType("core:point");
        PointSpecification spec = new PointSpecification();
        spec.setReward(new PointRewardDef("point-1", BigDecimal.valueOf(100.0)));
        SelectorDef selectorDef = new SelectorDef();
        selectorDef.setMatchEvent("event.a");
        spec.setSelector(selectorDef);
        pointDef.setSpec(spec);

        PersistedDef def = new PersistedDef();
        def.setType(PersistedDef.GAME_RULE_ADDED);
        def.setImpl(PointDef.class.getName());
        def.setData(toMap(pointDef));

        PointDef parsedDef = parser.parse(def);

        assertEquals(pointDef.getId(), parsedDef.getId());
        assertEquals(pointDef.getName(), parsedDef.getName());
        assertEquals(pointDef.getDescription(), parsedDef.getDescription());
        assertEquals(pointDef.getSpec().getSelector().getMatchEvent(), parsedDef.getSpec().getSelector().getMatchEvent());
        assertEquals(pointDef.getSpec().getReward().getAmount(), parsedDef.getSpec().getReward().getAmount());
    }

    @Test
    void testUnknownDef() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> parser.convert(null));
    }

    @Test
    void convertConstAward() {
        PointDef pointDef = new PointDef();
        pointDef.setId("POINT000002");
        pointDef.setName("point-1");
        pointDef.setType("core:point");
        PointSpecification spec = new PointSpecification();
        spec.setReward(new PointRewardDef("point-1", BigDecimal.valueOf(10.0)));
        SelectorDef selectorDef = new SelectorDef();
        selectorDef.setMatchEvent("event.a");
        spec.setSelector(selectorDef);
        pointDef.setSpec(spec);

        AbstractRule abstractRule = parser.convert(pointDef);

        assertTrue(abstractRule instanceof PointRule);
        PointRule rule = (PointRule) abstractRule;

        assertNotNull(rule.getId());
        assertTrue(rule.getId().length() > 0);
        assertEquals(pointDef.getName(), rule.getName());
        assertEquals(pointDef.getName(), rule.getPointId());
        assertFalse(rule.isAwardBasedOnEvent());
        assertEquals(new BigDecimal("10.0"), rule.getAmountToAward());
        assertEquals(EventExecutionFilterFactory.ALWAYS_TRUE, rule.getCriteria());
        assertTrue(rule.getEventTypeMatcher() instanceof SingleEventTypeMatcher);
    }

    @Test
    void convertDynamicAward() {
        PointDef pointDef = new PointDef();
        pointDef.setId("POINT00003");
        pointDef.setName("point-1");
        pointDef.setType("core:point");
        PointSpecification spec = new PointSpecification();
        spec.setReward(new PointRewardDef("point-1", "e.data.votes + 100"));
        SelectorDef selectorDef = new SelectorDef();
        selectorDef.setMatchEvent("event.a");
        spec.setSelector(selectorDef);
        pointDef.setSpec(spec);

        AbstractRule abstractRule = parser.convert(pointDef);

        assertTrue(abstractRule instanceof PointRule);
        PointRule rule = (PointRule) abstractRule;

        assertNotNull(rule.getId());
        assertEquals(pointDef.getName(), rule.getName());
        assertEquals(EventExecutionFilterFactory.ALWAYS_TRUE, rule.getCriteria());
        assertTrue(rule.getEventTypeMatcher() instanceof SingleEventTypeMatcher);
        assertTrue(rule.isAwardBasedOnEvent());
        assertNotNull(rule.getAmountExpression());
    }

    @Test
    void convertCustomPointId() {
        PointDef pointDef = new PointDef();
        pointDef.setId("POINT00004");
        pointDef.setName("point-1");
        pointDef.setType("core:point");
        PointSpecification spec = new PointSpecification();
        spec.setReward(new PointRewardDef("customPointId", "e.data.votes + 100"));
        SelectorDef selectorDef = new SelectorDef();
        selectorDef.setMatchEvent("event.a");
        spec.setSelector(selectorDef);
        pointDef.setSpec(spec);

        AbstractRule abstractRule = parser.convert(pointDef);

        assertTrue(abstractRule instanceof PointRule);
        PointRule rule = (PointRule) abstractRule;

        assertNotNull(rule.getId());
        assertEquals(pointDef.getName(), rule.getName());
        assertEquals(pointDef.getSpec().getReward().getPointId(), rule.getPointId());
        assertEquals(EventExecutionFilterFactory.ALWAYS_TRUE, rule.getCriteria());
        assertTrue(rule.getEventTypeMatcher() instanceof SingleEventTypeMatcher);
        assertTrue(rule.isAwardBasedOnEvent());
        assertNotNull(rule.getAmountExpression());
        assertNull(rule.getTimeRangeMatcher());
    }

    @Test
    void convertWithTimeRanges() {
        PointDef pointDef = new PointDef();
        pointDef.setId("POINT000005");
        pointDef.setName("point-1");
        pointDef.setType("core:point");
        PointSpecification spec = new PointSpecification();
        spec.setReward(new PointRewardDef("customPointId", "e.data.votes + 100"));
        SelectorDef selectorDef = new SelectorDef();
        selectorDef.setMatchEvent("event.a");
        selectorDef.setAcceptsWithin(new AcceptsWithinDef());
        selectorDef.getAcceptsWithin().setAnyOf(List.of(new TimeRangeDef(AbstractDef.TIME_RANGE_TYPE_SEASONAL, "05-01", "05-31")));
        spec.setSelector(selectorDef);
        pointDef.setSpec(spec);

        AbstractRule abstractRule = parser.convert(pointDef);

        assertTrue(abstractRule instanceof PointRule);
        PointRule rule = (PointRule) abstractRule;

        assertNotNull(rule.getId());
        assertEquals(pointDef.getName(), rule.getName());
        assertEquals(pointDef.getSpec().getReward().getPointId(), rule.getPointId());
        assertEquals(EventExecutionFilterFactory.ALWAYS_TRUE, rule.getCriteria());
        assertTrue(rule.getEventTypeMatcher() instanceof SingleEventTypeMatcher);
        assertTrue(rule.isAwardBasedOnEvent());
        assertNotNull(rule.getAmountExpression());
        assertNotNull(rule.getTimeRangeMatcher());
        assertTrue(rule.getTimeRangeMatcher() instanceof AnnualDateRangeMatcher);
        assertNull(rule.getCapDuration());
        assertNull(rule.getCapLimit());
    }

    @Test
    void convertWithCappedPoints() {
        PointDef pointDef = new PointDef();
        pointDef.setId("POINT000007");
        pointDef.setName("point-1");
        pointDef.setType("core:point");
        PointSpecification spec = new PointSpecification();
        spec.setCap(new CappedDef("daily", BigDecimal.valueOf(200)));
        spec.setReward(new PointRewardDef("customPointId", "e.data.votes + 100"));
        SelectorDef selectorDef = new SelectorDef();
        selectorDef.setMatchEvent("event.a");
        spec.setSelector(selectorDef);
        pointDef.setSpec(spec);

        AbstractRule abstractRule = parser.convert(pointDef);

        assertTrue(abstractRule instanceof PointRule);
        PointRule rule = (PointRule) abstractRule;

        assertNotNull(rule.getId());
        assertEquals(pointDef.getName(), rule.getName());
        assertEquals(pointDef.getSpec().getReward().getPointId(), rule.getPointId());
        assertEquals(EventExecutionFilterFactory.ALWAYS_TRUE, rule.getCriteria());
        assertTrue(rule.getEventTypeMatcher() instanceof SingleEventTypeMatcher);
        assertTrue(rule.isAwardBasedOnEvent());
        assertNotNull(rule.getAmountExpression());
        assertNull(rule.getTimeRangeMatcher());
        assertEquals("daily", rule.getCapDuration());
        assertEquals(BigDecimal.valueOf(200), rule.getCapLimit());
    }

    @Test
    void convertFromFile() {
        List<PointDef> pointDefs = ParserTest.parseAll("points.yml", parser);
        PointDef pointDef = ParserTest.findByName(pointDefs, "Monthly-Last-Sale").orElseThrow();
        PointRule rule = (PointRule) parser.convert(pointDef);

        assertNotNull(rule.getTimeRangeMatcher());
        assertTrue(rule.getTimeRangeMatcher() instanceof ScriptedTimeMatcher);
        assertTrue(rule.getTimeRangeMatcher().isBetween(1595588400000L, "UTC"));
        assertFalse(rule.getTimeRangeMatcher().isBetween(1595502000000L, "UTC"));
    }

    private Map<String, Object> toMap(PointDef def) {
        System.out.println(YAML.dumpAsMap(def));
        return YAML.load(YAML.dumpAsMap(def));
    }
}