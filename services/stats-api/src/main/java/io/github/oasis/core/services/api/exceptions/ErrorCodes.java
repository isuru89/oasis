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

    public static final String GAME_ID_SHOULD_NOT_SPECIFIED = "G0001";
    public static final String GAME_ID_NOT_SPECIFIED = "G0002";
    public static final String GAME_EXCEEDED_MOTTO_LEN = "G0003";
    public static final String GAME_EXCEEDED_DESC_LEND = "G0004";

    public static final String EVENT_SOURCE_NO_NAME = "ES0001";

    public static final String PLAYER_EXISTS = "PL0001";
    public static final String PLAYER_ALREADY_IN_TEAM = "PL0002";

    public static final String TEAM_EXISTS = "TM0001";
    public static final String TEAM_NOT_EXISTS = "TM0002";

    public static final String ELEMENT_ALREADY_EXISTS = "EL0001";
    public static final String ELEMENT_NOT_EXISTS = "EL0002";
}
