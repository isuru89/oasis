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

package io.github.oasis.services.events.model;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

import java.util.Objects;
import java.util.Optional;

/**
 * @author Isuru Weerarathna
 */
@DataObject
public class UserInfo {

    private static final JsonObject EMPTY = new JsonObject();

    public static final String EMAIL = "email";
    public static final String ID = "id";
    public static final String GAMES = "games";
    public static final String TEAM = "team";

    private JsonObject ref;

    public static UserInfo create(String email, JsonObject other) {
        return new UserInfo(new JsonObject()
                .mergeIn(other)
                .put(EMAIL, email));
    }

    public UserInfo(JsonObject ref) {
        this.ref = ref;
    }

    public JsonObject toJson() {
        return ref;
    }

    public long getId() {
        return ref.getLong(ID);
    }

    public Optional<Long> getTeamId(int gameId) {
        JsonObject games = ref.getJsonObject(GAMES, EMPTY);
        JsonObject gameInfo = games.getJsonObject(String.valueOf(gameId));
        if (Objects.isNull(gameInfo)) {
            return Optional.empty();
        }
        return Optional.ofNullable(gameInfo.getLong(TEAM));
    }
}
