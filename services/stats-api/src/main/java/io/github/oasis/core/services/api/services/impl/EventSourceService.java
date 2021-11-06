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

package io.github.oasis.core.services.api.services.impl;

import io.github.oasis.core.exception.OasisException;
import io.github.oasis.core.external.OasisRepository;
import io.github.oasis.core.model.EventSource;
import io.github.oasis.core.model.EventSourceSecrets;
import io.github.oasis.core.services.KeyGeneratorSupport;
import io.github.oasis.core.services.annotations.AdminDbRepository;
import io.github.oasis.core.services.api.beans.BackendRepository;
import io.github.oasis.core.services.api.exceptions.DataValidationException;
import io.github.oasis.core.services.api.exceptions.ErrorCodes;
import io.github.oasis.core.services.api.services.IEventSourceService;
import io.github.oasis.core.services.api.to.EventSourceCreateRequest;
import io.github.oasis.core.services.api.to.EventSourceKeysResponse;
import io.github.oasis.core.utils.Texts;
import org.springframework.stereotype.Service;

import java.security.KeyPair;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

/**
 * @author Isuru Weerarathna
 */
@Service
public class EventSourceService extends AbstractOasisService implements IEventSourceService {

    private final KeyGeneratorSupport keyGeneratorSupport;

    public EventSourceService(@AdminDbRepository OasisRepository backendRepository,
                              KeyGeneratorSupport keyGeneratorSupport) {
        super(backendRepository);

        this.keyGeneratorSupport = keyGeneratorSupport;
    }

    @Override
    public EventSource registerEventSource(EventSourceCreateRequest request) throws OasisException {
        validateEventSource(request);

        EventSource.EventSourceBuilder sourceBuilder = EventSource.builder()
                .name(request.getName())
                .token(generateRandomToken());

        // assign keys
        KeyPair oasisKeyPair = keyGeneratorSupport.generate(request.getName());
        EventSourceSecrets sourceSecrets = new EventSourceSecrets();
        sourceSecrets.setPublicKey(Base64.getEncoder().encodeToString(oasisKeyPair.getPublic().getEncoded()));
        sourceSecrets.setPrivateKey(Base64.getEncoder().encodeToString(oasisKeyPair.getPrivate().getEncoded()));
        EventSource source = sourceBuilder.secrets(sourceSecrets).build();

        return backendRepository.addEventSource(source);
    }

    @Override
    public EventSource readEventSource(int eventSourceId) {
        return backendRepository.readEventSource(eventSourceId);
    }

    @Override
    public void deleteEventSource(int eventSourceId) {
        backendRepository.deleteEventSource(eventSourceId);
    }

    @Override
    public EventSourceKeysResponse downloadEventSourceKeys(int eventSourceId) {
        EventSourceSecrets secrets = backendRepository.readEventSourceSecrets(eventSourceId);
        return new EventSourceKeysResponse(secrets.getPrivateKey());
    }

    @Override
    public void assignEventSourceToGame(int eventSource, int gameId) {
        backendRepository.addEventSourceToGame(eventSource, gameId);
    }

    @Override
    public void removeEventSourceFromGame(int eventSource, int gameId) {
        backendRepository.removeEventSourceFromGame(eventSource, gameId);
    }

    @Override
    public List<EventSource> listAllEventSources() {
        List<EventSource> sources = backendRepository.listAllEventSources();
        for (EventSource source : sources) {
            source.setSecrets(null);
        }
        return sources;
    }

    @Override
    public List<EventSource> listAllEventSourcesOfGame(int gameId) {
        List<EventSource> sources = backendRepository.listAllEventSourcesOfGame(gameId);
        for (EventSource source : sources) {
            source.setSecrets(null);
        }
        return sources;
    }

    private String generateRandomToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private void validateEventSource(EventSourceCreateRequest request) throws DataValidationException {
        if (Texts.isEmpty(request.getName())) {
            throw new DataValidationException(ErrorCodes.EVENT_SOURCE_NO_NAME);
        }
    }
}
