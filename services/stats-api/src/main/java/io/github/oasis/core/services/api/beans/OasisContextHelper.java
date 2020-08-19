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
import io.github.oasis.core.exception.OasisException;
import io.github.oasis.core.external.Db;
import io.github.oasis.core.external.DbContext;
import io.github.oasis.core.services.helpers.OasisContextHelperSupport;
import io.github.oasis.core.utils.Numbers;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private User createUserFromValue(long id, String val) {
        return new User(id, val);
    }

    private Team createTeamFromValue(int id, String val) {
        return new Team(id, val);
    }
}
