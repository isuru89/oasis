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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author Isuru Weerarathna
 */
class ChallengeDefTest {

    @Test
    void uniqueIDWithStartTime() {
        ChallengeDef def1 = new ChallengeDef();
        def1.setStartAt(System.currentTimeMillis());

        ChallengeDef def2 = new ChallengeDef();
        def2.setStartAt(def1.getStartAt());

        ChallengeDef def3 = new ChallengeDef();
        def3.setStartAt(System.currentTimeMillis() + 1);

        Assertions.assertEquals(def1.getStartAt(), def2.getStartAt());
        Assertions.assertEquals(def1.generateUniqueHash(), def2.generateUniqueHash());
        Assertions.assertNotEquals(def1.getStartAt(), def3.getStartAt());
        Assertions.assertNotEquals(def1.generateUniqueHash(), def3.generateUniqueHash());
    }

    @Test
    void uniqueIDWithExpireTime() {
        ChallengeDef def1 = new ChallengeDef();
        def1.setExpireAt(System.currentTimeMillis());

        ChallengeDef def2 = new ChallengeDef();
        def2.setExpireAt(def1.getExpireAt());

        ChallengeDef def3 = new ChallengeDef();
        def3.setExpireAt(System.currentTimeMillis() + 1);

        Assertions.assertEquals(def1.getExpireAt(), def2.getExpireAt());
        Assertions.assertEquals(def1.generateUniqueHash(), def2.generateUniqueHash());
        Assertions.assertNotEquals(def1.getExpireAt(), def3.getExpireAt());
        Assertions.assertNotEquals(def1.generateUniqueHash(), def3.generateUniqueHash());
    }

}