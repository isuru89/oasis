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

import io.github.oasis.model.defs.RaceDef;
import io.github.oasis.services.model.RaceWinRecord;
import io.github.oasis.services.model.TeamProfile;
import io.github.oasis.services.services.IGameDefService;
import io.github.oasis.services.services.IGameService;
import io.github.oasis.services.services.IProfileService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MonthlySchedulerTest extends AbstractSchedulerTest {


    @Autowired
    private MonthlyScheduler monthlyScheduler;

    @Autowired
    private IGameDefService gameDefService;

    @Autowired
    private IProfileService profileService;

    @Autowired
    private IGameService gameService;

    private long gameId;

    private List<RaceDef> raceDefs;

    @Before
    public void before() throws Exception {
        resetSchema();

        gameId = createGame();

        loadUserData();

        initPool(5);

        createPoints(gameId);
        createRaces(gameId, "MONTHLY");

        raceDefs = gameDefService.listRaces(gameId);
        Assert.assertNotNull(raceDefs);
        Assert.assertTrue(raceDefs.size() > 0);
    }

    @After
    public void after() {
        closePool();
    }


    @Test
    public void testMonthlyScheduler() throws Exception {
        long ts = System.currentTimeMillis();
        Map<Long, List<RaceWinRecord>> winnersByRace = monthlyScheduler.runForGame(profileService, gameDefService, gameService, gameId, ts);
        Assert.assertNotNull(winnersByRace);
        Assert.assertEquals(raceDefs.size(), winnersByRace.size());

        List<TeamProfile> teamProfiles = new ArrayList<>(teams.values());
        for (RaceDef raceDef : raceDefs) {
            Assert.assertTrue(winnersByRace.containsKey(raceDef.getId()));

            List<RaceWinRecord> raceWinRecords = winnersByRace.get(raceDef.getId());
            Assert.assertTrue(raceDef.getTop() * teamProfiles.size() >= raceWinRecords.size());

            System.out.println("Winner of monthly race: " + raceDef.getName());
            for (RaceWinRecord raceWinRecord : raceWinRecords) {
                Assert.assertTrue( raceWinRecord.getRank() > 0  && raceWinRecord.getRank() <= raceDef.getTop());
                Assert.assertTrue(raceWinRecord.getTeamId() > 0);
                Assert.assertTrue(raceWinRecord.getUserId() > 0);
                Assert.assertTrue(raceWinRecord.getTeamScopeId() > 0);

                System.out.println(String.format("\tUser %d from team '%d' [%d] : rank %d having points %.2f (%d) awarded %.2f",
                        raceWinRecord.getUserId(),
                        raceWinRecord.getTeamId(),
                        raceWinRecord.getTeamScopeId(),
                        raceWinRecord.getRank(),
                        raceWinRecord.getPoints(),
                        raceWinRecord.getTotalCount(),
                        raceWinRecord.getAwardedPoints()));
            }
        }
    }
}
