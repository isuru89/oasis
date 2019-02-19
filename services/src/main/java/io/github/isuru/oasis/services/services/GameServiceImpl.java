package io.github.isuru.oasis.services.services;

import io.github.isuru.oasis.model.AggregatorType;
import io.github.isuru.oasis.model.Constants;
import io.github.isuru.oasis.model.db.IOasisDao;
import io.github.isuru.oasis.model.defs.GameDef;
import io.github.isuru.oasis.model.defs.LeaderboardDef;
import io.github.isuru.oasis.model.defs.RaceDef;
import io.github.isuru.oasis.model.events.EventNames;
import io.github.isuru.oasis.model.events.RaceEvent;
import io.github.isuru.oasis.services.DataCache;
import io.github.isuru.oasis.services.dto.game.BadgeAwardDto;
import io.github.isuru.oasis.services.dto.game.GlobalLeaderboardRecordDto;
import io.github.isuru.oasis.services.dto.game.LeaderboardRequestDto;
import io.github.isuru.oasis.services.dto.game.PointAwardDto;
import io.github.isuru.oasis.services.dto.game.RaceCalculationDto;
import io.github.isuru.oasis.services.dto.game.TeamLeaderboardRecordDto;
import io.github.isuru.oasis.services.exception.ApiAuthException;
import io.github.isuru.oasis.services.exception.InputValidationException;
import io.github.isuru.oasis.services.model.RaceWinRecord;
import io.github.isuru.oasis.services.model.TeamProfile;
import io.github.isuru.oasis.services.model.UserRole;
import io.github.isuru.oasis.services.model.UserTeam;
import io.github.isuru.oasis.services.services.scheduler.CustomScheduler;
import io.github.isuru.oasis.services.utils.Checks;
import io.github.isuru.oasis.services.utils.Commons;
import io.github.isuru.oasis.services.utils.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author iweerarathna
 */
@Service("gameService")
public class GameServiceImpl implements IGameService {

    private static final Logger LOG = LoggerFactory.getLogger(GameServiceImpl.class);

    private IGameDefService gameDefService;
    private IProfileService profileService;
    private IEventsService eventsService;

    private IOasisDao dao;
    private DataCache dataCache;

    @Autowired
    public GameServiceImpl(IGameDefService gameDefService, IProfileService profileService, IEventsService eventsService, IOasisDao dao, DataCache dataCache) {
        this.gameDefService = gameDefService;
        this.profileService = profileService;
        this.eventsService = eventsService;
        this.dao = dao;
        this.dataCache = dataCache;
    }

    @Override
    public void awardPoints(long byUser, PointAwardDto awardDto) throws Exception {
        Checks.greaterThanZero(byUser, "user");
        long toUser = awardDto.getToUser();
        Checks.greaterThanZero(toUser, "toUser");
        Checks.validate(awardDto.getAmount() != 0.0f, "Point amount should not be equal to zero!");
        Checks.validate(toUser != byUser, "You cannot award points to yourself!");

        UserTeam currentTeamOfUser = profileService.findCurrentTeamOfUser(toUser);
        Checks.nonNull(currentTeamOfUser, "Cannot derive the current team of user #" + toUser + "!");

        UserTeam curTeamOfAwardBy = profileService.findCurrentTeamOfUser(byUser);
        Checks.nonNull(curTeamOfAwardBy, "Cannot derive the current team of awarding user #" + toUser + "!");

        long teamId = currentTeamOfUser.getTeamId();
        long scopeId = currentTeamOfUser.getScopeId();
        long gameId = awardDto.getGameId() != null ? awardDto.getGameId() : dataCache.getDefGameId();

        // awarder and awardee must be in same team scope now
        if (dataCache.getAdminUserId() == byUser
                || (UserRole.hasRole(curTeamOfAwardBy.getRoleId(), UserRole.CURATOR)
                && curTeamOfAwardBy.getScopeId().equals(currentTeamOfUser.getScopeId()))) {

            Map<String, Object> data = Maps.create()
                    .put(Constants.FIELD_EVENT_TYPE, EventNames.OASIS_EVENT_COMPENSATE_POINTS)
                    .put(Constants.FIELD_TIMESTAMP, awardDto.getTs() == null ? System.currentTimeMillis() : awardDto.getTs())
                    .put(Constants.FIELD_USER, toUser)
                    .put(Constants.FIELD_TEAM, teamId)
                    .put(Constants.FIELD_SCOPE, scopeId)
                    .put(Constants.FIELD_GAME_ID, gameId)
                    .put(Constants.FIELD_ID, awardDto.getAssociatedEventId())
                    .put("amount", awardDto.getAmount())
                    .put("tag", String.valueOf(byUser))
                    .build();

            String token = getInternalToken();
            eventsService.submitEvent(token, data);

        } else {
            throw new ApiAuthException("You cannot award points to user " + toUser
                    + ", because you do not have required permissions or user is not belong to same team scope as you!");
        }
    }

