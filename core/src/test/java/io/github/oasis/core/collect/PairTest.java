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

package io.github.oasis.core.collect;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * @author Isuru Weerarathna
 */
@DisplayName("Pair Test")
public class PairTest {

    @Test
    @DisplayName("Pair items")
    void testPair() {
        Pair<String, String> pair = Pair.of("a", "b");
        Assertions.assertEquals("a", pair.getLeft());
        Assertions.assertEquals("b", pair.getRight());
    }

    @Test
    @DisplayName("Pair ToString")
    void testStringify() {
        Pair<String, String> pair = Pair.of("a", "b");
        Assertions.assertTrue(pair.toString().startsWith("Pair"));
    }

}
