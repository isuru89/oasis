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

import io.github.oasis.core.elements.spec.SelectorDef;
import io.github.oasis.core.events.BasePointEvent;
import io.github.oasis.engine.element.points.spec.CappedDef;
import io.github.oasis.engine.element.points.spec.PointRewardDef;
import io.github.oasis.engine.element.points.spec.PointSpecification;
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
    void testPointSpecEquals() {
        CappedDef cappedDef1 = new CappedDef("days", BigDecimal.ONE);
        CappedDef cappedDef2 = new CappedDef("days", BigDecimal.ONE);
        Assertions.assertEquals(cappedDef1, cappedDef2);
        Assertions.assertEquals(cappedDef1.hashCode(), cappedDef2.hashCode());

        PointSpecification pSpec1 = createPointSpec("event.b");
        PointSpecification pSpec2 = createPointSpec("event.b");
        Assertions.assertEquals(pSpec1, pSpec2);

        pSpec1.setCap(cappedDef1);
        pSpec2.setCap(cappedDef2);
        Assertions.assertEquals(pSpec1, pSpec2);

        pSpec2.setCap(new CappedDef("days", BigDecimal.TEN));
        Assertions.assertNotEquals(pSpec1, pSpec2);
    }

    @Test
    void testValidityOfCapSpec() {
        Assertions.assertThrows(RuntimeException.class, () -> new CappedDef("daily", null).validate());
        Assertions.assertThrows(RuntimeException.class, () -> new CappedDef(null, null).validate());
        Assertions.assertThrows(RuntimeException.class, () -> new CappedDef(null, BigDecimal.valueOf(10)).validate());
    }

    @Test
    void testValidityOfRewardSpec() {
        Assertions.assertThrows(RuntimeException.class, () -> new PointRewardDef().validate());
        Assertions.assertThrows(RuntimeException.class, () -> new PointRewardDef(null, "").validate());
        Assertions.assertThrows(RuntimeException.class, () -> new PointRewardDef(null, "e.value").validate());
        Assertions.assertThrows(RuntimeException.class, () -> new PointRewardDef(null, (BigDecimal) null).validate());
        Assertions.assertThrows(RuntimeException.class, () -> new PointRewardDef(null, BigDecimal.TEN).validate());
        Assertions.assertThrows(RuntimeException.class, () -> new PointRewardDef("pointId", (BigDecimal) null).validate());
        Assertions.assertThrows(RuntimeException.class, () -> new PointRewardDef("pointId", (String) null).validate());
    }

    @Test
    void testValidityOfPointSpec() {
        Assertions.assertThrows(RuntimeException.class, () -> {
            PointSpecification pointSpecification = createPointSpec("event.a");
            pointSpecification.validate();
        });
    }

    private PointSpecification createPointSpec(String matchEvent) {
        SelectorDef selectorDef = new SelectorDef();
        selectorDef.setMatchEvent(matchEvent);
        PointSpecification pSpec = new PointSpecification();
        pSpec.setSelector(selectorDef);
        return pSpec;
    }
}