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

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.io.Serializable;

/**
 * @author Isuru Weerarathna
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlayerGameAssociationRequest implements Serializable {

    private Long userId;

    @NotNull(message = "Parameter 'gameId' is mandatory!")
    @Positive(message = "Parameter 'gameId' must be a valid game id!")
    private Integer gameId;

    @NotNull(message = "Parameter 'teamId' is mandatory!")
    @Positive(message = "Parameter 'teamId' must be a valid team id!")
    private Integer teamId;

}
