package io.github.isuru.oasis.services.services;

import io.github.isuru.oasis.model.defs.LeaderboardType;
import io.github.isuru.oasis.services.dto.game.UserRankRecordDto;
import io.github.isuru.oasis.services.dto.stats.BadgeBreakdownReqDto;
import io.github.isuru.oasis.services.dto.stats.BadgeBreakdownResDto;
import io.github.isuru.oasis.services.dto.stats.ChallengeInfoDto;
import io.github.isuru.oasis.services.dto.stats.PointBreakdownReqDto;
import io.github.isuru.oasis.services.dto.stats.PointBreakdownResDto;
import io.github.isuru.oasis.services.dto.stats.TeamHistoryRecordDto;
import io.github.isuru.oasis.services.dto.stats.UserBadgeStatDto;
import io.github.isuru.oasis.services.dto.stats.UserMilestoneStatDto;
import io.github.isuru.oasis.services.dto.stats.UserStatDto;
import io.github.isuru.oasis.services.dto.stats.UserStateStatDto;
import io.github.isuru.oasis.services.model.PurchasedItem;
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
