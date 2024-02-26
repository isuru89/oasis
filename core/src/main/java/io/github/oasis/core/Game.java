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

package io.github.oasis.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * Representation of game definition.
 *
 * @author Isuru Weerarathna
 */
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder(toBuilder = true)
@AllArgsConstructor
public class Game implements Serializable {

    @EqualsAndHashCode.Include private Integer id;

    @EqualsAndHashCode.Include private String name;
    @EqualsAndHashCode.Include private String motto;
    @EqualsAndHashCode.Include private String description;
    @EqualsAndHashCode.Include private String logoRef;
    @EqualsAndHashCode.Include private int version;

    private Long startTime;
    private Long endTime;

    private long createdAt;
    private long updatedAt;
    @EqualsAndHashCode.Include private boolean active;

    public Game() {}

}
