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

import io.github.oasis.services.admin.internal.dto.ExtAppRecord;
import io.github.oasis.services.admin.internal.dto.NewAppDto;
import io.github.oasis.services.admin.internal.exceptions.ExtAppAlreadyExistException;
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

/**
 * Dao responsible for dealing with external application
 * records and their management.
 *
 * @author Isuru Weerarathna
 */
@UseClasspathSqlLocator
public interface IExternalAppDao {

    @SqlUpdate("INSERT INTO OA_EXT_APP" +
            " (name, token, key_secret, key_public, is_internal)" +
            " VALUES " +
            " (:name, :token, :keySecret, :keyPublic, :internal)")
    @GetGeneratedKeys("app_id")
    int insertExternalApp(@BindBean NewAppDto appDto);

    @SqlQuery("SELECT token" +
            " FROM OA_EXT_APP" +
            " WHERE name = :name" +
            " LIMIT 1")
    Optional<String> findExternalAppByName(@Bind("name") String name);

    @SqlQuery
    @RegisterBeanMapper(value = ExtAppRecord.class, prefix = "a")
    @RegisterBeanMapper(value = ExtAppRecord.GameDef.class, prefix = "g")
    @RegisterBeanMapper(value = ExtAppRecord.EventType.class, prefix = "et")
    @UseRowReducer(AppGameReducer.class)
    List<ExtAppRecord> getAllExternalApps();

    @SqlQuery("SELECT game_id FROM OA_GAME_DEF")
    List<Integer> readAllGameIds();

    @SqlBatch("INSERT INTO OA_EXT_APP_EVENT (app_id, event_type) VALUES (:appId, :eventType)")
    void insertEventMappingsForApp(@Bind("appId") int appId,
                                   @Bind("eventType") List<String> eventTypes);

    @SqlBatch("INSERT INTO OA_EXT_APP_GAME (app_id, game_id) VALUES (:appId, :gameId)")
    void insertGameMappingForApp(@Bind("appId") int appId,
                                 @Bind("gameId") List<Integer> gameIds);

    @Transaction
    default int addApplication(NewAppDto dto) throws ExtAppAlreadyExistException {
        Optional<String> externalAppByName = findExternalAppByName(dto.getName());
        if (externalAppByName.isPresent()) {
            throw new ExtAppAlreadyExistException(
                    String.format("An application by name '%s' already exist!", dto.getName()));
        }
        int appId = insertExternalApp(dto);

        List<Integer> gameIds = readAllGameIds();
        insertGameMappingForApp(appId, gameIds);

        if (dto.hasEvents()) {
            insertEventMappingsForApp(appId, dto.getEventTypes());
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
