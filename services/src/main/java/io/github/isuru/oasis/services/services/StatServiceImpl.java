package io.github.isuru.oasis.services.services;

import io.github.isuru.oasis.model.db.IOasisDao;
import io.github.isuru.oasis.model.defs.ChallengeDef;
import io.github.isuru.oasis.model.defs.LeaderboardDef;
import io.github.isuru.oasis.model.defs.LeaderboardType;
import io.github.isuru.oasis.model.defs.ScopingType;
import io.github.isuru.oasis.services.DataCache;
import io.github.isuru.oasis.services.dto.game.*;
import io.github.isuru.oasis.services.dto.stats.*;
import io.github.isuru.oasis.services.exception.InputValidationException;
import io.github.isuru.oasis.services.model.PurchasedItem;
import io.github.isuru.oasis.services.model.UserTeam;
import io.github.isuru.oasis.services.utils.Checks;
import io.github.isuru.oasis.services.utils.Commons;
import io.github.isuru.oasis.services.utils.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.beans.Statement;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author iweerarathna
 */
@Service("statService")
public class StatServiceImpl implements IStatService {

    private static final List<LeaderboardType> RANGES = Arrays.asList(LeaderboardType.CURRENT_DAY,
            LeaderboardType.CURRENT_WEEK, LeaderboardType.CURRENT_MONTH);

    @Autowired
    private IOasisDao dao;

    @Autowired
    private IProfileService profileService;

    @Autowired
    private IGameDefService gameDefService;

    @Autowired
    private IGameService gameService;

    @Autowired
    private DataCache dataCache;

    @Override
    public PointBreakdownResDto getPointBreakdownList(PointBreakdownReqDto request) throws Exception {
        Map<String, Object> conditions = Maps.create()
                .put("hasOffset", ServiceUtils.isValid(request.getOffset()))
                .put("hasSize", ServiceUtils.isValid(request.getSize()))
                .put("hasRangeStart", ServiceUtils.isValid(request.getRangeStart()))
                .put("hasRangeEnd", ServiceUtils.isValid(request.getRangeEnd()))
                .put("hasPointId", ServiceUtils.isValid(request.getPointId()))
                .put("hasUserId", ServiceUtils.isValid(request.getUserId()))
                .put("hasTeamId", ServiceUtils.isValid(request.getTeamId()))
                .put("hasTeamScopeId", ServiceUtils.isValid(request.getTeamScopeId()))
                .build();

        List<PointRecordDto> recordDtos = ServiceUtils.toList(dao.executeQuery(
                Q.STATS.GET_POINT_BREAKDOWN,
                Maps.create()
                    .put("userId", request.getUserId())
                    .put("teamId", request.getTeamId())
                    .put("teamScopeId", request.getTeamScopeId())
                    .put("pointId", request.getPointId())
                    .put("offset", ServiceUtils.orDefault(request.getOffset(), 0))
                    .put("size", ServiceUtils.orDefault(request.getSize(), 100))
                    .put("rangeStart", ServiceUtils.orDefault(request.getRangeStart(), 0L))
                    .put("rangeEnd", ServiceUtils.orDefault(request.getRangeEnd(), 0L))
                    .build(),
                PointRecordDto.class,
                conditions));

        PointBreakdownResDto resDto = new PointBreakdownResDto();
        resDto.setRecords(recordDtos);
        resDto.setCount(recordDtos.size());
        return resDto;
    }

