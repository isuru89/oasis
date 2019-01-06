package io.github.isuru.oasis.services.controllers;

import io.github.isuru.oasis.model.DefaultEntities;
import io.github.isuru.oasis.model.collect.Pair;
import io.github.isuru.oasis.services.DataCache;
import io.github.isuru.oasis.services.configs.OasisConfigurations;
import io.github.isuru.oasis.services.dto.AuthResponse;
import io.github.isuru.oasis.services.exception.ApiAuthException;
import io.github.isuru.oasis.services.model.UserProfile;
import io.github.isuru.oasis.services.security.CurrentUser;
import io.github.isuru.oasis.services.security.JwtTokenProvider;
import io.github.isuru.oasis.services.security.UserPrincipal;
import io.github.isuru.oasis.services.services.IProfileService;
import io.github.isuru.oasis.services.utils.AuthUtils;
import io.github.isuru.oasis.services.utils.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private IProfileService profileService;

    @Autowired
    private OasisConfigurations oasisConfigurations;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Secured("ROLE_ADMIN")
    @PostMapping("/logout")
    @ResponseBody
    public Map<String, Object> logout(@CurrentUser UserPrincipal user) throws Exception {
        boolean success = profileService.logoutUser(user.getId(), System.currentTimeMillis());
        return Maps.create("success", success);
    }

    @PostMapping("/login")
    @ResponseBody
    public AuthResponse login(@RequestHeader("Authorization") String authHeader,
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

        checkReservedUserAuth(username, password);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        username,
                        password
                )
        );

        String jwt = tokenProvider.generateToken(authentication);

        // authentication successful. Let's create the token
        //
        AuthResponse authResponse = new AuthResponse();
        authResponse.setSuccess(true);
        authResponse.setActivated(profile.isActivated());
        authResponse.setToken(jwt);
        authResponse.setUserProfile(profile);
        return authResponse;
    }

    private void checkReservedUserAuth(String username, String password) throws ApiAuthException {
        if (DefaultEntities.RESERVED_USERS.contains(username)) {
            boolean success = false;
            if (DefaultEntities.DEF_ADMIN_USER.equals(username)) {
                // admin
                if (password.equals(oasisConfigurations.getDefaultAdminPassword())) {
                    success = true;
                }
            } else if (DefaultEntities.DEF_CURATOR_USER.equals(username)) {
                // curator
                if (password.equals(oasisConfigurations.getDefaultCuratorPassword())) {
                    success = true;
                }
            } else if (DefaultEntities.DEF_PLAYER_USER.equals(username)) {
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
            if (authHeader.startsWith("Basic")) {
                String token = authHeader.substring("Basic".length()).trim();
                String decode = new String(Base64.getDecoder().decode(token), StandardCharsets.UTF_8);
                String uname = decode.substring(0, decode.indexOf(":"));
                String pword = decode.substring(decode.indexOf(":") + 1);
                return Pair.of(uname, pword);
            }
        }
        return null;
    }
}
