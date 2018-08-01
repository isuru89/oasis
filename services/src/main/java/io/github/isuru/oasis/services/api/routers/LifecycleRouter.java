package io.github.isuru.oasis.services.api.routers;

import io.github.isuru.oasis.services.api.IOasisApiService;
import io.github.isuru.oasis.services.utils.Maps;
import spark.Spark;

import java.util.Map;

/**
 * @author iweerarathna
 */
public class LifecycleRouter extends BaseRouters {

    public LifecycleRouter(IOasisApiService apiService) {
        super(apiService);
    }

    @Override
    public void register() {
        post("/game/:gameId/start", (req, res) -> {
            if (checkAuth(req)) {
                return mapToStatus(getLCService().start(asPLong(req, "gameId")));
            } else {
                return Spark.halt(401);
            }
        });

        post("/game/:gameId/stop", (req, res) -> {
            if (checkAuth(req)) {
                return mapToStatus(getLCService().stop(asPLong(req, "gameId")));
            } else {
                return Spark.halt(401);
            }
        });

        post("/challenge/:cid/start", (req, res) -> {
            if (checkAuth(req)) {
                return mapToStatus(getLCService().startChallenge(asPLong(req, "cid")));
            } else {
                return Spark.halt(401);
            }
        });

        post("/challenge/:id/stop", (req, res) -> {
            if (checkAuth(req)) {
                return mapToStatus(getLCService().stop(asPLong(req, "id")));
            } else {
                return Spark.halt(401);
            }
        });
    }

    private static Map<String, Object> mapToStatus(boolean status) {
        return Maps.create("status", status);
    }
}
