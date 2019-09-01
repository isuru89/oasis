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
public final class EndPoints {

    private static final String PREFIX = "/admin";

    public static class GAME {
        private static final String GAME = PREFIX + "/game";

        public static final String GAME_ID = "id";

        public static final String START        = GAME + "/:" + GAME_ID + "/start";
        public static final String PAUSE        = GAME + "/:" + GAME_ID + "/pause";
        public static final String STOP         = GAME + "/:" + GAME_ID + "/stop";
        public static final String RESTART      = GAME + "/:" + GAME_ID + "/restart";
    }

    public static class APPS {
        private static final String APP = PREFIX + "/app";

        public static final String APP_ID = "id";

        public static final String REGISTER         = APP + "/register";
        public static final String LIST_ALL         = APP + "/all";
        public static final String DEACTIVATE       = APP + "/:" + APP_ID + "/deactivate";
        public static final String UPDATE           = APP + "/:" + APP_ID + "/update";
        public static final String DOWNLOAD_KEY     = APP + "/:" + APP_ID + "/download-key";

    }

}
