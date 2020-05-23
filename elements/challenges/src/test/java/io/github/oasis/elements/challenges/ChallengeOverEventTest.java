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

import io.github.oasis.core.EventJson;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author Isuru Weerarathna
 */
class ChallengeOverEventTest {

    @Test
    void testChallengeOverAttrs() {
        ChallengeOverEvent event = ChallengeOverEvent.createFor(100, "challenge-rule-1");
        Assertions.assertEquals(100, event.getGameId());
        Assertions.assertEquals("challenge-rule-1", event.getChallengeRuleId());
        event.setFieldValue(EventJson.USER_ID, 1);

        Assertions.assertNull(event.getAllFieldValues());
        Assertions.assertNull(event.getFieldValue("any"));
        Assertions.assertNull(event.getEventType());
        Assertions.assertNull(event.getExternalId());
        Assertions.assertNull(event.getSource());
        Assertions.assertNull(event.getTeam());
        Assertions.assertNull(event.getUserName());
        Assertions.assertEquals(0, event.getTimestamp());
        Assertions.assertEquals(0, event.getUser());

    }

}