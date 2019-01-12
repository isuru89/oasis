package io.github.isuru.oasis.services.controllers;

import io.github.isuru.oasis.model.defs.GameDef;
import io.github.isuru.oasis.model.defs.LeaderboardType;
import io.github.isuru.oasis.services.api.dto.*;
import io.github.isuru.oasis.services.exception.InputValidationException;
import io.github.isuru.oasis.services.model.UserRankRecordDto;
import io.github.isuru.oasis.services.model.UserTeam;
import io.github.isuru.oasis.services.model.enums.ScopingType;
import io.github.isuru.oasis.services.services.IGameDefService;
import io.github.isuru.oasis.services.services.IProfileService;
import io.github.isuru.oasis.services.services.IStatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class StatsController {

    @Autowired
    private IStatService statService;

    @Autowired
    private IProfileService profileService;

    @Autowired
    private IGameDefService gameDefService;


    @GetMapping("/stats/user/{userId}/summary")
    @ResponseBody
    public UserStatDto getUserStatSummary(@PathVariable("userId") long userId,
                                          @RequestParam(value = "since", defaultValue = "0") long since) throws Exception {
        return statService.readUserGameStats(userId, since);
    }

    @GetMapping("/stats/user/{userId}/badges")
    @ResponseBody
    public List<UserBadgeStatDto> getUserBadgesStats(@PathVariable("userId") long userId,
                                                     @RequestParam(value = "since", defaultValue = "0") long since) throws Exception {
        return statService.readUserBadges(userId, since);
    }

    @GetMapping("/stats/user/{userId}/milestones")
    @ResponseBody
    public List<UserMilestoneStatDto> getUserMilestoneStats(@PathVariable("userId") long userId) throws Exception {
        return statService.readUserMilestones(userId);
    }

    @GetMapping("/stats/user/{userId}/states")
    @ResponseBody
    public List<UserStateStatDto> getUserStatesStats(@PathVariable("userId") long userId) throws Exception {
        UserTeam currentTeamOfUser = profileService.findCurrentTeamOfUser(userId);
        if (currentTeamOfUser == null) {
            throw new InputValidationException("Current team of user '" + userId + "' cannot be found!");
        }
        long teamId = currentTeamOfUser.getTeamId();
        return statService.readUserStateStats(userId, teamId);
    }

    @GetMapping("/stats/user/{userId}/point-breakdown")
    @ResponseBody
    public PointBreakdownResDto getUserPointBreakdown(@RequestParam(value = "userId") long userId,
                                      @RequestParam(value = "pointId") int pointId,
                                      @RequestParam(value = "offset", defaultValue = "0") int offset,
                                      @RequestParam(value = "size", defaultValue = "0") int size,
                                      @RequestParam(value = "start", defaultValue = "0") long start,
                                      @RequestParam(value = "end", defaultValue = "0") long end) throws Exception {
        PointBreakdownReqDto reqDto = new PointBreakdownReqDto();
        reqDto.setUserId(userId);
        reqDto.setPointId(pointId);
        reqDto.setOffset(offset);
        reqDto.setSize(size);
        reqDto.setRangeStart(start);
        reqDto.setRangeEnd(end);

        return statService.getPointBreakdownList(reqDto);
    }

    @GetMapping("/stats/user/{userId}/badge-breakdown")
    @ResponseBody
    public BadgeBreakdownResDto getUserBadgesBreakdown(@RequestParam(value = "userId") long userId,
                                                       @RequestParam(value = "badgeId") int badgeId,
                                                       @RequestParam(value = "offset", defaultValue = "0") int offset,
                                                       @RequestParam(value = "size", defaultValue = "0") int size,
                                                       @RequestParam(value = "start", defaultValue = "0") long start,
                                                       @RequestParam(value = "end", defaultValue = "0") long end) throws Exception {
        BadgeBreakdownReqDto reqDto = new BadgeBreakdownReqDto();
        reqDto.setUserId(userId);
        reqDto.setBadgeId(badgeId);
        reqDto.setOffset(offset);
        reqDto.setSize(size);
        reqDto.setRangeStart(start);
        reqDto.setRangeEnd(end);

        return statService.getBadgeBreakdownList(reqDto);
    }

    @GetMapping("/stats/user/{userId}/team-history")
    @ResponseBody
    public List<TeamHistoryRecordDto> getUserTeamHistory(@PathVariable("userId") long userId) throws Exception {
        return statService.readUserTeamHistoryStat(userId);
    }

    @GetMapping("/stats/user/{userId}/team-rankings")
    @ResponseBody
    public List<UserRankRecordDto> getUserTeamRankings(@PathVariable("userId") long userId,
                                                       @RequestParam(value = "current", defaultValue = "true") boolean current) throws Exception {
        return statService.readUserTeamRankings(userId, current);
    }

    @GetMapping("/stats/user/{userId}/rankings")
    @ResponseBody
    public List<UserRankRecordDto> getUserRankingsStat(@PathVariable("userId") long userId,
                                                       @RequestParam(value = "gameId", defaultValue = "-1") long gameId,
                                                       @RequestParam(value = "scope", defaultValue = "") String scope,
                                                       @RequestParam(value = "period", defaultValue = "") String period) throws Exception {
        long gId = gameId;
        if (gId <= 0) {
            List<GameDef> gameDefs = gameDefService.listGames();
            if (gameDefs == null || gameDefs.isEmpty()) {
                throw new IllegalStateException("There are no games defined in Oasis yet!");
            }
            gId = gameDefs.get(0).getId();
        }
        ScopingType scopingType = ScopingType.from(scope);
        LeaderboardType leaderboardType = LeaderboardType.from(period);
        return statService.readMyLeaderboardRankings(gId, userId, scopingType, leaderboardType);
    }

    @GetMapping("/stats/challenge/{challengeId}")
    @ResponseBody
    public ChallengeInfoDto readChallengeStats(@PathVariable("challengeId") long challengeId) throws Exception {
        return statService.readChallengeStats(challengeId);
    }
}
