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

import io.github.oasis.core.Game;
import io.github.oasis.core.elements.RankInfo;
import io.github.oasis.core.elements.ElementDef;
import io.github.oasis.core.services.api.TestUtils;
import io.github.oasis.core.services.api.exceptions.ErrorCodes;
import io.github.oasis.core.services.api.to.ElementCreateRequest;
import io.github.oasis.core.services.api.to.ElementCreateRequest.ElementMetadata;
import io.github.oasis.core.services.api.to.ElementUpdateRequest;
import io.github.oasis.core.services.api.to.RankCreationRequest;
import io.github.oasis.core.services.api.to.GameCreateRequest;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Isuru Weerarathna
 */
public class ElementServiceTest extends AbstractServiceTest {

    private static final int GAME_ID = 1;

    private static final String DEF_ELEMENTS_URL = "/games/" + GAME_ID + "/elements";

    private ElementCreateRequest samplePoint;
    private ElementCreateRequest sampleBadge;

    private final RankCreationRequest gold = RankCreationRequest.builder()
            .name("gold").colorCode("#FFD700").priority(1).build();
    private final RankCreationRequest silver = RankCreationRequest.builder()
            .name("silver").colorCode("#C0C0C0").priority(2).build();
    private final RankCreationRequest bronze = RankCreationRequest.builder()
            .name("bronze").colorCode("#cd7f32").priority(3).build();

    @BeforeEach
    void beforeEachTest() {
        List<ElementCreateRequest> elementCreateRequests = TestUtils.parseElementRules("rules.yml", GAME_ID);
        samplePoint = TestUtils.findById("testpoint", elementCreateRequests);
        sampleBadge = TestUtils.findById("testbadge", elementCreateRequests);
    }

    @Test
    void testAdd() {
        ElementDef addedElement = callElementPost(GAME_ID, samplePoint);
        System.out.println(addedElement);
        assertNotNull(addedElement.getData());
        assertElement(addedElement, samplePoint, true);

        ElementDef addedBadge = callElementPost(GAME_ID, sampleBadge);
        System.out.println(addedBadge);
        assertNotNull(addedBadge);
        assertElement(addedBadge, sampleBadge, true);

        // add same element will throw error
        doPostError(DEF_ELEMENTS_URL, samplePoint, HttpStatus.BAD_REQUEST, ErrorCodes.ELEMENT_ALREADY_EXISTS);
    }

    @Test
    void testAddValidations() {
        // game id validations
        doPostError(DEF_ELEMENTS_URL, samplePoint.toBuilder().gameId(null).build(),
                HttpStatus.BAD_REQUEST, ErrorCodes.INVALID_PARAMETER);
        doPostError(DEF_ELEMENTS_URL, samplePoint.toBuilder().gameId(-1).build(),
                HttpStatus.BAD_REQUEST, ErrorCodes.INVALID_PARAMETER);
        doPostError(DEF_ELEMENTS_URL, samplePoint.toBuilder().gameId(0).build(),
                HttpStatus.BAD_REQUEST, ErrorCodes.INVALID_PARAMETER);

        // type validations
        doPostError(DEF_ELEMENTS_URL, samplePoint.toBuilder().type(null).build(),
                HttpStatus.BAD_REQUEST, ErrorCodes.INVALID_PARAMETER);
        doPostError(DEF_ELEMENTS_URL, samplePoint.toBuilder().type("").build(),
                HttpStatus.BAD_REQUEST, ErrorCodes.INVALID_PARAMETER);

        // metadata validations
        doPostError(DEF_ELEMENTS_URL, samplePoint.toBuilder().metadata(null).build(),
                HttpStatus.BAD_REQUEST, ErrorCodes.INVALID_PARAMETER);
        doPostError(DEF_ELEMENTS_URL, samplePoint.toBuilder()
                .metadata(ElementMetadata.builder().name("point-name").build())
                .build(),
                HttpStatus.BAD_REQUEST, ErrorCodes.INVALID_PARAMETER);
        doPostError(DEF_ELEMENTS_URL, samplePoint.toBuilder()
                        .metadata(ElementMetadata.builder().id("pointid").build())
                        .build(),
                HttpStatus.BAD_REQUEST, ErrorCodes.INVALID_PARAMETER);

        // data field
        doPostError(DEF_ELEMENTS_URL, samplePoint.toBuilder().data(null).build(),
                HttpStatus.BAD_REQUEST, ErrorCodes.INVALID_PARAMETER);
        doPostError(DEF_ELEMENTS_URL, samplePoint.toBuilder().data(Map.of()).build(),
                HttpStatus.BAD_REQUEST, ErrorCodes.INVALID_PARAMETER);
    }

