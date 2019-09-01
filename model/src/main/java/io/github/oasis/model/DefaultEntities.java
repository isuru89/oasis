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

package io.github.oasis.model;

import io.github.oasis.model.defs.LeaderboardDef;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author iweerarathna
 */
public class DefaultEntities {

    public static final LeaderboardDef DEFAULT_LEADERBOARD_DEF;

    public static final double DEFAULT_RACE_WIN_VALUE = 50.0;

    public static final String DEFAULT_TEAM_NAME = "default.default";
    public static final String DEFAULT_TEAM_SCOPE_NAME = "Default";

    public static final String INTERNAL_EVENT_SOURCE_NAME = "oasis_internal";

    public static final String DEF_ADMIN_USER = "admin@oasis.com";
    public static final String DEF_CURATOR_USER = "curator@oasis.com";
    public static final String DEF_PLAYER_USER = "player@oasis.com";

    public static final Set<String> RESERVED_USERS = new HashSet<>(
            Arrays.asList(DEF_ADMIN_USER, DEF_PLAYER_USER, DEF_CURATOR_USER));

    static {
        DEFAULT_LEADERBOARD_DEF = new LeaderboardDef();
        DEFAULT_LEADERBOARD_DEF.setName("Oasis_Leaderboard");
        DEFAULT_LEADERBOARD_DEF.setDisplayName("Oasis Leaderboard");
        DEFAULT_LEADERBOARD_DEF.setOrderBy("asc");
    }

    public static String deriveDefaultTeamName(String teamScopeName) {
        return String.format("%s.default", teamScopeName);
    }

    public static String deriveDefScopeUser(String teamScopeName) {
        return String.format("default@%s.oasis.com", teamScopeName);
    }

    public static String deriveDefTeamUser(String teamName) {
        return String.format("user@%s.oasis.com", teamName);
    }
}
