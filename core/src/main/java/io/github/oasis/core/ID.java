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

import io.github.oasis.core.utils.Texts;

/**
 * @author Isuru Weerarathna
 */
public final class ID {

    public static String getGameUserPointsSummary(int gameId, long userId) {
        return String.format("g%d:u%d:points", gameId, userId);
    }

    public static String getGameLeaderboard(int gameId, String trait, String duration) {
        if (Texts.isEmpty(duration)) {
            return String.format("g%d:leaderboard:%s", gameId, trait);
        } else {
            return String.format("g%d:leaderboard:%s:%s", gameId, trait, duration);
        }
    }

    public static String getGameTeamLeaderboard(int gameId, long teamId, String trait, String duration) {
        if (Texts.isEmpty(duration)) {
            return String.format("g%d:t%d:leaderboard:%s", gameId, teamId, trait);
        } else {
            return String.format("g%d:t%d:leaderboard:%s:%s", gameId, teamId, trait, duration);
        }
    }

    public static String getGameUserMilestonesSummary(int gameId, long userId) {
        return String.format("g%d:u%d:milestones", gameId, userId);
    }

    public static String getGameUseChallengesSummary(int gameId, long userId) {
        return String.format("g%d:u%d:challenges", gameId, userId);
    }

    public static String getGameUseChallengesLog(int gameId, long userId) {
        return String.format("g%d:u%d:challengeslog", gameId, userId);
    }

    public static String getGameUserBadgesSummary(int gameId, long userId) {
        return String.format("g%d:u%d:badges", gameId, userId);
    }

    public static String getGameUserBadgesLog(int gameId, long userId) {
        return String.format("g%d:u%d:badgeslog", gameId, userId);
    }

    public static String getGameUserRatingsLog(int gameId, long userId) {
        return String.format("g%d:u%d:ratingslog", gameId, userId);
    }

    public static String getUserRatingsKey(int gameId, long userId, String ratingId) {
        return String.format("u%d:g%d:rt:%s", userId, gameId, ratingId);
    }

    public static String getGameRatingKey(int gameId, String ratingId) {
        return String.format("g%d:rating:%s", gameId, ratingId);
    }

    public static String getGameChallengesKey(int gameId) {
        return String.format("g%d:challenges", gameId);
    }

    public static String getGameChallengeKey(int gameId, String ruleId) {
        return String.format("g%d:ch:%s", gameId, ruleId);
    }

    public static String getGameChallengeEventsKey(int gameId, String ruleId) {
        return String.format("g%d:challengeevents:%s", gameId, ruleId);
    }

    public static String getGameChallengeOOORefKey(int gameId, String ruleId) {
        return String.format("g%d:challengeoooref:%s", gameId, ruleId);
    }

    public static String getGameChallengeSubKey(String ruleId, String metaStatus) {
        return String.format("%s:%s", ruleId, metaStatus);
    }

    public static String getUserKeyUnderGameMilestone(long userId) {
        return String.format("u%d", userId);
    }

    public static String getPenaltiesUserKeyUnderGameMilestone(long userId) {
        return String.format("u%d:penalties", userId);
    }

    public static String getUserGameMilestonesKey(int gameId, long userId) {
        return String.format("u%d:g%d:milestones", userId, gameId);
    }

    public static String getGameMilestoneKey(int gameId, String milestoneId) {
        return String.format("g%d:ms:%s", gameId, milestoneId);
    }

    public static String getGameMilestoneSummaryKey(int gameId, String milestoneId) {
        return String.format("g%d:ms:%s:summary", gameId, milestoneId);
    }

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
        return String.format("u%d:g%d:bgmeta", userId, gameId);
    }

}
