/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.github.oasis.services.services;

import io.github.oasis.model.defs.GameDef;
import io.github.oasis.services.dto.defs.GameOptionsDto;
import io.github.oasis.services.dto.stats.PointBreakdownReqDto;
import io.github.oasis.services.dto.stats.PointBreakdownResDto;
import io.github.oasis.services.dto.stats.PointRecordDto;
import io.github.oasis.services.dto.stats.PointSummaryReq;
import io.github.oasis.services.dto.stats.PointSummaryRes;
import io.github.oasis.services.dto.stats.UserStatDto;
import io.github.oasis.services.model.TeamProfile;
import io.github.oasis.services.model.TeamScope;
import io.github.oasis.services.model.UserProfile;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;

public class StatPointsServiceTest extends WithDataTest {

    private long gameId;

    @Autowired
    private IGameDefService gameDefService;

    @Autowired
    private IStatService statService;


    @Before
    public void before() throws Exception {
        resetSchema();

        GameDef gameDef = new GameDef();
        gameDef.setName("so");
        gameDef.setDisplayName("Stackoverflow Game");
        gameId = gameDefService.createGame(gameDef, new GameOptionsDto());

        pointRuleIds = addPointRules(gameId,"so.rule.a", "so.rule.b", "so.rule.c", "so.rule.d", "so.rule.e");

        // populate dummy data
        loadUserData();

        initPool(5);
        System.out.println(StringUtils.repeat('-', 50));
    }

    @After
    public void after() {
        closePool();
        System.out.println(StringUtils.repeat('-', 50));
    }

