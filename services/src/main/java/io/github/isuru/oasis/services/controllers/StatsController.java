package io.github.isuru.oasis.services.controllers;

import io.github.isuru.oasis.model.defs.GameDef;
import io.github.isuru.oasis.model.defs.LeaderboardType;
import io.github.isuru.oasis.services.dto.game.UserRankRecordDto;
import io.github.isuru.oasis.services.dto.game.UserRankingsInRangeDto;
import io.github.isuru.oasis.services.dto.stats.*;
import io.github.isuru.oasis.services.model.UserTeam;
import io.github.isuru.oasis.model.defs.ScopingType;
import io.github.isuru.oasis.services.services.IGameDefService;
import io.github.isuru.oasis.services.services.IProfileService;
import io.github.isuru.oasis.services.services.IStatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
public class StatsController {

    @Autowired
    private IStatService statService;

    @Autowired
    private IProfileService profileService;

    @Autowired
    private IGameDefService gameDefService;


    @GetMapping("/stats/user/{userId}/summary")
    public UserStatDto getUserStatSummary(@PathVariable("userId") long userId,
                                          @RequestParam(value = "since", defaultValue = "0") long since) throws Exception {
        return statService.readUserGameStats(userId, since);
    }

    @PostMapping("/stats/user/{userId}/badges")
    public List<UserBadgeStatDto> getUserBadgesStats(@PathVariable("userId") long userId,
                                                     @RequestBody UserBadgeStatReq req) throws Exception {
        return null;
    }

    @GetMapping("/stats/user/{userId}/milestones")
    public List<UserMilestoneStatDto> getUserMilestoneStats(@PathVariable("userId") long userId) throws Exception {
        return statService.readUserMilestones(userId);
    }

    @GetMapping("/stats/user/{userId}/states")
    public List<UserStateStatDto> getUserStatesStats(@PathVariable("userId") long userId) throws Exception {
        UserTeam currentTeamOfUser = profileService.findCurrentTeamOfUser(userId);
        if (currentTeamOfUser == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current team of user '" + userId + "' cannot be found!");
        }
        long teamId = currentTeamOfUser.getTeamId();
        return statService.readUserStateStats(userId, teamId);
    }

    @GetMapping("/stats/user/{userId}/point-breakdown")
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
    public List<TeamHistoryRecordDto> getUserTeamHistory(@PathVariable("userId") long userId) throws Exception {
        return statService.readUserTeamHistoryStat(userId);
    }

    @GetMapping("/stats/user/{userId}/team-rankings")
    public UserRankingsInRangeDto getUserTeamRankings(@PathVariable("userId") long userId) throws Exception {
        return statService.readUserTeamRankings(userId);
    }

    @GetMapping("/stats/user/{userId}/rankings")
    public List<UserRankRecordDto> getUserRankingsStat(@PathVariable("userId") long userId,
                                                       @RequestParam(value = "gameId", defaultValue = "-1") long gameId,
                                                       @RequestParam(value = "scope", defaultValue = "") String scope,
                                                       @RequestParam(value = "period", defaultValue = "") String period) throws Exception {
        long gId = gameId;
        if (gId <= 0) {
            List<GameDef> gameDefs = gameDefService.listGames();
            if (gameDefs == null || gameDefs.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "There are no games defined in Oasis yet!");
            }
            gId = gameDefs.get(0).getId();
        }
        ScopingType scopingType = ScopingType.from(scope);
        LeaderboardType leaderboardType = LeaderboardType.from(period);
        return statService.readMyLeaderboardRankings(gId, userId, scopingType, leaderboardType);
    }

    @GetMapping("/stats/challenge/{challengeId}")
    public ChallengeInfoDto readChallengeStats(@PathVariable("challengeId") long challengeId) throws Exception {
        return statService.readChallengeStats(challengeId);
    }
}
