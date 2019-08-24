/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.github.oasis.services.services.scheduler;

import io.github.oasis.model.DefaultEntities;
import io.github.oasis.model.collect.Pair;
import io.github.oasis.model.defs.LeaderboardDef;
import io.github.oasis.model.defs.RaceDef;
import io.github.oasis.model.defs.ScopingType;
import io.github.oasis.services.dto.game.GlobalLeaderboardRecordDto;
import io.github.oasis.services.dto.game.LeaderboardRequestDto;
import io.github.oasis.services.dto.game.TeamLeaderboardRecordDto;
import io.github.oasis.services.dto.stats.UserCountStat;
import io.github.oasis.services.model.RaceWinRecord;
import io.github.oasis.services.model.UserTeam;
import io.github.oasis.services.services.IGameDefService;
import io.github.oasis.services.services.IGameService;
import io.github.oasis.services.services.IProfileService;
import io.github.oasis.services.utils.Commons;
import org.mvel2.MVEL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class BaseScheduler {

    private static final Logger LOG = LoggerFactory.getLogger(BaseScheduler.class);

    protected abstract Pair<Long, Long> deriveTimeRange(long ms, ZoneId zoneId);

    protected abstract String filterTimeWindow();

    protected Map<Long, List<RaceWinRecord>> runForGame(IProfileService profileService,
                                                        IGameDefService gameDefService,
                                                        IGameService gameService,
                                                        long gameId, long awardedAt) {

        Map<Long, List<RaceWinRecord>> winnersByRace = new HashMap<>();

        try {
            Map<Long, Long> teamCountMap = loadTeamStatus(profileService, awardedAt);
            Map<Long, Long> teamScopeCountMap = loadTeamScopeStatus(profileService, awardedAt);

            List<RaceDef> raceDefList = readRaces(gameDefService, gameId, filterTimeWindow());
            LOG.info(" #{} race(s) found for game #{}", raceDefList.size(), gameId);
            for (RaceDef raceDef : raceDefList) {
                List<RaceWinRecord> winners = calcWinnersForRace(raceDef,
                        awardedAt,
                        gameId,
                        gameDefService,
                        gameService,
                        profileService,
                        teamCountMap,
                        teamScopeCountMap);

                winnersByRace.put(raceDef.getId(), winners);
            }

        } catch (Exception e) {
            LOG.error("Error while reading race definitions from database!", e);
        }
        return winnersByRace;
    }

    List<RaceWinRecord> calcWinnersForRace(RaceDef raceDef,
                                           long awardedAt,
                                           long gameId,
                                           IGameDefService gameDefService,
                                           IGameService gameService,
                                           IProfileService profileService,
                                           Map<Long, Long> teamCountMap,
                                           Map<Long, Long> teamScopeCountMap) throws Exception {

        LeaderboardDef lb = gameDefService.readLeaderboardDef(raceDef.getLeaderboardId());
        if (lb == null) {
            LOG.warn("No leaderboard is found by referenced id '{}' in race definition '{}'!",
                    raceDef.getLeaderboardId(), raceDef.getId());
            return new ArrayList<>();
        }

        ScopingType scopingType = ScopingType.from(raceDef.getFromScope());

        Pair<Long, Long> timeRange = deriveTimeRange(awardedAt, ZoneId.systemDefault());

        Serializable expr = !Commons.isNullOrEmpty(raceDef.getRankPointsExpression())
                ? MVEL.compileExpression(raceDef.getRankPointsExpression())
                : null;

        LeaderboardRequestDto requestDto = new LeaderboardRequestDto(timeRange.getValue0(), timeRange.getValue1());
        requestDto.setLeaderboardDef(lb);
        requestDto.setTopThreshold(raceDef.getTop());
        requestDto.setMinPointThreshold(raceDef.getMinPointThreshold());

        LOG.info(" - Executing leaderboard for race #{} between @[{}, {}]...",
                raceDef.getId(), timeRange.getValue0(), timeRange.getValue1());

        List<RaceWinRecord> winners;
        if (scopingType == ScopingType.TEAM || scopingType == ScopingType.TEAM_SCOPE) {
            List<TeamLeaderboardRecordDto> recordOrder = gameService.readTeamLeaderboard(requestDto);
            winners = deriveTeamWinners(recordOrder, scopingType, raceDef, teamCountMap, teamScopeCountMap);
        } else {
            List<GlobalLeaderboardRecordDto> recordOrder = gameService.readGlobalLeaderboard(requestDto);
            winners = deriveGlobalWinners(profileService, recordOrder, timeRange.getValue1());
        }

        // append other information to the record
        winners.forEach(record -> {
            record.setGameId(gameId);
            record.setRaceStartAt(timeRange.getValue0());
            record.setRaceEndAt(timeRange.getValue1());
            record.setRaceId(raceDef.getId());
            record.setAwardedAt(awardedAt);

            calculateAwardPoints(record, raceDef, expr);
        });

        // insert winners to database
        return winners;
    }

    private void calculateAwardPoints(RaceWinRecord winner, RaceDef raceDef, Serializable expr) {
        if (expr != null) {
            Map<String, Object> vars = new HashMap<>();
            vars.put("$rank", winner.getRank());
            vars.put("$points", winner.getPoints());
            vars.put("$count", winner.getTotalCount());
            vars.put("$winner", winner);

            double awardPoints = Commons.asDouble(MVEL.executeExpression(expr, vars));
            if (Double.isNaN(awardPoints)) {
                awardPoints = DefaultEntities.DEFAULT_RACE_WIN_VALUE;
            }
            winner.setAwardedPoints(awardPoints);

        } else {
            winner.setAwardedPoints(raceDef.getRankPoints().getOrDefault(winner.getRank(),
                    DefaultEntities.DEFAULT_RACE_WIN_VALUE));
        }
    }

    private List<RaceWinRecord> deriveGlobalWinners(IProfileService profileService,
                                                    List<GlobalLeaderboardRecordDto> recordOrder,
                                                    long atTime) throws Exception {
        List<RaceWinRecord> winners = new LinkedList<>();
        for (GlobalLeaderboardRecordDto row : recordOrder) {
            RaceWinRecord winnerRecord = new RaceWinRecord();

            long userId = row.getUserId();
            winnerRecord.setUserId(userId);
            winnerRecord.setPoints(row.getTotalPoints());
            winnerRecord.setTotalCount(row.getTotalCount());

            UserTeam currentTeamOfUser = profileService.findCurrentTeamOfUser(userId, true, atTime);
            winnerRecord.setTeamId(currentTeamOfUser.getTeamId());
            winnerRecord.setTeamScopeId(currentTeamOfUser.getScopeId());
            winnerRecord.setRank(row.getRankGlobal());
            winners.add(winnerRecord);
        }
        return winners;
    }

    private List<RaceWinRecord> deriveTeamWinners(List<TeamLeaderboardRecordDto> recordOrder,
                                                        ScopingType scopingType,
                                                        RaceDef raceDef,
                                                        Map<Long, Long> teamCountMap,
                                                        Map<Long, Long> teamScopeCountMap) {
        List<RaceWinRecord> winners = new LinkedList<>();
        for (TeamLeaderboardRecordDto row : recordOrder) {
            RaceWinRecord winnerRecord = new RaceWinRecord();

            long userId = row.getUserId();
            int rank;

            winnerRecord.setUserId(userId);
            winnerRecord.setPoints(row.getTotalPoints());
            winnerRecord.setTotalCount(row.getTotalCount());

            if (scopingType == ScopingType.TEAM_SCOPE) {
                long teamScopeId = row.getTeamScopeId();
                rank = row.getRankInTeamScope();

                long playerCount = teamScopeCountMap.get(teamScopeId);
                if (playerCount <= 1) {     // we don't award when there is only one player
                    LOG.warn(" * No player in team scope #{} will be awarded, " +
                            "because this team scope has only 1 player!", teamScopeId);
                    continue;
                } else if (playerCount < raceDef.getTop() && rank > 1) {
                    LOG.warn(" * Only the top player will be awarded points, " +
                            "because this team scope #{} has players less than top {}!",
                            teamScopeId, raceDef.getTop());
                    continue;
                }

                winnerRecord.setTeamId(row.getTeamId().intValue());
                winnerRecord.setTeamScopeId(row.getTeamScopeId().intValue());
                winnerRecord.setRank(rank);

            } else {
                Long teamId = row.getTeamId();
                rank = row.getRankInTeam();

                long playerCount = teamCountMap.get(teamId);
                if (playerCount <= 1) { // no awards. skip.
                    LOG.warn(" * No player in team #{} will be awarded, " +
                            "because this team has only 1 player!", teamId);
                    continue;
                } else if (playerCount < raceDef.getTop() && rank > 1) {
                    // only the top will be awarded points
                    LOG.warn(" * Only the top player will be awarded points, " +
                                    "because this team #{} has players less than top {}!",
                            teamId, raceDef.getTop());
                    continue;
                }

                winnerRecord.setTeamId(teamId.intValue());
                winnerRecord.setTeamScopeId(row.getTeamScopeId().intValue());
                winnerRecord.setRank(rank);
            }

            winners.add(winnerRecord);
        }
        return winners;
    }


    Map<Long, Long> loadTeamStatus(IProfileService profileService, long atTime) throws Exception {
        List<UserCountStat> teamList = profileService.listUserCountInTeams(atTime, false);
        Map<Long, Long> teamCounts = new HashMap<>();
        for (UserCountStat statusStat : teamList) {
            teamCounts.put(statusStat.getId(), statusStat.getTotalUsers());
        }
        return teamCounts;
    }

    Map<Long, Long> loadTeamScopeStatus(IProfileService profileService, long atTime) throws Exception {
        List<UserCountStat> teamList = profileService.listUserCountInTeamScopes(atTime, false);
        Map<Long, Long> teamScopeCounts = new HashMap<>();
        for (UserCountStat statusStat : teamList) {
            teamScopeCounts.put(statusStat.getId(), statusStat.getTotalUsers());
        }
        return teamScopeCounts;
    }

    private List<RaceDef> readRaces(IGameDefService gameDefService, long gameId, String timePeriod) throws Exception {
        return gameDefService.listRaces(gameId).stream()
                .filter(r -> timePeriod.equalsIgnoreCase(r.getTimeWindow()))
                .collect(Collectors.toList());
    }

}
