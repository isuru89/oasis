package io.github.isuru.oasis.services.services;

import io.github.isuru.oasis.model.Constants;
import io.github.isuru.oasis.model.db.IOasisDao;
import io.github.isuru.oasis.model.defs.LeaderboardDef;
import io.github.isuru.oasis.model.events.EventNames;
import io.github.isuru.oasis.services.DataCache;
import io.github.isuru.oasis.services.dto.game.BadgeAwardDto;
import io.github.isuru.oasis.services.dto.game.LeaderboardRequestDto;
import io.github.isuru.oasis.services.dto.game.PointAwardDto;
import io.github.isuru.oasis.services.dto.game.UserRankRecordDto;
import io.github.isuru.oasis.services.exception.ApiAuthException;
import io.github.isuru.oasis.services.exception.InputValidationException;
import io.github.isuru.oasis.services.model.TeamProfile;
import io.github.isuru.oasis.services.model.UserTeam;
import io.github.isuru.oasis.services.utils.Checks;
import io.github.isuru.oasis.services.utils.Commons;
import io.github.isuru.oasis.services.utils.Maps;
import io.github.isuru.oasis.services.model.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author iweerarathna
 */
@Service("gameService")
public class GameServiceImpl implements IGameService {

    @Autowired
    private IProfileService profileService;

    @Autowired
    private IEventsService eventsService;

    @Autowired
    private IOasisDao dao;

    @Autowired
    private DataCache dataCache;

    @Override
    public void awardPoints(long byUser, PointAwardDto awardDto) throws Exception {
        Checks.greaterThanZero(byUser, "user");
        Checks.greaterThanZero(awardDto.getToUser(), "toUser");
        Checks.validate(awardDto.getAmount() != 0.0f, "Point amount should not be equal to zero!");
        Checks.validate(awardDto.getToUser() != byUser, "You cannot award points to yourself!");

        UserTeam currentTeamOfUser = profileService.findCurrentTeamOfUser(awardDto.getToUser());
        long teamId = currentTeamOfUser != null ? currentTeamOfUser.getTeamId() : dataCache.getTeamDefault().getId();
        long scopeId = currentTeamOfUser != null ? currentTeamOfUser.getScopeId() : dataCache.getTeamScopeDefault().getId();
        long gameId = awardDto.getGameId() != null ? awardDto.getGameId() : dataCache.getDefGameId();

        // only curators and admins can award points to the same user as
        if (dataCache.getAdminUserId() != byUser
                && profileService.listCurrentUserRoles(byUser).stream().noneMatch(uts -> uts.isApproved()
                                    && scopeId == uts.getTeamScopeId()
                                    && uts.getUserRole() == UserRole.CURATOR)) {
            throw new ApiAuthException("You cannot award points to user " + awardDto.getToUser()
                    + ", because you do not have required permissions or user is not belong to same team scope as you!");
        }

        Map<String, Object> data = Maps.create()
                .put(Constants.FIELD_EVENT_TYPE, EventNames.EVENT_COMPENSATE_POINTS)
                .put(Constants.FIELD_TIMESTAMP, awardDto.getTs() == null ? System.currentTimeMillis() : awardDto.getTs())
                .put(Constants.FIELD_USER, awardDto.getToUser())
                .put(Constants.FIELD_TEAM, teamId)
                .put(Constants.FIELD_SCOPE, scopeId)
                .put(Constants.FIELD_GAME_ID, gameId)
                .put(Constants.FIELD_ID, awardDto.getAssociatedEventId())
                .put("amount", awardDto.getAmount())
                .put("tag", String.valueOf(byUser))
                .build();

        String token = getInternalToken();
        eventsService.submitEvent(token, data);
    }

    @Override
    public void awardBadge(long byUser, BadgeAwardDto awardDto) throws Exception {
        Checks.greaterThanZero(byUser, "user");
        Checks.greaterThanZero(awardDto.getToUser(), "toUser");
        Checks.greaterThanZero(awardDto.getBadgeId(), "Badge id must be a valid one!");
        Checks.validate(byUser != awardDto.getToUser(), "You cannot award badges to yourself!");

        UserTeam currentTeamOfUser = profileService.findCurrentTeamOfUser(awardDto.getToUser());
        long teamId = currentTeamOfUser != null ? currentTeamOfUser.getTeamId() : dataCache.getTeamDefault().getId();
        long scopeId = currentTeamOfUser != null ? currentTeamOfUser.getScopeId() : dataCache.getTeamScopeDefault().getId();
        long gameId = awardDto.getGameId() != null ? awardDto.getGameId() : dataCache.getDefGameId();

        // only curators and admins can award points to the same user as
        if (dataCache.getAdminUserId() != byUser
                && profileService.listCurrentUserRoles(byUser).stream().noneMatch(uts -> uts.isApproved()
                && scopeId == uts.getTeamScopeId()
                && uts.getUserRole() == UserRole.CURATOR)) {
            throw new ApiAuthException("You cannot award badges to user " + awardDto.getToUser()
                    + ", because you do not have required permissions or user is not belong to same team scope as you!");
        }

        Map<String, Object> data = Maps.create()
                .put(Constants.FIELD_EVENT_TYPE, EventNames.EVENT_AWARD_BADGE)
                .put(Constants.FIELD_TIMESTAMP, awardDto.getTs() == null ? System.currentTimeMillis() : awardDto.getTs())
                .put(Constants.FIELD_USER, awardDto.getToUser())
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
    }

