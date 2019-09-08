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
import io.github.oasis.services.common.internal.events.profile.UserCreatedEvent;
import io.github.oasis.services.common.internal.events.profile.UserDeactivatedEvent;
import io.github.oasis.services.profile.domain.Assignment;
import io.github.oasis.services.profile.domain.Gender;
import io.github.oasis.services.profile.domain.Team;
import io.github.oasis.services.profile.domain.User;
import io.github.oasis.services.profile.domain.UserStatus;
import io.github.oasis.services.profile.internal.dao.ITeamDao;
import io.github.oasis.services.profile.internal.dao.IUserDao;
import io.github.oasis.services.profile.internal.dto.EditUserDto;
import io.github.oasis.services.profile.internal.dto.NewUserDto;
import io.github.oasis.services.profile.internal.exceptions.UserNotFoundException;
import io.github.oasis.services.profile.json.NewUserJson;
import io.github.oasis.services.profile.json.UserJson;
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

import static io.github.oasis.services.profile.utils.TestUtils.SINGLE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Isuru Weerarathna
 */
@DisplayName("User Management")
class UserManagementTest extends AbstractTest {

    private static final String ISURU_GMAIL_COM = "isuru@gmail.com";
    @Autowired private ITeamDao teamDao;
    @Autowired private IUserDao userDao;

    @MockBean
    private ApplicationEventPublisher publisher;

    private ProfileAggregate profile;

    @Captor private ArgumentCaptor<UserCreatedEvent> userCreatedEventArgumentCaptor;
    @Captor private ArgumentCaptor<UserDeactivatedEvent> userDeactivatedEventArgumentCaptor;

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
    @DisplayName("should be able to add a user")
    void testAddUser() {
        NewUserDto isuru89 = createUser(ISURU_GMAIL_COM,
                "Isuru",
                "Wee",
                "isuru89", Gender.MALE);
        NewUserJson newUserJson = profile.addUser(isuru89);
        assertNotNull(newUserJson);
        assertTrue(newUserJson.getId() > 0);

        verifyUserCreatedEventFired(newUserJson.getId());
    }

    @Test
    @DisplayName("should be able to add a user only with a valid email")
    void testAddUserWithValidEmail() {
        assertThrows(OasisValidationException.class, () -> profile.addUser(
            createUser(null, "isuru")));
        assertThrows(OasisValidationException.class, () -> profile.addUser(
                createUser(" ", "isuru")));

        assertThrows(OasisValidationException.class, () -> profile.addUser(
                createUser("isurugmail.com", "isuru")));
        assertThrows(OasisValidationException.class, () -> profile.addUser(
                createUser("isuru@@gmail.com", "isuru")));
        assertThrows(OasisValidationException.class, () -> profile.addUser(
                createUser("isuru@gmail .com", "isuru")));
        assertThrows(OasisValidationException.class, () -> profile.addUser(
                createUser("isuru wee@gmail.com", "isuru")));

        verifyNoUserEventFired();
    }

    @Test
    @DisplayName("should not be able to add a user without nickname")
    void testAddUserWithoutNickname() {
        assertThrows(OasisValidationException.class, () -> profile.addUser(
                createUser(ISURU_GMAIL_COM, null)));
        assertThrows(OasisValidationException.class, () -> profile.addUser(
                createUser(ISURU_GMAIL_COM, "")));
        assertThrows(OasisValidationException.class, () -> profile.addUser(
                createUser(ISURU_GMAIL_COM, " ")));

        verifyNoUserEventFired();
    }

    @Test
    @DisplayName("should be able to edit a user")
    void testEditUser() {
        NewUserDto isuru89 = createUser(ISURU_GMAIL_COM,
                "Isuru",
                "Wee",
                "isuru89", Gender.MALE);
        NewUserJson newUserJson = profile.addUser(isuru89);
        verifyUserCreatedEventFired(newUserJson.getId());

        int userId = newUserJson.getId();
        {
            EditUserDto dto = new EditUserDto();
            dto.setFirstName("New Isuru");
            UserJson modified = profile.editUser(userId, dto);
            assertEquals(dto.getFirstName(), modified.getFirstName(),
                    "Firstname should be able to modify!");
        }
        {
            EditUserDto dto = new EditUserDto();
            dto.setLastName("Wee New");
            UserJson modified = profile.editUser(userId, dto);
            assertEquals(dto.getLastName(), modified.getLastName(),
                    "Lastname should be able to modify!");
        }
        {
            EditUserDto dto = new EditUserDto();
            dto.setAvatar("images/new/u/isuru89.jpg");
            UserJson modified = profile.editUser(userId, dto);
            assertEquals(dto.getAvatar(), modified.getAvatar(),
                    "Avatar should be able to modify!");
        }
        {
            EditUserDto dto = new EditUserDto();
            dto.setNickname("new-nickname");
            UserJson modified = profile.editUser(userId, dto);
            assertEquals(dto.getNickname(), modified.getNickname(),
                    "Nickname should be able to modify!");
        }
        {
            EditUserDto dto = new EditUserDto();
            dto.setGender(Gender.FEMALE.getId());
            UserJson modified = profile.editUser(userId, dto);
            assertEquals(dto.getGender(), modified.getGender().getId(),
                    "Gender should be able to modify!");
        }
        verifyNoUserEventFired();
    }

