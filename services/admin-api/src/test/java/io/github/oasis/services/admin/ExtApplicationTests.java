/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.github.oasis.services.admin;


import io.github.oasis.services.admin.domain.ExternalAppService;
import io.github.oasis.services.admin.domain.Game;
import io.github.oasis.services.admin.domain.GameStateService;
import io.github.oasis.services.admin.handlers.GameCreateHandler;
import io.github.oasis.services.admin.internal.ApplicationKey;
import io.github.oasis.services.admin.internal.dao.IExternalAppDao;
import io.github.oasis.services.admin.internal.dao.IGameCreationDao;
import io.github.oasis.services.admin.internal.dao.IGameStateDao;
import io.github.oasis.services.admin.internal.dto.NewGameDto;
import io.github.oasis.services.admin.internal.exceptions.ExtAppNotFoundException;
import io.github.oasis.services.admin.internal.exceptions.KeyAlreadyDownloadedException;
import io.github.oasis.services.admin.json.apps.AppUpdateResultJson;
import io.github.oasis.services.admin.json.apps.ApplicationAddedJson;
import io.github.oasis.services.admin.json.apps.ApplicationJson;
import io.github.oasis.services.admin.json.apps.NewApplicationJson;
import io.github.oasis.services.admin.json.apps.UpdateApplicationJson;
import io.github.oasis.services.admin.json.game.GameJson;
import io.github.oasis.services.common.OasisValidationException;
import io.github.oasis.services.common.internal.events.admin.ExternalAppEvent;
import io.github.oasis.services.common.internal.events.admin.ExternalAppEventType;
import io.github.oasis.services.common.internal.events.game.GameCreatedEvent;
import org.apache.commons.collections4.ListUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static io.github.oasis.services.admin.utils.TestUtils.NONE;
import static io.github.oasis.services.admin.utils.TestUtils.SINGLE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Isuru Weerarathna
 */
@DisplayName("External Applications")
public class ExtApplicationTests extends AbstractTest {

    @Autowired private IExternalAppDao externalAppDao;
    @Autowired private IGameStateDao gameDao;
    @Autowired private IGameCreationDao gameCreationDao;

    @MockBean private ApplicationEventPublisher publisher;

    private AdminAggregate admin;

    @Captor private ArgumentCaptor<GameCreatedEvent> createdEventArgumentCaptor;

    @Captor
    private ArgumentCaptor<ExternalAppEvent> extAppCapture;

    @BeforeEach
    void beforeEach() {
        MockitoAnnotations.initMocks(this);
        ExternalAppService externalAppService = new ExternalAppService(externalAppDao);
        GameStateService gameStateService = new GameStateService(gameDao);
        Game game = new Game(gameCreationDao);
        admin = new AdminAggregate(publisher, externalAppService, gameStateService, game);

        super.runBeforeEach();
    }

    @DisplayName("Should be able to add applications even yet to define a game")
    @Test
    void testAddApplicationsByAdmin() {
        NewApplicationJson app = new NewApplicationJson();
        app.setName("test-1");
        app.setForAllGames(true);
        app.setEventTypes(Arrays.asList("e1", "e2"));
        ApplicationAddedJson addedApp = admin.registerNewApp(app);

        verifyAppEventFired(addedApp.getId(), ExternalAppEventType.CREATED);

        List<ApplicationJson> allApps = admin.readAllApps();
        assertEquals(1, allApps.size());
        verifyNewApp(allApps.get(0), app);
    }

    @DisplayName("When no applications exist, should return empty")
    @Test
    void testListApps() {
        List<ApplicationJson> allApps = admin.readAllApps();
        assertNotNull(allApps);
        assertEquals(0, allApps.size());
    }

    @DisplayName("Application secret key can only be downloaded once forever")
    @Test
    void testDownloadKeys() {
        NewApplicationJson app = new NewApplicationJson();
        app.setName("testing-123");
        app.setEventTypes(Arrays.asList("e1", "e2", "e3"));
        app.setForAllGames(true);
        ApplicationAddedJson addedApp = admin.registerNewApp(app);
        assertAddedApp(addedApp);

        verifyAppEventFired(addedApp.getId(), ExternalAppEventType.CREATED);

        {
            // download key
            ApplicationJson storedApp = readTheOnlyApp();
            assertFalse(storedApp.isDownloaded());
            assertFalse(storedApp.isInternal());

            ApplicationKey key = admin.readApplicationKey(addedApp.getId());
            assertEquals(storedApp.getId(), key.getId());
            assertTrue(key.getData().length > 0, "Should get the secret key for the new apps!");
        }

        ApplicationJson downloadedApp = readTheOnlyApp();
        assertTrue(downloadedApp.isDownloaded());

        assertThrows(KeyAlreadyDownloadedException.class,
                () -> admin.readApplicationKey(downloadedApp.getId()));
    }

