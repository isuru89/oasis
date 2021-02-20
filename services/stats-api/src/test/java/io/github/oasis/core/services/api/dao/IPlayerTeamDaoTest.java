/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.github.oasis.core.services.api.dao;

import io.github.oasis.core.model.PlayerObject;
import io.github.oasis.core.model.TeamObject;
import io.github.oasis.core.model.UserGender;
import io.github.oasis.core.services.api.dao.configs.OasisEnumArgTypeFactory;
import io.github.oasis.core.services.api.dao.configs.OasisEnumColumnFactory;
import io.github.oasis.core.services.api.dao.dto.PlayerUpdatePart;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.jdbc.DataSourceBuilder;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Isuru Weerarathna
 */
class IPlayerTeamDaoTest {

    private IPlayerTeamDao dao;

    private final PlayerObject playerJill = PlayerObject.builder()
            .displayName("Jill Baker")
            .email("jill@oasis.io")
            .gender(UserGender.MALE)
            .timeZone("Asia/Colombo")
            .avatarRef("https://oasis.io/assets/jill.png")
            .active(true)
            .build();
    private final PlayerObject playerMary = PlayerObject.builder()
            .displayName("Mary Sue")
            .email("mary@oasis.io")
            .gender(UserGender.FEMALE)
            .timeZone("America/Los_Angeles")
            .avatarRef("https://oasis.io/assets/mary.png")
            .active(true)
            .build();
    private final PlayerObject playerZoe = PlayerObject.builder()
            .displayName("Zoe Zanda")
            .email("zoe@oasis.io")
            .gender(UserGender.FEMALE)
            .timeZone("UTC")
            .avatarRef("https://oasis.io/assets/zoe.png")
            .active(true)
            .build();

    private final TeamObject teamWarriors = TeamObject.builder()
            .name("Wuhan Warriors")
            .avatarRef("https://oasis.io/assets/ww.jpeg")
            .gameId(1)
            .active(true)
            .build();
    private final TeamObject teamStrikers = TeamObject.builder()
            .name("Sydney Strikers")
            .avatarRef("https://oasis.io/assets/ss.jpeg")
            .gameId(1)
            .active(true)
            .build();

    @BeforeEach
    void setUp() throws IOException {
        DataSource ds = DataSourceBuilder.create()
                .url("jdbc:sqlite:sample.db")
                .build();
        Jdbi jdbi = Jdbi.create(ds)
                .installPlugin(new SqlObjectPlugin())
                .registerColumnMapper(new OasisEnumColumnFactory())
                .registerArgument(new OasisEnumArgTypeFactory());

        String schemaScript = IOUtils.resourceToString("io/github/oasis/db/schema/schema-sqlite.sql", StandardCharsets.UTF_8,
                Thread.currentThread().getContextClassLoader());
        try (Handle h = jdbi.open()) {
            h.createScript(schemaScript).executeAsSeparateStatements();
        }
        dao = jdbi.onDemand(IPlayerTeamDao.class);
    }

    @AfterEach
    void tearDown() throws IOException {
        File file = new File("sample.db");
        if (file.exists()) {
            FileUtils.forceDelete(file);
        }
    }

    @Test
    void readPlayer() {
        int jillId = dao.insertPlayer(playerJill);

        PlayerObject dbJill = dao.readPlayer(jillId);
        assertPlayerWithDbObject(playerJill, dbJill);
    }

    @Test
    void readNonExistingPlayer() {
        int jillId = dao.insertPlayer(playerJill);

        PlayerObject dbJill = dao.readPlayer(jillId + 100);
        assertNull(dbJill);
    }

    @Test
    void readPlayerByEmail() {
        int jillId = dao.insertPlayer(playerJill);
        int maryId = dao.insertPlayer(playerMary);

        PlayerObject jill = dao.readPlayerByEmail(playerJill.getEmail());
        PlayerObject mary = dao.readPlayerByEmail(playerMary.getEmail());

        System.out.println(jill);
        System.out.println(mary);

        assertEquals(jillId, jill.getId());
        assertEquals(maryId, mary.getId());
    }

    @Test
    void insertPlayer() {
        PlayerObject dbObject = dao.insertAndGet(playerJill);
        System.out.println(dbObject);

        assertPlayerWithDbObject(playerJill, dbObject);

        // should not be able to add same user again with same email
        assertThrows(UnableToExecuteStatementException.class, () -> dao.insertAndGet(playerJill));
    }

    @Test
    void updatePlayer() {
        int maryId = dao.insertPlayer(playerMary);

        PlayerObject maryNew = playerMary.toBuilder().displayName("New Mary Name").build();
        PlayerUpdatePart part = PlayerUpdatePart.from(maryNew);
        dao.updatePlayer(maryId, part);

        PlayerObject dbMary = dao.readPlayer(maryId);
        System.out.println(dbMary);
        assertPlayerWithDbObject(maryNew, dbMary);
    }

