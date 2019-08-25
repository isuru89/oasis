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

import io.github.oasis.services.admin.json.apps.NewApplicationJson;

import java.security.KeyPair;
import java.util.List;
import java.util.UUID;

/**
 * @author Isuru Weerarathna
 */
public class NewAppDto {

    private String name;
    private String token;

    private byte[] keySecret;
    private byte[] keyPublic;

    private boolean internal = false;

    private List<String> eventTypes;
    private List<Integer> gameIds;

    public NewAppDto() {
    }

    public static NewAppDto from(NewApplicationJson newApplicationJson) {
        NewAppDto dto = new NewAppDto();
        dto.name = newApplicationJson.getName();
        dto.token = UUID.randomUUID().toString().replace("-", "");
        dto.eventTypes = newApplicationJson.getEventTypes();
        dto.gameIds = newApplicationJson.getMappedGameIds();
        dto.internal = false;
        return dto;
    }

    public boolean forAllGames() {
        return gameIds == null || gameIds.isEmpty();
    }

    public boolean hasEvents() {
        return eventTypes != null && !eventTypes.isEmpty();
    }

    public NewAppDto assignKeys(KeyPair keyPair) {
        this.keySecret = keyPair.getPrivate().getEncoded();
        this.keyPublic = keyPair.getPublic().getEncoded();
        return this;
    }

    public List<String> getEventTypes() {
        return eventTypes;
    }

    public List<Integer> getGameIds() {
        return gameIds;
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
}
