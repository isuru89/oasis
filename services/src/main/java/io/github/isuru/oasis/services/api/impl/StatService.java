package io.github.isuru.oasis.services.api.impl;

import io.github.isuru.oasis.model.defs.ChallengeDef;
import io.github.isuru.oasis.model.defs.LeaderboardDef;
import io.github.isuru.oasis.model.defs.LeaderboardType;
import io.github.isuru.oasis.services.api.IOasisApiService;
import io.github.isuru.oasis.services.api.IStatService;
import io.github.isuru.oasis.services.api.dto.BadgeBreakdownReqDto;
import io.github.isuru.oasis.services.api.dto.BadgeBreakdownResDto;
import io.github.isuru.oasis.services.api.dto.BadgeRecordDto;
import io.github.isuru.oasis.services.api.dto.ChallengeInfoDto;
import io.github.isuru.oasis.services.api.dto.ChallengeWinnerDto;
import io.github.isuru.oasis.services.api.dto.PointBreakdownReqDto;
import io.github.isuru.oasis.services.api.dto.PointBreakdownResDto;
import io.github.isuru.oasis.services.api.dto.PointRecordDto;
import io.github.isuru.oasis.services.api.dto.TeamHistoryRecordDto;
import io.github.isuru.oasis.services.api.dto.UserBadgeStatDto;
import io.github.isuru.oasis.services.api.dto.UserMilestoneStatDto;
import io.github.isuru.oasis.services.api.dto.UserStatDto;
import io.github.isuru.oasis.services.exception.InputValidationException;
import io.github.isuru.oasis.services.model.LeaderboardRequestDto;
import io.github.isuru.oasis.services.model.PurchasedItem;
import io.github.isuru.oasis.services.model.UserRankRecordDto;
import io.github.isuru.oasis.services.model.UserTeam;
import io.github.isuru.oasis.services.model.enums.ScopingType;
import io.github.isuru.oasis.services.utils.Checks;
import io.github.isuru.oasis.services.utils.Maps;

import java.beans.Statement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author iweerarathna
 */
public class StatService extends BaseService implements IStatService {

    StatService(IOasisApiService apiService) {
        super(apiService);
    }

    @Override
    public PointBreakdownResDto getPointBreakdownList(PointBreakdownReqDto request) throws Exception {
        Checks.greaterThanZero(request.getUserId(), "userId");
        Checks.greaterThanZero(request.getPointId(), "pointId");

        Map<String, Object> conditions = Maps.create()
                .put("hasOffset", isValid(request.getOffset()))
                .put("hasSize", isValid(request.getSize()))
                .put("hasRangeStart", isValid(request.getRangeStart()))
                .put("hasRangeEnd", isValid(request.getRangeEnd()))
                .build();

        List<PointRecordDto> recordDtos = toList(getDao().executeQuery("stats/getUserPointsList",
                Maps.create().put("userId", request.getUserId())
                    .put("pointId", request.getPointId())
                    .put("offset", orDefault(request.getOffset(), 0))
                    .put("size", orDefault(request.getSize(), 100))
                    .put("rangeStart", orDefault(request.getRangeStart(), 0L))
                    .put("rangeEnd", orDefault(request.getRangeEnd(), 0L))
                    .build(),
                PointRecordDto.class,
                conditions));

        PointBreakdownResDto resDto = new PointBreakdownResDto();
        resDto.setRecords(recordDtos);
        resDto.setCount(recordDtos.size());
        return resDto;
    }

    @Override
    public BadgeBreakdownResDto getBadgeBreakdownList(BadgeBreakdownReqDto request) throws Exception {
        Checks.greaterThanZero(request.getUserId(), "userId");

        Map<String, Object> conditions = Maps.create()
                .put("hasBadgeId", isValid(request.getBadgeId()))
                .put("hasOffset", isValid(request.getOffset()))
                .put("hasSize", isValid(request.getSize()))
                .put("hasRangeStart", isValid(request.getRangeStart()))
                .put("hasRangeEnd", isValid(request.getRangeEnd()))
                .build();

        List<BadgeRecordDto> recordDtos = toList(getDao().executeQuery("stats/getUserBadgesList",
                Maps.create().put("userId", request.getUserId())
                        .put("badgeId", orDefault(request.getBadgeId(), 0))
                        .put("offset", orDefault(request.getOffset(), 0))
                        .put("size", orDefault(request.getSize(), 100))
                        .put("rangeStart", orDefault(request.getRangeStart(), 0L))
                        .put("rangeEnd", orDefault(request.getRangeEnd(), 0L))
                        .build(),
                BadgeRecordDto.class,
                conditions));

        BadgeBreakdownResDto resDto = new BadgeBreakdownResDto();
        resDto.setRecords(recordDtos);
        resDto.setCount(recordDtos.size());
        return resDto;
    }

