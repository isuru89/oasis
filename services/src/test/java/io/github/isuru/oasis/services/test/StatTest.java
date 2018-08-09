package io.github.isuru.oasis.services.test;

import io.github.isuru.oasis.db.DbProperties;
import io.github.isuru.oasis.db.IOasisDao;
import io.github.isuru.oasis.db.OasisDbFactory;
import io.github.isuru.oasis.db.OasisDbPool;
import io.github.isuru.oasis.services.api.IOasisApiService;
import io.github.isuru.oasis.services.api.IStatService;
import io.github.isuru.oasis.services.api.dto.BadgeBreakdownReqDto;
import io.github.isuru.oasis.services.api.dto.BadgeBreakdownResDto;
import io.github.isuru.oasis.services.api.dto.BadgeRecordDto;
import io.github.isuru.oasis.services.api.dto.PointBreakdownReqDto;
import io.github.isuru.oasis.services.api.dto.PointBreakdownResDto;
import io.github.isuru.oasis.services.api.dto.PointRecordDto;
import io.github.isuru.oasis.services.api.impl.DefaultOasisApiService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

/**
 * @author iweerarathna
 */
class StatTest extends AbstractApiTest {


    private static IOasisDao oasisDao;
    private static IOasisApiService apiService;

    @Test
    void testStat() throws Exception {
        IStatService statService = apiService.getStatService();

        {
            PointBreakdownReqDto reqDto = new PointBreakdownReqDto();
            reqDto.setUserId(55);
            reqDto.setPointId(32);
            PointBreakdownResDto pointBreakdownList = statService.getPointBreakdownList(reqDto);
            Assertions.assertNotNull(pointBreakdownList);
            Assertions.assertTrue(pointBreakdownList.getCount() > 0);
            List<PointRecordDto> records = pointBreakdownList.getRecords();
            for (PointRecordDto recordDto : records) {
                Assertions.assertEquals((int) recordDto.getPointId(), 32);
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
