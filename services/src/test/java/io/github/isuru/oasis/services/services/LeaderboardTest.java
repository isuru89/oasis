package io.github.isuru.oasis.services.services;

import io.github.isuru.oasis.model.defs.GameDef;
import io.github.isuru.oasis.model.defs.LeaderboardDef;
import io.github.isuru.oasis.model.defs.LeaderboardType;
import io.github.isuru.oasis.services.dto.defs.GameOptionsDto;
import io.github.isuru.oasis.services.dto.game.GlobalLeaderboardRecordDto;
import io.github.isuru.oasis.services.dto.game.LeaderboardRequestDto;
import io.github.isuru.oasis.services.dto.game.TeamLeaderboardRecordDto;
import io.github.isuru.oasis.services.model.TeamProfile;
import io.github.isuru.oasis.services.model.TeamScope;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class LeaderboardTest extends WithDataTest {

    private static final long TIME_RANGE = 3600L * 24 * 7 * 1000;

    @Autowired
    private IProfileService ps;

    @Autowired
    private IGameService gameService;

    @Autowired
    private IGameDefService gameDefService;

    private long gameId;
    private LeaderboardDef lb1;

    @Before
    public void beforeEach() throws Exception {
        resetSchema();

        GameDef gameDef = new GameDef();
        gameDef.setName("so");
        gameDef.setDisplayName("Stackoverflow Game");
        gameId = gameDefService.createGame(gameDef, new GameOptionsDto());

        addPointRules(gameId,"so.rule.a", "so.rule.b", "so.rule.c", "so.rule.d", "so.rule.e");

        // populate dummy data
        loadUserData();

        initPool(5);

        lb1 = new LeaderboardDef();
        lb1.setOrderBy("desc");
        lb1.setRuleIds(Arrays.asList("so.rule.a", "so.rule.b"));
        lb1.setName("leaderboard-ab");
        lb1.setDisplayName("A & B Leaderboard");
        gameDefService.addLeaderboardDef(gameId, lb1);
    }

    @After
    public void after() {
        closePool();
    }

    @Test
    public void run() throws Exception {
        Instant startTime = LocalDateTime.of(2019, 1, 27, 12, 30)
                .atZone(ZoneOffset.UTC)
                .toInstant();

        int count = loadPoints(startTime, TIME_RANGE, gameId);
        List<Map<String, Object>> points = ServiceUtils.toList(
                dao.executeRawQuery("SELECT * FROM OA_POINT", null));
        int size = points.size();
        Assert.assertEquals(count, size);

        int globalLbSize;
        {
            System.out.println("Global Leaderboard...");
            LeaderboardRequestDto dto = new LeaderboardRequestDto(LeaderboardType.CURRENT_WEEK,
                    System.currentTimeMillis());
            dto.setLeaderboardDef(lb1);
            System.out.println("  From: " + Instant.ofEpochMilli(dto.getRangeStart()));
            System.out.println("  To: " + Instant.ofEpochMilli(dto.getRangeEnd()));
            List<GlobalLeaderboardRecordDto> rankings = gameService.readGlobalLeaderboard(dto);
            System.out.println("Size: " + rankings.size());
            globalLbSize = rankings.size();
            for (GlobalLeaderboardRecordDto recordDto : rankings) {
                System.out.println(recordDto);
            }
        }

        {
            System.out.println("Calculating Winterfell team leaderboard for current month...");
            LeaderboardRequestDto dto = new LeaderboardRequestDto(LeaderboardType.CURRENT_MONTH,
                    System.currentTimeMillis());
            dto.setLeaderboardDef(lb1);
            TeamProfile teamProfile = teams.get("winterfell");
            System.out.println("  From: " + Instant.ofEpochMilli(dto.getRangeStart()));
            System.out.println("  To: " + Instant.ofEpochMilli(dto.getRangeEnd()));
            List<TeamLeaderboardRecordDto> rankings = gameService.readTeamLeaderboard(teamProfile.getId(), dto);
            System.out.println("Size: " + rankings.size());
            for (TeamLeaderboardRecordDto recordDto : rankings) {
                System.out.println(recordDto);
            }
        }

        {
            System.out.println("Calculating The-Riverlands team leaderboard...");
            LeaderboardRequestDto dto = new LeaderboardRequestDto(LeaderboardType.CURRENT_DAY,
                    System.currentTimeMillis());
            dto.setLeaderboardDef(lb1);
            TeamScope scope = scopes.get("the-riverlands");
            System.out.println("  From: " + Instant.ofEpochMilli(dto.getRangeStart()));
            System.out.println("  To: " + Instant.ofEpochMilli(dto.getRangeEnd()));
            List<TeamLeaderboardRecordDto> rankings = gameService.readTeamScopeLeaderboard(scope.getId(), dto);
            System.out.println("Size: " + rankings.size());
            for (TeamLeaderboardRecordDto recordDto : rankings) {
                System.out.println(recordDto);
            }
        }

        {
            System.out.println("Calculating For Race team leaderboard...");
            LeaderboardRequestDto dto = new LeaderboardRequestDto(LeaderboardType.CURRENT_WEEK,
                    System.currentTimeMillis());
            dto.setLeaderboardDef(lb1);
            List<TeamLeaderboardRecordDto> rankings = gameService.readTeamLeaderboard(dto);
            System.out.println("Size: " + rankings.size());
            Assert.assertEquals(rankings.size(), globalLbSize);
            for (TeamLeaderboardRecordDto recordDto : rankings) {
                System.out.println(recordDto);
            }
        }

        {
            System.out.println("Calculating For Race team leaderboard (Only top-2)...");
            LeaderboardRequestDto dto = new LeaderboardRequestDto(LeaderboardType.CURRENT_WEEK,
                    System.currentTimeMillis());
            dto.setLeaderboardDef(lb1);
            dto.setTopThreshold(2);
            List<TeamLeaderboardRecordDto> rankings = gameService.readTeamLeaderboard(dto);
            System.out.println("Size: " + rankings.size());
            for (TeamLeaderboardRecordDto recordDto : rankings) {
                Assert.assertTrue(recordDto.getRankInTeamScope() <= 2);
                System.out.println(recordDto);
            }
        }

        {
            double thold = 1500.0;
            System.out.println("Calculating For Race team leaderboard (Only having "+thold+" points)...");
            LeaderboardRequestDto dto = new LeaderboardRequestDto(LeaderboardType.CURRENT_WEEK,
                    System.currentTimeMillis());
            dto.setLeaderboardDef(lb1);
            dto.setMinPointThreshold(thold);
            List<TeamLeaderboardRecordDto> rankings = gameService.readTeamLeaderboard(dto);
            System.out.println("Size: " + rankings.size());
            for (TeamLeaderboardRecordDto recordDto : rankings) {
                Assert.assertTrue(recordDto.getTotalPoints() >= thold);
                System.out.println(recordDto);
            }
        }
    }

}
