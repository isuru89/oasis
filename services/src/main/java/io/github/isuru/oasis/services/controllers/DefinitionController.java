package io.github.isuru.oasis.services.controllers;

import io.github.isuru.oasis.model.defs.BadgeDef;
import io.github.isuru.oasis.model.defs.ChallengeDef;
import io.github.isuru.oasis.model.defs.GameDef;
import io.github.isuru.oasis.model.defs.KpiDef;
import io.github.isuru.oasis.model.defs.LeaderboardDef;
import io.github.isuru.oasis.model.defs.MilestoneDef;
import io.github.isuru.oasis.model.defs.PointDef;
import io.github.isuru.oasis.model.defs.StateDef;
import io.github.isuru.oasis.services.dto.DefinitionAddResponse;
import io.github.isuru.oasis.services.dto.DeleteResponse;
import io.github.isuru.oasis.services.dto.defs.AddGameDto;
import io.github.isuru.oasis.services.services.IGameDefService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping("/def")
public class DefinitionController {

    @Autowired
    private IGameDefService gameDefService;

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/game")
    @ResponseBody
    public DefinitionAddResponse addGame(@RequestBody AddGameDto game) throws Exception {
        return new DefinitionAddResponse("game", gameDefService.createGame(game.getDef(), game.getOptions()));
    }

    @GetMapping("/game/all")
    @ResponseBody
    public List<GameDef> listAllGames() throws Exception {
        return gameDefService.listGames();
    }

