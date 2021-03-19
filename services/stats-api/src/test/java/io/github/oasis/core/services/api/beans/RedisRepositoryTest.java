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

import com.fasterxml.jackson.databind.ObjectMapper;
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
import io.github.oasis.core.model.EventSource;
import io.github.oasis.core.model.EventSourceSecrets;
import io.github.oasis.core.model.PlayerObject;
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
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * @author Isuru Weerarathna
 */
class RedisRepositoryTest {

    private static final int DEF_GAME_ID = 1;

    private static RedisRepository redisRepository;
    private static Db dbPool;

    @BeforeAll
    public static void beforeAll() {
        RedisDb redisDb = RedisDb.create(OasisConfigs.defaultConfigs());
        redisDb.init();
        dbPool = redisDb;
        ObjectMapper jsonMapper = new SerializingConfigs().createSerializer();
        redisRepository = new RedisRepository(redisDb, new JsonSerializer(jsonMapper));
    }

    @BeforeEach
    public void beforeEach() throws IOException {
        try (DbContext db = dbPool.createContext()) {
            db.allKeys("*").forEach(db::removeKey);
        }
    }

    @Test
    void addEventSource() {
        EventSource eventSource = createEventSource(1, "token-1", "source-app", 1);
        redisRepository.addEventSource(eventSource);

        eventSource.setName("source-app-2");
        assertError(() -> redisRepository.addEventSource(eventSource));

        redisRepository.addEventSource(createEventSource(2, "token-2", "source-app-new", 1, 2));

        // without game ids
        redisRepository.addEventSource(createEventSource(3, "token-3", "source-app-new"));

        // without secrets, it should fail
        EventSource src = createEventSource(4, "token-no", "no-secret-app", 1);
        src.setSecrets(null);
        assertError(() -> redisRepository.addEventSource(src));
    }

    @Test
    void deleteEventSource() {
        Integer id = redisRepository.addEventSource(createEventSource(1, "token-1", "my-app", 1)).getId();

        Assertions.assertNotNull(redisRepository.readEventSource("token-1"));

        EventSource source = redisRepository.deleteEventSource(id);
        Assertions.assertEquals("token-1", source.getToken());
        Assertions.assertEquals("my-app", source.getName());
        Assertions.assertEquals(1, source.getGames().size());
        Assertions.assertNotNull(source.getSecrets());
        Assertions.assertNotNull(source.getSecrets().getPublicKey());
        Assertions.assertNotNull(source.getSecrets().getPrivateKey());

        assertError(() -> redisRepository.readEventSource("token-1"));

        // non-existing items
        assertError(() -> redisRepository.deleteEventSource(999));
    }

    @Test
    void readEventSource() {
        Integer id = redisRepository.addEventSource(createEventSource(1, "token-1", "my-app", 1)).getId();
        EventSource source = redisRepository.readEventSource(id);
        Assertions.assertEquals("token-1", source.getToken());
        Assertions.assertEquals("my-app", source.getName());
        Assertions.assertEquals(1, source.getGames().size());
        Assertions.assertNotNull(source.getSecrets());
        Assertions.assertNotNull(source.getSecrets().getPublicKey());
        Assertions.assertNotNull(source.getSecrets().getPrivateKey());

        EventSource eventSource = redisRepository.readEventSource("token-1");
        Assertions.assertEquals("token-1", eventSource.getToken());
        Assertions.assertEquals("my-app", eventSource.getName());
        Assertions.assertEquals(1, eventSource.getGames().size());
        Assertions.assertNotNull(eventSource.getSecrets());
        Assertions.assertNotNull(eventSource.getSecrets().getPublicKey());
        Assertions.assertNotNull(eventSource.getSecrets().getPrivateKey());

        // non-existing id
        assertError(() -> redisRepository.readEventSource(999));
        // non-existing token
        assertError(() -> redisRepository.readEventSource("unknown"));
    }

