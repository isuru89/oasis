package io.github.isuru.oasis.services.controllers;

import io.github.isuru.oasis.model.defs.*;
import io.github.isuru.oasis.services.dto.DefinitionAddResponse;
import io.github.isuru.oasis.services.dto.DeleteResponse;
import io.github.isuru.oasis.services.dto.defs.AddGameDto;
import io.github.isuru.oasis.services.services.IGameDefService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/def")
public class DefinitionController {

    @Autowired
    private IGameDefService gameDefService;

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/game")
    public DefinitionAddResponse addGame(@RequestBody AddGameDto game) throws Exception {
        return new DefinitionAddResponse("game", gameDefService.createGame(game.getDef(), game.getOptions()));
    }

    @GetMapping("/game/all")
    public List<GameDef> listAllGames() throws Exception {
        return gameDefService.listGames();
    }

    @GetMapping("/game/{id}")
    public GameDef readGame(@PathVariable("id") long gameId) throws Exception {
        return gameDefService.readGame(gameId);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/game/{id}")
    public DeleteResponse deleteGame(@PathVariable("id") long gameId) throws Exception {
        return new DeleteResponse("game", gameDefService.disableGame(gameId));
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //
    //  Definition Adding End Points
    //
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////


    @PostMapping("/game/{id}/kpi")
    public DefinitionAddResponse addKpi(@PathVariable("id") long gameId, @RequestBody KpiDef kpiDef) throws Exception {
        return new DefinitionAddResponse("kpi", gameDefService.addKpiCalculation(gameId, kpiDef));
    }

    @PostMapping("/game/{id}/point")
    public DefinitionAddResponse addPoint(@PathVariable("id") long gameId, @RequestBody PointDef pointDef) throws Exception {
        return new DefinitionAddResponse("point", gameDefService.addPointDef(gameId, pointDef));
    }

    @PostMapping("/game/{id}/badge")
    public DefinitionAddResponse addBadge(@PathVariable("id") long gameId, @RequestBody BadgeDef badgeDef) throws Exception {
        return new DefinitionAddResponse("badge", gameDefService.addBadgeDef(gameId, badgeDef));
    }

    @PostMapping("/game/{id}/milestone")
    public DefinitionAddResponse addMilestone(@PathVariable("id") long gameId, @RequestBody MilestoneDef milestoneDef) throws Exception {
        return new DefinitionAddResponse("milestone", gameDefService.addMilestoneDef(gameId, milestoneDef));
    }

    @PostMapping("/game/{id}/leaderboard")
    public DefinitionAddResponse addLeaderboard(@PathVariable("id") long gameId, @RequestBody LeaderboardDef leaderboardDef) throws Exception {
        return new DefinitionAddResponse("leaderboard", gameDefService.addLeaderboardDef(gameId, leaderboardDef));
    }

    @PostMapping("/game/{id}/challenge")
    public DefinitionAddResponse addChallenge(@PathVariable("id") long gameId, @RequestBody ChallengeDef challengeDef) throws Exception {
        return new DefinitionAddResponse("challenge", gameDefService.addChallenge(gameId, challengeDef));
    }

    @PostMapping("/game/{id}/state")
    public DefinitionAddResponse addState(@PathVariable("id") long gameId, @RequestBody StateDef stateDef) throws Exception {
        return new DefinitionAddResponse("state", gameDefService.addStatePlay(gameId, stateDef));
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //
    //  Definition Browse End Points
    //
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////


    @GetMapping("/game/{id}/kpi/all")
    public List<KpiDef> listKpis(@PathVariable("id") long gameId) throws Exception {
        return gameDefService.listKpiCalculations(gameId);
    }

    @GetMapping("/game/{id}/point/all")
    public List<PointDef> listPointDefs(@PathVariable("id") long gameId) throws Exception {
        return gameDefService.listPointDefs(gameId);
    }

    @GetMapping("/game/{id}/badge/all")
    public List<BadgeDef> listBadgeDefs(@PathVariable("id") long gameId) throws Exception {
        return gameDefService.listBadgeDefs(gameId);
    }

    @GetMapping("/game/{id}/milestone/all")
    public List<MilestoneDef> listMilestoneDefs(@PathVariable("id") long gameId) throws Exception {
        return gameDefService.listMilestoneDefs(gameId);
    }

    @GetMapping("/game/{id}/leaderboard/all")
    public List<LeaderboardDef> listLeaderboardDefs(@PathVariable("id") long gameId) throws Exception {
        return gameDefService.listLeaderboardDefs(gameId);
    }

    @GetMapping("/game/{id}/challenge/all")
    public List<ChallengeDef> listChallengeDefs(@PathVariable("id") long gameId) throws Exception {
        return gameDefService.listChallenges(gameId);
    }

    @GetMapping("/game/{id}/state/all")
    public List<StateDef> listStateDefs(@PathVariable("id") long gameId) throws Exception {
        return gameDefService.listStatePlays(gameId);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //
    //  Definition Reading End Points
    //
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////



    @GetMapping("/game/{id}/point/{pointId}")
    public PointDef readPointDef(@PathVariable("pointId") long pointId) throws Exception {
        return gameDefService.readPointDef(pointId);
    }

    @GetMapping("/game/{id}/kpi/{kpiId}")
    public KpiDef readKpiDef(@PathVariable("kpiId") long kpiId) throws Exception {
        return gameDefService.readKpiCalculation(kpiId);
    }

    @GetMapping("/game/{id}/badge/{badgeId}")
    public BadgeDef readBadgeDef(@PathVariable("badgeId") long badgeId) throws Exception {
        return gameDefService.readBadgeDef(badgeId);
    }

    @GetMapping("/game/{id}/milestone/{milestoneId}")
    public MilestoneDef readMilestoneDef(@PathVariable("milestoneId") long milestoneId) throws Exception {
        return gameDefService.readMilestoneDef(milestoneId);
    }

    @GetMapping("/game/{id}/leaderboard/{leaderboardId}")
    public LeaderboardDef readLeaderboardDef(@PathVariable("leaderboardId") long leaderboardId) throws Exception {
        return gameDefService.readLeaderboardDef(leaderboardId);
    }

    @GetMapping("/game/{id}/challenge/{challengeId}")
    public ChallengeDef readChallengeDef(@PathVariable("challengeId") long challengeId) throws Exception {
        return gameDefService.readChallenge(challengeId);
    }

    @GetMapping("/game/{id}/state/{stateId}")
    public StateDef readStateDef(@PathVariable("stateId") long stateId) throws Exception {
        return gameDefService.readStatePlay(stateId);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //
    //  Definition Deleting End Points
    //
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////



    @DeleteMapping("/game/{id}/point/{pointId}")
    public DeleteResponse deletePointDef(@PathVariable("pointId") long pointId) throws Exception {
        return new DeleteResponse("point", gameDefService.disablePointDef(pointId));
    }

    @DeleteMapping("/game/{id}/kpi/{kpiId}")
    public DeleteResponse deleteKpiDef(@PathVariable("kpiId") long kpiId) throws Exception {
        return new DeleteResponse("kpi", gameDefService.disableKpiCalculation(kpiId));
    }

    @DeleteMapping("/game/{id}/badge/{badgeId}")
    public DeleteResponse deleteBadgeDef(@PathVariable("badgeId") long badgeId) throws Exception {
        return new DeleteResponse("badge", gameDefService.disableBadgeDef(badgeId));
    }

    @DeleteMapping("/game/{id}/milestone/{milestoneId}")
    public DeleteResponse deleteMilestoneDef(@PathVariable("milestoneId") long milestoneId) throws Exception {
        return new DeleteResponse("milestone", gameDefService.disableMilestoneDef(milestoneId));
    }

    @DeleteMapping("/game/{id}/leaderboard/{leaderboardId}")
    public DeleteResponse deleteLeaderboardDef(@PathVariable("leaderboardId") long leaderboardId) throws Exception {
        return new DeleteResponse("leaderboard", gameDefService.disableLeaderboardDef(leaderboardId));
    }

    @DeleteMapping("/game/{id}/challenge/{challengeId}")
    public DeleteResponse deleteChallengeDef(@PathVariable("challengeId") long challengeId) throws Exception {
        return new DeleteResponse("challenge", gameDefService.disableChallenge(challengeId));
    }

    @DeleteMapping("/game/{id}/state/{stateId}")
    public DeleteResponse deleteStateDef(@PathVariable("stateId") long stateId) throws Exception {
        return new DeleteResponse("state", gameDefService.disableStatePlay(stateId));
    }
}
