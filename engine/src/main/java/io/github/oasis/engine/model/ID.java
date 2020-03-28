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

package io.github.oasis.engine.model;

/**
 * @author Isuru Weerarathna
 */
public final class ID {

    public static String getUserFirstEventsKey(int gameId, long userId) {
        return String.format("u%d:g%d:firstevents", userId, gameId);
    }

    public static String getBadgeHistogramKey(int gameId, long userId, String badgeId) {
        return String.format("u%d:g%d:badges:h:%s", userId, gameId, badgeId);
    }

    public static String getUserBadgeSpecKey(int gameId, long userId, String badgeId) {
        return String.format("u%d:g%d:badges:%s", userId, gameId, badgeId);
    }

    public static String getUserBadgeStreakKey(int gameId, long userId, String badgeId) {
        return String.format("u%d:g%d:%s:streak", userId, gameId, badgeId);
    }

    public static String getUserTemporalBadgeKey(int gameId, long userId, String badgeId) {
        return String.format("u%d:g%d:%s:temporal", userId, gameId, badgeId);
    }

    public static String getUserBadgesMetaKey(int gameId, long userId) {
        return String.format("u%d:g%d:bgmeta", gameId, userId);
    }

}
