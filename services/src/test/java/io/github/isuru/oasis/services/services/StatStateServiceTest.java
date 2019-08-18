package io.github.isuru.oasis.services.services;

import io.github.isuru.oasis.model.defs.GameDef;
import io.github.isuru.oasis.services.dto.defs.GameOptionsDto;
import io.github.isuru.oasis.services.dto.stats.UserStateStatDto;
import io.github.isuru.oasis.services.model.UserProfile;
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
