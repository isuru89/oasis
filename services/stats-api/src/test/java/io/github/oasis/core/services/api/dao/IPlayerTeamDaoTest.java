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
import io.github.oasis.core.model.UserGender;
import io.github.oasis.core.services.api.dao.configs.OasisEnumArgTypeFactory;
import io.github.oasis.core.services.api.dao.configs.OasisEnumColumnFactory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.jdbc.DataSourceBuilder;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Isuru Weerarathna
 */
class IPlayerTeamDaoTest {

    private IPlayerTeamDao dao;

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
    }

    @Test
    void readPlayerByEmail() {
    }

    @Test
    void insertPlayer() {
        PlayerObject player = PlayerObject.builder()
                .displayName("Jill Baker")
                .email("jill@oasis.io")
                .gender(UserGender.FEMALE)
                .timeZone("Asia/Colombo")
                .avatarRef("https://oasis.io/assets/jill.png")
                .build();

        PlayerObject dbObject = dao.insertAndGet(player);
        System.out.println(dbObject);

        assertTrue(dbObject.getId() > 0);
        assertEquals(player.getDisplayName(), dbObject.getDisplayName());
        assertEquals(player.getEmail(), dbObject.getEmail());
        assertEquals(player.getGender(), dbObject.getGender());
        assertEquals(player.getTimeZone(), dbObject.getTimeZone());
        assertEquals(player.getAvatarRef(), dbObject.getAvatarRef());

        // should not be able to add same user again with same email
        Assertions.assertThrows(UnableToExecuteStatementException.class, () -> {
            dao.insertAndGet(player);
        });
    }

    @Test
    void updatePlayer() {
    }

    @Test
    void deletePlayer() {
    }

    @Test
    void insertTeam() {
    }

    @Test
    void readTeam() {
    }

    @Test
    void readTeamByName() {
    }

    @Test
    void updateTeam() {
    }

    @Test
    void readTeamsByName() {
    }

    @Test
    void insertPlayerToTeam() {
    }

    @Test
    void removePlayerFromTeam() {
    }

    @Test
    void readPlayerTeams() {
    }

    @Test
    void readTeamPlayers() {
    }
}