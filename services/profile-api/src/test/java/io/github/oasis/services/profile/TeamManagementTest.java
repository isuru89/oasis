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

import io.github.oasis.services.common.OasisValidationException;
import io.github.oasis.services.common.internal.events.profile.TeamCreatedEvent;
import io.github.oasis.services.profile.domain.Assignment;
import io.github.oasis.services.profile.domain.Team;
import io.github.oasis.services.profile.domain.User;
import io.github.oasis.services.profile.internal.dao.ITeamDao;
import io.github.oasis.services.profile.internal.dao.IUserDao;
import io.github.oasis.services.profile.internal.dto.EditTeamDto;
import io.github.oasis.services.profile.internal.dto.NewTeamDto;
import io.github.oasis.services.profile.internal.exceptions.TeamUpdateException;
import io.github.oasis.services.profile.json.NewTeamJson;
import io.github.oasis.services.profile.json.TeamJson;
import io.github.oasis.services.profile.json.TeamListJson;
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

import java.util.Arrays;

import static io.github.oasis.services.profile.utils.TestUtils.SINGLE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Isuru Weerarathna
 */
@DisplayName("Team Management")
class TeamManagementTest extends AbstractTest {

    @Autowired private ITeamDao teamDao;
    @Autowired private IUserDao userDao;

    @MockBean private ApplicationEventPublisher publisher;

    private ProfileAggregate profile;

    @Captor private ArgumentCaptor<TeamCreatedEvent> teamCreatedEventArgumentCaptor;

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
    @DisplayName("should be able to add team with a name")
    void testAddTeam() {
        NewTeamDto dto = createTeam("test-team1", "Never Look Down");
        NewTeamJson newTeamJson = profile.addTeam(dto);
        assertNotNull(newTeamJson);
        assertTrue(newTeamJson.getId() > 0);

        verifyTeamCreatedEventFired(newTeamJson.getId());
    }

    @Test
    @DisplayName("should not be able to add team without a name")
    void testAddTeamWithoutName() {
        NewTeamDto dto = createTeam(null, "Never Look Down");
        assertThrows(OasisValidationException.class, () -> profile.addTeam(dto));

        verifyNoTeamCreatedEventFired();
    }

    @Test
    @DisplayName("should not be able to add team with an already existing name")
    void testAddTeamWithSameName() {
        NewTeamDto dto = createTeam("Team1", "Never Look Down");
        NewTeamJson newTeamJson = profile.addTeam(dto);
        assertNotNull(newTeamJson);
        assertTrue(newTeamJson.getId() > 0);

        verifyTeamCreatedEventFired(newTeamJson.getId());

        TeamJson storedTeam = profile.readTeam(newTeamJson.getId());
        assertAddedTeam(dto, storedTeam);

        // create next team with same name
        assertThrows(TeamUpdateException.class, () -> profile.addTeam(dto));
        verifyNoTeamCreatedEventFired();

        // create team with same name but with diff case
        assertThrows(TeamUpdateException.class,
                () -> profile.addTeam(createTeam("team1", "Never Look Down 2")));
        assertThrows(TeamUpdateException.class,
                () -> profile.addTeam(createTeam(" TEam1 ", "Never Look Down 3")));
        assertThrows(TeamUpdateException.class,
                () -> profile.addTeam(createTeam("TEAM1", "Never Look Down 4")));
        verifyNoTeamCreatedEventFired();
    }

    @Test
    @DisplayName("should be able to edit team motto")
    void testEditTeamMotto() {
        NewTeamDto dto = createTeam("test-edit", "Never Back Down");
        NewTeamJson newTeamJson = profile.addTeam(dto);

        verifyTeamCreatedEventFired(newTeamJson.getId());
        assertAddedTeam(dto, profile.readTeam(newTeamJson.getId()));

        EditTeamDto editTeamDto = new EditTeamDto();
        editTeamDto.setMotto("Changed Never Back Down");
        profile.editTeam(newTeamJson.getId(), editTeamDto);

        verifyNoTeamCreatedEventFired();

        TeamJson teamJson = profile.readTeam(newTeamJson.getId());
        assertEquals(editTeamDto.getMotto(), teamJson.getMotto());
        assertEquals(dto.getName(), teamJson.getName());
        assertEquals(dto.getAvatar(), teamJson.getAvatar());
    }

    @Test
    @DisplayName("should be able to edit team name")
    void testEditTeamName() {
        NewTeamDto dto = createTeam("test-edit", "Never Back Down");
        NewTeamJson newTeamJson = profile.addTeam(dto);

        verifyTeamCreatedEventFired(newTeamJson.getId());
        assertAddedTeam(dto, profile.readTeam(newTeamJson.getId()));

        EditTeamDto editTeamDto = new EditTeamDto();
        editTeamDto.setName("Changed test-edit-name");
        profile.editTeam(newTeamJson.getId(), editTeamDto);

        verifyNoTeamCreatedEventFired();

        TeamJson teamJson = profile.readTeam(newTeamJson.getId());
        assertEquals(editTeamDto.getName(), teamJson.getName());
        assertEquals(dto.getMotto(), teamJson.getMotto());
        assertEquals(dto.getAvatar(), teamJson.getAvatar());
    }

