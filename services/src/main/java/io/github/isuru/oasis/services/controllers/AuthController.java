package io.github.isuru.oasis.services.controllers;

import io.github.isuru.oasis.services.configs.OasisConfigurations;
import io.github.isuru.oasis.services.utils.AuthUtils;
import io.github.isuru.oasis.services.utils.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
public class AuthController {

    @Autowired
    private OasisConfigurations oasisConfigurations;

    @GetMapping("/auth/logout")
    @ResponseBody
    public Map<String, Object> logout(@RequestAttribute("token") AuthUtils.TokenInfo token) {
        System.out.println(token.getUser());
        System.out.println(token.getTeamId());
    }


}
