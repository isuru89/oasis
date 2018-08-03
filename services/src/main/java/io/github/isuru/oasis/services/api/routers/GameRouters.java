package io.github.isuru.oasis.services.api.routers;

import io.github.isuru.oasis.services.api.IGameService;
import io.github.isuru.oasis.services.api.IOasisApiService;
import io.github.isuru.oasis.services.exception.InputValidationException;
import io.github.isuru.oasis.services.model.LeaderboardRequestDto;
import io.github.isuru.oasis.services.model.enums.LeaderboardType;
import spark.Request;

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
        IGameService gameService = getApiService().getGameService();

        get("/leaderboard/weekly", (req, res) -> {
            LeaderboardRequestDto requestDto = generate(req, LeaderboardType.CURRENT_WEEK);
            return gameService.readLeaderboardStatus(requestDto);
        });
        get("/leaderboard/monthly", (req, res) -> {
            LeaderboardRequestDto requestDto = generate(req, LeaderboardType.CURRENT_MONTH);
            return gameService.readLeaderboardStatus(requestDto);
        });
        get("/leaderboard/daily", (req, res) -> {
            LeaderboardRequestDto requestDto = generate(req, LeaderboardType.CURRENT_DAY);
            return gameService.readLeaderboardStatus(requestDto);
        });

        get("/leaderboard/:lid/weekly", (req, res) -> {
            LeaderboardRequestDto requestDto = generate(req, LeaderboardType.CURRENT_WEEK);
            requestDto.setLeaderboardId(asPInt(req, "lid"));
            return gameService.readLeaderboardStatus(requestDto);
        });
        get("/leaderboard/:lid/monthly", (req, res) -> {
            LeaderboardRequestDto requestDto = generate(req, LeaderboardType.CURRENT_MONTH);
            requestDto.setLeaderboardId(asPInt(req, "lid"));
            return gameService.readLeaderboardStatus(requestDto);
        });
        get("/leaderboard/:lid/daily", (req, res) -> {
            LeaderboardRequestDto requestDto = generate(req, LeaderboardType.CURRENT_DAY);
            requestDto.setLeaderboardId(asPInt(req, "lid"));
            return gameService.readLeaderboardStatus(requestDto);
        });
        get("/leaderboard/:lid/custom", (req, res) -> {
            LeaderboardRequestDto requestDto = generate(req, LeaderboardType.CUSTOM);
            requestDto.setLeaderboardId(asPInt(req, "lid"));
            return gameService.readLeaderboardStatus(requestDto);
        });
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
