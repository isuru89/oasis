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

package io.github.oasis.elements.milestones;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author Isuru Weerarathna
 */
class MilestoneDefTest {

    @Test
    void uniqueIdGeneratorValueExtractor() {
        MilestoneDef def1 = new MilestoneDef();
        def1.setValueExtractor("e.data.value");

        MilestoneDef def2 = new MilestoneDef();
        def2.setValueExtractor("e.data.value");

        MilestoneDef def3 = new MilestoneDef();
        def3.setValueExtractor("e.data.score");

        Assertions.assertEquals(def1.getValueExtractor(), def2.getValueExtractor());
        Assertions.assertEquals(def1.generateUniqueHash(), def2.generateUniqueHash());
        Assertions.assertNotEquals(def1.getValueExtractor(), def3.getValueExtractor());
        Assertions.assertNotEquals(def1.generateUniqueHash(), def3.generateUniqueHash());
    }

}