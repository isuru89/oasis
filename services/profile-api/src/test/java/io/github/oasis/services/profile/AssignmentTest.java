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
import io.github.oasis.services.common.internal.events.profile.UserDeallocatedEvent;
import io.github.oasis.services.profile.domain.Assignment;
import io.github.oasis.services.profile.domain.Team;
import io.github.oasis.services.profile.domain.User;
import io.github.oasis.services.profile.internal.dao.ITeamDao;
import io.github.oasis.services.profile.internal.dao.IUserDao;
import io.github.oasis.services.profile.internal.dto.NewTeamDto;
import io.github.oasis.services.profile.internal.dto.NewUserDto;
import io.github.oasis.services.profile.internal.dto.TeamRecord;
import io.github.oasis.services.profile.internal.dto.UserRecord;
import io.github.oasis.services.profile.json.NewTeamJson;
import io.github.oasis.services.profile.json.NewUserJson;
import io.github.oasis.services.profile.json.TeamJson;
import io.github.oasis.services.profile.json.UserJson;
import io.github.oasis.services.profile.utils.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static io.github.oasis.services.profile.utils.TestUtils.SINGLE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Isuru Weerarathna
 */
@DisplayName("User Allocations/Deallocations")
class AssignmentTest extends AbstractTest {

    @Autowired private ITeamDao teamDao;
    @Autowired private IUserDao userDao;

    @MockBean
    private ApplicationEventPublisher publisher;

    private ProfileAggregate profile;

    @Captor private ArgumentCaptor<UserCreatedEvent> userCreatedEventArgumentCaptor;
    @Captor private ArgumentCaptor<TeamCreatedEvent> teamCreatedEventArgumentCaptor;
    @Captor private ArgumentCaptor<UserAllocatedEvent> userAllocatedEventArgumentCaptor;
    @Captor private ArgumentCaptor<UserDeallocatedEvent> userDeallocatedEventArgumentCaptor;

    @BeforeEach
    void beforeEach() {
        MockitoAnnotations.initMocks(this);
        Team team = new Team(teamDao);
        User user = new User(userDao);
        Assignment assignment = new Assignment(teamDao);
        profile = new ProfileAggregate(publisher, user, team, assignment);

        super.runBeforeEach();
    }

    @Test
    @DisplayName("should be able add any user to any team")
    void testAddUserToTeam() {
        List<Integer> teamsIds = createTeams(5);
        List<Integer> userIds = createUsers(10);

        int firstTeam = teamsIds.get(0);
        int userId = userIds.get(1);
        profile.allocateUserToTeam(userId, firstTeam);

        verifyUserAllocationEventFired(userId, firstTeam);
    }

    @Test
    @DisplayName("should be able read team with users")
    void testReadTeamWithUsers() {
        List<Integer> teamsIds = createTeams(5);
        List<Integer> userIds = createUsers(10);

        int firstTeam = teamsIds.get(0);
        List<Integer> userList = userIds.subList(6, 9);
        userList.forEach(userId -> profile.allocateUserToTeam(userId, firstTeam));

        verifyUserAllocationEventFiredNTimes(userList.size());

        TeamJson teamJson = profile.readTeamWithUsers(firstTeam);
        assertNotNull(teamJson);
        assertEquals(firstTeam, teamJson.getId());
        assertTrue(teamJson.isActive());
        assertEquals(userList.size() + 1, teamJson.getUsers().size());
        Set<Integer> storedUserIds = teamJson.getUsers().stream()
                .map(UserRecord::getId).collect(Collectors.toSet());
        userList.forEach(userId -> assertTrue(storedUserIds.contains(userId)));
    }

    @Test
    @DisplayName("should be able read users with team")
    void testReadUsersWithTeam() {
        List<Integer> teamsIds = createTeams(10);
        List<Integer> userIds = createUsers(6);

        int firstUser = userIds.get(0);
        List<Integer> teamList = teamsIds.subList(1, 4);
        teamList.forEach(teamId -> profile.allocateUserToTeam(firstUser, teamId));

        verifyUserAllocationEventFiredNTimes(teamList.size());

        UserJson userJson = profile.readUserWithTeams(firstUser);
        assertNotNull(userJson);
        assertEquals(firstUser, userJson.getId());
        assertTrue(userJson.isActive());
        assertEquals(teamList.size(), userJson.getTeams().size());
        Set<Integer> storedTeamIds = userJson.getTeams().stream()
                .map(TeamRecord::getId).collect(Collectors.toSet());
        teamList.forEach(teamId -> assertTrue(storedTeamIds.contains(teamId)));
    }