    @Test
    @DisplayName("should be able to delete a user")
    void testDeleteUser() {
        NewUserDto isuru89 = createUser(ISURU_GMAIL_COM,
                "Isuru",
                "Wee",
                "isuru89", Gender.MALE);
        NewUserJson newUserJson = profile.addUser(isuru89);
        int userId = newUserJson.getId();
        verifyUserCreatedEventFired(userId);

        profile.deactivateUser(userId);
        verifyUserDeactivatedEventFired(userId);

        UserJson userJson = profile.readUserById(userId);
        assertEquals(userId, userJson.getId());
        assertEquals(UserStatus.DEACTIVATED, userJson.getStatus());
        assertFalse(userJson.isActive());
    }

    @Test
    @DisplayName("should be able to read by email")
    void testUserReadByEmail() {
        NewUserDto isuru89 = createUser(ISURU_GMAIL_COM,
                "Isuru",
                "Wee",
                "isuru89", Gender.MALE);
        NewUserJson newUserJson = profile.addUser(isuru89);
        int userId = newUserJson.getId();
        verifyUserCreatedEventFired(userId);

        UserJson userJson = profile.readUserByEmail(ISURU_GMAIL_COM);
        assertEquals(userId, userJson.getId());

        verifyNoUserEventFired();
    }

    @Test
    @DisplayName("should throw an error if non existing user requested by email or id")
    void testUserReadByEmailNonExist() {
        assertThrows(UserNotFoundException.class, () ->
                profile.readUserByEmail("isuru2@gmail.com"));

        assertThrows(UserNotFoundException.class, () ->
                profile.readUserById(109));
    }

    @Test
    @DisplayName("should be able to update user status")
    void testUpdateUserStatus() {
        NewUserDto isuru89 = createUser(ISURU_GMAIL_COM,
                "Isuru",
                "Wee",
                "isuru89", Gender.MALE);
        NewUserJson newUserJson = profile.addUser(isuru89);
        int userId = newUserJson.getId();
        verifyUserCreatedEventFired(userId);

        {
            UserJson storedUser = profile.readUserById(userId);
            assertEquals(UserStatus.PENDING, storedUser.getStatus());
        }

        profile.updateUserStatus(userId, UserStatus.VERIFIED);
        UserJson storedUser = profile.readUserById(userId);
        assertEquals(UserStatus.VERIFIED, storedUser.getStatus());
        verifyNoUserEventFired();
    }

    void verifyUserDeactivatedEventFired(int userId) {
        Mockito.verify(publisher, SINGLE).publishEvent(userDeactivatedEventArgumentCaptor.capture());
        UserDeactivatedEvent event = userDeactivatedEventArgumentCaptor.getValue();
        assertEquals(userId, event.getUserId());
        Mockito.clearInvocations(publisher);
    }

    void verifyUserCreatedEventFired(int userId) {
        Mockito.verify(publisher, SINGLE).publishEvent(userCreatedEventArgumentCaptor.capture());
        UserCreatedEvent event = userCreatedEventArgumentCaptor.getValue();
        assertEquals(userId, event.getUserId());
        Mockito.clearInvocations(publisher);
    }

    void verifyNoUserEventFired() {
        Mockito.verifyZeroInteractions(publisher);
    }

    private NewUserDto createUser(String email,
                                  String nickname) {
        return createUser(email, null, null, nickname, Gender.MALE);
    }

    private NewUserDto createUser(String email,
                                  String firstName,
                                  String lastName,
                                  String nickname,
                                  Gender gender) {
        NewUserDto newUserDto = new NewUserDto();
        newUserDto.setEmail(email);
        newUserDto.setNickname(nickname);
        newUserDto.setFirstName(firstName);
        newUserDto.setLastName(lastName);
        newUserDto.setAutoUser(false);
        newUserDto.setAvatar("images/u/" + nickname + ".jpg");
        newUserDto.setGender(gender.getId());
        return newUserDto;
    }

}
