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
import io.github.oasis.core.model.EventSource;
import io.github.oasis.core.services.api.exceptions.ErrorCodes;
import io.github.oasis.core.services.api.handlers.CacheClearanceListener;
import io.github.oasis.core.services.api.handlers.events.BaseEventSourceChangedEvent;
import io.github.oasis.core.services.api.handlers.events.EntityChangeType;
import io.github.oasis.core.services.api.to.EventSourceCreateRequest;
import io.github.oasis.core.services.api.to.EventSourceKeysResponse;
import io.github.oasis.core.services.api.to.GameCreateRequest;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Isuru Weerarathna
 */
public class EventSourceServiceTest extends AbstractServiceTest {

    @SpyBean
    private CacheClearanceListener cacheClearanceListener;

    private final GameCreateRequest stackOverflow = GameCreateRequest.builder()
            .name("Stack-overflow")
            .description("Stackoverflow badges and points system")
            .logoRef("https://oasis.io/assets/so.jpeg")
            .motto("Help the community")
            .build();

    private final GameCreateRequest promotions = GameCreateRequest.builder()
            .name("Promotions")
            .description("Provides promotions for customers based on their loyality")
            .logoRef("https://oasis.io/assets/pm.jpeg")
            .motto("Serve your customers")
            .build();

    @Test
    void testRegisterEventSource() {
        EventSourceCreateRequest source = new EventSourceCreateRequest("test-1");
        EventSource dbSource = doPostSuccess("/admin/event-sources", source, EventSource.class);
        System.out.println(dbSource);
        assertSource(dbSource, source, true);

        doPostError("/admin/event-sources", source, HttpStatus.BAD_REQUEST, ErrorCodes.EVENT_SOURCE_ALREADY_EXISTS);
    }

    @Test
    void testRegisterEventSourceValidations() {
        EventSourceCreateRequest source = new EventSourceCreateRequest("test-1");
        doPostSuccess("/admin/event-sources", source, EventSource.class);

        doPostError("/admin/event-sources", new EventSourceCreateRequest(""), HttpStatus.BAD_REQUEST, ErrorCodes.INVALID_PARAMETER);
        doPostError("/admin/event-sources", new EventSourceCreateRequest(null), HttpStatus.BAD_REQUEST, ErrorCodes.INVALID_PARAMETER);
        doPostError("/admin/event-sources", new EventSourceCreateRequest(RandomStringUtils.randomAscii(65)), HttpStatus.BAD_REQUEST, ErrorCodes.INVALID_PARAMETER);

        doPostError("/admin/event-sources", new EventSourceCreateRequest("0numberstart"), HttpStatus.BAD_REQUEST, ErrorCodes.INVALID_PARAMETER);
        doPostError("/admin/event-sources", new EventSourceCreateRequest("invalidchar@#"), HttpStatus.BAD_REQUEST, ErrorCodes.INVALID_PARAMETER);
        doPostError("/admin/event-sources", new EventSourceCreateRequest("with space"), HttpStatus.BAD_REQUEST, ErrorCodes.INVALID_PARAMETER);
    }

    @Test
    void testDownloadEventSourceKeys() {
        EventSourceCreateRequest source = new EventSourceCreateRequest("test-1");
        EventSource dbSource = doPostSuccess("/admin/event-sources", source, EventSource.class);
        System.out.println(dbSource);
        assertSource(dbSource, source, true);

        EventSourceKeysResponse keyset = doGetSuccess("/admin/event-sources/" + dbSource.getId() + "/keys", EventSourceKeysResponse.class);
        assertNotNull(keyset);
        assertEquals(dbSource.getSecrets().getPrivateKey(), keyset.getPrivateKeyB64Encoded());

        doGetError("/admin/event-sources/" + dbSource.getId() + "/keys", HttpStatus.FORBIDDEN, ErrorCodes.EVENT_SOURCE_DOWNLOAD_LIMIT_EXCEEDED);
    }

    @Test
    void testReadEventSourceInfo() {
        EventSourceCreateRequest source = new EventSourceCreateRequest("test-1");
        EventSource dbSource = doPostSuccess("/admin/event-sources", source, EventSource.class);
        System.out.println(dbSource);
        assertSource(dbSource, source, true);

        EventSource eventSource = doGetSuccess("/admin/event-sources/" + dbSource.getId(), EventSource.class);
        System.out.println(eventSource);
        assertNull(eventSource.getSecrets());
        assertSource(eventSource, dbSource, false);

        doGetError("/admin/event-sources/" + dbSource.getId() + 500, HttpStatus.NOT_FOUND, ErrorCodes.EVENT_SOURCE_NOT_EXISTS);
    }

