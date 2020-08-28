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

import io.github.oasis.core.Game;
import io.github.oasis.core.ID;
import io.github.oasis.core.TeamMetadata;
import io.github.oasis.core.UserMetadata;
import io.github.oasis.core.collect.Pair;
import io.github.oasis.core.elements.AttributeInfo;
import io.github.oasis.core.elements.ElementDef;
import io.github.oasis.core.elements.SimpleElementDefinition;
import io.github.oasis.core.exception.OasisDbException;
import io.github.oasis.core.exception.OasisException;
import io.github.oasis.core.exception.OasisRuntimeException;
import io.github.oasis.core.external.Db;
import io.github.oasis.core.external.DbContext;
import io.github.oasis.core.external.Mapped;
import io.github.oasis.core.external.OasisRepository;
import io.github.oasis.core.external.PaginatedResult;
import io.github.oasis.core.model.TeamObject;
import io.github.oasis.core.model.UserObject;
import io.github.oasis.core.services.SerializationSupport;
import io.github.oasis.core.services.helpers.OasisMetadataSupport;
import io.github.oasis.core.utils.Numbers;
import io.github.oasis.core.utils.Texts;
import io.github.oasis.core.utils.Utils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.github.oasis.core.utils.Constants.COMMA;

/**
 * @author Isuru Weerarathna
 */
@Component
public class RedisRepository implements OasisRepository, OasisMetadataSupport {

    private static final String ALL_PATTERN = "*";

    private static final String INCR_GAME_KEY = "games";
    private static final String INCR_USER_KEY = "users";
    private static final String INCR_TEAM_KEY = "teams";
    public static final String ALL_ATTRIBUTES_KEY = "attributes";

    private final Db dbPool;
    private final SerializationSupport serializationSupport;

    public RedisRepository(Db dbPool, SerializationSupport serializationSupport) {
        this.dbPool = dbPool;
        this.serializationSupport = serializationSupport;
    }

    @Override
    public Game addNewGame(Game game) {
        return withDbContext(db -> {
            if (existsGame(game.getName())) {
                throw new OasisDbException("Game by given name '" + game.getName() + "' already exists!");
            }

            int gameId = db.MAP(ID.OASIS_ID_STORAGE).incrementByOne(INCR_GAME_KEY);
            game.setId(gameId);

            String gameIdStr = String.valueOf(gameId);
            db.setValueInMap(ID.ALL_GAMES, gameIdStr, serializationSupport.serialize(game));
            updateGameIndex(game, null, db);
            return game;
        });
    }

    @Override
    public Game updateGame(int gameId, Game game) {
        return withDbContext(db -> {
            Game existingGame = readGame(gameId);
            if (gameId != existingGame.getId() || gameId != game.getId()) {
                throw new OasisRuntimeException("Game id is not allowed to modify!");
            }

            String prevName = existingGame.getName();

            String gameIdStr = String.valueOf(gameId);
            db.setValueInMap(ID.ALL_GAMES, gameIdStr, serializationSupport.serialize(game));
            updateGameIndex(game, prevName, db);
            return game;
        });
    }

    @Override
    public Game readGame(int gameId) {
        return withDbContext(db -> {
            String gameStr = db.getValueFromMap(ID.ALL_GAMES, String.valueOf(gameId));
            if (Texts.isEmpty(gameStr)) {
                throw new OasisRuntimeException("No game is found by id " + gameId + "!");
            }
            return serializationSupport.deserialize(gameStr, Game.class);
        });
    }

    @Override
    public Game deleteGame(int gameId) {
        return withDbContext(db -> {
            Mapped gamesMap = db.MAP(ID.ALL_GAMES);
            String subKey = String.valueOf(gameId);
            String gameStr = gamesMap.getValue(subKey);
            if (Texts.isEmpty(gameStr)) {
                throw new OasisDbException("No game exist by given id " + gameId + "!");
            }

            gamesMap.remove(subKey);
            Game deletedGame = serializationSupport.deserialize(gameStr, Game.class);
            db.removeKeyFromMap(ID.ALL_GAMES_INDEX, deletedGame.getName());
            return deletedGame;
        });
    }

