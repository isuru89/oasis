package io.github.isuru.oasis.services.services;

import io.github.isuru.oasis.model.defs.LeaderboardType;
import io.github.isuru.oasis.services.dto.game.FeedItem;
import io.github.isuru.oasis.services.dto.game.FeedItemReq;
import io.github.isuru.oasis.services.dto.game.UserRankRecordDto;
import io.github.isuru.oasis.services.dto.game.UserRankingsInRangeDto;
import io.github.isuru.oasis.services.dto.stats.*;
import io.github.isuru.oasis.services.model.PurchasedItem;
import io.github.isuru.oasis.model.defs.ScopingType;

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

    List<PurchasedItem> readUserPurchasedItems(long userId, long since) throws Exception;
    UserRankingsInRangeDto readUserTeamRankings(long userId) throws Exception;
    List<UserRankRecordDto> readMyLeaderboardRankings(long gameId, long userId, ScopingType scopingType,
                                                      LeaderboardType rangeType) throws Exception;
    UserScopeRankingsStat readMyRankings(long gameId, long userId, LeaderboardType rangeType) throws Exception;
    List<TeamHistoryRecordDto> readUserTeamHistoryStat(long userId) throws Exception;

    List<FeedItem> readUserGameTimeline(FeedItemReq req) throws Exception;

}
