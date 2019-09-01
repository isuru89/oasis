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

package io.github.oasis.services.model;

/**
 * @author iweerarathna
 */
public final class UserRole {

    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_CURATOR = "ROLE_CURATOR";
    public static final String ROLE_PLAYER = "ROLE_PLAYER";

    public static final int ADMIN = 1;
    public static final int CURATOR = 2;
    //public static final int TEAM_LEADER = 4;
    public static final int PLAYER = 8;

    public static final int ALL_ROLE = ADMIN | CURATOR | PLAYER;

    public static boolean hasRole(int role, int roleType) {
        return role > 0 && (role & roleType) == roleType;
    }
}
