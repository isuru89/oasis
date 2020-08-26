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

package io.github.oasis.core.services.api.beans;

import com.google.gson.Gson;
import io.github.oasis.core.Game;
import io.github.oasis.core.TeamMetadata;
import io.github.oasis.core.configs.OasisConfigs;
import io.github.oasis.core.exception.OasisRuntimeException;
import io.github.oasis.core.external.Db;
import io.github.oasis.core.external.DbContext;
import io.github.oasis.core.external.PaginatedResult;
import io.github.oasis.core.model.TeamObject;
import io.github.oasis.core.services.api.configs.SerializingConfigs;
import io.github.oasis.db.redis.RedisDb;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.io.IOException;
import java.util.List;

/**
 * @author Isuru Weerarathna
 */
class RedisRepositoryTest {

    private static RedisRepository redisRepository;
    private static Db dbPool;

    @BeforeAll
    public static void beforeAll() {
        RedisDb redisDb = RedisDb.create(OasisConfigs.defaultConfigs());
        redisDb.init();
        dbPool = redisDb;
        Gson gson = new SerializingConfigs().createSerializer();
        redisRepository = new RedisRepository(redisDb, new GsonSerializer(gson));
    }

    @BeforeEach
    public void beforeEach() throws IOException {
        try (DbContext db = dbPool.createContext()) {
            db.allKeys("*").forEach(db::removeKey);
        }
    }

    @Test
    @DisplayName("Game: Add")
    void addNewGame() {
        Game game = createGame("game1");
        int gameId = redisRepository.addNewGame(game).getId();
        assertError(() -> redisRepository.addNewGame(game));

        game.setName("game2");
        int game2Id = redisRepository.addNewGame(game).getId();
        Assertions.assertEquals(gameId + 1, game2Id);
    }

    @Test
    @DisplayName("Game: Update")
    void updateGame() {
        Game game = createGame("game1");
        int gameId = redisRepository.addNewGame(game).getId();

        game.setName("gamenameupdated");
        redisRepository.updateGame(gameId, game);
        Game updatedGame = redisRepository.readGame(gameId);
        Assertions.assertEquals(game.getName(), updatedGame.getName());

        // ID cannot update
        assertError(() -> redisRepository.updateGame(123, game));
        game.setId(123);
        assertError(() -> redisRepository.updateGame(1, game));
    }

    @Test
    @DisplayName("Game: Read")
    void readGame() {
        Integer gameId = redisRepository.addNewGame(createGame("game1")).getId();
        Game game = redisRepository.readGame(gameId);
        Assertions.assertNotNull(game);
        Assertions.assertNotNull(game.getId());
        Assertions.assertNotNull(game.getName());
        Assertions.assertNotNull(game.getDescription());
        Assertions.assertNotNull(game.getMotto());

        // read non-existing game
        assertError(() -> redisRepository.readGame(999));
    }

    @Test
    @DisplayName("Game: Delete")
    void deleteGame() {
        Game oldGame = createGame("game1");
        Integer gameId = redisRepository.addNewGame(oldGame).getId();
        Game game = redisRepository.deleteGame(gameId);
        Assertions.assertEquals(oldGame.getId(), game.getId());
        Assertions.assertEquals(oldGame.getName(), game.getName());

        // cannot delete again
        assertError(() -> redisRepository.deleteGame(gameId));

        // should be able to add prev game again
        redisRepository.addNewGame(oldGame);

        // deleting non existing game
        assertError(() -> redisRepository.deleteGame(999));
    }

    @Test
    @DisplayName("Game: List All")
    void listGames() {
        redisRepository.addNewGame(createGame("game-apex-1"));
        redisRepository.addNewGame(createGame("game-apex-2"));
        redisRepository.addNewGame(createGame("game-apex-3"));
        redisRepository.addNewGame(createGame("helloworld"));
        redisRepository.addNewGame(createGame("olympic"));
        redisRepository.addNewGame(createGame("commonwealth"));

        List<Game> games = redisRepository.listGames();
        Assertions.assertEquals(6, games.size());
    }

    @Test
    void readUsersByIdStrings() {
    }

    @Test
    void readUsersByIds() {
    }

    @Test
    void readUserMetadata() {
    }

    @Test
    void testReadUserMetadata() {
    }

    @Test
    void readUser() {
    }

    @Test
    void testReadUser() {
    }

    @Test
    void readTeamsByIdStrings() {
    }

    @Test
    void readTeamsById() {
    }

    @Test
    void readTeamMetadata() {
    }

    @Test
    void testReadTeamMetadata() {
    }

    @Test
    void addUser() {
    }

    @Test
    void updateUser() {
    }

    @Test
    void deleteUser() {
    }

    @Test
    void addTeam() {
    }

    @Test
    void readTeam() {
    }

    @Test
    void readElementDefinition() {
    }

    @Test
    void readElementDefinitions() {
    }

    @Test
    void readAttributeInfo() {
    }

    @Test
    void updateTeam() {
    }

    @Test
    void existTeam() {
    }

    @Test
    void searchTeam() {
        redisRepository.addTeam(createTeam("wildfire"));
        redisRepository.addTeam(createTeam("t-apex-1"));
        redisRepository.addTeam(createTeam("t-apex-2"));
        redisRepository.addTeam(createTeam("sunrise warrior"));
        redisRepository.addTeam(createTeam("musketeers"));
        redisRepository.addTeam(createTeam("avengers"));

        PaginatedResult<TeamMetadata> result = redisRepository.searchTeam("apex", null, 10);
        Assertions.assertTrue(result.isCompleted());
        Assertions.assertNull(result.getNextCursor());
        Assertions.assertEquals(2, result.getRecords().size());
        System.out.println(result.getRecords());

        result = redisRepository.searchTeam("r", null, 2);
        Assertions.assertEquals(4, result.getRecords().size());
    }

    @Test
    void removeUserFromTeam() {
    }

    @Test
    void addUserToTeam() {
    }

    @Test
    void getUserTeams() {
    }

    @Test
    void getTeamUsers() {
    }

    @Test
    void addNewElement() {
    }

    @Test
    void updateElement() {
    }

    @Test
    void deleteElement() {
    }

    @Test
    void readElement() {
    }

    @Test
    void addAttribute() {
    }

    @Test
    void listAllAttributes() {
    }

    private TeamObject createTeam(String name) {
        TeamObject team = new TeamObject();
        team.setName(name);
        return team;
    }

    private Game createGame(String name) {
        Game game = new Game();
        game.setName(name);
        game.setDescription("Test Game");
        game.setMotto("All the way!");
        return game;
    }

    private void assertError(Executable executable) {
        try {
            executable.execute();
        } catch (Throwable throwable) {
            System.out.println("Error message: " + throwable.getMessage());
            if (!(throwable instanceof OasisRuntimeException)) {
                Assertions.fail("Expected to have oasis specific exception, but received " + throwable.getClass().getSimpleName());
            }
            return;
        }
        Assertions.fail("Should not expected to success!");
    }

    @AfterAll
    public static void afterAll() {

    }
}