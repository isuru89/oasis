package io.github.isuru.oasis.services.controllers;

import io.github.isuru.oasis.model.collect.Pair;
import io.github.isuru.oasis.services.dto.AuthResponse;
import io.github.isuru.oasis.services.security.TokenSecurityFilter;
import io.github.isuru.oasis.services.utils.AuthUtils;
import io.github.isuru.oasis.services.utils.Maps;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.Map;

@Controller
public class AuthController {

    @PostMapping("/auth/logout")
    @ResponseBody
    public Map<String, Object> logout(@RequestAttribute("token") AuthUtils.TokenInfo token) {
        System.out.println(token.getUser());
        System.out.println(token.getTeamId());
        return Maps.create("success", true);
    }

    @PostMapping("/auth/login")
    @ResponseBody
    public AuthResponse login(@RequestHeader(TokenSecurityFilter.AUTHORIZATION) String authHeader,
                              HttpServletResponse response) throws IOException {
        Pair<String, String> basicAuthPair = getBasicAuthPair(authHeader);
        if (basicAuthPair == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No valid authorization header found!");
            return null;
        }

        String username = basicAuthPair.getValue0();
        String password = basicAuthPair.getValue1();


        AuthResponse authResponse = new AuthResponse();
        authResponse.setSuccess(true);
        return authResponse;
    }

    private Pair<String, String> getBasicAuthPair(String authHeader) throws UnsupportedEncodingException {
        if (authHeader != null) {
            if (authHeader.startsWith(TokenSecurityFilter.BASIC)) {
                String token = authHeader.substring(TokenSecurityFilter.BASIC.length());
                String decode = new String(Base64.getDecoder().decode(token), "UTF-8");
                String uname = decode.substring(0, decode.indexOf(":"));
                String pword = decode.substring(decode.indexOf(":") + 1);
                return Pair.of(uname, pword);
            }
        }
        return null;
    }
}