    @Test
    @DisplayName("should be able deallocate users from team")
    void testDeallocateFromTeam() {
        List<Integer> teamsIds = createTeams(8);
        List<Integer> userIds = createUsers(1);

        int firstUser = userIds.get(0);
        List<Integer> teamList = teamsIds.subList(1, 4);
        teamList.forEach(teamId -> profile.allocateUserToTeam(firstUser, teamId));

        verifyUserAllocationEventFiredNTimes(teamList.size());

        // verify user perspective
        {
            UserJson userJson = profile.readUserWithTeams(firstUser);
            assertEquals(teamList.size(), userJson.getTeams().size());

            TeamJson teamJson = profile.readTeamWithUsers(teamList.get(0));
            assertEquals(2, teamJson.getUsers().size());
        }

        int removedTeamId = teamList.get(0);
        profile.deallocateUserFromTeam(firstUser, removedTeamId);
        verifyUserDeAllocationEventFired(firstUser, removedTeamId);

        {
            UserJson userJson = profile.readUserWithTeams(firstUser);
            assertEquals(teamList.size() - 1, userJson.getTeams().size());
            assertTrue(userJson.getTeams().stream().noneMatch(t -> t.getId() == removedTeamId));

            TeamJson teamJson = profile.readTeamWithUsers(removedTeamId);
            assertEquals(1, teamJson.getUsers().size());
        }
    }

    private List<Integer> createTeams(int teamSize) {
        List<Integer> teamIds = new ArrayList<>();
        for (int i = 0; i < teamSize; i++) {
            NewTeamDto teamDto = new NewTeamDto();
            teamDto.setName("Team-" + i);
            teamDto.setMotto("No Mercy from " + i);
            teamDto.setAvatar("/images/t/team-" + i + ".jpg");
            NewTeamJson newTeamJson = profile.addTeam(teamDto);
            teamIds.add(newTeamJson.getId());
        }
        verifyTeamCreatedEventFiredNTimes(teamSize);
        return teamIds;
    }

    private List<Integer> createUsers(int size) {
        List<Integer> userIds = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            NewUserDto userDto = new NewUserDto();
            userDto.setEmail("user" + i + "@gmail.com");
            userDto.setNickname("User-" + i);
            userDto.setAvatar("/images/u/user-" + i + ".jpg");
            NewUserJson newUserJson = profile.addUser(userDto);
            userIds.add(newUserJson.getId());
        }
        verifyUserCreatedEventFiredNTimes(size);
        return userIds;
    }

    void verifyNoEventFired() {
        Mockito.verifyZeroInteractions(publisher);
    }

    private void verifyTeamCreatedEventFiredNTimes(int times) {
        Mockito.verify(publisher, TestUtils.times(times))
                .publishEvent(teamCreatedEventArgumentCaptor.capture());
        Mockito.clearInvocations(publisher);
    }

    private void verifyUserAllocationEventFiredNTimes(int times) {
        Mockito.verify(publisher, TestUtils.times(times))
                .publishEvent(userAllocatedEventArgumentCaptor.capture());
        Mockito.clearInvocations(publisher);
    }

    private void verifyUserAllocationEventFired(int userId, int teamId) {
        Mockito.verify(publisher, SINGLE)
                .publishEvent(userAllocatedEventArgumentCaptor.capture());
        UserAllocatedEvent event = userAllocatedEventArgumentCaptor.getValue();
        assertEquals(userId, event.getUserId());
        assertEquals(teamId, event.getTeamId());
        Mockito.clearInvocations(publisher);
    }

    private void verifyUserDeAllocationEventFired(int userId, int teamId) {
        Mockito.verify(publisher, SINGLE)
                .publishEvent(userDeallocatedEventArgumentCaptor.capture());
        UserDeallocatedEvent event = userDeallocatedEventArgumentCaptor.getValue();
        assertEquals(userId, event.getUserId());
        assertEquals(teamId, event.getTeamId());
        Mockito.clearInvocations(publisher);
    }

    private void verifyUserCreatedEventFiredNTimes(int times) {
        Mockito.verify(publisher, TestUtils.times(times))
                .publishEvent(userCreatedEventArgumentCaptor.capture());
        Mockito.clearInvocations(publisher);
    }
}
