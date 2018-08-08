package io.github.isuru.oasis.services.api.routers;

import io.github.isuru.oasis.services.api.IOasisApiService;
import io.github.isuru.oasis.services.exception.ApiAuthException;
import io.github.isuru.oasis.services.exception.InputValidationException;
import io.github.isuru.oasis.services.utils.Maps;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

/**
 * @author iweerarathna
 */
public final class Routers {

    private final IOasisApiService apiService;

    public Routers(IOasisApiService apiService) {
        this.apiService = apiService;
    }

    public void register() {
        Spark.before("/*", (req, res) -> res.type(BaseRouters.JSON_TYPE));

        get("/echo", this::handleEcho);

        Spark.path("/auth", () -> new AuthRouter(apiService).register());
        Spark.path("/def", () -> new DefinitionRouter(apiService).register());
        Spark.path("/control", () -> new LifecycleRouter(apiService).register());
        Spark.path("/admin", () -> new ProfileRouter(apiService).register());
        Spark.path("/game", () -> new GameRouters(apiService).register());
        new EventsRouter(apiService).register();
    }

    public void registerExceptionHandlers() {
        Spark.exception(InputValidationException.class, (ex, req, res) -> {
            res.status(400);
            res.body(BaseRouters.TRANSFORMER.toStr(Maps.create()
                    .put("success", false)
                    .put("error", ex.getMessage())
                    .build()));
        });
        Spark.exception(ApiAuthException.class, (ex, req, res) -> {
            res.status(401);
            res.body(BaseRouters.TRANSFORMER.toStr(Maps.create()
                    .put("success", false)
                    .put("error", ex.getMessage())
                    .build()));
        });
        Spark.exception(Exception.class, (ex, req, res) -> {
            res.status(500);
            res.body(BaseRouters.TRANSFORMER.toStr(Maps.create()
                    .put("success", false)
                    .put("error", ex.getMessage())
                    .build()));
        });
    }

    protected void get(String path, Route route) {
        Spark.get(path, BaseRouters.JSON_TYPE, route, BaseRouters.TRANSFORMER);
    }

    private Object handleEcho(Request req, Response res) {
        return Maps.create("message", "Oasis is working!");
    }
}