    @Test
    void testReadEventSourceInfoWithKeys() {
        EventSourceCreateRequest source = new EventSourceCreateRequest("test-1");

        int gameId1 = doPostSuccess("/games", stackOverflow, Game.class).getId();

        EventSource dbSource = doPostSuccess("/admin/event-sources", source, EventSource.class);
        doPostSuccess("/admin/games/" + gameId1 + "/event-sources/" + dbSource.getId(), null, null);
        System.out.println(dbSource);
        assertSource(dbSource, source, true);

        EventSource eventSource = doGetSuccess("/admin/event-source?token=" + dbSource.getToken(), EventSource.class);
        System.out.println(eventSource);
        assertNotNull(eventSource.getSecrets());
        assertNotNull(eventSource.getSecrets().getPublicKey());
        assertNull(eventSource.getSecrets().getPrivateKey());
        Assertions.assertEquals(1, eventSource.getGames().size());
        Assertions.assertTrue(eventSource.getGames().contains(gameId1));

        doGetError("/admin/event-source?token=unknown", HttpStatus.NOT_FOUND, ErrorCodes.EVENT_SOURCE_NOT_EXISTS);
    }

    @Test
    void testListAllEventSources() {
        assertEquals(0, doGetListSuccess("/admin/event-sources", EventSource.class).size());

        EventSourceCreateRequest src1 = EventSourceCreateRequest.builder().name("test-1").build();
        EventSourceCreateRequest src2 = EventSourceCreateRequest.builder().name("test-2").build();
        EventSourceCreateRequest src3 = EventSourceCreateRequest.builder().name("test-3").build();
        doPostSuccess("/admin/event-sources", src1, EventSource.class);

        assertEquals(1, doGetListSuccess("/admin/event-sources", EventSource.class).size());
        doPostSuccess("/admin/event-sources", src2, EventSource.class);
        doPostSuccess("/admin/event-sources", src3, EventSource.class);

        assertEquals(3, doGetListSuccess("/admin/event-sources", EventSource.class).size());
        doPostError("/admin/event-sources", src2, HttpStatus.BAD_REQUEST, ErrorCodes.EVENT_SOURCE_ALREADY_EXISTS);
        assertEquals(3, doGetListSuccess("/admin/event-sources", EventSource.class).size());

        List<EventSource> allSrc = doGetListSuccess("/admin/event-sources", EventSource.class);
        assertEquals(3, (int) allSrc.stream().filter(s -> Objects.isNull(s.getSecrets())).count());
    }

    @Test
    void testDeleteEventSources() {
        assertEquals(0, doGetListSuccess("/admin/event-sources", EventSource.class).size());

        EventSourceCreateRequest src1 = EventSourceCreateRequest.builder().name("test-1").build();
        EventSourceCreateRequest src2 = EventSourceCreateRequest.builder().name("test-2").build();
        EventSourceCreateRequest src3 = EventSourceCreateRequest.builder().name("test-3").build();
        EventSource eventSource = doPostSuccess("/admin/event-sources", src1, EventSource.class);
        int id1 = eventSource.getId();
        int id2 = doPostSuccess("/admin/event-sources", src2, EventSource.class).getId();
        int id3 = doPostSuccess("/admin/event-sources", src3, EventSource.class).getId();

        assertEquals(3, doGetListSuccess("/admin/event-sources", EventSource.class).size());

        doDeleteSuccess("/admin/event-sources/" + id1, null);
        assertOnceCacheClearanceCalled(eventSource.getToken(), EntityChangeType.REMOVED);

        assertEquals(2, doGetListSuccess("/admin/event-sources", EventSource.class).size());

        doDeleteSuccess("/admin/event-sources/" + id2, null);
        doDeleteSuccess("/admin/event-sources/" + id3, null);

        assertEquals(0, doGetListSuccess("/admin/event-sources", EventSource.class).size());

        Mockito.reset(cacheClearanceListener);
        doDeletetError("/admin/event-sources/99999999", HttpStatus.BAD_REQUEST, ErrorCodes.EVENT_SOURCE_NOT_EXISTS);
        assertNeverCacheClearanceCalled();
    }

