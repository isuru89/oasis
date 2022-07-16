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

package io.github.oasis.core.services.api.dao.dto;

import io.github.oasis.core.elements.ElementDef;
import io.github.oasis.core.elements.SimpleElementDefinition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author Isuru Weerarathna
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ElementDto implements Serializable {

    private Integer id;
    private String type;
    private String impl;
    private Integer gameId;
    private String elementId;
    private String elementName;
    private String elementDescription;

    private int version;
    private boolean active;

    private byte[] data;

    public static ElementDto fromWithoutData(ElementDef def) {
        return ElementDto.builder()
                .gameId(def.getGameId())
                .id(def.getId())
                .elementName(def.getMetadata().getName())
                .elementDescription(def.getMetadata().getDescription())
                .elementId(def.getElementId())
                .type(def.getType())
                .build();
    }

    public ElementDef toDefWithoutData() {
        SimpleElementDefinition meta = new SimpleElementDefinition(elementId, elementName, elementDescription);
        return ElementDef.builder()
                .id(id)
                .elementId(elementId)
                .type(type)
                .gameId(gameId)
                .version(version)
                .metadata(meta)
                .build();
    }

}