    @Test
    void listAllEventSources() {
        Assertions.assertEquals(0, redisRepository.listAllEventSources().size());

        redisRepository.addEventSource(createEventSource(1, "t-1", "app-1", 1));
        redisRepository.addEventSource(createEventSource(2, "t-2", "app-2", 2, 3));
        redisRepository.addEventSource(createEventSource(3, "t-3", "app-3", 3));
        redisRepository.addEventSource(createEventSource(4, "t-4", "app-4"));
        redisRepository.addEventSource(createEventSource(5, "t-5", "app-5", 2, 3));

        List<EventSource> sources = redisRepository.listAllEventSources();
        Assertions.assertEquals(5, sources.size());
        Assertions.assertTrue(sources.stream().anyMatch(s -> "t-1".equals(s.getToken())));
        Assertions.assertTrue(sources.stream().anyMatch(s -> "t-2".equals(s.getToken())));
        Assertions.assertTrue(sources.stream().anyMatch(s -> "t-3".equals(s.getToken())));
        Assertions.assertTrue(sources.stream().anyMatch(s -> "t-4".equals(s.getToken())));
        Assertions.assertTrue(sources.stream().anyMatch(s -> "t-5".equals(s.getToken())));

        redisRepository.deleteEventSource(redisRepository.readEventSource("t-1").getId());
        Assertions.assertEquals(4, redisRepository.listAllEventSources().size());
    }

    @Test
    void listAllEventSourcesOfGame() {
        redisRepository.addEventSource(createEventSource(1, "t-1", "app-1", 1));
        redisRepository.addEventSource(createEventSource(2, "t-2", "app-2", 2, 3));
        redisRepository.addEventSource(createEventSource(3, "t-3", "app-3", 3));
        redisRepository.addEventSource(createEventSource(4, "t-4", "app-4"));
        redisRepository.addEventSource(createEventSource(5, "t-5", "app-5", 2, 3));

        Assertions.assertEquals(5, redisRepository.listAllEventSources().size());

        Assertions.assertEquals(1, redisRepository.listAllEventSourcesOfGame(1).size());
        Assertions.assertEquals(2, redisRepository.listAllEventSourcesOfGame(2).size());
        Assertions.assertEquals(3, redisRepository.listAllEventSourcesOfGame(3).size());
        Assertions.assertEquals(0, redisRepository.listAllEventSourcesOfGame(4).size());
    }

    @Test
    void addEventSourceToGame() {
        int src1Id = redisRepository.addEventSource(createEventSource(1, "t-1", "app-1")).getId();

        Assertions.assertNull(redisRepository.readEventSource(src1Id).getGames());

        redisRepository.addEventSourceToGame(src1Id, 1);
        redisRepository.addEventSourceToGame(src1Id, 2);
        Assertions.assertEquals(2, redisRepository.readEventSource(src1Id).getGames().size());

        // adding to same game again
        assertError(() -> redisRepository.addEventSourceToGame(src1Id, 1));

        // adding with non-existance source id
        assertError(() -> redisRepository.addEventSourceToGame(999, 1));
    }

    @Test
    void removeEventSourceFromGame() {
        int src1Id = redisRepository.addEventSource(createEventSource(1, "t-1", "app-1")).getId();

        Assertions.assertNull(redisRepository.readEventSource(src1Id).getGames());

        redisRepository.addEventSourceToGame(src1Id, 1);
        redisRepository.addEventSourceToGame(src1Id, 2);
        Assertions.assertEquals(2, redisRepository.readEventSource(src1Id).getGames().size());
        Assertions.assertEquals(2, redisRepository.readEventSource("t-1").getGames().size());

        redisRepository.removeEventSourceFromGame(src1Id, 1);
        Assertions.assertEquals(1, redisRepository.readEventSource(src1Id).getGames().size());
        Assertions.assertEquals(1, redisRepository.readEventSource("t-1").getGames().size());

        // removing from non-exist game id
        assertError(() -> redisRepository.removeEventSourceFromGame(src1Id, 999));

        // non-existing source id
        assertError(() -> redisRepository.removeEventSourceFromGame(999, 1));
    }

