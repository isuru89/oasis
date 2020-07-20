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
import io.github.oasis.core.external.messages.PersistedDef;
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
    @SuppressWarnings("unchecked")
    void testPointParser() {
        List<PointDef> pointDefs = parseAll("points.yml", pointParser);
        findByName(pointDefs, "Answer-Accepted").ifPresent(def -> {
            assertTrue(isNonEmptyString(def.getDescription()));
            assertTrue(isNumber(def.getAward()));
            assertTrue(isNonEmptyString(def.getEvent()));
            assertEquals("stackoverflow.reputation", def.getPointId());
            assertTrue(Objects.isNull(def.getLimit()));
        });
        findByName(pointDefs, "Night-time-bonus").ifPresent(def -> {
            assertTrue(isNonEmptyString(def.getDescription()));
            assertTrue(isNumber(def.getAward()));
            assertEquals(1, def.getTimeRanges().size());
            assertEquals("marks", def.getPointId());
            AbstractDef.TimeRangeDef timeRangeDef = def.getTimeRanges().get(0);
            assertEquals(AbstractDef.TIME_RANGE_TYPE_TIME, timeRangeDef.getType());
            assertEquals("00:00", timeRangeDef.getFrom());
            assertEquals("06:00", timeRangeDef.getTo());
        });
        findByName(pointDefs, "Special-Seasonal-Award").ifPresent(def -> {
            assertTrue(isNonEmptyString(def.getDescription()));
            assertTrue(isNonEmptyString(def.getAward()));
            assertEquals(1, def.getTimeRanges().size());
            assertEquals("star.points", def.getPointId());
            AbstractDef.TimeRangeDef timeRangeDef = def.getTimeRanges().get(0);
            assertEquals(AbstractDef.TIME_RANGE_TYPE_SEASONAL, timeRangeDef.getType());
            assertEquals("12-01", timeRangeDef.getFrom());
            assertEquals("12-31", timeRangeDef.getTo());
        });
        findByName(pointDefs, "General-Spending-Rule").ifPresent(def -> {
            assertTrue(isNonEmptyString(def.getDescription()));
            assertTrue(isNonEmptyString(def.getAward()));
            assertTrue(isNonEmptyString(def.getEvent()));
            assertEquals("star.points", def.getPointId());
        });
        findByName(pointDefs, "Big-Purchase-Bonus").ifPresent(def -> {
            assertTrue(isNonEmptyString(def.getDescription()));
            assertTrue(isNonEmptyString(def.getAward()));
            assertTrue(isNonEmptyString(def.getEvent()));
            assertTrue(def.getAward().toString().contains("\n"));
            assertEquals("star.points", def.getPointId());
        });
        findByName(pointDefs, "Questions-Asked-Limited").ifPresent(def -> {
            assertTrue(isNonEmptyString(def.getDescription()));
            assertTrue(isNumber(def.getAward()));
            assertTrue(isNonEmptyString(def.getEvent()));
            assertTrue(Objects.nonNull(def.getLimit()));
            assertEquals("stackoverflow.reputation", def.getPointId());
            Map<String, Object> limit = (Map<String, Object>) def.getLimit();
            assertTrue(limit.containsKey("daily"));
            assertTrue(isNumber(limit.get("daily")));
        });
        findByName(pointDefs, "Monthly-Last-Sale").ifPresent(def -> {
            assertNotNull(def.getTimeRanges());
            assertEquals(1, def.getTimeRanges().size());
            AbstractDef.TimeRangeDef rangeDef = def.getTimeRanges().get(0);
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

    private static PersistedDef asPersistedDef(Map<String, Object> data) {
        PersistedDef def = new PersistedDef();
        def.setData(data);
        def.setType(PersistedDef.GAME_RULE_ADDED);
        def.setImpl(PointDef.class.getName());
        return def;
    }

    @SuppressWarnings("unchecked")
    public static List<PointDef> parseAll(String resourcePath, PointParser pointParser) {
        Map<String, Object> map = loadGroupFile(resourcePath);
        List<Map<String, Object>> items = (List<Map<String, Object>>) map.get("points");
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
