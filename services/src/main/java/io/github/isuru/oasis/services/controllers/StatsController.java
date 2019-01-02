package io.github.isuru.oasis.services.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class StatsController {

    @GetMapping("/stats/user/{uid}/summary")
    @ResponseBody
    public void getUserStatSummary() {

    }

    @GetMapping("/stats/user/{uid}/badges")
    @ResponseBody
    public void getUserBadgesStats() {

    }

    @GetMapping("/stats/user/{uid}/milestones")
    @ResponseBody
    public void getUserMilestoneStats() {

    }

    @GetMapping("/stats/user/{uid}/states")
    @ResponseBody
    public void getUserStatesStats() {

    }

    @GetMapping("/stats/user/{uid}/point-breakdown")
    @ResponseBody
    public void getUserPointBreakdown() {

    }

    @GetMapping("/stats/user/{uid}/badge-breakdown")
    @ResponseBody
    public void getUserBadgesBreakdown() {

    }

    @GetMapping("/stats/user/{uid}/team-history")
    @ResponseBody
    public void getUserTeamHistory() {

    }

    @GetMapping("/stats/user/{uid}/team-rankings")
    @ResponseBody
    public void getUserTeamRankings() {

    }

    @GetMapping("/stats/user/{uid}/rankings")
    @ResponseBody
    public void getUserRankingsStat() {

    }

    @GetMapping("/stats/challenge/{id}")
    @ResponseBody
    public void readChallengeStats() {

    }
}
