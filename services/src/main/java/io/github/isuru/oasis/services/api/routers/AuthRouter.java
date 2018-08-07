package io.github.isuru.oasis.services.api.routers;

import io.github.isuru.oasis.model.collect.Pair;
import io.github.isuru.oasis.model.configs.Configs;
import io.github.isuru.oasis.services.api.IOasisApiService;
import io.github.isuru.oasis.services.exception.ApiAuthException;
import io.github.isuru.oasis.services.model.UserProfile;
import io.github.isuru.oasis.services.model.UserTeam;
import io.github.isuru.oasis.services.model.enums.UserRole;
import io.github.isuru.oasis.services.utils.AuthUtils;
import io.github.isuru.oasis.services.utils.Maps;
import spark.Request;
import spark.Response;

/**
 * @author iweerarathna
 */
public class AuthRouter extends BaseRouters {

    public AuthRouter(IOasisApiService apiService) {
        super(apiService);
    }

    @Override
    public void register() {
        post("/login", this::login)
        .post("/logout", this::logout);
    }

    private Object logout(Request req, Response res) throws Exception {
        checkAuth(req);
        return Maps.create("success", true);
    }

    private Object login(Request req, Response res) throws Exception {
        Pair<String, String> basicAuthPair = getBasicAuthPair(req);
        String pw = basicAuthPair.getValue1();
        UserProfile profile = getApiService().getProfileService().readUserProfile(basicAuthPair.getValue0());
        if (profile == null) {
            throw new ApiAuthException("Authentication failure! Username or password incorrect!");
        }
        UserTeam team = getApiService().getProfileService().findCurrentTeamOfUser(profile.getId());
        int role = UserRole.ADMIN.getIndex();
        if (team != null) {
            role = team.getRoleId();
        }
        if (role == UserRole.ADMIN.getIndex()) {
            // admin
            if (!pw.equals(Configs.get().getStrReq("oasis.default.admin.password"))) {
                throw new ApiAuthException("Authentication failure! Username or password incorrect!");
            }
        } else if (role == UserRole.CURATOR.getIndex()) {
            // curator
            if (!pw.equals(Configs.get().getStrReq("oasis.default.curator.password"))) {
                throw new ApiAuthException("Authentication failure! Username or password incorrect!");
            }
        } else if (role == UserRole.PLAYER.getIndex()) {
            // player
            if (!pw.equals(Configs.get().getStrReq("oasis.default.player.password"))) {
                throw new ApiAuthException("Authentication failure! Username or password incorrect!");
            }
        }

        AuthUtils.TokenInfo info = new AuthUtils.TokenInfo();
        info.setAdmin(role == UserRole.ADMIN.getIndex());
        info.setCurator(role == UserRole.CURATOR.getIndex());
        info.setUser(profile.getId());
        info.setExp(1956561602000L);
        String token = AuthUtils.get().issueToken(info);
        return Maps.create()
                .put("token", token)
                .build();
    }
}
