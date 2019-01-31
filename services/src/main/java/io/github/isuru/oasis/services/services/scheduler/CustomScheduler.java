package io.github.isuru.oasis.services.services.scheduler;

import io.github.isuru.oasis.model.collect.Pair;
import io.github.isuru.oasis.model.defs.RaceDef;
import io.github.isuru.oasis.services.dto.game.RaceCalculationDto;
import io.github.isuru.oasis.services.model.RaceWinRecord;
import io.github.isuru.oasis.services.services.IGameDefService;
import io.github.isuru.oasis.services.services.IGameService;
import io.github.isuru.oasis.services.services.IProfileService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

public class CustomScheduler extends BaseScheduler {

    private static final Logger LOG = LoggerFactory.getLogger(CustomScheduler.class);

    private RaceCalculationDto calculationOptions;

    private IGameService gameService;
    private IGameDefService gameDefService;
    private IProfileService profileService;

    public CustomScheduler(RaceCalculationDto calculationDto,
                           IGameDefService gameDefService,
                           IProfileService profileService,
                           IGameService gameService) {
        this.calculationOptions = calculationDto;

        this.gameDefService = gameDefService;
        this.gameService = gameService;
        this.profileService = profileService;
    }

    public List<RaceWinRecord> runCustomInvoke(RaceDef raceDef, long gameId, long awardedAt) throws Exception {
        LOG.info("{}", StringUtils.repeat('-', 50));
        LOG.info("Running for Race Winners - Custom @{} ({})", awardedAt, Instant.ofEpochMilli(awardedAt));

        LOG.info("  Calculating All Race Winners for race {}", raceDef.getId());

        Map<Long, Long> teamCountMap = loadTeamStatus(profileService, awardedAt);
        Map<Long, Long> teamScopeCountMap = loadTeamScopeStatus(profileService, awardedAt);

        List<RaceWinRecord> winners = calcWinnersForRace(raceDef,
                awardedAt,
                gameId,
                gameDefService,
                gameService,
                profileService,
                teamCountMap,
                teamScopeCountMap);

        if (calculationOptions.isDoPersist()) {
            gameService.addRaceWinners(gameId, raceDef.getId(), winners);
        }
        return winners;
    }

    @Override
    protected Pair<Long, Long> deriveTimeRange(long ms, ZoneId zoneId) {
        return Pair.of(calculationOptions.getStartTime(), calculationOptions.getEndTime());
    }

    @Override
    protected String filterTimeWindow() {
        return "custom";
    }
}
