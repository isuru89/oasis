package io.github.isuru.oasis.services.services;

import io.github.isuru.oasis.model.defs.GameDef;
import io.github.isuru.oasis.services.dto.defs.GameOptionsDto;
import io.github.isuru.oasis.services.dto.stats.UserMilestoneStatDto;
import io.github.isuru.oasis.services.model.UserProfile;
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

        initPool(5);
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
                System.out.println(String.format("%d\t%s\t%s\t%d\t%d\t%d\t%s",
                        dto.getMilestoneId(),
                        dto.getMilestoneName(),
                        dto.getMilestoneDisplayName(),
                        dto.getCurrentLevel(),
                        dto.getCurrentValueL(),
                        dto.getNextValueL(),
                        Instant.ofEpochMilli(dto.getAchievedTime())));
            }
        }

    }
}
