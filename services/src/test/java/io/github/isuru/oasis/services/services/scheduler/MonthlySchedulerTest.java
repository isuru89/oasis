package io.github.isuru.oasis.services.services.scheduler;

import io.github.isuru.oasis.model.defs.RaceDef;
import io.github.isuru.oasis.services.model.RaceWinRecord;
import io.github.isuru.oasis.services.model.TeamProfile;
import io.github.isuru.oasis.services.services.IGameDefService;
import io.github.isuru.oasis.services.services.IGameService;
import io.github.isuru.oasis.services.services.IProfileService;
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
