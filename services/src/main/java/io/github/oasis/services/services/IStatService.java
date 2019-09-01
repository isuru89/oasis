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

package io.github.oasis.services.services;

import io.github.oasis.model.defs.LeaderboardType;
import io.github.oasis.services.dto.game.FeedItem;
import io.github.oasis.services.dto.game.FeedItemReq;
import io.github.oasis.services.dto.game.UserLeaderboardRankingsDto;
import io.github.oasis.services.dto.game.UserRankingsInRangeDto;
import io.github.oasis.services.dto.stats.BadgeBreakdownReqDto;
import io.github.oasis.services.dto.stats.BadgeBreakdownResDto;
import io.github.oasis.services.dto.stats.BadgeSummaryReq;
import io.github.oasis.services.dto.stats.BadgeSummaryRes;
import io.github.oasis.services.dto.stats.ChallengeInfoDto;
import io.github.oasis.services.dto.stats.MyLeaderboardReq;
import io.github.oasis.services.dto.stats.PointBreakdownReqDto;
import io.github.oasis.services.dto.stats.PointBreakdownResDto;
import io.github.oasis.services.dto.stats.PointSummaryReq;
import io.github.oasis.services.dto.stats.PointSummaryRes;
import io.github.oasis.services.dto.stats.TeamHistoryRecordDto;
import io.github.oasis.services.dto.stats.UserChallengeWinRes;
import io.github.oasis.services.dto.stats.UserMilestoneStatDto;
import io.github.oasis.services.dto.stats.UserScopeRankingsStat;
import io.github.oasis.services.dto.stats.UserStatDto;
import io.github.oasis.services.dto.stats.UserStateStatDto;

import java.util.List;

/**
 * @author iweerarathna
 */
public interface IStatService {

    PointBreakdownResDto getPointBreakdownList(PointBreakdownReqDto request) throws Exception;
    PointSummaryRes getPointSummary(PointSummaryReq request) throws Exception;
    BadgeBreakdownResDto getBadgeBreakdownList(BadgeBreakdownReqDto request) throws Exception;
    BadgeSummaryRes getBadgeSummary(BadgeSummaryReq request) throws Exception;
    UserStatDto readUserGameStats(long userId, long since) throws Exception;

    List<UserMilestoneStatDto> readUserMilestones(long userId) throws Exception;
    ChallengeInfoDto readChallengeStats(long challengeId) throws Exception;
    UserChallengeWinRes readUserChallengeWins(long userId) throws Exception;
    List<UserStateStatDto> readUserStateStats(long userId) throws Exception;

    UserRankingsInRangeDto readUserTeamRankings(long gameId, long userId, long leaderboardId, long timestamp) throws Exception;
    List<UserLeaderboardRankingsDto> readMyLeaderboardRankings(long gameId, long userId, MyLeaderboardReq req) throws Exception;
    UserScopeRankingsStat readMyRankings(long gameId, long userId, LeaderboardType rangeType) throws Exception;
    List<TeamHistoryRecordDto> readUserTeamHistoryStat(long userId) throws Exception;

    List<FeedItem> readUserGameTimeline(FeedItemReq req) throws Exception;

}