    @Test
    @DisplayName("Game: Add")
    void addNewGame() {
        Game game = createGame(1, "game1");
        int gameId = redisRepository.addNewGame(game).getId();
        assertError(() -> redisRepository.addNewGame(game));

        Game game2 = game.toBuilder().id(2).name("game2").build();
        int game2Id = redisRepository.addNewGame(game2).getId();
        Assertions.assertTrue(gameId < game2Id);
    }

    @Test
    @DisplayName("Game: Update")
    void updateGame() {
        Game game = createGame(1, "game1");
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
        Integer gameId = redisRepository.addNewGame(createGame(1, "game1")).getId();
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
        Game oldGame = createGame(1, "game1");
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
        redisRepository.addNewGame(createGame(1, "game-apex-1"));
        redisRepository.addNewGame(createGame(2, "game-apex-2"));
        redisRepository.addNewGame(createGame(3, "game-apex-3"));
        redisRepository.addNewGame(createGame(4, "helloworld"));
        redisRepository.addNewGame(createGame(5, "olympic"));
        redisRepository.addNewGame(createGame(6, "commonwealth"));

        List<Game> games = redisRepository.listGames("0", 50).getRecords();
        Assertions.assertEquals(6, games.size());
    }

    @Test
    void readUsersByIdStrings() {
        long userId = redisRepository.addPlayer(createUser(1, "john@oasis.io", "John Doe")).getId();
        long user2Id = redisRepository.addPlayer(createUser(2, "alice@oasis.io", "Alice Lena")).getId();
        long user3Id = redisRepository.addPlayer(createUser(3, "bob@oasis.io", "Bob Hopkins")).getId();

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
        long userId = redisRepository.addPlayer(createUser(1, "john@oasis.io", "John Doe")).getId();
        long user2Id = redisRepository.addPlayer(createUser(2, "alice@oasis.io", "Alice Lena")).getId();

        PlayerObject userById = redisRepository.readPlayer(userId);
        PlayerObject userByEmail = redisRepository.readPlayer("john@oasis.io");
        Assertions.assertEquals(userById.getId(), userId);
        Assertions.assertEquals(userById.getId(), userByEmail.getId());
        Assertions.assertEquals(userById.getEmail(), userByEmail.getEmail());
        Assertions.assertEquals(userById.getDisplayName(), userByEmail.getDisplayName());

        // read metadata
        UserMetadata userMetadata = redisRepository.readUserMetadata(userId);
        Assertions.assertEquals(userById.getDisplayName(), userMetadata.getDisplayName());
        Assertions.assertEquals(userById.getId(), userMetadata.getUserId());
        Assertions.assertEquals(userById.getDisplayName(), redisRepository.readUserMetadata(String.valueOf(userId)).getDisplayName());

        Map<Long, UserMetadata> userMap = redisRepository.readUsersByIds(List.of(userId, user2Id));
        Assertions.assertEquals(2, userMap.size());

        // non existing user
        assertError(() -> redisRepository.readPlayer(999));
        // non-existing email
        assertError(() -> redisRepository.readPlayer("unknown@oasis.io"));
    }

    @Test
    void addUser() {
        long userId = redisRepository.addPlayer(createUser(1, "john@oasis.io", "John Doe")).getId();

        // no duplicate emails
        assertError(() -> redisRepository.addPlayer(createUser(2, "john@oasis.io", "Other John")));

        long user2Id = redisRepository.addPlayer(createUser(3, "alice@oasis.io", "Alice Lena")).getId();
        Assertions.assertTrue(user2Id > userId);
    }

