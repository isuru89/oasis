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

package io.github.oasis.services.profile;

import io.github.oasis.services.common.internal.events.profile.TeamCreatedEvent;
import io.github.oasis.services.common.internal.events.profile.UserAllocatedEvent;
import io.github.oasis.services.common.internal.events.profile.UserCreatedEvent;
import io.github.oasis.services.common.internal.events.profile.UserDeactivatedEvent;
import io.github.oasis.services.common.internal.events.profile.UserDeallocatedEvent;
import io.github.oasis.services.profile.domain.Assignment;
import io.github.oasis.services.profile.domain.Team;
import io.github.oasis.services.profile.domain.User;
import io.github.oasis.services.profile.domain.UserStatus;
import io.github.oasis.services.profile.internal.dto.EditTeamDto;
import io.github.oasis.services.profile.internal.dto.EditUserDto;
import io.github.oasis.services.profile.internal.dto.NewTeamDto;
import io.github.oasis.services.profile.internal.dto.NewUserDto;
import io.github.oasis.services.profile.internal.dto.TeamRecord;
import io.github.oasis.services.profile.internal.exceptions.UserNotFoundException;
import io.github.oasis.services.profile.json.NewTeamJson;
import io.github.oasis.services.profile.json.NewUserJson;
import io.github.oasis.services.profile.json.TeamJson;
import io.github.oasis.services.profile.json.TeamListJson;
import io.github.oasis.services.profile.json.UserJson;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Isuru Weerarathna
 */
@Component
public class ProfileAggregate {

    private ApplicationEventPublisher publisher;
    private User user;
    private Team team;
    private Assignment assignment;

    public ProfileAggregate(ApplicationEventPublisher publisher, User user, Team team, Assignment assignment) {
        this.publisher = publisher;
        this.user = user;
        this.team = team;
        this.assignment = assignment;
    }

    /////////////////////////////////////////////////////////////////////////////
    //
    //  USER - ACTIONS
    //
    /////////////////////////////////////////////////////////////////////////////

    public NewUserJson addUser(NewUserDto newUserInfo) {
        NewUserJson newUserJson = user.addUser(newUserInfo);
        publisher.publishEvent(new UserCreatedEvent(newUserJson.getId()));
        return newUserJson;
    }

    public UserJson editUser(int userId, EditUserDto editUserInfo) {
        return UserJson.from(user.editUser(userId, editUserInfo));
    }

    public void deactivateUser(int userId) {
        user.deleteUser(userId);
        publisher.publishEvent(new UserDeactivatedEvent(userId));
    }

    public UserJson readUserById(int userId) {
        return UserJson.from(user.readUserById(userId));
    }

    public UserJson readUserByEmail(String userEmail) {
        return UserJson.from(user.readUserByEmail(userEmail)
            .orElseThrow(() -> new UserNotFoundException(userEmail)));
    }

    public UserJson readUserWithTeams(int userId) {
        return UserJson.from(user.readUserWithTeams(userId));
    }

    public void updateUserStatus(int userId, UserStatus newStatus) {
        user.updateUserStatus(userId, newStatus.getId());
    }

    /////////////////////////////////////////////////////////////////////////////
    //
    //  TEAM - ACTIONS
    //
    /////////////////////////////////////////////////////////////////////////////

    public NewTeamJson addTeam(NewTeamDto newTeamInfo) {
        NewTeamJson newTeamJson = team.addTeam(newTeamInfo);
        publisher.publishEvent(new TeamCreatedEvent(newTeamJson.getId()));
        return newTeamJson;
    }

    public void editTeam(int teamId, EditTeamDto editTeamInfo) {
        team.updateTeam(teamId, editTeamInfo);
    }

    public TeamJson readTeam(int teamId) {
        return TeamJson.from(team.readTeam(teamId));
    }

    public TeamJson readTeamWithUsers(int teamId) {
        return TeamJson.from(team.readTeamWithUsers(teamId));
    }

    public TeamListJson readTeams(List<Integer> teamIds) {
        List<TeamRecord> teamRecords = team.readTeams(teamIds);
        return TeamListJson.from(teamIds, teamRecords);
    }

    /////////////////////////////////////////////////////////////////////////////
    //
    //  ASSIGNMENTS - ACTIONS
    //
    /////////////////////////////////////////////////////////////////////////////

    public void allocateUserToTeam(int userId, int teamId) {
        boolean allocated = assignment.assignToTeam(userId, teamId);
        if (allocated) {
            publisher.publishEvent(new UserAllocatedEvent(userId, teamId));
        }
    }

    public void deallocateUserFromTeam(int userId, int teamId) {
        boolean deallocated = assignment.removeFromTeam(userId, teamId);
        if (deallocated) {
            publisher.publishEvent(new UserDeallocatedEvent(userId, teamId));
        }
    }

}
