package io.github.isuru.oasis.services.controllers;

import io.github.isuru.oasis.services.dto.StatusResponse;
import io.github.isuru.oasis.services.services.ILifecycleService;
import io.github.isuru.oasis.services.services.LifecycleImplManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/control")
public class ControlController {

    private ILifecycleService lifecycleService;

    @Autowired
    public ControlController(LifecycleImplManager lifecycleImplHolder) {
        this.lifecycleService = lifecycleImplHolder.get();
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/game/{gameId}/start")
    public StatusResponse startGame(@PathVariable("gameId") long gameId) throws Exception {
        return new StatusResponse(lifecycleService.start(gameId));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/game/{gameId}/stop")
    public StatusResponse stopGame(@PathVariable("gameId") long gameId) throws Exception {
        return new StatusResponse(lifecycleService.stop(gameId));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/challenge/{challengeId}/start")
    public StatusResponse startChallenge(@PathVariable("challengeId") long challengeId) throws Exception {
        return new StatusResponse(lifecycleService.startChallenge(challengeId));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/challenge/{challengeId}/stop")
    public StatusResponse stopChallenge(@PathVariable("challengeId") long challengeId) throws Exception {
        return new StatusResponse(lifecycleService.stop(challengeId));
    }
}
