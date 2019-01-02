package io.github.isuru.oasis.services.controllers;

import io.github.isuru.oasis.services.model.UserRankRecordDto;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class GameController {

    @GetMapping("/game/leaderboard/{id}/global")
    @ResponseBody
    public List<UserRankRecordDto> readGlobalLeaderboard(@PathVariable("id") int leaderboardId) {
        return null;
    }

    @GetMapping("/game/leaderboard/{id}/team/{teamId}")
    @ResponseBody
    public List<UserRankRecordDto> readTeamLeaderboard(@PathVariable("id") int leaderboardId,
                                                       @PathVariable("teamId") int teamId) {
        return null;
    }

    @GetMapping("/game/leaderboard/{id}/teamscope/{scopeId}")
    @ResponseBody
    public List<UserRankRecordDto> readTeamScopeLeaderboard(@PathVariable("id") int leaderboardId,
                                                            @PathVariable("scopeId") int scopeId) {
        return null;
    }



    @PostMapping("/game/shop/buy")
    @ResponseBody
    public void buyItem() {

    }

    @PostMapping("/game/shop/share")
    @ResponseBody
    public void shareItem() {

    }


    @PostMapping("/game/award/badge")
    @ResponseBody
    public void awardBadge() {

    }

    @PostMapping("/game/award/points")
    @ResponseBody
    public void awardPoints() {

    }
}
