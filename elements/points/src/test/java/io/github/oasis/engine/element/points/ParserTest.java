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
import io.github.oasis.core.elements.spec.TimeRangeDef;
import io.github.oasis.core.external.messages.EngineMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Isuru Weerarathna
 */
public class ParserTest {

    private final PointParser pointParser = new PointParser();

    @Test
    void testPointParser() {
        List<PointDef> pointDefs = parseAll("points.yml", pointParser);
        findByName(pointDefs, "Answer-Accepted").ifPresent(def -> {
            assertTrue(isNonEmptyString(def.getDescription()));
            assertNotNull(def.getSpec());
            assertTrue(isNumber(def.getSpec().getReward().getAmount()));
            assertTrue(isNonEmptyString(def.getSpec().getSelector().getMatchEvent()));
            assertEquals("stackoverflow.reputation", def.getSpec().getReward().getPointId());
            assertTrue(Objects.isNull(def.getSpec().getCap()));
        });
        findByName(pointDefs, "Night-time-bonus").ifPresent(def -> {
            assertTrue(isNonEmptyString(def.getDescription()));
            assertNotNull(def.getSpec());
            assertTrue(isNumber(def.getSpec().getReward().getAmount()));
            assertEquals(1, def.getSpec().getSelector().getAcceptsWithin().getAnyOf().size());
            assertEquals("marks", def.getSpec().getReward().getPointId());
            TimeRangeDef timeRangeDef = def.getSpec().getSelector().getAcceptsWithin().getAnyOf().get(0);
            assertEquals(AbstractDef.TIME_RANGE_TYPE_TIME, timeRangeDef.getType());
            assertEquals("00:00", timeRangeDef.getFrom());
            assertEquals("06:00", timeRangeDef.getTo());
        });
        findByName(pointDefs, "Special-Seasonal-Award").ifPresent(def -> {
            assertTrue(isNonEmptyString(def.getDescription()));
            assertNotNull(def.getSpec());
            assertTrue(isNonEmptyString(def.getSpec().getReward().getExpression()));
            assertEquals(1, def.getSpec().getSelector().getAcceptsWithin().getAnyOf().size());
            assertEquals("star.points", def.getSpec().getReward().getPointId());
            TimeRangeDef timeRangeDef = def.getSpec().getSelector().getAcceptsWithin().getAnyOf().get(0);
            assertEquals(AbstractDef.TIME_RANGE_TYPE_SEASONAL, timeRangeDef.getType());
            assertEquals("12-01", timeRangeDef.getFrom());
            assertEquals("12-31", timeRangeDef.getTo());
        });
        findByName(pointDefs, "General-Spending-Rule").ifPresent(def -> {
            assertTrue(isNonEmptyString(def.getDescription()));
            assertNotNull(def.getSpec());
            assertTrue(isNonEmptyString(def.getSpec().getReward().getExpression()));
            assertTrue(isNonEmptyString(def.getSpec().getSelector().getMatchEvent()));
            assertEquals("star.points", def.getSpec().getReward().getPointId());
        });
        findByName(pointDefs, "Big-Purchase-Bonus").ifPresent(def -> {
            assertTrue(isNonEmptyString(def.getDescription()));
            assertNotNull(def.getSpec());
            assertTrue(isNonEmptyString(def.getSpec().getReward().getExpression()));
            assertTrue(isNonEmptyString(def.getSpec().getSelector().getMatchEvent()));
            assertTrue(def.getSpec().getReward().getExpression().contains("\n"));
            assertEquals("star.points", def.getSpec().getReward().getPointId());
        });
        findByName(pointDefs, "Questions-Asked-Limited").ifPresent(def -> {
            assertTrue(isNonEmptyString(def.getDescription()));
            assertNotNull(def.getSpec().getReward().getAmount());
            assertTrue(isNonEmptyString(def.getSpec().getSelector().getMatchEvent()));
            assertEquals("stackoverflow.reputation", def.getSpec().getReward().getPointId());
            assertTrue(Objects.nonNull(def.getSpec().getCap()));
            assertEquals("daily", def.getSpec().getCap().getDuration());
            assertTrue(Objects.nonNull(def.getSpec().getCap().getLimit()));
        });
        findByName(pointDefs, "Monthly-Last-Sale").ifPresent(def -> {
            assertNotNull(def.getSpec().getSelector().getAcceptsWithin());
            assertEquals(1, def.getSpec().getSelector().getAcceptsWithin().getAnyOf().size());
            TimeRangeDef rangeDef = def.getSpec().getSelector().getAcceptsWithin().getAnyOf().get(0);
            assertEquals("custom", rangeDef.getType());
            assertTrue(isNonEmptyString(rangeDef.getExpression()));
        });
    }

    private static Map<String, Object> loadGroupFile(String resourcePath) {
        try (InputStream resourceAsStream = Thread.currentThread()
                .getContextClassLoader().getResourceAsStream(resourcePath)) {
            return new Yaml().load(resourceAsStream);
        } catch (IOException e) {
            Assertions.fail("Cannot load resource " + resourcePath);
            throw new RuntimeException(e);
        }
    }

    private static EngineMessage asPersistedDef(Map<String, Object> data) {
        EngineMessage def = new EngineMessage();
        def.setData(data);
        def.setType(EngineMessage.GAME_RULE_ADDED);
        def.setImpl(PointDef.class.getName());
        return def;
    }

    @SuppressWarnings("unchecked")
    public static List<PointDef> parseAll(String resourcePath, PointParser pointParser) {
        Map<String, Object> map = loadGroupFile(resourcePath);
        List<Map<String, Object>> items = (List<Map<String, Object>>) map.get("elements");
        return items.stream().map(ParserTest::asPersistedDef)
                .map(pointParser::parse)
                .collect(Collectors.toList());
    }

    public static Optional<PointDef> findByName(List<PointDef> pointDefs, String name) {
        return pointDefs.stream()
                .filter(p -> name.equals(p.getName()))
                .findFirst();
    }

    private boolean isNumber(Object value) {
        return value instanceof Number;
    }

    private boolean isNonEmptyString(Object value) {
        return value instanceof String && !((String) value).isBlank();
    }

}