    @Test
    void updateUser() {
        long userId = redisRepository.addPlayer(createUser(1, "john@oasis.io", "John Doe")).getId();
        PlayerObject playerObject = redisRepository.readPlayer(userId);
        Assertions.assertNotNull(playerObject);
        Assertions.assertEquals("john@oasis.io", playerObject.getEmail());
        Assertions.assertEquals("John Doe", playerObject.getDisplayName());

        UserMetadata userMetadata = redisRepository.readUserMetadata(userId);
        Assertions.assertEquals("John Doe", userMetadata.getDisplayName());

        // changing email and try to save should fail
        playerObject.setEmail("john1@oasis.io");
        assertError(() -> redisRepository.updatePlayer(userId, playerObject));

        // impersonating users should fail
        redisRepository.addPlayer(createUser(2, "bob@oasis.io", "Bob Hopkins"));
        playerObject.setEmail("bob@oasis.io");
        assertError(() -> redisRepository.updatePlayer(userId, playerObject));

        playerObject.setEmail("john@oasis.io");
        playerObject.setDisplayName("John Changed");
        redisRepository.updatePlayer(userId, playerObject);
        Assertions.assertEquals("John Changed", redisRepository.readPlayer(userId).getDisplayName());

        // metadata should also change
        Assertions.assertEquals("John Changed", redisRepository.readUserMetadata(userId).getDisplayName());
    }

    @Test
    void deleteUser() {
        long userId = redisRepository.addPlayer(createUser(1, "john@oasis.io", "John Doe")).getId();
        Assertions.assertTrue(redisRepository.existsPlayer("john@oasis.io"));

        PlayerObject playerObject = redisRepository.deletePlayer(userId);
        Assertions.assertEquals("John Doe", playerObject.getDisplayName());

        Assertions.assertFalse(redisRepository.existsPlayer("john@oasis.io"));

        UserMetadata userMetadata = redisRepository.readUserMetadata(userId);
        Assertions.assertNull(userMetadata.getDisplayName());

        assertError(() -> redisRepository.getPlayerTeams(userId));

        // non-existing user
        assertError(() -> redisRepository.deletePlayer(999));
    }

    @Test
    void existsUser() {
        long userId = redisRepository.addPlayer(createUser(1, "john@oasis.io", "John Doe")).getId();
        Assertions.assertTrue(redisRepository.existsPlayer(userId));
        Assertions.assertFalse(redisRepository.existsPlayer(999));
    }

    @Test
    void addTeam() {
        Integer teamId = redisRepository.addTeam(createTeam(1, "Warriors")).getId();
        Integer team2Id = redisRepository.addTeam(createTeam(2, "Avengers")).getId();
        Assertions.assertTrue(teamId > 0);
        Assertions.assertEquals(team2Id, teamId + 1);

        // should not be able to add same team again
        assertError(() -> redisRepository.addTeam(createTeam(1, "Warriors")));
        assertError(() -> redisRepository.addTeam(createTeam(2, "warriors")));
    }

    @Test
    void existsTeam() {
        Integer teamId = redisRepository.addTeam(createTeam(1, "Warriors")).getId();
        Assertions.assertTrue(redisRepository.existsTeam(teamId));
        Assertions.assertFalse(redisRepository.existsTeam(999));
    }

    @Test
    void readTeam() {
        Integer teamId = redisRepository.addTeam(createTeam(1, "Warriors")).getId();
        Assertions.assertEquals("Warriors", redisRepository.readTeam(teamId).getName());

        // non-exist team id
        assertError(() -> redisRepository.readTeam(999));
    }

    @Test
    void readTeamsByIdStrings() {
        Integer teamId = redisRepository.addTeam(createTeam(1, "Warriors")).getId();
        Integer team2Id = redisRepository.addTeam(createTeam(2, "Avengers")).getId();

        // read metadata
        Map<Integer, TeamMetadata> teamMap = redisRepository.readTeamsById(List.of(teamId, team2Id));
        Assertions.assertEquals(2, teamMap.size());
        Assertions.assertEquals("Warriors", teamMap.get(teamId).getName());
        Assertions.assertEquals("Avengers", teamMap.get(team2Id).getName());
    }

