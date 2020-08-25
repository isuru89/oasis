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
import io.github.oasis.core.elements.AttributeInfo;
import io.github.oasis.core.elements.ElementDef;
import io.github.oasis.core.elements.SimpleElementDefinition;
import io.github.oasis.core.exception.OasisException;
import io.github.oasis.core.exception.OasisRuntimeException;
import io.github.oasis.core.external.Db;
import io.github.oasis.core.external.DbContext;
import io.github.oasis.core.external.Mapped;
import io.github.oasis.core.external.OasisRepository;
import io.github.oasis.core.model.TeamObject;
import io.github.oasis.core.model.UserObject;
import io.github.oasis.core.services.SerializationSupport;
import io.github.oasis.core.services.helpers.OasisMetadataSupport;
import io.github.oasis.core.utils.Constants;
import io.github.oasis.core.utils.Numbers;
import io.github.oasis.core.utils.Texts;
import io.github.oasis.core.utils.Utils;
import org.springframework.stereotype.Component;

import java.io.IOException;
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

    private final Db dbPool;
    private final SerializationSupport serializationSupport;

    public RedisRepository(Db dbPool, SerializationSupport serializationSupport) {
        this.dbPool = dbPool;
        this.serializationSupport = serializationSupport;
    }

    @Override
    public Game addNewGame(Game game) {
        return withDbContext(db -> {
            int gameId = db.MAP(ID.OASIS_ID_STORAGE).incrementByOne("games");
            game.setId(gameId);

            String gameIdStr = String.valueOf(gameId);
            db.setValueInMap(ID.ALL_GAMES, gameIdStr, serializationSupport.serialize(game));
            return game;
        });
    }

    @Override
    public Game updateGame(int gameId, Game game) {
        return withDbContext(db -> {
            String gameIdStr = String.valueOf(gameId);
            db.setValueInMap(ID.ALL_GAMES, gameIdStr, serializationSupport.serialize(game));
            return game;
        });
    }

    @Override
    public Game readGame(int gameId) {
        return withDbContext(db -> {
            String gameStr = db.getValueFromMap(ID.ALL_GAMES, String.valueOf(gameId));
            return serializationSupport.deserialize(gameStr, Game.class);
        });
    }

    @Override
    public Game deleteGame(int gameId) {
        return withDbContext(db -> {
            Mapped gamesMap = db.MAP(ID.ALL_GAMES);
            String subKey = String.valueOf(gameId);
            String gameStr = gamesMap.getValue(subKey);
            gamesMap.remove(subKey);
            return serializationSupport.deserialize(gameStr, Game.class);
        });
    }

    @Override
    public List<Game> listGames() {
        return null;
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
                userMap.put(userId, createUserFromValue(Numbers.asLong(userId), valuesFromMap.get(i)));
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
        return null;
    }

    @Override
    public UserMetadata readUserMetadata(String userId) {
        return null;
    }

    @Override
    public UserObject readUser(long userId) {
        return null;
    }

    @Override
    public UserObject readUser(String email) {
        return null;
    }

    @Override
    public Map<String, TeamMetadata> readTeamsByIdStrings(Collection<String> teamIds) {
        return null;
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
        return null;
    }

    @Override
    public TeamMetadata readTeamMetadata(int teamId) {
        return null;
    }

    @Override
    public UserObject addUser(UserObject newUser) {
        return withDbContext(db -> {
            long userId = db.MAP(ID.OASIS_ID_STORAGE).incrementBy("users", 1L);
            String userIdStr = String.valueOf(userId);
            newUser.setUserId(userId);
            db.setValueInMap(ID.ALL_USERS, userIdStr, serializationSupport.serialize(newUser));
            updateUserMetadata(newUser, db);
            return newUser;
        });
    }

    @Override
    public UserObject updateUser(long userId, UserObject updatedUser) {
        return withDbContext(db -> {
            String userIdStr = String.valueOf(userId);
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
            db.removeKeyFromMap(ID.ALL_USERS_NAMES, userIdStr);
            db.removeKeyFromMap(ID.ALL_USERS, userIdStr);
            return serializationSupport.deserialize(userVal, UserObject.class);
        });
    }

    @Override
    public TeamObject addTeam(TeamObject teamObject) {
        return withDbContext(db -> {
            int teamId = db.MAP(ID.OASIS_ID_STORAGE).incrementByOne("teams");
            String teamIdStr = String.valueOf(teamId);
            teamObject.setTeamId(teamId);
            db.setValueInMap(ID.ALL_TEAMS, teamIdStr, serializationSupport.serialize(teamObject));
            updateTeamMetadata(teamObject, db);
            return teamObject;
        });
    }

    @Override
    public TeamObject readTeam(int teamId) {
        return withDbContext(db -> {
            String value = db.getValueFromMap(ID.ALL_TEAMS, String.valueOf(teamId));
            return serializationSupport.deserialize(value, TeamObject.class);
        });
    }

    @Override
    public SimpleElementDefinition readElementDefinition(int gameId, String id) {
        return withDbContext(db -> {
            String baseKey = ID.getBasicElementDefKeyForGame(gameId);
            List<String> valuesFromMap = db.getValuesFromMap(baseKey,
                    id + Constants.COLON + "name",
                    id + Constants.COLON + "description");

            return new SimpleElementDefinition(id, valuesFromMap.get(0), valuesFromMap.get(1));
        });
    }

    @Override
    public Map<String, SimpleElementDefinition> readElementDefinitions(int gameId, Collection<String> ids) {
        return withDbContext(db -> {
            String baseKey = ID.getBasicElementDefKeyForGame(gameId);
            List<String> subKeys = ids.stream().flatMap(id -> Stream.of(id + ":name", id + ":description"))
                    .collect(Collectors.toList());

            List<String> elementValues = db.getValuesFromMap(baseKey, subKeys.toArray(new String[0]));
            Map<String, SimpleElementDefinition> defs = new HashMap<>();
            for (int i = 0; i < subKeys.size(); i += 2) {
                String ruleId = subKeys.get(i).split(Constants.COLON)[0];
                defs.put(ruleId, new SimpleElementDefinition(ruleId, elementValues.get(i), elementValues.get(i + 1)));
            }
            return defs;
        });
    }

    @Override
    public Map<Integer, AttributeInfo> readAttributeInfo(int gameId) {
        return withDbContext(db -> {
            String baseKey = ID.getGameAttributesInfoKey(gameId);
            String attributes = db.getValueFromMap(baseKey, "attributes");
            if (Texts.isEmpty(attributes)) {
                return new HashMap<>();
            }

            List<String> attrKeys = Stream.of(attributes.split(",")).distinct().collect(Collectors.toList());
            String[] subKeys = attrKeys.stream()
                    .flatMap(attr -> Stream.of(attr + ":name", attr + ":order"))
                    .toArray(String[]::new);

            List<String> elementValues = db.getValuesFromMap(baseKey, subKeys);
            Map<Integer, AttributeInfo> defs = new HashMap<>();
            for (int i = 0; i < subKeys.length; i += 2) {
                int attrId = Numbers.asInt(subKeys[i].split(Constants.COLON)[0]);
                defs.put(attrId, new AttributeInfo(attrId, elementValues.get(i), Numbers.asInt(elementValues.get(i + 1))));
            }
            return defs;
        });
    }

    @Override
    public TeamObject updateTeam(int teamId, TeamObject updatedTeam) {
        return withDbContext(db -> {
            db.setValueInMap(ID.ALL_TEAMS, String.valueOf(teamId), serializationSupport.serialize(updatedTeam));
            updateTeamMetadata(updatedTeam, db);
            return updatedTeam;
        });
    }

    @Override
    public boolean existTeam(String teamName) {
        return withDbContext(db -> db.MAP(ID.ALL_TEAMS_INDEX).existKey(teamName));
    }

    @Override
    public List<TeamObject> searchTeam(String teamName, int offset, int maxRecords) {
        return null;
    }

    @Override
    public void removeUserFromTeam(long userId, int teamId) {
        withDbContext(db -> {
            String currentUserTeams = db.getValueFromMap(ID.ALL_USERS_TEAMS, String.valueOf(userId));
            String currentTeamUsers = db.getValueFromMap(ID.ALL_TEAMS_USERS, String.valueOf(teamId));
            Set<String> userTeamsSet = Stream.of(currentUserTeams.split(COMMA)).collect(Collectors.toSet());
            if (userTeamsSet.remove(String.valueOf(teamId))) {
                Set<String> teamUserSet = Stream.of(currentTeamUsers.split(COMMA)).collect(Collectors.toSet());
                if (teamUserSet.remove(String.valueOf(userId))) {
                    db.setValueInMap(ID.ALL_TEAMS_USERS, String.valueOf(teamId), String.join(COMMA, teamUserSet));
                }
                db.setValueInMap(ID.ALL_USERS_TEAMS, String.valueOf(userId), String.join(COMMA, userTeamsSet));
            }
            return null;
        });
    }

    @Override
    public void addUserToTeam(long userId, int teamId) {
        withDbContext(db -> {
            String currentUserTeams = db.getValueFromMap(ID.ALL_USERS_TEAMS, String.valueOf(userId));
            String currentTeamUsers = db.getValueFromMap(ID.ALL_TEAMS_USERS, String.valueOf(teamId));
            Set<String> userTeamsSet = Stream.of(currentUserTeams.split(COMMA)).collect(Collectors.toSet());
            if (userTeamsSet.add(String.valueOf(teamId))) {
                Set<String> teamUserSet = Stream.of(currentTeamUsers.split(COMMA)).collect(Collectors.toSet());
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
            String currentUserTeams = db.getValueFromMap(ID.ALL_USERS_TEAMS, String.valueOf(userId));
            List<String> userTeamsList = Stream.of(currentUserTeams.split(COMMA)).collect(Collectors.toList());
            return userTeamsList.stream().map(userTeam -> {
                String teamJson = db.getValueFromMap(ID.ALL_TEAMS, userTeam);
                return serializationSupport.deserialize(teamJson, TeamObject.class);
            }).collect(Collectors.toList());
        });
    }

    @Override
    public List<UserObject> getTeamUsers(int teamId) {
        return withDbContext(db -> {
            String currentTeamUsers = db.getValueFromMap(ID.ALL_TEAMS_USERS, String.valueOf(teamId));
            List<String> teamUserList = Stream.of(currentTeamUsers.split(COMMA)).collect(Collectors.toList());
            return teamUserList.stream().map(userTeam -> {
                String userJson = db.getValueFromMap(ID.ALL_USERS, userTeam);
                return serializationSupport.deserialize(userJson, UserObject.class);
            }).collect(Collectors.toList());
        });
    }

    @Override
    public ElementDef addNewElement(int gameId, ElementDef elementDef) {
        return null;
    }

    @Override
    public ElementDef updateElement(int gameId, String id, ElementDef elementDef) {
        return null;
    }

    @Override
    public ElementDef deleteElement(int gameId, String id) {
        return null;
    }

    @Override
    public ElementDef readElement(int gameId, String id) {
        return null;
    }

    @Override
    public AttributeInfo addAttribute(int gameId, AttributeInfo newAttribute) {
        return withDbContext(db -> {
            String baseKey = ID.getGameAttributesInfoKey(gameId);
            String attrId = String.valueOf(newAttribute.getId());
            String attributes = db.getValueFromMap(baseKey, "attributes");
            if (Texts.isEmpty(attributes)) {
                db.setValueInMap(baseKey, "attributes", attrId);
            } else {
                Set<String> attrs = Stream.of(attributes.split(COMMA)).collect(Collectors.toSet());
                attrs.add(attrId);
                db.setValueInMap(baseKey, "attributes", String.join(COMMA, attrs));
            }

            db.setValueInMap(baseKey, attrId + ":name", newAttribute.getName());
            db.setValueInMap(baseKey, attrId + ":order", String.valueOf(newAttribute.getOrder()));
            return newAttribute;
        });
    }

    @Override
    public List<AttributeInfo> listAllAttributes(int gameId) {
        return null;
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

    private void updateUserMetadata(UserObject userObject, DbContext db) {
        db.setValueInMap(ID.ALL_USERS_NAMES, String.valueOf(userObject.getUserId()), userObject.getDisplayName());
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
