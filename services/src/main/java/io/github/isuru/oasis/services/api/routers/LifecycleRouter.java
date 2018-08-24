package io.github.isuru.oasis.services.api.routers;

import io.github.isuru.oasis.services.api.IOasisApiService;
import io.github.isuru.oasis.services.utils.Maps;
import spark.Request;
import spark.Response;
import spark.Spark;

import java.util.Map;

/**
 * @author iweerarathna
 */
public class LifecycleRouter extends BaseRouters {

    private static final String GAME_ID = "gameId";
    private static final String CHALLENGE_ID = "challengeId";

    LifecycleRouter(IOasisApiService apiService) {
        super(apiService);
    }

    @Override
    public void register() {
        Spark.before("/game/*", (request, response) -> {
            checkAuth(request);
            checkAdmin(request);
        });
        Spark.before("/challenge/*", (request, response) -> {
            checkAdmin(request);
            checkCurator(request);
        });

        post("/game/:gameId/start", this::startGame);
        post("/game/:gameId/stop", this::stopGame);
        post("/challenge/:challengeId/start", this::startChallenge);
        post("/challenge/:challengeId/stop", this::stopChallenge);
    }

    private Object startGame(Request req, Response res) throws Exception {
        return mapToStatus(getLCService().start(asPLong(req, GAME_ID)));
    }

    private Object startChallenge(Request req, Response res) throws Exception {
        return mapToStatus(getLCService().startChallenge(asPLong(req, CHALLENGE_ID)));
    }

    private Object stopGame(Request req, Response res) throws Exception {
        return mapToStatus(getLCService().stop(asPLong(req, GAME_ID)));
    }

    private Object stopChallenge(Request req, Response res) throws Exception {
        return mapToStatus(getLCService().stop(asPLong(req, CHALLENGE_ID)));
    }

    private static Map<String, Object> mapToStatus(boolean status) {
        return Maps.create("status", status);
    }
}