    @Override
    public UserStatDto readUserGameStats(long userId, long since) throws Exception {
        Checks.greaterThanZero(userId, "userId");

        Map<String, Object> tdata = new HashMap<>();
        tdata.put("hasSince", since > 0);

        Iterable<Map<String, Object>> summaryStat = getDao().executeQuery(
                "stats/getUserStatSummary",
                Maps.create()
                        .put("userId", userId)
                        .put("since", since)
                        .build(),
                tdata);

        UserStatDto dto = new UserStatDto();
        dto.setUserId((int)userId);
        for (Map<String, Object> row : summaryStat) {
            Statement stmt = new Statement(dto,
                    "set" + row.get("type").toString(),
                    new Object[] { firstNonNull(row.get("value_i"), row.get("value_f")) });
            stmt.execute();
        }
        return dto;
    }

    @Override
    public List<PurchasedItem> readUserPurchasedItems(long userId, long since) throws Exception {
        Checks.greaterThanZero(userId, "userId");

        Map<String, Object> tdata = new HashMap<>();
        tdata.put("hasSince", since > 0);

        return toList(getDao().executeQuery("stats/getPurchasedItems",
                Maps.create()
                    .put("userId", userId).put("since", since)
                    .build(),
                PurchasedItem.class,
                tdata));
    }

    @Override
    public List<UserBadgeStatDto> readUserBadges(long userId, long since) throws Exception {
        Checks.greaterThanZero(userId, "userId");

        Map<String, Object> tdata = new HashMap<>();
        tdata.put("hasSince", since > 0);

        return toList(getDao().executeQuery(
                "stats/getUserBadgesStat",
                Maps.create().put("userId", userId).put("since", since).build(),
                UserBadgeStatDto.class,
                tdata));
    }

    @Override
    public List<UserMilestoneStatDto> readUserMilestones(long userId) throws Exception {
        Checks.greaterThanZero(userId, "userId");

        return toList(getDao().executeQuery(
                "stats/getUserMilestoneStat",
                Maps.create("userId", userId),
                UserMilestoneStatDto.class));
    }

    @Override
    public List<UserRankRecordDto> readUserTeamRankings(long userId, boolean currentTeamOnly) throws Exception {
        Checks.greaterThanZero(userId, "userId");

        UserTeam currentTeamOfUser = getApiService().getProfileService().findCurrentTeamOfUser(userId);

        Map<String, Object> tdata = new HashMap<>();
        tdata.put("teamWise", currentTeamOfUser != null && currentTeamOnly);
        tdata.put("hasTeamScope", currentTeamOfUser != null);

        int tid = currentTeamOfUser == null ? 0 : currentTeamOfUser.getTeamId();
        int sid = currentTeamOfUser == null ? 0 : currentTeamOfUser.getScopeId();
        return toList(getDao().executeQuery("stats/getUserTeamRanking",
                Maps.create()
                    .put("userId", userId)
                    .put("teamId", tid)
                    .put("teamScopeId", sid).build(),
                UserRankRecordDto.class,
                tdata
        ));
    }