    @Override
    public void awardBadge(long byUser, BadgeAwardDto awardDto) throws Exception {
        Checks.greaterThanZero(byUser, "user");
        long toUser = awardDto.getToUser();
        Checks.greaterThanZero(toUser, "toUser");
        Checks.greaterThanZero(awardDto.getBadgeId(), "Badge id must be a valid one!");
        Checks.validate(byUser != toUser, "You cannot award badges to yourself!");

        UserTeam currentTeamOfUser = profileService.findCurrentTeamOfUser(toUser);
        Checks.nonNull(currentTeamOfUser, "Cannot derive the current team of user #" + toUser + "!");

        UserTeam curTeamOfAwardBy = profileService.findCurrentTeamOfUser(byUser);
        Checks.nonNull(curTeamOfAwardBy, "Cannot derive the current team of awarding user #" + toUser + "!");

        long teamId = currentTeamOfUser.getTeamId();
        long scopeId = currentTeamOfUser.getScopeId();
        long gameId = awardDto.getGameId() != null ? awardDto.getGameId() : dataCache.getDefGameId();

        // awarder and awardee must be in same team scope now
        if (dataCache.getAdminUserId() == byUser
                || (UserRole.hasRole(curTeamOfAwardBy.getRoleId(), UserRole.CURATOR)
                    && curTeamOfAwardBy.getScopeId().equals(currentTeamOfUser.getScopeId()))) {

            Map<String, Object> data = Maps.create()
                    .put(Constants.FIELD_EVENT_TYPE, EventNames.OASIS_EVENT_AWARD_BADGE)
                    .put(Constants.FIELD_TIMESTAMP, awardDto.getTs() == null ? System.currentTimeMillis() : awardDto.getTs())
                    .put(Constants.FIELD_USER, toUser)
                    .put(Constants.FIELD_TEAM, teamId)
                    .put(Constants.FIELD_SCOPE, scopeId)
                    .put(Constants.FIELD_GAME_ID, gameId)
                    .put(Constants.FIELD_ID, awardDto.getAssociatedEventId())
                    .put("badge", awardDto.getBadgeId())
                    .put("subBadge", awardDto.getSubBadgeId())
                    .put("tag", String.valueOf(byUser))
                    .build();

            String token = getInternalToken();
            eventsService.submitEvent(token, data);

        } else {
            throw new ApiAuthException("You cannot award badges to user #" + toUser
                + ", because you do not have required permissions or user is not belong to same team scope as you!");
        }
    }

