/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.github.oasis.services.profile.domain;

import io.github.oasis.services.profile.internal.ITeamUserGenerator;
import io.github.oasis.services.profile.internal.OasisTeamUserGenerator;
import io.github.oasis.services.profile.internal.dao.ITeamDao;
import io.github.oasis.services.profile.internal.dto.EditTeamDto;
import io.github.oasis.services.profile.internal.dto.NewTeamDto;
import io.github.oasis.services.profile.internal.dto.NewUserDto;
import io.github.oasis.services.profile.internal.dto.TeamRecord;
import io.github.oasis.services.profile.internal.exceptions.TeamNotFoundException;
import io.github.oasis.services.profile.internal.exceptions.TeamUpdateException;
import io.github.oasis.services.profile.json.NewTeamJson;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * @author Isuru Weerarathna
 */
@Component
public class Team {

    private ITeamDao team;
    private ITeamUserGenerator teamUserGenerator;

    public Team(ITeamDao team) {
        this.team = team;
    }

    public NewTeamJson addTeam(NewTeamDto newTeam) throws TeamUpdateException {
        newTeam.validate().fixName();

        NewUserDto teamUser = OasisTeamUserGenerator.INSTANCE.createTeamUser(newTeam);
        int newTeamId = team.insertTeam(newTeam, teamUser);
        return new NewTeamJson(newTeamId);
    }

    public int updateTeam(int teamId, EditTeamDto editTeam) throws TeamNotFoundException, TeamUpdateException {
        int updated = team.editTeam(teamId, editTeam);
        return updated;
    }

    public TeamRecord readTeam(int teamId) throws TeamNotFoundException {
        return team.readTeamById(teamId)
            .orElseThrow(() -> new TeamNotFoundException(String.valueOf(teamId)));
    }

    public TeamRecord readTeamWithUsers(int teamId) throws TeamNotFoundException {
        return Optional.ofNullable(team.listTeamUsers(teamId))
                .orElseThrow(() -> new TeamNotFoundException(String.valueOf(teamId)));
    }

    public List<TeamRecord> readTeams(List<Integer> teamIds) {
        return team.readTeams(teamIds);
    }
}