    @Test
    void readTeamsById() {
        Integer teamId = redisRepository.addTeam(createTeam(1, "Warriors")).getId();
        Integer team2Id = redisRepository.addTeam(createTeam(2, "Avengers")).getId();

        // read metadata
        Map<String, TeamMetadata> teamMap = redisRepository.readTeamsByIdStrings(List.of(String.valueOf(teamId), String.valueOf(team2Id)));
        Assertions.assertEquals(2, teamMap.size());
        Assertions.assertEquals("Warriors", teamMap.get(String.valueOf(teamId)).getName());
        Assertions.assertEquals("Avengers", teamMap.get(String.valueOf(team2Id)).getName());
    }

    @Test
    void readTeamMetadata() {
        Integer teamId = redisRepository.addTeam(createTeam(1, "Warriors")).getId();

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
        Integer teamId = redisRepository.addTeam(createTeam(1, "Warriors")).getId();
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
        redisRepository.addTeam(createTeam(1, "Warriors"));

        Assertions.assertTrue(redisRepository.existsTeam("Warriors"));
        Assertions.assertTrue(redisRepository.existsTeam("warriors"));
    }

    @Test
    void searchTeam() {
        redisRepository.addTeam(createTeam(1, "wildfire"));
        redisRepository.addTeam(createTeam(2, "t-apex-1"));
        redisRepository.addTeam(createTeam(3, "t-apex-2"));
        redisRepository.addTeam(createTeam(4, "sunrise warrior"));
        redisRepository.addTeam(createTeam(5, "musketeers"));
        redisRepository.addTeam(createTeam(6, "avengers"));

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
        long u1 = redisRepository.addPlayer(createUser(1, "john@oasis.io", "John Doe")).getId();
        int t1 = redisRepository.addTeam(createTeam(DEF_GAME_ID, "sunrise warrior")).getId();
        int t2 = redisRepository.addTeam(createTeam(2, "musketeers")).getId();
        int t3 = redisRepository.addTeam(createTeam(3, "avengers")).getId();

        redisRepository.addPlayerToTeam(u1, DEF_GAME_ID, t1);
        redisRepository.addPlayerToTeam(u1, 2, t2);
        redisRepository.addPlayerToTeam(u1, 3, t3);

        List<TeamObject> userTeams = redisRepository.getPlayerTeams(u1);
        Assertions.assertEquals(3, userTeams.size());
        Assertions.assertTrue(userTeams.stream().anyMatch(t -> t.getName().equals("sunrise warrior")));
        Assertions.assertTrue(userTeams.stream().anyMatch(t -> t.getName().equals("musketeers")));
        Assertions.assertTrue(userTeams.stream().anyMatch(t -> t.getName().equals("avengers")));

        // non-existing user
        assertError(() -> redisRepository.getPlayerTeams(999));
    }

    @Test
    void addUserToTeam() {
        long u1 = redisRepository.addPlayer(createUser(1, "john@oasis.io", "John Doe")).getId();
        long u2 = redisRepository.addPlayer(createUser(2, "alice@oasis.io", "Alice Lee")).getId();

        int t1 = redisRepository.addTeam(createTeam(1, "sunrise warrior")).getId();
        int t2 = redisRepository.addTeam(createTeam(2, "musketeers")).getId();
        int t3 = redisRepository.addTeam(createTeam(2, "avengers")).getId();

        redisRepository.addPlayerToTeam(u1, DEF_GAME_ID, t1);
        assertError(() -> redisRepository.addPlayerToTeam(u1, DEF_GAME_ID, t2));
        redisRepository.addPlayerToTeam(u1, 2, t3);

        List<TeamObject> userTeams = redisRepository.getPlayerTeams(u1);
        Assertions.assertEquals(2, userTeams.size());
        Assertions.assertTrue(userTeams.stream().anyMatch(t -> t.getName().equals("sunrise warrior")));
        Assertions.assertTrue(userTeams.stream().anyMatch(t -> t.getName().equals("avengers")));

        Assertions.assertTrue(redisRepository.getPlayerTeams(u2).isEmpty());

        // add user to same team
        assertError(() -> redisRepository.addPlayerToTeam(u1, DEF_GAME_ID, t1));
        Assertions.assertEquals(2, redisRepository.getPlayerTeams(u1).size());
    }

