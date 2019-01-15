package io.github.isuru.oasis.services.controllers;

import io.github.isuru.oasis.services.dto.StatusResponse;
import io.github.isuru.oasis.services.services.ILifecycleService;
import io.github.isuru.oasis.services.services.LifecycleImplManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/control")
public class ControlController {

    private ILifecycleService lifecycleService;

    @Autowired
    public ControlController(LifecycleImplManager lifecycleImplHolder) {
        this.lifecycleService = lifecycleImplHolder.get();
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/game/{gameId}/start")
    @ResponseBody
    public StatusResponse startGame(@PathVariable("gameId") long gameId) throws Exception {
        return new StatusResponse(lifecycleService.start(gameId));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/game/{gameId}/stop")
    @ResponseBody
    public StatusResponse stopGame(@PathVariable("gameId") long gameId) throws Exception {
        return new StatusResponse(lifecycleService.stop(gameId));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/challenge/{challengeId}/start")
    @ResponseBody
    public StatusResponse startChallenge(@PathVariable("challengeId") long challengeId) throws Exception {
        return new StatusResponse(lifecycleService.startChallenge(challengeId));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/challenge/{challengeId}/stop")
    @ResponseBody
    public StatusResponse stopChallenge(@PathVariable("challengeId") long challengeId) throws Exception {
        return new StatusResponse(lifecycleService.stop(challengeId));
    }
}
