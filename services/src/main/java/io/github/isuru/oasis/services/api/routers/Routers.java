package io.github.isuru.oasis.services.api.routers;

import io.github.isuru.oasis.services.exception.ApiAuthException;
import io.github.isuru.oasis.services.exception.InputValidationException;
import io.github.isuru.oasis.services.services.IOasisApiService;
import io.github.isuru.oasis.services.utils.Maps;
import io.github.isuru.oasis.services.utils.OasisOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

/**
 * @author iweerarathna
 */
public final class Routers {

    private static final Logger LOG = LoggerFactory.getLogger(Routers.class);

    private final IOasisApiService apiService;
    private final OasisOptions oasisOptions;

    public Routers(IOasisApiService apiService, OasisOptions oasisOptions) {
        this.apiService = apiService;
        this.oasisOptions = oasisOptions;
    }

    public void register() {
        Spark.before("/*", (req, res) -> res.type(BaseRouters.JSON_TYPE));

        get("/echo", this::handleEcho);

        Spark.path("/event", () -> new EventsRouter(apiService, oasisOptions).register());
        //Spark.path("/auth", () -> new AuthRouter(apiService, oasisOptions).register());
        Spark.path("/stats", () -> new StatsRouter(apiService, oasisOptions).register());
        Spark.path("/def", () -> new DefinitionRouter(apiService, oasisOptions).register());
        Spark.path("/control", () -> new LifecycleRouter(apiService, oasisOptions).register());
        //Spark.path("/admin", () -> new ProfileRouter(apiService, oasisOptions).register());
        Spark.path("/game", () -> new GameRouters(apiService, oasisOptions).register());
    }

    public void registerExceptionHandlers() {
        Spark.exception(InputValidationException.class, (ex, req, res) -> {
            res.status(400);
            LOG.error("Error occurred in {}", req.contextPath(), ex);
            res.body(BaseRouters.TRANSFORMER.toStr(Maps.create()
                    .put("success", false)
                    .put("error", ex.getMessage())
                    .build()));
        });
        Spark.exception(ApiAuthException.class, (ex, req, res) -> {
            res.status(401);
            LOG.error("Error occurred in {}", req.contextPath(), ex);
            res.body(BaseRouters.TRANSFORMER.toStr(Maps.create()
                    .put("success", false)
                    .put("error", ex.getMessage())
                    .build()));
        });
        Spark.exception(Exception.class, (ex, req, res) -> {
            res.status(500);
            LOG.error("Error occurred in {}", req.contextPath(), ex);
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
