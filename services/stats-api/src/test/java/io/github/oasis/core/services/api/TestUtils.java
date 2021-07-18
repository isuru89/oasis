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

package io.github.oasis.core.services.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.oasis.core.elements.GameDef;
import io.github.oasis.core.elements.SimpleElementDefinition;
import io.github.oasis.core.parser.GameParserYaml;
import io.github.oasis.core.services.api.to.ElementCreateRequest;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Isuru Weerarathna
 */
public class TestUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static String toJson(Object value) {
        try {
            return MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Unable to perform serialization!");
        }
    }

    public static List<ElementCreateRequest> parseElementRules(String clzPath, int gameId) {
        GameDef gameDef = GameParserYaml.fromClasspath(clzPath, Thread.currentThread().getContextClassLoader());
        return gameDef.getRuleDefinitions().stream()
                .map(def -> ElementCreateRequest.builder()
                        .type(def.getImpl())
                        .gameId(gameId)
                        .metadata(new SimpleElementDefinition(
                                def.getData().get("id").toString(),
                                def.getData().get("name").toString(),
                                def.getData().get("description").toString()
                        ))
                        .data(def.getData())
                        .build())
                .collect(Collectors.toList());
    }

    public static ElementCreateRequest findById(String id, List<ElementCreateRequest> requests) {
        return requests.stream()
                .filter(req -> req.getMetadata().getId().equals(id))
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Given rule id not found!"));
    }

}
