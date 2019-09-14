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

package io.github.oasis.services.admin.json.apps;

import io.github.oasis.services.common.Validation;
import lombok.Data;
import org.apache.commons.collections4.SetUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Isuru Weerarathna
 */
@Data
public class UpdateApplicationJson {

    private List<String> eventTypes;
    private List<Integer> gameIds;

    public List<String> newlyAddedEvents(Set<String> storedEvents) {
        return new ArrayList<>(SetUtils.difference(new HashSet<>(eventTypes), storedEvents));
    }

    public List<String> removedEvents(Set<String> storedEvents) {
        return new ArrayList<>(SetUtils.difference(storedEvents, new HashSet<>(eventTypes)));
    }

    public List<Integer> newlyAddedGames(Set<Integer> mappedGames) {
        return new ArrayList<>(SetUtils.difference(new HashSet<>(gameIds), mappedGames));
    }

    public List<Integer> removedGames(Set<Integer> mappedGames) {
        return new ArrayList<>(SetUtils.difference(mappedGames, new HashSet<>(gameIds)));
    }

    public boolean hasEventTypes() {
        return !Validation.isEmpty(eventTypes);
    }

    public boolean hasGameIds() {
        return !Validation.isEmpty(gameIds);
    }

    public boolean hasSomethingToUpdate() {
        return hasEventTypes() || hasGameIds();
    }

}
