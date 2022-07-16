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

import io.github.oasis.core.Game;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * @author Isuru Weerarathna
 */
@Getter
@Builder(toBuilder = true)
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class GameCreateRequest implements Serializable {

    @NotBlank(message = "Parameter 'name' is mandatory!")
    @Size(message = "Game name must not exceed 255 characters in length!", min = 1, max = 255)
    private String name;
    private String motto;
    private String description;
    private String logoRef;

    public Game createGame() {
        return Game.builder()
                .name(name)
                .motto(motto)
                .description(description)
                .logoRef(logoRef)
                .build();
    }
}