    @Test
    void testRegisterSourcesToGame() {
        assertEquals(0, doGetListSuccess("/admin/event-sources", EventSource.class).size());
        int gameId1 = doPostSuccess("/games", stackOverflow, Game.class).getId();
        int gameId2 = doPostSuccess("/games", promotions, Game.class).getId();

        EventSourceCreateRequest src1 = EventSourceCreateRequest.builder().name("test-1").build();
        EventSourceCreateRequest src2 = EventSourceCreateRequest.builder().name("test-2").build();
        EventSourceCreateRequest src3 = EventSourceCreateRequest.builder().name("test-3").build();
        EventSource eventSource = doPostSuccess("/admin/event-sources", src1, EventSource.class);
        int id1 = eventSource.getId();
        int id2 = doPostSuccess("/admin/event-sources", src2, EventSource.class).getId();
        int id3 = doPostSuccess("/admin/event-sources", src3, EventSource.class).getId();

        assertEquals(3, doGetListSuccess("/admin/event-sources", EventSource.class).size());

        Mockito.reset(cacheClearanceListener);
        doPostSuccess("/admin/games/" + gameId1 + "/event-sources/" + id1, null, null);
        assertOnceCacheClearanceCalled(eventSource.getToken(), EntityChangeType.MODIFIED);
        doPostSuccess("/admin/games/" + gameId1 + "/event-sources/" + id2, null, null);
        doPostSuccess("/admin/games/" + gameId1 + "/event-sources/" + id3, null, null);

        doPostSuccess("/admin/games/" + gameId2 + "/event-sources/" + id2, null, null);
        doPostSuccess("/admin/games/" + gameId2 + "/event-sources/" + id1, null, null);

        List<EventSource> game1Sources = doGetListSuccess("/admin/games/" + gameId1 + "/event-sources", EventSource.class);
        assertEquals(3, game1Sources.size());
        List<String> game1Names = game1Sources.stream().map(EventSource::getName).collect(Collectors.toList());
        assertTrue(game1Names.contains(src1.getName()));
        assertTrue(game1Names.contains(src2.getName()));
        assertTrue(game1Names.contains(src3.getName()));

        List<EventSource> game2Sources = doGetListSuccess("/admin/games/" + gameId2 + "/event-sources", EventSource.class);
        assertEquals(2, game2Sources.size());
        List<String> game2Names = game2Sources.stream().map(EventSource::getName).collect(Collectors.toList());
        assertTrue(game2Names.contains(src1.getName()));
        assertTrue(game2Names.contains(src2.getName()));
        assertFalse(game2Names.contains(src3.getName()));

        Mockito.reset(cacheClearanceListener);
        doPostError("/admin/games/" + gameId1 + "/event-sources/" + id1, null, HttpStatus.BAD_REQUEST, ErrorCodes.EVENT_SOURCE_ALREADY_MAPPED);
        assertNeverCacheClearanceCalled();
    }

    @Test
    void testRegisterSourcesFailures() {
        assertEquals(0, doGetListSuccess("/admin/event-sources", EventSource.class).size());
        int gameId1 = doPostSuccess("/games", stackOverflow, Game.class).getId();

        EventSourceCreateRequest src1 = EventSourceCreateRequest.builder().name("test-1").build();
        int id1 = doPostSuccess("/admin/event-sources", src1, EventSource.class).getId();

        // assignment failure in non existing games
        Mockito.reset(cacheClearanceListener);
        doPostError("/admin/games/999999/event-sources/" + id1, null, HttpStatus.NOT_FOUND, ErrorCodes.GAME_NOT_EXISTS);
        assertNeverCacheClearanceCalled();

        // assignment failure in non-existing event source ids
        Mockito.reset(cacheClearanceListener);
        doPostError("/admin/games/" + gameId1 + "/event-sources/999999", null, HttpStatus.NOT_FOUND, ErrorCodes.EVENT_SOURCE_NOT_EXISTS);
        assertNeverCacheClearanceCalled();
    }

