package io.github.isuru.oasis.services.controllers;

import io.github.isuru.oasis.model.defs.LeaderboardType;
import io.github.isuru.oasis.services.dto.game.BadgeAwardDto;
import io.github.isuru.oasis.services.dto.game.LeaderboardRequestDto;
import io.github.isuru.oasis.services.dto.game.PointAwardDto;
import io.github.isuru.oasis.services.dto.game.UserRankRecordDto;
import io.github.isuru.oasis.services.services.IGameDefService;
import io.github.isuru.oasis.services.services.IGameService;
import io.github.isuru.oasis.services.model.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@SuppressWarnings("unused")
@Controller
@RequestMapping("/game")
public class GameController {

    @Autowired
    private IGameService gameService;

    @Autowired
    private IGameDefService gameDefService;


    @GetMapping("/leaderboard/{id}/global")
    @ResponseBody
    public List<UserRankRecordDto> readGlobalLeaderboard(@PathVariable("id") int leaderboardId,
                                                         @RequestParam(value = "range", defaultValue = "weekly") String range,
                                                         @RequestParam(value = "top", defaultValue = "50") int top,
                                                         @RequestParam(value = "bottom", defaultValue = "0") int bottom,
                                                         @RequestParam(value = "when", defaultValue = "0") long when,
                                                         @RequestParam(value = "start", defaultValue = "-1") long start,
                                                         @RequestParam(value = "end", defaultValue = "-1") long end,
                                                         @RequestParam(value = "user", defaultValue = "0") int userId) throws Exception {

        LeaderboardRequestDto dto = generate(toType(range), top, bottom, when, start, end, userId);
        dto.setLeaderboardDef(gameDefService.readLeaderboardDef(leaderboardId));
        return gameService.readGlobalLeaderboard(dto);
    }

    @GetMapping("/leaderboard/{id}/team/{teamId}")
    @ResponseBody
    public List<UserRankRecordDto> readTeamLeaderboard(@PathVariable("id") int leaderboardId,
                                                       @PathVariable("teamId") int teamId,
                                                       @RequestParam(value = "range", defaultValue = "weekly") String range,
                                                       @RequestParam(value = "top", defaultValue = "50") int top,
                                                       @RequestParam(value = "bottom", defaultValue = "0") int bottom,
                                                       @RequestParam(value = "when", defaultValue = "0") long when,
                                                       @RequestParam(value = "start", defaultValue = "-1") long start,
                                                       @RequestParam(value = "end", defaultValue = "-1") long end,
                                                       @RequestParam(value = "user", defaultValue = "0") int userId) throws Exception {

        LeaderboardRequestDto dto = generate(toType(range), top, bottom, when, start, end, userId);
        dto.setLeaderboardDef(gameDefService.readLeaderboardDef(leaderboardId));
        return gameService.readTeamLeaderboard(teamId, dto);
    }

    @GetMapping("/leaderboard/{id}/teamscope/{scopeId}")
    @ResponseBody
    public List<UserRankRecordDto> readTeamScopeLeaderboard(@PathVariable("id") int leaderboardId,
                                                            @PathVariable("scopeId") int scopeId,
                                                            @RequestParam(value = "range", defaultValue = "weekly") String range,
                                                            @RequestParam(value = "top", defaultValue = "50") int top,
                                                            @RequestParam(value = "bottom", defaultValue = "0") int bottom,
                                                            @RequestParam(value = "when", defaultValue = "0") long when,
                                                            @RequestParam(value = "start", defaultValue = "-1") long start,
                                                            @RequestParam(value = "end", defaultValue = "-1") long end,
                                                            @RequestParam(value = "user", defaultValue = "0") int userId) throws Exception {

        LeaderboardRequestDto dto = generate(toType(range), top, bottom, when, start, end, userId);
        dto.setLeaderboardDef(gameDefService.readLeaderboardDef(leaderboardId));
        return gameService.readTeamScopeLeaderboard(scopeId, dto);
    }

    @Secured({ UserRole.ROLE_ADMIN, UserRole.ROLE_CURATOR })
    @PostMapping("/award/badge")
    @ResponseBody
    public void awardBadge(@RequestBody BadgeAwardDto awardDto,
                           @RequestAttribute("userId") long userId) throws Exception {
        gameService.awardBadge(userId, awardDto);
    }

    @Secured({ UserRole.ROLE_ADMIN, UserRole.ROLE_CURATOR })
    @PostMapping("/award/points")
    @ResponseBody
    public void awardPoints(@RequestBody PointAwardDto awardDto,
                            @RequestAttribute("userId") long userId) throws Exception {
        gameService.awardPoints(userId, awardDto);
    }



    private LeaderboardRequestDto generate(LeaderboardType type,
                                           int top, int bottom, long when,
                                           long start, long end,
                                           long userId) {
        if (top > 0 && bottom > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Leaderboard request cannot have " +
                    "both 'top' and 'bottom' parameters!");
        }

        LeaderboardRequestDto requestDto = null;
        if (when > 0) {
            if (type == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Leaderboard type must be defined 'when' is specified!");
            }
            requestDto = new LeaderboardRequestDto(type, when);

        } else if (start > 0 && end > 0) {
            requestDto = new LeaderboardRequestDto(start, end);
            requestDto.setType(LeaderboardType.CUSTOM);

        } else if (!type.isCustom()) {
            requestDto = new LeaderboardRequestDto(type, System.currentTimeMillis());
        }

        if (requestDto == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Custom leaderboards must specify time range!");
        }

        requestDto.setForUser(userId > 0 ? userId : null);
        requestDto.setTopN(top > 0 ? top : null);
        requestDto.setBottomN(bottom > 0 ? bottom : null);
        return requestDto;
    }

    private static LeaderboardType toType(String rangeType) {
        if (rangeType.startsWith("week")) {
            return LeaderboardType.CURRENT_WEEK;
        } else if (rangeType.startsWith("month")) {
            return LeaderboardType.CURRENT_MONTH;
        } else if (rangeType.startsWith("da")) {
            return LeaderboardType.CURRENT_DAY;
        } else if (rangeType.startsWith("custom")) {
            return LeaderboardType.CUSTOM;
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown range query parameter value! [" + rangeType + "]");
        }
    }
}
