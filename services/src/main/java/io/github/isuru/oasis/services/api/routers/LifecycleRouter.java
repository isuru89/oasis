package io.github.isuru.oasis.services.api.routers;

import io.github.isuru.oasis.services.api.IOasisApiService;
import io.github.isuru.oasis.services.utils.Maps;
import spark.Spark;

import java.util.Map;

/**
 * @author iweerarathna
 */
public class LifecycleRouter extends BaseRouters {

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

        post("/game/:gameId/start", (req, res) -> {
            return mapToStatus(getLCService().start(asPLong(req, "gameId")));
        });

        post("/game/:gameId/stop", (req, res) -> {
            return mapToStatus(getLCService().stop(asPLong(req, "gameId")));
        });

        post("/challenge/:cid/start", (req, res) -> {
            return mapToStatus(getLCService().startChallenge(asPLong(req, "cid")));
        });

        post("/challenge/:id/stop", (req, res) -> {
            return mapToStatus(getLCService().stop(asPLong(req, "id")));
        });
    }

    private static Map<String, Object> mapToStatus(boolean status) {
        return Maps.create("status", status);
    }
}