    @Test
    void testDeRegisterSourcesFromGame() {
        assertEquals(0, doGetListSuccess("/admin/event-sources", EventSource.class).size());
        int gameId1 = doPostSuccess("/games", stackOverflow, Game.class).getId();
        int gameId2 = doPostSuccess("/games", promotions, Game.class).getId();

        EventSourceCreateRequest src1 = EventSourceCreateRequest.builder().name("test-1").build();
        EventSourceCreateRequest src2 = EventSourceCreateRequest.builder().name("test-2").build();
        EventSourceCreateRequest src3 = EventSourceCreateRequest.builder().name("test-3").build();
        int id1 = doPostSuccess("/admin/event-sources", src1, EventSource.class).getId();
        EventSource eventSource = doPostSuccess("/admin/event-sources", src2, EventSource.class);
        int id2 = eventSource.getId();
        int id3 = doPostSuccess("/admin/event-sources", src3, EventSource.class).getId();

        assertEquals(3, doGetListSuccess("/admin/event-sources", EventSource.class).size());

        doPostSuccess("/admin/games/" + gameId1 + "/event-sources/" + id1, null, null);
        doPostSuccess("/admin/games/" + gameId1 + "/event-sources/" + id2, null, null);
        doPostSuccess("/admin/games/" + gameId1 + "/event-sources/" + id3, null, null);

        doPostSuccess("/admin/games/" + gameId2 + "/event-sources/" + id2, null, null);
        doPostSuccess("/admin/games/" + gameId2 + "/event-sources/" + id1, null, null);

        assertEquals(3, doGetListSuccess("/admin/games/" + gameId1 + "/event-sources", EventSource.class).size());

        Mockito.reset(cacheClearanceListener);
        doDeleteSuccess("/admin/games/" + gameId1 + "/event-sources/" + id2, null);
        assertOnceCacheClearanceCalled(eventSource.getToken(), EntityChangeType.MODIFIED);
        {
            List<EventSource> game1Sources = doGetListSuccess("/admin/games/" + gameId1 + "/event-sources", EventSource.class);
            assertEquals(2, game1Sources.size());
            List<String> game1Names = game1Sources.stream().map(EventSource::getName).collect(Collectors.toList());
            assertTrue(game1Names.contains(src1.getName()));
            assertFalse(game1Names.contains(src2.getName()));
            assertTrue(game1Names.contains(src3.getName()));
        }
    }

    @Test
    void testDeleteSourcesFailures() {
        assertEquals(0, doGetListSuccess("/admin/event-sources", EventSource.class).size());
        int gameId1 = doPostSuccess("/games", stackOverflow, Game.class).getId();

        EventSourceCreateRequest src1 = EventSourceCreateRequest.builder().name("test-1").build();
        int id1 = doPostSuccess("/admin/event-sources", src1, EventSource.class).getId();

        // delete in non-existing games
        Mockito.reset(cacheClearanceListener);
        doDeletetError("/admin/games/999999/event-sources/" + id1, HttpStatus.NOT_FOUND, ErrorCodes.GAME_NOT_EXISTS);
        assertNeverCacheClearanceCalled();

        // delete in non-existing games
        Mockito.reset(cacheClearanceListener);
        doDeletetError("/admin/games/" + gameId1 + "/event-sources/9999999", HttpStatus.NOT_FOUND, ErrorCodes.EVENT_SOURCE_NOT_EXISTS);
        assertNeverCacheClearanceCalled();
    }

    private void assertSource(EventSource db, EventSourceCreateRequest other, boolean withKeys) {
        assertTrue(db.getId() > 0);
        assertEquals(other.getName(), db.getName());
        assertTrue(db.getToken().length() > 0);
        if (withKeys) {
            assertNotNull(db.getSecrets());
            assertTrue(db.getSecrets().isValid());
        } else {
            assertNull(db.getSecrets());
        }
    }

    private void assertSource(EventSource db, EventSource other, boolean withKeys) {
        assertTrue(db.getId() > 0);
        assertEquals(other.getName(), db.getName());
        assertTrue(db.getToken().length() > 0);
        if (withKeys) {
            assertNotNull(db.getSecrets());
            assertTrue(db.getSecrets().isValid());
        } else {
            assertNull(db.getSecrets());
        }
    }

    private void assertOnceCacheClearanceCalled(String token, EntityChangeType changeType) {
        ArgumentCaptor<BaseEventSourceChangedEvent> captor = ArgumentCaptor.forClass(BaseEventSourceChangedEvent.class);

        Mockito.verify(cacheClearanceListener, Mockito.times(1))
                .handleEventSourceUpdateEvent(captor.capture());

        Assertions.assertEquals(token, captor.getValue().getToken());
        Assertions.assertEquals(changeType, captor.getValue().getChangeType());
        Mockito.reset(cacheClearanceListener);
    }

    private void assertNeverCacheClearanceCalled() {
        Mockito.verify(cacheClearanceListener, Mockito.never())
                .handleEventSourceUpdateEvent(Mockito.any(BaseEventSourceChangedEvent.class));
        Mockito.reset(cacheClearanceListener);
    }
}
