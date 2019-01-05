package io.github.isuru.oasis.services.services;

import io.github.isuru.oasis.model.db.IOasisDao;
import io.github.isuru.oasis.model.defs.ChallengeDef;
import io.github.isuru.oasis.model.defs.LeaderboardDef;
import io.github.isuru.oasis.model.defs.LeaderboardType;
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
import io.github.isuru.oasis.services.api.dto.UserStateStatDto;
import io.github.isuru.oasis.services.exception.InputValidationException;
import io.github.isuru.oasis.services.model.LeaderboardRequestDto;
import io.github.isuru.oasis.services.model.PurchasedItem;
import io.github.isuru.oasis.services.model.UserRankRecordDto;
import io.github.isuru.oasis.services.model.UserTeam;
import io.github.isuru.oasis.services.model.enums.ScopingType;
import io.github.isuru.oasis.services.utils.Checks;
import io.github.isuru.oasis.services.utils.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
@Service("statService")
public class StatServiceImpl implements IStatService {

    @Autowired
    private IOasisDao dao;

    @Autowired
    private IProfileService profileService;

    @Autowired
    private IGameDefService gameDefService;

    @Autowired
    private IGameService gameService;

    @Override
    public PointBreakdownResDto getPointBreakdownList(PointBreakdownReqDto request) throws Exception {
        Checks.greaterThanZero(request.getUserId(), "userId");
        Checks.greaterThanZero(request.getPointId(), "pointId");

        Map<String, Object> conditions = Maps.create()
                .put("hasOffset", ServiceUtils.isValid(request.getOffset()))
                .put("hasSize", ServiceUtils.isValid(request.getSize()))
                .put("hasRangeStart", ServiceUtils.isValid(request.getRangeStart()))
                .put("hasRangeEnd", ServiceUtils.isValid(request.getRangeEnd()))
                .build();

        List<PointRecordDto> recordDtos = ServiceUtils.toList(dao.executeQuery(Q.STATS.GET_USER_POINTS_LIST,
                Maps.create().put("userId", request.getUserId())
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
    public BadgeBreakdownResDto getBadgeBreakdownList(BadgeBreakdownReqDto request) throws Exception {
        Checks.greaterThanZero(request.getUserId(), "userId");

        Map<String, Object> conditions = Maps.create()
                .put("hasBadgeId", ServiceUtils.isValid(request.getBadgeId()))
                .put("hasOffset", ServiceUtils.isValid(request.getOffset()))
                .put("hasSize", ServiceUtils.isValid(request.getSize()))
                .put("hasRangeStart", ServiceUtils.isValid(request.getRangeStart()))
                .put("hasRangeEnd", ServiceUtils.isValid(request.getRangeEnd()))
                .build();

        List<BadgeRecordDto> recordDtos = ServiceUtils.toList(dao.executeQuery(Q.STATS.GET_USER_BADGES_LIST,
                Maps.create().put("userId", request.getUserId())
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
    public UserStatDto readUserGameStats(long userId, long since) throws Exception {
        Checks.greaterThanZero(userId, "userId");

        Map<String, Object> tdata = new HashMap<>();
        tdata.put("hasSince", since > 0);

        Iterable<Map<String, Object>> summaryStat = dao.executeQuery(
                Q.STATS.GET_USER_STAT_SUMMARY,
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

        return ServiceUtils.toList(dao.executeQuery(Q.STATS.GET_PURCHASED_ITEMS,
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

        return ServiceUtils.toList(dao.executeQuery(
                Q.STATS.GET_USER_BADGE_STAT,
                Maps.create().put("userId", userId).put("since", since).build(),
                UserBadgeStatDto.class,
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
    public List<UserRankRecordDto> readUserTeamRankings(long userId, boolean currentTeamOnly) throws Exception {
        Checks.greaterThanZero(userId, "userId");

        UserTeam currentTeamOfUser = profileService.findCurrentTeamOfUser(userId);

        Map<String, Object> tdata = new HashMap<>();
        tdata.put("teamWise", currentTeamOfUser != null && currentTeamOnly);
        tdata.put("hasTeamScope", currentTeamOfUser != null);

        int tid = currentTeamOfUser == null ? 0 : currentTeamOfUser.getTeamId();
        int sid = currentTeamOfUser == null ? 0 : currentTeamOfUser.getScopeId();
        return ServiceUtils.toList(dao.executeQuery(Q.STATS.GET_USER_TEAM_RANKING,
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

            List<UserRankRecordDto> userRankRecordDtos = null;
            if (scopingType == ScopingType.TEAM) {
                userRankRecordDtos = gameService
                        .readTeamLeaderboard(currentTeamOfUser.getTeamId(), requestDto);
            } else if (scopingType == ScopingType.TEAM_SCOPE) {
                userRankRecordDtos = gameService
                        .readTeamScopeLeaderboard(currentTeamOfUser.getScopeId(), requestDto);
            } else if (scopingType == ScopingType.GLOBAL) {
                userRankRecordDtos = gameService.readGlobalLeaderboard(requestDto);
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
