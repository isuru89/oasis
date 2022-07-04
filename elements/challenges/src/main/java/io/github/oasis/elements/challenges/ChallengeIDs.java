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

package io.github.oasis.elements.challenges;

/**
 * @author Isuru Weerarathna
 */
public final class ChallengeIDs {

    public static String getGameUseChallengesSummary(int gameId, long userId) {
        return String.format("{g%d}:u%d:challenges", gameId, userId);
    }

    public static String getGameUseChallengesLog(int gameId, long userId) {
        return String.format("{g%d}:u%d:challengeslog", gameId, userId);
    }

    public static String getGameChallengesKey(int gameId) {
        return String.format("{g%d}:challenges", gameId);
    }

    public static String getGameChallengeKey(int gameId, String ruleId) {
        return String.format("{g%d}:ch:%s", gameId, ruleId);
    }

    public static String getGameChallengeEventsKey(int gameId, String ruleId) {
        return String.format("{g%d}:challengeevents:%s", gameId, ruleId);
    }

    public static String getGameChallengeOOORefKey(int gameId, String ruleId) {
        return String.format("{g%d}:challengeoooref:%s", gameId, ruleId);
    }

    public static String getGameChallengeSubKey(String ruleId, String metaStatus) {
        return String.format("%s:%s", ruleId, metaStatus);
    }


}
