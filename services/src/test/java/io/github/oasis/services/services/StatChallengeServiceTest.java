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

import io.github.oasis.model.defs.BaseDef;
import io.github.oasis.model.defs.ChallengeDef;
import io.github.oasis.model.defs.GameDef;
import io.github.oasis.services.dto.defs.GameOptionsDto;
import io.github.oasis.services.dto.stats.ChallengeInfoDto;
import io.github.oasis.services.dto.stats.ChallengeWinDto;
import io.github.oasis.services.dto.stats.ChallengeWinnerDto;
import io.github.oasis.services.dto.stats.UserChallengeWinRes;
import io.github.oasis.services.exception.InputValidationException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StatChallengeServiceTest extends WithDataTest {

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

        loadUserData();

        addChallenges(gameId, "Answer Question 123",
                "Ask A Well Question",
                "Award Reputation",
                "Review #100 Old Question",
                "Ask Well Question on Area51");

        initPool(3);
    }

    @After
    public void after() {
        closePool();
    }

    @Test
    public void testChallengeStats() throws Exception {
        Instant startTime = LocalDateTime.of(2019, 1, 27, 12, 30)
                .atZone(ZoneOffset.UTC)
                .toInstant();
        loadChallenges(startTime, 3600L * 24 * 10 * 1000, gameId);

        {
            for (Map<String, Object> row : dao.executeRawQuery("SELECT * FROM OA_CHALLENGE_WINNER", null)) {
                System.out.println(row);
            }
        }


        List<Long> userIds = new ArrayList<>();
        {
            List<ChallengeDef> challengeDefs = gameDefService.listChallenges(gameId);
            for (ChallengeDef def : challengeDefs) {
                ChallengeInfoDto dto = statService.readChallengeStats(def.getId());
                Assert.assertNotNull(dto.getChallengeDef());
                Assert.assertTrue(dto.getWinners().size() > 0);
                System.out.println("Winners of challenge #" + def.getId());
                for (ChallengeWinnerDto winner : dto.getWinners()) {
                    Assert.assertNotNull(winner.getWinNo());

                    userIds.add(winner.getUserId());
                    System.out.println(String.format("(%d)\t%s\t%s\t%s\t%.2f",
                            winner.getWinNo(),
                            winner.getUserNickname(),
                            winner.getTeamName(),
                            winner.getTeamScopeDisplayName(),
                            winner.getPointsScored()));
                }
            }
        }

        {
            for (int i = 0; i < userIds.size(); i++) {
                long userId = userIds.get(i);
                System.out.println("Finding User #" + userId + " - Challenge Wins");
                UserChallengeWinRes userWins = statService.readUserChallengeWins(userId);
                int frequency = Collections.frequency(userIds, userId);

                Assert.assertEquals(frequency, userWins.getWins().size());
                for (ChallengeWinDto win : userWins.getWins()) {
                    Assert.assertNotNull(win.getWinNo());

                    System.out.println(String.format("(%d)\t%d\t%s\t%s\t%.2f",
                            win.getWinNo(),
                            win.getChallengeId(),
                            win.getChallengeName(),
                            win.getChallengeDisplayName(),
                            win.getPointsScored()));
                }
            }
        }
    }

    @Test
    public void testChallengeStatsFailures() throws Exception {
        List<Long> cIds = gameDefService.listChallenges(gameId)
                .stream()
                .map(BaseDef::getId)
                .collect(Collectors.toList());

        long min = cIds.stream().min(Comparator.comparingLong(o -> o)).orElse(0L);

        long nonExistId = -1;
        for (long i = min; i < min + 100; i++) {
            if (!cIds.contains(i)) {
                nonExistId = i;
                break;
            }
        }

        Assert.assertTrue(nonExistId > 0);
        shouldFail(nonExistId);
    }

    private void shouldFail(final long challengeId) {
        Assertions.assertThrows(InputValidationException.class, () -> statService.readChallengeStats(challengeId));
    }
}