package io.github.isuru.oasis.services.api.routers;

import io.github.isuru.oasis.model.collect.Pair;
import io.github.isuru.oasis.model.configs.Configs;
import io.github.isuru.oasis.services.DataCache;
import io.github.isuru.oasis.services.api.IOasisApiService;
import io.github.isuru.oasis.services.exception.ApiAuthException;
import io.github.isuru.oasis.services.model.UserProfile;
import io.github.isuru.oasis.services.model.UserTeam;
import io.github.isuru.oasis.services.utils.AuthUtils;
import io.github.isuru.oasis.services.utils.Maps;
import io.github.isuru.oasis.services.utils.OasisOptions;
import io.github.isuru.oasis.services.utils.UserRole;
import spark.Request;
import spark.Response;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author iweerarathna
 */
public class AuthRouter extends BaseRouters {

    private static final Set<String> RESERVED_USERS = new HashSet<>(
            Arrays.asList("admin@oasis.com", "player@oasis.com", "curator@oasis.com"));

    private Configs configs;

    AuthRouter(IOasisApiService apiService, OasisOptions oasisOptions) {
        super(apiService, oasisOptions);

        configs = oasisOptions.getConfigs();
    }

    @Override
    public void register() {
        post("/login", this::login);
        post("/logout", this::logout);
    }

    private Object logout(Request req, Response res) throws Exception {
        checkAuth(req);

        AuthUtils.TokenInfo tokenInfo = req.attribute("token");
        boolean status = getApiService().getProfileService()
                .logoutUser(tokenInfo.getUser(), System.currentTimeMillis());
        return Maps.create("success", status);
    }

    private Object login(Request req, Response res) throws Exception {
        Pair<String, String> basicAuthPair = getBasicAuthPair(req);
        String username = basicAuthPair.getValue0();
        String password = basicAuthPair.getValue1();

        if (!RESERVED_USERS.contains(username)) {
            // @TODO remove this in production
            if (!password.equals(DataCache.get().getAllUserTmpPassword())) {
                AuthUtils.get().ldapAuthUser(username, password);
            }
        }

        // if ldap auth success
        UserProfile profile = getApiService().getProfileService().readUserProfile(username);
        if (profile == null) {
            // no profiles associated with user.
            throw new ApiAuthException("Authentication failure! Username or password incorrect!");
        }
        boolean activated = profile.isActivated();
        UserTeam team = getApiService().getProfileService().findCurrentTeamOfUser(profile.getId());
        int role = UserRole.PLAYER;
        if (team != null) {
            role = team.getRoleId();
        }

        if (RESERVED_USERS.contains(username)) {
            if (role == UserRole.ADMIN) {
                // admin
                if (!password.equals(configs.getStrReq("oasis.default.admin.password"))) {
                    throw new ApiAuthException("Username or password incorrect!");
                }
            } else if (role == UserRole.CURATOR) {
                // curator
                if (!password.equals(configs.getStrReq("oasis.default.curator.password"))) {
                    throw new ApiAuthException("Username or password incorrect!");
                }
            } else if (role == UserRole.PLAYER) {
                // player
                if (!password.equals(configs.getStrReq("oasis.default.player.password"))) {
                    throw new ApiAuthException("Username or password incorrect!");
                }
            } else {
                throw new ApiAuthException("You do not have a proper role in Oasis!");
            }
        }

        AuthUtils.TokenInfo info = new AuthUtils.TokenInfo();
        info.setRole(role);
        info.setUser(profile.getId());
        info.setExp(AuthUtils.get().getExpiryDate());
        String token = AuthUtils.get().issueToken(info);
        return Maps.create()
                .put("token", token)
                .put("activated", activated)
                .put("profile", profile)
                .build();
    }

    private static String captureNameFromEmail(String email) {
        int pos = email.lastIndexOf('@');
        if (pos > 0) {
            return email.substring(0, pos);
        } else {
            return email;
        }
    }
}
