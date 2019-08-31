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

package io.github.oasis.services.admin.internal;

/**
 * @author Isuru Weerarathna
 */
public final class ErrorCodes {

    public static final int NON_EXIST_APP = 40001;
    public static final int ALREADY_EXIST_APP = 40002;
    public static final int INVALID_APP_DETAILS = 40003;
    public static final int KEY_ALREADY_DOWNLOADED = 40004;

    public static final int GAME_ALREADY_REMOVED = 30001;
    public static final int GAME_CANNOT_START = 30002;
    public static final int GAME_CANNOT_PAUSE = 30003;
    public static final int GAME_CANNOT_STOP = 30004;

    public static final int GAME_CANNOT_CREATE = 30100;
    public static final int INVALID_GAME_DETAILS = 30101;

}
