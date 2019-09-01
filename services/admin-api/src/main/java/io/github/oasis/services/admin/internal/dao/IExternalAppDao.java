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

package io.github.oasis.services.admin.internal.dao;

import io.github.oasis.services.admin.internal.ApplicationKey;
import io.github.oasis.services.admin.internal.dto.ExtAppRecord;
import io.github.oasis.services.admin.internal.dto.ExtAppUpdateResult;
import io.github.oasis.services.admin.internal.dto.NewAppDto;
import io.github.oasis.services.admin.internal.dto.ResetKeyDto;
import io.github.oasis.services.admin.internal.exceptions.ExtAppAlreadyExistException;
import io.github.oasis.services.admin.internal.exceptions.ExtAppNotFoundException;
import io.github.oasis.services.admin.internal.exceptions.KeyAlreadyDownloadedException;
import io.github.oasis.services.admin.json.apps.UpdateApplicationJson;
import io.github.oasis.services.common.Validation;
import org.jdbi.v3.core.result.LinkedHashMapRowReducer;
import org.jdbi.v3.core.result.RowView;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.locator.UseClasspathSqlLocator;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlBatch;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.sqlobject.statement.UseRowReducer;
import org.jdbi.v3.sqlobject.transaction.Transaction;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Dao responsible for dealing with external application
 * records and their management.
 *
 * @author Isuru Weerarathna
 */
public interface IExternalAppDao {

    @SqlUpdate("INSERT INTO OA_EXT_APP" +
            " (name, token, key_secret, key_public, is_internal, for_all_games, created_at)" +
            " VALUES " +
            " (:name, :token, :keySecret, :keyPublic, :internal, :forAllGames, :createdAt)")
    @GetGeneratedKeys("app_id")
    int insertExternalApp(@BindBean NewAppDto appDto);

    @SqlQuery("SELECT token FROM OA_EXT_APP WHERE name = :name LIMIT 1")
    Optional<String> findExternalAppByName(@Bind("name") String name);

    @UseClasspathSqlLocator
    @SqlQuery
    @RegisterBeanMapper(value = ExtAppRecord.class, prefix = "a")
    @RegisterBeanMapper(value = ExtAppRecord.GameDef.class, prefix = "g")
    @RegisterBeanMapper(value = ExtAppRecord.EventType.class, prefix = "et")
    @UseRowReducer(AppGameReducer.class)
    List<ExtAppRecord> getAllRegisteredApps();

    @SqlQuery("SELECT game_id FROM OA_GAME_DEF WHERE is_active = true")
    List<Integer> readAllGameIds();

    @SqlBatch("INSERT INTO OA_EXT_APP_EVENT (app_id, event_type) VALUES (:appId, :eventType)")
    void insertEventMappingsForApp(@Bind("appId") int appId, @Bind("eventType") List<String> eventTypes);

    @SqlBatch("DELETE FROM OA_EXT_APP_EVENT WHERE app_id = :appId AND event_type = :eventType")
    void removeEventMappingsForApp(@Bind("appId") int appId, @Bind("eventType") List<String> eventType);

    @SqlBatch("INSERT INTO OA_EXT_APP_GAME (app_id, game_id) VALUES (:appId, :gameId)")
    void insertGameMappingsForApp(@Bind("appId") int appId, @Bind("gameId") List<Integer> gameIds);

    @SqlBatch("INSERT INTO OA_EXT_APP_GAME (app_id, game_id) VALUES (:appId, :gameId)")
    void insertAppMappingsForGame(@Bind("gameId") int gameId, @Bind("appId") List<Integer> appIds);

    @SqlBatch("DELETE FROM OA_EXT_APP_GAME WHERE app_id = :appId AND game_id = :gameId")
    void removeGameMappingsForApp(@Bind("appId") int appId, @Bind("gameId") List<Integer> gameId);

    @UseClasspathSqlLocator
    @RegisterBeanMapper(value = ExtAppRecord.class, prefix = "a")
    @RegisterBeanMapper(value = ExtAppRecord.GameDef.class, prefix = "g")
    @RegisterBeanMapper(value = ExtAppRecord.EventType.class, prefix = "et")
    @SqlQuery
    @UseRowReducer(AppGameReducer.class)
    Optional<ExtAppRecord> readApplication(@Bind("appId") int appId);

    @SqlUpdate("UPDATE OA_EXT_APP" +
            " SET key_secret = :secretKey, key_public = :publicKey, key_reset_at = :keyResetAt," +
            "     is_downloaded = false" +
            " WHERE app_id = :appId")
    int resetKeysOfApp(@Bind("appId") int appId, @BindBean ResetKeyDto resetKey);

