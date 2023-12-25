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

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author Isuru Weerarathna
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventSourceCreateRequest implements Serializable {

    @NotBlank(message = "Parameter 'name' is mandatory!")
    @Size(message = "Event source name must not exceed 64 characters in length!", min = 1, max = 64)
    @Pattern(regexp = "[a-zA-Z][a-zA-Z\\d-_]+",
            message = "Event source name should only contain alphanumeric, -, _ characters only")
    private String name;

}
