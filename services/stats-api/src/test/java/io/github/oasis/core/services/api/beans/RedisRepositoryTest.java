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
import io.github.oasis.core.UserMetadata;
import io.github.oasis.core.configs.OasisConfigs;
import io.github.oasis.core.exception.OasisRuntimeException;
import io.github.oasis.core.external.Db;
import io.github.oasis.core.external.DbContext;
import io.github.oasis.core.external.PaginatedResult;
import io.github.oasis.core.model.TeamObject;
import io.github.oasis.core.model.UserObject;
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
import java.util.Map;

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
        long userId = redisRepository.addUser(createUser("john@oasis.com", "John Doe")).getUserId();
        long user2Id = redisRepository.addUser(createUser("alice@oasis.com", "Alice Lena")).getUserId();

        UserObject userById = redisRepository.readUser(userId);
        UserObject userByEmail = redisRepository.readUser("john@oasis.com");
        Assertions.assertEquals(userById.getUserId(), userId);
        Assertions.assertEquals(userById.getUserId(), userByEmail.getUserId());
        Assertions.assertEquals(userById.getEmail(), userByEmail.getEmail());
        Assertions.assertEquals(userById.getDisplayName(), userByEmail.getDisplayName());

        // read metadata
        UserMetadata userMetadata = redisRepository.readUserMetadata(userId);
        Assertions.assertEquals(userById.getDisplayName(), userMetadata.getDisplayName());
        Assertions.assertEquals(userById.getUserId(), userMetadata.getUserId());
        Assertions.assertEquals(userById.getDisplayName(), redisRepository.readUserMetadata(String.valueOf(userId)).getDisplayName());

        Map<Long, UserMetadata> userMap = redisRepository.readUsersByIds(List.of(userId, user2Id));
        Assertions.assertEquals(2, userMap.size());

        // non existing user
        assertError(() -> redisRepository.readUser(999));
        // non-existing email
        assertError(() -> redisRepository.readUser("unknown@oasis.com"));
    }

    @Test
    void addUser() {
        long userId = redisRepository.addUser(createUser("john@oasis.com", "John Doe")).getUserId();

        // no duplicate emails
        assertError(() -> redisRepository.addUser(createUser("john@oasis.com", "Other John")));

        long user2Id = redisRepository.addUser(createUser("alice@oasis.com", "Alice Lena")).getUserId();
        Assertions.assertEquals(user2Id, userId + 1);
    }

    @Test
    void updateUser() {
        long userId = redisRepository.addUser(createUser("john@oasis.com", "John Doe")).getUserId();
        UserObject userObject = redisRepository.readUser(userId);
        Assertions.assertNotNull(userObject);
        Assertions.assertEquals("john@oasis.com", userObject.getEmail());
        Assertions.assertEquals("John Doe", userObject.getDisplayName());

        UserMetadata userMetadata = redisRepository.readUserMetadata(userId);
        Assertions.assertEquals("John Doe", userMetadata.getDisplayName());

        // changing email and try to save should fail
        userObject.setEmail("john1@oasis.com");
        assertError(() -> redisRepository.updateUser(userId, userObject));

        // impersonating users should fail
        redisRepository.addUser(createUser("bob@oasis.com", "Bob Hopkins"));
        userObject.setEmail("bob@oasis.com");
        assertError(() -> redisRepository.updateUser(userId, userObject));

        userObject.setEmail("john@oasis.com");
        userObject.setDisplayName("John Changed");
        redisRepository.updateUser(userId, userObject);
        Assertions.assertEquals("John Changed", redisRepository.readUser(userId).getDisplayName());

        // metadata should also change
        Assertions.assertEquals("John Changed", redisRepository.readUserMetadata(userId).getDisplayName());
    }

    @Test
    void deleteUser() {
        long userId = redisRepository.addUser(createUser("john@oasis.com", "John Doe")).getUserId();
        Assertions.assertTrue(redisRepository.existsUser("john@oasis.com"));

        UserObject userObject = redisRepository.deleteUser(userId);
        Assertions.assertEquals("John Doe", userObject.getDisplayName());

        Assertions.assertFalse(redisRepository.existsUser("john@oasis.com"));

        UserMetadata userMetadata = redisRepository.readUserMetadata(userId);
        Assertions.assertNull(userMetadata.getDisplayName());
    }

    @Test
    void addTeam() {
        Integer teamId = redisRepository.addTeam(createTeam("Warriors")).getTeamId();
        Integer team2Id = redisRepository.addTeam(createTeam("Avengers")).getTeamId();
        Assertions.assertTrue(teamId > 0);
        Assertions.assertEquals(team2Id, teamId + 1);

        // should not be able to add same team again
        assertError(() -> redisRepository.addTeam(createTeam("Warriors")));
        assertError(() -> redisRepository.addTeam(createTeam("warriors")));
    }

    @Test
    void readTeam() {
        Integer teamId = redisRepository.addTeam(createTeam("Warriors")).getTeamId();
        Assertions.assertEquals("Warriors", redisRepository.readTeam(teamId).getName());

        // non-exist team id
        assertError(() -> redisRepository.readTeam(999));
    }

    @Test
    void readTeamsByIdStrings() {
        Integer teamId = redisRepository.addTeam(createTeam("Warriors")).getTeamId();
        Integer team2Id = redisRepository.addTeam(createTeam("Avengers")).getTeamId();

        // read metadata
        Map<Integer, TeamMetadata> teamMap = redisRepository.readTeamsById(List.of(teamId, team2Id));
        Assertions.assertEquals(2, teamMap.size());
        Assertions.assertEquals("Warriors", teamMap.get(teamId).getName());
        Assertions.assertEquals("Avengers", teamMap.get(team2Id).getName());
    }

    @Test
    void readTeamsById() {
        Integer teamId = redisRepository.addTeam(createTeam("Warriors")).getTeamId();
        Integer team2Id = redisRepository.addTeam(createTeam("Avengers")).getTeamId();

        // read metadata
        Map<String, TeamMetadata> teamMap = redisRepository.readTeamsByIdStrings(List.of(String.valueOf(teamId), String.valueOf(team2Id)));
        Assertions.assertEquals(2, teamMap.size());
        Assertions.assertEquals("Warriors", teamMap.get(String.valueOf(teamId)).getName());
        Assertions.assertEquals("Avengers", teamMap.get(String.valueOf(team2Id)).getName());
    }

    @Test
    void readTeamMetadata() {
    }

    @Test
    void testReadTeamMetadata() {
    }

    @Test
    void updateTeam() {
        Integer teamId = redisRepository.addTeam(createTeam("Warriors")).getTeamId();
        TeamObject teamObject = redisRepository.readTeam(teamId);
        Assertions.assertEquals("Warriors", teamObject.getName());

        Assertions.assertTrue(redisRepository.existTeam("warriors"));

        teamObject.setName("New Warriors");
        redisRepository.updateTeam(teamId, teamObject);

        teamObject = redisRepository.readTeam(teamId);
        Assertions.assertEquals("New Warriors", teamObject.getName());

        Assertions.assertFalse(redisRepository.existTeam("warriors"));
        Assertions.assertTrue(redisRepository.existTeam("New warriors"));
    }

    @Test
    void existTeam() {
        redisRepository.addTeam(createTeam("Warriors"));

        Assertions.assertTrue(redisRepository.existTeam("Warriors"));
        Assertions.assertTrue(redisRepository.existTeam("warriors"));
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
        long u1 = redisRepository.addUser(createUser("john@oasis.com", "John Doe")).getUserId();
        long u2 = redisRepository.addUser(createUser("alice@oasis.com", "Alice Lee")).getUserId();
        long u3 = redisRepository.addUser(createUser("bob@oasis.com", "Bob Hopkins")).getUserId();

        int t1 = redisRepository.addTeam(createTeam("sunrise warrior")).getTeamId();
        int t2 = redisRepository.addTeam(createTeam("musketeers")).getTeamId();
        int t3 = redisRepository.addTeam(createTeam("avengers")).getTeamId();

        redisRepository.addUserToTeam(u1, t1);
        redisRepository.addUserToTeam(u1, t2);

        List<TeamObject> userTeams = redisRepository.getUserTeams(u1);
        Assertions.assertEquals(2, userTeams.size());
        Assertions.assertTrue(userTeams.stream().anyMatch(t -> t.getName().equals("sunrise warrior")));
        Assertions.assertTrue(userTeams.stream().anyMatch(t -> t.getName().equals("musketeers")));
        Assertions.assertFalse(userTeams.stream().anyMatch(t -> t.getName().equals("avengers")));

        Assertions.assertTrue(redisRepository.getUserTeams(u2).isEmpty());

        // add user to same team
        redisRepository.addUserToTeam(u1, t2);
        Assertions.assertEquals(2, redisRepository.getUserTeams(u1).size());
    }

    @Test
    void getUserTeams() {
    }

    @Test
    void getTeamUsers() {
        long u1 = redisRepository.addUser(createUser("john@oasis.com", "John Doe")).getUserId();
        long u2 = redisRepository.addUser(createUser("alice@oasis.com", "Alice Lee")).getUserId();
        long u3 = redisRepository.addUser(createUser("bob@oasis.com", "Bob Hopkins")).getUserId();

        int t1 = redisRepository.addTeam(createTeam("sunrise warrior")).getTeamId();
        int t2 = redisRepository.addTeam(createTeam("musketeers")).getTeamId();
        int t3 = redisRepository.addTeam(createTeam("avengers")).getTeamId();

        redisRepository.addUserToTeam(u1, t1);
        redisRepository.addUserToTeam(u1, t2);
        redisRepository.addUserToTeam(u3, t2);

        List<UserObject> teamUsers = redisRepository.getTeamUsers(t2);
        Assertions.assertEquals(2, teamUsers.size());
        Assertions.assertTrue(teamUsers.stream().anyMatch(u -> u.getEmail().equals("john@oasis.com")));
        Assertions.assertTrue(teamUsers.stream().anyMatch(u -> u.getEmail().equals("bob@oasis.com")));
        Assertions.assertFalse(teamUsers.stream().anyMatch(u -> u.getEmail().equals("alice@oasis.com")));

        // no users team
        Assertions.assertTrue(redisRepository.getTeamUsers(t3).isEmpty());

        // non-existing team
        assertError(() -> redisRepository.getTeamUsers(999));
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
    void readElementDefinition() {
    }

    @Test
    void readElementDefinitions() {
    }

    @Test
    void readAttributeInfo() {
    }

    @Test
    void addAttribute() {
    }

    @Test
    void listAllAttributes() {
    }

    private UserObject createUser(String email, String name) {
        UserObject userObject = new UserObject();
        userObject.setDisplayName(name);
        userObject.setEmail(email);
        return userObject;
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