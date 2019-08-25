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

package io.github.oasis.services.admin.internal.dto;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * @author Isuru Weerarathna
 */
public class ExtAppRecord {

    private int id;
    private String name;
    private String token;

    private byte[] keySecret;
    private byte[] keyPublic;

    private boolean internal;
    private boolean downloaded;

    private Set<EventType> eventTypes = new HashSet<>();

    private Set<GameDef> mappedGames = new HashSet<>();

    public Set<GameDef> getMappedGames() {
        return mappedGames;
    }

    public Set<EventType> getEventTypes() {
        return eventTypes;
    }

    public void setEventTypes(Set<EventType> eventTypes) {
        this.eventTypes = eventTypes;
    }

    public void setMappedGames(Set<GameDef> mappedGames) {
        this.mappedGames = mappedGames;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setKeySecret(byte[] keySecret) {
        this.keySecret = keySecret;
    }

    public void setKeyPublic(byte[] keyPublic) {
        this.keyPublic = keyPublic;
    }

    public void setInternal(boolean internal) {
        this.internal = internal;
    }

    public void setDownloaded(boolean downloaded) {
        this.downloaded = downloaded;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getToken() {
        return token;
    }

    public byte[] getKeySecret() {
        return keySecret;
    }

    public byte[] getKeyPublic() {
        return keyPublic;
    }

    public boolean isInternal() {
        return internal;
    }

    public boolean isDownloaded() {
        return downloaded;
    }

    public static class EventType {
        private String eventType;

        public String getEventType() {
            return eventType;
        }

        public void setEventType(String eventType) {
            this.eventType = eventType;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            EventType eventType1 = (EventType) o;
            return Objects.equals(eventType, eventType1.eventType);
        }

        @Override
        public int hashCode() {
            return Objects.hash(eventType);
        }
    }

    public static class GameDef {
        private int id;
        private String name;
        private String description;

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public void setId(int id) {
            this.id = id;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            GameDef gameDef = (GameDef) o;
            return id == gameDef.id;
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }
    }

}
