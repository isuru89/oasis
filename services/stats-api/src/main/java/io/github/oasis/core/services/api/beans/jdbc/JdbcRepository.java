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

package io.github.oasis.core.services.api.beans.jdbc;

import io.github.oasis.core.Game;
import io.github.oasis.core.TeamMetadata;
import io.github.oasis.core.elements.AttributeInfo;
import io.github.oasis.core.elements.ElementDef;
import io.github.oasis.core.external.OasisRepository;
import io.github.oasis.core.external.PaginatedResult;
import io.github.oasis.core.model.EventSource;
import io.github.oasis.core.model.TeamObject;
import io.github.oasis.core.model.UserObject;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * JDBC database implementation for admin database.
 *
 * @author Isuru Weerarathna
 */
@Component("jdbc")
public class JdbcRepository implements OasisRepository {

    @Override
    public EventSource addEventSource(EventSource eventSource) {
        return null;
    }

    @Override
    public EventSource deleteEventSource(int id) {
        return null;
    }

    @Override
    public EventSource readEventSource(int id) {
        return null;
    }

    @Override
    public EventSource readEventSource(String token) {
        return null;
    }

    @Override
    public List<EventSource> listAllEventSources() {
        return null;
    }

    @Override
    public List<EventSource> listAllEventSourcesOfGame(int gameId) {
        return null;
    }

    @Override
    public void addEventSourceToGame(int sourceId, int gameId) {

    }

    @Override
    public void removeEventSourceFromGame(int sourceId, int gameId) {

    }

    @Override
    public Game addNewGame(Game game) {
        return null;
    }

    @Override
    public Game updateGame(int gameId, Game game) {
        return null;
    }

    @Override
    public Game readGame(int gameId) {
        return null;
    }

    @Override
    public Game deleteGame(int gameId) {
        return null;
    }

    @Override
    public boolean existsGame(String gameName) {
        return false;
    }

    @Override
    public List<Game> listGames() {
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
    public UserObject addUser(UserObject newUser) {
        return null;
    }

    @Override
    public boolean existsUser(String email) {
        return false;
    }

    @Override
    public boolean existsUser(long userId) {
        return false;
    }

    @Override
    public UserObject updateUser(long userId, UserObject updatedUser) {
        return null;
    }

    @Override
    public UserObject deleteUser(long userId) {
        return null;
    }

    @Override
    public TeamObject addTeam(TeamObject teamObject) {
        return null;
    }

    @Override
    public TeamObject readTeam(int teamId) {
        return null;
    }

    @Override
    public TeamObject updateTeam(int teamId, TeamObject updatedTeam) {
        return null;
    }

    @Override
    public boolean existsTeam(String teamName) {
        return false;
    }

    @Override
    public boolean existsTeam(int teamId) {
        return false;
    }

    @Override
    public PaginatedResult<TeamMetadata> searchTeam(String teamName, String offset, int maxRecords) {
        return null;
    }

    @Override
    public void removeUserFromTeam(long userId, int gameId, int teamId) {

    }

    @Override
    public void addUserToTeam(long userId, int gameId, int teamId) {

    }

    @Override
    public List<TeamObject> getUserTeams(long userId) {
        return null;
    }

    @Override
    public List<UserObject> getTeamUsers(int teamId) {
        return null;
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
        return null;
    }

    @Override
    public List<AttributeInfo> listAllAttributes(int gameId) {
        return null;
    }
}