    @Test
    public void testPointBreakdown() throws Exception {
        Instant startTime = LocalDateTime.of(2019, 1, 27, 12, 30)
                .atZone(ZoneOffset.UTC)
                .toInstant();
        int count = loadPoints(startTime, 3600L * 24 * 10 * 1000, gameId);

        {
            Iterable<Map<String, Object>> maps = dao.executeRawQuery("SELECT DISTINCT point_id FROM OA_POINT", null);
            for (Map<String, Object> map : maps) {
                System.out.println(map);
            }
        }

        UserProfile ned = users.get("ned-stark");
        {
            // filter all for a single point
            PointBreakdownReqDto dto = new PointBreakdownReqDto();
            dto.setUserId(ned.getId());
            dto.setPointId(pointRuleIds.get(0).intValue());
            System.out.println("User: " + dto.getUserId());
            System.out.println("Point: " + dto.getPointId());
            PointBreakdownResDto res = statService.getPointBreakdownList(dto);
            Assert.assertTrue(res.getCount() > 0);
            for (PointRecordDto row : res.getRecords()) {
                Assert.assertEquals(dto.getPointId(), row.getPointId());
                Assert.assertEquals(dto.getUserId().intValue(), row.getUserId().intValue());
                System.out.println(String.format("%s\t%.2f\t%d", row.getPointName(),
                        row.getPoints(), row.getTs()));
            }
        }

        {
            // filter all point rules
            PointBreakdownReqDto dto = new PointBreakdownReqDto();
            dto.setUserId(ned.getId());
            System.out.println("User: " + dto.getUserId());
            PointBreakdownResDto res = statService.getPointBreakdownList(dto);
            Assert.assertTrue(res.getCount() > 0);
            for (PointRecordDto row : res.getRecords()) {
                Assert.assertEquals(dto.getUserId().intValue(), row.getUserId().intValue());
                System.out.println(String.format("%s\t%.2f\t" + Instant.ofEpochMilli(row.getTs()),
                        row.getPointName(),
                        row.getPoints()));
            }
        }

        {
            long st = LocalDateTime.of(2019, 1,28, 0, 0).atZone(ZoneOffset.UTC).toInstant().toEpochMilli();
            long et = LocalDateTime.of(2019, 1,30, 0, 0).atZone(ZoneOffset.UTC).toInstant().toEpochMilli() - 1;
            // filter for time range
            PointBreakdownReqDto dto = new PointBreakdownReqDto();
            dto.setUserId(ned.getId());
            dto.setRangeStart(st);
            dto.setRangeEnd(et);
            System.out.println("User: " + dto.getUserId());
            System.out.println("Start: " + Instant.ofEpochMilli(dto.getRangeStart()));
            System.out.println("End: " + Instant.ofEpochMilli(dto.getRangeEnd()));
            PointBreakdownResDto res = statService.getPointBreakdownList(dto);
            Assert.assertTrue(res.getCount() > 0);
            for (PointRecordDto row : res.getRecords()) {
                Assert.assertEquals(dto.getUserId().intValue(), row.getUserId().intValue());
                Assert.assertTrue(row.getTs() >= st && row.getTs() < et);
                System.out.println(String.format("%s\t%.2f\t" + Instant.ofEpochMilli(row.getTs()),
                        row.getPointName(),
                        row.getPoints()));
            }
        }

        {
            TeamProfile sunspear = teams.get("sunspear");
            // filter all point rules
            PointBreakdownReqDto dto = new PointBreakdownReqDto();
            dto.setTeamId(sunspear.getId().longValue());
            System.out.println("Team: " + dto.getTeamId());
            PointBreakdownResDto res = statService.getPointBreakdownList(dto);
            Assert.assertTrue(res.getCount() > 0);
            for (PointRecordDto row : res.getRecords()) {
                Assert.assertEquals(dto.getTeamId().intValue(), row.getTeamId().intValue());
                System.out.println(String.format("%s\t%s\t%.2f\t" + Instant.ofEpochMilli(row.getTs()),
                        row.getUserName(),
                        row.getPointName(),
                        row.getPoints()));
            }
        }

        {
            TeamScope dorne = scopes.get("dorne");
            // filter all point rules
            PointBreakdownReqDto dto = new PointBreakdownReqDto();
            dto.setTeamScopeId(dorne.getId().longValue());
            System.out.println("TeamScope: " + dto.getTeamId());
            PointBreakdownResDto res = statService.getPointBreakdownList(dto);
            Assert.assertTrue(res.getCount() > 0);
            for (PointRecordDto row : res.getRecords()) {
                Assert.assertEquals(dto.getTeamScopeId().intValue(), row.getTeamScopeId().intValue());
                System.out.println(String.format("%s\t%s\t%s\t%.2f\t" + Instant.ofEpochMilli(row.getTs()),
                        row.getUserName(),
                        row.getTeamName(),
                        row.getPointName(),
                        row.getPoints()));
            }
        }

    }

