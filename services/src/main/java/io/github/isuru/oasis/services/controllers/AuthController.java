package io.github.isuru.oasis.services.controllers;

import io.github.isuru.oasis.model.DefaultEntities;
import io.github.isuru.oasis.model.collect.Pair;
import io.github.isuru.oasis.services.DataCache;
import io.github.isuru.oasis.services.configs.OasisConfigurations;
import io.github.isuru.oasis.services.dto.AuthResponse;
import io.github.isuru.oasis.services.dto.StatusResponse;
import io.github.isuru.oasis.services.model.UserProfile;
import io.github.isuru.oasis.services.security.CurrentUser;
import io.github.isuru.oasis.services.security.JwtTokenProvider;
import io.github.isuru.oasis.services.security.OasisAuthenticator;
import io.github.isuru.oasis.services.security.UserPrincipal;
import io.github.isuru.oasis.services.services.IProfileService;
import io.github.isuru.oasis.services.utils.Checks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Controller
@SuppressWarnings("unused")
@RequestMapping("/auth")
public class AuthController {

    private static final Logger LOG = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private IProfileService profileService;

    @Autowired
    private OasisConfigurations oasisConfigurations;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private DataCache dataCache;

    @Autowired
    private OasisAuthenticator authenticator;

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/logout")
    @ResponseBody
    public StatusResponse logout(@CurrentUser UserPrincipal user) throws Exception {
        boolean success = profileService.logoutUser(user.getId(), System.currentTimeMillis());
        return new StatusResponse(success);
    }

    @PostMapping("/login")
    @ResponseBody
    public AuthResponse login(@RequestHeader("Authorization") String authHeader) throws Exception {
        Pair<String, String> basicAuthPair = getBasicAuthPair(authHeader);
        if (basicAuthPair == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No valid authorization header found!");
        }

        String username = basicAuthPair.getValue0();
        String password = basicAuthPair.getValue1();

        if (!DefaultEntities.RESERVED_USERS.contains(username)) {
            // @TODO remove this in production
            if (!password.equals(dataCache.getAllUserTmpPassword())) {
                if (!authenticator.authenticate(username, password)) {
                    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication failed for user " + username + "!");
                }
            }
        }

        // if ldap auth success
        UserProfile profile = profileService.readUserProfile(username);
        if (profile == null) {
            // no profiles associated with user.
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is not found or registered in the Oasis!");
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

    private void checkReservedUserAuth(String username, String password) {
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
            } else {
                // player
                if (password.equals(oasisConfigurations.getDefaultPlayerPassword())) {
                    success = true;
                }
            }

            if (!success) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Username or password incorrect!");
            }
        }
    }

    private Pair<String, String> getBasicAuthPair(String authHeader) {
        if (!Checks.isNullOrEmpty(authHeader)) {
            if (authHeader.startsWith("Basic")) {
                try {
                    String token = authHeader.substring("Basic".length()).trim();
                    String decode = new String(Base64.getDecoder().decode(token), StandardCharsets.UTF_8);
                    if (decode.contains(":")) {
                        String uname = decode.substring(0, decode.indexOf(":"));
                        String pword = decode.substring(decode.indexOf(":") + 1);
                        return Pair.of(uname, pword);
                    }
                } catch (IllegalArgumentException e) {
                    LOG.error("Authorization header is incorrect! " + authHeader);
                    return null;
                }
            }
        }
        return null;
    }
}
