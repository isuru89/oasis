package io.github.isuru.oasis.services.api.routers;

import io.github.isuru.oasis.model.collect.Pair;
import io.github.isuru.oasis.services.exception.ApiAuthException;
import io.github.isuru.oasis.services.exception.InputValidationException;
import io.github.isuru.oasis.services.model.TokenInfo;
import io.github.isuru.oasis.services.model.UserProfile;
import io.github.isuru.oasis.services.services.IGameDefService;
import io.github.isuru.oasis.services.services.ILifecycleService;
import io.github.isuru.oasis.services.services.IOasisApiService;
import io.github.isuru.oasis.services.utils.*;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

/**
 * @author iweerarathna
 */
public abstract class BaseRouters {

    static final String JSON_TYPE = "application/json";

    static final JsonTransformer TRANSFORMER = new JsonTransformer();
    static final String AUTHORIZATION = "Authorization";
    static final String BEARER = "Bearer ";
    static final String BASIC = "Basic ";

    private final IOasisApiService apiService;
    private final OasisOptions options;

    BaseRouters(IOasisApiService apiService, OasisOptions options) {
        this.apiService = apiService;
        this.options = options;
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

    public void register() {}

    Pair<String, String> getBasicAuthPair(Request request) throws ApiAuthException, UnsupportedEncodingException {
        String auth = request.headers(AUTHORIZATION);
        if (auth != null) {
            if (auth.startsWith(BASIC)) {
                String token = auth.substring(BASIC.length());
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
        if (auth != null && !auth.trim().isEmpty()) {
            if (auth.startsWith(BEARER)) {
                String token = auth.substring(BEARER.length());
                TokenInfo tokenInfo = AuthUtils.get().verifyToken(token);
                checkForLogoutToken(tokenInfo);
                request.attribute("token", tokenInfo);
                request.attribute("userId", tokenInfo.getUser()); // set user
            }
        } else {
            throw new ApiAuthException("You are not allowed to access end point " + request.pathInfo());
        }
    }

    private void checkForLogoutToken(TokenInfo tokenInfo) throws ApiAuthException {
        long userId = tokenInfo.getUser();
        String key = String.format("user.logout.%d", userId);
        Optional<String> logoutOpt = options.getCacheProxy().get(key);
        long lastLogoutTime = -1;
        if (logoutOpt.isPresent()) {
            lastLogoutTime = Long.parseLong(logoutOpt.get());
        } else {
            try {
                UserProfile userProfile = apiService.getProfileService().readUserProfile(tokenInfo.getUser());
                if (userProfile != null && userProfile.getLastLogoutAt() != null) {
                    options.getCacheProxy().update(key, String.valueOf(userProfile.getLastLogoutAt()));
                    lastLogoutTime = userProfile.getLastLogoutAt();
                }
            } catch (Exception ex) {
                throw new ApiAuthException("Cannot verify user in oasis, because no user record found!", ex);
            }
        }

        if (lastLogoutTime > 0 && lastLogoutTime >= tokenInfo.getIssuedAt() * 1000L) {
            throw new ApiAuthException("Provided authentication token is invalid and expired!");
        }
    }

    void checkCurator(Request request) throws ApiAuthException {
        TokenInfo tokenInfo = request.attribute("token");
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
        TokenInfo tokenInfo = request.attribute("token");
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

    BaseRouters post(String path, Route route, int role) {
        Route auth = new Route() {
            @Override
            public Object handle(Request request, Response response) throws Exception {
                checkAuth(request);
                TokenInfo token = request.attribute("token");
                if (!UserRole.hasRole(token.getRole(), role)) {
                    throw new ApiAuthException("You do not have necessary permissions to access this api!");
                }

                return route.handle(request, response);
            }
        };
        Spark.post(path, JSON_TYPE, auth, TRANSFORMER);
        return this;
    }

    BaseRouters delete(String path, Route route, int role) {
        Route auth = new Route() {
            @Override
            public Object handle(Request request, Response response) throws Exception {
                checkAuth(request);
                TokenInfo token = request.attribute("token");
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

    String asQStr(Request req, String name, String defVal) {
        return req.queryParamOrDefault(name, defVal);
    }

    long asQLong(Request req, String name, long defVal) {
        return Long.parseLong(req.queryParamOrDefault(name, String.valueOf(defVal)));
    }

    boolean hasQ(Request req, String name) {
        return req.queryParams() != null && req.queryParams().contains(name);
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

    Map<String, Object> asResAdd(long id) {
        return Maps.create()
                .put("success", true)
                .put("id", id).build();
    }

    Map<String, Object> asResBool(boolean edited) {
        return Maps.create("success", edited);
    }
}
