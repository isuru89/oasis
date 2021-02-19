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

package io.github.oasis.core.services.api;

import com.mysql.cj.exceptions.AssertionFailedException;
import io.github.oasis.core.exception.OasisException;
import io.github.oasis.core.model.EventSource;
import io.github.oasis.core.services.api.beans.BackendRepository;
import io.github.oasis.core.services.api.beans.KeyGeneratorHelper;
import io.github.oasis.core.services.api.beans.jdbc.JdbcRepository;
import io.github.oasis.core.services.api.controllers.admin.EventSourceController;
import io.github.oasis.core.services.api.dao.IEventSourceDao;
import io.github.oasis.core.services.api.dao.IGameDao;
import io.github.oasis.core.services.api.exceptions.DataValidationException;
import io.github.oasis.core.services.api.exceptions.ErrorCodes;
import io.github.oasis.core.services.api.exceptions.OasisApiRuntimeException;
import io.github.oasis.core.services.api.services.EventSourceService;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.Test;

import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Isuru Weerarathna
 */
public class EventSourceServiceTest extends AbstractServiceTest {

    private EventSourceController esController;
    private final KeyGeneratorHelper keyGeneratorSupport = new KeyGeneratorHelper();

    public EventSourceServiceTest() {
        try {
            keyGeneratorSupport.init();
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionFailedException("Cannot initialize key generator!");
        }
    }

    @Test
    void testRegisterEventSource() throws OasisException {
        EventSource source = EventSource.builder().name("test-1").build();
        EventSource dbSource = esController.registerEventSource(source);
        System.out.println(dbSource);
        assertSource(dbSource, source, true);

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> esController.registerEventSource(source))
                .isInstanceOf(OasisApiRuntimeException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCodes.EVENT_SOURCE_ALREADY_EXISTS);

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> esController.registerEventSource(EventSource.builder().name("").build()))
                .isInstanceOf(DataValidationException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCodes.EVENT_SOURCE_NO_NAME);
    }

    @Test
    void testListAllEventSources() throws OasisException {
        assertEquals(0, esController.getAllEventSources().size());

        EventSource src1 = EventSource.builder().name("test-1").build();
        EventSource src2 = EventSource.builder().name("test-2").build();
        EventSource src3 = EventSource.builder().name("test-3").build();
        esController.registerEventSource(src1);

        assertEquals(1, esController.getAllEventSources().size());
        esController.registerEventSource(src2);
        esController.registerEventSource(src3);

        assertEquals(3, esController.getAllEventSources().size());
        assertThrows(OasisApiRuntimeException.class, () -> esController.registerEventSource(src2));
        assertEquals(3, esController.getAllEventSources().size());

        List<EventSource> allSrc = esController.getAllEventSources();
        assertEquals(3, (int) allSrc.stream().filter(s -> Objects.isNull(s.getSecrets())).count());
    }

    @Test
    void testDeleteEventSources() throws OasisException {
        assertEquals(0, esController.getAllEventSources().size());

        EventSource src1 = EventSource.builder().name("test-1").build();
        EventSource src2 = EventSource.builder().name("test-2").build();
        EventSource src3 = EventSource.builder().name("test-3").build();
        int id1 = esController.registerEventSource(src1).getId();
        int id2 = esController.registerEventSource(src2).getId();
        int id3 = esController.registerEventSource(src3).getId();

        assertEquals(3, esController.getAllEventSources().size());

        esController.deleteEventSource(id1);

        assertEquals(2, esController.getAllEventSources().size());

        esController.deleteEventSource(id2);
        esController.deleteEventSource(id3);

        assertEquals(0, esController.getAllEventSources().size());
    }

    @Test
    void testRegisterSourcesToGame() throws OasisException {
        assertEquals(0, esController.getAllEventSources().size());

        EventSource src1 = EventSource.builder().name("test-1").build();
        EventSource src2 = EventSource.builder().name("test-2").build();
        EventSource src3 = EventSource.builder().name("test-3").build();
        int id1 = esController.registerEventSource(src1).getId();
        int id2 = esController.registerEventSource(src2).getId();
        int id3 = esController.registerEventSource(src3).getId();

        assertEquals(3, esController.getAllEventSources().size());

        esController.associateEventSourceToGame(1, id1);
        esController.associateEventSourceToGame(1, id2);
        esController.associateEventSourceToGame(1, id3);

        esController.associateEventSourceToGame(2, id2);
        esController.associateEventSourceToGame(2, id1);

        List<EventSource> game1Sources = esController.getEventSourcesOfGame(1);
        assertEquals(3, game1Sources.size());
        List<String> game1Names = game1Sources.stream().map(EventSource::getName).collect(Collectors.toList());
        assertTrue(game1Names.contains(src1.getName()));
        assertTrue(game1Names.contains(src2.getName()));
        assertTrue(game1Names.contains(src3.getName()));

        List<EventSource> game2Sources = esController.getEventSourcesOfGame(2);
        assertEquals(2, game2Sources.size());
        List<String> game2Names = game2Sources.stream().map(EventSource::getName).collect(Collectors.toList());
        assertTrue(game2Names.contains(src1.getName()));
        assertTrue(game2Names.contains(src2.getName()));
        assertFalse(game2Names.contains(src3.getName()));

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> esController.associateEventSourceToGame(1, id1))
                .isInstanceOf(OasisApiRuntimeException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCodes.EVENT_SOURCE_ALREADY_MAPPED);
    }

    @Test
    void testDeRegisterSourcesToGame() throws OasisException {
        assertEquals(0, esController.getAllEventSources().size());

        EventSource src1 = EventSource.builder().name("test-1").build();
        EventSource src2 = EventSource.builder().name("test-2").build();
        EventSource src3 = EventSource.builder().name("test-3").build();
        int id1 = esController.registerEventSource(src1).getId();
        int id2 = esController.registerEventSource(src2).getId();
        int id3 = esController.registerEventSource(src3).getId();

        assertEquals(3, esController.getAllEventSources().size());

        esController.associateEventSourceToGame(1, id1);
        esController.associateEventSourceToGame(1, id2);
        esController.associateEventSourceToGame(1, id3);

        esController.associateEventSourceToGame(2, id2);
        esController.associateEventSourceToGame(2, id1);

        assertEquals(3, esController.getEventSourcesOfGame(1).size());

        esController.removeEventSourceFromGame(1, id2);
        {
            List<EventSource> game1Sources = esController.getEventSourcesOfGame(1);
            assertEquals(2, game1Sources.size());
            List<String> game1Names = game1Sources.stream().map(EventSource::getName).collect(Collectors.toList());
            assertTrue(game1Names.contains(src1.getName()));
            assertFalse(game1Names.contains(src2.getName()));
            assertTrue(game1Names.contains(src3.getName()));
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

    @Override
    JdbcRepository createJdbcRepository(Jdbi jdbi) {
        return new JdbcRepository(jdbi.onDemand(IGameDao.class),
                jdbi.onDemand(IEventSourceDao.class),
                null,
                null,
                null);
    }

    @Override
    void createServices(BackendRepository backendRepository) {
        esController = new EventSourceController(new EventSourceService(backendRepository, keyGeneratorSupport));
    }
}