    @Test
    @DisplayName("should not be able to edit team name to existing name")
    void testEditTeamNameToExistingName() {
        NewTeamDto dto = createTeam("test-edit", "Never Back Down");
        NewTeamJson newTeamJson = profile.addTeam(dto);

        verifyTeamCreatedEventFired(newTeamJson.getId());
        NewTeamJson team2Json = profile.addTeam(createTeam("test-edit2", "Never Back Down 2"));
        verifyTeamCreatedEventFired(team2Json.getId());

        EditTeamDto editTeamDto = new EditTeamDto();
        editTeamDto.setName("test-edit");
        assertThrows(TeamUpdateException.class, () -> profile.editTeam(team2Json.getId(), editTeamDto));
        verifyNoTeamCreatedEventFired();
    }

    @Test
    @DisplayName("should be able to read multiple team at once")
    void testReadMultipleTeams() {
        NewTeamJson team1 = profile.addTeam(createTeam("test-team1", "Motto 1"));
        NewTeamJson team2 = profile.addTeam(createTeam("test-team2", "Motto 2"));
        NewTeamJson team3 = profile.addTeam(createTeam("test-team3", "Motto 3"));
        NewTeamJson team4 = profile.addTeam(createTeam("test-team4", "Motto 4"));
        NewTeamJson team5 = profile.addTeam(createTeam("test-team5", "Motto 5"));

        verifyTeamCreatedEventFiredNTimes(5);

        TeamListJson teamListJson = profile.readTeams(Arrays.asList(team1.getId(), team4.getId()));
        assertEquals(0, teamListJson.getNotFoundTeams().size());
        assertEquals(2, teamListJson.getTeams().size());
        assertTrue(teamListJson.getTeams().containsKey(team1.getId()));
        assertTrue(teamListJson.getTeams().containsKey(team4.getId()));
        assertFalse(teamListJson.getTeams().containsKey(team3.getId()));
    }

    @Test
    @DisplayName("should be able to read multiple team at once with non existing items")
    void testReadMultipleTeamsWithNonExistingItems() {
        profile.addTeam(createTeam("test-team1", "Motto 1"));
        NewTeamJson team2 = profile.addTeam(createTeam("test-team2", "Motto 2"));
        NewTeamJson team3 = profile.addTeam(createTeam("test-team3", "Motto 3"));
        profile.addTeam(createTeam("test-team4", "Motto 4"));
        NewTeamJson team5 = profile.addTeam(createTeam("test-team5", "Motto 5"));

        verifyTeamCreatedEventFiredNTimes(5);

        int neId = team5.getId() + 100;
        TeamListJson teamListJson = profile.readTeams(Arrays.asList(team2.getId(),
                team3.getId(),
                neId));
        assertEquals(1, teamListJson.getNotFoundTeams().size());
        assertTrue(teamListJson.getNotFoundTeams().contains(neId));
        assertEquals(2, teamListJson.getTeams().size());
        assertTrue(teamListJson.getTeams().containsKey(team3.getId()));
        assertTrue(teamListJson.getTeams().containsKey(team2.getId()));
        assertFalse(teamListJson.getTeams().containsKey(team5.getId()));
    }

    private void assertAddedTeam(NewTeamDto dto, TeamJson json) {
        assertEquals(dto.getName(), json.getName());
        assertEquals(dto.getMotto(), json.getMotto());
        assertEquals(dto.getAvatar(), json.getAvatar());
        assertTrue(json.isActive());
    }

    private void verifyNoTeamCreatedEventFired() {
        Mockito.verifyZeroInteractions(publisher);
        Mockito.clearInvocations(publisher);
    }

    private void verifyTeamCreatedEventFiredNTimes(int times) {
        Mockito.verify(publisher, TestUtils.times(times))
                .publishEvent(teamCreatedEventArgumentCaptor.capture());
        Mockito.clearInvocations(publisher);
    }

    private void verifyTeamCreatedEventFired(int teamId) {
        Mockito.verify(publisher, SINGLE).publishEvent(teamCreatedEventArgumentCaptor.capture());
        TeamCreatedEvent event = teamCreatedEventArgumentCaptor.getValue();
        assertEquals(teamId, event.getTeamId());
        Mockito.clearInvocations(publisher);
    }


    private NewTeamDto createTeam(String name, String motto) {
        NewTeamDto dto = new NewTeamDto();
        dto.setName(name);
        dto.setMotto(motto);
        dto.setAvatar("/images/team/" + name + ".jpg");
        return dto;
    }
}
