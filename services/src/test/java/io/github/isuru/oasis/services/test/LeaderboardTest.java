package io.github.isuru.oasis.services.test;

import io.github.isuru.oasis.db.DbProperties;
import io.github.isuru.oasis.db.IOasisDao;
import io.github.isuru.oasis.db.OasisDbFactory;
import io.github.isuru.oasis.db.OasisDbPool;
import io.github.isuru.oasis.services.api.IGameService;
import io.github.isuru.oasis.services.api.IOasisApiService;
import io.github.isuru.oasis.services.api.impl.DefaultOasisApiService;
import io.github.isuru.oasis.services.model.LeaderboardRecordDto;
import io.github.isuru.oasis.services.model.LeaderboardRequestDto;
import io.github.isuru.oasis.services.model.LeaderboardResponseDto;
import io.github.isuru.oasis.services.model.enums.LeaderboardType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.time.LocalDate;
import java.time.ZoneId;

/**
 * @author iweerarathna
 */
class LeaderboardTest extends AbstractApiTest {

    private static IOasisDao oasisDao;
    private static IOasisApiService apiService;

    @Test
    void testReadLeaderboard() throws Exception {
        IGameService gameService = apiService.getGameService();
        LeaderboardRequestDto req1 = new LeaderboardRequestDto(1483920000000L,
                1484524800000L);
        LeaderboardResponseDto res1 = gameService.readLeaderboardStatus(req1);

        long ms = LocalDate.of(2017, 1, 10)
                .atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli();
        LeaderboardRequestDto req2 = new LeaderboardRequestDto(LeaderboardType.CURRENT_WEEK, ms);

        Assertions.assertEquals(req1.getRangeStart(), req2.getRangeStart());
        Assertions.assertEquals(req1.getRangeEnd(), req2.getRangeEnd());

        LeaderboardResponseDto res2 = gameService.readLeaderboardStatus(req2);

        // res1 and res2 must be same
        Assertions.assertEquals(res1.getRankings().size(), res2.getRankings().size());
        LeaderboardRecordDto f1 = res1.getRankings().get(0);
        LeaderboardRecordDto l1 = res1.getRankings().get(res1.getRankings().size() - 1);
        LeaderboardRecordDto f2 = res2.getRankings().get(0);
        LeaderboardRecordDto l2 = res2.getRankings().get(res2.getRankings().size() - 1);
        Assertions.assertEquals(f1.getRank(), f2.getRank());
        Assertions.assertEquals(f1.getTotalPoints(), f2.getTotalPoints());
        Assertions.assertEquals(l1.getRank(), l2.getRank());
        Assertions.assertEquals(l1.getTotalPoints(), l2.getTotalPoints());


        // filter top 20 records
        req1.setTopN(20);
        res1 = gameService.readLeaderboardStatus(req1);

        System.out.println(res1.getRankings());
        Assertions.assertEquals(20, res1.getRankings().size());

        req1.setTopN(null);
        req1.setBottomN(30);
        res1 = gameService.readLeaderboardStatus(req1);

        System.out.println(res1.getRankings());
        Assertions.assertEquals(30, res1.getRankings().size());
    }

    @BeforeAll
    static void beforeAnyTest() throws Exception {
        DbProperties properties = new DbProperties(OasisDbPool.DEFAULT);
        properties.setUrl("jdbc:mysql://localhost/oasis");
        properties.setUsername("isuru");
        properties.setPassword("isuru");
        File file = new File("./scripts/db");
        if (!file.exists()) {
            file = new File("../scripts/db");
            if (!file.exists()) {
                Assertions.fail("Database scripts directory is not found!");
            }
        }
        properties.setQueryLocation(file.getAbsolutePath());

        oasisDao = OasisDbFactory.create(properties);
        apiService = new DefaultOasisApiService(oasisDao, null);
    }

    @AfterAll
    static void afterAnyTest() throws Exception {
        System.out.println("Shutting down db connection.");
        oasisDao.close();
        apiService = null;
    }

}
