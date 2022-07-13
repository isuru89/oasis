/*
 *
 *  * Licensed to the Apache Software Foundation (ASF) under one
 *  * or more contributor license agreements.  See the NOTICE file
 *  * distributed with this work for additional information
 *  * regarding copyright ownership.  The ASF licenses this file
 *  * to you under the Apache License, Version 2.0 (the
 *  * "License"); you may not use this file except in compliance
 *  * with the License.  You may obtain a copy of the License at
 *  *
 *  *    http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing,
 *  * software distributed under the License is distributed on an
 *  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  * KIND, either express or implied.  See the License for the
 *  * specific language governing permissions and limitations
 *  * under the License.
 *
 */

package io.github.oasis.services.feeds;

public final class Constants {

    static final String ENV_CONFIG_FILE = "OASIS_CONFIG_FILE";

    static final String SYS_CONFIG_FILE = "oasis.config.file";

    public static final int DEF_CONNECT_TIMEOUT = 10;
    public static final int DEF_REQUEST_TIMEOUT = 15;

    public static final int DEF_GAME_CACHE_SIZE = 10;
    public static final int DEF_SOURCE_CACHE_SIZE = 10;
    public static final int DEF_TEAMS_CACHE_SIZE = 100;
    public static final int DEF_PLAYERS_CACHE_SIZE = 200;
    public static final int DEF_EXPIRE_DURATION = 900;

    /**
     * Configuration keys
     */
    public static final String GAMES = "games";
    public static final String EVENT_SOURCES = "eventSources";
    public static final String TEAMS = "teams";
    public static final String PLAYERS = "players";
    public static final String OASIS_CACHE_CONFIGS_EXPIRE_AFTER = "oasis.cache.configs.expireAfter";
    public static final String OASIS_CACHE_CONFIGS_MAX_ENTRIES = "oasis.cache.configs.maxEntries.";

    private Constants() {}
}