    @Override
    public boolean existsGame(String gameName) {
        return withDbContext(db -> db.mapKeyExists(ID.ALL_GAMES_INDEX, gameName));
    }

    @Override
    public List<Game> listGames() {
        return withDbContext(db -> {
            List<Game> games = new ArrayList<>();
            Mapped gamesMap = db.MAP(ID.ALL_GAMES);
            PaginatedResult<Pair<String, String>> searchResult;
            do {
                searchResult = gamesMap.search(ALL_PATTERN, 50);
                games.addAll(searchResult.getRecords().stream()
                        .map(rec -> serializationSupport.deserialize(rec.getRight(), Game.class))
                        .collect(Collectors.toList()));

            } while (!searchResult.isCompleted());
            return games;
        });
    }

    @Override
    public Map<String, UserMetadata> readUsersByIdStrings(Collection<String> userIds) {
        return withDbContext(db -> {
            if (Utils.isEmpty(userIds)) {
                return new HashMap<>();
            }

            String[] keys = userIds.toArray(String[]::new);
            List<String> valuesFromMap = db.getValuesFromMap(ID.ALL_USERS_NAMES, keys);
            Map<String, UserMetadata> userMap = new HashMap<>();
            for (int i = 0; i < keys.length; i++) {
                String userId = keys[i];
                if (Texts.isNotEmpty(valuesFromMap.get(i))) {
                    userMap.put(userId, createUserFromValue(Numbers.asLong(userId), valuesFromMap.get(i)));
                }
            }
            return userMap;
        });
    }

    @Override
    public Map<Long, UserMetadata> readUsersByIds(Collection<Long> userIds) {
        return withDbContext(db -> {
            String[] keys = userIds.stream().map(String::valueOf).toArray(String[]::new);
            List<String> valuesFromMap = db.getValuesFromMap(ID.ALL_USERS_NAMES, keys);
            Map<Long, UserMetadata> userMap = new HashMap<>();
            for (int i = 0; i < keys.length; i++) {
                long userId = Numbers.asLong(keys[i]);
                userMap.put(userId, createUserFromValue(userId, valuesFromMap.get(i)));
            }
            return userMap;
        });
    }

    @Override
    public UserMetadata readUserMetadata(long userId) {
        return withDbContext(db -> {
            String valuesFromMap = db.getValueFromMap(ID.ALL_USERS_NAMES, String.valueOf(userId));
            return createUserFromValue(userId, valuesFromMap);
        });
    }

    @Override
    public UserMetadata readUserMetadata(String userId) {
        return withDbContext(db -> {
            String valuesFromMap = db.getValueFromMap(ID.ALL_USERS_NAMES, userId);
            return createUserFromValue(Long.parseLong(userId), valuesFromMap);
        });
    }

    @Override
    public UserObject readUser(long userId) {
        return withDbContext(db -> {
            String userStr = db.getValueFromMap(ID.ALL_USERS, String.valueOf(userId));
            if (Texts.isEmpty(userStr)) {
                throw new OasisRuntimeException("No user found by given id!");
            }
            return serializationSupport.deserialize(userStr, UserObject.class);
        });
    }

    @Override
    public UserObject readUser(String email) {
        return withDbContext(db -> {
            String userIdStr = db.getValueFromMap(ID.ALL_USERS_INDEX, email);
            if (Texts.isEmpty(userIdStr)) {
                throw new OasisRuntimeException("No user found by given email!");
            }
            String userStr = db.getValueFromMap(ID.ALL_USERS, userIdStr);
            return serializationSupport.deserialize(userStr, UserObject.class);
        });
    }

