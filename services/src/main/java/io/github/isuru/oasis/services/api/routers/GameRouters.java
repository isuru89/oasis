package io.github.isuru.oasis.services.api.routers;

import io.github.isuru.oasis.services.api.IGameService;
import io.github.isuru.oasis.services.api.IOasisApiService;
import io.github.isuru.oasis.services.exception.InputValidationException;
import io.github.isuru.oasis.services.model.BadgeAwardDto;
import io.github.isuru.oasis.services.model.LeaderboardRequestDto;
import io.github.isuru.oasis.services.model.PointAwardDto;
import io.github.isuru.oasis.services.model.enums.LeaderboardType;
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
    private static final String Q_TEAM = "team";
    private static final String Q_TEAM_SCOPE = "teamscope";

    public GameRouters(IOasisApiService apiService) {
        super(apiService);
    }

    @Override
    public void register() {
        get("/leaderboard/weekly", this::leaderboardWeekly);
        get("/leaderboard/monthly", this::leaderboardMonthly);
        get("/leaderboard/daily", this::leaderboarDaily);
        get("/leaderboard/:lid/weekly", this::leaderboardCustomWeekly);
        get("/leaderboard/:lid/monthly", this::leaderboardCustomMonthly);
        get("/leaderboard/:lid/daily", this::leaderboardCustomDaily);
        get("/leaderboard/:lid/custom", this::leaderboardCustom);

        post("/shop/buy", this::shopBuyItem);
        post("/shop/share", this::shopShareItem);

        post("/award/badge", this::awardBadge);
        post("/award/points", this::awardPoints);
    }

    private Object awardBadge(Request req, Response res) throws Exception {
        if (!req.attributes().contains("userId")) {
            throw new InputValidationException("You need to authenticate to award badge!");
        }

        long userId = req.attribute("userId");
        BadgeAwardDto badgeAwardDto = bodyAs(req, BadgeAwardDto.class);
        getApiService().getGameService().awardBadge(userId, badgeAwardDto);
        return true;
    }

    private Object awardPoints(Request req, Response res) throws Exception {
        if (!req.attributes().contains("userId")) {
            throw new InputValidationException("You need to authenticate to award points!");
        }

        long userId = req.attribute("userId");
        PointAwardDto pointAwardDto = bodyAs(req, PointAwardDto.class);
        getApiService().getGameService().awardPoints(userId, pointAwardDto);
        return true;
    }

    private Object shopBuyItem(Request req, Response res) throws Exception {
        if (!req.attributes().contains("userId")) {
            throw new InputValidationException("You need to authenticate to buy an item!");
        }

        long userId = req.attribute("userId");
        ValueMap body = bodyAsMap(req);
        long itemId = body.getLongReq("itemId");
        if (body.has("price")) {
            float price = body.getFloatReq("price");
            getApiService().getGameService().buyItem(userId, itemId, price);
        } else {
            getApiService().getGameService().buyItem(userId, itemId);
        }
        return null;
    }

    private Object shopShareItem(Request req, Response res) throws Exception {
        if (!req.attributes().contains("userId")) {
            throw new InputValidationException("You need to authenticate to share an item!");
        }

        long userId = req.attribute("userId");
        ValueMap body = bodyAsMap(req);
        long itemId = body.getLongReq("itemId");
        long toUser = body.getLongReq("toUser");
        getApiService().getGameService().shareItem(userId, itemId, toUser,
                body.getInt("amount", 1));
        return null;
    }

    private Object leaderboardCustomWeekly(Request req, Response res) throws Exception {
        IGameService gameService = getApiService().getGameService();
        LeaderboardRequestDto requestDto = generate(req, LeaderboardType.CURRENT_WEEK);
        requestDto.setLeaderboardId(asPInt(req, "lid"));
        return gameService.readLeaderboardStatus(requestDto);
    }

    private Object leaderboardCustomMonthly(Request req, Response res) throws Exception {
        IGameService gameService = getApiService().getGameService();
        LeaderboardRequestDto requestDto = generate(req, LeaderboardType.CURRENT_MONTH);
        requestDto.setLeaderboardId(asPInt(req, "lid"));
        return gameService.readLeaderboardStatus(requestDto);
    }

    private Object leaderboardCustomDaily(Request req, Response res) throws Exception {
        IGameService gameService = getApiService().getGameService();
        LeaderboardRequestDto requestDto = generate(req, LeaderboardType.CURRENT_DAY);
        requestDto.setLeaderboardId(asPInt(req, "lid"));
        return gameService.readLeaderboardStatus(requestDto);
    }

    private Object leaderboardCustom(Request req, Response res) throws Exception {
        IGameService gameService = getApiService().getGameService();
        LeaderboardRequestDto requestDto = generate(req, LeaderboardType.CUSTOM);
        requestDto.setLeaderboardId(asPInt(req, "lid"));
        return gameService.readLeaderboardStatus(requestDto);
    }

    private Object leaderboardWeekly(Request req, Response res) throws Exception {
        IGameService gameService = getApiService().getGameService();
        LeaderboardRequestDto requestDto = generate(req, LeaderboardType.CURRENT_WEEK);
        return gameService.readLeaderboardStatus(requestDto);
    }

    private Object leaderboardMonthly(Request req, Response res) throws Exception {
        IGameService gameService = getApiService().getGameService();
        LeaderboardRequestDto requestDto = generate(req, LeaderboardType.CURRENT_MONTH);
        return gameService.readLeaderboardStatus(requestDto);
    }

    private Object leaderboarDaily(Request req, Response res) throws Exception {
        IGameService gameService = getApiService().getGameService();
        LeaderboardRequestDto requestDto = generate(req, LeaderboardType.CURRENT_DAY);
        return gameService.readLeaderboardStatus(requestDto);
    }

    private LeaderboardRequestDto generate(Request req, LeaderboardType type) throws InputValidationException {
        if (req.queryParams(Q_TOP) != null && req.queryParams(Q_BOTTOM) != null) {
            throw new InputValidationException("Leaderboard request cannot have both 'top' and 'bottom' parameters!");
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

        requestDto.setTeamWise(asQBool(req, Q_TEAM, true));
        requestDto.setTeamScopeWise(asQBool(req, Q_TEAM_SCOPE, false));
        requestDto.setTopN(asQInt(req, Q_TOP, 50));
        requestDto.setBottomN(asQInt(req, Q_BOTTOM, 50));
        return requestDto;
    }
}
