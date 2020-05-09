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

package io.github.oasis.core.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Isuru Weerarathna
 */
@DisplayName("Numbers Test")
class NumbersTest {

    @Test
    void isNegative() {
        Assertions.assertTrue(Numbers.isNegative(BigDecimal.valueOf(-1)));
        Assertions.assertFalse(Numbers.isNegative(BigDecimal.ZERO));
        Assertions.assertFalse(Numbers.isNegative(BigDecimal.ONE));
    }

    @Test
    void isZero() {
        Assertions.assertFalse(Numbers.isZero(null));
        Assertions.assertFalse(Numbers.isZero(""));
        Assertions.assertFalse(Numbers.isZero("1"));
        Assertions.assertTrue(Numbers.isZero("0"));
    }

    @Test
    void isFirstOne() {
        Assertions.assertFalse(Numbers.isFirstOne(null));
        Assertions.assertFalse(Numbers.isFirstOne(1200L));
        Assertions.assertFalse(Numbers.isFirstOne(-1L));
        Assertions.assertTrue(Numbers.isFirstOne(1L));
    }

    @Test
    void isIncreasedOrEqual() {
    }

    @Test
    void isThresholdCrossedUp() {
    }

    @Test
    void isThresholdCrossedDown() {
    }

    @Test
    void asInt() {
    }

    @Test
    void testAsInt() {
    }

    @Test
    void testAsInt1() {
    }

    @Test
    void testAsInt2() {
    }

    @Test
    void asLong() {
    }

    @Test
    void testAsLong() {
    }

    @Test
    void addToScale() {
    }

    @Test
    void asDecimal() {
    }

    @Test
    void testAsDecimal() {
    }
}