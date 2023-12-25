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

package io.github.oasis.core.services.api.to;

import io.github.oasis.core.elements.SimpleElementDefinition;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.io.Serializable;
import java.util.Map;

/**
 * @author Isuru Weerarathna
 */
@Getter
@ToString
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class ElementCreateRequest implements Serializable {

    @NotNull(message = "Parameter 'gameId' is mandatory!")
    @Positive(message = "Parameter 'gameId' must be a valid game id!")
    private Integer gameId;

    @NotBlank(message = "Parameter 'type' is mandatory")
    private String type;

    @NotNull(message = "Parameter 'metadata' is mandatory!")
    @Valid
    private ElementMetadata metadata;

    @NotNull(message = "Parameter 'data' is mandatory!")
    @NotEmpty(message = "Parameter 'data' must have at least one field!")
    private Map<String, Object> data;

    @Data
    @Builder(toBuilder = true)
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ElementMetadata implements Serializable {

        @NotBlank(message = "Parameter 'metadata.id' is mandatory")
        private String id;
        @NotBlank(message = "Parameter 'metadata.name' is mandatory")
        private String name;
        private String description;
        private Integer version;

        public ElementMetadata(String id, String name, String description) {
            this.id = id;
            this.name = name;
            this.description = description;
        }

        public SimpleElementDefinition toElementDefinition() {
            return new SimpleElementDefinition(id, name, description, version);
        }
    }
}