    @Override
    public List<RaceWinRecord> calculateRaceWinners(long gameId, long raceId,
                                                    RaceCalculationDto calculationDto) throws Exception {
        GameDef gameDef = gameDefService.readGame(gameId);
        Optional<RaceDef> raceOpt = gameDefService.listRaces(gameId).stream()
                .filter(r -> r.getId() == raceId)
                .findFirst();

        if (!raceOpt.isPresent()) {
            throw new InputValidationException("No race is found by id #" + raceId + "!");
        }

        CustomScheduler customScheduler = new CustomScheduler(calculationDto,
                gameDefService,
                profileService,
                this);

        long ts = System.currentTimeMillis();
        return customScheduler.runCustomInvoke(raceOpt.get(), gameDef.getId(), ts);

    }

    @Override
    public void addRaceWinners(long gameId, long raceId, List<RaceWinRecord> winners) throws Exception {
        // prepare for events...
        String token = getInternalToken();
        long ts = System.currentTimeMillis();

        List<Map<String, Object>> events = new ArrayList<>();
        winners.forEach(winner -> {
            Map<String, Object> event = new HashMap<>();
            // event.put(Constants.FIELD_ID, UUID.randomUUID().toString());  // no id is needed for now
            event.put(Constants.FIELD_GAME_ID, gameId);
            event.put(Constants.FIELD_EVENT_TYPE, EventNames.OASIS_EVENT_RACE_AWARD);
            event.put(Constants.FIELD_USER, winner.getUserId());
            event.put(Constants.FIELD_TIMESTAMP, ts);
            event.put(Constants.FIELD_TEAM, winner.getTeamId());
            event.put(Constants.FIELD_SCOPE, winner.getTeamScopeId());

            event.put(RaceEvent.KEY_DEF_ID, winner.getRaceId());
            event.put(RaceEvent.KEY_POINTS, winner.getAwardedPoints());
            event.put(RaceEvent.KEY_RACE_STARTED_AT, winner.getRaceStartAt());
            event.put(RaceEvent.KEY_RACE_ENDED_AT, winner.getRaceEndAt());
            event.put(RaceEvent.KEY_RACE_RANK, winner.getRank());
            event.put(RaceEvent.KEY_RACE_SCORE, winner.getPoints());
            event.put(RaceEvent.KEY_RACE_SCORE_COUNT, winner.getTotalCount());

            events.add(event);
        });


        LOG.info("Sending #{} race events to game engine...", events.size());
        eventsService.submitEvents(token, events);
    }

    @Override
    public List<GlobalLeaderboardRecordDto> readGlobalLeaderboard(LeaderboardRequestDto request) throws Exception {
        checkLeaderboardRequest(request);

        LeaderboardDef ldef = request.getLeaderboardDef();
        Map<String, Object> templateData = createLeaderboardTemplateMap(request, ldef);

        Map<String, Object> data = createLeaderboardParams(request, ldef);

        return ServiceUtils.toList(dao.executeQuery(
                Q.LEADERBOARD.GLOBAL_LEADERBOARD,
                data,
                GlobalLeaderboardRecordDto.class,
                templateData));
    }

    @Override
    public List<TeamLeaderboardRecordDto> readTeamLeaderboard(long teamId, LeaderboardRequestDto request) throws Exception {
        Checks.greaterThanZero(teamId, "teamId");
        checkLeaderboardRequest(request);

        LeaderboardDef ldef = request.getLeaderboardDef();
        Map<String, Object> templateData = createLeaderboardTemplateMap(request, ldef);
        templateData.put("hasTeam", true);

        TeamProfile teamProfile = profileService.readTeam(teamId);

        Map<String, Object> data = createLeaderboardParams(request, ldef);
        data.put("teamId", teamId);
        data.put("teamScopeId", teamProfile.getTeamScope());

        return ServiceUtils.toList(dao.executeQuery(
                Q.LEADERBOARD.TEAM_LEADERBOARD,
                data,
                TeamLeaderboardRecordDto.class,
                templateData));
    }

