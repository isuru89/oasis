package io.github.isuru.oasis.injector.scheduler;

import io.github.isuru.oasis.injector.BaseConsumer;
import io.github.isuru.oasis.model.collect.Pair;
import io.github.isuru.oasis.model.db.IOasisDao;
import io.github.isuru.oasis.model.defs.DefWrapper;
import io.github.isuru.oasis.model.defs.LeaderboardDef;
import io.github.isuru.oasis.model.defs.OasisDefinition;
import io.github.isuru.oasis.model.defs.RaceDef;
import io.github.isuru.oasis.model.defs.ScopingType;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.ZoneId;
import java.util.*;

abstract class BaseScheduler implements Job {

    private static final Logger LOG = LoggerFactory.getLogger(BaseScheduler.class);

    List<RaceDef> readRaces(int gameId, IOasisDao dao, String timePeriod) throws Exception {
        List<DefWrapper> defWrappers = dao.getDefinitionDao().listDefinitionsOfGame(gameId, OasisDefinition.RACE.getTypeId());
        List<RaceDef> raceDefList = new LinkedList<>();
        for (DefWrapper wrapper : defWrappers) {
            try {
                RaceDef raceDef = BaseConsumer.MAPPER.readValue(wrapper.getContent(), RaceDef.class);
                if (timePeriod.equalsIgnoreCase(raceDef.getTimeWindow())) {
                    raceDefList.add(raceDef);
                }
            } catch (IOException e) {
                LOG.error(e.getMessage(), e);
            }
        }
        return raceDefList;
    }

