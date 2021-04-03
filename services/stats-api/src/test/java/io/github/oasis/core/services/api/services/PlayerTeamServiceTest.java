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

package io.github.oasis.core.services.api.services;

import io.github.oasis.core.TeamMetadata;
import io.github.oasis.core.external.PaginatedResult;
import io.github.oasis.core.model.PlayerObject;
import io.github.oasis.core.model.TeamObject;
import io.github.oasis.core.model.UserGender;
import io.github.oasis.core.services.api.beans.BackendRepository;
import io.github.oasis.core.services.api.beans.jdbc.JdbcRepository;
import io.github.oasis.core.services.api.controllers.admin.PlayerController;
import io.github.oasis.core.services.api.dao.IPlayerTeamDao;
import io.github.oasis.core.services.api.exceptions.ErrorCodes;
import io.github.oasis.core.services.api.exceptions.OasisApiRuntimeException;
import io.github.oasis.core.services.api.to.PlayerCreateRequest;
import io.github.oasis.core.services.api.to.PlayerGameAssociationRequest;
import io.github.oasis.core.services.api.to.PlayerUpdateRequest;
import io.github.oasis.core.services.api.to.TeamCreateRequest;
import io.github.oasis.core.services.api.to.TeamUpdateRequest;
import io.github.oasis.core.services.exceptions.OasisApiException;
import org.assertj.core.api.Assertions;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Isuru Weerarathna
 */
public class PlayerTeamServiceTest extends AbstractServiceTest {

    private PlayerController controller;

    private final PlayerCreateRequest reqAlice = PlayerCreateRequest.builder()
            .email("alice@oasis.io")
            .timeZone("America/New_York")
            .displayName("alice88")
            .avatarRef("https://oasis.io/assets/alice.png")
            .gender(UserGender.FEMALE)
            .build();
    private final PlayerCreateRequest reqBob = PlayerCreateRequest.builder()
            .email("bob@oasis.io")
            .timeZone("Asia/Colombo")
            .displayName("bob_anderson")
            .avatarRef("https://oasis.io/assets/bob.jpeg")
            .gender(UserGender.MALE)
            .build();

    private final TeamCreateRequest teamWarriors = TeamCreateRequest.builder()
            .name("Wuhan Warriors")
            .avatarRef("https://oasis.io/assets/wuhanw.jpeg")
            .gameId(1)
            .colorCode("#000000")
            .build();
    private final TeamCreateRequest teamRenegades = TeamCreateRequest.builder()
            .name("Ruthless Renegade")
            .avatarRef("https://oasis.io/assets/rr.jpeg")
            .gameId(1)
            .colorCode("#FF0000")
            .build();


    @Test
    void addPlayer() {
        assertFalse(engineRepo.existsPlayer(reqAlice.getEmail()));
        assertFalse(adminRepo.existsPlayer(reqAlice.getEmail()));

        PlayerObject addedPlayer = controller.registerPlayer(reqAlice);
        System.out.println(addedPlayer);
        assertFalse(addedPlayer.getId() <= 0);
        assertTrue(engineRepo.existsPlayer(reqAlice.getEmail()));
        assertTrue(adminRepo.existsPlayer(reqAlice.getEmail()));

        assertThrows(OasisApiRuntimeException.class, () -> controller.registerPlayer(reqAlice));
    }

    @Test
    void readPlayer() {
        PlayerObject bob = controller.registerPlayer(reqBob);
        System.out.println(bob);
        assertPlayerWithAnother(bob, reqBob);

        PlayerObject bobById = controller.readPlayerProfile(bob.getId());
        System.out.println(bobById);
        assertPlayerWithAnother(bobById, reqBob);

        PlayerObject nonExistencePlayer = controller.readPlayerProfile(Long.MAX_VALUE);
        assertNull(nonExistencePlayer);

        PlayerObject bobByEmail = controller.readPlayerProfileByEmail(bob.getEmail());
        assertPlayerWithAnother(bobByEmail, bobById);
    }

