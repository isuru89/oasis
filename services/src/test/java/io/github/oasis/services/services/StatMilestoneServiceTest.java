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
import io.github.oasis.services.dto.stats.UserMilestoneStatDto;
import io.github.oasis.services.model.UserProfile;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

public class StatMilestoneServiceTest extends WithDataTest {

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

        addMilestoneRules(gameId, "Total Questions", "Total Answers", "Total Votes", "Total Edits",
                "Total Reviews");

        loadUserData();

        initPool(3);
    }

    @After
    public void after() {
        closePool();
    }

    @Test
    public void testMilestoneStats() throws Exception {
        Instant startTime = LocalDateTime.of(2019, 1, 27, 12, 30)
                .atZone(ZoneOffset.UTC)
                .toInstant();
        loadMilestones(startTime, 3600L * 24 * 10 * 1000, gameId);

//        {
//            for (Map<String, Object> row : dao.executeRawQuery("SELECT * FROM OA_MILESTONE", null)) {
//                System.out.println(row);
//            }
//        }

        {
            UserProfile jaime = users.get("jaime-lannister");
            List<UserMilestoneStatDto> records = statService.readUserMilestones(jaime.getId());
            Assert.assertTrue(records.size() > 0);
            for (UserMilestoneStatDto dto : records) {
                Assert.assertNotNull(dto.getMilestoneDisplayName());
                Assert.assertNotNull(dto.getMilestoneName());
                System.out.println(String.format("%d\t%s\t%s\t%d/%d\t%d\t%d\t%d\t%s",
                        dto.getMilestoneId(),
                        dto.getMilestoneName(),
                        dto.getMilestoneDisplayName(),
                        dto.getCurrentLevel(),
                        dto.getMaximumLevel(),
                        dto.getCurrentBaseValueL(),
                        dto.getCurrentValueL(),
                        dto.getNextValueL(),
                        Instant.ofEpochMilli(dto.getAchievedTime())));
            }
        }

    }
}
