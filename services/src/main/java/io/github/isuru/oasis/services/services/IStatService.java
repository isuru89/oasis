package io.github.isuru.oasis.services.services;

import io.github.isuru.oasis.model.defs.LeaderboardType;
import io.github.isuru.oasis.services.dto.game.FeedItem;
import io.github.isuru.oasis.services.dto.game.FeedItemReq;
import io.github.isuru.oasis.services.dto.game.UserLeaderboardRankingsDto;
import io.github.isuru.oasis.services.dto.game.UserRankingsInRangeDto;
import io.github.isuru.oasis.services.dto.stats.BadgeBreakdownReqDto;
import io.github.isuru.oasis.services.dto.stats.BadgeBreakdownResDto;
import io.github.isuru.oasis.services.dto.stats.BadgeSummaryReq;
import io.github.isuru.oasis.services.dto.stats.BadgeSummaryRes;
import io.github.isuru.oasis.services.dto.stats.ChallengeInfoDto;
import io.github.isuru.oasis.services.dto.stats.MyLeaderboardReq;
import io.github.isuru.oasis.services.dto.stats.PointBreakdownReqDto;
import io.github.isuru.oasis.services.dto.stats.PointBreakdownResDto;
import io.github.isuru.oasis.services.dto.stats.PointSummaryReq;
import io.github.isuru.oasis.services.dto.stats.PointSummaryRes;
import io.github.isuru.oasis.services.dto.stats.TeamHistoryRecordDto;
import io.github.isuru.oasis.services.dto.stats.UserChallengeWinRes;
import io.github.isuru.oasis.services.dto.stats.UserMilestoneStatDto;
import io.github.isuru.oasis.services.dto.stats.UserScopeRankingsStat;
import io.github.isuru.oasis.services.dto.stats.UserStatDto;
import io.github.isuru.oasis.services.dto.stats.UserStateStatDto;

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