    @DisplayName("At least one event type must be mapped with an application")
    @Test
    void testAddApplicationsWithEventTypes() {
        NewApplicationJson app = new NewApplicationJson();
        app.setName("testing-123");
        app.setForAllGames(true);
        assertThrows(OasisValidationException.class, () -> admin.registerNewApp(app));

        verifyNoAppEventFired();
    }

    @DisplayName("When not selected for all games, at least one game id must be specified.")
    @Test
    void testAddApplicationsWithoutExplicitGames() {
        NewApplicationJson app = new NewApplicationJson();
        app.setName("testing-123");
        app.setEventTypes(Arrays.asList("e1", "e2", "e3"));
        app.setForAllGames(false);
        assertThrows(OasisValidationException.class, () -> admin.registerNewApp(app));

        verifyNoAppEventFired();
    }

    @DisplayName("When all are non-existing game ids, should not be attached with application")
    @Test
    void testAddForSubSetOfGamesWithNonExistingIds() {
        NewApplicationJson app = new NewApplicationJson();
        app.setName("testing-123");
        app.setForAllGames(false);
        app.setMappedGameIds(Arrays.asList(100, 101, 102));
        app.setEventTypes(Arrays.asList("e1", "e2", "e3", "e4"));
        ApplicationAddedJson addedApp = admin.registerNewApp(app);
        assertAddedApp(addedApp);

        verifyAppEventFired(addedApp.getId(), ExternalAppEventType.CREATED);

        List<ApplicationJson> registeredApps = admin.readAllApps();
        assertEquals(1, registeredApps.size());
        ApplicationJson storedApp = registeredApps.get(0);
        verifyNewApp(storedApp, app);
    }

    @DisplayName("When some of are non-existing game ids, should return only for existing games")
    @Test
    void testAddForSubSetOfGamesWithSomeNonExistingIds() {
        List<Integer> addedGameIds = addGames(100, 101, 102);
        List<Integer> suppliedGameIds = new ArrayList<>(addedGameIds);
        suppliedGameIds.add(999);
        NewApplicationJson app = new NewApplicationJson();
        app.setName("testing-123");
        app.setForAllGames(false);
        app.setMappedGameIds(suppliedGameIds);
        app.setEventTypes(Arrays.asList("e1", "e2", "e3", "e4"));
        ApplicationAddedJson addedApp = admin.registerNewApp(app);
        assertAddedApp(addedApp);

        verifyAppEventFired(addedApp.getId(), ExternalAppEventType.CREATED);

        List<ApplicationJson> registeredApps = admin.readAllApps();
        assertEquals(1, registeredApps.size());
        ApplicationJson storedApp = registeredApps.get(0);
        verifyNewApp(storedApp, app, 3);
    }

    @DisplayName("Application can optionally restricted to subset of games")
    @Test
    void testAddApplicationsForSubSetOfGames() {
        List<Integer> gameIds = addGames(100, 101, 102);
        NewApplicationJson app = new NewApplicationJson();
        app.setName("testing-123");
        app.setForAllGames(false);
        app.setMappedGameIds(gameIds);
        app.setEventTypes(Arrays.asList("e1", "e2", "e3", "e4"));
        ApplicationAddedJson addedApp = admin.registerNewApp(app);
        assertAddedApp(addedApp);

        verifyAppEventFired(addedApp.getId(), ExternalAppEventType.CREATED);

        List<ApplicationJson> registeredApps = admin.readAllApps();
        assertEquals(1, registeredApps.size());
        ApplicationJson storedApp = registeredApps.get(0);
        verifyNewApp(storedApp, app, 3);
    }

    @DisplayName("Application name is mandatory")
    @Test
    void testAddApplicationsWithoutName() {
        NewApplicationJson app = new NewApplicationJson();
        app.setForAllGames(true);
        app.setEventTypes(Arrays.asList("e1", "e2"));
        assertThrows(OasisValidationException.class, () -> admin.registerNewApp(app));

        verifyNoAppEventFired();
    }

