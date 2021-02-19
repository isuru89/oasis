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
import io.github.oasis.core.model.EventSourceSecrets;
import io.github.oasis.core.services.KeyGeneratorSupport;
import io.github.oasis.core.services.api.beans.BackendRepository;
import io.github.oasis.core.services.api.exceptions.DataValidationException;
import io.github.oasis.core.services.api.exceptions.ErrorCodes;
import io.github.oasis.core.utils.Texts;
import io.github.oasis.core.utils.Utils;
import org.springframework.stereotype.Service;

import java.security.KeyPair;
import java.util.*;

/**
 * @author Isuru Weerarathna
 */
@Service
public class EventSourceService extends AbstractOasisService {

    private final KeyGeneratorSupport keyGeneratorSupport;

    public EventSourceService(BackendRepository backendRepository, KeyGeneratorSupport keyGeneratorSupport) {
        super(backendRepository);

        this.keyGeneratorSupport = keyGeneratorSupport;
    }

    public EventSource registerEventSource(EventSource source) throws OasisException {
        validateEventSource(source);

        source.setGames(null);
        source.setToken(generateRandomToken());

        // assign keys
        KeyPair oasisKeyPair = keyGeneratorSupport.generate(source);
        EventSourceSecrets sourceSecrets = new EventSourceSecrets();
        sourceSecrets.setPublicKey(Base64.getEncoder().encodeToString(oasisKeyPair.getPublic().getEncoded()));
        sourceSecrets.setPrivateKey(Base64.getEncoder().encodeToString(oasisKeyPair.getPrivate().getEncoded()));
        source.setSecrets(sourceSecrets);

        EventSource dbSource = backendRepository.addEventSource(source);

        if (Utils.isNotEmpty(source.getGames())) {
            Set<Integer> gameIds = new HashSet<>(source.getGames());
            for (Integer gameId : gameIds) {
                backendRepository.addEventSourceToGame(dbSource.getId(), gameId);
            }
        }

        return dbSource;
    }

    public void deleteEventSource(int eventSourceId) {
        backendRepository.deleteEventSource(eventSourceId);
    }

    public void downloadEventSourceKeys(int eventSourceId) {

    }

    public void assignEventSourceToGame(int eventSource, int gameId) {
        backendRepository.addEventSourceToGame(eventSource, gameId);
    }

    public void removeEventSourceFromGame(int eventSource, int gameId) {
        backendRepository.removeEventSourceFromGame(eventSource, gameId);
    }

    public List<EventSource> listAllEventSources() {
        List<EventSource> sources = backendRepository.listAllEventSources();
        for (EventSource source : sources) {
            source.setSecrets(null);
        }
        return sources;
    }

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

    private void validateEventSource(EventSource source) throws DataValidationException {
        if (Texts.isEmpty(source.getName())) {
            throw new DataValidationException(ErrorCodes.EVENT_SOURCE_NO_NAME);
        }
    }
}