    @Override
    public PointSummaryRes getPointSummary(PointSummaryReq request) throws Exception {
        Map<String, Object> conditions = Maps.create()
                .put("hasOffset", ServiceUtils.isValid(request.getOffset()))
                .put("hasSize", ServiceUtils.isValid(request.getSize()))
                .put("hasRangeStart", ServiceUtils.isValid(request.getRangeStart()))
                .put("hasRangeEnd", ServiceUtils.isValid(request.getRangeEnd()))
                .put("hasUserId", ServiceUtils.isValid(request.getUserId()))
                .put("hasTeamId", ServiceUtils.isValid(request.getTeamId()))
                .put("hasTeamScopeId", ServiceUtils.isValid(request.getTeamScopeId()))
                .build();

        List<PointSummaryRes.PointSummaryRecord> recordDtos = ServiceUtils.toList(dao.executeQuery(
                Q.STATS.GET_POINT_SUMMARY,
                Maps.create()
                        .put("userId", request.getUserId())
                        .put("teamId", request.getTeamId())
                        .put("teamScopeId", request.getTeamScopeId())
                        .put("offset", ServiceUtils.orDefault(request.getOffset(), 0))
                        .put("size", ServiceUtils.orDefault(request.getSize(), 100))
                        .put("rangeStart", ServiceUtils.orDefault(request.getRangeStart(), 0L))
                        .put("rangeEnd", ServiceUtils.orDefault(request.getRangeEnd(), 0L))
                        .build(),
                PointSummaryRes.PointSummaryRecord.class,
                conditions));

        PointSummaryRes resDto = new PointSummaryRes();
        resDto.setRecords(recordDtos);
        resDto.setCount(recordDtos.size());
        return resDto;
    }

    @Override
    public BadgeBreakdownResDto getBadgeBreakdownList(BadgeBreakdownReqDto request) throws Exception {
        Map<String, Object> conditions = Maps.create()
                .put("hasBadgeId", ServiceUtils.isValid(request.getBadgeId()))
                .put("hasOffset", ServiceUtils.isValid(request.getOffset()))
                .put("hasSize", ServiceUtils.isValid(request.getSize()))
                .put("hasRangeStart", ServiceUtils.isValid(request.getRangeStart()))
                .put("hasRangeEnd", ServiceUtils.isValid(request.getRangeEnd()))
                .put("hasUserId", ServiceUtils.isValid(request.getUserId()))
                .put("hasTeamId", ServiceUtils.isValid(request.getTeamId()))
                .put("hasTeamScopeId", ServiceUtils.isValid(request.getTeamScopeId()))
                .build();

        List<BadgeRecordDto> recordDtos = ServiceUtils.toList(dao.executeQuery(
                Q.STATS.GET_BADGE_BREAKDOWN,
                Maps.create()
                        .put("userId", request.getUserId())
                        .put("teamId", request.getTeamId())
                        .put("teamScopeId", request.getTeamScopeId())
                        .put("badgeId", ServiceUtils.orDefault(request.getBadgeId(), 0))
                        .put("offset", ServiceUtils.orDefault(request.getOffset(), 0))
                        .put("size", ServiceUtils.orDefault(request.getSize(), 100))
                        .put("rangeStart", ServiceUtils.orDefault(request.getRangeStart(), 0L))
                        .put("rangeEnd", ServiceUtils.orDefault(request.getRangeEnd(), 0L))
                        .build(),
                BadgeRecordDto.class,
                conditions));

        BadgeBreakdownResDto resDto = new BadgeBreakdownResDto();
        resDto.setRecords(recordDtos);
        resDto.setCount(recordDtos.size());
        return resDto;
    }

    @Override
    public BadgeSummaryRes getBadgeSummary(BadgeSummaryReq request) throws Exception {
        Map<String, Object> conditions = Maps.create()
                .put("hasOffset", ServiceUtils.isValid(request.getOffset()))
                .put("hasSize", ServiceUtils.isValid(request.getSize()))
                .put("hasRangeStart", ServiceUtils.isValid(request.getRangeStart()))
                .put("hasRangeEnd", ServiceUtils.isValid(request.getRangeEnd()))
                .put("hasUserId", ServiceUtils.isValid(request.getUserId()))
                .put("hasTeamId", ServiceUtils.isValid(request.getTeamId()))
                .put("hasTeamScopeId", ServiceUtils.isValid(request.getTeamScopeId()))
                .build();

        List<BadgeSummaryRes.BadgeSummaryRecord> recordDtos = ServiceUtils.toList(dao.executeQuery(
                Q.STATS.GET_BADGE_SUMMARY,
                Maps.create()
                        .put("userId", request.getUserId())
                        .put("teamId", request.getTeamId())
                        .put("teamScopeId", request.getTeamScopeId())
                        .put("offset", ServiceUtils.orDefault(request.getOffset(), 0))
                        .put("size", ServiceUtils.orDefault(request.getSize(), 100))
                        .put("rangeStart", ServiceUtils.orDefault(request.getRangeStart(), 0L))
                        .put("rangeEnd", ServiceUtils.orDefault(request.getRangeEnd(), 0L))
                        .build(),
                BadgeSummaryRes.BadgeSummaryRecord.class,
                conditions));

        BadgeSummaryRes resDto = new BadgeSummaryRes();
        resDto.setRecords(recordDtos);
        resDto.setCount(recordDtos.size());
        return resDto;
    }

