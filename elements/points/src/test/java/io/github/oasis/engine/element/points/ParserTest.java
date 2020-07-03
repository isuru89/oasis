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
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Isuru Weerarathna
 */
public class ParserTest {

    private final PointParser pointParser = new PointParser();

    private Map<String, Object> loadGroupFile(String resourcePath) {
        try (InputStream resourceAsStream = Thread.currentThread()
                .getContextClassLoader().getResourceAsStream(resourcePath)) {
            return new Yaml().load(resourceAsStream);
        } catch (IOException e) {
            Assertions.fail("Cannot load resource " + resourcePath);
            throw new RuntimeException(e);
        }
    }

    private PersistedDef asPersistedDef(Map<String, Object> data) {
        PersistedDef def = new PersistedDef();
        def.setData(data);
        def.setType(PersistedDef.GAME_RULE_ADDED);
        def.setImpl(PointDef.class.getName());
        return def;
    }

    @SuppressWarnings("unchecked")
    private List<PointDef> parseAll(String resourcePath) {
        Map<String, Object> map = loadGroupFile(resourcePath);
        List<Map<String, Object>> items = (List<Map<String, Object>>) map.get("points");
        return items.stream().map(this::asPersistedDef)
                .map(def -> pointParser.parse(def))
                .collect(Collectors.toList());
    }

    private Optional<PointDef> findByName(List<PointDef> pointDefs, String name) {
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

    @Test
    void testPointParser() {
        List<PointDef> pointDefs = parseAll("points.yml");
        findByName(pointDefs, "Answer-Accepted").ifPresent(answerAccepted -> {
            assertTrue(isNonEmptyString(answerAccepted.getDescription()));
            assertTrue(isNumber(answerAccepted.getAward()));
            assertTrue(isNonEmptyString(answerAccepted.getEvent()));
        });
        findByName(pointDefs, "Night-time-bonus").ifPresent(def -> {
            assertTrue(isNonEmptyString(def.getDescription()));
            assertTrue(isNumber(def.getAward()));
            assertEquals(1, def.getTimeRanges().size());
            AbstractDef.TimeRangeDef timeRangeDef = def.getTimeRanges().get(0);
            assertEquals(AbstractDef.TIME_RANGE_TYPE_TIME, timeRangeDef.getType());
            assertEquals("00:00", timeRangeDef.getFrom());
            assertEquals("06:00", timeRangeDef.getTo());
        });
        findByName(pointDefs, "Special-Seasonal-Award").ifPresent(def -> {
            assertTrue(isNonEmptyString(def.getDescription()));
            assertTrue(isNonEmptyString(def.getAward()));
            assertEquals(1, def.getTimeRanges().size());
            AbstractDef.TimeRangeDef timeRangeDef = def.getTimeRanges().get(0);
            assertEquals(AbstractDef.TIME_RANGE_TYPE_SEASONAL, timeRangeDef.getType());
            assertEquals("12-01", timeRangeDef.getFrom());
            assertEquals("12-31", timeRangeDef.getTo());
        });
        findByName(pointDefs, "General-Spending-Rule").ifPresent(answerAccepted -> {
            assertTrue(isNonEmptyString(answerAccepted.getDescription()));
            assertTrue(isNonEmptyString(answerAccepted.getAward()));
            assertTrue(isNonEmptyString(answerAccepted.getEvent()));
        });
        findByName(pointDefs, "Big-Purchase-Bonus").ifPresent(answerAccepted -> {
            assertTrue(isNonEmptyString(answerAccepted.getDescription()));
            assertTrue(isNonEmptyString(answerAccepted.getAward()));
            assertTrue(isNonEmptyString(answerAccepted.getEvent()));
            assertTrue(answerAccepted.getAward().toString().contains("\n"));
        });
    }

}
