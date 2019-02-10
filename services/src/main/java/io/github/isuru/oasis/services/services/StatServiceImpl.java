package io.github.isuru.oasis.services.services;

import io.github.isuru.oasis.model.db.IOasisDao;
import io.github.isuru.oasis.model.defs.ChallengeDef;
import io.github.isuru.oasis.model.defs.LeaderboardDef;
import io.github.isuru.oasis.model.defs.LeaderboardType;
import io.github.isuru.oasis.model.defs.ScopingType;
import io.github.isuru.oasis.services.DataCache;
import io.github.isuru.oasis.services.dto.game.FeedItem;
import io.github.isuru.oasis.services.dto.game.FeedItemReq;
import io.github.isuru.oasis.services.dto.game.GlobalLeaderboardRecordDto;
import io.github.isuru.oasis.services.dto.game.LeaderboardRequestDto;
import io.github.isuru.oasis.services.dto.game.RankingRecord;
import io.github.isuru.oasis.services.dto.game.TeamLeaderboardRecordDto;
import io.github.isuru.oasis.services.dto.game.UserLeaderboardRankingsDto;
import io.github.isuru.oasis.services.dto.game.UserRankRecordDto;
import io.github.isuru.oasis.services.dto.game.UserRankingsInRangeDto;
import io.github.isuru.oasis.services.dto.stats.BadgeBreakdownReqDto;
import io.github.isuru.oasis.services.dto.stats.BadgeBreakdownResDto;
import io.github.isuru.oasis.services.dto.stats.BadgeRecordDto;
import io.github.isuru.oasis.services.dto.stats.BadgeSummaryReq;
import io.github.isuru.oasis.services.dto.stats.BadgeSummaryRes;
import io.github.isuru.oasis.services.dto.stats.ChallengeInfoDto;
import io.github.isuru.oasis.services.dto.stats.ChallengeWinDto;
import io.github.isuru.oasis.services.dto.stats.ChallengeWinnerDto;
import io.github.isuru.oasis.services.dto.stats.MyLeaderboardReq;
import io.github.isuru.oasis.services.dto.stats.PointBreakdownReqDto;
import io.github.isuru.oasis.services.dto.stats.PointBreakdownResDto;
import io.github.isuru.oasis.services.dto.stats.PointRecordDto;
import io.github.isuru.oasis.services.dto.stats.PointSummaryReq;
import io.github.isuru.oasis.services.dto.stats.PointSummaryRes;
import io.github.isuru.oasis.services.dto.stats.TeamHistoryRecordDto;
import io.github.isuru.oasis.services.dto.stats.UserChallengeWinRes;
import io.github.isuru.oasis.services.dto.stats.UserMilestoneStatDto;
import io.github.isuru.oasis.services.dto.stats.UserScopeRankingsStat;
import io.github.isuru.oasis.services.dto.stats.UserStatDto;
import io.github.isuru.oasis.services.dto.stats.UserStateStatDto;
import io.github.isuru.oasis.services.exception.InputValidationException;
import io.github.isuru.oasis.services.model.UserTeam;
import io.github.isuru.oasis.services.utils.Checks;
import io.github.isuru.oasis.services.utils.Commons;
import io.github.isuru.oasis.services.utils.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.beans.Statement;
import java.util.ArrayList;
import java.util.Arrays;
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
    public List<UserMilestoneStatDto> readUserMilestones(long userId) throws Exception {
        Checks.greaterThanZero(userId, "userId");

        return ServiceUtils.toList(dao.executeQuery(
                Q.STATS.GET_USER_MILESTONE_STAT,
                Maps.create("userId", userId),
                UserMilestoneStatDto.class));
    }

    @Override
    public UserRankingsInRangeDto readUserTeamRankings(long gameId, long userId,
                                                       long leaderboardId, long timestamp) throws Exception {
        Checks.greaterThanZero(userId, "userId");
        Checks.greaterThanZero(gameId, "gameId");

        // consider game scoped leaderboard
        long ts = timestamp > 0 ? timestamp : System.currentTimeMillis();
        LeaderboardDef lbDef = dataCache.getDefaultLeaderboard();
        if (leaderboardId > 0) {
            lbDef = gameDefService.readLeaderboardDef(leaderboardId);
            if (lbDef == null) {
                throw new InputValidationException("No leaderboard is found by id " + leaderboardId + "!");
            }
        }
        UserTeam currentTeamOfUser = profileService.findCurrentTeamOfUser(userId);

        UserRankingsInRangeDto rankings = new UserRankingsInRangeDto();
        rankings.setUserId(userId);

        for (LeaderboardType type : RANGES) {
            LeaderboardRequestDto requestDto = new LeaderboardRequestDto(type, ts);
            requestDto.setForUser(userId);
            requestDto.setLeaderboardDef(lbDef);

            List<TeamLeaderboardRecordDto> order = gameService.readTeamLeaderboard(currentTeamOfUser.getTeamId(),
                    requestDto);
            RankingRecord rank = createRank(order, ScopingType.TEAM);
            UserRankRecordDto result = new UserRankRecordDto();
            result.setLeaderboard(lbDef);
            result.setRank(rank);

            rankings.setWithRange(type, result);
        }
        return rankings;
    }

    @Override
    public List<UserLeaderboardRankingsDto> readMyLeaderboardRankings(long gameId, long userId,
                                                                      MyLeaderboardReq req) throws Exception {
        Checks.greaterThanZero(gameId, "gameId");
        Checks.greaterThanZero(userId, "userId");
        Checks.nonNull(req, "request");
        Checks.nonNull(req.getScopingType(), "scopeType");

        LeaderboardType rangeType = req.getRangeType();
        Checks.nonNull(rangeType, "rangeType");

        if (rangeType == LeaderboardType.CUSTOM
            && (!ServiceUtils.isValid(req.getRangeStart()) || !ServiceUtils.isValid(req.getRangeEnd()))) {
            throw new InputValidationException("Range start and end must specify for custom leaderboards!");
        }

        UserTeam currentTeamOfUser = profileService.findCurrentTeamOfUser(userId);
        List<LeaderboardDef> leaderboardDefs = gameDefService.listLeaderboardDefs(gameId);
        List<UserLeaderboardRankingsDto> rankings = new LinkedList<>();

        for (LeaderboardDef def : leaderboardDefs) {
            LeaderboardRequestDto requestDto = rangeType == LeaderboardType.CUSTOM ?
                    new LeaderboardRequestDto(1, System.currentTimeMillis())
                    : new LeaderboardRequestDto(rangeType, System.currentTimeMillis());
            requestDto.setForUser(userId);
            requestDto.setLeaderboardDef(def);

            if (req.getScopingType() == ScopingType.GLOBAL) {
                UserLeaderboardRankingsDto record = new UserLeaderboardRankingsDto();
                record.setLeaderboardDef(def);
                record.setUserId(userId);

                RankingRecord rank = createRank(gameService.readGlobalLeaderboard(requestDto));
                record.setGlobal(rank);
                rankings.add(record);
            } else {
                List<TeamLeaderboardRecordDto> result = gameService.readTeamLeaderboard(currentTeamOfUser.getTeamId(), requestDto);
                RankingRecord tRank = createRank(result, ScopingType.TEAM);
                RankingRecord tsRank = createRank(result, ScopingType.TEAM_SCOPE);

                UserLeaderboardRankingsDto record = new UserLeaderboardRankingsDto();
                record.setLeaderboardDef(def);
                record.setUserId(userId);
                record.setTeam(tRank);
                record.setTeamScope(tsRank);
                rankings.add(record);
            }

        }
        return rankings;
    }

    @Override
    public UserScopeRankingsStat readMyRankings(long gameId, long userId, LeaderboardType rangeType) throws Exception {
        Checks.greaterThanZero(gameId, "gameId");
        Checks.greaterThanZero(userId, "userId");
        Checks.nonNull(rangeType, "rangeType");

        UserTeam currentTeamOfUser = profileService.findCurrentTeamOfUser(userId);
        LeaderboardDef defaultLeaderboard = dataCache.getDefaultLeaderboard();

        LeaderboardRequestDto requestDto = rangeType == LeaderboardType.CUSTOM ?
                new LeaderboardRequestDto(1, System.currentTimeMillis())
                : new LeaderboardRequestDto(rangeType, System.currentTimeMillis());
        requestDto.setForUser(userId);
        requestDto.setLeaderboardDef(defaultLeaderboard);

        UserScopeRankingsStat stat = new UserScopeRankingsStat();
        stat.setUserId(userId);
        RankingRecord globalRank = createRank(gameService.readGlobalLeaderboard(requestDto));
        List<TeamLeaderboardRecordDto> teamRanks = gameService.readTeamLeaderboard(
                currentTeamOfUser.getTeamId(),
                requestDto);
        stat.setTeam(createRank(teamRanks, ScopingType.TEAM));
        stat.setTeamScope(createRank(teamRanks, ScopingType.TEAM_SCOPE));
        stat.setGlobal(globalRank);
        return stat;
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
    public List<UserStateStatDto> readUserStateStats(long userId) throws Exception {
        Checks.greaterThanZero(userId, "userId");

        return ServiceUtils.toList(dao.executeQuery(Q.STATS.GET_USER_STATE_VALUES,
                Maps.create()
                    .put("userId", userId).build(),
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
    public UserChallengeWinRes readUserChallengeWins(long userId) throws Exception {
        Checks.greaterThanZero(userId, "userId");

        UserChallengeWinRes challengeInfoDto = new UserChallengeWinRes();
        List<ChallengeWinDto> winners = ServiceUtils.toList(dao.executeQuery(
                Q.STATS.GET_USER_CHALLENGES,
                Maps.create("userId", userId),
                ChallengeWinDto.class));

        challengeInfoDto.setWins(winners);
        return challengeInfoDto;
    }

    @Override
    public List<FeedItem> readUserGameTimeline(FeedItemReq req) throws Exception {
        Checks.nonNull(req, "request");

        Map<String, Object> templating = Maps.create()
                .put("hasUser", ServiceUtils.isValid(req.getUserId()))
                .put("hasTeam", ServiceUtils.isValid(req.getTeamId()))
                .put("hasTeamScope", ServiceUtils.isValid(req.getTeamScopeId()))
                .put("hasStartRange", ServiceUtils.isValid(req.getRangeStart()))
                .put("hasEndRange", ServiceUtils.isValid(req.getRangeEnd()))
                .build();

        Map<String, Object> data = Maps.create()
                .put("userId", req.getUserId())
                .put("teamId", req.getTeamId())
                .put("teamScopeId", req.getTeamScopeId())
                .put("rangeStart", req.getRangeStart())
                .put("rangeEnd", req.getRangeEnd())
                .put("size", Commons.orDefault(req.getSize(), 50))
                .put("offset", Commons.orDefault(req.getOffset(), 0))
                .build();

        return ServiceUtils.toList(dao.executeQuery(Q.STATS.READ_FEEDS,
                data,
                FeedItem.class,
                templating));
    }

    private RankingRecord createRank(List<GlobalLeaderboardRecordDto> recordsDto) {
        if (Commons.isNullOrEmpty(recordsDto)) {
            return null;
        } else {
            GlobalLeaderboardRecordDto first = recordsDto.get(0);
            RankingRecord rankingRecord = new RankingRecord();
            rankingRecord.setRank(first.getRankGlobal());
            rankingRecord.setMyValue(first.getTotalPoints());
            rankingRecord.setMyCount(first.getTotalCount());
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
            rankingRecord.setMyValue(first.getTotalPoints());
            rankingRecord.setMyCount(first.getTotalCount());
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
        if (curr.getTotalRaceWins() != null && base.getTotalRaceWins() == null) {
            base.setTotalRaceWins(curr.getTotalRaceWins());
        }
        if (curr.getTotalPoints() != null && base.getTotalPoints() == null) {
            base.setTotalPoints(curr.getTotalPoints());
        }
        if (curr.getTotalCount() != null && base.getTotalCount() == null) {
            base.setTotalCount(curr.getTotalCount());
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
