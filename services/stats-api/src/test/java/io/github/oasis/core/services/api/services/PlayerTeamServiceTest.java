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

    private PlayerController service;

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

    private final TeamObject teamWarriors = TeamObject.builder()
            .name("Wuhan Warriors")
            .avatarRef("https://oasis.io/assets/wuhanw.jpeg")
            .gameId(1)
            .colorCode("#000000")
            .build();
    private final TeamObject teamRenegades = TeamObject.builder()
            .name("Ruthless Renegade")
            .avatarRef("https://oasis.io/assets/rr.jpeg")
            .gameId(1)
            .colorCode("#FF0000")
            .build();


    @Test
    void addPlayer() {
        assertFalse(engineRepo.existsPlayer(reqAlice.getEmail()));
        assertFalse(adminRepo.existsPlayer(reqAlice.getEmail()));

        PlayerObject addedPlayer = service.registerPlayer(reqAlice);
        System.out.println(addedPlayer);
        assertFalse(addedPlayer.getId() <= 0);
        assertTrue(engineRepo.existsPlayer(reqAlice.getEmail()));
        assertTrue(adminRepo.existsPlayer(reqAlice.getEmail()));

        assertThrows(OasisApiRuntimeException.class, () -> service.registerPlayer(reqAlice));
    }

    @Test
    void readPlayer() {
        PlayerObject bob = service.registerPlayer(reqBob);
        System.out.println(bob);
        assertPlayerWithAnother(bob, reqBob);

        PlayerObject bobById = service.readPlayerProfile(bob.getId());
        System.out.println(bobById);
        assertPlayerWithAnother(bobById, reqBob);

        PlayerObject nonExistencePlayer = service.readPlayerProfile(Long.MAX_VALUE);
        assertNull(nonExistencePlayer);

        PlayerObject bobByEmail = service.readPlayerProfileByEmail(bob.getEmail());
        assertPlayerWithAnother(bobByEmail, bobById);
    }

    @Test
    void updatePlayer() {
        PlayerObject alice = service.registerPlayer(reqAlice);

        assertTrue(adminRepo.existsPlayer(reqAlice.getEmail()));
        assertTrue(engineRepo.existsPlayer(reqAlice.getEmail()));

        PlayerObject toUpdateAlice = alice.toBuilder()
                .displayName("new alice name")
                .avatarRef("https://oasis.io/assets/alice_new.jpg")
                .build();

        PlayerObject aliceUpdated = service.updatePlayer(alice.getId(), toUpdateAlice);
        assertPlayerWithAnother(aliceUpdated, toUpdateAlice);

        {
            PlayerObject engineAlice = engineRepo.readPlayer(reqAlice.getEmail());
            assertPlayerWithAnother(engineAlice, aliceUpdated);
        }
    }

    @Test
    void deactivatePlayer() {
        PlayerObject bob = service.registerPlayer(reqBob);
        System.out.println(bob);
        assertPlayerWithAnother(bob, reqBob);

        assertTrue(adminRepo.existsPlayer(bob.getId()));
        assertTrue(engineRepo.existsPlayer(bob.getId()));

        service.deactivatePlayer(bob.getId());

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

        TeamObject warriors = service.addTeam(teamWarriors);
        System.out.println(warriors);

        TeamObject renegades = service.addTeam(teamRenegades);
        System.out.println(renegades);

        assertTrue(adminRepo.existsTeam(teamWarriors.getName()));
        assertTrue(engineRepo.existsTeam(teamWarriors.getName()));
        assertTrue(adminRepo.existsTeam(warriors.getId()));
        assertTrue(engineRepo.existsTeam(warriors.getId()));

        assertThrows(OasisApiRuntimeException.class, () -> service.addTeam(teamWarriors));
    }

    @Test
    void readTeam() throws OasisApiException {
        TeamObject warriors = service.addTeam(teamWarriors);
        System.out.println(warriors);
        assertTeamWithAnother(warriors, teamWarriors);

        {
            // by name
            TeamObject readWarriors = service.readTeamInfoByName(teamWarriors.getName());
            assertTeamWithAnother(readWarriors, teamWarriors);
        }
        {
            // by id
            TeamObject readWarriors = service.readTeamInfo(warriors.getId());
            assertTeamWithAnother(readWarriors, teamWarriors);
        }

        Assertions.assertThatThrownBy(() -> service.readTeamInfoByName("Non existing team"))
                .isInstanceOf(OasisApiException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCodes.TEAM_NOT_EXISTS)
                .hasFieldOrPropertyWithValue("statusCode", HttpStatus.NOT_FOUND.value());
        Assertions.assertThatThrownBy(() -> service.readTeamInfo(999999))
                .isInstanceOf(OasisApiException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCodes.TEAM_NOT_EXISTS)
                .hasFieldOrPropertyWithValue("statusCode", HttpStatus.NOT_FOUND.value());
    }

    @Test
    void updateTeam() {
        TeamObject renegades = service.addTeam(teamRenegades);
        System.out.println(renegades);

        TeamObject toBeUpdatedTeam = renegades.toBuilder().colorCode("#00ff00")
                .avatarRef("https://oasis.io/assets/new_rr.jpeg")
                .build();
        TeamObject updatedTeam = service.updateTeam(renegades.getId(), toBeUpdatedTeam);
        System.out.println(updatedTeam);
        assertEquals(toBeUpdatedTeam.getId(), updatedTeam.getId());
        assertTeamWithAnother(updatedTeam, toBeUpdatedTeam);
    }

    @Test
    void addPlayerToTeam() {
        PlayerObject alice = service.registerPlayer(reqAlice);

        TeamObject renegades = service.addTeam(teamRenegades);
        TeamObject warriors = service.addTeam(teamWarriors);

        service.addPlayerToTeam(alice.getId(), new PlayerGameAssociationRequest(alice.getId(), 100, renegades.getId()));
        service.addPlayerToTeam(alice.getId(), new PlayerGameAssociationRequest(alice.getId(), 101, warriors.getId()));

        System.out.println(service.browsePlayerTeams(alice.getId()));

        // can't add same user in multiple teams of same game
        Assertions.assertThatThrownBy(
                () -> service.addPlayerToTeam(alice.getId(), new PlayerGameAssociationRequest(alice.getId(), 100, renegades.getId())))
            .isInstanceOf(OasisApiRuntimeException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCodes.PLAYER_ALREADY_IN_TEAM);
    }

    @Test
    void addPlayersToTeam() {
        TeamObject warriors = service.addTeam(teamWarriors);

        PlayerObject bob = service.registerPlayer(reqBob);
        PlayerObject alice = service.registerPlayer(reqAlice);
        PlayerObject candy = service.registerPlayer(reqAlice.toBuilder()
                                .displayName("Candy").email("candy@oasis.io")
                                .avatarRef("https://oasis.io/assets/cnd.png")
                                .build());

        service.addPlayersToTeam(warriors.getId(), List.of(bob.getId(), alice.getId()));

        // if one user failed, other status will depend on order
        Assertions.assertThatThrownBy(() -> service.addPlayersToTeam(warriors.getId(), List.of(bob.getId(), candy.getId())))
                .isInstanceOf(OasisApiRuntimeException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCodes.PLAYER_ALREADY_IN_TEAM);
        assertEquals(0, service.browsePlayerTeams(candy.getId()).size());

        // candy first in order
        Assertions.assertThatThrownBy(() -> service.addPlayersToTeam(warriors.getId(), List.of(candy.getId(), bob.getId())))
                .isInstanceOf(OasisApiRuntimeException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCodes.PLAYER_ALREADY_IN_TEAM);
        assertEquals(1, service.browsePlayerTeams(candy.getId()).size());
    }


    @Test
    void getTeamsOfPlayer() {
        TeamObject warriors = service.addTeam(teamWarriors);
        TeamObject renegades = service.addTeam(teamRenegades);

        PlayerObject bob = service.registerPlayer(reqBob);
        PlayerObject alice = service.registerPlayer(reqAlice);

        service.addPlayersToTeam(warriors.getId(), List.of(bob.getId(), alice.getId()));
        service.addPlayerToTeam(alice.getId(), new PlayerGameAssociationRequest(alice.getId(), 100, renegades.getId()));

        List<TeamObject> bobTeams = service.browsePlayerTeams(bob.getId());
        assertEquals(1, bobTeams.size());
        assertEquals(warriors.getName(), bobTeams.get(0).getName());

        List<TeamObject> aliceTeams = service.browsePlayerTeams(alice.getId());
        assertEquals(2, aliceTeams.size());
        assertTrue(aliceTeams.stream().anyMatch(t -> t.getName().equals(warriors.getName())));
        assertTrue(aliceTeams.stream().anyMatch(t -> t.getName().equals(renegades.getName())));
    }


    @Test
    void listAllUsersInTeam() {
        TeamObject warriors = service.addTeam(teamWarriors);
        TeamObject renegades = service.addTeam(teamRenegades);

        PlayerObject bob = service.registerPlayer(reqBob);
        PlayerObject alice = service.registerPlayer(reqAlice);

        service.addPlayersToTeam(warriors.getId(), List.of(bob.getId(), alice.getId()));
        service.addPlayerToTeam(alice.getId(), new PlayerGameAssociationRequest(alice.getId(), 100, renegades.getId()));

        List<PlayerObject> playersInWarriors = service.browsePlayers(warriors.getId());
        assertEquals(2, playersInWarriors.size());
        assertTrue(playersInWarriors.stream().anyMatch(p -> p.getEmail().equals(bob.getEmail())));
        assertTrue(playersInWarriors.stream().anyMatch(p -> p.getEmail().equals(alice.getEmail())));
    }

    @Test
    void removePlayerFromTeam() {
        TeamObject warriors = service.addTeam(teamWarriors);

        PlayerObject bob = service.registerPlayer(reqBob);
        PlayerObject alice = service.registerPlayer(reqAlice);

        service.addPlayersToTeam(warriors.getId(), List.of(bob.getId(), alice.getId()));

        assertEquals(2, service.browsePlayers(warriors.getId()).size());
        assertEquals(1, service.browsePlayerTeams(bob.getId()).size());

        service.removePlayerFromTeam(bob.getId(), new PlayerGameAssociationRequest(bob.getId(), warriors.getGameId(), warriors.getId()));

        assertEquals(1, service.browsePlayers(warriors.getId()).size());
        assertEquals(0, service.browsePlayerTeams(bob.getId()).size());
    }

    @Test
    void searchTeam() throws OasisApiException {
        service.addTeam(teamWarriors);
        service.addTeam(teamWarriors.toBuilder().name("wuh war").build());
        service.addTeam(teamWarriors.toBuilder().name("toronto tiq").build());
        service.addTeam(teamWarriors.toBuilder().name("WUHAN WAR").build());
        service.addTeam(teamWarriors.toBuilder().name("la liga").build());

        PaginatedResult<TeamMetadata> wuhResults = service.searchTeams("wuh", "0", 5);
        assertEquals(3, wuhResults.getRecords().size());
        assertEquals(2, service.searchTeams("wuh", "1", 5).getRecords().size());
        assertEquals(0, service.searchTeams("wuh", "4", 1).getRecords().size());
        assertEquals(1, service.searchTeams("wuh", "0", 1).getRecords().size());
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
        service = new PlayerController(new PlayerTeamService(backendRepository));
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

    private void assertTeamWithAnother(TeamObject dbTeam, TeamObject other) {
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