    @Override
    public List<UserRankRecordDto> readGlobalLeaderboard(LeaderboardRequestDto request) throws Exception {
        checkLeaderboardRequest(request);

        LeaderboardDef ldef = request.getLeaderboardDef();
        Map<String, Object> templateData = Maps.create()
                .put("hasUser", ServiceUtils.isValid(request.getForUser()))
                .put("hasTimeRange", request.getRangeStart() > 0 && request.getRangeEnd() > request.getRangeStart())
                .put("hasInclusions", ldef != null && !Commons.isNullOrEmpty(ldef.getRuleIds()))
                .put("hasExclusions", ldef != null && !Commons.isNullOrEmpty(ldef.getExcludeRuleIds()))
                .put("isTopN", ServiceUtils.isValid(request.getTopN()))
                .put("isBottomN", ServiceUtils.isValid(request.getBottomN()))
                .build();


        Maps.MapBuilder dataBuilder = Maps.create()
                .put("userId", request.getForUser())
                .put("rangeStart", request.getRangeStart())
                .put("rangeEnd", request.getRangeEnd())
                .put("topN", request.getTopN())
                .put("bottomN", request.getBottomN());

        if (ldef != null) {
            dataBuilder = dataBuilder.put("ruleIds", ldef.getRuleIds())
                    .put("excludeRuleIds", ldef.getExcludeRuleIds());
        }

        return ServiceUtils.toList(dao.executeQuery(
                Q.LEADERBOARD.GLOBAL_LEADERBOARD,
                dataBuilder.build(),
                UserRankRecordDto.class,
                templateData));
    }

    @Override
    public List<UserRankRecordDto> readTeamLeaderboard(long teamId, LeaderboardRequestDto request) throws Exception {
        Checks.greaterThanZero(teamId, "teamId");
        checkLeaderboardRequest(request);

        LeaderboardDef ldef = request.getLeaderboardDef();
        Map<String, Object> templateData = Maps.create()
                .put("hasTeam", true)
                .put("hasUser", ServiceUtils.isValid(request.getForUser()))
                .put("hasTimeRange", request.getRangeStart() > 0 && request.getRangeEnd() > request.getRangeStart())
                .put("hasInclusions", ldef != null && !Commons.isNullOrEmpty(ldef.getRuleIds()))
                .put("hasExclusions", ldef != null && !Commons.isNullOrEmpty(ldef.getExcludeRuleIds()))
                .put("isTopN", ServiceUtils.isValid(request.getTopN()))
                .put("isBottomN", ServiceUtils.isValid(request.getBottomN()))
                .build();

        TeamProfile teamProfile = profileService.readTeam(teamId);

        Maps.MapBuilder dataBuilder = Maps.create()
                .put("teamId", teamId)
                .put("userId", request.getForUser())
                .put("teamScopeId", teamProfile.getTeamScope())
                .put("rangeStart", request.getRangeStart())
                .put("rangeEnd", request.getRangeEnd())
                .put("topN", request.getTopN())
                .put("bottomN", request.getBottomN());

        if (ldef != null) {
            dataBuilder = dataBuilder.put("ruleIds", ldef.getRuleIds())
                    .put("excludeRuleIds", ldef.getExcludeRuleIds());
        }

        return ServiceUtils.toList(dao.executeQuery(
                Q.LEADERBOARD.TEAM_LEADERBOARD,
                dataBuilder.build(),
                UserRankRecordDto.class,
                templateData));
    }

    @Override
    public List<UserRankRecordDto> readTeamScopeLeaderboard(long teamScopeId, LeaderboardRequestDto request) throws Exception {
        Checks.greaterThanZero(teamScopeId, "teamScopeId");
        checkLeaderboardRequest(request);

        LeaderboardDef ldef = request.getLeaderboardDef();
        Map<String, Object> templateData = Maps.create()
                .put("hasTeam", false)
                .put("hasUser", ServiceUtils.isValid(request.getForUser()))
                .put("hasTimeRange", request.getRangeStart() > 0 && request.getRangeEnd() > request.getRangeStart())
                .put("hasInclusions", ldef != null && !Commons.isNullOrEmpty(ldef.getRuleIds()))
                .put("hasExclusions", ldef != null && !Commons.isNullOrEmpty(ldef.getExcludeRuleIds()))
                .put("isTopN", ServiceUtils.isValid(request.getTopN()))
                .put("isBottomN", ServiceUtils.isValid(request.getBottomN()))
                .build();

        Maps.MapBuilder dataBuilder = Maps.create()
                .put("teamScopeId", teamScopeId)
                .put("userId", request.getForUser())
                .put("rangeStart", request.getRangeStart())
                .put("rangeEnd", request.getRangeEnd())
                .put("topN", request.getTopN())
                .put("bottomN", request.getBottomN());

        if (ldef != null) {
            dataBuilder = dataBuilder.put("ruleIds", ldef.getRuleIds())
                    .put("excludeRuleIds", ldef.getExcludeRuleIds());
        }

        return ServiceUtils.toList(dao.executeQuery(
                Q.LEADERBOARD.TEAM_LEADERBOARD,
                dataBuilder.build(),
                UserRankRecordDto.class,
                templateData));
    }

    private String getInternalToken() {
        return dataCache.getInternalEventSourceToken().getToken();
    }

    private void checkLeaderboardRequest(LeaderboardRequestDto dto) throws InputValidationException {
        Checks.validate(dto.getRangeStart() <= dto.getRangeEnd(), "Time range end must be greater than or equal to start time!");
        if (ServiceUtils.isValid(dto.getForUser()) && (ServiceUtils.isValid(dto.getTopN()) || ServiceUtils.isValid(dto.getBottomN()))) {
            throw new InputValidationException("Top or bottom listing is not supported when " +
                    "a specific user has been specified in the request!");
        }
    }
}