    @Override
    public UserStatDto readUserGameStats(long userId, long since) throws Exception {
        Checks.greaterThanZero(userId, "userId");

        Map<String, Object> tdata = new HashMap<>();
        tdata.put("hasSince", since > 0);

        Iterable<UserStatDto.StatRecord> summaryStat = dao.executeQuery(
                Q.STATS.GET_USER_STAT_SUMMARY,
                Maps.create()
                        .put("userId", userId)
                        .put("since", since)
                        .build(),
                UserStatDto.StatRecord.class,
                tdata);

        UserStatDto dto = new UserStatDto();
        dto.setUserId((int)userId);
        String uName = null;
        String email = null;
        for (UserStatDto.StatRecord row : summaryStat) {
            Statement stmt = new Statement(dto,
                    "set" + row.getType(),
                    new Object[] { firstNonNull(row.getValue_i(), row.getValue_f()) });
            stmt.execute();
            uName = (String) firstNonNull(uName, row.getUserName());
            email = (String) firstNonNull(email, row.getUserEmail());
        }
        dto.setUserName(uName);
        dto.setUserEmail(email);
        return dto;
    }

    @Override
    public List<PurchasedItem> readUserPurchasedItems(long userId, long since) throws Exception {
        Checks.greaterThanZero(userId, "userId");

        Map<String, Object> tdata = new HashMap<>();
        tdata.put("hasSince", since > 0);

        return ServiceUtils.toList(dao.executeQuery(Q.STATS.GET_PURCHASED_ITEMS,
                Maps.create()
                    .put("userId", userId).put("since", since)
                    .build(),
                PurchasedItem.class,
                tdata));
    }

    @Override
    public List<UserMilestoneStatDto> readUserMilestones(long userId) throws Exception {
        Checks.greaterThanZero(userId, "userId");

        return ServiceUtils.toList(dao.executeQuery(
                Q.STATS.GET_USER_MILESTONE_STAT,
                Maps.create("userId", userId),
                UserMilestoneStatDto.class));
    }

    @Override
    public UserRankingsInRangeDto readUserTeamRankings(long userId) throws Exception {
        Checks.greaterThanZero(userId, "userId");

        LeaderboardDef defaultLeaderboard = dataCache.getDefaultLeaderboard();
        UserTeam currentTeamOfUser = profileService.findCurrentTeamOfUser(userId);

        UserRankingsInRangeDto rankings = new UserRankingsInRangeDto();
        rankings.setUserId(userId);

        for (LeaderboardType type : RANGES) {
            LeaderboardRequestDto requestDto = new LeaderboardRequestDto(type, System.currentTimeMillis());
            requestDto.setForUser(userId);
            requestDto.setLeaderboardDef(defaultLeaderboard);

            List<TeamLeaderboardRecordDto> order = gameService.readTeamLeaderboard(currentTeamOfUser.getTeamId(),
                    requestDto);
            RankingRecord rank = createRank(order, ScopingType.TEAM);
            UserRankRecordDto result = new UserRankRecordDto();
            result.setLeaderboard(defaultLeaderboard);
            result.setRank(rank);

            rankings.setWithRange(type, result);
        }
        return rankings;
    }

