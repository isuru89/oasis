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

package io.github.oasis.core.services.api.dao;

import io.github.oasis.core.services.api.dao.configs.UseOasisSqlLocator;
import io.github.oasis.core.services.api.dao.dto.EventSourceDto;
import io.github.oasis.core.services.api.dao.dto.EventSourceSecretsDto;
import org.jdbi.v3.core.transaction.TransactionIsolationLevel;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.sqlobject.transaction.Transaction;

import java.util.List;

/**
 * @author Isuru Weerarathna
 */
@UseOasisSqlLocator("io/github/oasis/db/scripts/eventsource")
@RegisterBeanMapper(EventSourceDto.class)
@RegisterBeanMapper(EventSourceSecretsDto.class)
public interface IEventSourceDao {

    @SqlUpdate
    @GetGeneratedKeys("id")
    int insertEventSource(@BindBean EventSourceDto eventSource, @Bind("ts") long timestamp);

    @SqlUpdate
    void insertEventSourceKeys(@Bind("id") int id, @BindBean EventSourceSecretsDto secrets);

    @SqlQuery
    EventSourceDto readEventSource(@Bind("id") int id);

    @SqlQuery
    EventSourceDto readEventSourceByToken(@Bind("token") String token);

    @SqlQuery
    EventSourceSecretsDto readEventSourceKeys(@Bind("id") int id);

    @SqlUpdate
    void updateDownloadCount(@Bind("id") int id, @Bind("downloadCount") int count);

    @Transaction(TransactionIsolationLevel.SERIALIZABLE)
    default EventSourceDto insertEventSource(EventSourceDto source) {
        int id = insertEventSource(source, System.currentTimeMillis());
        insertEventSourceKeys(id, source.getSecrets());
        EventSourceDto dbSource = readEventSource(id);
        EventSourceSecretsDto eventSourceSecrets = readEventSourceKeys(id);
        dbSource.setSecrets(eventSourceSecrets);
        return dbSource;
    }

    @Transaction(TransactionIsolationLevel.SERIALIZABLE)
    default EventSourceSecretsDto readKeysAndIncrement(int eventSourceId, int limit) {
        EventSourceSecretsDto secretsDto = readEventSourceKeys(eventSourceId);
        if (secretsDto.getDownloadCount() < limit) {
            updateDownloadCount(eventSourceId, secretsDto.getDownloadCount() + 1);
        }
        return secretsDto;
    }

    @SqlUpdate
    void deleteEventSourceById(@Bind("id") int id);

    @SqlUpdate
    void deleteEventSourceKeys(@Bind("id") int id);

    @Transaction
    default void deleteEventSource(int id) {
        deleteEventSourceById(id);
        deleteEventSourceKeys(id);
    }

    @SqlQuery
    List<EventSourceDto> readAllEventSources();

    @SqlQuery
    List<EventSourceDto> readEventSourcesOfGame(@Bind("gameId") int gameId);

    @SqlUpdate
    void addEventSourceToGame(@Bind("gameId") int gameId, @Bind("eventSourceId") int eventSourceId);

    @SqlUpdate
    void removeEventSourceFromGame(@Bind("gameId") int gameId, @Bind("eventSourceId") int eventSourceId);

    @SqlQuery
    List<Integer> readEventSourceGames(@Bind("eventSourceId") int eventSourceId);
}
