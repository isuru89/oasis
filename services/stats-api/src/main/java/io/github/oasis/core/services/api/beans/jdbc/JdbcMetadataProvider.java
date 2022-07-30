/*
 *
 *  * Licensed to the Apache Software Foundation (ASF) under one
 *  * or more contributor license agreements.  See the NOTICE file
 *  * distributed with this work for additional information
 *  * regarding copyright ownership.  The ASF licenses this file
 *  * to you under the Apache License, Version 2.0 (the
 *  * "License"); you may not use this file except in compliance
 *  * with the License.  You may obtain a copy of the License at
 *  *
 *  *    http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing,
 *  * software distributed under the License is distributed on an
 *  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  * KIND, either express or implied.  See the License for the
 *  * specific language governing permissions and limitations
 *  * under the License.
 *
 *
 */

package io.github.oasis.core.services.api.beans.jdbc;

import io.github.oasis.core.ID;
import io.github.oasis.core.TeamMetadata;
import io.github.oasis.core.UserMetadata;
import io.github.oasis.core.elements.RankInfo;
import io.github.oasis.core.elements.ElementDef;
import io.github.oasis.core.elements.SimpleElementDefinition;
import io.github.oasis.core.external.OasisRepository;
import io.github.oasis.core.model.PlayerObject;
import io.github.oasis.core.model.TeamObject;
import io.github.oasis.core.services.annotations.AdminDbRepository;
import io.github.oasis.core.services.helpers.OasisMetadataSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Read actual definitions from admin db and returns them as metadata references.
 *
 * @author Isuru Weerarathna
 */
@Component("jdbcMetadataProvider")
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
public class JdbcMetadataProvider implements OasisMetadataSupport {

    private OasisRepository adminDbRepository;

    // we use this self reference to activate caching within instance itself.
    @Autowired
    private JdbcMetadataProvider self;

    public JdbcMetadataProvider(@AdminDbRepository OasisRepository adminDbRepository) {
        this.adminDbRepository = adminDbRepository;
    }

    @Override
    public Map<String, UserMetadata> readUsersByIdStrings(Collection<String> userIds) {
        Map<String, UserMetadata> metadataMap = new HashMap<>();
        for (String userId : userIds) {
            metadataMap.put(userId, self.readUserMetadata(userId));
        }
        return metadataMap;
    }

    @Override
    public Map<Long, UserMetadata> readUsersByIds(Collection<Long> userIds) {
        Map<Long, UserMetadata> metadataMap = new HashMap<>();
        for (Long userId : userIds) {
            metadataMap.put(userId, self.readUserMetadata(userId));
        }
        return metadataMap;
    }

    @Cacheable(ID.CACHE_USERS_META)
    @Override
    public UserMetadata readUserMetadata(long userId) {
        PlayerObject playerObject = adminDbRepository.readPlayer(userId);

        return new UserMetadata(
                playerObject.getId(),
                playerObject.getDisplayName(),
                playerObject.getTimeZone(),
                playerObject.getGender() != null ? playerObject.getGender().name() : null
        );
    }

    @Cacheable(ID.CACHE_TEAMS_META)
    @Override
    public TeamMetadata readTeamMetadata(int teamId) {
        TeamObject teamObject = adminDbRepository.readTeam(teamId);

        TeamMetadata metadata = new TeamMetadata();
        metadata.setTeamId(teamObject.getId());
        metadata.setName(teamObject.getName());
        return metadata;
    }

    @Override
    public UserMetadata readUserMetadata(String userId) {
        return self.readUserMetadata(Long.parseLong(userId));
    }

    @Override
    public Map<String, TeamMetadata> readTeamsByIdStrings(Collection<String> teamIds) {
        Map<String, TeamMetadata> metadataMap = new HashMap<>();
        for (String teamId : teamIds) {
            metadataMap.put(teamId, self.readTeamMetadata(teamId));
        }
        return metadataMap;
    }

    @Override
    public TeamMetadata readTeamMetadata(String teamId) {
        return self.readTeamMetadata(Integer.parseInt(teamId));
    }

    @Cacheable(value = ID.CACHE_ELEMENTS, key = "{#gameId, #id}", unless = "#result == null")
    @Override
    public ElementDef readFullElementDef(int gameId, String id) {
        return adminDbRepository.readElement(gameId, id);
    }

    @Cacheable(value = ID.CACHE_ELEMENTS_META, key = "{#gameId, #id}", unless = "#result == null")
    @Override
    public SimpleElementDefinition readElementDefinition(int gameId, String id) {
        ElementDef elementDef = adminDbRepository.readElement(gameId, id);
        if (elementDef == null) {
            return null;
        }

        return new SimpleElementDefinition(
            elementDef.getMetadata().getId(),
            elementDef.getMetadata().getName(),
            elementDef.getMetadata().getDescription()
        );
    }

    @Override
    public Map<String, SimpleElementDefinition> readElementDefinitions(int gameId, Collection<String> ids) {
        Map<String, SimpleElementDefinition> metadataMap = new HashMap<>();
        for (String elementId : ids) {
            metadataMap.put(elementId, self.readElementDefinition(gameId, elementId));
        }
        return metadataMap;
    }

    @Cacheable(ID.CACHE_RANKS)
    @Override
    public Map<Integer, RankInfo> readAllRankInfo(int gameId) {
        return adminDbRepository.listAllRanks(gameId).stream()
                .collect(Collectors.toMap(RankInfo::getId, Function.identity()));
    }

    public void setSelf(JdbcMetadataProvider self) {
        this.self = self;
    }

    public void setAdminDbRepository(OasisRepository adminDbRepository) {
        this.adminDbRepository = adminDbRepository;
    }
}