    @Test
    void removeUserFromTeam() {
        long u1 = redisRepository.addPlayer(createUser(1, "john@oasis.io", "John Doe")).getId();

        int t1 = redisRepository.addTeam(createTeam(DEF_GAME_ID, "sunrise warrior")).getId();
        int t2 = redisRepository.addTeam(createTeam(2, "musketeers")).getId();
        int t3 = redisRepository.addTeam(createTeam(1, "avengers")).getId();

        redisRepository.addPlayerToTeam(u1, DEF_GAME_ID, t1);
        redisRepository.addPlayerToTeam(u1, 2, t2);

        Assertions.assertEquals(2, redisRepository.getPlayerTeams(u1).size());

        redisRepository.removePlayerFromTeam(u1, DEF_GAME_ID, t1);
        Assertions.assertEquals(1, redisRepository.getPlayerTeams(u1).size());

        // remove non-existing user
        assertError(() -> redisRepository.removePlayerFromTeam(999, DEF_GAME_ID, t1));
        // remove non-existing team
        assertError(() -> redisRepository.removePlayerFromTeam(u1, DEF_GAME_ID, t3));
        assertError(() -> redisRepository.removePlayerFromTeam(u1, DEF_GAME_ID, 999));
    }

    @Test
    void getTeamUsers() {
        long u1 = redisRepository.addPlayer(createUser(1, "john@oasis.io", "John Doe")).getId();
        long u3 = redisRepository.addPlayer(createUser(2, "bob@oasis.io", "Bob Hopkins")).getId();

        int t1 = redisRepository.addTeam(createTeam(DEF_GAME_ID, "sunrise warrior")).getId();
        int t2 = redisRepository.addTeam(createTeam(2, "musketeers")).getId();
        int t3 = redisRepository.addTeam(createTeam(3, "avengers")).getId();

        redisRepository.addPlayerToTeam(u1, DEF_GAME_ID, t1);
        redisRepository.addPlayerToTeam(u1, 2, t2);
        redisRepository.addPlayerToTeam(u3, 2, t2);

        List<PlayerObject> teamUsers = redisRepository.getTeamPlayers(t2);
        Assertions.assertEquals(2, teamUsers.size());
        Assertions.assertTrue(teamUsers.stream().anyMatch(u -> u.getEmail().equals("john@oasis.io")));
        Assertions.assertTrue(teamUsers.stream().anyMatch(u -> u.getEmail().equals("bob@oasis.io")));
        Assertions.assertFalse(teamUsers.stream().anyMatch(u -> u.getEmail().equals("alice@oasis.io")));

        // no users team
        Assertions.assertTrue(redisRepository.getTeamPlayers(t3).isEmpty());

        // non-existing team
        assertError(() -> redisRepository.getTeamPlayers(999));
    }

    @Test
    void addNewElement() {
        ElementDef element = createElement("ELE0001", "Bonus Points", "points");
        ElementDef elementDef = redisRepository.addNewElement(element.getGameId(), element);
        Assertions.assertEquals(element.getElementId(), elementDef.getElementId());

        assertError(() -> redisRepository.addNewElement(element.getGameId(), elementDef));
    }

