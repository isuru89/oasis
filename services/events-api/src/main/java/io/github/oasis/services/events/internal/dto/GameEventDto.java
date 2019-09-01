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

package io.github.oasis.services.events.internal.dto;

import io.github.oasis.services.common.Validation;
import io.github.oasis.services.events.domain.UserId;
import io.github.oasis.services.events.internal.ErrorCodes;
import io.github.oasis.services.events.internal.exceptions.EventSubmissionException;
import io.github.oasis.services.events.json.NewEvent;

import java.util.HashMap;
import java.util.Map;

import static io.github.oasis.model.Event.EVENT_TYPE;
import static io.github.oasis.model.Event.GAME;
import static io.github.oasis.model.Event.ID;
import static io.github.oasis.model.Event.SOURCE;
import static io.github.oasis.model.Event.TIMESTAMP;
import static io.github.oasis.model.Event.USER;

/**
 * @author Isuru Weerarathna
 */
public class GameEventDto extends HashMap<String, Object> {

    public static GameEventDto from(NewEvent event) {
        GameEventDto dto = new GameEventDto()
                .addId(event.getId())
                .addEventType(event.getEventType())
                .addTs(event.getTs());
        if (Validation.isNonEmpty(event.getData())) {
            dto.addAllDataFields(event.getData());
        }
        return dto;
    }

    public GameEventDto cloneEventForGame(int gameId) {
        GameEventDto cloned = new GameEventDto();
        cloned.putAll(this);
        cloned.addGame(gameId);
        return cloned;
    }

    public String getId() {
        return (String) get(ID);
    }

    public int getUser() {
        return (Integer) get(USER);
    }

    public boolean hasId() {
        return containsKey(ID) && get(ID) != null;
    }

    public GameEventDto addId(String id) {
        put(ID, id);
        return this;
    }

    private GameEventDto addTs(long ts) {
        put(TIMESTAMP, ts);
        return this;
    }

    private GameEventDto addEventType(String type) {
        put(EVENT_TYPE, type);
        return this;
    }

    public GameEventDto addUser(UserId userId) {
        put(USER, userId.getId());
        return this;
    }

    public GameEventDto addEventSource(int extAppId) {
        put(SOURCE, extAppId);
        return this;
    }

    public GameEventDto addGame(int gameId) {
        put(GAME, gameId);
        return this;
    }

    private GameEventDto addAllDataFields(Map<? extends String, ?> other) throws EventSubmissionException {
        if (other.keySet().stream().anyMatch(key -> key.startsWith("_"))) {
            throw new EventSubmissionException(ErrorCodes.INVALID_DATA_FIELDS,
                    "No field in data cannot start with reserved '_'!");
        }
        super.putAll(other);
        return this;
    }

}
