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
import io.github.oasis.model.defs.LeaderboardDef;
import io.github.oasis.model.defs.LeaderboardType;
import io.github.oasis.model.defs.ScopingType;
import io.github.oasis.services.dto.defs.GameOptionsDto;
import io.github.oasis.services.dto.game.RankingRecord;
import io.github.oasis.services.dto.game.UserLeaderboardRankingsDto;
import io.github.oasis.services.dto.game.UserRankingsInRangeDto;
import io.github.oasis.services.dto.stats.MyLeaderboardReq;
import io.github.oasis.services.dto.stats.TeamHistoryRecordDto;
import io.github.oasis.services.dto.stats.UserScopeRankingsStat;
import io.github.oasis.services.exception.InputValidationException;
import io.github.oasis.services.model.UserProfile;
import org.apache.commons.lang3.StringUtils;
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
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class StatRankingServiceTest extends WithDataTest {

    private long gameId;

    @Autowired
    private IGameDefService gameDefService;

    @Autowired
    private IStatService statService;

    private LeaderboardDef l1;
    private LeaderboardDef l2;

    @Before
    public void before() throws Exception {
        resetSchema();

        GameDef gameDef = new GameDef();
        gameDef.setName("so");
        gameDef.setDisplayName("Stackoverflow Game");
        gameId = gameDefService.createGame(gameDef, new GameOptionsDto());

        pointRuleIds = addPointRules(gameId,"so.rule.a", "so.rule.b", "so.rule.c", "so.rule.d", "so.rule.e");
        addBadgeNames(gameId,
                Arrays.asList("so-badge-1", "so-b-sub-gold", "so-b-sub-silver"),
                Arrays.asList("so-badge-2", "so-b-sub-gold", "so-b-sub-silver", "so-b-sub-bronze"),
                Arrays.asList("so-badge-3", "so-b-sub-1", "so-b-sub-2", "so-b-sub-2")
        );

        // populate dummy data
        loadUserData();


        addChallenges(gameId, "Answer Question 123",
                "Ask A Well Question",
                "Award Reputation",
                "Review #100 Old Question",
                "Ask Well Question on Area51");

        // add leaderboard defs
        {
            LeaderboardDef l1 = new LeaderboardDef();
            l1.setName("A & B");
            l1.setDisplayName("AB Leaderboard");
            l1.setRuleIds(Arrays.asList("so.rule.a", "so.rule.b"));
            l1.setIncludeStatePoints(false);
            l1.setOrderBy("desc");
            long id = gameDefService.addLeaderboardDef(gameId, l1);
            this.l1 = gameDefService.readLeaderboardDef(id);
        }
        {
            LeaderboardDef l1 = new LeaderboardDef();
            l1.setName("Not-A-n-B");
            l1.setDisplayName("!AB! Leaderboard");
            l1.setExcludeRuleIds(Arrays.asList("so.rule.a", "so.rule.b"));
            l1.setOrderBy("asc");
            l1.setIncludeStatePoints(false);
            long id = gameDefService.addLeaderboardDef(gameId, l1);
            this.l2 = gameDefService.readLeaderboardDef(id);
        }

        initPool(5);
        System.out.println(StringUtils.repeat('-', 50));
    }

    @After
    public void after() {
        closePool();
        System.out.println(StringUtils.repeat('-', 50));
    }

    @Test
    public void testLeaderboardRangeRankTest() throws Exception {
        {
            // invalid parameters must failed.
            Assertions.assertThrows(InputValidationException.class,
                    () -> statService.readUserTeamRankings(0, 1, 0, 3L));
            Assertions.assertThrows(InputValidationException.class,
                    () -> statService.readUserTeamRankings(-1, 1, 0, 3L));
            Assertions.assertThrows(InputValidationException.class,
                    () -> statService.readUserTeamRankings(1, -1, 0, 3L));
            Assertions.assertThrows(InputValidationException.class,
                    () -> statService.readUserTeamRankings(1, 0, 0, 3L));

            // non existing leaderboard
            Assertions.assertThrows(InputValidationException.class,
                    () -> statService.readUserTeamRankings(1, 3, l2.getId() + 100, 3L));
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime earlier = now.minusDays(10);

        Instant startTime = LocalDateTime.of(earlier.getYear(), earlier.getMonth(), earlier.getDayOfMonth(), 12, 30)
                .atZone(ZoneOffset.UTC)
                .toInstant();
        loadPoints(startTime, 3600L * 24 * 20 * 1000, gameId);

        LocalDateTime dayBefore = now.minusDays(1);
        long relTime = LocalDateTime.of(dayBefore.getYear(), dayBefore.getMonth(), dayBefore.getDayOfMonth(), 12, 30)
                .atZone(ZoneOffset.UTC)
                .toInstant().toEpochMilli();
        List<UserProfile> oUsers = new ArrayList<>(users.values());
        Random random = new Random(System.currentTimeMillis());
        for (UserProfile oUser : oUsers) {
            {
                // global leaderboard
                System.out.println("---");
                System.out.println("Reading user " + oUser.getName() + "'s global leaderboard rankings for all ranges.");
                UserRankingsInRangeDto rankings = statService.readUserTeamRankings(gameId, oUser.getId(),
                        0,
                        random.nextBoolean() ? relTime : 0);
                Assert.assertNotNull(rankings);
                System.out.println("  Daily  : " + printRanking(rankings.getDaily().getRank()));
                System.out.println("  Weekly : " + printRanking(rankings.getWeekly().getRank()));
                System.out.println("  Monthly: " + printRanking(rankings.getMonthly().getRank()));
            }

            {
                // leaderboard def 1
                System.out.println("---");
                System.out.println("Reading user " + oUser.getName() + "'s A&B leaderboard rankings for all ranges.");
                UserRankingsInRangeDto rankings = statService.readUserTeamRankings(gameId, oUser.getId(), l1.getId(), relTime);
                Assert.assertNotNull(rankings);
                System.out.println("  Daily  : " + printRanking(rankings.getDaily().getRank()));
                System.out.println("  Weekly : " + printRanking(rankings.getWeekly().getRank()));
                System.out.println("  Monthly: " + printRanking(rankings.getMonthly().getRank()));
            }
        }
    }

    private String printRanking(RankingRecord record) {
        if (record == null) {
            return "NONE";
        }
        return String.format("%d - %.2f (%d)", record.getRank(),
                record.getMyValue(),
                record.getMyCount());
    }

    @Test
    public void testMyLeaderboardRangeRankTest() throws Exception {
        {
            // invalid parameters must failed.
            Assertions.assertThrows(InputValidationException.class,
                    () -> statService.readMyLeaderboardRankings(0, 1, null));
            Assertions.assertThrows(InputValidationException.class,
                    () -> statService.readMyLeaderboardRankings(-1, 1, null));
            Assertions.assertThrows(InputValidationException.class,
                    () -> statService.readMyLeaderboardRankings(1, -1, null));
            Assertions.assertThrows(InputValidationException.class,
                    () -> statService.readMyLeaderboardRankings(1, 0, null));

            // null req
            Assertions.assertThrows(InputValidationException.class,
                    () -> statService.readMyLeaderboardRankings(1, 3, null));
            Assertions.assertThrows(InputValidationException.class,
                    () -> statService.readMyLeaderboardRankings(1, 3, new MyLeaderboardReq()));

            Assertions.assertThrows(InputValidationException.class,
                    () -> statService.readMyLeaderboardRankings(1, 3,
                            new MyLeaderboardReq(ScopingType.GLOBAL, null, null, null)));
            Assertions.assertThrows(InputValidationException.class,
                    () -> statService.readMyLeaderboardRankings(1, 3,
                            new MyLeaderboardReq(ScopingType.GLOBAL, LeaderboardType.CUSTOM, null, null)));
            Assertions.assertThrows(InputValidationException.class,
                    () -> statService.readMyLeaderboardRankings(1, 3,
                            new MyLeaderboardReq(ScopingType.TEAM, LeaderboardType.CUSTOM, 1L, null)));
            Assertions.assertThrows(InputValidationException.class,
                    () -> statService.readMyLeaderboardRankings(1, 3,
                            new MyLeaderboardReq(ScopingType.TEAM_SCOPE, LeaderboardType.CUSTOM, null, 123L)));

        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime earlier = now.minusDays(10);

        Instant startTime = LocalDateTime.of(earlier.getYear(), earlier.getMonth(), earlier.getDayOfMonth(), 12, 30)
                .atZone(ZoneOffset.UTC)
                .toInstant();
        loadPoints(startTime, 3600L * 24 * 20 * 1000, gameId);

        LocalDateTime before = now.minusDays(3);
        long relTime = LocalDateTime.of(before.getYear(), before.getMonth(), before.getDayOfMonth(), 12, 30)
                .atZone(ZoneOffset.UTC)
                .toInstant().toEpochMilli();
        long currTime = System.currentTimeMillis();
        List<UserProfile> oUsers = new ArrayList<>(users.values());
        for (UserProfile oUser : oUsers) {
            {
                // global leaderboards
                System.out.println("---");
                System.out.println("Reading user " + oUser.getName() + "'s all leaderboard global weekly rankings:");
                MyLeaderboardReq req = new MyLeaderboardReq();
                req.setScopingType(ScopingType.GLOBAL);
                req.setRangeType(LeaderboardType.CURRENT_WEEK);
                List<UserLeaderboardRankingsDto> rankings = statService.readMyLeaderboardRankings(gameId,
                        oUser.getId(), req);
                Assert.assertNotNull(rankings);
                Assert.assertEquals(3, rankings.size());

                for (UserLeaderboardRankingsDto ranking : rankings) {
                    System.out.println(" - " + ranking.getLeaderboardDef().getName() + " => "
                            + printRanking(ranking.getGlobal()));
                }
            }

            {
                // team and team scope leaderboards
                System.out.println("---");
                System.out.println("Reading user " + oUser.getName() + "'s all leaderboard team/scope daily rankings:");
                MyLeaderboardReq req = new MyLeaderboardReq();
                req.setScopingType(ScopingType.TEAM);
                req.setRangeType(LeaderboardType.CUSTOM);
                req.setRangeStart(relTime);
                req.setRangeEnd(currTime);
                List<UserLeaderboardRankingsDto> rankings = statService.readMyLeaderboardRankings(gameId,
                        oUser.getId(), req);
                Assert.assertNotNull(rankings);
                Assert.assertEquals(3, rankings.size());

                for (UserLeaderboardRankingsDto ranking : rankings) {
                    System.out.println(" - Team: " + ranking.getLeaderboardDef().getName() + " => "
                            + printRanking(ranking.getTeam()));
                    System.out.println(" - Scope: " + ranking.getLeaderboardDef().getName() + " => "
                            + printRanking(ranking.getTeamScope()));
                }
            }
        }
    }


    @Test
    public void testRankingTest() throws Exception {
        Instant startTime = LocalDateTime.of(2019, 1, 27, 12, 30)
                .atZone(ZoneOffset.UTC)
                .toInstant();
        loadPoints(startTime, 3600L * 24 * 10 * 1000, gameId);

        List<UserProfile> oUsers = new ArrayList<>(users.values());
        List<LeaderboardType> leaderboardTypes = Arrays.asList(LeaderboardType.CURRENT_WEEK, LeaderboardType.CURRENT_DAY,
                LeaderboardType.CURRENT_MONTH);
        for (int i = 0; i < oUsers.size(); i++) {
            UserProfile p = oUsers.get(i);
            System.out.println("User " + p.getName() + ":");
            for (LeaderboardType leaderboardType : leaderboardTypes) {
                UserScopeRankingsStat ranks = statService.readMyRankings(gameId, p.getId(), leaderboardType);
                System.out.println("  - " + leaderboardType + " rankings:");

                {
                    RankingRecord rank = ranks.getGlobal();
                    if (rank != null) {
                        System.out.println(String.format("\t%s:\t\t%d\t%.2f (%d)\t%.2f\t%.2f",
                                "Global",
                                rank.getRank(),
                                rank.getMyValue(),
                                rank.getMyCount(),
                                rank.getNextValue(),
                                rank.getTopValue()));
                    }
                }
                {
                    RankingRecord rank = ranks.getTeamScope();
                    if (rank != null) {
                        System.out.println(String.format("\t%s:\t%d\t%.2f (%d)\t%.2f\t%.2f",
                                "TeamScope",
                                rank.getRank(),
                                rank.getMyValue(),
                                rank.getMyCount(),
                                rank.getNextValue(),
                                rank.getTopValue()));
                    }
                }
                {
                    RankingRecord rank = ranks.getTeam();
                    if (rank != null) {
                        System.out.println(String.format("\t%s:\t\t%d\t%.2f (%d)\t%.2f\t%.2f",
                                "Team",
                                rank.getRank(),
                                rank.getMyValue(),
                                rank.getMyCount(),
                                rank.getNextValue(),
                                rank.getTopValue()));
                    }
                }

            }
        }
    }

    @Test
    public void testTeamWiseSummary() throws Exception {
        Instant startTime = LocalDateTime.of(2019, 1, 27, 12, 30)
                .atZone(ZoneOffset.UTC)
                .toInstant();
        loadPoints(startTime, 3600L * 24 * 10 * 1000, gameId);
        loadBadges(startTime, 3600L * 24 * 10 * 1000, gameId);
        loadChallenges(startTime, 3600L * 24 * 10 * 1000, gameId);


        List<UserProfile> oUsers = new ArrayList<>(users.values());
        for (int i = 0; i < oUsers.size(); i++) {
            UserProfile p = oUsers.get(i);

            System.out.println("Stat for user " + p.getName() + ":");
            List<TeamHistoryRecordDto> records = statService.readUserTeamHistoryStat(p.getId());

            for (TeamHistoryRecordDto record : records) {
                System.out.println(String.format("%s\t%s\t%.2f (%d)\t%d [%d]\t%d\t%d",
                        record.getTeamName(),
                        record.getTeamScopeName(),
                        record.getTotalPoints(),
                        record.getTotalCount(),
                        record.getTotalBadges(),
                        record.getTotalUniqueBadges(),
                        record.getTotalChallengeWins(),
                        record.getTotalRaceWins()));
            }
        }
    }
}
