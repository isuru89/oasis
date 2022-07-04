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

package io.github.oasis.core.services.api.services;

import io.github.oasis.core.TeamMetadata;
import io.github.oasis.core.external.PaginatedResult;
import io.github.oasis.core.model.TeamObject;
import io.github.oasis.core.services.api.to.TeamCreateRequest;
import io.github.oasis.core.services.api.to.TeamUpdateRequest;
import io.github.oasis.core.services.exceptions.OasisApiException;

/**
 * @author Isuru Weerarathna
 */
public interface ITeamManagementService {

    TeamObject addTeam(TeamCreateRequest request);

    TeamObject readTeamByName(String name) throws OasisApiException;

    TeamObject readTeam(int teamId) throws OasisApiException;

    TeamObject updateTeam(int teamId, TeamUpdateRequest request);

    PaginatedResult<TeamMetadata> searchTeam(String teamPrefix, String offset, int maxSize);

}
