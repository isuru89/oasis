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
import io.github.oasis.services.dto.stats.BadgeBreakdownReqDto;
import io.github.oasis.services.dto.stats.BadgeBreakdownResDto;
import io.github.oasis.services.dto.stats.BadgeRecordDto;
import io.github.oasis.services.dto.stats.BadgeSummaryReq;
import io.github.oasis.services.dto.stats.BadgeSummaryRes;
import io.github.oasis.services.model.TeamProfile;
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
import java.util.Arrays;

public class StatBadgesServiceTest extends WithDataTest {

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

        addBadgeNames(gameId,
                Arrays.asList("so-badge-1", "so-b-sub-gold", "so-b-sub-silver"),
                Arrays.asList("so-badge-2", "so-b-sub-gold", "so-b-sub-silver", "so-b-sub-bronze"),
                Arrays.asList("so-badge-3", "so-b-sub-1", "so-b-sub-2", "so-b-sub-2")
        );

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
    public void testBadgeBreakdown() throws Exception {
        Instant startTime = LocalDateTime.of(2019, 1, 27, 12, 30)
                .atZone(ZoneOffset.UTC)
                .toInstant();
        int count = loadBadges(startTime, 3600L * 24 * 10 * 1000, gameId);

        {
            UserProfile ned = users.get("ned-stark");
            BadgeBreakdownReqDto req = new BadgeBreakdownReqDto();
            req.setUserId(ned.getId());
            BadgeBreakdownResDto resDto = statService.getBadgeBreakdownList(req);
            Assert.assertTrue(resDto.getCount() > 0);
            for (BadgeRecordDto badgeRecordDto : resDto.getRecords()) {
                System.out.println(badgeRecordDto);
            }
        }
    }

    @Test
    public void testBadgeSummary() throws Exception {
        Instant startTime = LocalDateTime.of(2019, 1, 27, 12, 30)
                .atZone(ZoneOffset.UTC)
                .toInstant();
        int count = loadBadges(startTime, 3600L * 24 * 10 * 1000, gameId);

        {
            TeamProfile winterfell = teams.get("winterfell");
            BadgeSummaryReq req = new BadgeSummaryReq();
            req.setTeamId(winterfell.getId().longValue());
            BadgeSummaryRes res = statService.getBadgeSummary(req);
            Assert.assertTrue(res.getCount() > 0);
            for (BadgeSummaryRes.BadgeSummaryRecord record : res.getRecords()) {
                System.out.println(String.format("%d\t%s\t%s\t%d",
                        record.getBadgeId(),
                        record.getBadgeName(),
                        record.getSubBadgeId(),
                        record.getBadgeCount()));
            }

        }
    }

}
