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

package io.github.oasis.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Isuru Weerarathna
 */
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class EventSource implements Serializable {

    private Integer id;
    private String token;
    private String name;

    private Set<Integer> games;

    private EventSourceSecrets secrets;
    private boolean active;

    public EventSourceMetadata createCopyOfMeta() {
        EventSourceMetadata source = new EventSourceMetadata();
        source.setId(id);
        source.setToken(token);
        if (games != null) {
            source.setGames(new HashSet<>(games));
        } else {
            source.setGames(new HashSet<>());
        }
        source.setKey(secrets.getPublicKey());
        return source;
    }
}