    @Test
    void updatePlayer() {
        PlayerObject alice = controller.registerPlayer(reqAlice);

        assertTrue(adminRepo.existsPlayer(reqAlice.getEmail()));
        assertTrue(engineRepo.existsPlayer(reqAlice.getEmail()));

        PlayerUpdateRequest toUpdateAlice = PlayerUpdateRequest.builder()
                .displayName("new alice name")
                .avatarRef("https://oasis.io/assets/alice_new.jpg")
                .build();

        PlayerObject aliceUpdated = controller.updatePlayer(alice.getId(), toUpdateAlice);
        assertEquals(toUpdateAlice.getDisplayName(), aliceUpdated.getDisplayName());
        assertEquals(toUpdateAlice.getAvatarRef(), aliceUpdated.getAvatarRef());
        assertEquals(alice.getGender(), aliceUpdated.getGender());

        {
            PlayerObject engineAlice = engineRepo.readPlayer(reqAlice.getEmail());
            assertPlayerWithAnother(engineAlice, aliceUpdated);
        }
    }

    @Test
    void deactivatePlayer() {
        PlayerObject bob = controller.registerPlayer(reqBob);
        System.out.println(bob);
        assertPlayerWithAnother(bob, reqBob);

        assertTrue(adminRepo.existsPlayer(bob.getId()));
        assertTrue(engineRepo.existsPlayer(bob.getId()));

        PlayerObject deletedPlayer = controller.deactivatePlayer(bob.getId());
        assertPlayerWithAnother(bob, deletedPlayer);

        assertFalse(combinedRepo.existsPlayer(bob.getId()));
        assertFalse(engineRepo.existsPlayer(bob.getId()));
        assertFalse(engineRepo.existsPlayer(bob.getEmail()));
        assertTrue(adminRepo.existsPlayer(bob.getId()));
        assertTrue(adminRepo.existsPlayer(bob.getEmail()));
    }

    @Test
    void addTeam() {
        assertFalse(adminRepo.existsTeam(teamWarriors.getName()));
        assertFalse(engineRepo.existsTeam(teamWarriors.getName()));

        TeamObject warriors = controller.addTeam(teamWarriors);
        System.out.println(warriors);

        TeamObject renegades = controller.addTeam(teamRenegades);
        System.out.println(renegades);

        assertTrue(adminRepo.existsTeam(teamWarriors.getName()));
        assertTrue(engineRepo.existsTeam(teamWarriors.getName()));
        assertTrue(adminRepo.existsTeam(warriors.getId()));
        assertTrue(engineRepo.existsTeam(warriors.getId()));

        assertThrows(OasisApiRuntimeException.class, () -> controller.addTeam(teamWarriors));
    }

    @Test
    void readTeam() throws OasisApiException {
        TeamObject warriors = controller.addTeam(teamWarriors);
        System.out.println(warriors);
        assertTeamWithAnother(warriors, teamWarriors);

        {
            // by name
            TeamObject readWarriors = controller.readTeamInfoByName(teamWarriors.getName());
            assertTeamWithAnother(readWarriors, teamWarriors);
        }
        {
            // by id
            TeamObject readWarriors = controller.readTeamInfo(warriors.getId());
            assertTeamWithAnother(readWarriors, teamWarriors);
        }

        Assertions.assertThatThrownBy(() -> controller.readTeamInfoByName("Non existing team"))
                .isInstanceOf(OasisApiException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCodes.TEAM_NOT_EXISTS)
                .hasFieldOrPropertyWithValue("statusCode", HttpStatus.NOT_FOUND.value());
        Assertions.assertThatThrownBy(() -> controller.readTeamInfo(999999))
                .isInstanceOf(OasisApiException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCodes.TEAM_NOT_EXISTS)
                .hasFieldOrPropertyWithValue("statusCode", HttpStatus.NOT_FOUND.value());
    }

    @Test
    void updateTeam() {
        TeamObject renegades = controller.addTeam(teamRenegades);
        System.out.println(renegades);

        TeamUpdateRequest toBeUpdatedTeam = TeamUpdateRequest.builder()
                .colorCode("#00ff00")
                .avatarRef("https://oasis.io/assets/new_rr.jpeg")
                .build();
        TeamObject updatedTeam = controller.updateTeam(renegades.getId(), toBeUpdatedTeam);
        System.out.println(updatedTeam);
        assertTeamWithAnother(updatedTeam, toBeUpdatedTeam, renegades);
    }