    @Test
    void testAddWithInvalidSpec() {
        doPostError(DEF_ELEMENTS_URL, ElementCreateRequest.builder()
                .gameId(1)
                .type("core:badge")
                .metadata(new ElementMetadata("test.badge", "Mega badge", "another description"))
                .data(Map.of("f3", "v3", "f4", "v4", "f9", 3))
                .build(),
                HttpStatus.BAD_REQUEST, ErrorCodes.ELEMENT_SPEC_INVALID);
    }

    @Test
    void testAddWithUnknownType() {
        doPostError(DEF_ELEMENTS_URL, ElementCreateRequest.builder()
                        .gameId(1)
                        .type("core:unknown")
                        .metadata(new ElementMetadata("test.badge", "Mega badge", "another description"))
                        .data(Map.of("f3", "v3", "f4", "v4", "f9", 3))
                        .build(),
                HttpStatus.BAD_REQUEST, ErrorCodes.ELEMENT_SPEC_INVALID);
    }

    @Test
    void testRead() {
        callElementPost(GAME_ID, samplePoint);
        {
            ElementDef withData = callElementGet(GAME_ID, samplePoint.getMetadata().getId(), true);
            assertNotNull(withData);
            assertElement(withData, samplePoint, true);
        }

        {
            ElementDef withData = callElementGet(GAME_ID, samplePoint.getMetadata().getId(), false);
            assertNotNull(withData);
            assertElement(withData, samplePoint, false);
            assertNull(withData.getData());
        }

        doGetError("/games/" + GAME_ID + "/elements/non.existing.id?withData=true",
                HttpStatus.NOT_FOUND,
                ErrorCodes.ELEMENT_NOT_EXISTS);
    }

    @Test
    void testUpdate() {
        ElementDef addedPoint = callElementPost(GAME_ID, samplePoint);
        {
            ElementUpdateRequest request = new ElementUpdateRequest();
            request.setName("Star Points 234");
            request.setDescription("buha buha buha buha");
            request.setVersion(addedPoint.getVersion());
            assertNotEquals(request.getName(), addedPoint.getMetadata().getName());
            assertNotEquals(request.getDescription(), addedPoint.getMetadata().getDescription());

            ElementDef updatedElements = callElementPatch(GAME_ID, addedPoint.getMetadata().getId(), request);
            assertEquals(request.getName(), updatedElements.getMetadata().getName());
            assertEquals(request.getDescription(), updatedElements.getMetadata().getDescription());
            assertEquals(samplePoint.getMetadata().getId(), updatedElements.getMetadata().getId());
        }

        ElementUpdateRequest uReq = new ElementUpdateRequest();
        uReq.setName("unknown name");
        uReq.setDescription("unknown des");
        uReq.setVersion(1);
        doPatchError("/games/" + GAME_ID + "/elements/non.existing.id", uReq, HttpStatus.NOT_FOUND, ErrorCodes.ELEMENT_NOT_EXISTS);
    }

    @Test
    void testUpdateFailWhenNoVersion() {
        ElementDef addedPoint = callElementPost(GAME_ID, samplePoint);
        {
            ElementUpdateRequest request = new ElementUpdateRequest();
            request.setName("Star Points 234");
            request.setDescription("buha buha buha buha");
            assertNotEquals(request.getName(), addedPoint.getMetadata().getName());
            assertNotEquals(request.getDescription(), addedPoint.getMetadata().getDescription());

            doPatchError(
                    "/games/" + GAME_ID + "/elements/" + addedPoint.getElementId(),
                    request,
                    HttpStatus.BAD_REQUEST,
                    ErrorCodes.INVALID_PARAMETER);
        }

        {
            ElementUpdateRequest request = new ElementUpdateRequest();
            request.setName("Star Points 234");
            request.setDescription("buha buha buha buha");
            request.setVersion(addedPoint.getVersion() + 100);
            assertNotEquals(request.getName(), addedPoint.getMetadata().getName());
            assertNotEquals(request.getDescription(), addedPoint.getMetadata().getDescription());

            doPatchError(
                    "/games/" + GAME_ID + "/elements/" + addedPoint.getElementId(),
                    request,
                    HttpStatus.CONFLICT,
                    ErrorCodes.ELEMENT_UPDATE_CONFLICT);
        }

        var dbElement = callElementGet(GAME_ID, addedPoint.getMetadata().getId(), false);
        assertElement(dbElement, samplePoint, false);
    }

