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

import io.github.oasis.core.events.BasePointEvent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * @author Isuru Weerarathna
 */
class PointDefTest {

    @Test
    void testPointEventCreation() {
        TEvent tEvent = TEvent.createKeyValue(Instant.now().toEpochMilli(), "event.a", 56);
        PointEvent event = new PointEvent("my.points", BigDecimal.TEN, tEvent);

        System.out.println(event.toString());
        Assertions.assertTrue(event.toString().contains("id=my.points"));
        Assertions.assertNotEquals(tEvent.getExternalId(), event.getExternalId());
        Assertions.assertEquals(BasePointEvent.DEFAULT_POINTS_KEY, event.getPointStoredKey());
        Assertions.assertEquals("my.points", event.getEventType());
        Assertions.assertEquals("my.points", event.getPointId());
        Assertions.assertEquals(tEvent.getTimestamp(), event.getTimestamp());
        Assertions.assertEquals(tEvent.getGameId(), event.getGameId());
        Assertions.assertEquals(tEvent.getSource(), event.getSource());
    }

    @Test
    void testUniqueIDGenerationWhenAwardChanged() {
        PointDef def1 = new PointDef();
        def1.setId(1);
        def1.setName("point-1");
        def1.setAward(23.0);

        PointDef def2 = new PointDef();
        def2.setId(def1.getId());
        def2.setName(def1.getName());
        def2.setAward("e.data.votes + 100");

        Assertions.assertTrue(def2.getAward() instanceof String);
        Assertions.assertNotEquals(def1.generateUniqueHash(), def2.generateUniqueHash());
    }

    @Test
    void testSameIDGenerationWhenSameAward() {
        PointDef def1 = new PointDef();
        def1.setId(1);
        def1.setName("point-1");
        def1.setAward(23.0);

        PointDef def2 = new PointDef();
        def2.setId(def1.getId());
        def2.setName(def1.getName());
        def2.setAward("23.0");

        Assertions.assertEquals(def1.generateUniqueHash(), def2.generateUniqueHash());
    }

}