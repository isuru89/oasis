package io.github.isuru.oasis.injector.scheduler;

import io.github.isuru.oasis.model.collect.Pair;
import io.github.isuru.oasis.model.db.IOasisDao;
import io.github.isuru.oasis.model.defs.RaceDef;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WeeklyScheduler extends BaseScheduler implements Job {

    private static final Logger LOG = LoggerFactory.getLogger(WeeklyScheduler.class);

    @Override
    protected Pair<Long, Long> deriveTimeRange(long ms, ZoneId zoneId) {
        ZonedDateTime startT = ZonedDateTime
                .ofInstant(Instant.ofEpochMilli(ms), ZoneId.systemDefault())
                .minusDays(1)
                .with(DayOfWeek.MONDAY);
        long rangeStart = startT.toInstant().toEpochMilli();
        long rangeEnd = startT.plusDays(7).toInstant().toEpochMilli();
        return Pair.of(rangeStart, rangeEnd);
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        IOasisDao dao = (IOasisDao) context.getMergedJobDataMap().get("dao");
        int gameId = (int) context.getMergedJobDataMap().get("gameId");

        try {
            List<RaceDef> raceDefList = readRaces(gameId, dao, "weekly");
            for (RaceDef raceDef : raceDefList) {
                Map<String, Object> templateData = new HashMap<>();
                templateData.put("hasUser", false);
                templateData.put("hasTimeRange", true);
                templateData.put("hasInclusions", raceDef.getRuleIds() != null && !raceDef.getRuleIds().isEmpty());
                templateData.put("isTopN", raceDef.getTop() != null && raceDef.getTop() > 0);
                templateData.put("isBottomN", raceDef.getBottom() != null && raceDef.getBottom() > 0);

                long currMs = System.currentTimeMillis();
                Pair<Long, Long> timeRange = deriveTimeRange(currMs, ZoneId.systemDefault());

                Map<String, Object> data = new HashMap<>();
                data.put("rangeStart", timeRange.getValue0());
                data.put("rangeEnd", timeRange.getValue1());
                data.put("topN", raceDef.getTop());
                data.put("bottomN", raceDef.getBottom());
                data.put("ruleIds", raceDef.getRuleIds());
                data.put("aggType", raceDef.getAggregatorType());

                String qFile = "leaderboard/raceGlobalLeaderboard";
                if ("teamScope".equalsIgnoreCase(raceDef.getFromScope())
                        || "team".equalsIgnoreCase(raceDef.getFromScope())) {
                    qFile = "leaderboard/raceTeamLeaderboard";
                }

                Iterable<Map<String, Object>> maps = dao.executeQuery(qFile, data, templateData);
            }

        } catch (Exception e) {
            LOG.error("Error while reading race definitions from database!", e);
        }
    }
}
