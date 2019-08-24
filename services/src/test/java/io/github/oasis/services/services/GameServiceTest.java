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

import io.github.oasis.model.Constants;
import io.github.oasis.model.db.IOasisDao;
import io.github.oasis.model.defs.GameDef;
import io.github.oasis.model.defs.RaceDef;
import io.github.oasis.model.events.EventNames;
import io.github.oasis.model.events.RaceEvent;
import io.github.oasis.services.DataCache;
import io.github.oasis.services.dto.game.BadgeAwardDto;
import io.github.oasis.services.dto.game.PointAwardDto;
import io.github.oasis.services.dto.game.RaceCalculationDto;
import io.github.oasis.services.exception.ApiAuthException;
import io.github.oasis.services.exception.InputValidationException;
import io.github.oasis.services.model.EventSourceToken;
import io.github.oasis.services.model.RaceWinRecord;
import io.github.oasis.services.model.UserRole;
import io.github.oasis.services.model.UserTeam;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class GameServiceTest {

    private static final long ADMIN_USER_ID = 32;
    private static final long GAME_ID = 1L;

    private static final String INT_TOKEN = "abcdefgh";

    private IGameDefService gameDefService;
    private IProfileService profileService;
    private IEventsService eventsService;
    private IOasisDao dao;
    private DataCache dataCache;

    @Before
    public void before() throws Exception {
        gameDefService = Mockito.mock(IGameDefService.class);
        profileService = Mockito.mock(IProfileService.class);
        eventsService = Mockito.mock(IEventsService.class);
        dao = Mockito.mock(IOasisDao.class);
        dataCache = Mockito.mock(DataCache.class);

        GameDef gameDef = new GameDef();
        gameDef.setId(GAME_ID);
        gameDef.setName("so");
        gameDef.setDisplayName("Stackoverflow");
        Mockito.when(gameDefService.readGame(Mockito.eq(GAME_ID))).thenReturn(gameDef);

        Mockito.when(dataCache.getDefGameId()).thenReturn(GAME_ID);
        Mockito.when(dataCache.getAdminUserId()).thenReturn(ADMIN_USER_ID);

        UserTeam admin = createUserTeam(ADMIN_USER_ID, 1, 1, UserRole.ADMIN);
        Mockito.when(profileService.findCurrentTeamOfUser(ADMIN_USER_ID)).thenReturn(admin);

        EventSourceToken token = new EventSourceToken();
        token.setInternal(true);
        token.setToken(INT_TOKEN);
        Mockito.when(dataCache.getInternalEventSourceToken()).thenReturn(token);

        Assert.assertEquals(GAME_ID, dataCache.getDefGameId());
        Assert.assertEquals(ADMIN_USER_ID, dataCache.getAdminUserId());

        Assert.assertNotNull(dataCache.getInternalEventSourceToken());
        Assert.assertEquals(INT_TOKEN, dataCache.getInternalEventSourceToken().getToken());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testAdminAwardBadge() throws Exception {
        IGameService gs = new GameServiceImpl(gameDefService,
                profileService,
                eventsService,
                dao,
                dataCache);

        UserTeam player = createUserTeam(1L, 300, 105, UserRole.PLAYER);
        UserTeam curator = createUserTeam(5L, 300, 101, UserRole.CURATOR);
        Mockito.when(profileService.findCurrentTeamOfUser(1L)).thenReturn(player);
        Mockito.when(profileService.findCurrentTeamOfUser(5L)).thenReturn(curator);

        // admin can award to any player in any team
        {
            gs.awardBadge(ADMIN_USER_ID, awardBadge(1L, 1, GAME_ID));
            ArgumentCaptor<Map> arg = ArgumentCaptor.forClass(Map.class);
            Mockito.verify(eventsService).submitEvent(Mockito.eq(INT_TOKEN), (Map<String, Object>) arg.capture());
            Map<String, Object> map = (Map<String, Object>) arg.getValue();
            Assert.assertEquals(EventNames.OASIS_EVENT_AWARD_BADGE, map.get(Constants.FIELD_EVENT_TYPE));
            Assert.assertEquals(1L, map.get(Constants.FIELD_USER));
            Assert.assertEquals(300L, map.get(Constants.FIELD_SCOPE));
            Assert.assertEquals(105L, map.get(Constants.FIELD_TEAM));
            Assert.assertEquals(GAME_ID, map.get(Constants.FIELD_GAME_ID));
            Assert.assertEquals(1, map.get("badge"));
            Assert.assertNull(map.get("subBadge"));
            Assert.assertEquals(String.valueOf(ADMIN_USER_ID), map.get("tag"));
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testAdminAwardPoints() throws Exception {
        IGameService gs = new GameServiceImpl(gameDefService,
                profileService,
                eventsService,
                dao,
                dataCache);

        UserTeam player = createUserTeam(1L, 300, 105, UserRole.PLAYER);
        UserTeam curator = createUserTeam(5L, 300, 101, UserRole.CURATOR);
        Mockito.when(profileService.findCurrentTeamOfUser(1L)).thenReturn(player);
        Mockito.when(profileService.findCurrentTeamOfUser(5L)).thenReturn(curator);

        // admin can award to any player in any team
        {
            gs.awardPoints(ADMIN_USER_ID, awardPoint(1L, 100.0f, GAME_ID));
            ArgumentCaptor<Map> arg = ArgumentCaptor.forClass(Map.class);
            Mockito.verify(eventsService).submitEvent(Mockito.eq(INT_TOKEN), (Map<String, Object>) arg.capture());
            Map<String, Object> map = (Map<String, Object>) arg.getValue();
            Assert.assertEquals(EventNames.OASIS_EVENT_COMPENSATE_POINTS, map.get(Constants.FIELD_EVENT_TYPE));
            Assert.assertEquals(1L, map.get(Constants.FIELD_USER));
            Assert.assertEquals(300L, map.get(Constants.FIELD_SCOPE));
            Assert.assertEquals(105L, map.get(Constants.FIELD_TEAM));
            Assert.assertEquals(GAME_ID, map.get(Constants.FIELD_GAME_ID));
            Assert.assertEquals(100.0f, map.get("amount"));
            Assert.assertEquals(String.valueOf(ADMIN_USER_ID), map.get("tag"));
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void awardBadgeCuratorToPlayer() throws Exception {
        IGameService gs = new GameServiceImpl(gameDefService,
                profileService,
                eventsService,
                dao,
                dataCache);

        UserTeam player = createUserTeam(1L, 300, 105, UserRole.PLAYER);
        UserTeam curator = createUserTeam(5L, 300, 101, UserRole.CURATOR);
        Mockito.when(profileService.findCurrentTeamOfUser(1L)).thenReturn(player);
        Mockito.when(profileService.findCurrentTeamOfUser(5L)).thenReturn(curator);

        {
            BadgeAwardDto dto = awardBadge(1L, 8, GAME_ID);
            long ts = System.currentTimeMillis();
            dto.setTs(ts);
            dto.setSubBadgeId("sub");
            gs.awardBadge(5L, dto);
            ArgumentCaptor<Map> arg = ArgumentCaptor.forClass(Map.class);
            Mockito.verify(eventsService).submitEvent(Mockito.eq(INT_TOKEN), (Map<String, Object>) arg.capture());
            Map<String, Object> map = (Map<String, Object>) arg.getValue();
            Assert.assertEquals(EventNames.OASIS_EVENT_AWARD_BADGE, map.get(Constants.FIELD_EVENT_TYPE));
            Assert.assertEquals(8, map.get("badge"));
            Assert.assertEquals("sub", map.get("subBadge"));
            Assert.assertEquals(ts, map.get(Constants.FIELD_TIMESTAMP));
            Assert.assertEquals(String.valueOf(5), map.get("tag"));
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void awardPlayerCuratorToPlayer() throws Exception {
        IGameService gs = new GameServiceImpl(gameDefService,
                profileService,
                eventsService,
                dao,
                dataCache);

        UserTeam player = createUserTeam(1L, 300, 105, UserRole.PLAYER);
        UserTeam curator = createUserTeam(5L, 300, 101, UserRole.CURATOR);
        Mockito.when(profileService.findCurrentTeamOfUser(1L)).thenReturn(player);
        Mockito.when(profileService.findCurrentTeamOfUser(5L)).thenReturn(curator);

        {
            PointAwardDto dto = awardPoint(1L, 80.0f, GAME_ID);
            long ts = System.currentTimeMillis();
            dto.setTs(ts);
            gs.awardPoints(5L, dto);
            ArgumentCaptor<Map> arg = ArgumentCaptor.forClass(Map.class);
            Mockito.verify(eventsService).submitEvent(Mockito.eq(INT_TOKEN), (Map<String, Object>) arg.capture());
            Map<String, Object> map = (Map<String, Object>) arg.getValue();
            Assert.assertEquals(EventNames.OASIS_EVENT_COMPENSATE_POINTS, map.get(Constants.FIELD_EVENT_TYPE));
            Assert.assertEquals(80.0f, map.get("amount"));
            Assert.assertEquals(ts, map.get(Constants.FIELD_TIMESTAMP));
            Assert.assertEquals(String.valueOf(5), map.get("tag"));
        }
    }

    @Test
    public void awardBadgesFailure() throws Exception {
        UserTeam user1Team = createUserTeam(1L, 300, 101, UserRole.PLAYER);
        UserTeam user2Team = createUserTeam(2L, 300, 102, UserRole.PLAYER);
        UserTeam user5Team = createUserTeam(5L, 300, 103, UserRole.CURATOR);
        UserTeam user6Team = createUserTeam(6L, 301, 107, UserRole.CURATOR);
        Mockito.when(profileService.findCurrentTeamOfUser(1L)).thenReturn(user1Team);
        Mockito.when(profileService.findCurrentTeamOfUser(2L)).thenReturn(user2Team);
        Mockito.when(profileService.findCurrentTeamOfUser(5L)).thenReturn(user5Team);
        Mockito.when(profileService.findCurrentTeamOfUser(6L)).thenReturn(user6Team);
        Mockito.when(profileService.findCurrentTeamOfUser(9L)).thenReturn(null);
        Mockito.when(profileService.findCurrentTeamOfUser(19L)).thenReturn(null);

        IGameService gs = new GameServiceImpl(gameDefService, profileService, eventsService, dao, dataCache);
        Assertions.assertThrows(InputValidationException.class, () -> gs.awardBadge(0, null));
        Assertions.assertThrows(InputValidationException.class, () -> gs.awardBadge(-1, null));
        Assertions.assertThrows(InputValidationException.class, () ->
                gs.awardBadge(1, awardBadge(1, 2)));
        Assertions.assertThrows(InputValidationException.class, () ->
                gs.awardBadge(1, awardBadge(2, 0)));
        Assertions.assertThrows(InputValidationException.class, () ->
                gs.awardBadge(1, awardBadge(2, -1)));

        // when no association with toUser
        Assertions.assertThrows(InputValidationException.class, () ->
                gs.awardBadge(1, awardBadge(3, 1)));

        // when no association with byUser
        Assertions.assertThrows(InputValidationException.class, () ->
                gs.awardBadge(4, awardBadge(1, 1)));

        // when player-to-player => invalid
        Assertions.assertThrows(ApiAuthException.class, () ->
                gs.awardBadge(1, awardBadge(2, 1)));
        // when player-to-curator => invalid
        Assertions.assertThrows(ApiAuthException.class, () ->
                gs.awardBadge(1, awardBadge(5, 1)));

        // when curator to player in other team => invalid
        Assertions.assertThrows(ApiAuthException.class, () ->
                gs.awardBadge(6, awardBadge(1, 1, GAME_ID)));
    }

    @Test
    public void awardPointsFailure() throws Exception {
        UserTeam user1Team = createUserTeam(1L, 300, 101, UserRole.PLAYER);
        UserTeam user2Team = createUserTeam(2L, 300, 102, UserRole.PLAYER);
        UserTeam user5Team = createUserTeam(5L, 300, 103, UserRole.CURATOR);
        UserTeam user6Team = createUserTeam(6L, 301, 107, UserRole.CURATOR);
        Mockito.when(profileService.findCurrentTeamOfUser(1L)).thenReturn(user1Team);
        Mockito.when(profileService.findCurrentTeamOfUser(2L)).thenReturn(user2Team);
        Mockito.when(profileService.findCurrentTeamOfUser(5L)).thenReturn(user5Team);
        Mockito.when(profileService.findCurrentTeamOfUser(6L)).thenReturn(user6Team);
        Mockito.when(profileService.findCurrentTeamOfUser(9L)).thenReturn(null);
        Mockito.when(profileService.findCurrentTeamOfUser(19L)).thenReturn(null);

        IGameService gs = new GameServiceImpl(gameDefService, profileService, eventsService, dao, dataCache);
        Assertions.assertThrows(InputValidationException.class, () -> gs.awardPoints(0, null));
        Assertions.assertThrows(InputValidationException.class, () -> gs.awardPoints(-1, null));
        Assertions.assertThrows(InputValidationException.class, () ->
                gs.awardPoints(1, awardPoint(1, 200.0f)));
        Assertions.assertThrows(InputValidationException.class, () ->
                gs.awardPoints(1, awardPoint(2, 0.0f)));

        // when no association with toUser
        Assertions.assertThrows(InputValidationException.class, () ->
                gs.awardPoints(1, awardPoint(3, 100.0f)));

        // when no association with byUser
        Assertions.assertThrows(InputValidationException.class, () ->
                gs.awardPoints(4, awardPoint(1, 50.0f)));

        // when player-to-player => invalid
        Assertions.assertThrows(ApiAuthException.class, () ->
                gs.awardPoints(1, awardPoint(2, 200.0f)));
        // when player-to-curator => invalid
        Assertions.assertThrows(ApiAuthException.class, () ->
                gs.awardPoints(1, awardPoint(5, 100.0f)));

        // when curator to player in other team => invalid
        Assertions.assertThrows(ApiAuthException.class, () ->
                gs.awardPoints(6, awardPoint(1, 100.0f, GAME_ID)));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testAddWinners() throws Exception {
        List<RaceWinRecord> winners = new ArrayList<>();
        long raceId = 5;
        long st = System.currentTimeMillis() - 84000L;
        long et = System.currentTimeMillis();

        {
            RaceWinRecord winRecord = new RaceWinRecord();
            winRecord.setUserId(1);
            winRecord.setRank(2);
            winRecord.setRaceId(raceId);
            winRecord.setRaceStartAt(st);
            winRecord.setRaceEndAt(et);
            winRecord.setTeamId(100);
            winRecord.setTeamScopeId(300);
            winRecord.setPoints(84.0);
            winRecord.setTotalCount(17);
            winRecord.setAwardedPoints(50.0);
            winners.add(winRecord);
        }
        {
            RaceWinRecord winRecord = new RaceWinRecord();
            winRecord.setUserId(2);
            winRecord.setRank(1);
            winRecord.setRaceId(raceId);
            winRecord.setRaceStartAt(st);
            winRecord.setRaceEndAt(et);
            winRecord.setTeamId(100);
            winRecord.setTeamScopeId(300);
            winRecord.setPoints(132.0);
            winRecord.setTotalCount(22);
            winRecord.setAwardedPoints(30.0);
            winners.add(winRecord);
        }

        IGameService gameService = new GameServiceImpl(gameDefService,
                profileService, eventsService, dao, dataCache);

        gameService.addRaceWinners(GAME_ID, raceId, winners);
        ArgumentCaptor<List> arg = ArgumentCaptor.forClass(List.class);
        Mockito.verify(eventsService).submitEvents(Mockito.eq(INT_TOKEN), (List<Map<String,Object>>) arg.capture());
        List<Map<String, Object>> list = (List<Map<String, Object>>) arg.getValue();
        Assert.assertEquals(2, list.size());
        {
            Map<String, Object> map = list.get(0);
            Assert.assertEquals(GAME_ID, map.get(Constants.FIELD_GAME_ID));
            Assert.assertEquals(EventNames.OASIS_EVENT_RACE_AWARD, map.get(Constants.FIELD_EVENT_TYPE));
            Assert.assertEquals(1L, map.get(Constants.FIELD_USER));
            Assert.assertEquals(100, map.get(Constants.FIELD_TEAM));
            Assert.assertEquals(300, map.get(Constants.FIELD_SCOPE));

            Assert.assertEquals(raceId, map.get(RaceEvent.KEY_DEF_ID));
            Assert.assertEquals(50.0, map.get(RaceEvent.KEY_POINTS));
            Assert.assertEquals(st, map.get(RaceEvent.KEY_RACE_STARTED_AT));
            Assert.assertEquals(et, map.get(RaceEvent.KEY_RACE_ENDED_AT));
            Assert.assertEquals(2, map.get(RaceEvent.KEY_RACE_RANK));
            Assert.assertEquals(84.0, map.get(RaceEvent.KEY_RACE_SCORE));
            Assert.assertEquals(17L, map.get(RaceEvent.KEY_RACE_SCORE_COUNT));
        }
        {
            Map<String, Object> map = list.get(1);
            Assert.assertEquals(GAME_ID, map.get(Constants.FIELD_GAME_ID));
            Assert.assertEquals(EventNames.OASIS_EVENT_RACE_AWARD, map.get(Constants.FIELD_EVENT_TYPE));
            Assert.assertEquals(2L, map.get(Constants.FIELD_USER));
            Assert.assertEquals(100, map.get(Constants.FIELD_TEAM));
            Assert.assertEquals(300, map.get(Constants.FIELD_SCOPE));

            Assert.assertEquals(raceId, map.get(RaceEvent.KEY_DEF_ID));
            Assert.assertEquals(30.0, map.get(RaceEvent.KEY_POINTS));
            Assert.assertEquals(st, map.get(RaceEvent.KEY_RACE_STARTED_AT));
            Assert.assertEquals(et, map.get(RaceEvent.KEY_RACE_ENDED_AT));
            Assert.assertEquals(1, map.get(RaceEvent.KEY_RACE_RANK));
            Assert.assertEquals(132.0, map.get(RaceEvent.KEY_RACE_SCORE));
            Assert.assertEquals(22L, map.get(RaceEvent.KEY_RACE_SCORE_COUNT));
        }
    }

    @Test
    public void testCalculateWinners() throws Exception {
        List<RaceWinRecord> winners = new ArrayList<>();
        long raceId = 5;
        long st = System.currentTimeMillis() - 84000L;
        long et = System.currentTimeMillis();

        {
            RaceWinRecord winRecord = new RaceWinRecord();
            winRecord.setUserId(1);
            winRecord.setRank(2);
            winRecord.setRaceId(raceId);
            winRecord.setRaceStartAt(st);
            winRecord.setRaceEndAt(et);
            winRecord.setTeamId(100);
            winRecord.setTeamScopeId(300);
            winRecord.setPoints(84.0);
            winRecord.setTotalCount(17);
            winRecord.setAwardedPoints(50.0);
            winners.add(winRecord);
        }
        {
            RaceWinRecord winRecord = new RaceWinRecord();
            winRecord.setUserId(2);
            winRecord.setRank(1);
            winRecord.setRaceId(raceId);
            winRecord.setRaceStartAt(st);
            winRecord.setRaceEndAt(et);
            winRecord.setTeamId(100);
            winRecord.setTeamScopeId(300);
            winRecord.setPoints(132.0);
            winRecord.setTotalCount(22);
            winRecord.setAwardedPoints(30.0);
            winners.add(winRecord);
        }

        IGameService gameService = new GameServiceImpl(gameDefService,
                profileService, eventsService, dao, dataCache);

        RaceCalculationDto calculationDto = new RaceCalculationDto();
        calculationDto.setStartTime(st);
        calculationDto.setEndTime(et);
        Assert.assertTrue(calculationDto.isDoPersist());

        // when no race is defined in advance
        Assertions.assertThrows(InputValidationException.class,
                () -> gameService.calculateRaceWinners(GAME_ID, raceId, calculationDto));

        RaceDef def = new RaceDef();
        def.setId(raceId);
        def.setName("Test Race");
        Mockito.when(gameDefService.listRaces(Mockito.eq(GAME_ID)))
                .thenReturn(Collections.singletonList(def));

        gameService.calculateRaceWinners(GAME_ID, raceId, calculationDto);

    }

    private UserTeam createUserTeam(long userId, int scopeId, int teamId, int userRole) {
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(userId);
        userTeam.setRoleId(userRole);
        userTeam.setTeamId(teamId);
        userTeam.setScopeId(scopeId);
        userTeam.setApproved(true);
        return userTeam;
    }

    private PointAwardDto awardPoint(long toUser, float amount) {
        PointAwardDto dto = new PointAwardDto();
        dto.setToUser(toUser);
        dto.setAmount(amount);
        return dto;
    }

    private PointAwardDto awardPoint(long toUser, float amount, long gameId) {
        PointAwardDto dto = new PointAwardDto();
        dto.setToUser(toUser);
        dto.setAmount(amount);
        dto.setGameId((int) gameId);
        return dto;
    }

    private BadgeAwardDto awardBadge(long toUser, int badgeId) {
        BadgeAwardDto dto = new BadgeAwardDto();
        dto.setToUser(toUser);
        dto.setBadgeId(badgeId);
        return dto;
    }

    private BadgeAwardDto awardBadge(long toUser, int badgeId, long gameId) {
        BadgeAwardDto dto = new BadgeAwardDto();
        dto.setToUser(toUser);
        dto.setBadgeId(badgeId);
        dto.setGameId((int) gameId);
        return dto;
    }

}
