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

import io.github.oasis.core.utils.Utils;

import java.io.Serializable;
import java.util.Map;

/**
 * @author Isuru Weerarathna
 */
public class PersistedDef implements Serializable {

    public static final String GAME_ADDED = "GAME.ADDED";
    public static final String GAME_REMOVED = "GAME.REMOVED";
    public static final String GAME_EVENT = "GAME.EVENT";

    private String type;
    private String impl;
    private Map<String, Object> data;

    public PersistedDef() {
    }

    public String getImpl() {
        return impl;
    }

    public String getType() {
        return type;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public boolean isEvent() {
        return GAME_EVENT.equals(type);
    }

    public boolean isGameAdded() {
        return GAME_ADDED.equals(type);
    }

    public boolean isGameRemoved() {
        return GAME_REMOVED.equals(type);
    }
}
