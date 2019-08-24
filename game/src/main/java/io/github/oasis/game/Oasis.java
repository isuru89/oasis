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

package io.github.oasis.game;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Oasis implements Serializable {

    private final String id;
    private final Map<String, Object> gameVariables = new HashMap<>();

    public Oasis(String id) {
        this.id = id;
    }

    public void setGameVariables(Map<String, Object> variables) {
        gameVariables.putAll(variables);
    }

    public Map<String, Object> getGameVariables() {
        return gameVariables;
    }

    public String getId() {
        return id;
    }

}
