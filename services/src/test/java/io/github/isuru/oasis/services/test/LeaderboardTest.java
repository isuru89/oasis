package io.github.isuru.oasis.services.test;

import io.github.isuru.oasis.model.defs.LeaderboardType;
import io.github.isuru.oasis.services.api.IGameService;
import io.github.isuru.oasis.services.exception.InputValidationException;
import io.github.isuru.oasis.services.model.LeaderboardRequestDto;
import io.github.isuru.oasis.services.model.UserRankRecordDto;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

/**
 * @author iweerarathna
 */
class LeaderboardTest extends AbstractApiTest {

    @Test
    void testReadLeaderboard() throws Exception {
        IGameService gameService = apiService.getGameService();
        LeaderboardRequestDto req1 = new LeaderboardRequestDto(1483920000000L,
                1484524800000L);
        List<UserRankRecordDto> res1 = gameService.readGlobalLeaderboard(req1);

        long ms = LocalDate.of(2017, 1, 10)
                .atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli();
        LeaderboardRequestDto req2 = new LeaderboardRequestDto(LeaderboardType.CURRENT_WEEK, ms);

        Assertions.assertEquals(req1.getRangeStart(), req2.getRangeStart());
        Assertions.assertEquals(req1.getRangeEnd(), req2.getRangeEnd());

        List<UserRankRecordDto> res2 = gameService.readGlobalLeaderboard(req2);

        // res1 and res2 must be same
        Assertions.assertEquals(res1.size(), res2.size());
        UserRankRecordDto f1 = res1.get(0);
        UserRankRecordDto l1 = res1.get(res1.size() - 1);
        UserRankRecordDto f2 = res2.get(0);
        UserRankRecordDto l2 = res2.get(res2.size() - 1);
        Assertions.assertEquals(f1.getRankGlobal(), f2.getRankGlobal());
        Assertions.assertEquals(f1.getTotalPoints(), f2.getTotalPoints());
        Assertions.assertEquals(l1.getRankGlobal(), l2.getRankGlobal());
        Assertions.assertEquals(l1.getTotalPoints(), l2.getTotalPoints());


        // filter top 20 records
        req1.setTopN(20);
        res1 = gameService.readGlobalLeaderboard(req1);

        System.out.println(res1);
        Assertions.assertEquals(20, res1.size());

        req1.setTopN(null);
        req1.setBottomN(30);
        res1 = gameService.readGlobalLeaderboard(req1);

        System.out.println(res1);
        Assertions.assertEquals(30, res1.size());

        req1.setForUser(55L);
        assertFail(() -> gameService.readGlobalLeaderboard(req1), InputValidationException.class);
    }

    @BeforeAll
    static void beforeAnyTest() throws Exception {
        dbStart();
    }

    @AfterAll
    static void afterAnyTest() throws Exception {
        dbClose();
    }

}