    @Test
    void testReadBySingleType() {
        callElementPost(GAME_ID, samplePoint);
        callElementPost(GAME_ID, sampleBadge);

        List<ElementDef> badgeTypes = doGetListSuccess("/games/{gameId}/elements?types=core:badge" , ElementDef.class, GAME_ID);
        assertEquals(1, badgeTypes.size());
        assertTrue(badgeTypes.stream().anyMatch(t -> t.getElementId().equals(sampleBadge.getMetadata().getId())));

        doGetError("/games/" + GAME_ID + "/elements/non.existing.id?withData=true", HttpStatus.NOT_FOUND, ErrorCodes.ELEMENT_NOT_EXISTS);
    }

    @Test
    void testReadByMultipleType() {
        callElementPost(GAME_ID, samplePoint);
        callElementPost(GAME_ID, sampleBadge);

        List<ElementDef> badgeTypes = doGetListSuccess("/games/{gameId}/elements?types=core:badge,core:point" , ElementDef.class, GAME_ID);
        assertEquals(2, badgeTypes.size());
        assertTrue(badgeTypes.stream().anyMatch(t -> t.getElementId().equals(sampleBadge.getMetadata().getId())));
        assertTrue(badgeTypes.stream().anyMatch(t -> t.getElementId().equals(samplePoint.getMetadata().getId())));
    }

    @Test
    void testReadByMultipleTypeWithDuplicates() {
        callElementPost(GAME_ID, samplePoint);
        callElementPost(GAME_ID, sampleBadge);

        List<ElementDef> badgeTypes = doGetListSuccess("/games/{gameId}/elements?types=core:badge,core:point,core:badge" , ElementDef.class, GAME_ID);
        assertEquals(2, badgeTypes.size());
        assertTrue(badgeTypes.stream().anyMatch(t -> t.getElementId().equals(sampleBadge.getMetadata().getId())));
        assertTrue(badgeTypes.stream().anyMatch(t -> t.getElementId().equals(samplePoint.getMetadata().getId())));
    }

    @Test
    void testReadByMultipleTypesWithUnknownType() {
        callElementPost(GAME_ID, samplePoint);
        callElementPost(GAME_ID, sampleBadge);

        List<ElementDef> badgeTypes = doGetListSuccess("/games/{gameId}/elements?types=core:badge,core:point,unknown:type" , ElementDef.class, GAME_ID);
        assertEquals(2, badgeTypes.size());
        assertTrue(badgeTypes.stream().anyMatch(t -> t.getElementId().equals(sampleBadge.getMetadata().getId())));
        assertTrue(badgeTypes.stream().anyMatch(t -> t.getElementId().equals(samplePoint.getMetadata().getId())));
    }

    @Test
    void testListInGame() {
        GameCreateRequest request = GameCreateRequest.builder()
                .name("sample game")
                .description("description here")
                .build();
        int gameId = doPostSuccess("/games", request, Game.class).getId();

        callElementPost(gameId, samplePoint);
        callElementPost(gameId, sampleBadge);

        List<ElementDef> allElements = doGetListSuccess("/games/{gameId}/elements", ElementDef.class, gameId);
        assertEquals(2, allElements.size());
        assertTrue(allElements.stream().anyMatch(t -> t.getElementId().equals(sampleBadge.getMetadata().getId())));
        assertTrue(allElements.stream().anyMatch(t -> t.getElementId().equals(samplePoint.getMetadata().getId())));

        callElementDelete(gameId, samplePoint.getMetadata().getId());
        assertEquals(1, doGetListSuccess("/games/{gameId}/elements", ElementDef.class, gameId).size());

        doGetListError("/games/{gameId}/elements", HttpStatus.NOT_FOUND, ErrorCodes.GAME_NOT_EXISTS, 999);
    }

    @Test
    void testDelete() {
        doGetError("/games/" + GAME_ID + "/elements/" + samplePoint.getMetadata().getId(), HttpStatus.NOT_FOUND, ErrorCodes.ELEMENT_NOT_EXISTS);
        doGetError("/games/" + GAME_ID + "/elements/" + sampleBadge.getMetadata().getId(), HttpStatus.NOT_FOUND, ErrorCodes.ELEMENT_NOT_EXISTS);

        ElementDef addedElement = callElementPost(GAME_ID, samplePoint);
        System.out.println(addedElement);
        assertNotNull(addedElement.getData());
        assertElement(addedElement, samplePoint, true);

        ElementDef addedBadge = callElementPost(GAME_ID, sampleBadge);
        System.out.println(addedBadge);
        assertNotNull(addedBadge);
        assertElement(addedBadge, sampleBadge, true);

        {
            List<ElementDef> elementsByType = doGetListSuccess("/games/{gameId}/elements?types={type}", ElementDef.class, GAME_ID, samplePoint.getType());
            assertEquals(1, elementsByType.size());
        }

        ElementDef delete = callElementDelete(GAME_ID, samplePoint.getMetadata().getId());
        assertElement(delete, samplePoint, false);

        {
            List<ElementDef> elementsByType = doGetListSuccess("/games/{gameId}/elements?types={type}", ElementDef.class, GAME_ID, samplePoint.getType());
            assertEquals(0, elementsByType.size());
        }
        doGetError("/games/" + GAME_ID + "/elements/" + samplePoint.getMetadata().getId(), HttpStatus.NOT_FOUND, ErrorCodes.ELEMENT_NOT_EXISTS);
    }