    @Test
    void deletePlayer() {
        int jillId = dao.insertPlayer(playerJill);
        int maryId = dao.insertPlayer(playerMary);

        assertTrue(dao.readPlayer(maryId).isActive());
        assertTrue(dao.readPlayer(jillId).isActive());

        dao.deletePlayer(jillId);

        assertFalse(dao.readPlayer(jillId).isActive());
        assertTrue(dao.readPlayer(maryId).isActive());
    }

    @Test
    void insertTeam() {
        int warriorsId = dao.insertTeam(teamWarriors);
        assertTrue(warriorsId > 0);
        int strikersId = dao.insertTeam(teamStrikers);
        assertTrue(strikersId > 0);

        assertTrue(warriorsId != strikersId);

        // should fail if team with same name inserted
        assertThrows(UnableToExecuteStatementException.class, () -> dao.insertTeam(teamStrikers));

        // change name and try it. It should fail
        TeamObject lowerCaseName = teamStrikers.toBuilder().name(teamStrikers.getName().toLowerCase(Locale.ROOT)).build();
        assertThrows(UnableToExecuteStatementException.class, () -> dao.insertTeam(lowerCaseName));
    }

    @Test
    void readTeam() {
        int warriorsId = dao.insertTeam(teamWarriors);
        int strikersId = dao.insertTeam(teamStrikers);

        TeamObject dbStrikersTeam = dao.readTeam(strikersId);
        TeamObject dbWarriorsTeam = dao.readTeam(warriorsId);

        System.out.println(dbStrikersTeam);
        System.out.println(dbWarriorsTeam);

        assertTeamWithDbObject(teamStrikers, dbStrikersTeam);
        assertTeamWithDbObject(teamWarriors, dbWarriorsTeam);
    }

    @Test
    void readTeamByName() {
        dao.insertTeam(teamStrikers);

        TeamObject dbStrikersTeam = dao.readTeamByName(teamStrikers.getName());
        System.out.println(dbStrikersTeam);
        assertNotNull(dbStrikersTeam);
        assertTeamWithDbObject(teamStrikers, dbStrikersTeam);

        // read by all lower case
        TeamObject strikersTemp = dao.readTeamByName(teamStrikers.getName().toLowerCase(Locale.ROOT));
        assertNotNull(strikersTemp);
        assertTeamWithDbObject(teamStrikers, strikersTemp);
    }

    @Test
    void updateTeam() {
        int id = dao.insertTeam(teamStrikers);
        TeamObject teamObject = dao.readTeam(id);
        assertTeamWithDbObject(teamStrikers, teamObject);

        TeamObject teamNew = teamStrikers.toBuilder()
                .avatarRef("https://oasis.io/assets/new-ss.jpeg")
                .colorCode("#f98322")
                .build();

        dao.updateTeam(id, teamNew);
        TeamObject dbTeam = dao.readTeam(id);
        System.out.println(dbTeam);
        assertTeamWithDbObject(teamNew, dbTeam);
        assertTrue(dbTeam.getCreatedAt() != dbTeam.getUpdatedAt());
    }

    @Test
    void readTeamsByName() {
        dao.insertTeam(teamStrikers);
        dao.insertTeam(teamStrikers.toBuilder().name("Sydney Dancers").build());
        dao.insertTeam(teamStrikers.toBuilder().name("Chicago Panthers").build());
        dao.insertTeam(teamStrikers.toBuilder().name("sydney Marines").build());

        {
            List<TeamObject> dbTeams = dao.readTeamsByName("sydney", 0, 10);
            System.out.println(dbTeams);
            assertEquals(3, dbTeams.size());
        }

        {
            // only one result
            List<TeamObject> dbTeams = dao.readTeamsByName("sydney", 0, 1);
            System.out.println(dbTeams);
            assertEquals(1, dbTeams.size());
        }

        {
            // only two results with offset
            List<TeamObject> dbTeams = dao.readTeamsByName("sydney", 1, 2);
            System.out.println(dbTeams);
            assertEquals(2, dbTeams.size());
        }
    }

    @Test
    void insertPlayerToTeam() {
        int jillId = dao.insertPlayer(playerJill);
        int maryId = dao.insertPlayer(playerMary);
        int zoeId = dao.insertPlayer(playerZoe);

        int strikerId = dao.insertTeam(teamStrikers);
        int warriorId = dao.insertTeam(teamWarriors);

        dao.insertPlayerToTeam(1, jillId, strikerId);
        dao.insertPlayerToTeam(1, maryId, strikerId);
        dao.insertPlayerToTeam(1, zoeId, warriorId);

        // should not be able to add jill again to same team
        assertThrows(UnableToExecuteStatementException.class, () -> dao.insertPlayerToTeam(1, jillId, strikerId));
    }

