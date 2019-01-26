package io.github.isuru.oasis.services.test;

import io.github.isuru.oasis.services.dto.stats.BadgeBreakdownReqDto;
import io.github.isuru.oasis.services.dto.stats.BadgeBreakdownResDto;
import io.github.isuru.oasis.services.dto.stats.BadgeRecordDto;
import io.github.isuru.oasis.services.dto.stats.PointBreakdownReqDto;
import io.github.isuru.oasis.services.dto.stats.PointBreakdownResDto;
import io.github.isuru.oasis.services.dto.stats.PointRecordDto;
import io.github.isuru.oasis.services.dto.stats.TeamHistoryRecordDto;
import io.github.isuru.oasis.services.dto.stats.UserBadgeStatDto;
import io.github.isuru.oasis.services.dto.stats.UserMilestoneStatDto;
import io.github.isuru.oasis.services.dto.stats.UserStatDto;
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

        List<UserBadgeStatDto> userBadgeStatDtos = statService.readUserBadges(1763, 0);
        System.out.println(userBadgeStatDtos);
        int totalBadges = userBadgeStatDtos.stream().mapToInt(UserBadgeStatDto::getBadgeCount).sum();
        Assertions.assertEquals(183, totalBadges);

        List<TeamHistoryRecordDto> teamHistoryRecordDtos = statService.readUserTeamHistoryStat(145);
        Assertions.assertEquals(1, teamHistoryRecordDtos.size());

//        List<UserRankRecordDto> userRankRecordDtos = statService.readUserTeamRankings(55, true);
//        System.out.println(userRankRecordDtos);
    }

    @Test
    void testStat() throws Exception {
        IStatService statService = apiService.getStatService();

        {
            PointBreakdownReqDto reqDto = new PointBreakdownReqDto();
            reqDto.setUserId(55);
            reqDto.setPointId(12);
            PointBreakdownResDto pointBreakdownList = statService.getPointBreakdownList(reqDto);
            Assertions.assertNotNull(pointBreakdownList);
            Assertions.assertTrue(pointBreakdownList.getCount() > 0);
            List<PointRecordDto> records = pointBreakdownList.getRecords();
            for (PointRecordDto recordDto : records) {
                Assertions.assertEquals((int) recordDto.getPointId(), 12);
                Assertions.assertEquals((int) recordDto.getUserId(), 55);
            }
            System.out.println(records.size());
        }

        {
            BadgeBreakdownReqDto breakdownReqDto = new BadgeBreakdownReqDto();
            breakdownReqDto.setUserId(145);
            BadgeBreakdownResDto badgeBreakdownList = statService.getBadgeBreakdownList(breakdownReqDto);
            Assertions.assertNotNull(badgeBreakdownList);
            Assertions.assertTrue(badgeBreakdownList.getCount() > 0);
            List<BadgeRecordDto> records = badgeBreakdownList.getRecords();
            for (BadgeRecordDto record : records) {
                Assertions.assertEquals(145, (int) record.getUserId());
            }
            System.out.println(records.size());

            int n = records.size();
            int half = n / 2;
            int tq = (2 * n) / 3;
            int expect = tq - half;
            breakdownReqDto = new BadgeBreakdownReqDto();
            breakdownReqDto.setUserId(145);
            breakdownReqDto.setRangeEnd(records.get(half).getTs());
            breakdownReqDto.setRangeStart(records.get(tq).getTs());
            System.out.println(breakdownReqDto.getRangeStart());
            System.out.println(breakdownReqDto.getRangeEnd());

            badgeBreakdownList = statService.getBadgeBreakdownList(breakdownReqDto);
            Assertions.assertNotNull(badgeBreakdownList);
            Assertions.assertEquals(expect, badgeBreakdownList.getCount());
        }
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
