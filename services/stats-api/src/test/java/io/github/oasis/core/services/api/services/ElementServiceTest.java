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

import io.github.oasis.core.elements.AttributeInfo;
import io.github.oasis.core.elements.ElementDef;
import io.github.oasis.core.elements.SimpleElementDefinition;
import io.github.oasis.core.exception.OasisException;
import io.github.oasis.core.exception.OasisRuntimeException;
import io.github.oasis.core.services.api.beans.BackendRepository;
import io.github.oasis.core.services.api.beans.jdbc.JdbcRepository;
import io.github.oasis.core.services.api.controllers.admin.ElementsController;
import io.github.oasis.core.services.api.controllers.admin.GameAttributesController;
import io.github.oasis.core.services.api.dao.IElementDao;
import io.github.oasis.core.services.api.exceptions.ErrorCodes;
import io.github.oasis.core.services.api.exceptions.OasisApiRuntimeException;
import io.github.oasis.core.services.exceptions.OasisApiException;
import io.github.oasis.elements.badges.BadgeDef;
import io.github.oasis.engine.element.points.PointDef;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Isuru Weerarathna
 */
public class ElementServiceTest extends AbstractServiceTest {

    private ElementsController controller;
    private GameAttributesController attrController;

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

    private final AttributeInfo gold = AttributeInfo.builder()
            .name("gold").colorCode("#FFD700").priority(1).build();
    private final AttributeInfo silver = AttributeInfo.builder()
            .name("silver").colorCode("#C0C0C0").priority(2).build();
    private final AttributeInfo bronze = AttributeInfo.builder()
            .name("bronze").colorCode("#cd7f32").priority(3).build();

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

    @Test
    void testReadByType() throws OasisException {
        controller.add(1, samplePoint);
        controller.add(1, sampleBadge);

        List<ElementDef> badgeTypes = controller.getElementsByType(1, "badge");
        assertEquals(1, badgeTypes.size());
        assertTrue(badgeTypes.stream().anyMatch(t -> t.getElementId().equals(sampleBadge.getElementId())));

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> controller.read(1, "non.existing.id", true))
                .isInstanceOf(OasisApiException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCodes.ELEMENT_NOT_EXISTS);
    }

    @Test
    void testDelete() {
        assertThrows(OasisRuntimeException.class, () -> engineRepo.readElement(1, samplePoint.getElementId()));
        assertThrows(OasisRuntimeException.class, () -> engineRepo.readElement(1, sampleBadge.getElementId()));

        ElementDef addedElement = controller.add(1, samplePoint);
        System.out.println(addedElement);
        assertNotNull(addedElement.getData());
        assertElement(addedElement, samplePoint, true);

        ElementDef addedBadge = controller.add(1, sampleBadge);
        System.out.println(addedBadge);
        assertNotNull(addedBadge);
        assertElement(addedBadge, sampleBadge, true);

        {
            assertElement(engineRepo.readElement(1, samplePoint.getElementId()), samplePoint, true);
            assertElement(engineRepo.readElement(1, sampleBadge.getElementId()), sampleBadge, true);
        }

        controller.delete(1, samplePoint.getElementId());

        {
            assertThrows(OasisRuntimeException.class, () -> engineRepo.readElement(1, samplePoint.getElementId()));
            assertElement(engineRepo.readElement(1, sampleBadge.getElementId()), sampleBadge, true);
        }
    }

    @Test
    void testAddAttributes() {
        AttributeInfo dbGold = attrController.addAttribute(1, gold);
        assertAttribute(dbGold, gold);

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> attrController.addAttribute(1, gold))
                .isInstanceOf(OasisApiRuntimeException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCodes.ATTRIBUTE_EXISTS);

        // can add same attribute to different game
        attrController.addAttribute(2, gold);
    }

    @Test
    void testListAttributes() {
        attrController.addAttribute(1, gold);
        attrController.addAttribute(1, silver);
        attrController.addAttribute(1, bronze);
        attrController.addAttribute(2, gold);
        attrController.addAttribute(2, silver);

        {
            List<AttributeInfo> attributeInfos = attrController.listAttributes(1);
            assertEquals(3, attributeInfos.size());
            List<String> attrNames = attributeInfos.stream().map(AttributeInfo::getName).collect(Collectors.toList());
            assertTrue(attrNames.contains(gold.getName()));
            assertTrue(attrNames.contains(silver.getName()));
            assertTrue(attrNames.contains(bronze.getName()));
        }

        {
            List<AttributeInfo> attributeInfos = attrController.listAttributes(2);
            assertEquals(2, attributeInfos.size());
            List<String> attrNames = attributeInfos.stream().map(AttributeInfo::getName).collect(Collectors.toList());
            assertTrue(attrNames.contains(gold.getName()));
            assertTrue(attrNames.contains(silver.getName()));
            assertFalse(attrNames.contains(bronze.getName()));
        }
    }

    @Override
    protected JdbcRepository createJdbcRepository(Jdbi jdbi) {
        return new JdbcRepository(null,
                null,
                jdbi.onDemand(IElementDao.class),
                null,
                serializationSupport);
    }

    @Override
    protected void createServices(BackendRepository backendRepository) {
        controller = new ElementsController(new ElementService(backendRepository));
        attrController = new GameAttributesController(new GameAttributeService(backendRepository));
    }

    private void assertAttribute(AttributeInfo db, AttributeInfo other) {
        assertTrue(db.getId() > 0);
        assertEquals(other.getColorCode(), db.getColorCode());
        assertEquals(other.getName(), db.getName());
        assertEquals(other.getPriority(), db.getPriority());
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