    @Test
    void addPlayerToTeam() {
        PlayerObject alice = controller.registerPlayer(reqAlice);

        TeamObject renegades = controller.addTeam(teamRenegades);
        TeamObject warriors = controller.addTeam(teamWarriors);

        controller.addPlayerToTeam(alice.getId(), new PlayerGameAssociationRequest(alice.getId(), 100, renegades.getId()));
        controller.addPlayerToTeam(alice.getId(), new PlayerGameAssociationRequest(alice.getId(), 101, warriors.getId()));

        System.out.println(controller.browsePlayerTeams(alice.getId()));

        // can't add same user in multiple teams of same game
        Assertions.assertThatThrownBy(
                () -> controller.addPlayerToTeam(alice.getId(), new PlayerGameAssociationRequest(alice.getId(), 100, renegades.getId())))
            .isInstanceOf(OasisApiRuntimeException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCodes.PLAYER_ALREADY_IN_TEAM);
    }

    @Test
    void addPlayersToTeam() {
        TeamObject warriors = controller.addTeam(teamWarriors);

        PlayerObject bob = controller.registerPlayer(reqBob);
        PlayerObject alice = controller.registerPlayer(reqAlice);
        PlayerObject candy = controller.registerPlayer(reqAlice.toBuilder()
                                .displayName("Candy").email("candy@oasis.io")
                                .avatarRef("https://oasis.io/assets/cnd.png")
                                .build());

        controller.addPlayersToTeam(warriors.getId(), List.of(bob.getId(), alice.getId()));

        // if one user failed, other status will depend on order
        Assertions.assertThatThrownBy(() -> controller.addPlayersToTeam(warriors.getId(), List.of(bob.getId(), candy.getId())))
                .isInstanceOf(OasisApiRuntimeException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCodes.PLAYER_ALREADY_IN_TEAM);
        assertEquals(0, controller.browsePlayerTeams(candy.getId()).size());

        // candy first in order
        Assertions.assertThatThrownBy(() -> controller.addPlayersToTeam(warriors.getId(), List.of(candy.getId(), bob.getId())))
                .isInstanceOf(OasisApiRuntimeException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCodes.PLAYER_ALREADY_IN_TEAM);
        assertEquals(1, controller.browsePlayerTeams(candy.getId()).size());
    }


    @Test
    void getTeamsOfPlayer() {
        TeamObject warriors = controller.addTeam(teamWarriors);
        TeamObject renegades = controller.addTeam(teamRenegades);

        PlayerObject bob = controller.registerPlayer(reqBob);
        PlayerObject alice = controller.registerPlayer(reqAlice);

        controller.addPlayersToTeam(warriors.getId(), List.of(bob.getId(), alice.getId()));
        controller.addPlayerToTeam(alice.getId(), new PlayerGameAssociationRequest(alice.getId(), 100, renegades.getId()));

        List<TeamObject> bobTeams = controller.browsePlayerTeams(bob.getId());
        assertEquals(1, bobTeams.size());
        assertEquals(warriors.getName(), bobTeams.get(0).getName());

        List<TeamObject> aliceTeams = controller.browsePlayerTeams(alice.getId());
        assertEquals(2, aliceTeams.size());
        assertTrue(aliceTeams.stream().anyMatch(t -> t.getName().equals(warriors.getName())));
        assertTrue(aliceTeams.stream().anyMatch(t -> t.getName().equals(renegades.getName())));
    }


    @Test
    void listAllUsersInTeam() {
        TeamObject warriors = controller.addTeam(teamWarriors);
        TeamObject renegades = controller.addTeam(teamRenegades);

        PlayerObject bob = controller.registerPlayer(reqBob);
        PlayerObject alice = controller.registerPlayer(reqAlice);

        controller.addPlayersToTeam(warriors.getId(), List.of(bob.getId(), alice.getId()));
        controller.addPlayerToTeam(alice.getId(), new PlayerGameAssociationRequest(alice.getId(), 100, renegades.getId()));

        List<PlayerObject> playersInWarriors = controller.browsePlayers(warriors.getId());
        assertEquals(2, playersInWarriors.size());
        assertTrue(playersInWarriors.stream().anyMatch(p -> p.getEmail().equals(bob.getEmail())));
        assertTrue(playersInWarriors.stream().anyMatch(p -> p.getEmail().equals(alice.getEmail())));
    }