    @Test
    void updateElement() {
        ElementDef element = createElement("ELE0001", "Bonus Points", "points");
        ElementDef elementDef = redisRepository.addNewElement(element.getGameId(), element);

        elementDef.getMetadata().setName("Bonus Points 2");
        redisRepository.updateElement(elementDef.getGameId(), elementDef.getElementId(), elementDef);
        ElementDef updatedElement = redisRepository.readElement(elementDef.getGameId(), elementDef.getElementId());
        Assertions.assertEquals("Bonus Points 2", updatedElement.getMetadata().getName());

        // update non-existing element
        elementDef.setElementId("NEX00000");
        assertError(() -> redisRepository.updateElement(element.getGameId(), elementDef.getElementId(), elementDef));
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
        Assertions.assertEquals(element.getElementId(), elementDef.getElementId());
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
    void listAllElementDefinitions() {
        Assertions.assertEquals(0, redisRepository.listAllElementDefinitions(1, "points").size());

        redisRepository.addNewElement(1, createElement("ELE0001", "Bonus Points", "points"));
        redisRepository.addNewElement(1, createElement("ELE0002", "Star Points", "points"));
        redisRepository.addNewElement(1, createElement("ELE0003", "Combo", "badges"));

        Assertions.assertEquals(2, redisRepository.listAllElementDefinitions(1, "points").size());
        Assertions.assertEquals(1, redisRepository.listAllElementDefinitions(1, "badges").size());
        Assertions.assertEquals(0, redisRepository.listAllElementDefinitions(1, "challenges").size());

        ElementDef bonusPoints = redisRepository.readElement(1, "ELE0001");
        bonusPoints.getMetadata().setName("Bonus Points 2");
        redisRepository.updateElement(1, bonusPoints.getElementId(), bonusPoints);

        Assertions.assertEquals(2, redisRepository.listAllElementDefinitions(1, "points").size());
        Assertions.assertTrue(redisRepository.listAllElementDefinitions(1, "points").stream().anyMatch(e -> e.getName().equals("Bonus Points 2")));
        Assertions.assertEquals(1, redisRepository.listAllElementDefinitions(1, "badges").size());

        // delete now
        redisRepository.deleteElement(1, "ELE0001");
        Assertions.assertEquals(1, redisRepository.listAllElementDefinitions(1, "points").size());
        Assertions.assertEquals(1, redisRepository.listAllElementDefinitions(1, "badges").size());
        Assertions.assertEquals(0, redisRepository.listAllElementDefinitions(1, "challenges").size());
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
        return ElementDef.builder()
                .elementId(id)
                .metadata(new SimpleElementDefinition(id, name, ""))
                .type(type)
                .gameId(1)
                .data(new HashMap<>())
                .build();
    }

    private PlayerObject createUser(long id, String email, String name) {
        PlayerObject playerObject = new PlayerObject();
        playerObject.setId(id);
        playerObject.setDisplayName(name);
        playerObject.setEmail(email);
        return playerObject;
    }

    private TeamObject createTeam(int id, String name) {
        TeamObject team = new TeamObject();
        team.setId(id);
        team.setGameId(DEF_GAME_ID);
        team.setName(name);
        return team;
    }

    private Game createGame(int id, String name) {
        Game game = new Game();
        game.setId(id);
        game.setName(name);
        game.setDescription("Test Game");
        game.setMotto("All the way!");
        return game;
    }

    private EventSource createEventSource(int id, String token, String name, Integer... gameIds) {
        EventSource source = new EventSource();
        source.setId(id);
        source.setName(name);
        source.setToken(token);
        if (gameIds != null && gameIds.length > 0) {
            source.setGames(Set.of(gameIds));
        }
        EventSourceSecrets sourceSecrets = new EventSourceSecrets();
        sourceSecrets.setPublicKey(Base64.getEncoder().encodeToString(UUID.randomUUID().toString().getBytes()));
        sourceSecrets.setPrivateKey(Base64.getEncoder().encodeToString(UUID.randomUUID().toString().getBytes()));
        source.setSecrets(sourceSecrets);
        return source;
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