    @DisplayName("Application event types can be updated while game is running")
    @Test
    void testUpdateEventTypes() {
        NewApplicationJson app = new NewApplicationJson();
        app.setName("testing-123");
        app.setEventTypes(Arrays.asList("e1", "e2", "e3"));
        app.setForAllGames(true);
        ApplicationAddedJson addedApp = admin.registerNewApp(app);
        assertAddedApp(addedApp);

        verifyAppEventFired(addedApp.getId(), ExternalAppEventType.CREATED);

        {
            // add two event types
            UpdateApplicationJson updateJson = new UpdateApplicationJson();
            updateJson.setEventTypes(ListUtils.union(Arrays.asList("e5", "e6"), app.getEventTypes()));
            AppUpdateResultJson updateResult = admin.updateApp(addedApp.getId(), updateJson);
            assertAppUpdate(updateResult, 2, 0, 0, 0);

            verifyAppEventFired(addedApp.getId(), ExternalAppEventType.MODIFIED);
            ApplicationJson updatedApp = readTheOnlyApp();
            assertEquals(5, updatedApp.getEventTypes().size());
        }

        {
            // remove event type
            UpdateApplicationJson updateJson = new UpdateApplicationJson();
            updateJson.setEventTypes(Arrays.asList("e1", "e3", "e5", "e6"));
            AppUpdateResultJson updateResult = admin.updateApp(addedApp.getId(), updateJson);
            assertAppUpdate(updateResult, 0, 1, 0, 0);

            verifyAppEventFired(addedApp.getId(), ExternalAppEventType.MODIFIED);
            ApplicationJson updatedApp = readTheOnlyApp();
            assertEquals(4, updatedApp.getEventTypes().size());
        }

    }

    @DisplayName("should attach new games for existing applications specified for all games")
    @Test
    void testAttachGamesForApps() {
        addGames(101, 102, 105);
        NewApplicationJson app = new NewApplicationJson();
        app.setName("testing-123");
        app.setEventTypes(Arrays.asList("e1", "e2", "e3"));
        app.setForAllGames(true);
        ApplicationAddedJson addedApp = admin.registerNewApp(app);
        assertAddedApp(addedApp);

        verifyAppEventFired(addedApp.getId(), ExternalAppEventType.CREATED);

        {
            ApplicationJson storedApp = readTheOnlyApp();
            verifyNewApp(storedApp, app, 3);
        }

        GameCreateHandler handler = new GameCreateHandler(admin);
        GameCreatedEvent createdEvent = addGame("newly-added-game");
        handler.onGameCreatedEvent(createdEvent);

        // now the app should have 4 games mapped
        {
            ApplicationJson storedApp = readTheOnlyApp();
            verifyNewApp(storedApp, app, 4);
        }
    }

    @DisplayName("Application can restrict to a game while that game is running")
    @Test
    void testUpdateAppRestrictToGames() {
        List<Integer> addedGameIds = addGames(101, 102, 105, 107, 103, 109, 110, 111);
        NewApplicationJson app = new NewApplicationJson();
        app.setName("testing-123");
        app.setEventTypes(Arrays.asList("e1", "e2", "e3"));
        app.setForAllGames(false);
        app.setMappedGameIds(addedGameIds.subList(0, 4));
        ApplicationAddedJson addedApp = admin.registerNewApp(app);
        assertAddedApp(addedApp);

        verifyAppEventFired(addedApp.getId(), ExternalAppEventType.CREATED);

        {
            // add for new three games
            UpdateApplicationJson updateJson = new UpdateApplicationJson();
            updateJson.setGameIds(addedGameIds.subList(0, 7));
            AppUpdateResultJson updateResult = admin.updateApp(addedApp.getId(), updateJson);
            assertAppUpdate(updateResult, 0, 0, 3, 0);

            verifyAppEventFired(addedApp.getId(), ExternalAppEventType.MODIFIED);
            ApplicationJson updatedApp = readTheOnlyApp();
            assertEquals(7, updatedApp.getMappedGameIds().size());
        }

        {
            // remove from another two games
            UpdateApplicationJson updateJson = new UpdateApplicationJson();
            updateJson.setGameIds(addedGameIds.subList(0, 5));
            AppUpdateResultJson updateResult = admin.updateApp(addedApp.getId(), updateJson);
            assertAppUpdate(updateResult, 0, 0, 0, 2);

            verifyAppEventFired(addedApp.getId(), ExternalAppEventType.MODIFIED);
            ApplicationJson updatedApp = readTheOnlyApp();
            assertEquals(5, updatedApp.getMappedGameIds().size());
        }
    }

    @DisplayName("Non existing applications cannot be deleted")
    @Test
    void testTryDeleteApp() {
        assertThrows(ExtAppNotFoundException.class, () -> admin.deactivateApp(Integer.MAX_VALUE - 1));

        verifyNoAppEventFired();
    }

    @DisplayName("Existing application can be deleted only by admin")
    @Test
    void testDeleteAppByAdmin() {
        NewApplicationJson app = new NewApplicationJson();
        app.setName("test-345");
        app.setForAllGames(true);
        app.setEventTypes(Arrays.asList("e1", "e2"));

        ApplicationAddedJson addedApp = admin.registerNewApp(app);
        assertAddedApp(addedApp);

        verifyAppEventFired(addedApp.getId(), ExternalAppEventType.CREATED);

        admin.deactivateApp(addedApp.getId());

        verifyAppEventFired(addedApp.getId(), ExternalAppEventType.DEACTIVATED);
    }