    @Override
    public Map<String, TeamMetadata> readTeamsByIdStrings(Collection<String> teamIds) {
        return withDbContext(db -> {
            String[] keys = teamIds.stream().map(String::valueOf).toArray(String[]::new);
            List<String> valuesFromMap = db.getValuesFromMap(ID.ALL_TEAMS_NAMES, keys);
            Map<String, TeamMetadata> teamMap = new HashMap<>();
            for (int i = 0; i < keys.length; i++) {
                int teamId = Numbers.asInt(keys[i]);
                teamMap.put(keys[i], createTeamFromValue(teamId, valuesFromMap.get(i)));
            }
            return teamMap;
        });
    }

    @Override
    public Map<Integer, TeamMetadata> readTeamsById(Collection<Integer> teamIds) {
        return withDbContext(db -> {
            String[] keys = teamIds.stream().map(String::valueOf).toArray(String[]::new);
            List<String> valuesFromMap = db.getValuesFromMap(ID.ALL_TEAMS_NAMES, keys);
            Map<Integer, TeamMetadata> teamMap = new HashMap<>();
            for (int i = 0; i < keys.length; i++) {
                int userId = Numbers.asInt(keys[i]);
                teamMap.put(userId, createTeamFromValue(userId, valuesFromMap.get(i)));
            }
            return teamMap;
        });
    }

    @Override
    public TeamMetadata readTeamMetadata(String teamId) {
        return withDbContext(db -> {
            String valuesFromMap = db.getValueFromMap(ID.ALL_TEAMS_NAMES, teamId);
            if (Texts.isEmpty(valuesFromMap)) {
                return null;
            }

            return createTeamFromValue(Integer.parseInt(teamId), valuesFromMap);
        });
    }

    @Override
    public TeamMetadata readTeamMetadata(int teamId) {
        return readTeamMetadata(String.valueOf(teamId));
    }

    @Override
    public UserObject addUser(UserObject newUser) {
        return withDbContext(db -> {
            if (db.mapKeyExists(ID.ALL_USERS_INDEX, newUser.getEmail())) {
                throw new OasisRuntimeException("User by email already exist!");
            }

            long userId = db.MAP(ID.OASIS_ID_STORAGE).incrementBy(INCR_USER_KEY, 1L);
            String userIdStr = String.valueOf(userId);
            newUser.setUserId(userId);
            db.setValueInMap(ID.ALL_USERS, userIdStr, serializationSupport.serialize(newUser));
            updateUserMetadata(newUser, db);
            updateUserIndex(newUser, db);
            return newUser;
        });
    }

    @Override
    public boolean existsUser(String email) {
        return withDbContext(db -> db.mapKeyExists(ID.ALL_USERS_INDEX, email));
    }

    @Override
    public boolean existsUser(long userId) {
        return withDbContext(db -> existUser(userId, db));
    }

    @Override
    public UserObject updateUser(long userId, UserObject updatedUser) {
        return withDbContext(db -> {
            String userIdStr = String.valueOf(userId);
            String userIdOfEmail = db.getValueFromMap(ID.ALL_USERS_INDEX, updatedUser.getEmail());
            if (Texts.isEmpty(userIdOfEmail) || !userIdStr.equals(userIdOfEmail)) {
                throw new OasisRuntimeException("No such existing user found by email or id!");
            }

            updatedUser.setUserId(userId);
            db.setValueInMap(ID.ALL_USERS, userIdStr, serializationSupport.serialize(updatedUser));
            updateUserMetadata(updatedUser, db);
            return updatedUser;
        });
    }

    @Override
    public UserObject deleteUser(long userId) {
        return withDbContext(db -> {
            String userIdStr = String.valueOf(userId);
            String userVal = db.getValueFromMap(ID.ALL_USERS, userIdStr);

            if (Texts.isEmpty(userVal)) {
                throw new OasisRuntimeException("No user exist by given id!");
            }

            db.removeKeyFromMap(ID.ALL_USERS_NAMES, userIdStr);
            db.removeKeyFromMap(ID.ALL_USERS, userIdStr);
            UserObject userObject = serializationSupport.deserialize(userVal, UserObject.class);
            db.removeKeyFromMap(ID.ALL_USERS_INDEX, userObject.getEmail());
            return userObject;
        });
    }

