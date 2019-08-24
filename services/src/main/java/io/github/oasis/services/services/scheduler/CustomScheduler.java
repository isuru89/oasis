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

import io.github.oasis.model.collect.Pair;
import io.github.oasis.model.defs.RaceDef;
import io.github.oasis.services.dto.game.RaceCalculationDto;
import io.github.oasis.services.model.RaceWinRecord;
import io.github.oasis.services.services.IGameDefService;
import io.github.oasis.services.services.IGameService;
import io.github.oasis.services.services.IProfileService;
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
