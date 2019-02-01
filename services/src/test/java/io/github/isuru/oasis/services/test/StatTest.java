package io.github.isuru.oasis.services.test;

import io.github.isuru.oasis.services.dto.stats.*;
import io.github.isuru.oasis.services.model.PurchasedItem;
import io.github.isuru.oasis.services.services.IStatService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * @author iweerarathna
 */
class StatTest extends AbstractApiTest {

    @Test
    void testGameSummaryStat() throws Exception {
        IStatService statService = apiService.getStatService();

        UserStatDto dto = statService.readUserGameStats(145, 0);
        Assertions.assertEquals(145, dto.getUserId());
        Assertions.assertTrue(dto.getTotalPoints() > 0);
        Assertions.assertTrue(dto.getTotalBadges() > 0);
        System.out.println(dto);

        List<UserMilestoneStatDto> msStats = statService.readUserMilestones(54);
        Assertions.assertEquals(3, msStats.size());

        List<PurchasedItem> purchasedItems = statService.readUserPurchasedItems(54, 0);
        Assertions.assertEquals(0, purchasedItems.size());

//        List<UserBadgeStatDto> userBadgeStatDtos = statService.readUserBadges(1763, 0);
//        System.out.println(userBadgeStatDtos);
//        int totalBadges = userBadgeStatDtos.stream().mapToInt(UserBadgeStatDto::getBadgeCount).sum();
//        Assertions.assertEquals(183, totalBadges);
//
//        List<TeamHistoryRecordDto> teamHistoryRecordDtos = statService.readUserTeamHistoryStat(145);
//        Assertions.assertEquals(1, teamHistoryRecordDtos.size());

//        List<UserRankRecordDto> userRankRecordDtos = statService.readUserTeamRankings(55, true);
//        System.out.println(userRankRecordDtos);
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
