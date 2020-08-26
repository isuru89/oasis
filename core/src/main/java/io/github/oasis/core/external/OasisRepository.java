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

package io.github.oasis.core.external;

import io.github.oasis.core.Game;
import io.github.oasis.core.TeamMetadata;
import io.github.oasis.core.elements.AttributeInfo;
import io.github.oasis.core.elements.ElementDef;
import io.github.oasis.core.model.TeamObject;
import io.github.oasis.core.model.UserObject;

import java.util.List;

/**
 * @author Isuru Weerarathna
 */
public interface OasisRepository {

    Game addNewGame(Game game);
    Game updateGame(int gameId, Game game);
    Game readGame(int gameId);
    Game deleteGame(int gameId);
    boolean existsGame(String gameName);
    List<Game> listGames();

    UserObject readUser(long userId);
    UserObject readUser(String email);
    UserObject addUser(UserObject newUser);
    UserObject updateUser(long userId, UserObject updatedUser);
    UserObject deleteUser(long userId);

    TeamObject addTeam(TeamObject teamObject);
    TeamObject readTeam(int teamId);
    TeamObject updateTeam(int teamId, TeamObject updatedTeam);
    boolean existTeam(String teamName);
    PaginatedResult<TeamMetadata> searchTeam(String teamName, String offset, int maxRecords);

    void removeUserFromTeam(long userId, int teamId);
    void addUserToTeam(long userId, int teamId);
    List<TeamObject> getUserTeams(long userId);
    List<UserObject> getTeamUsers(int teamId);

    ElementDef addNewElement(int gameId, ElementDef elementDef);
    ElementDef updateElement(int gameId, String id, ElementDef elementDef);
    ElementDef deleteElement(int gameId, String id);
    ElementDef readElement(int gameId, String id);

    AttributeInfo addAttribute(int gameId, AttributeInfo newAttribute);
    List<AttributeInfo> listAllAttributes(int gameId);


}
