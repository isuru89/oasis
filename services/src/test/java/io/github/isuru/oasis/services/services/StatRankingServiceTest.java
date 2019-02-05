package io.github.isuru.oasis.services.services;

import io.github.isuru.oasis.model.defs.GameDef;
import io.github.isuru.oasis.model.defs.LeaderboardType;
import io.github.isuru.oasis.services.dto.defs.GameOptionsDto;
import io.github.isuru.oasis.services.dto.game.RankingRecord;
import io.github.isuru.oasis.services.dto.stats.TeamHistoryRecordDto;
import io.github.isuru.oasis.services.dto.stats.UserScopeRankingsStat;
import io.github.isuru.oasis.services.model.UserProfile;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StatRankingServiceTest extends WithDataTest {

    private long gameId;

    @Autowired
    private IGameDefService gameDefService;

    @Autowired
    private IStatService statService;

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

        initPool(5);
        System.out.println(StringUtils.repeat('-', 50));
    }

    @After
    public void after() {
        closePool();
        System.out.println(StringUtils.repeat('-', 50));
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
