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

package io.github.oasis.elements.badges;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Isuru Weerarathna
 */
class BadgeDefTest {

    @Test
    void testUniqueIDGenerationMaxAwards() {
        BadgeDef def1 = new BadgeDef();
        def1.setMaxAwardTimes(3);

        BadgeDef def2 = new BadgeDef();
        def2.setMaxAwardTimes(3);

        BadgeDef def3 = new BadgeDef();
        def3.setMaxAwardTimes(5);

        Assertions.assertEquals(def1.getMaxAwardTimes(), def2.getMaxAwardTimes());
        Assertions.assertEquals(def1.generateUniqueHash(), def2.generateUniqueHash());
        Assertions.assertNotEquals(def1.getMaxAwardTimes(), def3.getMaxAwardTimes());
        Assertions.assertEquals(def1.generateUniqueHash(), def3.generateUniqueHash());
    }

    @Test
    void testUniqueIDGenerationConsecutiveness() {
        BadgeDef def1 = new BadgeDef();
        def1.setConsecutive(true);

        BadgeDef def2 = new BadgeDef();
        def2.setConsecutive(true);

        BadgeDef def3 = new BadgeDef();
        def3.setConsecutive(false);

        Assertions.assertEquals(def1.getConsecutive(), def2.getConsecutive());
        Assertions.assertEquals(def1.generateUniqueHash(), def2.generateUniqueHash());
        Assertions.assertNotEquals(def1.getConsecutive(), def3.getConsecutive());
        Assertions.assertNotEquals(def1.generateUniqueHash(), def3.generateUniqueHash());
    }

    @Test
    void testUniqueIDGenerationAttribute() {
        BadgeDef def1 = new BadgeDef();
        def1.setAttribute(1);

        BadgeDef def2 = new BadgeDef();
        def2.setAttribute(1);

        BadgeDef def3 = new BadgeDef();
        def3.setAttribute(2);

        Assertions.assertEquals(def1.getAttribute(), def2.getAttribute());
        Assertions.assertEquals(def1.generateUniqueHash(), def2.generateUniqueHash());
        Assertions.assertNotEquals(def1.getAttribute(), def3.getAttribute());
        Assertions.assertNotEquals(def1.generateUniqueHash(), def3.generateUniqueHash());
    }

    @Test
    void testUniqueIDGenerationThreshold() {
        BadgeDef def1 = new BadgeDef();
        def1.setThreshold(BigDecimal.valueOf(100.0));

        BadgeDef def2 = new BadgeDef();
        def2.setThreshold(BigDecimal.valueOf(100.0));

        BadgeDef def3 = new BadgeDef();
        def3.setThreshold(BigDecimal.valueOf(200.0));

        Assertions.assertEquals(def1.getThreshold(), def2.getThreshold());
        Assertions.assertEquals(def1.generateUniqueHash(), def2.generateUniqueHash());
        Assertions.assertNotEquals(def1.getThreshold(), def3.getThreshold());
        Assertions.assertNotEquals(def1.generateUniqueHash(), def3.generateUniqueHash());
    }

    @Test
    void testUniqueIDGenerationTimeUnit() {
        BadgeDef def1 = new BadgeDef();
        def1.setTimeUnit(84000);

        BadgeDef def2 = new BadgeDef();
        def2.setTimeUnit(84000);

        BadgeDef def3 = new BadgeDef();
        def3.setTimeUnit(3600);

        Assertions.assertEquals(def1.getTimeUnit(), def2.getTimeUnit());
        Assertions.assertEquals(def1.generateUniqueHash(), def2.generateUniqueHash());
        Assertions.assertNotEquals(def1.getTimeUnit(), def3.getTimeUnit());
        Assertions.assertNotEquals(def1.generateUniqueHash(), def3.generateUniqueHash());
    }