    @Override
    public TeamObject addTeam(TeamObject teamObject) {
        return withDbContext(db -> {
            if (existsTeam(teamObject.getName())) {
                throw new OasisRuntimeException("A team is already exist by given name!");
            }

            int teamId = db.MAP(ID.OASIS_ID_STORAGE).incrementByOne(INCR_TEAM_KEY);
            String teamIdStr = String.valueOf(teamId);
            teamObject.setTeamId(teamId);
            db.setValueInMap(ID.ALL_TEAMS, teamIdStr, serializationSupport.serialize(teamObject));
            updateTeamMetadata(teamObject, db);
            updateTeamIndex(teamObject, null, db);
            return teamObject;
        });
    }

    @Override
    public TeamObject readTeam(int teamId) {
        return withDbContext(db -> {
            String value = db.getValueFromMap(ID.ALL_TEAMS, String.valueOf(teamId));
            if (Texts.isEmpty(value)) {
                throw new OasisRuntimeException("No team is found by given team id!");
            }
            return serializationSupport.deserialize(value, TeamObject.class);
        });
    }

    @Override
    public TeamObject updateTeam(int teamId, TeamObject updatedTeam) {
        return withDbContext(db -> {
            String value = db.getValueFromMap(ID.ALL_TEAMS, String.valueOf(teamId));
            String prevName = null;
            if (Texts.isNotEmpty(value)) {
                TeamObject readTeam = serializationSupport.deserialize(value, TeamObject.class);
                prevName = readTeam.getName();
            }

            db.setValueInMap(ID.ALL_TEAMS, String.valueOf(teamId), serializationSupport.serialize(updatedTeam));
            updateTeamMetadata(updatedTeam, db);
            updateTeamIndex(updatedTeam, prevName, db);
            return updatedTeam;
        });
    }

    @Override
    public boolean existsTeam(String teamName) {
        return withDbContext(db -> db.mapKeyExists(ID.ALL_TEAMS_INDEX, teamName.toLowerCase()));
    }

    @Override
    public boolean existsTeam(int teamId) {
        return withDbContext(db -> existTeam(teamId, db));
    }

    @Override
    public PaginatedResult<TeamMetadata> searchTeam(String teamName, String cursor, int maxRecords) {
        return withDbContext(db -> {
            String searchPattern = "*" + teamName + "*";
            Mapped gamesMap = db.MAP(ID.ALL_TEAMS_INDEX);
            String cur = Texts.isEmpty(cursor) ? null : cursor;
            PaginatedResult<Pair<String, String>> searchResult = gamesMap.search(searchPattern, maxRecords, cur);

            List<String> teamIds = searchResult.getRecords().stream()
                    .map(Pair::getRight)
                    .collect(Collectors.toList());

            List<TeamMetadata> list = new ArrayList<>(readTeamsByIdStrings(teamIds).values());
            return new PaginatedResult<>(
                    Texts.isEmpty(searchResult.getNextCursor()) ? null : searchResult.getNextCursor(),
                    list);
        });
    }

    @Override
    public void removeUserFromTeam(long userId, int teamId) {
        withDbContext(db -> {
            if (!existUser(userId, db)) {
                throw new OasisRuntimeException("Provided user id does not exist!");
            } else if (!existTeam(teamId, db)) {
                throw new OasisRuntimeException("Provided team id does not exist!");
            }

            String currentUserTeams = Texts.orDefault(db.getValueFromMap(ID.ALL_USERS_TEAMS, String.valueOf(userId)));
            String currentTeamUsers = Texts.orDefault(db.getValueFromMap(ID.ALL_TEAMS_USERS, String.valueOf(teamId)));
            Set<String> userTeamsSet = splitToSet(currentUserTeams);
            if (userTeamsSet.remove(String.valueOf(teamId))) {
                Set<String> teamUserSet = splitToSet(currentTeamUsers);
                if (teamUserSet.remove(String.valueOf(userId))) {
                    db.setValueInMap(ID.ALL_TEAMS_USERS, String.valueOf(teamId), String.join(COMMA, teamUserSet));
                }
                db.setValueInMap(ID.ALL_USERS_TEAMS, String.valueOf(userId), String.join(COMMA, userTeamsSet));
            } else {
                throw new OasisRuntimeException("Given team id was not associated with user id!");
            }
            return null;
        });
    }

