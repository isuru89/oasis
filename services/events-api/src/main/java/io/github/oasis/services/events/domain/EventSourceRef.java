/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.github.oasis.services.events.domain;

import java.util.Set;

/**
 * @author Isuru Weerarathna
 */
public class EventSourceRef {

    private int id;
    private Set<Integer> mappedGames;
    private Set<String> allowedEventTypes;

    public boolean isEventAllowed(String event) {
        return allowedEventTypes.contains(event);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Set<Integer> getMappedGames() {
        return mappedGames;
    }

    public void setMappedGames(Set<Integer> mappedGames) {
        this.mappedGames = mappedGames;
    }

    public Set<String> getAllowedEventTypes() {
        return allowedEventTypes;
    }

    public void setAllowedEventTypes(Set<String> allowedEventTypes) {
        this.allowedEventTypes = allowedEventTypes;
    }
}
