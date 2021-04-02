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

import io.github.oasis.core.elements.AttributeInfo;
import io.github.oasis.core.services.api.beans.BackendRepository;
import io.github.oasis.core.services.api.to.GameAttributeCreateRequest;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Isuru Weerarathna
 */
@Service
public class GameAttributeService extends AbstractOasisService {

    public GameAttributeService(BackendRepository backendRepository) {
        super(backendRepository);
    }

    public AttributeInfo addAttribute(int gameId, GameAttributeCreateRequest request) {
        AttributeInfo attributeInfo = AttributeInfo.builder()
                .name(request.getName())
                .colorCode(request.getColorCode())
                .priority(request.getPriority())
                .build();

        return backendRepository.addAttribute(gameId, attributeInfo);
    }

    public List<AttributeInfo> listAttributes(int gameId) {
        return backendRepository.listAllAttributes(gameId);
    }

}
