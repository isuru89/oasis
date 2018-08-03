package io.github.isuru.oasis.services.api.routers;

import io.github.isuru.oasis.services.api.IGameDefService;
import io.github.isuru.oasis.services.api.ILifecycleService;
import io.github.isuru.oasis.services.api.IOasisApiService;
import io.github.isuru.oasis.services.utils.JsonTransformer;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

/**
 * @author iweerarathna
 */
public abstract class BaseRouters {

    public static final String JSON_TYPE = "application/json";

    public static final JsonTransformer TRANSFORMER = new JsonTransformer();
    private static final String AUTHORIZATION = "Authorization";

    private final IOasisApiService apiService;

    BaseRouters(IOasisApiService apiService) {
        this.apiService = apiService;
    }

    public IOasisApiService getApiService() {
        return apiService;
    }

    protected IGameDefService getGameDefService() {
        return apiService.getGameDefService();
    }
    protected ILifecycleService getLCService() {
        return apiService.getLifecycleService();
    }

    public abstract void register();

    protected boolean checkAuth(Request request) {
        String auth = request.headers(AUTHORIZATION);
        if (auth != null) {
            if (auth.startsWith("Bearer ")) {
                String token = auth.substring("Bearer ".length());
                return token.startsWith("00");
            }
        }
        return false;
    }

    protected BaseRouters get(String path, Route route) {
        Spark.get(path, JSON_TYPE, route, TRANSFORMER);
        return this;
    }

    protected BaseRouters put(String path, Route route) {
        Spark.put(path, JSON_TYPE, route, TRANSFORMER);
        return this;
    }

    protected BaseRouters post(String path, Route route) {
        Spark.post(path, JSON_TYPE, route, TRANSFORMER);
        return this;
    }

    protected BaseRouters delete(String path, Route route) {
        Route auth = new Route() {
            @Override
            public Object handle(Request request, Response response) throws Exception {
                if (checkAuth(request)) {
                    return route.handle(request, response);
                } else {
                    return Spark.halt(401);
                }
            }
        };
        Spark.delete(path, JSON_TYPE, auth, TRANSFORMER);
        return this;
    }

    protected long asPLong(Request req, String name) {
        return Long.parseLong(req.params(name));
    }

    protected int asPInt(Request req, String name) {
        return Integer.parseInt(req.params(name));
    }

    protected long asQLong(Request req, String name, long defVal) {
        return Long.parseLong(req.queryParamOrDefault(name, String.valueOf(defVal)));
    }

    protected int asQInt(Request req, String name, int defVal) {
        return Integer.parseInt(req.queryParamOrDefault(name, String.valueOf(defVal)));
    }

    protected boolean asQBool(Request req, String name, boolean defVal) {
        return Boolean.parseBoolean(req.queryParamOrDefault(name, String.valueOf(defVal)));
    }

    protected long asPLong(Request req, String name, long defaultVal) {
        if (req.params(name) != null) {
            return Long.parseLong(req.params(name));
        } else {
            return defaultVal;
        }
    }

    protected <T> T bodyAs(Request req, Class<T> clz) throws Exception {
        return TRANSFORMER.parse(req.body(), clz);
    }
}
