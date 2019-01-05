package io.github.isuru.oasis.services.controllers;

import io.github.isuru.oasis.model.defs.*;
import io.github.isuru.oasis.services.api.dto.AddGameDto;
import io.github.isuru.oasis.services.api.dto.HeroDto;
import io.github.isuru.oasis.services.dto.DefinitionAddResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class DefinitionController {

    @PostMapping("/def/game")
    @ResponseBody
    public DefinitionAddResponse addGame(@RequestBody AddGameDto game) {
        return new DefinitionAddResponse("game", 0);
    }

    @GetMapping("/def/game/all")
    @ResponseBody
    public List<GameDef> listAllGames() {
        return null;
    }

    @GetMapping("/def/game/{id}")
    @ResponseBody
    public List<HeroDto> readGame(@PathVariable("id") long gameId) {
        return null;
    }

    @DeleteMapping("/def/game/{id}")
    @ResponseBody
    public List<HeroDto> deleteGame(@PathVariable("id") long gameId) {
        return null;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //
    //  Definition Adding End Points
    //
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////


    @PostMapping("/def/game/{id}/kpi")
    @ResponseBody
    public DefinitionAddResponse addKpi(@RequestBody KpiDef kpiDef) {
        return new DefinitionAddResponse("kpi", 0);
    }

    @PostMapping("/def/game/{id}/point")
    @ResponseBody
    public DefinitionAddResponse addPoint(@RequestBody PointDef pointDef) {
        return new DefinitionAddResponse("point", 0);
    }

    @PostMapping("/def/game/{id}/badge")
    @ResponseBody
    public DefinitionAddResponse addBadge(@RequestBody BadgeDef badgeDef) {
        return new DefinitionAddResponse("badge", 0);
    }

    @PostMapping("/def/game/{id}/milestone")
    @ResponseBody
    public DefinitionAddResponse addMilestone(@RequestBody MilestoneDef milestoneDef) {
        return new DefinitionAddResponse("milestone", 0);
    }

    @PostMapping("/def/game/{id}/leaderboard")
    @ResponseBody
    public DefinitionAddResponse addLeaderboard(@RequestBody LeaderboardDef leaderboardDef) {
        return new DefinitionAddResponse("leaderboard", 0);
    }

    @PostMapping("/def/game/{id}/challenge")
    @ResponseBody
    public DefinitionAddResponse addChallenge(@RequestBody ChallengeDef challengeDef) {
        return new DefinitionAddResponse("challenge", 0);
    }

    @PostMapping("/def/game/{id}/state")
    @ResponseBody
    public DefinitionAddResponse addState(@RequestBody StateDef stateDef) {
        return new DefinitionAddResponse("state", 0);
    }

}