    @GetMapping("/game/{id}")
    @ResponseBody
    public GameDef readGame(@PathVariable("id") long gameId) throws Exception {
        return gameDefService.readGame(gameId);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/game/{id}")
    @ResponseBody
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
    @ResponseBody
    public DefinitionAddResponse addKpi(@PathVariable("id") long gameId, @RequestBody KpiDef kpiDef) throws Exception {
        return new DefinitionAddResponse("kpi", gameDefService.addKpiCalculation(gameId, kpiDef));
    }

    @PostMapping("/game/{id}/point")
    @ResponseBody
    public DefinitionAddResponse addPoint(@PathVariable("id") long gameId, @RequestBody PointDef pointDef) throws Exception {
        return new DefinitionAddResponse("point", gameDefService.addPointDef(gameId, pointDef));
    }

    @PostMapping("/game/{id}/badge")
    @ResponseBody
    public DefinitionAddResponse addBadge(@PathVariable("id") long gameId, @RequestBody BadgeDef badgeDef) throws Exception {
        return new DefinitionAddResponse("badge", gameDefService.addBadgeDef(gameId, badgeDef));
    }

    @PostMapping("/game/{id}/milestone")
    @ResponseBody
    public DefinitionAddResponse addMilestone(@PathVariable("id") long gameId, @RequestBody MilestoneDef milestoneDef) throws Exception {
        return new DefinitionAddResponse("milestone", gameDefService.addMilestoneDef(gameId, milestoneDef));
    }

    @PostMapping("/game/{id}/leaderboard")
    @ResponseBody
    public DefinitionAddResponse addLeaderboard(@PathVariable("id") long gameId, @RequestBody LeaderboardDef leaderboardDef) throws Exception {
        return new DefinitionAddResponse("leaderboard", gameDefService.addLeaderboardDef(gameId, leaderboardDef));
    }

    @PostMapping("/game/{id}/challenge")
    @ResponseBody
    public DefinitionAddResponse addChallenge(@PathVariable("id") long gameId, @RequestBody ChallengeDef challengeDef) throws Exception {
        return new DefinitionAddResponse("challenge", gameDefService.addChallenge(gameId, challengeDef));
    }

    @PostMapping("/game/{id}/state")
    @ResponseBody
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
    @ResponseBody
    public List<KpiDef> listKpis(@PathVariable("id") long gameId) throws Exception {
        return gameDefService.listKpiCalculations(gameId);
    }

    @GetMapping("/game/{id}/point/all")
    @ResponseBody
    public List<PointDef> listPointDefs(@PathVariable("id") long gameId) throws Exception {
        return gameDefService.listPointDefs(gameId);
    }

    @GetMapping("/game/{id}/badge/all")
    @ResponseBody
    public List<BadgeDef> listBadgeDefs(@PathVariable("id") long gameId) throws Exception {
        return gameDefService.listBadgeDefs(gameId);
    }

    @GetMapping("/game/{id}/milestone/all")
    @ResponseBody
    public List<MilestoneDef> listMilestoneDefs(@PathVariable("id") long gameId) throws Exception {
        return gameDefService.listMilestoneDefs(gameId);
    }

    @GetMapping("/game/{id}/leaderboard/all")
    @ResponseBody
    public List<LeaderboardDef> listLeaderboardDefs(@PathVariable("id") long gameId) throws Exception {
        return gameDefService.listLeaderboardDefs(gameId);
    }

    @GetMapping("/game/{id}/challenge/all")
    @ResponseBody
    public List<ChallengeDef> listChallengeDefs(@PathVariable("id") long gameId) throws Exception {
        return gameDefService.listChallenges(gameId);
    }

    @GetMapping("/game/{id}/state/all")
    @ResponseBody
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
    @ResponseBody
    public PointDef readPointDef(@PathVariable("pointId") long pointId) throws Exception {
        return gameDefService.readPointDef(pointId);
    }

    @GetMapping("/game/{id}/kpi/{kpiId}")
    @ResponseBody
    public KpiDef readKpiDef(@PathVariable("kpiId") long kpiId) throws Exception {
        return gameDefService.readKpiCalculation(kpiId);
    }

    @GetMapping("/game/{id}/badge/{badgeId}")
    @ResponseBody
    public BadgeDef readBadgeDef(@PathVariable("badgeId") long badgeId) throws Exception {
        return gameDefService.readBadgeDef(badgeId);
    }

    @GetMapping("/game/{id}/milestone/{milestoneId}")
    @ResponseBody
    public MilestoneDef readMilestoneDef(@PathVariable("milestoneId") long milestoneId) throws Exception {
        return gameDefService.readMilestoneDef(milestoneId);
    }

    @GetMapping("/game/{id}/leaderboard/{leaderboardId}")
    @ResponseBody
    public LeaderboardDef readLeaderboardDef(@PathVariable("leaderboardId") long leaderboardId) throws Exception {
        return gameDefService.readLeaderboardDef(leaderboardId);
    }

    @GetMapping("/game/{id}/challenge/{challengeId}")
    @ResponseBody
    public ChallengeDef readChallengeDef(@PathVariable("challengeId") long challengeId) throws Exception {
        return gameDefService.readChallenge(challengeId);
    }

    @GetMapping("/game/{id}/state/{stateId}")
    @ResponseBody
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
    @ResponseBody
    public DeleteResponse deletePointDef(@PathVariable("pointId") long pointId) throws Exception {
        return new DeleteResponse("point", gameDefService.disablePointDef(pointId));
    }

    @DeleteMapping("/game/{id}/kpi/{kpiId}")
    @ResponseBody
    public DeleteResponse deleteKpiDef(@PathVariable("kpiId") long kpiId) throws Exception {
        return new DeleteResponse("kpi", gameDefService.disableKpiCalculation(kpiId));
    }

    @DeleteMapping("/game/{id}/badge/{badgeId}")
    @ResponseBody
    public DeleteResponse deleteBadgeDef(@PathVariable("badgeId") long badgeId) throws Exception {
        return new DeleteResponse("badge", gameDefService.disableBadgeDef(badgeId));
    }

    @DeleteMapping("/game/{id}/milestone/{milestoneId}")
    @ResponseBody
    public DeleteResponse deleteMilestoneDef(@PathVariable("milestoneId") long milestoneId) throws Exception {
        return new DeleteResponse("milestone", gameDefService.disableMilestoneDef(milestoneId));
    }

    @DeleteMapping("/game/{id}/leaderboard/{leaderboardId}")
    @ResponseBody
    public DeleteResponse deleteLeaderboardDef(@PathVariable("leaderboardId") long leaderboardId) throws Exception {
        return new DeleteResponse("leaderboard", gameDefService.disableLeaderboardDef(leaderboardId));
    }

    @DeleteMapping("/game/{id}/challenge/{challengeId}")
    @ResponseBody
    public DeleteResponse deleteChallengeDef(@PathVariable("challengeId") long challengeId) throws Exception {
        return new DeleteResponse("challenge", gameDefService.disableChallenge(challengeId));
    }

    @DeleteMapping("/game/{id}/state/{stateId}")
    @ResponseBody
    public DeleteResponse deleteStateDef(@PathVariable("stateId") long stateId) throws Exception {
        return new DeleteResponse("state", gameDefService.disableStatePlay(stateId));
    }
}
