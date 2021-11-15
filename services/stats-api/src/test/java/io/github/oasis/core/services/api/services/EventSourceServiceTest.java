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

import io.github.oasis.core.exception.OasisException;
import io.github.oasis.core.model.EventSource;
import io.github.oasis.core.services.api.exceptions.DataValidationException;
import io.github.oasis.core.services.api.exceptions.ErrorCodes;
import io.github.oasis.core.services.api.exceptions.OasisApiRuntimeException;
import io.github.oasis.core.services.api.services.impl.EventSourceService;
import io.github.oasis.core.services.api.to.EventSourceCreateRequest;
import io.github.oasis.core.services.api.to.EventSourceKeysResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Objects;
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
public class EventSourceServiceTest extends AbstractServiceTest {

    @Autowired
    private EventSourceService eventSourceService;

    @Test
    void testRegisterEventSource() throws OasisException {
        EventSourceCreateRequest source = new EventSourceCreateRequest("test-1");
        EventSource dbSource = eventSourceService.registerEventSource(source);
        System.out.println(dbSource);
        assertSource(dbSource, source, true);

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> eventSourceService.registerEventSource(source))
                .isInstanceOf(OasisApiRuntimeException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCodes.EVENT_SOURCE_ALREADY_EXISTS);

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> eventSourceService.registerEventSource(new EventSourceCreateRequest("")))
                .isInstanceOf(DataValidationException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCodes.EVENT_SOURCE_NO_NAME);
    }

    @Test
    void testDownloadEventSourceKeys() throws OasisException {
        EventSourceCreateRequest source = new EventSourceCreateRequest("test-1");
        EventSource dbSource = eventSourceService.registerEventSource(source);
        System.out.println(dbSource);
        assertSource(dbSource, source, true);

        EventSourceKeysResponse keyset = eventSourceService.downloadEventSourceKeys(dbSource.getId());
        assertNotNull(keyset);
        assertEquals(dbSource.getSecrets().getPrivateKey(), keyset.getPrivateKeyB64Encoded());

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> eventSourceService.downloadEventSourceKeys(dbSource.getId()))
                .isInstanceOf(OasisApiRuntimeException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCodes.EVENT_SOURCE_DOWNLOAD_LIMIT_EXCEEDED);
    }

    @Test
    void testReadEventSourceInfo() throws OasisException {
        EventSourceCreateRequest source = new EventSourceCreateRequest("test-1");
        EventSource dbSource = eventSourceService.registerEventSource(source);
        System.out.println(dbSource);
        assertSource(dbSource, source, true);

        EventSource eventSource = eventSourceService.readEventSource(dbSource.getId());
        System.out.println(eventSource);
        assertNull(eventSource.getSecrets());
        assertSource(eventSource, dbSource, false);

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> eventSourceService.readEventSource(dbSource.getId() + 500))
                .isInstanceOf(OasisApiRuntimeException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCodes.EVENT_SOURCE_NOT_EXISTS);
    }

    @Test
    void testReadEventSourceInfoWithKeys() throws OasisException {
        EventSourceCreateRequest source = new EventSourceCreateRequest("test-1");
        EventSource dbSource = eventSourceService.registerEventSource(source);
        eventSourceService.assignEventSourceToGame(dbSource.getId(), 1);
        System.out.println(dbSource);
        assertSource(dbSource, source, true);

        EventSource eventSource = eventSourceService.readEventSourceByToken(dbSource.getToken());
        System.out.println(eventSource);
        assertNotNull(eventSource.getSecrets());
        assertNotNull(eventSource.getSecrets().getPublicKey());
        assertNull(eventSource.getSecrets().getPrivateKey());
        Assertions.assertEquals(1, eventSource.getGames().size());
        Assertions.assertTrue(eventSource.getGames().contains(1));

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> eventSourceService.readEventSourceByToken("unknown token"))
                .isInstanceOf(OasisApiRuntimeException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCodes.EVENT_SOURCE_NOT_EXISTS);
    }

    @Test
    void testListAllEventSources() throws OasisException {
        assertEquals(0, eventSourceService.listAllEventSources().size());

        EventSourceCreateRequest src1 = EventSourceCreateRequest.builder().name("test-1").build();
        EventSourceCreateRequest src2 = EventSourceCreateRequest.builder().name("test-2").build();
        EventSourceCreateRequest src3 = EventSourceCreateRequest.builder().name("test-3").build();
        eventSourceService.registerEventSource(src1);

        assertEquals(1, eventSourceService.listAllEventSources().size());
        eventSourceService.registerEventSource(src2);
        eventSourceService.registerEventSource(src3);

        assertEquals(3, eventSourceService.listAllEventSources().size());
        assertThrows(OasisApiRuntimeException.class, () -> eventSourceService.registerEventSource(src2));
        assertEquals(3, eventSourceService.listAllEventSources().size());

        List<EventSource> allSrc = eventSourceService.listAllEventSources();
        assertEquals(3, (int) allSrc.stream().filter(s -> Objects.isNull(s.getSecrets())).count());
    }

    @Test
    void testDeleteEventSources() throws OasisException {
        assertEquals(0, eventSourceService.listAllEventSources().size());

        EventSourceCreateRequest src1 = EventSourceCreateRequest.builder().name("test-1").build();
        EventSourceCreateRequest src2 = EventSourceCreateRequest.builder().name("test-2").build();
        EventSourceCreateRequest src3 = EventSourceCreateRequest.builder().name("test-3").build();
        int id1 = eventSourceService.registerEventSource(src1).getId();
        int id2 = eventSourceService.registerEventSource(src2).getId();
        int id3 = eventSourceService.registerEventSource(src3).getId();

        assertEquals(3, eventSourceService.listAllEventSources().size());

        eventSourceService.deleteEventSource(id1);

        assertEquals(2, eventSourceService.listAllEventSources().size());

        eventSourceService.deleteEventSource(id2);
        eventSourceService.deleteEventSource(id3);

        assertEquals(0, eventSourceService.listAllEventSources().size());
    }

    @Test
    void testRegisterSourcesToGame() throws OasisException {
        assertEquals(0, eventSourceService.listAllEventSources().size());

        EventSourceCreateRequest src1 = EventSourceCreateRequest.builder().name("test-1").build();
        EventSourceCreateRequest src2 = EventSourceCreateRequest.builder().name("test-2").build();
        EventSourceCreateRequest src3 = EventSourceCreateRequest.builder().name("test-3").build();
        int id1 = eventSourceService.registerEventSource(src1).getId();
        int id2 = eventSourceService.registerEventSource(src2).getId();
        int id3 = eventSourceService.registerEventSource(src3).getId();

        assertEquals(3, eventSourceService.listAllEventSources().size());

        eventSourceService.assignEventSourceToGame(id1, 1);
        eventSourceService.assignEventSourceToGame(id2, 1);
        eventSourceService.assignEventSourceToGame(id3, 1);

        eventSourceService.assignEventSourceToGame(id2, 2);
        eventSourceService.assignEventSourceToGame(id1, 2);

        List<EventSource> game1Sources = eventSourceService.listAllEventSourcesOfGame(1);
        assertEquals(3, game1Sources.size());
        List<String> game1Names = game1Sources.stream().map(EventSource::getName).collect(Collectors.toList());
        assertTrue(game1Names.contains(src1.getName()));
        assertTrue(game1Names.contains(src2.getName()));
        assertTrue(game1Names.contains(src3.getName()));

        List<EventSource> game2Sources = eventSourceService.listAllEventSourcesOfGame(2);
        assertEquals(2, game2Sources.size());
        List<String> game2Names = game2Sources.stream().map(EventSource::getName).collect(Collectors.toList());
        assertTrue(game2Names.contains(src1.getName()));
        assertTrue(game2Names.contains(src2.getName()));
        assertFalse(game2Names.contains(src3.getName()));

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> eventSourceService.assignEventSourceToGame(id1, 1))
                .isInstanceOf(OasisApiRuntimeException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCodes.EVENT_SOURCE_ALREADY_MAPPED);
    }

    @Test
    void testDeRegisterSourcesToGame() throws OasisException {
        assertEquals(0, eventSourceService.listAllEventSources().size());

        EventSourceCreateRequest src1 = EventSourceCreateRequest.builder().name("test-1").build();
        EventSourceCreateRequest src2 = EventSourceCreateRequest.builder().name("test-2").build();
        EventSourceCreateRequest src3 = EventSourceCreateRequest.builder().name("test-3").build();
        int id1 = eventSourceService.registerEventSource(src1).getId();
        int id2 = eventSourceService.registerEventSource(src2).getId();
        int id3 = eventSourceService.registerEventSource(src3).getId();

        assertEquals(3, eventSourceService.listAllEventSources().size());

        eventSourceService.assignEventSourceToGame(id1, 1);
        eventSourceService.assignEventSourceToGame(id2, 1);
        eventSourceService.assignEventSourceToGame(id3, 1);

        eventSourceService.assignEventSourceToGame(id2, 2);
        eventSourceService.assignEventSourceToGame(id1, 2);

        assertEquals(3, eventSourceService.listAllEventSourcesOfGame(1).size());

        eventSourceService.removeEventSourceFromGame(id2, 1);
        {
            List<EventSource> game1Sources = eventSourceService.listAllEventSourcesOfGame(1);
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
