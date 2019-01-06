package io.github.isuru.oasis.services.controllers;

import io.github.isuru.oasis.model.DefaultEntities;
import io.github.isuru.oasis.model.collect.Pair;
import io.github.isuru.oasis.services.DataCache;
import io.github.isuru.oasis.services.configs.OasisConfigurations;
import io.github.isuru.oasis.services.dto.AuthResponse;
import io.github.isuru.oasis.services.exception.ApiAuthException;
import io.github.isuru.oasis.services.model.TokenInfo;
import io.github.isuru.oasis.services.model.UserProfile;
import io.github.isuru.oasis.services.model.UserTeam;
import io.github.isuru.oasis.services.security.TokenSecurityFilter;
import io.github.isuru.oasis.services.services.IProfileService;
import io.github.isuru.oasis.services.utils.AuthUtils;
import io.github.isuru.oasis.services.utils.Maps;
import io.github.isuru.oasis.services.utils.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Controller
public class AuthController {

    @Autowired
    private IProfileService profileService;

    @Autowired
    private OasisConfigurations oasisConfigurations;

    @PostMapping("/auth/logout")
    @ResponseBody
    public Map<String, Object> logout(@RequestAttribute("token") TokenInfo token) throws Exception {
        boolean success = profileService.logoutUser(token.getUser(), System.currentTimeMillis());
        return Maps.create("success", success);
    }

    @PostMapping("/auth/login")
    @ResponseBody
    public AuthResponse login(@RequestHeader(TokenSecurityFilter.AUTHORIZATION) String authHeader,
                              HttpServletResponse response) throws Exception {
        Pair<String, String> basicAuthPair = getBasicAuthPair(authHeader);
        if (basicAuthPair == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No valid authorization header found!");
            return null;
        }

        String username = basicAuthPair.getValue0();
        String password = basicAuthPair.getValue1();

        if (!DefaultEntities.RESERVED_USERS.contains(username)) {
            // @TODO remove this in production
            if (!password.equals(DataCache.get().getAllUserTmpPassword())) {
                AuthUtils.get().ldapAuthUser(username, password);
            }
        }

        // if ldap auth success
        UserProfile profile = profileService.readUserProfile(username);
        if (profile == null) {
            // no profiles associated with user.
            throw new ApiAuthException("Authentication failure! Username or password incorrect!");
        }
        UserTeam team = profileService.findCurrentTeamOfUser(profile.getId());
        int role = UserRole.PLAYER;
        if (team != null) {
            role = team.getRoleId();
        }

        checkReservedUserAuth(username, password, role);

        // authentication successful. Let's create the token
        //
        TokenInfo tokenInfo = new TokenInfo();
        tokenInfo.setRole(role);
        tokenInfo.setUser(profile.getId());
        tokenInfo.setExp(AuthUtils.get().getExpiryDate());
        String token = AuthUtils.get().issueToken(tokenInfo);

        AuthResponse authResponse = new AuthResponse();
        authResponse.setSuccess(true);
        authResponse.setActivated(profile.isActivated());
        authResponse.setToken(token);
        authResponse.setUserProfile(profile);
        return authResponse;
    }

    private void checkReservedUserAuth(String username, String password, int role) throws ApiAuthException {
        if (DefaultEntities.RESERVED_USERS.contains(username)) {
            boolean success = false;
            if (role == UserRole.ADMIN) {
                // admin
                if (password.equals(oasisConfigurations.getDefaultAdminPassword())) {
                    success = true;
                }
            } else if (role == UserRole.CURATOR) {
                // curator
                if (password.equals(oasisConfigurations.getDefaultCuratorPassword())) {
                    success = true;
                }
            } else if (role == UserRole.PLAYER) {
                // player
                if (password.equals(oasisConfigurations.getDefaultPlayerPassword())) {
                    success = true;
                }
            } else {
                throw new ApiAuthException("User '" + username + "' does not have an associate role in Oasis!");
            }

            if (!success) {
                throw new ApiAuthException("Username or password incorrect!");
            }
        }
    }

    private Pair<String, String> getBasicAuthPair(String authHeader) {
        if (authHeader != null) {
            if (authHeader.startsWith(TokenSecurityFilter.BASIC)) {
                String token = authHeader.substring(TokenSecurityFilter.BASIC.length());
                String decode = new String(Base64.getDecoder().decode(token), StandardCharsets.UTF_8);
                String uname = decode.substring(0, decode.indexOf(":"));
                String pword = decode.substring(decode.indexOf(":") + 1);
                return Pair.of(uname, pword);
            }
        }
        return null;
    }
}
