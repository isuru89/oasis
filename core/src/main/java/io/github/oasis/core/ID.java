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

package io.github.oasis.core;

/**
 * @author Isuru Weerarathna
 */
public final class ID {

    public static final String ENGINE_STATUS_CHANNEL = "game.status.channel";
    public static final String GAME_ENGINES = "oasis.engines.games";

    public static final String ALL_GAMES = "oasis:games";
    public static final String ALL_USERS = "oasis:users";
    public static final String ALL_TEAMS = "oasis:teams";
    public static final String ALL_SOURCES = "oasis:sources";
    public static final String ALL_TEAMS_INDEX = "oasis:teams:index";
    public static final String ALL_USERS_INDEX = "oasis:users:index";
    public static final String ALL_GAMES_INDEX = "oasis:games:index";
    public static final String ALL_SOURCES_INDEX = "oasis:sources:token";
    public static final String ALL_USERS_TEAMS = "oasis:users:teams";
    public static final String ALL_USERS_NAMES = "oasis:users:names";
    public static final String ALL_TEAMS_NAMES = "oasis:teams:names";
    public static final String ALL_TEAMS_USERS = "oasis:teams:users";
    public static final String ALL_BASIC_ELEMENT_DEFINITIONS = "oasis:g%d:element:defs";
    public static final String ALL_DETAILED_ELEMENT_DEFINITIONS = "oasis:g%d:element:detailed";
    public static final String ALL_ELEMENTS_BY_TYPE = "oasis:g%d:elementtype:%s";
    public static final String ALL_ATTRIBUTE_DEFINITIONS = "oasis:g%d:attributes:defs";

    public static String getDetailedElementDefKeyForGame(int gameId) {
        return String.format(ALL_DETAILED_ELEMENT_DEFINITIONS, gameId);
    }

    public static String getBasicElementDefKeyForGame(int gameId) {
        return String.format(ALL_BASIC_ELEMENT_DEFINITIONS, gameId);
    }

    public static String getGameAttributesInfoKey(int gameId) {
        return String.format(ALL_ATTRIBUTE_DEFINITIONS, gameId);
    }

    public static String getElementMetadataByTypeForGame(int gameId, String type) {
        return String.format(ALL_ELEMENTS_BY_TYPE, gameId, type);
    }

}
