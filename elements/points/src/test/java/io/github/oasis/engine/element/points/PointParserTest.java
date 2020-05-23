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
import io.github.oasis.core.elements.matchers.SingleEventTypeMatcher;
import io.github.oasis.core.external.messages.PersistedDef;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.math.BigDecimal;
import java.util.Map;

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
        pointDef.setId(1);
        pointDef.setName("point-1");
        pointDef.setAward(10.0);
        pointDef.setEvent("event.a");

        PersistedDef def = new PersistedDef();
        def.setType(PersistedDef.GAME_RULE_ADDED);
        def.setImpl(PointDef.class.getName());
        def.setData(toMap(pointDef));

        AbstractDef parsedDef = parser.parse(def);

        Assertions.assertTrue(parsedDef instanceof PointDef);
        Assertions.assertEquals(pointDef.getId(), parsedDef.getId());
        Assertions.assertEquals(pointDef.getName(), parsedDef.getName());
        Assertions.assertEquals(pointDef.getDescription(), parsedDef.getDescription());
        Assertions.assertEquals(pointDef.getEvent(), parsedDef.getEvent());
        Assertions.assertEquals(pointDef.getAward(), ((PointDef) parsedDef).getAward());
    }

    @Test
    void convertConstAward() {
        PointDef pointDef = new PointDef();
        pointDef.setId(1);
        pointDef.setName("point-1");
        pointDef.setAward(10.0);
        pointDef.setEvent("event.a");

        AbstractRule abstractRule = parser.convert(pointDef);

        Assertions.assertTrue(abstractRule instanceof PointRule);
        PointRule rule = (PointRule) abstractRule;

        Assertions.assertNotNull(rule.getId());
        Assertions.assertTrue(rule.getId().length() > 0);
        Assertions.assertEquals(pointDef.getName(), rule.getName());
        Assertions.assertFalse(rule.isAwardBasedOnEvent());
        Assertions.assertEquals(new BigDecimal("10.0"), rule.getAmountToAward());
        Assertions.assertEquals(EventExecutionFilterFactory.ALWAYS_TRUE, rule.getCriteria());
        Assertions.assertTrue(rule.getEventTypeMatcher() instanceof SingleEventTypeMatcher);
    }

    @Test
    void convertDynamicAward() {
        PointDef pointDef = new PointDef();
        pointDef.setId(1);
        pointDef.setName("point-1");
        pointDef.setAward("e.data.votes + 100");
        pointDef.setEvent("event.a");

        AbstractRule abstractRule = parser.convert(pointDef);

        Assertions.assertTrue(abstractRule instanceof PointRule);
        PointRule rule = (PointRule) abstractRule;

        Assertions.assertNotNull(rule.getId());
        Assertions.assertEquals(pointDef.getName(), rule.getName());
        Assertions.assertEquals(EventExecutionFilterFactory.ALWAYS_TRUE, rule.getCriteria());
        Assertions.assertTrue(rule.getEventTypeMatcher() instanceof SingleEventTypeMatcher);
        Assertions.assertTrue(rule.isAwardBasedOnEvent());
        Assertions.assertNotNull(rule.getAmountExpression());
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> toMap(PointDef def) {
        System.out.println(YAML.dumpAsMap(def));
        return (Map<String, Object>) YAML.load(YAML.dumpAsMap(def));
    }
}