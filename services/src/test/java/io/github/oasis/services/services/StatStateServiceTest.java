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

package io.github.oasis.services.services;

import io.github.oasis.model.defs.GameDef;
import io.github.oasis.services.dto.defs.GameOptionsDto;
import io.github.oasis.services.dto.stats.UserStateStatDto;
import io.github.oasis.services.model.UserProfile;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class StatStateServiceTest extends WithDataTest {

    @Autowired
    private IGameDefService gameDefService;

    @Autowired
    private IStatService statService;

    private long gameId;

    @Before
    public void before() throws Exception {
        resetSchema();

        GameDef gameDef = new GameDef();
        gameDef.setName("so");
        gameDef.setDisplayName("Stackoverflow Game");
        gameId = gameDefService.createGame(gameDef, new GameOptionsDto());

        // populate dummy data
        loadUserData();

        loadStateDefs(gameId,
                Arrays.asList("Good Question Ratio", "good", "bad"),
                Arrays.asList("Quality Vote Ratio", "good", "normal", "bad"),
                Arrays.asList("Review Quality", "excellent", "good", "ok", "bad", "worse"),
                Arrays.asList("Maintainability", "cool", "meh")
        );

        initPool(5);
        System.out.println(StringUtils.repeat('-', 50));
    }

    @After
    public void after() {
        closePool();
        System.out.println(StringUtils.repeat('-', 50));
    }

    @Test
    public void testStateInfo() throws Exception {
        loadStates(gameId);

        {
            for (Map<String, Object> row : dao.executeRawQuery("SELECT * FROM OA_RATING", null)) {
                System.out.println(row);
            }
        }

        {
            List<UserProfile> profiles = new ArrayList<>(users.values());
            for (UserProfile profile : profiles) {
                System.out.println("State of user: " + profile.getName());
                List<UserStateStatDto> stats = statService.readUserStateStats(profile.getId());
                Assert.assertNotNull(stats);
                for (UserStateStatDto stat : stats) {
                    Assert.assertNotNull(stat.getCurrentStateName());
                    Assert.assertNotNull(stat.getRatingDefDisplayName());
                    Assert.assertNotNull(stat.getCurrentPoints());
                    Assert.assertNotNull(stat.getCurrentValue());

                    System.out.println(String.format("%d\t%d\t%s\t%s\t%d\t[%s]\t%.2f\t%s\t%s",
                            stat.getTeamId(),
                            stat.getTeamScopeId(),
                            stat.getRatingDefName(),
                            stat.getRatingDefDisplayName(),
                            stat.getCurrentState(),
                            stat.getCurrentStateName(),
                            stat.getCurrentPoints(),
                            stat.getCurrentValue(),
                            Instant.ofEpochMilli(stat.getLastChangedAt())));
                }
            }

        }
    }
}
