package io.github.isuru.oasis.services.services;

import io.github.isuru.oasis.model.defs.LeaderboardType;
import io.github.isuru.oasis.services.api.dto.*;
import io.github.isuru.oasis.services.model.PurchasedItem;
import io.github.isuru.oasis.services.model.UserRankRecordDto;
import io.github.isuru.oasis.services.model.enums.ScopingType;

import java.util.List;

/**
 * @author iweerarathna
 */
public interface IStatService {

    PointBreakdownResDto getPointBreakdownList(PointBreakdownReqDto request) throws Exception;
    BadgeBreakdownResDto getBadgeBreakdownList(BadgeBreakdownReqDto request) throws Exception;

    UserStatDto readUserGameStats(long userId, long since) throws Exception;
    List<PurchasedItem> readUserPurchasedItems(long userId, long since) throws Exception;
    List<UserBadgeStatDto> readUserBadges(long userId, long since) throws Exception;
    List<UserMilestoneStatDto> readUserMilestones(long userId) throws Exception;
    List<UserRankRecordDto> readUserTeamRankings(long userId, boolean currentTeamOnly) throws Exception;
    List<UserRankRecordDto> readMyLeaderboardRankings(long gameId, long userId, ScopingType scopingType,
                                                      LeaderboardType rangeType) throws Exception;
    List<TeamHistoryRecordDto> readUserTeamHistoryStat(long userId) throws Exception;
    List<UserStateStatDto> readUserStateStats(long userId, long teamId) throws Exception;

    ChallengeInfoDto readChallengeStats(long challengeId) throws Exception;
    void readUserGameTimeline(long userId);

}
