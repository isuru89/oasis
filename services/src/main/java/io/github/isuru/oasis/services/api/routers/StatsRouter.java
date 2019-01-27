package io.github.isuru.oasis.services.api.routers;

import io.github.isuru.oasis.model.defs.GameDef;
import io.github.isuru.oasis.model.defs.LeaderboardType;
import io.github.isuru.oasis.services.dto.stats.BadgeBreakdownReqDto;
import io.github.isuru.oasis.services.dto.stats.PointBreakdownReqDto;
import io.github.isuru.oasis.services.exception.InputValidationException;
import io.github.isuru.oasis.services.model.UserTeam;
import io.github.isuru.oasis.model.defs.ScopingType;
import io.github.isuru.oasis.services.services.IOasisApiService;
import io.github.isuru.oasis.services.utils.OasisOptions;
import spark.Request;
import spark.Response;

import java.util.List;

/**
 * @author iweerarathna
 */
public class StatsRouter extends BaseRouters {

    StatsRouter(IOasisApiService apiService, OasisOptions oasisOptions) {
        super(apiService, oasisOptions);
    }

    @Override
    public void register() {
        get("/user/:userId/summary", this::userStats);
        get("/user/:userId/badges", this::userBadgesStat);
        get("/user/:userId/milestones", this::userMilestoneStat);
        get("/user/:userId/states", this::userStatesStat);
        get("/user/:userId/items", this::userItemsStat);
        get("/user/:userId/team-history", this::userTeamHistoryStat);
        get("/user/:userId/team-rankings", this::userTeamRankingStat);
        get("/user/:userId/rankings", this::userRankingStat);

        get("/challenge/:challengeId", this::readChallengeStats);

        get("/breakdown/points", this::userBreakdownPointsStat);
        get("/breakdown/badges", this::userBreakdownBadgesStat);
    }

    private Object readChallengeStats(Request req, Response res) throws Exception {
        long challengeId = asPLong(req, "challengeId");
        return getApiService().getStatService().readChallengeStats(challengeId);
    }


    private Object userStats(Request req, Response res) throws Exception {
        long userId = asPLong(req, "userId");
        long since = asQLong(req, "since", 0);
        return getApiService().getStatService().readUserGameStats(userId, since);
    }

    private Object userBadgesStat(Request req, Response res) throws Exception {
        long userId = asPLong(req, "userId");
        long since = asQLong(req, "since", 0);
        return getApiService().getStatService().readUserBadges(userId, since);
    }

    private Object userMilestoneStat(Request req, Response res) throws Exception {
        long userId = asPLong(req, "userId");
        return getApiService().getStatService().readUserMilestones(userId);
    }

    private Object userStatesStat(Request req, Response res) throws Exception {
        long userId = asPLong(req, "userId");
        UserTeam currentTeamOfUser = getApiService().getProfileService().findCurrentTeamOfUser(userId);
        if (currentTeamOfUser == null) {
            throw new InputValidationException("Current team of user '" + userId + "' cannot be found!");
        }
        long teamId = currentTeamOfUser.getTeamId();
        return getApiService().getStatService().readUserStateStats(userId, teamId);
    }


    private Object userItemsStat(Request req, Response res) throws Exception {
        long userId = asPLong(req, "userId");
        long since = asQLong(req, "since", 0);
        return getApiService().getStatService().readUserPurchasedItems(userId, since);
    }

    private Object userTeamHistoryStat(Request req, Response res) throws Exception {
        long userId = asPLong(req, "userId");
        return getApiService().getStatService().readUserTeamHistoryStat(userId);
    }

    private Object userTeamRankingStat(Request req, Response res) throws Exception {
        long userId = asPLong(req, "userId");
        return getApiService().getStatService().readUserTeamRankings(userId);
    }

    private Object userRankingStat(Request req, Response res) throws Exception {
        String gameIdStr = req.queryParamOrDefault("gameId", null);
        Long gameId;
        if (gameIdStr == null) {
            List<GameDef> gameDefs = getApiService().getGameDefService().listGames();
            if (gameDefs == null || gameDefs.isEmpty()) {
                throw new InputValidationException("No game is running at the moment in Oasis!");
            }
            gameId = gameDefs.get(0).getId();
        } else {
            gameId = Long.parseLong(gameIdStr);
        }
        long userId = asPLong(req, "userId");
        ScopingType scopingType = ScopingType.from(asQStr(req, "scope", ""));
        LeaderboardType period = LeaderboardType.from(asQStr(req, "period", ""));
        return getApiService().getStatService().readMyLeaderboardRankings(gameId, userId, scopingType, period);
    }

    private Object userBreakdownPointsStat(Request req, Response res) throws Exception {
        PointBreakdownReqDto reqDto = new PointBreakdownReqDto();
        reqDto.setUserId(asQInt(req, "userId", -1));
        if (reqDto.getUserId() < 0) {
            throw new InputValidationException("Parameter 'userId' is mandatory and must be a valid one!");
        }
        reqDto.setPointId(asQInt(req, "pointId", -1));
        if (reqDto.getPointId() < 0) {
            throw new InputValidationException("Parameter 'pointId' is mandatory and must be a valid one!");
        }
        reqDto.setOffset(asQInt(req, "offset", 0));
        reqDto.setSize(asQInt(req, "size", 0));
        reqDto.setRangeStart(asQLong(req, "start", 0L));
        reqDto.setRangeEnd(asQLong(req, "end", 0L));
        return getApiService().getStatService().getPointBreakdownList(reqDto);
    }

    private Object userBreakdownBadgesStat(Request req, Response res) throws Exception {
        BadgeBreakdownReqDto reqDto = new BadgeBreakdownReqDto();
        reqDto.setUserId(asQInt(req, "userId", -1));
        if (reqDto.getUserId() < 0) {
            throw new InputValidationException("Parameter 'userId' is mandatory and must be a valid one!");
        }
        reqDto.setBadgeId(asQInt(req, "badgeId", -1));
        if (reqDto.getBadgeId() < 0) {
            throw new InputValidationException("Parameter 'badgeId' is mandatory and must be a valid one!");
        }
        reqDto.setOffset(asQInt(req, "offset", 0));
        reqDto.setSize(asQInt(req, "size", 0));
        reqDto.setRangeStart(asQLong(req, "start", 0L));
        reqDto.setRangeEnd(asQLong(req, "end", 0L));
        return getApiService().getStatService().getBadgeBreakdownList(reqDto);
    }

}