    boolean validateTeamAndScope(RaceDef raceDef, IOasisDao dao) throws Exception {
        Integer maxN = raceDef.getTop() == null ? 0 : raceDef.getTop();
        ScopingType scope = ScopingType.from(raceDef.getFromScope());

        if (scope == ScopingType.TEAM_SCOPE) {
            return true;
        } else if (scope == ScopingType.TEAM) {
            Map<String, Object> map = new HashMap<>();
            map.put("teamId", 0);
            map.put("offset", 0);
            map.put("limit", maxN);
            Iterable<Map<String, Object>> usersIt = dao.executeQuery("profile/listUsersOfTeam", map);
            Iterator<Map<String, Object>> it = usersIt.iterator();
            int size = 0;
            while (it.hasNext()) {
                size++;
                it.next();
            }
            if (size < maxN) {
                // not enough members in team
                // @TODO handle one team member vs. many team members
                return false;
            } else {
                return true;
            }

        } else {
            return true;
        }
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        long awardedAt = System.currentTimeMillis();
        IOasisDao dao = (IOasisDao) context.getMergedJobDataMap().get("dao");
        int gameId = (int) context.getMergedJobDataMap().get("gameId");

        try {
            Map<Long, Long> teamCountMap = loadTeamStatus(dao);
            Map<Long, Long> teamScopeCountMap = loadTeamScopeStatus(dao);

            List<RaceDef> raceDefList = readRaces(gameId, dao, "weekly");
            for (RaceDef raceDef : raceDefList) {
                DefWrapper lbWrapper = dao.getDefinitionDao().readDefinition(raceDef.getLeaderboardId());
                LeaderboardDef leaderboardDef;
                if (lbWrapper != null) {
                    leaderboardDef = BaseConsumer.MAPPER.readValue(lbWrapper.getContent(), LeaderboardDef.class);
                } else {
                    LOG.warn("No leaderboard is found by referenced id '{}' in race definition '{}'!",
                            raceDef.getLeaderboardId(), raceDef.getId());
                    continue;
                }

                ScopingType scopingType = ScopingType.from(raceDef.getFromScope());

                Map<String, Object> templateData = new HashMap<>();
                templateData.put("hasUser", false);
                templateData.put("hasTeam", false);
                templateData.put("hasTimeRange", true);
                templateData.put("hasInclusions", leaderboardDef.getRuleIds() != null && !leaderboardDef.getRuleIds().isEmpty());
                templateData.put("hasExclusions", leaderboardDef.getExcludeRuleIds() != null && !leaderboardDef.getExcludeRuleIds().isEmpty());
                templateData.put("isTopN", false);
                templateData.put("isBottomN", false);
                templateData.put("hasStates", leaderboardDef.hasStates());
                templateData.put("onlyFinalTops", true);

                long currMs = System.currentTimeMillis();
                Pair<Long, Long> timeRange = deriveTimeRange(currMs, ZoneId.systemDefault());

                Map<String, Object> data = new HashMap<>();
                data.put("rangeStart", timeRange.getValue0());
                data.put("rangeEnd", timeRange.getValue1());
                data.put("topN", raceDef.getTop());
                data.put("ruleIds", leaderboardDef.getRuleIds());
                data.put("aggType", leaderboardDef.getAggregatorType());
                data.put("topThreshold", raceDef.getTop());

                String qFile = "leaderboard/raceGlobalLeaderboard";
                if (scopingType == ScopingType.TEAM_SCOPE || scopingType == ScopingType.TEAM) {
                    qFile = "leaderboard/raceTeamLeaderboard";
                }

                Iterable<Map<String, Object>> maps = dao.executeQuery(qFile, data, templateData);

                // insert winners to database
                for (Map<String, Object> row : maps) {
                    Map<String, Object> winnerRecord = new HashMap<>();

                    winnerRecord.put("userId", row.get("userId"));
                    winnerRecord.put("raceId", raceDef.getId());
                    winnerRecord.put("raceStartAt", timeRange.getValue0());
                    winnerRecord.put("raceEndAt", timeRange.getValue1());
                    winnerRecord.put("points", row.get("totalPoints"));
                    winnerRecord.put("awardedAt", awardedAt);
                    winnerRecord.put("gameId", gameId);

                    if ("teamScope".equalsIgnoreCase(raceDef.getFromScope())) {
                        long teamScopeId = Long.parseLong(row.get("teamScopeId").toString());
                        int rankScopeTeam = Integer.parseInt(row.get("rankTeamScope").toString());

                        long playerCount = teamScopeCountMap.get(teamScopeId);
                        if (playerCount == 1) {
                            continue;
                        } else if (playerCount < raceDef.getTop() && rankScopeTeam != 1) {
                            continue;
                        }

                        winnerRecord.put("teamId", row.get("teamId"));
                        winnerRecord.put("teamScopeId", teamScopeId);
                        winnerRecord.put("rankPos", rankScopeTeam);

                    } else if ("team".equalsIgnoreCase(raceDef.getFromScope())) {
                        long teamId = Long.parseLong(row.get("teamId").toString());
                        int rankTeam = Integer.parseInt(row.get("rankTeam").toString());

                        long playerCount = teamCountMap.get(teamId);
                        if (playerCount == 1) { // no awards. skip.
                            continue;
                        } else if (playerCount < raceDef.getTop() && rankTeam != 1) {
                            // only the top will be awarded points
                            continue;
                        }

                        winnerRecord.put("teamId", teamId);
                        winnerRecord.put("teamScopeId", row.get("teamScopeId"));
                        winnerRecord.put("rankPos", rankTeam);

                    } else {
                        winnerRecord.put("rankPos", row.get("rankGlobal"));
                    }

                    dao.executeInsert("game/addRaceAward", winnerRecord, null);
                }
            }

        } catch (Exception e) {
            LOG.error("Error while reading race definitions from database!", e);
        }
    }

    private Map<Long, Long> loadTeamScopeStatus(IOasisDao dao) throws Exception {
        List<TeamStatusStat> teamList = toList(dao.executeQuery("profile/listUserCountOfTeamScope",
                new HashMap<>(), TeamStatusStat.class));
        Map<Long, Long> teamScopeCounts = new HashMap<>();
        for (TeamStatusStat statusStat : teamList) {
            teamScopeCounts.put(statusStat.getId(), statusStat.getTotalUsers());
        }
        return teamScopeCounts;
    }

    private Map<Long, Long> loadTeamStatus(IOasisDao dao) throws Exception {
        List<TeamStatusStat> teamList = toList(dao.executeQuery("profile/listUserCountOfTeams",
                new HashMap<>(), TeamStatusStat.class));
        Map<Long, Long> teamCounts = new HashMap<>();
        for (TeamStatusStat statusStat : teamList) {
            teamCounts.put(statusStat.getId(), statusStat.getTotalUsers());
        }
        return teamCounts;
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> toList(Iterable<T> iterable) {
        if (iterable instanceof List) {
            return (List)iterable;
        } else {
            List<T> list = new LinkedList<>();
            for (T item : iterable) {
                list.add(item);
            }
            return list;
        }
    }

    protected abstract Pair<Long, Long> deriveTimeRange(long ms, ZoneId zoneId);

}