    @Override
    public void addUserToTeam(long userId, int teamId) {
        withDbContext(db -> {
            String currentUserTeams = Texts.orDefault(db.getValueFromMap(ID.ALL_USERS_TEAMS, String.valueOf(userId)));
            String currentTeamUsers = Texts.orDefault(db.getValueFromMap(ID.ALL_TEAMS_USERS, String.valueOf(teamId)));
            Set<String> userTeamsSet = splitToSet(currentUserTeams);
            if (userTeamsSet.add(String.valueOf(teamId))) {
                Set<String> teamUserSet = splitToSet(currentTeamUsers);
                if (teamUserSet.add(String.valueOf(userId))) {
                    db.setValueInMap(ID.ALL_TEAMS_USERS, String.valueOf(teamId), String.join(COMMA, teamUserSet));
                }
                db.setValueInMap(ID.ALL_USERS_TEAMS, String.valueOf(userId), String.join(COMMA, userTeamsSet));
            }
            return null;
        });
    }

    @Override
    public List<TeamObject> getUserTeams(long userId) {
        return withDbContext(db -> {
            if (!existUser(userId, db)) {
                throw new OasisRuntimeException("No user is found by given id!");
            }

            String currentUserTeams = Texts.orDefault(db.getValueFromMap(ID.ALL_USERS_TEAMS, String.valueOf(userId)));
            List<String> userTeamsList = Stream.of(currentUserTeams.split(COMMA)).filter(Texts::isNotEmpty).collect(Collectors.toList());
            return userTeamsList.stream().map(userTeam -> {
                String teamJson = db.getValueFromMap(ID.ALL_TEAMS, userTeam);
                return serializationSupport.deserialize(teamJson, TeamObject.class);
            }).collect(Collectors.toList());
        });
    }

    @Override
    public List<UserObject> getTeamUsers(int teamId) {
        return withDbContext(db -> {
            if (!existTeam(teamId, db)) {
                throw new OasisRuntimeException("No team is found by given id!");
            }

            String currentTeamUsers = Texts.orDefault(db.getValueFromMap(ID.ALL_TEAMS_USERS, String.valueOf(teamId)));
            List<String> teamUserList = Stream.of(currentTeamUsers.split(COMMA)).filter(Texts::isNotEmpty).collect(Collectors.toList());
            return teamUserList.stream().map(userTeam -> {
                String userJson = db.getValueFromMap(ID.ALL_USERS, userTeam);
                return serializationSupport.deserialize(userJson, UserObject.class);
            }).collect(Collectors.toList());
        });
    }

    @Override
    public ElementDef addNewElement(int gameId, ElementDef elementDef) {
        return withDbContext(db -> {
            String id = elementDef.getId();
            String baseKey = ID.getDetailedElementDefKeyForGame(gameId);
            elementDef.setGameId(gameId);

            if (db.mapKeyExists(baseKey, id)) {
                throw new OasisRuntimeException("Element by given id already exists!");
            }

            db.setValueInMap(baseKey, id, serializationSupport.serialize(elementDef));
            updateElementMetadata(elementDef, db);
            return elementDef;
        });
    }

