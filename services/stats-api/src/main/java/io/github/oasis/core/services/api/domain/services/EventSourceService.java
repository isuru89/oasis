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
 *
 */

package io.github.oasis.core.services.api.domain.services;

import io.github.oasis.core.exception.OasisException;
import io.github.oasis.core.external.storage.EventSourceDao;
import io.github.oasis.core.external.storage.OasisDao;
import io.github.oasis.core.model.EventSource;
import io.github.oasis.core.model.EventSourceSecrets;
import io.github.oasis.core.services.KeyGeneratorSupport;
import io.github.oasis.core.services.api.app.request.EventSourceCreateRequest;
import io.github.oasis.core.services.api.app.response.EventSourceKeysResponse;
import io.github.oasis.core.services.api.exceptions.ErrorCodes;
import io.github.oasis.core.services.api.exceptions.OasisApiRuntimeException;
import io.github.oasis.core.services.api.exceptions.OasisEntityNotFoundException;
import io.github.oasis.core.services.api.handlers.events.BaseEventSourceChangedEvent;
import io.github.oasis.core.services.api.handlers.events.EntityChangeType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

/**
 * @author Isuru Weerarathna
 */
@Service
public class EventSourceService extends AbstractOasisService implements IEventSourceService {

    private final KeyGeneratorSupport keyGenerator;
    private final ApplicationEventPublisher eventPublisher;
    private final EventSourceDao eventSourceDao;

    public EventSourceService(@Qualifier("jpa") OasisDao dao,
                              KeyGeneratorSupport keyGenerator,
                              ApplicationEventPublisher eventPublisher) {
        super(dao);

        this.eventSourceDao = dao.getEventSourceDao();
        this.keyGenerator = keyGenerator;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public EventSource registerEventSource(EventSourceCreateRequest request) throws OasisException {


        // assign keys
        var oasisKeyPair = keyGenerator.generate(request.getName());
        var sourceSecrets = EventSourceSecrets.builder()
                .publicKey(Base64.getEncoder().encodeToString(oasisKeyPair.getPublic().getEncoded()))
                .privateKey(Base64.getEncoder().encodeToString(oasisKeyPair.getPrivate().getEncoded()))
                .build();

        var source = EventSource.builder()
                .name(request.getName())
                .token(generateRandomToken())
                .secrets(sourceSecrets)
                .build();

        try {
            return eventSourceDao.addEventSource(source);
        } catch (Exception ex) {
            throw new OasisApiRuntimeException(ErrorCodes.EVENT_SOURCE_ALREADY_EXISTS, ex);
        }
    }

    @Override
    public EventSource readEventSource(int eventSourceId) {
        return eventSourceDao.readEventSource(eventSourceId)
                .orElseThrow(() -> new OasisEntityNotFoundException(ErrorCodes.EVENT_SOURCE_NOT_EXISTS));
    }

    @Transactional(readOnly = true)
    @Override
    public EventSource readEventSourceByToken(String token) {
        var eventSource = eventSourceDao.readEventSource(token)
                .orElseThrow(() -> new OasisEntityNotFoundException(ErrorCodes.EVENT_SOURCE_NOT_EXISTS));

        var eventSourceSecrets = eventSourceDao.readEventSourcePublicSecrets(eventSource.getId());
        eventSource.setSecrets(new EventSourceSecrets(eventSourceSecrets.publicKey(), null, eventSourceSecrets.viewedTimes()));

        return eventSource;
    }

    @Transactional
    @Override
    public void deleteEventSource(int eventSourceId) {
        var eventSource = eventSourceDao.readEventSource(eventSourceId)
                .orElseThrow(() -> new OasisEntityNotFoundException(ErrorCodes.EVENT_SOURCE_NOT_EXISTS));

        eventSourceDao.deleteEventSource(eventSource.getId());

        eventPublisher.publishEvent(BaseEventSourceChangedEvent.builder()
                .changeType(EntityChangeType.REMOVED)
                .sourceId(eventSourceId)
                .token(eventSource.getToken())
                .build());
    }

    @Transactional
    @Override
    public EventSourceKeysResponse downloadEventSourceKeys(int eventSourceId) {
        var eventSource = eventSourceDao.readEventSource(eventSourceId)
                .orElseThrow(() -> new OasisEntityNotFoundException(ErrorCodes.EVENT_SOURCE_NOT_EXISTS));

        var secrets = eventSourceDao.readEventSourceSecrets(eventSourceId);

        var updatedSecrets = secrets.toBuilder().viewedTimes(secrets.viewedTimes() + 1).build();
        eventSource.setSecrets(updatedSecrets);

        eventSourceDao.save(eventSource);

        return new EventSourceKeysResponse(secrets.privateKey());
    }

    @Override
    public void assignEventSourceToGame(int eventSource, int gameId) {
        var eventSourceInDb = readEventSource(eventSource);
        eventSourceDao.addEventSourceToGame(eventSourceInDb.getId(), dao.getGameDao().readGame(gameId).getId());

        eventPublisher.publishEvent(BaseEventSourceChangedEvent.builder()
                .changeType(EntityChangeType.MODIFIED)
                .sourceId(eventSource)
                .token(eventSourceInDb.getToken())
                .build());
    }

    @Override
    public void removeEventSourceFromGame(int eventSource, int gameId) {
        var eventSourceInDb = readEventSource(eventSource);
        var gameRef = dao.getGameDao().readGame(gameId);
        eventSourceDao.removeEventSourceFromGame(eventSourceInDb.getId(), gameRef.getId());

        eventPublisher.publishEvent(BaseEventSourceChangedEvent.builder()
                .changeType(EntityChangeType.MODIFIED)
                .sourceId(eventSource)
                .token(eventSourceInDb.getToken())
                .build());
    }

    @Override
    public List<EventSource> listAllEventSources() {
        List<EventSource> sources = eventSourceDao.listAllEventSources();
        for (EventSource source : sources) {
            source.setSecrets(null);
        }
        return sources;
    }

    @Override
    public List<EventSource> listAllActiveEventSources() {
        return listAllEventSources().stream()
                .filter(EventSource::isActive)
                .toList();
    }

    @Override
    public List<EventSource> listAllEventSourcesOfGame(int gameId) {
        List<EventSource> sources = eventSourceDao.listAllEventSourcesOfGame(gameId);
        for (EventSource source : sources) {
            source.setSecrets(null);
        }
        return sources;
    }

    private String generateRandomToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }

}