    @Override
    public List<UserRankRecordDto> readMyLeaderboardRankings(long gameId, long userId, ScopingType scopingType,
                                                             LeaderboardType rangeType) throws Exception {
        Checks.greaterThanZero(userId, "userId");
        Checks.nonNull(scopingType, "scopeType");

        UserTeam currentTeamOfUser = getApiService().getProfileService().findCurrentTeamOfUser(userId);
        List<LeaderboardDef> leaderboardDefs = getApiService().getGameDefService().listLeaderboardDefs(gameId);
        List<UserRankRecordDto> rankings = new LinkedList<>();

        for (LeaderboardDef def : leaderboardDefs) {
            LeaderboardRequestDto requestDto = rangeType == LeaderboardType.CUSTOM ?
                    new LeaderboardRequestDto(1, System.currentTimeMillis())
                    : new LeaderboardRequestDto(rangeType, System.currentTimeMillis());
            requestDto.setForUser(userId);
            requestDto.setLeaderboardDef(def);

            List<UserRankRecordDto> userRankRecordDtos = null;
            if (scopingType == ScopingType.TEAM) {
                userRankRecordDtos = getApiService().getGameService()
                        .readTeamLeaderboard(currentTeamOfUser.getTeamId(), requestDto);
            } else if (scopingType == ScopingType.TEAM_SCOPE) {
                userRankRecordDtos = getApiService().getGameService()
                        .readTeamScopeLeaderboard(currentTeamOfUser.getScopeId(), requestDto);
            } else if (scopingType == ScopingType.GLOBAL) {
                userRankRecordDtos = getApiService().getGameService()
                        .readGlobalLeaderboard(requestDto);
            }

            if (userRankRecordDtos != null && !userRankRecordDtos.isEmpty()) {
                UserRankRecordDto record = userRankRecordDtos.get(0);
                record.setLeaderboard(def);
                rankings.add(record);
            }
        }
        return rankings;
    }

    @Override
    public List<TeamHistoryRecordDto> readUserTeamHistoryStat(long userId) throws Exception {
        Checks.greaterThanZero(userId, "userId");

        List<TeamHistoryRecordDto> historyRecords = toList(getDao().executeQuery(
                "stats/teamWiseSummaryStats",
                    Maps.create("userId", userId),
                    TeamHistoryRecordDto.class))
                .stream()
                .sorted(Comparator.comparingInt(TeamHistoryRecordDto::getTeamId))
                .collect(Collectors.toList());

        if (historyRecords.isEmpty()) {
            return historyRecords;
        }

        List<TeamHistoryRecordDto> accumulated = new ArrayList<>();
        Iterator<TeamHistoryRecordDto> iterator = historyRecords.iterator();
        TeamHistoryRecordDto base = iterator.next();
        while (iterator.hasNext()) {
            TeamHistoryRecordDto current = iterator.next();
            if (current.getTeamId().equals(base.getTeamId())) {
                mergeTwoTeamRecords(base, current);
            } else {
                // new record
                accumulated.add(base);
                base = current;
            }
        }
        accumulated.add(base);
        return accumulated;
    }

    @Override
    public ChallengeInfoDto readChallengeStats(long challengeId) throws Exception {
        Checks.greaterThanZero(challengeId, "challengeId");

        ChallengeDef def = getApiService().getGameDefService().readChallenge(challengeId);
        if (def != null) {
            ChallengeInfoDto challengeInfoDto = new ChallengeInfoDto();
            List<ChallengeWinnerDto> winners = toList(getDao().executeQuery(
                    "stats/getChallengeWinners",
                    Maps.create("challengeId", challengeId),
                    ChallengeWinnerDto.class));

            challengeInfoDto.setWinners(winners);
            challengeInfoDto.setChallengeDef(def);
            return challengeInfoDto;

        } else {
            throw new InputValidationException("No challenge is found by id " + challengeId + "!");
        }
    }

    @Override
    public void readUserGameTimeline(long userId) {

    }

    private void mergeTwoTeamRecords(TeamHistoryRecordDto base, TeamHistoryRecordDto curr) {
        if (curr.getTotalBadges() != null && base.getTotalBadges() == null) {
            base.setTotalBadges(curr.getTotalBadges());
        }
        if (curr.getTotalChallengeWins() != null && base.getTotalChallengeWins() == null) {
            base.setTotalChallengeWins(curr.getTotalChallengeWins());
        }
        if (curr.getTotalPoints() != null && base.getTotalPoints() == null) {
            base.setTotalPoints(curr.getTotalPoints());
        }
        if (curr.getTotalUniqueBadges() != null && base.getTotalUniqueBadges() == null) {
            base.setTotalUniqueBadges(curr.getTotalBadges());
        }
    }

    private Object firstNonNull(Object... vals) {
        for (Object o : vals) {
            if (o != null) {
                return o;
            }
        }
        return null;
    }
}
