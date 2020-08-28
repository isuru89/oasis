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
import io.github.oasis.core.elements.AttributeInfo;
import io.github.oasis.core.elements.ElementDef;
import io.github.oasis.core.elements.SimpleElementDefinition;
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
import java.util.HashMap;
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
        long userId = redisRepository.addUser(createUser("john@oasis.io", "John Doe")).getUserId();
        long user2Id = redisRepository.addUser(createUser("alice@oasis.io", "Alice Lena")).getUserId();
        long user3Id = redisRepository.addUser(createUser("bob@oasis.io", "Bob Hopkins")).getUserId();

        Map<String, UserMetadata> userMap = redisRepository.readUsersByIdStrings(List.of(String.valueOf(userId), String.valueOf(user2Id)));
        Assertions.assertEquals(2, userMap.size());
        Assertions.assertTrue(userMap.containsKey(String.valueOf(userId)));
        Assertions.assertTrue(userMap.containsKey(String.valueOf(user2Id)));

        userMap = redisRepository.readUsersByIdStrings(List.of(String.valueOf(user3Id), "999"));
        Assertions.assertEquals(1, userMap.size());
        Assertions.assertTrue(userMap.containsKey(String.valueOf(user3Id)));
        Assertions.assertFalse(userMap.containsKey("999"));

        Assertions.assertEquals(0, redisRepository.readUsersByIdStrings(List.of()).size());
    }

    @Test
    void readUser() {
        long userId = redisRepository.addUser(createUser("john@oasis.io", "John Doe")).getUserId();
        long user2Id = redisRepository.addUser(createUser("alice@oasis.io", "Alice Lena")).getUserId();

        UserObject userById = redisRepository.readUser(userId);
        UserObject userByEmail = redisRepository.readUser("john@oasis.io");
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
        assertError(() -> redisRepository.readUser("unknown@oasis.io"));
    }

    @Test
    void addUser() {
        long userId = redisRepository.addUser(createUser("john@oasis.io", "John Doe")).getUserId();

        // no duplicate emails
        assertError(() -> redisRepository.addUser(createUser("john@oasis.io", "Other John")));

        long user2Id = redisRepository.addUser(createUser("alice@oasis.io", "Alice Lena")).getUserId();
        Assertions.assertEquals(user2Id, userId + 1);
    }

    @Test
    void updateUser() {
        long userId = redisRepository.addUser(createUser("john@oasis.io", "John Doe")).getUserId();
        UserObject userObject = redisRepository.readUser(userId);
        Assertions.assertNotNull(userObject);
        Assertions.assertEquals("john@oasis.io", userObject.getEmail());
        Assertions.assertEquals("John Doe", userObject.getDisplayName());

        UserMetadata userMetadata = redisRepository.readUserMetadata(userId);
        Assertions.assertEquals("John Doe", userMetadata.getDisplayName());

        // changing email and try to save should fail
        userObject.setEmail("john1@oasis.io");
        assertError(() -> redisRepository.updateUser(userId, userObject));

        // impersonating users should fail
        redisRepository.addUser(createUser("bob@oasis.io", "Bob Hopkins"));
        userObject.setEmail("bob@oasis.io");
        assertError(() -> redisRepository.updateUser(userId, userObject));

        userObject.setEmail("john@oasis.io");
        userObject.setDisplayName("John Changed");
        redisRepository.updateUser(userId, userObject);
        Assertions.assertEquals("John Changed", redisRepository.readUser(userId).getDisplayName());

        // metadata should also change
        Assertions.assertEquals("John Changed", redisRepository.readUserMetadata(userId).getDisplayName());
    }

    @Test
    void deleteUser() {
        long userId = redisRepository.addUser(createUser("john@oasis.io", "John Doe")).getUserId();
        Assertions.assertTrue(redisRepository.existsUser("john@oasis.io"));

        UserObject userObject = redisRepository.deleteUser(userId);
        Assertions.assertEquals("John Doe", userObject.getDisplayName());

        Assertions.assertFalse(redisRepository.existsUser("john@oasis.io"));

        UserMetadata userMetadata = redisRepository.readUserMetadata(userId);
        Assertions.assertNull(userMetadata.getDisplayName());

        // non-existing user
        assertError(() -> redisRepository.deleteUser(999));
    }

    @Test
    void existsUser() {
        long userId = redisRepository.addUser(createUser("john@oasis.io", "John Doe")).getUserId();
        Assertions.assertTrue(redisRepository.existsUser(userId));
        Assertions.assertFalse(redisRepository.existsUser(999));
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
    void existsTeam() {
        Integer teamId = redisRepository.addTeam(createTeam("Warriors")).getTeamId();
        Assertions.assertTrue(redisRepository.existsTeam(teamId));
        Assertions.assertFalse(redisRepository.existsTeam(999));
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
        Integer teamId = redisRepository.addTeam(createTeam("Warriors")).getTeamId();

        TeamMetadata teamMetadata = redisRepository.readTeamMetadata(teamId);
        Assertions.assertNotNull(teamMetadata);
        Assertions.assertEquals("Warriors", teamMetadata.getName());
        Assertions.assertEquals("Warriors", redisRepository.readTeamMetadata(String.valueOf(teamId)).getName());

        // non-existing id
        Assertions.assertNull(redisRepository.readTeamMetadata(999));
        Assertions.assertNull(redisRepository.readTeamMetadata("999"));
    }

    @Test
    void updateTeam() {
        Integer teamId = redisRepository.addTeam(createTeam("Warriors")).getTeamId();
        TeamObject teamObject = redisRepository.readTeam(teamId);
        Assertions.assertEquals("Warriors", teamObject.getName());

        Assertions.assertTrue(redisRepository.existsTeam("warriors"));

        teamObject.setName("New Warriors");
        redisRepository.updateTeam(teamId, teamObject);

        teamObject = redisRepository.readTeam(teamId);
        Assertions.assertEquals("New Warriors", teamObject.getName());

        Assertions.assertFalse(redisRepository.existsTeam("warriors"));
        Assertions.assertTrue(redisRepository.existsTeam("New warriors"));
    }

    @Test
    void existTeam() {
        redisRepository.addTeam(createTeam("Warriors"));

        Assertions.assertTrue(redisRepository.existsTeam("Warriors"));
        Assertions.assertTrue(redisRepository.existsTeam("warriors"));
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
    void getUserTeams() {
        long u1 = redisRepository.addUser(createUser("john@oasis.io", "John Doe")).getUserId();
        int t1 = redisRepository.addTeam(createTeam("sunrise warrior")).getTeamId();
        int t2 = redisRepository.addTeam(createTeam("musketeers")).getTeamId();
        int t3 = redisRepository.addTeam(createTeam("avengers")).getTeamId();

        redisRepository.addUserToTeam(u1, t1);
        redisRepository.addUserToTeam(u1, t2);
        redisRepository.addUserToTeam(u1, t3);

        List<TeamObject> userTeams = redisRepository.getUserTeams(u1);
        Assertions.assertEquals(3, userTeams.size());
        Assertions.assertTrue(userTeams.stream().anyMatch(t -> t.getName().equals("sunrise warrior")));
        Assertions.assertTrue(userTeams.stream().anyMatch(t -> t.getName().equals("musketeers")));
        Assertions.assertTrue(userTeams.stream().anyMatch(t -> t.getName().equals("avengers")));

        // non-existing user
        assertError(() -> redisRepository.getUserTeams(999));
    }

    @Test
    void addUserToTeam() {
        long u1 = redisRepository.addUser(createUser("john@oasis.io", "John Doe")).getUserId();
        long u2 = redisRepository.addUser(createUser("alice@oasis.io", "Alice Lee")).getUserId();

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
    void removeUserFromTeam() {
        long u1 = redisRepository.addUser(createUser("john@oasis.io", "John Doe")).getUserId();
        long u2 = redisRepository.addUser(createUser("alice@oasis.io", "Alice Lee")).getUserId();
        long u3 = redisRepository.addUser(createUser("bob@oasis.io", "Bob Hopkins")).getUserId();

        int t1 = redisRepository.addTeam(createTeam("sunrise warrior")).getTeamId();
        int t2 = redisRepository.addTeam(createTeam("musketeers")).getTeamId();
        int t3 = redisRepository.addTeam(createTeam("avengers")).getTeamId();

        redisRepository.addUserToTeam(u1, t1);
        redisRepository.addUserToTeam(u1, t2);

        Assertions.assertEquals(2, redisRepository.getUserTeams(u1).size());

        redisRepository.removeUserFromTeam(u1, t1);
        Assertions.assertEquals(1, redisRepository.getUserTeams(u1).size());

        // remove non-existing user
        assertError(() -> redisRepository.removeUserFromTeam(999, t1));
        // remove non-existing team
        assertError(() -> redisRepository.removeUserFromTeam(u1, t3));
        assertError(() -> redisRepository.removeUserFromTeam(u1, 999));
    }

    @Test
    void getTeamUsers() {
        long u1 = redisRepository.addUser(createUser("john@oasis.io", "John Doe")).getUserId();
        long u2 = redisRepository.addUser(createUser("alice@oasis.io", "Alice Lee")).getUserId();
        long u3 = redisRepository.addUser(createUser("bob@oasis.io", "Bob Hopkins")).getUserId();

        int t1 = redisRepository.addTeam(createTeam("sunrise warrior")).getTeamId();
        int t2 = redisRepository.addTeam(createTeam("musketeers")).getTeamId();
        int t3 = redisRepository.addTeam(createTeam("avengers")).getTeamId();

        redisRepository.addUserToTeam(u1, t1);
        redisRepository.addUserToTeam(u1, t2);
        redisRepository.addUserToTeam(u3, t2);

        List<UserObject> teamUsers = redisRepository.getTeamUsers(t2);
        Assertions.assertEquals(2, teamUsers.size());
        Assertions.assertTrue(teamUsers.stream().anyMatch(u -> u.getEmail().equals("john@oasis.io")));
        Assertions.assertTrue(teamUsers.stream().anyMatch(u -> u.getEmail().equals("bob@oasis.io")));
        Assertions.assertFalse(teamUsers.stream().anyMatch(u -> u.getEmail().equals("alice@oasis.io")));

        // no users team
        Assertions.assertTrue(redisRepository.getTeamUsers(t3).isEmpty());

        // non-existing team
        assertError(() -> redisRepository.getTeamUsers(999));
    }

    @Test
    void addNewElement() {
        ElementDef element = createElement("ELE0001", "Bonus Points", "points");
        ElementDef elementDef = redisRepository.addNewElement(element.getGameId(), element);
        Assertions.assertEquals(element.getId(), elementDef.getId());

        assertError(() -> redisRepository.addNewElement(element.getGameId(), elementDef));
    }

    @Test
    void updateElement() {
        ElementDef element = createElement("ELE0001", "Bonus Points", "points");
        ElementDef elementDef = redisRepository.addNewElement(element.getGameId(), element);

        elementDef.getMetadata().setName("Bonus Points 2");
        redisRepository.updateElement(elementDef.getGameId(), elementDef.getId(), elementDef);
        ElementDef updatedElement = redisRepository.readElement(elementDef.getGameId(), elementDef.getId());
        Assertions.assertEquals("Bonus Points 2", updatedElement.getMetadata().getName());

        // update non-existing element
        elementDef.setId("NEX00000");
        assertError(() -> redisRepository.updateElement(element.getGameId(), elementDef.getId(), elementDef));
        assertError(() -> redisRepository.updateElement(element.getGameId(), "NEX9999", elementDef));
    }

    @Test
    void deleteElement() {
        ElementDef element = createElement("ELE0001", "Bonus Points", "points");
        redisRepository.addNewElement(element.getGameId(), element);

        // deleting non existence element
        assertError(() -> redisRepository.deleteElement(element.getGameId(), "ELE9999"));

        ElementDef elementDef = redisRepository.deleteElement(element.getGameId(), "ELE0001");
        Assertions.assertEquals(element.getMetadata().getName(), elementDef.getMetadata().getName());
    }

    @Test
    void readElement() {
        ElementDef element = createElement("ELE0001", "Bonus Points", "points");
        redisRepository.addNewElement(element.getGameId(), element);

        ElementDef elementDef = redisRepository.readElement(element.getGameId(), "ELE0001");
        Assertions.assertEquals(element.getId(), elementDef.getId());
        Assertions.assertEquals(element.getMetadata().getName(), elementDef.getMetadata().getName());
        Assertions.assertEquals(element.getType(), elementDef.getType());

        // read non-existence id
        assertError(() -> redisRepository.readElement(element.getGameId(), "ELE9999"));
    }

    @Test
    void readElementDefinition() {
        redisRepository.addNewElement(1, createElement("ELE0001", "Bonus Points", "points"));

        SimpleElementDefinition elementDefinition = redisRepository.readElementDefinition(1, "ELE0001");
        Assertions.assertNotNull(elementDefinition);
        Assertions.assertEquals("Bonus Points", elementDefinition.getName());
        Assertions.assertEquals("ELE0001", elementDefinition.getId());

        // read non-existence def
        Assertions.assertNull(redisRepository.readElementDefinition(1, "ELE9999"));
    }

    @Test
    void readElementDefinitions() {
        redisRepository.addNewElement(1, createElement("ELE0001", "Bonus Points", "points"));
        redisRepository.addNewElement(1, createElement("ELE0002", "Star Points", "points"));
        redisRepository.addNewElement(1, createElement("ELE0003", "Combo", "badges"));

        Map<String, SimpleElementDefinition> elementDefinition = redisRepository.readElementDefinitions(1, List.of("ELE0001", "ELE0002"));
        Assertions.assertEquals(2, elementDefinition.size());
        Assertions.assertTrue(elementDefinition.containsKey("ELE0001"));
        Assertions.assertTrue(elementDefinition.containsKey("ELE0002"));
        Assertions.assertNotNull(elementDefinition.get("ELE0001"));
        Assertions.assertNotNull(elementDefinition.get("ELE0002"));

        Map<String, SimpleElementDefinition> defs = redisRepository.readElementDefinitions(1, List.of("ELE0003", "ELE9999"));
        Assertions.assertEquals(1, defs.size());
        Assertions.assertTrue(defs.containsKey("ELE0003"));
        Assertions.assertNotNull(defs.get("ELE0003"));
    }

    @Test
    void readAttributeInfo() {
        redisRepository.addAttribute(1, new AttributeInfo(1, "Gold", 30));
        redisRepository.addAttribute(1, new AttributeInfo(2, "Silver", 20));
        redisRepository.addAttribute(1, new AttributeInfo(3, "Bronze", 10));

        redisRepository.readAttributesInfo(1);
    }

    @Test
    void addAttribute() {
        redisRepository.addAttribute(1, new AttributeInfo(1, "Gold", 30));
        redisRepository.addAttribute(1, new AttributeInfo(2, "Silver", 20));
        redisRepository.addAttribute(1, new AttributeInfo(3, "Bronze", 10));

        // already existing id
        assertError(() -> redisRepository.addAttribute(1, new AttributeInfo(3, "Iron", 5)));
    }

    @Test
    void listAllAttributes() {
        List<AttributeInfo> attributeInfos = redisRepository.listAllAttributes(1);
        Assertions.assertEquals(0, attributeInfos.size());

        redisRepository.addAttribute(1, new AttributeInfo(1, "Gold", 30));
        redisRepository.addAttribute(1, new AttributeInfo(2, "Silver", 20));
        redisRepository.addAttribute(1, new AttributeInfo(3, "Bronze", 10));

        List<AttributeInfo> infos = redisRepository.listAllAttributes(1);
        Assertions.assertEquals(3, infos.size());
        Assertions.assertTrue(infos.stream().anyMatch(attr -> attr.getName().equals("Gold")));
        Assertions.assertTrue(infos.stream().anyMatch(attr -> attr.getName().equals("Silver")));
        Assertions.assertTrue(infos.stream().anyMatch(attr -> attr.getName().equals("Bronze")));
    }

    private ElementDef createElement(String id, String name, String type) {
        ElementDef def = new ElementDef();
        def.setId(id);
        def.setMetadata(new SimpleElementDefinition(id, name, ""));
        def.setType(type);
        def.setGameId(1);
        def.setData(new HashMap<>());
        return def;
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