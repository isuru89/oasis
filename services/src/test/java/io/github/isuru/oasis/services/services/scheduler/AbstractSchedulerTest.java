package io.github.isuru.oasis.services.services.scheduler;

import io.github.isuru.oasis.model.defs.GameDef;
import io.github.isuru.oasis.model.defs.LeaderboardDef;
import io.github.isuru.oasis.model.defs.RaceDef;
import io.github.isuru.oasis.model.defs.ScopingType;
import io.github.isuru.oasis.services.dto.defs.GameOptionsDto;
import io.github.isuru.oasis.services.services.IGameDefService;
import io.github.isuru.oasis.services.services.WithDataTest;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractSchedulerTest extends WithDataTest {

    @Autowired
    private IGameDefService gameDefService;

    long createGame() throws Exception {
        GameDef gameDef = new GameDef();
        gameDef.setName("so");
        gameDef.setDisplayName("Stackoverflow");
        return gameDefService.createGame(gameDef, new GameOptionsDto());
    }

    void createPoints(long gameId) throws Exception {
        pointRuleIds = addPointRules(gameId,"so.rule.a", "so.rule.b", "so.rule.c", "so.rule.d", "so.rule.e");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime earlier = now.minusDays(10);

        Instant startTime = LocalDateTime.of(earlier.getYear(), earlier.getMonth(), earlier.getDayOfMonth(), 12, 30)
                .atZone(ZoneOffset.UTC)
                .toInstant();
        loadPoints(startTime, 3600L * 24 * 20 * 1000, gameId);

    }

    void createRaces(long gameId, String timeWindow) throws Exception {
        {
            LeaderboardDef l1 = new LeaderboardDef();
            l1.setName("A-n-B");
            l1.setDisplayName("AB Leaderboard");
            l1.setOrderBy("desc");
            l1.setIncludeStatePoints(false);
            l1.setRuleIds(Arrays.asList("so.rule.a", "so.rule.b"));
            long lid = gameDefService.addLeaderboardDef(gameId, l1);

            RaceDef raceDef = new RaceDef();
            raceDef.setName(timeWindow + " AB Race");
            raceDef.setDisplayName(raceDef.getName());
            raceDef.setLeaderboardId((int) lid);
            raceDef.setFromScope(ScopingType.TEAM_SCOPE.name());
            raceDef.setTop(3);
            raceDef.setTimeWindow(timeWindow);
            raceDef.setRankPointsExpression("1000.0");
            gameDefService.addRace(gameId, raceDef);
        }

        {
            LeaderboardDef l1 = new LeaderboardDef();
            l1.setName("D-n-E");
            l1.setDisplayName("DE Leaderboard");
            l1.setOrderBy("desc");
            l1.setIncludeStatePoints(false);
            l1.setRuleIds(Arrays.asList("so.rule.d", "so.rule.e"));
            long lid = gameDefService.addLeaderboardDef(gameId, l1);

            RaceDef raceDef = new RaceDef();
            raceDef.setName(timeWindow + " DE Race");
            raceDef.setDisplayName(raceDef.getName());
            raceDef.setLeaderboardId((int) lid);
            raceDef.setFromScope(ScopingType.TEAM.name());
            raceDef.setTop(2);
            raceDef.setTimeWindow(timeWindow);
            Map<Integer, Double> points = new HashMap<>();
            points.put(1, 300.0);
            points.put(2, 100.0);
            raceDef.setRankPoints(points);
            gameDefService.addRace(gameId, raceDef);
        }

        {
            LeaderboardDef l1 = new LeaderboardDef();
            l1.setName("A-n-C-n-E");
            l1.setDisplayName("ACE Leaderboard");
            l1.setOrderBy("desc");
            l1.setIncludeStatePoints(false);
            l1.setRuleIds(Arrays.asList("so.rule.a", "so.rule.c", "so.rule.e"));
            long lid = gameDefService.addLeaderboardDef(gameId, l1);

            RaceDef raceDef = new RaceDef();
            raceDef.setName(timeWindow + " ACE Race");
            raceDef.setDisplayName(raceDef.getName());
            raceDef.setLeaderboardId((int) lid);
            raceDef.setFromScope(ScopingType.TEAM_SCOPE.name());
            raceDef.setTop(10);
            raceDef.setTimeWindow(timeWindow);
            Map<Integer, Double> points = new HashMap<>();
            points.put(1, 100.0);
            points.put(2, 90.0);
            points.put(3, 80.0);
            points.put(4, 70.0);
            points.put(5, 60.0);
            points.put(6, 50.0);
            points.put(7, 40.0);
            points.put(8, 30.0);
            points.put(9, 20.0);
            points.put(10, 10.0);
            raceDef.setRankPoints(points);
            gameDefService.addRace(gameId, raceDef);
        }

        Assert.assertEquals(4, gameDefService.listLeaderboardDefs(gameId).size());
        Assert.assertEquals(3, gameDefService.listRaces(gameId).size());
    }

}
