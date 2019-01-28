package io.github.isuru.oasis.services.services.scheduler;

import io.github.isuru.oasis.model.collect.Pair;
import io.github.isuru.oasis.model.defs.GameDef;
import io.github.isuru.oasis.services.model.RaceWinRecord;
import io.github.isuru.oasis.services.services.IGameDefService;
import io.github.isuru.oasis.services.services.IGameService;
import io.github.isuru.oasis.services.services.IProfileService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

@Component
public class MonthlyScheduler extends BaseScheduler {

    private static final Logger LOG = LoggerFactory.getLogger(MonthlyScheduler.class);

    @Autowired
    private IGameDefService gameDefService;

    @Autowired
    private IGameService gameService;

    @Autowired
    private IProfileService profileService;


    @Scheduled(cron = "1 0 0 1 * ?")
    public void runMonthlyAtMidnight() throws Exception {
        long awardedAt = System.currentTimeMillis();
        List<GameDef> gameDefs = gameDefService.listGames();

        LOG.info("{}", StringUtils.repeat('-', 50));
        LOG.info("Running for Race Winners - Monthly @{} ({})", awardedAt, Instant.ofEpochMilli(awardedAt));
        for (GameDef gameDef : gameDefs) {
            LOG.info("  Calculating All Race Winners for game {} [#{}]", gameDef.getName(), gameDef.getId());
            Map<Long, List<RaceWinRecord>> winnersByRace = runForGame(profileService,
                    gameDefService, gameService, gameDef.getId(), awardedAt);

            for (Map.Entry<Long, List<RaceWinRecord>> entry : winnersByRace.entrySet()) {
                gameService.addRaceWinners(gameDef.getId(), entry.getKey(), entry.getValue());
            }
        }
    }

    @Override
    protected Pair<Long, Long> deriveTimeRange(long ms, ZoneId zoneId) {
        ZonedDateTime zonedDateTime = Instant.ofEpochMilli(ms).atZone(zoneId);
        YearMonth from = YearMonth.of(zonedDateTime.getYear(), zonedDateTime.getMonth());
        long start = from.atDay(1).atStartOfDay()
                .toInstant(ZoneOffset.UTC).toEpochMilli();
        long end = from.atEndOfMonth().plusDays(1).atStartOfDay()
                .toInstant(ZoneOffset.UTC).toEpochMilli();
        return Pair.of(start, end);
    }

    @Override
    protected String filterTimeWindow() {
        return "monthly";
    }
}
