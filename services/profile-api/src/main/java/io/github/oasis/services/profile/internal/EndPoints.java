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

package io.github.oasis.services.profile.internal;

/**
 * @author Isuru Weerarathna
 */
public final class EndPoints {

    private static final String USER = "/users";
    private static final String TEAM = "/teams";

    public static final class USER {
        public static final String CREATE       = USER;
        public static final String EDIT         = USER + "/:userId";
        public static final String DELETE       = USER + "/:userId";
        public static final String READ         = USER + "/:userId";

        public static final String TEAM_LIST    = USER + "/:userId/teams";

        public static final String ID           = "userId";
    }

    public static final class TEAM {
        public static final String CREATE       = TEAM;
        public static final String LIST         = TEAM;
        public static final String EDIT         = TEAM + "/:teamId";
        public static final String READ         = TEAM + "/:teamId";

        public static final String USER_LIST    = TEAM + "/:teamId/users";

        public static final String ALLOCATE     = TEAM + "/:teamId/users/:userId";
        public static final String DEALLOCATE   = TEAM + "/:teamId/users/:userId";

        public static final String ID       = "teamId";
        public static final String IDS      = "teamIds";
    }

}