    @DisplayName("should be able to reset key of an application")
    @Test
    void testResetKeyByAdmin() {
        addGame("game-reset-key");
        NewApplicationJson app = new NewApplicationJson();
        app.setName("test-345");
        app.setForAllGames(true);
        app.setEventTypes(Arrays.asList("e1", "e2"));
        ApplicationAddedJson addedApp = admin.registerNewApp(app);
        assertAddedApp(addedApp);
        verifyAppEventFired(addedApp.getId(), ExternalAppEventType.CREATED);

        ApplicationKey keyBefore = admin.readApplicationKey(addedApp.getId());
        assertNotNull(keyBefore.getData());
        ApplicationJson appBeforeReset = readTheOnlyApp();
        assertTrue(appBeforeReset.isDownloaded());
        assertNull(appBeforeReset.getKeyResetAt());

        // reset key now
        // should be downloaded set to false
        admin.resetApplicationKey(addedApp.getId());
        verifyAppEventFired(addedApp.getId(), ExternalAppEventType.KEY_RESET);

        ApplicationJson appAfterReset = readTheOnlyApp();
        assertFalse(appAfterReset.isDownloaded());
        assertNotNull(appAfterReset.getKeyResetAt());
        assertTrue(appAfterReset.getKeyResetAt() > appAfterReset.getCreatedAt(),
                "key reset time must be after created time!");
        ApplicationKey keyAfter = admin.readApplicationKey(appAfterReset.getId());
        assertNotNull(keyAfter.getData());
        assertNotEquals(keyBefore.getData(), keyAfter.getData(), "After reset keys must be different!");
    }

    GameCreatedEvent addGame(String name) {
        NewGameDto dto = new NewGameDto(name, name);
        admin.createGame(dto);
        Mockito.verify(publisher, SINGLE).publishEvent(createdEventArgumentCaptor.capture());
        GameCreatedEvent event = createdEventArgumentCaptor.getValue();
        Mockito.clearInvocations(publisher);
        return event;
    }

    List<Integer> addGames(int... ids) {
        List<Integer> addedIds = new ArrayList<>(ids.length);
        for (int id : ids) {
            NewGameDto dto = new NewGameDto(String.format("game-%d", id), String.format("description-%d", id));
            GameJson game = admin.createGame(dto);
            addedIds.add(game.getId());
        }
        assertEquals(ids.length, admin.listAllGames().size());
        Mockito.clearInvocations(publisher);
        return addedIds;
    }

    ApplicationJson readTheOnlyApp() {
        List<ApplicationJson> apps = admin.readAllApps();
        assertEquals(1, apps.size(), "Must have a single registered app!");
        return apps.get(0);
    }

    void verifyNewApp(ApplicationJson output, NewApplicationJson input) {
        verifyNewApp(output, input, 0);
    }

    void verifyNewApp(ApplicationJson output, NewApplicationJson input, int expectedGameCount) {
        assertEquals(input.getName(), output.getName());
        assertEquals(input.isForAllGames(), output.isForAllGames());
        assertEquals(input.getEventTypes().size(), output.getEventTypes().size());
        assertEquals(expectedGameCount, output.getMappedGameIds().size());
        assertTrue(output.getCreatedAt() > 0, "must have created time persisted!");
    }

    void assertAppUpdate(AppUpdateResultJson updateResult,
                         int addedEvents, int removedEvents, int addedGames, int removedGames) {
        assertEquals(addedEvents, ListUtils.emptyIfNull(updateResult.getAddedEventTypes()).size());
        assertEquals(addedGames, ListUtils.emptyIfNull(updateResult.getMappedGameIds()).size());
        assertEquals(removedEvents, ListUtils.emptyIfNull(updateResult.getRemovedEventTypes()).size());
        assertEquals(removedGames, ListUtils.emptyIfNull(updateResult.getUnmappedGameIds()).size());
    }

    void verifyNoAppEventFired() {
        Mockito.verify(publisher, NONE).publishEvent(Mockito.any(ExternalAppEvent.class));
    }

    void verifyAppEventFired(int appId, ExternalAppEventType type) {
        Mockito.verify(publisher, SINGLE).publishEvent(extAppCapture.capture());
        ExternalAppEvent event = extAppCapture.getValue();
        assertExtEventType(event, appId, type);
        Mockito.clearInvocations(publisher);
    }

    void assertAddedApp(ApplicationAddedJson added) {
        assertTrue(added.getId() > 0, "The new application must have an id!");
        assertEquals(32, added.getToken().length(), "The application token must be 32 length UUID!");
    }

    void assertExtEventType(ExternalAppEvent event, int appId, ExternalAppEventType type) {
        assertEquals(type, event.getType());
        assertEquals(appId, event.getId());
    }

}
