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

import io.github.oasis.core.ID;
import io.github.oasis.core.Team;
import io.github.oasis.core.User;
import io.github.oasis.core.elements.AttributeInfo;
import io.github.oasis.core.elements.SimpleElementDefinition;
import io.github.oasis.core.exception.OasisException;
import io.github.oasis.core.external.Db;
import io.github.oasis.core.external.DbContext;
import io.github.oasis.core.services.helpers.OasisContextHelperSupport;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Isuru Weerarathna
 */
@Component
public class OasisContextHelper implements OasisContextHelperSupport {

    private final Db dbPool;

    public OasisContextHelper(Db dbPool) {
        this.dbPool = dbPool;
    }

    @Override
    public Map<String, User> readUsersByIdStrings(Collection<String> userIds) throws OasisException {
        try (DbContext db = dbPool.createContext()) {

            if (Utils.isEmpty(userIds)) {
                return new HashMap<>();
            }

            String[] keys = userIds.toArray(String[]::new);
            List<String> valuesFromMap = db.getValuesFromMap(ID.ALL_USERS_NAMES, keys);
            Map<String, User> userMap = new HashMap<>();
            for (int i = 0; i < keys.length; i++) {
                String userId = keys[i];
                userMap.put(userId, createUserFromValue(Numbers.asLong(userId), valuesFromMap.get(i)));
            }
            return userMap;

        } catch (IOException e) {
            throw new OasisException("Unable to read user details!", e);
        }
    }

    @Override
    public Map<Long, User> readUsersByIds(Collection<Long> userIds) throws OasisException {
        try (DbContext db = dbPool.createContext()) {

            String[] keys = userIds.stream().map(String::valueOf).toArray(String[]::new);
            List<String> valuesFromMap = db.getValuesFromMap(ID.ALL_USERS_NAMES, keys);
            Map<Long, User> userMap = new HashMap<>();
            for (int i = 0; i < keys.length; i++) {
                long userId = Numbers.asLong(keys[i]);
                userMap.put(userId, createUserFromValue(userId, valuesFromMap.get(i)));
            }
            return userMap;

        } catch (IOException e) {
            throw new OasisException("Unable to read user details!", e);
        }
    }

    @Override
    public User readUser(long userId) throws OasisException {
        try (DbContext db = dbPool.createContext()) {

            String userValue = db.getValueFromMap(ID.ALL_USERS_NAMES, String.valueOf(userId));
            return createUserFromValue(userId, userValue);

        } catch (IOException e) {
            throw new OasisException("Unable to read user details!", e);
        }
    }

    @Override
    public User readUser(String userId) throws OasisException {
        return readUser(Numbers.asLong(userId));
    }

    @Override
    public Map<String, Team> readTeamsByIdStrings(Collection<String> teamIds) throws OasisException {
        return null;
    }

    @Override
    public Map<Integer, Team> readTeamsById(Collection<Integer> teamIds) throws OasisException {
        try (DbContext db = dbPool.createContext()) {

            String[] keys = teamIds.stream().map(String::valueOf).toArray(String[]::new);
            List<String> valuesFromMap = db.getValuesFromMap(ID.ALL_TEAMS_NAMES, keys);
            Map<Integer, Team> teamMap = new HashMap<>();
            for (int i = 0; i < keys.length; i++) {
                int userId = Numbers.asInt(keys[i]);
                teamMap.put(userId, createTeamFromValue(userId, valuesFromMap.get(i)));
            }
            return teamMap;

        } catch (IOException e) {
            throw new OasisException("Unable to read team details!", e);
        }
    }

    @Override
    public Team readTeam(String teamId) throws OasisException {
        return null;
    }

    @Override
    public Team readTeam(int teamId) throws OasisException {
        return null;
    }

    @Override
    public SimpleElementDefinition readElementDefinition(int gameId, String id) throws OasisException {
        try (DbContext db = dbPool.createContext()) {

            String baseKey = ID.getBasicElementDefKeyForGame(gameId);
            List<String> valuesFromMap = db.getValuesFromMap(baseKey,
                    id + Constants.COLON + "name",
                    id + Constants.COLON + "description");

            return new SimpleElementDefinition(id, valuesFromMap.get(0), valuesFromMap.get(1));

        } catch (IOException e) {
            throw new OasisException("Unable to read team details!", e);
        }
    }

    @Override
    public Map<String, SimpleElementDefinition> readElementDefinitions(int gameId, Collection<String> ids) throws OasisException {
        try (DbContext db = dbPool.createContext()) {

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

        } catch (IOException e) {
            throw new OasisException("Unable to read team details!", e);
        }
    }

    @Override
    public Map<Integer, AttributeInfo> readAttributeInfo(int gameId) throws OasisException {
        try (DbContext db = dbPool.createContext()) {

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

        } catch (IOException e) {
            throw new OasisException("Unable to read team details!", e);
        }
    }

    private User createUserFromValue(long id, String val) {
        return new User(id, val);
    }

    private Team createTeamFromValue(int id, String val) {
        return new Team(id, val);
    }
}
