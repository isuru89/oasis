package io.github.isuru.oasis.services.api.routers;

import io.github.isuru.oasis.model.defs.LeaderboardDef;
import io.github.isuru.oasis.services.api.IGameService;
import io.github.isuru.oasis.services.api.IOasisApiService;
import io.github.isuru.oasis.services.exception.InputValidationException;
import io.github.isuru.oasis.services.model.BadgeAwardDto;
import io.github.isuru.oasis.services.model.LeaderboardRequestDto;
import io.github.isuru.oasis.services.model.PointAwardDto;
import io.github.isuru.oasis.model.defs.LeaderboardType;
import io.github.isuru.oasis.services.utils.ValueMap;
import spark.Request;
import spark.Response;

/**
 * @author iweerarathna
 */
public class GameRouters extends BaseRouters {

    private static final String Q_TOP = "top";
    private static final String Q_BOTTOM = "bottom";
    private static final String Q_WHEN = "when";
    private static final String Q_START = "start";
    private static final String Q_END = "end";
    private static final String Q_USER = "user";
    private static final String USER_ID = "userId";
    private static final String ITEM_ID = "itemId";
    private static final String TEAM_ID = "teamId";
    private static final String SCOPE_ID = "scopeId";
    private static final String LB_ID = "lid";

    GameRouters(IOasisApiService apiService) {
        super(apiService);
    }

    @Override
    public void register() {
        get("/leaderboard/:lid/global", this::leaderboardGlobal);
        get("/leaderboard/:lid/team/:teamId", this::leaderboardTeam);
        get("/leaderboard/:lid/teamscope/:scopeId", this::leaderboardTeamScope);

        post("/shop/buy", this::shopBuyItem);
        post("/shop/share", this::shopShareItem);

        post("/award/badge", this::awardBadge);
        post("/award/points", this::awardPoints);
    }

    private Object awardBadge(Request req, Response res) throws Exception {
        if (!req.attributes().contains(USER_ID)) {
            throw new InputValidationException("You need to authenticate to award badge!");
        }

        long userId = req.attribute(USER_ID);
        checkSameUser(req, userId);

        BadgeAwardDto badgeAwardDto = bodyAs(req, BadgeAwardDto.class);
        getApiService().getGameService().awardBadge(userId, badgeAwardDto);
        return true;
    }

    private Object awardPoints(Request req, Response res) throws Exception {
        if (!req.attributes().contains(USER_ID)) {
            throw new InputValidationException("You need to authenticate to award points!");
        }

        long userId = req.attribute(USER_ID);
        checkSameUser(req, userId);

        PointAwardDto pointAwardDto = bodyAs(req, PointAwardDto.class);
        getApiService().getGameService().awardPoints(userId, pointAwardDto);
        return true;
    }

    private Object shopBuyItem(Request req, Response res) throws Exception {
        if (!req.attributes().contains(USER_ID)) {
            throw new InputValidationException("You need to authenticate to buy an item!");
        }

        long userId = req.attribute(USER_ID);
        checkSameUser(req, userId);

        ValueMap body = bodyAsMap(req);
        long itemId = body.getLongReq(ITEM_ID);
        if (body.has("price")) {
            float price = body.getFloatReq("price");
            getApiService().getGameService().buyItem(userId, itemId, price);
        } else {
            getApiService().getGameService().buyItem(userId, itemId);
        }
        return null;
    }

    private Object shopShareItem(Request req, Response res) throws Exception {
        if (!req.attributes().contains(USER_ID)) {
            throw new InputValidationException("You need to authenticate to share an item!");
        }

        long userId = req.attribute(USER_ID);
        checkSameUser(req, userId);

        ValueMap body = bodyAsMap(req);
        long itemId = body.getLongReq(ITEM_ID);
        long toUser = body.getLongReq("toUser");
        getApiService().getGameService().shareItem(userId, itemId, toUser,
                body.getInt("amount", 1));
        return null;
    }

