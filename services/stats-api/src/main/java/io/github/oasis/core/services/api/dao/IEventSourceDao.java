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

import io.github.oasis.core.model.EventSource;
import io.github.oasis.core.model.EventSourceSecrets;
import io.github.oasis.core.services.api.dao.configs.UseOasisSqlLocator;
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
@RegisterBeanMapper(EventSource.class)
@RegisterBeanMapper(EventSourceSecrets.class)
public interface IEventSourceDao {

    @SqlUpdate
    @GetGeneratedKeys("id")
    int insertEventSource(@BindBean EventSource eventSource, @Bind("ts") long timestamp);

    @SqlUpdate
    void insertEventSourceKeys(@Bind("id") int id, @BindBean EventSourceSecrets secrets);

    @Transaction
    default EventSource insertEventSource(EventSource source) {
        int id = insertEventSource(source, System.currentTimeMillis());
        insertEventSourceKeys(id, source.getSecrets());
        EventSource dbSource = readEventSource(id);
        EventSourceSecrets eventSourceSecrets = readEventSourceSecrets(id);
        dbSource.setSecrets(eventSourceSecrets);
        return dbSource;
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
    EventSource readEventSource(@Bind("id") int id);

    @SqlQuery
    EventSource readEventSourceByToken(@Bind("token") String token);

    @SqlQuery
    EventSourceSecrets readEventSourceSecrets(@Bind("id") int id);

    @SqlQuery
    List<EventSource> readAllEventSources();

    @SqlQuery
    List<EventSource> readEventSourcesOfGame(@Bind("gameId") int gameId);

    @SqlUpdate
    void addEventSourceToGame(@Bind("gameId") int gameId, @Bind("eventSourceId") int eventSourceId);

    @SqlUpdate
    void removeEventSourceFromGame(@Bind("gameId") int gameId, @Bind("eventSourceId") int eventSourceId);

}