    @Override
    public ElementDef updateElement(int gameId, String id, ElementDef elementDef) {
        return withDbContext(db -> {
            if (!id.equals(elementDef.getId())) {
                throw new OasisRuntimeException("Provided id and element id mismatches!");
            }
            String baseKey = ID.getDetailedElementDefKeyForGame(gameId);

            if (!db.mapKeyExists(baseKey, id)) {
                throw new OasisRuntimeException("Element by given id does not exist!");
            }

            db.setValueInMap(baseKey, id, serializationSupport.serialize(elementDef));
            updateElementMetadata(elementDef, db);
            return elementDef;
        });
    }

    @Override
    public ElementDef deleteElement(int gameId, String id) {
        return withDbContext(db -> {
            String baseKey = ID.getDetailedElementDefKeyForGame(gameId);

            String valueFromMap = db.getValueFromMap(baseKey, id);
            if (Texts.isEmpty(valueFromMap)) {
                throw new OasisRuntimeException("Element by given id does not exist!");
            }

            db.removeKeyFromMap(baseKey, id);
            db.removeKeyFromMap(ID.getBasicElementDefKeyForGame(gameId), id);
            return serializationSupport.deserialize(valueFromMap, ElementDef.class);
        });
    }

    @Override
    public ElementDef readElement(int gameId, String id) {
        return withDbContext(db -> {
            String baseKey = ID.getDetailedElementDefKeyForGame(gameId);

            String valueFromMap = db.getValueFromMap(baseKey, id);
            if (Texts.isEmpty(valueFromMap)) {
                throw new OasisRuntimeException("Element by given id does not exist!");
            }

            return serializationSupport.deserialize(valueFromMap, ElementDef.class);
        });
    }

    @Override
    public SimpleElementDefinition readElementDefinition(int gameId, String id) {
        return withDbContext(db -> {
            String baseKey = ID.getBasicElementDefKeyForGame(gameId);
            String metadata = db.getValueFromMap(baseKey, id);

            if (Texts.isEmpty(metadata)) {
                return null;
            }
            SimpleElementDefinition def = serializationSupport.deserialize(metadata, SimpleElementDefinition.class);
            def.setId(id);
            return def;
        });
    }

    @Override
    public Map<String, SimpleElementDefinition> readElementDefinitions(int gameId, Collection<String> ids) {
        return withDbContext(db -> {
            String baseKey = ID.getBasicElementDefKeyForGame(gameId);

            List<String> elementValues = db.getValuesFromMap(baseKey, ids.toArray(new String[0]));
            Map<String, SimpleElementDefinition> defs = new HashMap<>();
            System.out.println(elementValues);
            for (String value : elementValues) {
                SimpleElementDefinition def = serializationSupport.deserialize(value, SimpleElementDefinition.class);
                if (def != null) {
                    defs.put(def.getId(), def);
                }
            }
            return defs;
        });
    }

    @Override
    public AttributeInfo addAttribute(int gameId, AttributeInfo newAttribute) {
        return withDbContext(db -> {
            String baseKey = ID.getGameAttributesInfoKey(gameId);
            String attrId = String.valueOf(newAttribute.getId());
            String attributes = db.getValueFromMap(baseKey, ALL_ATTRIBUTES_KEY);
            if (Texts.isEmpty(attributes)) {
                db.setValueInMap(baseKey, ALL_ATTRIBUTES_KEY, attrId);
            } else {
                Set<String> attrs = splitToSet(attributes);
                if (!attrs.add(attrId)) {
                    throw new OasisRuntimeException("Attribute already exist by given id!");
                }
                db.setValueInMap(baseKey, ALL_ATTRIBUTES_KEY, String.join(COMMA, attrs));
            }

            db.setValueInMap(baseKey, attrId, serializationSupport.serialize(newAttribute));
            return newAttribute;
        });
    }

