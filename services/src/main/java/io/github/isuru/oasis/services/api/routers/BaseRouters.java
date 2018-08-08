package io.github.isuru.oasis.services.api.routers;

import io.github.isuru.oasis.model.collect.Pair;
import io.github.isuru.oasis.services.api.IGameDefService;
import io.github.isuru.oasis.services.api.ILifecycleService;
import io.github.isuru.oasis.services.api.IOasisApiService;
import io.github.isuru.oasis.services.exception.ApiAuthException;
import io.github.isuru.oasis.services.exception.InputValidationException;
import io.github.isuru.oasis.services.utils.UserRole;
import io.github.isuru.oasis.services.utils.AuthUtils;
import io.github.isuru.oasis.services.utils.JsonTransformer;
import io.github.isuru.oasis.services.utils.ValueMap;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

import java.io.UnsupportedEncodingException;
import java.util.Base64;

/**
 * @author iweerarathna
 */
public abstract class BaseRouters {

    static final String JSON_TYPE = "application/json";

    static final JsonTransformer TRANSFORMER = new JsonTransformer();
    private static final String AUTHORIZATION = "Authorization";

    private final IOasisApiService apiService;

    BaseRouters(IOasisApiService apiService) {
        this.apiService = apiService;
    }

    public IOasisApiService getApiService() {
        return apiService;
    }

    IGameDefService getGameDefService() {
        return apiService.getGameDefService();
    }
    ILifecycleService getLCService() {
        return apiService.getLifecycleService();
    }

    public abstract void register();

    Pair<String, String> getBasicAuthPair(Request request) throws ApiAuthException, UnsupportedEncodingException {
        String auth = request.headers(AUTHORIZATION);
        if (auth != null) {
            if (auth.startsWith("Basic ")) {
                String token = auth.substring("Basic ".length());
                String decode = new String(Base64.getDecoder().decode(token), "UTF-8");
                String uname = decode.substring(0, decode.indexOf(":"));
                String pword = decode.substring(decode.indexOf(":") + 1);
                return Pair.of(uname, pword);
            }
        }
        throw new ApiAuthException("No Authorization header is found in the request!");
    }

    void checkAuth(Request request) throws ApiAuthException {
        String auth = request.headers(AUTHORIZATION);
        if (auth != null) {
            if (auth.startsWith("Bearer ")) {
                String token = auth.substring("Bearer ".length());
                AuthUtils.TokenInfo tokenInfo = AuthUtils.get().verifyToken(token);
                request.attribute("token", tokenInfo);
                request.attribute("userId", tokenInfo.getUser()); // set user
            }
        }
    }

    void checkCurator(Request request) throws ApiAuthException {
        AuthUtils.TokenInfo tokenInfo = request.attribute("token");
        if (tokenInfo == null) {
            throw new ApiAuthException("You need to first authenticate to access this api!");
        }
        if (!UserRole.hasRole(tokenInfo.getRole(), UserRole.CURATOR)) {
            throw new ApiAuthException("You do not have necessary permissions to access this api!");
        }
    }

    void checkSameUser(Request req, long userId) throws ApiAuthException {
        long authUser = req.attribute("userId");
        if (userId > 0 && userId == authUser) {
            return;
        }
        throw new ApiAuthException("You are not allowed to access this api!");
    }

    void checkAdmin(Request request) throws ApiAuthException {
        AuthUtils.TokenInfo tokenInfo = request.attribute("token");
        if (tokenInfo == null) {
            throw new ApiAuthException("You need to first authenticate to access this api!");
        }
        if (!UserRole.hasRole(tokenInfo.getRole(), UserRole.ADMIN)) {
            throw new ApiAuthException("You do not have necessary permissions to access this api!");
        }
    }

    protected BaseRouters get(String path, Route route) {
        Spark.get(path, JSON_TYPE, route, TRANSFORMER);
        return this;
    }

    protected BaseRouters put(String path, Route route) {
        Spark.put(path, JSON_TYPE, route, TRANSFORMER);
        return this;
    }

    BaseRouters post(String path, Route route) {
        Spark.post(path, JSON_TYPE, route, TRANSFORMER);
        return this;
    }

    BaseRouters delete(String path, Route route, int role) {
        Route auth = new Route() {
            @Override
            public Object handle(Request request, Response response) throws Exception {
                checkAuth(request);
                AuthUtils.TokenInfo token = request.attribute("token");
                if (!UserRole.hasRole(token.getRole(), role)) {
                    throw new ApiAuthException("You do not have necessary permissions to access this api!");
                }

                return route.handle(request, response);
            }
        };
        Spark.delete(path, JSON_TYPE, auth, TRANSFORMER);
        return this;
    }

    long asPLong(Request req, String name) {
        return Long.parseLong(req.params(name));
    }

    int asPInt(Request req, String name) {
        return Integer.parseInt(req.params(name));
    }

    long asQLong(Request req, String name, long defVal) {
        return Long.parseLong(req.queryParamOrDefault(name, String.valueOf(defVal)));
    }

    int asQInt(Request req, String name, int defVal) {
        return Integer.parseInt(req.queryParamOrDefault(name, String.valueOf(defVal)));
    }

    boolean asQBool(Request req, String name, boolean defVal) {
        return Boolean.parseBoolean(req.queryParamOrDefault(name, String.valueOf(defVal)));
    }

    protected long asPLong(Request req, String name, long defaultVal) {
        if (req.params(name) != null) {
            return Long.parseLong(req.params(name));
        } else {
            return defaultVal;
        }
    }

    <T> T bodyAs(Request req, Class<T> clz) throws InputValidationException {
        try {
            return TRANSFORMER.parse(req.body(), clz);
        } catch (Exception e) {
            throw new InputValidationException("Invalid request body!", e);
        }
    }

    ValueMap bodyAsMap(Request req) throws Exception {
        return new ValueMap(TRANSFORMER.parseAsMap(req.body()));
    }
}