    @SqlUpdate("UPDATE OA_EXT_APP SET is_downloaded = true WHERE app_id = :appId")
    int markKeyAsDownloaded(@Bind("appId") int appId);

    @SqlUpdate("UPDATE OA_EXT_APP SET is_active = false WHERE app_id = :appId")
    int deactivateApplication(@Bind("appId") int appId);

    @Transaction
    default void attachAppsToNewGame(int gameId) {
        List<Integer> appIds = getAllRegisteredApps().stream()
                .filter(ExtAppRecord::isForAllGames)
                .map(ExtAppRecord::getId)
                .collect(Collectors.toList());
        insertAppMappingsForGame(gameId, appIds);
    }

    @Transaction
    default ExtAppUpdateResult updateApplication(int appId, UpdateApplicationJson updateData) {
        ExtAppRecord appRecord = readApplication(appId).orElseThrow(
                () -> new ExtAppNotFoundException("No registered app is found by given id!"));

        ExtAppUpdateResult.ExtAppUpdateResultBuilder builder = new ExtAppUpdateResult.ExtAppUpdateResultBuilder();
        if (updateData.hasEventTypes()) {
            Set<String> existingEvents = appRecord.getEventTypes().stream()
                    .map(ExtAppRecord.EventType::getEventType)
                    .collect(Collectors.toSet());
            List<String> removedEvents = updateData.removedEvents(existingEvents);
            if (Validation.isNonEmpty(removedEvents)) {
                removeEventMappingsForApp(appId, removedEvents);
            }

            List<String> newlyAddedEvents = updateData.newlyAddedEvents(existingEvents);
            if (Validation.isNonEmpty(newlyAddedEvents)) {
                insertEventMappingsForApp(appId, newlyAddedEvents);
            }

            builder.addedEventTypes(newlyAddedEvents).removedEventTypes(removedEvents);
        }

        if (updateData.hasGameIds()) {
            Set<Integer> existingGames = appRecord.getMappedGames().stream()
                    .map(ExtAppRecord.GameDef::getId)
                    .collect(Collectors.toSet());
            List<Integer> removedGames = updateData.removedGames(existingGames);
            if (Validation.isNonEmpty(removedGames)) {
                removeGameMappingsForApp(appId, removedGames);
            }

            List<Integer> newlyAddedGames = updateData.newlyAddedGames(existingGames);
            if (Validation.isNonEmpty(newlyAddedGames)) {
                insertGameMappingsForApp(appId, newlyAddedGames);
            }

            builder.addedGameIds(newlyAddedGames).removedGameIds(removedGames);
        }

        return builder.create();
    }

    @Transaction
    default ApplicationKey readApplicationKey(int appId) throws ExtAppNotFoundException {
        Optional<ExtAppRecord> appOpt = readApplication(appId);
        if (appOpt.isPresent()) {
            ExtAppRecord app = appOpt.get();
            if (!app.isDownloaded() && markKeyAsDownloaded(appId) > 0) {
                return ApplicationKey.from(app);
            } else {
                throw new KeyAlreadyDownloadedException("Key has been already downloaded for the app!");
            }
        } else {
            throw new ExtAppNotFoundException(
                    String.format("No registered app is found by given id! [%d]", appId));
        }
    }

    @Transaction
    default int addApplication(NewAppDto newApp) throws ExtAppAlreadyExistException {
        Optional<String> externalAppByName = findExternalAppByName(newApp.getName());
        if (externalAppByName.isPresent()) {
            throw new ExtAppAlreadyExistException(
                    String.format("An application by name '%s' already exist!", newApp.getName()));
        }
        int appId = insertExternalApp(newApp);

        List<Integer> gameIds = newApp.isForAllGames() ? readAllGameIds() : newApp.getGameIds();
        System.out.println(gameIds);
        insertGameMappingsForApp(appId, gameIds);

        if (newApp.hasEvents()) {
            insertEventMappingsForApp(appId, newApp.getEventTypes());
        }

        return appId;
    }

    class AppGameReducer implements LinkedHashMapRowReducer<Integer, ExtAppRecord> {
        @Override
        public void accumulate(Map<Integer, ExtAppRecord> map, RowView rowView) {
            ExtAppRecord f = map.computeIfAbsent(rowView.getColumn("a_id", Integer.class),
                    id -> rowView.getRow(ExtAppRecord.class));

            if (rowView.getColumn("g_id", Integer.class) != null) {
                f.getMappedGames().add(rowView.getRow(ExtAppRecord.GameDef.class));
            }

            if (rowView.getColumn("et_id", Integer.class) != null) {
                f.getEventTypes().add(rowView.getRow(ExtAppRecord.EventType.class));
            }
        }
    }
}
