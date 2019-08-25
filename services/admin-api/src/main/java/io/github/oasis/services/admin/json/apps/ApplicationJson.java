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

import io.github.oasis.services.admin.internal.dto.ExtAppRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Isuru Weerarathna
 */
public class ApplicationJson {

    private int id;
    private String name;
    private String token;

    private List<String> eventTypes;
    private List<ExtAppRecord.GameDef> mappedGameIds;

    private boolean internal = false;
    private boolean downloaded = true;

    public static ApplicationJson from(ExtAppRecord record) {
        ApplicationJson json = new ApplicationJson();
        json.id = record.getId();
        json.name = record.getName();
        json.token = record.getToken();
        json.downloaded = record.isDownloaded();
        json.internal = record.isInternal();

        json.eventTypes = record.getEventTypes().stream()
                .map(ExtAppRecord.EventType::getEventType)
                .collect(Collectors.toList());
        json.mappedGameIds = new ArrayList<>(record.getMappedGames());

        return json;
    }

    public boolean isInternal() {
        return internal;
    }

    public boolean isDownloaded() {
        return downloaded;
    }

    public String getToken() {
        return token;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<String> getEventTypes() {
        return eventTypes;
    }

    public List<ExtAppRecord.GameDef> getMappedGameIds() {
        return mappedGameIds;
    }
}