    @Test
    void removePlayerFromTeam() {
        TeamObject warriors = controller.addTeam(teamWarriors);

        PlayerObject bob = controller.registerPlayer(reqBob);
        PlayerObject alice = controller.registerPlayer(reqAlice);

        controller.addPlayersToTeam(warriors.getId(), List.of(bob.getId(), alice.getId()));

        assertEquals(2, controller.browsePlayers(warriors.getId()).size());
        assertEquals(1, controller.browsePlayerTeams(bob.getId()).size());

        controller.removePlayerFromTeam(bob.getId(), warriors.getId());

        assertEquals(1, controller.browsePlayers(warriors.getId()).size());
        assertEquals(0, controller.browsePlayerTeams(bob.getId()).size());
    }

    @Test
    void searchTeam() throws OasisApiException {
        controller.addTeam(teamWarriors);
        controller.addTeam(teamWarriors.toBuilder().name("wuh war").build());
        controller.addTeam(teamWarriors.toBuilder().name("toronto tiq").build());
        controller.addTeam(teamWarriors.toBuilder().name("WUHAN WAR").build());
        controller.addTeam(teamWarriors.toBuilder().name("la liga").build());

        PaginatedResult<TeamMetadata> wuhResults = controller.searchTeams("wuh", "0", 5);
        assertEquals(3, wuhResults.getRecords().size());
        assertEquals(2, controller.searchTeams("wuh", "1", 5).getRecords().size());
        assertEquals(0, controller.searchTeams("wuh", "4", 1).getRecords().size());
        assertEquals(1, controller.searchTeams("wuh", "0", 1).getRecords().size());
    }

    @Override
    protected JdbcRepository createJdbcRepository(Jdbi jdbi) {
        return new JdbcRepository(null,
                null,
                null,
                jdbi.onDemand(IPlayerTeamDao.class),
                serializationSupport);
    }

    @Override
    protected void createServices(BackendRepository backendRepository) {
        controller = new PlayerController(new PlayerTeamService(backendRepository));
    }

    private void assertPlayerWithAnother(PlayerObject player, PlayerCreateRequest request) {
        assertTrue(player.getId() > 0);
        assertEquals(request.getDisplayName(), player.getDisplayName());
        assertEquals(request.getEmail(), player.getEmail());
        assertEquals(request.getGender(), player.getGender());
        assertEquals(request.getTimeZone(), player.getTimeZone());
        assertEquals(request.getAvatarRef(), player.getAvatarRef());
        assertTrue(player.getCreatedAt() > 0);
        assertTrue(player.getUpdatedAt() > 0);
        assertTrue(player.isActive());
    }

    private void assertPlayerWithAnother(PlayerObject dbPlayer, PlayerObject other) {
        assertTrue(dbPlayer.getId() > 0);
        assertEquals(other.getDisplayName(), dbPlayer.getDisplayName());
        assertEquals(other.getEmail(), dbPlayer.getEmail());
        assertEquals(other.getGender(), dbPlayer.getGender());
        assertEquals(other.getTimeZone(), dbPlayer.getTimeZone());
        assertEquals(other.getAvatarRef(), dbPlayer.getAvatarRef());
        assertTrue(dbPlayer.getCreatedAt() > 0);
        assertTrue(dbPlayer.getUpdatedAt() > 0);
        assertTrue(dbPlayer.isActive());
    }

    private void assertTeamWithAnother(TeamObject dbTeam, TeamUpdateRequest other, TeamObject originalTeam) {
        assertTrue(dbTeam.getId() > 0);
        assertEquals(originalTeam.getName(), dbTeam.getName());
        assertEquals(other.getAvatarRef(), dbTeam.getAvatarRef());
        assertEquals(other.getColorCode(), dbTeam.getColorCode());
        assertEquals(originalTeam.getGameId(), dbTeam.getGameId());
        assertTrue(dbTeam.getCreatedAt() > 0);
        assertTrue(dbTeam.getUpdatedAt() > 0);
        assertTrue(dbTeam.isActive());
    }

    private void assertTeamWithAnother(TeamObject dbTeam, TeamCreateRequest other) {
        assertTrue(dbTeam.getId() > 0);
        assertEquals(other.getName(), dbTeam.getName());
        assertEquals(other.getAvatarRef(), dbTeam.getAvatarRef());
        assertEquals(other.getColorCode(), dbTeam.getColorCode());
        assertEquals(other.getGameId(), dbTeam.getGameId());
        assertTrue(dbTeam.getCreatedAt() > 0);
        assertTrue(dbTeam.getUpdatedAt() > 0);
        assertTrue(dbTeam.isActive());
    }
}