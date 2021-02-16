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

package io.github.oasis.core.services.api.services;

import io.github.oasis.core.elements.ElementDef;
import io.github.oasis.core.elements.SimpleElementDefinition;
import io.github.oasis.core.services.api.beans.BackendRepository;
import io.github.oasis.core.services.api.beans.jdbc.JdbcRepository;
import io.github.oasis.core.services.api.controllers.admin.ElementsController;
import io.github.oasis.core.services.api.dao.IElementDao;
import io.github.oasis.core.services.api.exceptions.ErrorCodes;
import io.github.oasis.core.services.api.exceptions.OasisApiRuntimeException;
import io.github.oasis.core.services.exceptions.OasisApiException;
import io.github.oasis.elements.badges.BadgeDef;
import io.github.oasis.engine.element.points.PointDef;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Isuru Weerarathna
 */
public class ElementServiceTest extends AbstractServiceTest {

    private ElementsController controller;

    private final ElementDef samplePoint = ElementDef.builder()
            .elementId("test.point1")
            .gameId(1)
            .impl(PointDef.class.getName())
            .type("point")
            .metadata(new SimpleElementDefinition("test.point1", "Star points", "blah blah blah"))
            .data(Map.of("f1", "v1", "f2", "v2"))
            .build();

    private final ElementDef sampleBadge = ElementDef.builder()
            .elementId("test.badge")
            .gameId(1)
            .impl(BadgeDef.class.getName())
            .type("badge")
            .metadata(new SimpleElementDefinition("test.badge", "Mega badge", "another description"))
            .data(Map.of("f3", "v3", "f4", "v4"))
            .build();

    @Test
    void testAdd() {
        ElementDef addedElement = controller.add(1, samplePoint);
        System.out.println(addedElement);
        assertNotNull(addedElement.getData());
        assertElement(addedElement, samplePoint, true);

        ElementDef addedBadge = controller.add(1, sampleBadge);
        System.out.println(addedBadge);
        assertNotNull(addedBadge);
        assertElement(addedBadge, sampleBadge, true);

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> controller.add(1, samplePoint))
            .isInstanceOf(OasisApiRuntimeException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCodes.ELEMENT_ALREADY_EXISTS);
    }

    @Test
    void testRead() throws OasisApiException {
        controller.add(1, samplePoint);
        {
            ElementDef withData = controller.read(1, samplePoint.getElementId(), true);
            assertNotNull(withData);
            assertElement(withData, samplePoint, true);
        }

        {
            ElementDef withData = controller.read(1, samplePoint.getElementId(), false);
            assertNotNull(withData);
            assertElement(withData, samplePoint, false);
            assertNull(withData.getData());
        }

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> controller.read(1, "non.existing.id", true))
                .isInstanceOf(OasisApiException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCodes.ELEMENT_NOT_EXISTS);
    }

    @Override
    JdbcRepository createJdbcRepository(Jdbi jdbi) {
        return new JdbcRepository(null,
                null,
                jdbi.onDemand(IElementDao.class),
                null,
                serializationSupport);
    }

    @Override
    void createServices(BackendRepository backendRepository) {
        controller = new ElementsController(new ElementService(backendRepository, null));
    }

    private void assertElement(ElementDef db, ElementDef other, boolean withData) {
        assertTrue(db.getId() > 0);
        assertEquals(other.getElementId(), db.getElementId());
        assertEquals(other.getType(), db.getType());
        assertEquals(other.getImpl(), db.getImpl());
        assertEquals(other.getMetadata().getId(), db.getMetadata().getId());
        assertEquals(other.getMetadata().getName(), db.getMetadata().getName());
        assertEquals(other.getMetadata().getDescription(), db.getMetadata().getDescription());
        if (withData) assertMap(db.getData(), other.getData());
    }

    private void assertMap(Map<String, Object> db, Map<String, Object> other) {
        assertEquals(other.size(), db.size());
        {
            Set<String> keys = new HashSet<>(other.keySet());
            keys.removeAll(db.keySet());
            assertTrue(keys.isEmpty());
        }
    }
}