    @Test
    void removePlayerFromTeam() {
        int jillId = dao.insertPlayer(playerJill);
        int maryId = dao.insertPlayer(playerMary);
        int zoeId = dao.insertPlayer(playerZoe);

        int strikerId = dao.insertTeam(teamStrikers);
        int warriorId = dao.insertTeam(teamWarriors);

        dao.insertPlayerToTeam(1, jillId, strikerId);
        dao.insertPlayerToTeam(1, maryId, strikerId);
        dao.insertPlayerToTeam(1, zoeId, warriorId);

        assertEquals(2, dao.readTeamPlayers(strikerId).size());

        dao.removePlayerFromTeam(1, jillId, strikerId);
        assertEquals(1, dao.readTeamPlayers(strikerId).size());

        // same operation is idempotent
        dao.removePlayerFromTeam(1, jillId, strikerId);
        assertEquals(1, dao.readTeamPlayers(strikerId).size());

        // removing from non-existing game/team/player should not fail
        dao.removePlayerFromTeam(9, maryId, strikerId);
        assertEquals(1, dao.readTeamPlayers(strikerId).size());
    }

    @Test
    void readPlayerTeams() {
        int jillId = dao.insertPlayer(playerJill);
        int maryId = dao.insertPlayer(playerMary);
        int zoeId = dao.insertPlayer(playerZoe);

        int strikerId = dao.insertTeam(teamStrikers);
        int warriorId = dao.insertTeam(teamWarriors);

        dao.insertPlayerToTeam(1, jillId, strikerId);
        dao.insertPlayerToTeam(2, jillId, warriorId);
        dao.insertPlayerToTeam(1, maryId, strikerId);
        dao.insertPlayerToTeam(1, zoeId, warriorId);

        List<TeamObject> jillTeams = dao.readPlayerTeams(jillId);
        List<TeamObject> maryTeams = dao.readPlayerTeams(maryId);
        List<TeamObject> zoeTeams = dao.readPlayerTeams(zoeId);
        assertEquals(2, jillTeams.size());
        assertEquals(1, maryTeams.size());
        assertEquals(1, zoeTeams.size());

        Set<String> jillTeamNames = jillTeams.stream().map(TeamObject::getName).collect(Collectors.toSet());
        assertTrue(jillTeamNames.contains(teamStrikers.getName()));
        assertTrue(jillTeamNames.contains(teamWarriors.getName()));

        dao.removePlayerFromTeam(1, jillId, strikerId);
        assertEquals(1, dao.readPlayerTeams(jillId).size());
    }

    @Test
    void readTeamPlayers() {
        int jillId = dao.insertPlayer(playerJill);
        int maryId = dao.insertPlayer(playerMary);
        int zoeId = dao.insertPlayer(playerZoe);

        int strikerId = dao.insertTeam(teamStrikers);
        int warriorId = dao.insertTeam(teamWarriors);

        dao.insertPlayerToTeam(1, jillId, strikerId);
        dao.insertPlayerToTeam(1, maryId, strikerId);
        dao.insertPlayerToTeam(1, zoeId, warriorId);

        List<PlayerObject> strikerPlayers = dao.readTeamPlayers(strikerId);
        System.out.println(strikerPlayers);
        assertEquals(2, strikerPlayers.size());

        Set<String> emailIds = strikerPlayers.stream().map(PlayerObject::getEmail).collect(Collectors.toSet());
        assertTrue(emailIds.contains(playerJill.getEmail()));
        assertTrue(emailIds.contains(playerMary.getEmail()));
        assertFalse(emailIds.contains(playerZoe.getEmail()));
    }

    private void assertPlayerWithDbObject(PlayerObject expected, PlayerObject dbObject) {
        assertTrue(dbObject.getId() > 0);
        assertEquals(expected.getDisplayName(), dbObject.getDisplayName());
        assertEquals(expected.getEmail(), dbObject.getEmail());
        assertEquals(expected.getGender(), dbObject.getGender());
        assertEquals(expected.getTimeZone(), dbObject.getTimeZone());
        assertEquals(expected.getAvatarRef(), dbObject.getAvatarRef());
        assertTrue(dbObject.getCreatedAt() > 0);
        assertTrue(dbObject.getUpdatedAt() > 0);
        assertEquals(expected.isActive(), dbObject.isActive());
    }

    private void assertTeamWithDbObject(TeamObject expected, TeamObject dbObject) {
        assertTrue(dbObject.getId() > 0);
        assertEquals(expected.getName(), dbObject.getName());
        assertEquals(expected.getColorCode(), dbObject.getColorCode());
        assertEquals(expected.getAvatarRef(), dbObject.getAvatarRef());
        assertTrue(dbObject.getCreatedAt() > 0);
        assertTrue(dbObject.getUpdatedAt() > 0);
        assertEquals(expected.isActive(), dbObject.isActive());
    }
}