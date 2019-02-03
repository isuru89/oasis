package io.github.isuru.oasis.services.services;

import io.github.isuru.oasis.model.defs.ChallengeDef;
import io.github.isuru.oasis.model.defs.GameDef;
import io.github.isuru.oasis.services.dto.defs.GameOptionsDto;
import io.github.isuru.oasis.services.dto.stats.ChallengeInfoDto;
import io.github.isuru.oasis.services.dto.stats.ChallengeWinDto;
import io.github.isuru.oasis.services.dto.stats.ChallengeWinnerDto;
import io.github.isuru.oasis.services.dto.stats.UserChallengeWinRes;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
                    userIds.add(winner.getUserId());
                    System.out.println(String.format("%s\t%s\t%s\t%.2f",
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
                    System.out.println(String.format("%d\t%s\t%s\t%.2f",
                            win.getChallengeId(),
                            win.getChallengeName(),
                            win.getChallengeDisplayName(),
                            win.getPointsScored()));
                }
            }
        }
    }

}
