/*
 *
 *  * Licensed to the Apache Software Foundation (ASF) under one
 *  * or more contributor license agreements.  See the NOTICE file
 *  * distributed with this work for additional information
 *  * regarding copyright ownership.  The ASF licenses this file
 *  * to you under the Apache License, Version 2.0 (the
 *  * "License"); you may not use this file except in compliance
 *  * with the License.  You may obtain a copy of the License at
 *  *
 *  *    http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing,
 *  * software distributed under the License is distributed on an
 *  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  * KIND, either express or implied.  See the License for the
 *  * specific language governing permissions and limitations
 *  * under the License.
 *
 */

package io.github.oasis.services.feeds.services;

import io.github.oasis.core.Game;
import io.github.oasis.core.configs.OasisConfigs;
import io.github.oasis.core.model.EventSource;
import io.github.oasis.core.model.PlayerObject;
import io.github.oasis.core.model.TeamObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryCachedDataServiceTest {

    private DataService delegate;
    private DataService inMemoryService;

    @BeforeEach
    void beforeEach() {
        delegate = Mockito.mock(DataService.class);
        inMemoryService = new InMemoryCachedDataService(delegate);

        OasisConfigs configs = OasisConfigs.create(Map.of());
        inMemoryService.init(configs);
    }

    @Test
    void testGameCache() {
        Mockito.when(delegate.getGame(Mockito.eq(1))).thenReturn(Game.builder().id(1).name("Game 1").build());
        Mockito.when(delegate.getGame(Mockito.eq(2))).thenReturn(Game.builder().id(2).name("Game 2").build());

        {
            Game actual = inMemoryService.getGame(1);
            assertEquals("Game 1", actual.getName());
            Mockito.verify(delegate, Mockito.times(1)).getGame(Mockito.eq(1));
            Mockito.clearInvocations(delegate);
        }

        // repeated call should serve from cache
        {
            Game actual = inMemoryService.getGame(1);
            assertEquals("Game 1", actual.getName());
            Mockito.verify(delegate, Mockito.never()).getGame(Mockito.anyInt());
        }
    }

    @Test
    void testPlayerCache() {
        Mockito.when(delegate.getPlayer(Mockito.eq(1L))).thenReturn(PlayerObject.builder().id(1L).displayName("User 1").build());
        Mockito.when(delegate.getPlayer(Mockito.eq(2L))).thenReturn(PlayerObject.builder().id(2L).displayName("User 2").build());

        {
            var actual = inMemoryService.getPlayer(1);
            assertEquals("User 1", actual.getDisplayName());
            Mockito.verify(delegate, Mockito.times(1)).getPlayer(Mockito.eq(1L));
            Mockito.clearInvocations(delegate);
        }

        // repeated call should serve from cache
        {
            var actual = inMemoryService.getPlayer(1);
            assertEquals("User 1", actual.getDisplayName());
            Mockito.verify(delegate, Mockito.never()).getPlayer(Mockito.anyLong());
        }
    }

    @Test
    void testTeamCache() {
        Mockito.when(delegate.getTeam(Mockito.eq(1L))).thenReturn(TeamObject.builder().id(1).name("Team 1").build());
        Mockito.when(delegate.getTeam(Mockito.eq(2L))).thenReturn(TeamObject.builder().id(2).name("Team 2").build());

        {
            var actual = inMemoryService.getTeam(1);
            assertEquals("Team 1", actual.getName());
            Mockito.verify(delegate, Mockito.times(1)).getTeam(Mockito.eq(1L));
            Mockito.clearInvocations(delegate);
        }

        // repeated call should serve from cache
        {
            var actual = inMemoryService.getTeam(1);
            assertEquals("Team 1", actual.getName());
            Mockito.verify(delegate, Mockito.never()).getTeam(Mockito.anyLong());
        }
    }

    @Test
    void testEventSourceCache() {
        Mockito.when(delegate.getEventSource(Mockito.eq(1))).thenReturn(EventSource.builder().id(1).name("Source 1").build());
        Mockito.when(delegate.getEventSource(Mockito.eq(2))).thenReturn(EventSource.builder().id(2).name("Source 2").build());

        {
            var actual = inMemoryService.getEventSource(1);
            assertEquals("Source 1", actual.getName());
            Mockito.verify(delegate, Mockito.times(1)).getEventSource(Mockito.eq(1));
            Mockito.clearInvocations(delegate);
        }

        // repeated call should serve from cache
        {
            var actual = inMemoryService.getEventSource(1);
            assertEquals("Source 1", actual.getName());
            Mockito.verify(delegate, Mockito.never()).getEventSource(Mockito.anyInt());
        }
    }
}