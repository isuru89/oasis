package io.github.isuru.oasis.services.controllers;

import io.github.isuru.oasis.services.dto.AuthResponse;
import io.github.isuru.oasis.services.security.TokenSecurityFilter;
import io.github.isuru.oasis.services.utils.AuthUtils;
import io.github.isuru.oasis.services.utils.Maps;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;

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
    public AuthResponse login(@RequestHeader(TokenSecurityFilter.AUTHORIZATION) String authHeader) {

        AuthResponse authResponse = new AuthResponse();
        authResponse.setSuccess(true);
        return authResponse;
    }

}
