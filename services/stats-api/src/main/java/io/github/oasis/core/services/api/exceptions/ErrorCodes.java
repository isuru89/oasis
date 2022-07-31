/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.github.oasis.core.services.api.exceptions;

/**
 * @author Isuru Weerarathna
 */
public final class ErrorCodes {

    public static final String INVALID_PARAMETER = "GEN001";

    public static final String AUTH_BAD_CREDENTIALS = "AUTH001";
    public static final String AUTH_NO_SUCH_CREDENTIALS = "AUTH002";

    public static final String GAME_ID_SHOULD_NOT_SPECIFIED = "G0001";
    public static final String GAME_EXCEEDED_MOTTO_LEN = "G0003";
    public static final String GAME_EXCEEDED_DESC_LEND = "G0004";
    public static final String GAME_ALREADY_EXISTS = "G0005";
    public static final String GAME_UNKNOWN_STATE = "G0006";
    public static final String GAME_NOT_EXISTS = "G0007";
    public static final String GAME_UPDATE_CONFLICT = "G0008";

    public static final String UNABLE_TO_CHANGE_GAME_STATE = "EG0001";


    public static final String EVENT_SOURCE_ALREADY_EXISTS = "ES0002";
    public static final String EVENT_SOURCE_ALREADY_MAPPED = "ES0003";
    public static final String EVENT_SOURCE_NOT_EXISTS = "ES0004";
    public static final String EVENT_SOURCE_DOWNLOAD_LIMIT_EXCEEDED = "ES0005";

    public static final String PLAYER_EXISTS = "PL0001";
    public static final String PLAYER_ALREADY_IN_TEAM = "PL0002";
    public static final String PLAYER_DOES_NOT_EXISTS = "PL0003";
    public static final String PLAYER_IS_DEACTIVATED = "PL0004";
    public static final String PLAYER_UPDATE_CONFLICT = "PL0005";

    public static final String TEAM_EXISTS = "TM0001";
    public static final String TEAM_NOT_EXISTS = "TM0002";
    public static final String TEAM_UPDATE_CONFLICT = "TM0003";

    public static final String ELEMENT_ALREADY_EXISTS = "EL0001";
    public static final String ELEMENT_NOT_EXISTS = "EL0002";
    public static final String ELEMENT_SPEC_INVALID = "EL0003";
    public static final String ELEMENT_UPDATE_CONFLICT = "EL0004";

    public static final String RANK_EXISTS = "RK0001";

    public static final String MODULE_DOES_NOT_EXISTS = "MOD404";
}