    @Override
    public Map<Integer, AttributeInfo> readAttributesInfo(int gameId) {
        return withDbContext(db -> {
            String baseKey = ID.getGameAttributesInfoKey(gameId);
            String attributes = db.getValueFromMap(baseKey, ALL_ATTRIBUTES_KEY);
            if (Texts.isEmpty(attributes)) {
                return new HashMap<>();
            }

            List<String> elementValues = db.getValuesFromMap(baseKey, Stream.of(attributes.split(COMMA))
                    .distinct().toArray(String[]::new));
            Map<Integer, AttributeInfo> defs = new HashMap<>();
            for (String elementValue : elementValues) {
                AttributeInfo attributeInfo = serializationSupport.deserialize(elementValue, AttributeInfo.class);
                defs.put(attributeInfo.getId(), attributeInfo);
            }
            return defs;
        });
    }

    @Override
    public List<AttributeInfo> listAllAttributes(int gameId) {
        Map<Integer, AttributeInfo> attributeInfoMap = readAttributesInfo(gameId);
        return new ArrayList<>(attributeInfoMap.values());
    }

    ////////////////////////////////////////////////////////////////////////////////////////
    //
    //  Private Methods
    //
    ///////////////////////////////////////////////////////////////////////////////////////

    private Set<String> splitToSet(String text) {
        return Stream.of(text.split(COMMA)).filter(Texts::isNotEmpty).collect(Collectors.toSet());
    }

    private boolean existTeam(int teamId, DbContext db) {
        return db.mapKeyExists(ID.ALL_TEAMS, String.valueOf(teamId));
    }

    private boolean existUser(long userId, DbContext db) {
        return db.mapKeyExists(ID.ALL_USERS, String.valueOf(userId));
    }

    private UserMetadata createUserFromValue(long id, String val) {
        return new UserMetadata(id, val);
    }

    private TeamMetadata createTeamFromValue(int id, String val) {
        return new TeamMetadata(id, val);
    }

    private void updateTeamMetadata(TeamObject teamObject, DbContext db) {
        TeamMetadata teamMetadata = new TeamMetadata(teamObject.getTeamId(), teamObject.getName());
        db.setValueInMap(ID.ALL_TEAMS_NAMES, String.valueOf(teamMetadata.getTeamId()), teamMetadata.getName());
    }

    private void updateUserIndex(UserObject userObject, DbContext db) {
        db.setValueInMap(ID.ALL_USERS_INDEX, userObject.getEmail(), String.valueOf(userObject.getUserId()));
    }

    private void updateTeamIndex(TeamObject teamObject, String prevName, DbContext db) {
        if (!teamObject.getName().equals(prevName)) {
            if (prevName != null) {
                db.removeKeyFromMap(ID.ALL_TEAMS_INDEX, prevName.toLowerCase());
            }
            db.setValueInMap(ID.ALL_TEAMS_INDEX, teamObject.getName().toLowerCase(), String.valueOf(teamObject.getTeamId()));
        }
    }

    private void updateGameIndex(Game game, String prevName, DbContext db) {
        if (!game.getName().equals(prevName)) {
            if (prevName != null) {
                db.removeKeyFromMap(ID.ALL_GAMES_INDEX, prevName);
            }
            db.setValueInMap(ID.ALL_GAMES_INDEX, game.getName(), String.valueOf(game.getId()));
        }
    }

    private void updateUserMetadata(UserObject userObject, DbContext db) {
        db.setValueInMap(ID.ALL_USERS_NAMES, String.valueOf(userObject.getUserId()), userObject.getDisplayName());
    }

    private void updateElementMetadata(ElementDef def, DbContext db) {
        SimpleElementDefinition simpleElementDefinition = def.getMetadata();
        db.setValueInMap(ID.getBasicElementDefKeyForGame(def.getGameId()),
                def.getId(), serializationSupport.serialize(simpleElementDefinition));
    }

    private <T> T withDbContext(Handler<T> executor) {
        try (DbContext dbContext = dbPool.createContext()) {
            return executor.execute(dbContext);
        } catch (IOException | OasisException e) {
            throw new OasisRuntimeException(e.getMessage(), e);
        }
    }

    private interface Handler<T> {

        T execute(DbContext db) throws OasisException;

    }
}