    @Test
    void testAddRanks() {
        RankInfo dbGold = doPostSuccess("/games/" + GAME_ID + "/ranks", gold, RankInfo.class);
        assertRank(dbGold, gold);

        doPostError("/games/" + GAME_ID + "/ranks", gold, HttpStatus.BAD_REQUEST, ErrorCodes.RANK_EXISTS);

        // can add same rank to different game
        doPostSuccess("/games/2/ranks", gold, RankInfo.class);
    }

    @Test
    void testAddRanksValidations() {
        doPostError("/games/" + GAME_ID + "/ranks",
                gold.toBuilder().name(null).build(),
                HttpStatus.BAD_REQUEST, ErrorCodes.INVALID_PARAMETER);

        doPostError("/games/" + GAME_ID + "/ranks",
                gold.toBuilder().name("").build(),
                HttpStatus.BAD_REQUEST, ErrorCodes.INVALID_PARAMETER);

        doPostError("/games/" + GAME_ID + "/ranks",
                gold.toBuilder().name(RandomStringUtils.randomAscii(33)).build(),
                HttpStatus.BAD_REQUEST, ErrorCodes.INVALID_PARAMETER);
    }

    @Test
    void testListRanks() {
        doPostSuccess("/games/" + GAME_ID + "/ranks", gold, RankInfo.class);
        doPostSuccess("/games/" + GAME_ID + "/ranks", silver, RankInfo.class);
        doPostSuccess("/games/" + GAME_ID + "/ranks", bronze, RankInfo.class);
        doPostSuccess("/games/2/ranks", gold, RankInfo.class);
        doPostSuccess("/games/2/ranks", silver, RankInfo.class);

        {
            List<RankInfo> rankInfos = doGetListSuccess("/games/{gameId}/ranks", RankInfo.class, GAME_ID);
            assertEquals(3, rankInfos.size());
            List<String> attrNames = rankInfos.stream().map(RankInfo::getName).collect(Collectors.toList());
            assertTrue(attrNames.contains(gold.getName()));
            assertTrue(attrNames.contains(silver.getName()));
            assertTrue(attrNames.contains(bronze.getName()));
        }

        {
            List<RankInfo> rankInfos = doGetListSuccess("/games/{gameId}/ranks", RankInfo.class, 2);
            assertEquals(2, rankInfos.size());
            List<String> attrNames = rankInfos.stream().map(RankInfo::getName).collect(Collectors.toList());
            assertTrue(attrNames.contains(gold.getName()));
            assertTrue(attrNames.contains(silver.getName()));
            assertFalse(attrNames.contains(bronze.getName()));
        }
    }

    private ElementDef callElementPost(int gameId, ElementCreateRequest request) {
        return doPostSuccess("/games/" + gameId + "/elements", request, ElementDef.class);
    }

    private ElementDef callElementPatch(int gameId, String elementId, ElementUpdateRequest request) {
        return doPatchSuccess("/games/" + gameId + "/elements/" + elementId, request, ElementDef.class);
    }

    private ElementDef callElementDelete(int gameId, String elementId) {
        return doDeleteSuccess("/games/" + gameId + "/elements/" + elementId, ElementDef.class);
    }

    private ElementDef callElementGet(int gameId, String elementId, boolean withData) {
        String qParams = "";
        if (withData) {
            qParams = "?withData=true";
        }
        return doGetSuccess("/games/" + gameId + "/elements/" + elementId + qParams, ElementDef.class);
    }

    private void assertRank(RankInfo db, RankCreationRequest other) {
        assertTrue(db.getId() > 0);
        assertEquals(other.getColorCode(), db.getColorCode());
        assertEquals(other.getName(), db.getName());
        assertEquals(other.getPriority(), db.getPriority());
    }

    private void assertElement(ElementDef db, ElementCreateRequest other, boolean withData) {
        assertTrue(db.getId() > 0);
        assertEquals(other.getMetadata().getId(), db.getElementId());
        assertEquals(other.getType(), db.getType());
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
