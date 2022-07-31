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

package io.github.oasis.core.elements.spec;

import io.github.oasis.core.annotations.DefinitionDetails;
import io.github.oasis.core.elements.Validator;
import io.github.oasis.core.exception.OasisParseException;
import io.github.oasis.core.utils.Texts;
import lombok.Data;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author Isuru Weerarathna
 */
@Data
public class SelectorDef implements Validator, Serializable {

    @DefinitionDetails(description = "Event id to match a single event")
    private String matchEvent;
    @DefinitionDetails(description = "Criteria to match multiple events by event ids")
    private MatchEventsDef matchEvents;

    /**
     * Accepted point ids. These are same as event ids in game engine.
     */
    @DefinitionDetails(description = "Criteria to match multiple events by point ids")
    private MatchEventsDef matchPointIds;

    @DefinitionDetails(description = "Filter to match an event by its content.")
    private EventFilterDef filter;

    @DefinitionDetails(description = "Criteria to match events by time or date range")
    private AcceptsWithinDef acceptsWithin;

    public static SelectorDef singleEvent(String matchEvent) {
        SelectorDef def = new SelectorDef();
        def.setMatchEvent(matchEvent);
        return def;
    }

    @Override
    public void validate() throws OasisParseException {
        if (Texts.isEmpty(matchEvent) && matchEvents == null && matchPointIds == null) {
            throw new OasisParseException("Either 'matchEvent', 'matchEvents' or 'matchPointIds' must be specified!");
        }

        if (matchEvents != null) {
            matchEvents.validate();
        }

        if (matchPointIds != null) {
            matchPointIds.validate();
        }

        if (filter != null) {
            filter.validate();
        }

        if (Objects.nonNull(acceptsWithin)) {
            acceptsWithin.validate();
        }
    }
}