    private Object leaderboardGlobal(Request req, Response res) throws Exception {
        IGameService gameService = getApiService().getGameService();
        String range = asQStr(req, "range", "weekly");
        LeaderboardRequestDto requestDto = generate(req, toType(range));

        int leaderboardId = asPInt(req, LB_ID);
        requestDto.setLeaderboardDef(readLeaderboardDef(leaderboardId));
        return gameService.readGlobalLeaderboard(requestDto);
    }

    private Object leaderboardTeam(Request req, Response res) throws Exception {
        IGameService gameService = getApiService().getGameService();
        String range = asQStr(req, "range", "weekly");
        LeaderboardRequestDto requestDto = generate(req, toType(range));

        long teamId = asPLong(req, TEAM_ID);
        int leaderboardId = asPInt(req, LB_ID);
        requestDto.setLeaderboardDef(readLeaderboardDef(leaderboardId));
        return gameService.readTeamLeaderboard(teamId, requestDto);
    }

    private Object leaderboardTeamScope(Request req, Response res) throws Exception {
        IGameService gameService = getApiService().getGameService();
        String range = asQStr(req, "range", "weekly");
        LeaderboardRequestDto requestDto = generate(req, toType(range));

        long teamScopeId = asPLong(req, SCOPE_ID);
        int leaderboardId = asPInt(req, LB_ID);
        requestDto.setLeaderboardDef(readLeaderboardDef(leaderboardId));
        return gameService.readTeamScopeLeaderboard(teamScopeId, requestDto);
    }

    private static LeaderboardType toType(String rangeType) throws InputValidationException {
        if (rangeType.startsWith("week")) {
            return LeaderboardType.CURRENT_WEEK;
        } else if (rangeType.startsWith("month")) {
            return LeaderboardType.CURRENT_MONTH;
        } else if (rangeType.startsWith("da")) {
            return LeaderboardType.CURRENT_DAY;
        } else if (rangeType.startsWith("custom")) {
            return LeaderboardType.CUSTOM;
        } else {
            throw new InputValidationException("Unknown range query parameter value! [" + rangeType + "]");
        }
    }

    private LeaderboardRequestDto generate(Request req, LeaderboardType type) throws InputValidationException {
        if (req.queryParams(Q_TOP) != null && req.queryParams(Q_BOTTOM) != null) {
            throw new InputValidationException("Leaderboard request cannot have " +
                    "both 'top' and 'bottom' parameters!");
        }

        LeaderboardRequestDto requestDto = null;
        if (req.queryParams(Q_WHEN) != null) {
            if (type == null) {
                throw new InputValidationException("Leaderboard type must be defined 'when' is specified!");
            }
            long when = Long.parseLong(req.queryParams(Q_WHEN));
            requestDto = new LeaderboardRequestDto(type, when);

        } else if (req.queryParams(Q_START) != null && req.queryParams(Q_END) != null) {
            long start = Long.parseLong(req.queryParams(Q_START));
            long end = Long.parseLong(req.queryParams(Q_END));
            requestDto = new LeaderboardRequestDto(start, end);
            requestDto.setType(LeaderboardType.CUSTOM);

        } else if (!type.isCustom()) {
            requestDto = new LeaderboardRequestDto(type, System.currentTimeMillis());
        }

        if (requestDto == null) {
            throw new InputValidationException("Custom leaderboards must specify time range!");
        }

        requestDto.setForUser(hasQ(req, Q_USER) ? asQLong(req, Q_USER, -1) : null);
        requestDto.setTopN(hasQ(req, Q_TOP) ? asQInt(req, Q_TOP, 50) : null);
        requestDto.setBottomN(hasQ(req, Q_BOTTOM) ? asQInt(req, Q_BOTTOM, 50) : null);
        return requestDto;
    }

    private LeaderboardDef readLeaderboardDef(int id) throws Exception {
        return getApiService().getGameDefService().readLeaderboardDef(id);
    }
}
