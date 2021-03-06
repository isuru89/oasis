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

package io.github.oasis.core.external.messages;

import io.github.oasis.core.Event;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 * @author Isuru Weerarathna
 */
@Getter
@Setter
public class PersistedDef implements Serializable {

    public static final String FIELD_TYPE = "type";
    public static final String FIELD_IMPL = "impl";
    public static final String FIELD_SCOPE = "scope";
    public static final String FIELD_DATA = "data";

    public static final String GAME_CREATED = "GAME.CREATED";
    public static final String GAME_PAUSED = "GAME.PAUSED";
    public static final String GAME_STARTED = "GAME.STARTED";
    public static final String GAME_REMOVED = "GAME.REMOVED";
    public static final String GAME_UPDATED = "GAME.UPDATED";
    public static final String GAME_EVENT = "GAME.EVENT";
    public static final String GAME_RULE_ADDED = "GAME.RULE.ADDED";
    public static final String GAME_RULE_REMOVED = "GAME.RULE.REMOVED";
    public static final String GAME_RULE_ACTIVATED = "GAME.RULE.ACTIVATED";
    public static final String GAME_RULE_DEACTIVATED = "GAME.RULE.DEACTIVATED";
    public static final String GAME_RULE_UPDATED = "GAME.RULE.UPDATED";

    private static final Set<String> ALL_GAME_LIFECYCLE_TYPES = Set.of(
            GAME_CREATED,
            GAME_PAUSED,
            GAME_STARTED,
            GAME_REMOVED,
            GAME_UPDATED);
    private static final Set<String> ALL_RULE_TYPES = Set.of(
            GAME_RULE_ADDED,
            GAME_RULE_REMOVED,
            GAME_RULE_UPDATED,
            GAME_RULE_ACTIVATED,
            GAME_RULE_DEACTIVATED);

    private String type;
    private String impl;
    private Scope scope;
    private Object messageId;
    private Map<String, Object> data;

    public PersistedDef() {
    }

    public static PersistedDef fromEvent(Event event) {
        PersistedDef def = new PersistedDef();
        def.setType(PersistedDef.GAME_EVENT);
        def.setScope(new PersistedDef.Scope(event.getGameId()));
        def.setData(event.getAllFieldValues());
        return def;
    }

    public static PersistedDef createGameLifecycleEvent(int gameId, GameState gameStatus) {
        PersistedDef def = new PersistedDef();
        def.setType(gameStatus.getCommand());
        def.setScope(new PersistedDef.Scope(gameId));
        return def;
    }

    public boolean isEvent() {
        return GAME_EVENT.equals(type);
    }

    public boolean isGameLifecycleEvent() {
        return ALL_GAME_LIFECYCLE_TYPES.contains(type);
    }

    public boolean isRuleEvent() {
        return ALL_RULE_TYPES.contains(type);
    }



    @Override
    public String toString() {
        return "PersistedDef{" +
                "type='" + type + '\'' +
                ", impl='" + impl + '\'' +
                ", scope=" + scope +
                ", messageId=" + messageId +
                ", data=" + data +
                '}';
    }

    public static class Scope implements Serializable {
        private Integer gameId;

        public Scope() {
        }

        public Scope(Integer gameId) {
            this.gameId = gameId;
        }

        public Integer getGameId() {
            return gameId;
        }

        public void setGameId(Integer gameId) {
            this.gameId = gameId;
        }

        @Override
        public String toString() {
            return "Scope{" +
                    "gameId=" + gameId +
                    '}';
        }
    }
}
