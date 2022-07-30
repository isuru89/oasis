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

package io.github.oasis.core.services.helpers;

import io.github.oasis.core.TeamMetadata;
import io.github.oasis.core.UserMetadata;
import io.github.oasis.core.elements.RankInfo;
import io.github.oasis.core.elements.ElementDef;
import io.github.oasis.core.elements.SimpleElementDefinition;
import io.github.oasis.core.exception.OasisException;

import java.util.Collection;
import java.util.Map;

/**
 * @author Isuru Weerarathna
 */
public interface OasisMetadataSupport {

    Map<String, UserMetadata> readUsersByIdStrings(Collection<String> userIds) throws OasisException;
    Map<Long, UserMetadata> readUsersByIds(Collection<Long> userIds) throws OasisException;

    UserMetadata readUserMetadata(String userId) throws OasisException;
    default UserMetadata readUserMetadata(long userId) throws OasisException {
        return readUserMetadata(String.valueOf(userId));
    }

    Map<String, TeamMetadata> readTeamsByIdStrings(Collection<String> teamIds) throws OasisException;

    TeamMetadata readTeamMetadata(String teamId) throws OasisException;
    default TeamMetadata readTeamMetadata(int teamId) throws OasisException {
        return readTeamMetadata(String.valueOf(teamId));
    }

    ElementDef readFullElementDef(int gameId, String ruleId) throws OasisException;
    SimpleElementDefinition readElementDefinition(int gameId, String id) throws OasisException;
    Map<String, SimpleElementDefinition> readElementDefinitions(int gameId, Collection<String> ids) throws OasisException;
    Map<Integer, RankInfo> readAllRankInfo(int gameId) throws OasisException;
}
