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

import io.github.oasis.core.Game;
import io.github.oasis.core.elements.ElementDef;
import io.github.oasis.core.elements.SimpleElementDefinition;
import io.github.oasis.core.services.api.beans.BackendRepository;
import io.github.oasis.core.services.api.exceptions.ErrorCodes;
import io.github.oasis.core.services.api.exceptions.OasisApiRuntimeException;
import io.github.oasis.core.services.api.to.ElementUpdateRequest;
import io.github.oasis.core.services.exceptions.OasisApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * @author Isuru Weerarathna
 */
@Service
public class ElementService extends AbstractOasisService {

    public ElementService(BackendRepository backendRepository) {
        super(backendRepository);
    }


    public ElementDef readElement(int gameId, String elementId, boolean withData) throws OasisApiException {
        ElementDef def;
        if (withData) {
            def = backendRepository.readElement(gameId, elementId);
        } else {
            def = backendRepository.readElementWithoutData(gameId, elementId);
        }
        return Optional.ofNullable(def)
                .orElseThrow(() -> new OasisApiException(
                        ErrorCodes.ELEMENT_NOT_EXISTS,
                        HttpStatus.NOT_FOUND.value(),
                        "Element not found!"));
    }

    public ElementDef addElement(int gameId, ElementDef elementDef) {
        return backendRepository.addNewElement(gameId, elementDef);
    }

    public ElementDef updateElement(int gameId, String elementId, ElementUpdateRequest updateRequest) {
        SimpleElementDefinition metadata = new SimpleElementDefinition(elementId, updateRequest.getName(), updateRequest.getDescription());
        return backendRepository.updateElement(gameId, elementId, metadata);
    }

    public ElementDef deleteElement(int gameId, String elementId) {
        return backendRepository.deleteElement(gameId, elementId);
    }

    public List<ElementDef> listElementsByType(int gameId, String type) {
        return backendRepository.readElementsByType(gameId, type);
    }

    public List<ElementDef> listElementsFromGameId(int gameId) {
        Game game = backendRepository.readGame(gameId);
        if (game == null) {
            throw new OasisApiRuntimeException(ErrorCodes.GAME_NOT_EXISTS);
        }

        return backendRepository.readElementsByGameId(gameId);
    }
}