    @Test
    public void testPointSummary() throws Exception {
        Instant startTime = LocalDateTime.of(2019, 1, 27, 12, 30)
                .atZone(ZoneOffset.UTC)
                .toInstant();
        int count = loadPoints(startTime, 3600L * 24 * 10 * 1000, gameId);

        {
            // user-wise summary
            UserProfile ned = users.get("ned-stark");
            PointSummaryReq req = new PointSummaryReq();
            req.setUserId(ned.getId());
            PointSummaryRes pointSummary = statService.getPointSummary(req);
            Assert.assertTrue(pointSummary.getCount() > 0);
            System.out.println("Ned Stark - Points Summary");
            System.out.println("PID\tPoints\tCount");
            for (PointSummaryRes.PointSummaryRecord record : pointSummary.getRecords()) {
                Assert.assertEquals(ned.getId(), record.getUserId().longValue());
                Assert.assertNull(record.getTeamId());
                Assert.assertNull(record.getTeamScopeId());
                System.out.println(String.format("%d\t%.2f\t%d",
                        record.getPointId(),
                        record.getTotalPoints(),
                        record.getOccurrences()
                        )
                );
            }
        }

        {
            long st = LocalDateTime.of(2019, 1, 27, 0, 0).atZone(ZoneOffset.UTC).toInstant().toEpochMilli();
            long et = LocalDateTime.of(2019, 1, 30, 0, 0).atZone(ZoneOffset.UTC).toInstant().toEpochMilli();
            // user-wise summary - with range
            UserProfile ned = users.get("ned-stark");
            PointSummaryReq req = new PointSummaryReq();
            req.setUserId(ned.getId());
            req.setRangeStart(st);
            req.setRangeEnd(et);
            PointSummaryRes pointSummary = statService.getPointSummary(req);
            Assert.assertTrue(pointSummary.getCount() > 0);
            System.out.println("Ned Stark - Points Summary (Between " + st + ", " + et + ")");
            System.out.println("PID\tPoints\tCount");
            for (PointSummaryRes.PointSummaryRecord record : pointSummary.getRecords()) {
                Assert.assertEquals(ned.getId(), record.getUserId().longValue());
                Assert.assertNull(record.getTeamId());
                Assert.assertNull(record.getTeamScopeId());
                System.out.println(String.format("%d\t%.2f\t%d",
                        record.getPointId(),
                        record.getTotalPoints(),
                        record.getOccurrences()
                        )
                );
            }
        }

        {
            // team-wise summary
            TeamProfile winterfell = teams.get("winterfell");
            PointSummaryReq req = new PointSummaryReq();
            req.setTeamId(winterfell.getId().longValue());
            PointSummaryRes pointSummary = statService.getPointSummary(req);
            Assert.assertTrue(pointSummary.getCount() > 0);
            System.out.println("Winterfell - Points Summary");
            System.out.println("PID\tPoints\tCount");
            for (PointSummaryRes.PointSummaryRecord record : pointSummary.getRecords()) {
                Assert.assertEquals(winterfell.getId().intValue(), record.getTeamId().intValue());
                Assert.assertNull(record.getUserId());
                Assert.assertNull(record.getTeamScopeId());
                System.out.println(String.format("%d\t%.2f\t%d",
                        record.getPointId(),
                        record.getTotalPoints(),
                        record.getOccurrences()
                        )
                );
            }
        }

        {
            // team-wise summary
            TeamScope riverlands = scopes.get("the-riverlands");
            PointSummaryReq req = new PointSummaryReq();
            req.setTeamScopeId(riverlands.getId().longValue());
            PointSummaryRes pointSummary = statService.getPointSummary(req);
            Assert.assertTrue(pointSummary.getCount() > 0);
            System.out.println("Riverlands - Points Summary");
            System.out.println("PID\tPoints\tCount");
            for (PointSummaryRes.PointSummaryRecord record : pointSummary.getRecords()) {
                Assert.assertEquals(riverlands.getId().intValue(), record.getTeamScopeId().intValue());
                Assert.assertNull(record.getUserId());
                Assert.assertNull(record.getTeamId());
                System.out.println(String.format("%d\t%.2f\t%d",
                        record.getPointId(),
                        record.getTotalPoints(),
                        record.getOccurrences()
                        )
                );
            }
        }
    }

    @Test
    public void testUserStat() throws Exception {
        Instant startTime = LocalDateTime.of(2019, 1, 27, 12, 30)
                .atZone(ZoneOffset.UTC)
                .toInstant();
        loadPoints(startTime, 3600L * 24 * 10 * 1000, gameId);

        {
            UserProfile ned = users.get("ned-stark");
            Instant since = LocalDateTime.of(2019, 1, 30, 12, 30)
                    .atZone(ZoneOffset.UTC)
                    .toInstant();
            UserStatDto userStatDto = statService.readUserGameStats(ned.getId(), since.toEpochMilli());
            System.out.println(userStatDto);
            Assert.assertTrue(userStatDto.getUserId() > 0);
            Assert.assertNotNull(userStatDto.getUserEmail());
            Assert.assertNotNull(userStatDto.getUserName());
        }
    }
}
