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

import io.github.oasis.core.model.EventSource;
import io.github.oasis.core.services.api.exceptions.ErrorCodes;
import io.github.oasis.core.services.api.to.EventSourceCreateRequest;
import io.github.oasis.core.services.api.to.EventSourceKeysResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Isuru Weerarathna
 */
public class EventSourceServiceTest extends AbstractServiceTest {

    @Test
    void testRegisterEventSource() {
        EventSourceCreateRequest source = new EventSourceCreateRequest("test-1");
        EventSource dbSource = doPostSuccess("/admin/event-sources", source, EventSource.class);
        System.out.println(dbSource);
        assertSource(dbSource, source, true);

        doPostError("/admin/event-sources", source, HttpStatus.BAD_REQUEST, ErrorCodes.EVENT_SOURCE_ALREADY_EXISTS);
        doPostError("/admin/event-sources", new EventSourceCreateRequest(""), HttpStatus.BAD_REQUEST, ErrorCodes.EVENT_SOURCE_NO_NAME);
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

        doGetError("/admin/event-sources/" + dbSource.getId() + "/keys", HttpStatus.INTERNAL_SERVER_ERROR, ErrorCodes.EVENT_SOURCE_DOWNLOAD_LIMIT_EXCEEDED);
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
        EventSource dbSource = doPostSuccess("/admin/event-sources", source, EventSource.class);
        doPostSuccess("/admin/games/1/event-sources/" + dbSource.getId(), null, null);
        System.out.println(dbSource);
        assertSource(dbSource, source, true);

        EventSource eventSource = doGetSuccess("/admin/event-source?token=" + dbSource.getToken(), EventSource.class);
        System.out.println(eventSource);
        assertNotNull(eventSource.getSecrets());
        assertNotNull(eventSource.getSecrets().getPublicKey());
        assertNull(eventSource.getSecrets().getPrivateKey());
        Assertions.assertEquals(1, eventSource.getGames().size());
        Assertions.assertTrue(eventSource.getGames().contains(1));

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
        int id1 = doPostSuccess("/admin/event-sources", src1, EventSource.class).getId();
        int id2 = doPostSuccess("/admin/event-sources", src2, EventSource.class).getId();
        int id3 = doPostSuccess("/admin/event-sources", src3, EventSource.class).getId();

        assertEquals(3, doGetListSuccess("/admin/event-sources", EventSource.class).size());

        doDeleteSuccess("/admin/event-sources/" + id1, null);

        assertEquals(2, doGetListSuccess("/admin/event-sources", EventSource.class).size());

        doDeleteSuccess("/admin/event-sources/" + id2, null);
        doDeleteSuccess("/admin/event-sources/" + id3, null);

        assertEquals(0, doGetListSuccess("/admin/event-sources", EventSource.class).size());
    }

    @Test
    void testRegisterSourcesToGame() {
        assertEquals(0, doGetListSuccess("/admin/event-sources", EventSource.class).size());

        EventSourceCreateRequest src1 = EventSourceCreateRequest.builder().name("test-1").build();
        EventSourceCreateRequest src2 = EventSourceCreateRequest.builder().name("test-2").build();
        EventSourceCreateRequest src3 = EventSourceCreateRequest.builder().name("test-3").build();
        int id1 = doPostSuccess("/admin/event-sources", src1, EventSource.class).getId();
        int id2 = doPostSuccess("/admin/event-sources", src2, EventSource.class).getId();
        int id3 = doPostSuccess("/admin/event-sources", src3, EventSource.class).getId();

        assertEquals(3, doGetListSuccess("/admin/event-sources", EventSource.class).size());

        doPostSuccess("/admin/games/1/event-sources/" + id1, null, null);
        doPostSuccess("/admin/games/1/event-sources/" + id2, null, null);
        doPostSuccess("/admin/games/1/event-sources/" + id3, null, null);

        doPostSuccess("/admin/games/2/event-sources/" + id2, null, null);
        doPostSuccess("/admin/games/2/event-sources/" + id1, null, null);

        List<EventSource> game1Sources = doGetListSuccess("/admin/games/1/event-sources", EventSource.class);
        assertEquals(3, game1Sources.size());
        List<String> game1Names = game1Sources.stream().map(EventSource::getName).collect(Collectors.toList());
        assertTrue(game1Names.contains(src1.getName()));
        assertTrue(game1Names.contains(src2.getName()));
        assertTrue(game1Names.contains(src3.getName()));

        List<EventSource> game2Sources = doGetListSuccess("/admin/games/2/event-sources", EventSource.class);
        assertEquals(2, game2Sources.size());
        List<String> game2Names = game2Sources.stream().map(EventSource::getName).collect(Collectors.toList());
        assertTrue(game2Names.contains(src1.getName()));
        assertTrue(game2Names.contains(src2.getName()));
        assertFalse(game2Names.contains(src3.getName()));

        doPostError("/admin/games/1/event-sources/" + id1, null, HttpStatus.INTERNAL_SERVER_ERROR, ErrorCodes.EVENT_SOURCE_ALREADY_MAPPED);
    }

    @Test
    void testDeRegisterSourcesFromGame() {
        assertEquals(0, doGetListSuccess("/admin/event-sources", EventSource.class).size());

        EventSourceCreateRequest src1 = EventSourceCreateRequest.builder().name("test-1").build();
        EventSourceCreateRequest src2 = EventSourceCreateRequest.builder().name("test-2").build();
        EventSourceCreateRequest src3 = EventSourceCreateRequest.builder().name("test-3").build();
        int id1 = doPostSuccess("/admin/event-sources", src1, EventSource.class).getId();
        int id2 = doPostSuccess("/admin/event-sources", src2, EventSource.class).getId();
        int id3 = doPostSuccess("/admin/event-sources", src3, EventSource.class).getId();

        assertEquals(3, doGetListSuccess("/admin/event-sources", EventSource.class).size());

        doPostSuccess("/admin/games/1/event-sources/" + id1, null, null);
        doPostSuccess("/admin/games/1/event-sources/" + id2, null, null);
        doPostSuccess("/admin/games/1/event-sources/" + id3, null, null);

        doPostSuccess("/admin/games/2/event-sources/" + id2, null, null);
        doPostSuccess("/admin/games/2/event-sources/" + id1, null, null);

        assertEquals(3, doGetListSuccess("/admin/games/1/event-sources", EventSource.class).size());

        doDeleteSuccess("/admin/games/1/event-sources/" + id2, null);
        {
            List<EventSource> game1Sources = doGetListSuccess("/admin/games/1/event-sources", EventSource.class);
            assertEquals(2, game1Sources.size());
            List<String> game1Names = game1Sources.stream().map(EventSource::getName).collect(Collectors.toList());
            assertTrue(game1Names.contains(src1.getName()));
            assertFalse(game1Names.contains(src2.getName()));
            assertTrue(game1Names.contains(src3.getName()));
        }

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

}