    @Override
    public List<UserRankRecordDto> readMyLeaderboardRankings(long gameId, long userId, ScopingType scopingType,
                                                             LeaderboardType rangeType) throws Exception {
        Checks.greaterThanZero(gameId, "gameId");
        Checks.greaterThanZero(userId, "userId");
        Checks.nonNull(scopingType, "scopeType");

        UserTeam currentTeamOfUser = profileService.findCurrentTeamOfUser(userId);
        List<LeaderboardDef> leaderboardDefs = gameDefService.listLeaderboardDefs(gameId);
        List<UserRankRecordDto> rankings = new LinkedList<>();

        for (LeaderboardDef def : leaderboardDefs) {
            LeaderboardRequestDto requestDto = rangeType == LeaderboardType.CUSTOM ?
                    new LeaderboardRequestDto(1, System.currentTimeMillis())
                    : new LeaderboardRequestDto(rangeType, System.currentTimeMillis());
            requestDto.setForUser(userId);
            requestDto.setLeaderboardDef(def);

            UserRankRecordDto record = new UserRankRecordDto();
            record.setLeaderboard(def);
            RankingRecord rank;
            if (scopingType == ScopingType.TEAM) {
                rank = createRank(gameService.readTeamLeaderboard(currentTeamOfUser.getTeamId(), requestDto),
                            scopingType);
            } else if (scopingType == ScopingType.TEAM_SCOPE) {
                rank = createRank(gameService.readTeamScopeLeaderboard(currentTeamOfUser.getScopeId(), requestDto),
                        scopingType);
            } else if (scopingType == ScopingType.GLOBAL) {
                rank = createRank(gameService.readGlobalLeaderboard(requestDto));
            } else {
                throw new InputValidationException("Unknown leaderboard scoping type. It must be either GLOBAL, TEAMSCOPE, or TEAM!");
            }

            record.setRank(rank);
            rankings.add(record);
        }
        return rankings;
    }

    @Override
    public List<TeamHistoryRecordDto> readUserTeamHistoryStat(long userId) throws Exception {
        Checks.greaterThanZero(userId, "userId");

        List<TeamHistoryRecordDto> historyRecords = ServiceUtils.toList(dao.executeQuery(
                Q.STATS.TEAM_WISE_SUMMARY_STATS,
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
    public List<UserStateStatDto> readUserStateStats(long userId, long teamId) throws Exception {
        Checks.greaterThanZero(userId, "userId");

        return ServiceUtils.toList(dao.executeQuery(Q.STATS.GET_USER_STATE_VALUES,
                Maps.create()
                    .put("userId", userId)
                    .put("teamId", teamId).build(),
                UserStateStatDto.class));
    }

    @Override
    public ChallengeInfoDto readChallengeStats(long challengeId) throws Exception {
        Checks.greaterThanZero(challengeId, "challengeId");

        ChallengeDef def = gameDefService.readChallenge(challengeId);
        if (def != null) {
            ChallengeInfoDto challengeInfoDto = new ChallengeInfoDto();
            List<ChallengeWinnerDto> winners = ServiceUtils.toList(dao.executeQuery(
                    Q.STATS.GET_CHALLENGE_WINNERS,
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

    private RankingRecord createRank(List<GlobalLeaderboardRecordDto> recordsDto) {
        if (Commons.isNullOrEmpty(recordsDto)) {
            return null;
        } else {
            GlobalLeaderboardRecordDto first = recordsDto.get(0);
            RankingRecord rankingRecord = new RankingRecord();
            rankingRecord.setRank(first.getRankGlobal());
            rankingRecord.setNextValue(first.getNextRankValue());
            rankingRecord.setTopValue(first.getTopRankValue());
            return rankingRecord;
        }
    }

    private RankingRecord createRank(List<TeamLeaderboardRecordDto> recordsDto,
                                                       ScopingType scopingType) {
        if (Commons.isNullOrEmpty(recordsDto)) {
            return null;
        } else {
            TeamLeaderboardRecordDto first = recordsDto.get(0);
            RankingRecord rankingRecord = new RankingRecord();
            if (scopingType == ScopingType.TEAM) {
                rankingRecord.setRank(first.getRankInTeam());
                rankingRecord.setNextValue(first.getNextTeamRankValue());
            } else {
                rankingRecord.setRank(first.getRankInTeamScope());
                rankingRecord.setNextValue(first.getNextTeamScopeRankValue());
            }
            return rankingRecord;
        }
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