    @Test
    void testUniqueIDGenerationValueExtractor() {
        BadgeDef def1 = new BadgeDef();
        def1.setValueExtractorExpression("e.data.value");

        BadgeDef def2 = new BadgeDef();
        def2.setValueExtractorExpression("e.data.value");

        BadgeDef def3 = new BadgeDef();
        def3.setValueExtractorExpression("e.data.score");

        Assertions.assertEquals(def1.getValueExtractorExpression(), def2.getValueExtractorExpression());
        Assertions.assertEquals(def1.generateUniqueHash(), def2.generateUniqueHash());
        Assertions.assertNotEquals(def1.getValueExtractorExpression(), def3.getValueExtractorExpression());
        Assertions.assertNotEquals(def1.generateUniqueHash(), def3.generateUniqueHash());
    }

    @Test
    void testUniqueIDGenerationConditions() {
        BadgeDef def1 = new BadgeDef();
        def1.setConditions(List.of(
                new BadgeDef.Condition(1, "e.data.value > 100", 1),
                new BadgeDef.Condition(2, "e.data.value > 200", 2),
                new BadgeDef.Condition(3, "e.data.value > 300", 3)
        ));

        BadgeDef def2 = new BadgeDef();
        def2.setConditions(List.of(
                new BadgeDef.Condition(1, "e.data.value > 100", 1),
                new BadgeDef.Condition(2, "e.data.value > 200", 2),
                new BadgeDef.Condition(3, "e.data.value > 300", 3)
        ));

        BadgeDef def3 = new BadgeDef();
        def3.setConditions(List.of(
                new BadgeDef.Condition(1, "e.data.value > 100", 1),
                new BadgeDef.Condition(2, "e.data.value > 200", 2),
                new BadgeDef.Condition(4, "e.data.value > 300", 3)
        ));

        Assertions.assertEquals(def1.generateUniqueHash(), def2.generateUniqueHash());
        Assertions.assertNotEquals(def1.generateUniqueHash(), def3.generateUniqueHash());
    }

    @Test
    void testUniqueIDGenerationThresholds() {
        BadgeDef def1 = new BadgeDef();
        def1.setThresholds(List.of(
                new BadgeDef.Threshold(BigDecimal.valueOf(100.0), 1),
                new BadgeDef.Threshold(BigDecimal.valueOf(200.0), 2),
                new BadgeDef.Threshold(BigDecimal.valueOf(300.0), 3)
        ));

        BadgeDef def2 = new BadgeDef();
        def2.setThresholds(List.of(
                new BadgeDef.Threshold(BigDecimal.valueOf(100.0), 1),
                new BadgeDef.Threshold(BigDecimal.valueOf(200.0), 2),
                new BadgeDef.Threshold(BigDecimal.valueOf(300.0), 3)
        ));

        BadgeDef def3 = new BadgeDef();
        def3.setThresholds(List.of(
                new BadgeDef.Threshold(BigDecimal.valueOf(100.0), 1),
                new BadgeDef.Threshold(BigDecimal.valueOf(200.0), 2),
                new BadgeDef.Threshold(BigDecimal.valueOf(500.0), 3)
        ));

        Assertions.assertEquals(def1.generateUniqueHash(), def2.generateUniqueHash());
        Assertions.assertNotEquals(def1.generateUniqueHash(), def3.generateUniqueHash());
    }

    @Test
    void testUniqueIDGenerationStreaks() {
        BadgeDef def1 = new BadgeDef();
        def1.setStreaks(List.of(
                new BadgeDef.Streak(3, 1),
                new BadgeDef.Streak(5, 2),
                new BadgeDef.Streak(10, 3)
        ));

        BadgeDef def2 = new BadgeDef();
        def2.setStreaks(List.of(
                new BadgeDef.Streak(3, 1),
                new BadgeDef.Streak(5, 2),
                new BadgeDef.Streak(10, 3)
        ));

        BadgeDef def3 = new BadgeDef();
        def3.setStreaks(List.of(
                new BadgeDef.Streak(3, 1),
                new BadgeDef.Streak(7, 2),
                new BadgeDef.Streak(10, 3)
        ));

        Assertions.assertEquals(def1.generateUniqueHash(), def2.generateUniqueHash());
        Assertions.assertNotEquals(def1.generateUniqueHash(), def3.generateUniqueHash());
    }

}