    @Override
    public List<TeamLeaderboardRecordDto> readTeamLeaderboard(LeaderboardRequestDto request) throws Exception {
        checkLeaderboardRequest(request);

        LeaderboardDef ldef = request.getLeaderboardDef();
        Map<String, Object> templateData = createLeaderboardTemplateMap(request, ldef);

        Map<String, Object> data = createLeaderboardParams(request, ldef);
        data.put("teamId", null);
        data.put("teamScopeId", null);

        return ServiceUtils.toList(dao.executeQuery(
                Q.LEADERBOARD.TEAM_LEADERBOARD,
                data,
                TeamLeaderboardRecordDto.class,
                templateData));
    }

    @Override
    public List<TeamLeaderboardRecordDto> readTeamScopeLeaderboard(long teamScopeId, LeaderboardRequestDto request) throws Exception {
        Checks.greaterThanZero(teamScopeId, "teamScopeId");
        checkLeaderboardRequest(request);

        LeaderboardDef ldef = request.getLeaderboardDef();
        Map<String, Object> templateData = createLeaderboardTemplateMap(request, ldef);
        templateData.put("hasTeamScope", true);

        Map<String, Object> data = createLeaderboardParams(request, ldef);
        data.put("teamScopeId", teamScopeId);

        return ServiceUtils.toList(dao.executeQuery(
                Q.LEADERBOARD.TEAM_LEADERBOARD,
                data,
                TeamLeaderboardRecordDto.class,
                templateData));
    }

    private String getInternalToken() {
        return dataCache.getInternalEventSourceToken().getToken();
    }

    private Map<String, Object> createLeaderboardParams(LeaderboardRequestDto request,
                                                        LeaderboardDef ldef) {
        Maps.MapBuilder dataBuilder = Maps.create()
                .put("userId", request.getForUser())
                .put("rangeStart", request.getRangeStart())
                .put("rangeEnd", request.getRangeEnd())
                .put("topN", request.getTopN())
                .put("pointThreshold", request.getMinPointThreshold())
                .put("topThreshold", request.getTopThreshold())
                .put("aggType", AggregatorType.SUM.name());

        if (ldef != null) {
            dataBuilder = dataBuilder
                    .put("ruleIds", ldef.getRuleIds())
                    .put("aggType", Commons.orDefault(ldef.getAggregatorType(), AggregatorType.SUM.name()))
                    .put("excludeRuleIds", ldef.getExcludeRuleIds());
        }

        return dataBuilder.build();
    }

    private Map<String, Object> createLeaderboardTemplateMap(LeaderboardRequestDto request,
                                                             LeaderboardDef ldef) {
        return Maps.create()
                .put("hasTeam", false)
                .put("hasTeamScope", false)
                .put("hasUser", ServiceUtils.isValid(request.getForUser()))
                .put("hasTimeRange", request.getRangeStart() > 0 && request.getRangeEnd() > request.getRangeStart())
                .put("hasInclusions", ldef != null && !Commons.isNullOrEmpty(ldef.getRuleIds()))
                .put("hasExclusions", ldef != null && !Commons.isNullOrEmpty(ldef.getExcludeRuleIds()))
                .put("isTopN", ServiceUtils.isValid(request.getTopN()))
                .put("hasPointThreshold", ServiceUtils.isValid(request.getMinPointThreshold()))
                .put("onlyFinalTops", ServiceUtils.isValid(request.getTopThreshold()))
                .put("hasStates", ldef != null && ldef.hasStates())
                .put("aggType", ldef != null ?
                        (Commons.orDefault(ldef.getAggregatorType(), AggregatorType.SUM.name()))
                    : AggregatorType.SUM.name())
                .build();
    }

    private void checkLeaderboardRequest(LeaderboardRequestDto dto) throws InputValidationException {
        Checks.validate(dto.getRangeStart() <= dto.getRangeEnd(), "Time range end must be greater than or equal to start time!");
        if (ServiceUtils.isValid(dto.getForUser()) && ServiceUtils.isValid(dto.getTopN())) {
            throw new InputValidationException("Top or bottom listing is not supported when " +
                    "a specific user has been specified in the request!");
        }
    }
}
