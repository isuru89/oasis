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
import io.github.oasis.model.defs.GameDef;
import io.github.oasis.services.model.RaceWinRecord;
import io.github.oasis.services.services.IGameDefService;
import io.github.oasis.services.services.IGameService;
import io.github.oasis.services.services.IProfileService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

@Component
public class WeeklyScheduler extends BaseScheduler {

    private static final Logger LOG = LoggerFactory.getLogger(WeeklyScheduler.class);

    @Autowired
    private IGameDefService gameDefService;

    @Autowired
    private IGameService gameService;

    @Autowired
    private IProfileService profileService;

    @Scheduled(cron = "1 0 0 * * MON")
    public void runWeeklyAtMidnight() throws Exception {
        long awardedAt = System.currentTimeMillis();
        List<GameDef> gameDefs = gameDefService.listGames();

        LOG.info("{}", StringUtils.repeat('-', 50));
        LOG.info("Running for Race Winners - Weekly @{} ({})", awardedAt, Instant.ofEpochMilli(awardedAt));
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
        ZonedDateTime startT = ZonedDateTime
                .ofInstant(Instant.ofEpochMilli(ms), ZoneId.systemDefault())
                .minusDays(1)
                .with(DayOfWeek.MONDAY);
        long rangeStart = startT.toInstant().toEpochMilli();
        long rangeEnd = startT.plusDays(7).toInstant().toEpochMilli();
        return Pair.of(rangeStart, rangeEnd);
    }

    @Override
    protected String filterTimeWindow() {
        return "weekly";
    }
}
