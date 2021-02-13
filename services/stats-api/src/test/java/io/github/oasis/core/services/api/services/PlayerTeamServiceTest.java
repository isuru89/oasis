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

import io.github.oasis.core.model.PlayerObject;
import io.github.oasis.core.model.UserGender;
import io.github.oasis.core.services.api.beans.BackendRepository;
import io.github.oasis.core.services.api.beans.jdbc.JdbcRepository;
import io.github.oasis.core.services.api.dao.IPlayerTeamDao;
import io.github.oasis.core.services.api.exceptions.OasisApiRuntimeException;
import io.github.oasis.core.services.api.to.PlayerCreateRequest;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Isuru Weerarathna
 */
class PlayerTeamServiceTest extends AbstractServiceTest {

    private PlayerTeamService service;

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


    @Test
    void addPlayer() {
        assertFalse(engineRepo.existsPlayer(reqAlice.getEmail()));
        assertFalse(adminRepo.existsPlayer(reqAlice.getEmail()));

        PlayerObject addedPlayer = service.addPlayer(reqAlice);
        System.out.println(addedPlayer);
        assertFalse(addedPlayer.getId() <= 0);
        assertTrue(engineRepo.existsPlayer(reqAlice.getEmail()));
        assertTrue(adminRepo.existsPlayer(reqAlice.getEmail()));

        assertThrows(OasisApiRuntimeException.class, () -> service.addPlayer(reqAlice));
    }

    @Test
    void readPlayer() {
        PlayerObject bob = service.addPlayer(reqBob);
        System.out.println(bob);
        assertPlayerWithRequest(bob, reqBob);

        PlayerObject bobById = service.readPlayer(bob.getId());
        System.out.println(bobById);
        assertPlayerWithRequest(bobById, reqBob);

        PlayerObject nonExistencePlayer = service.readPlayer(Integer.MAX_VALUE);
        assertNull(nonExistencePlayer);
    }

    @Test
    void updatePlayer() {
        PlayerObject alice = service.addPlayer(reqAlice);

        assertTrue(adminRepo.existsPlayer(reqAlice.getEmail()));
        assertTrue(engineRepo.existsPlayer(reqAlice.getEmail()));

        PlayerObject toUpdateAlice = alice.toBuilder()
                .displayName("new alice name")
                .avatarRef("https://oasis.io/assets/alice_new.jpg")
                .build();

        PlayerObject aliceUpdated = service.updatePlayer(alice.getId(), toUpdateAlice);
        assertPlayerWithRequest(aliceUpdated, toUpdateAlice);

        {
            PlayerObject engineAlice = engineRepo.readPlayer(reqAlice.getEmail());
            assertPlayerWithRequest(engineAlice, aliceUpdated);
        }
    }

    @Test
    void deactivatePlayer() {
        PlayerObject bob = service.addPlayer(reqBob);
        System.out.println(bob);
        assertPlayerWithRequest(bob, reqBob);

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
    void getTeamsOfPlayer() {
    }

    @Test
    void addTeam() {
    }

    @Test
    void updateTeam() {
    }

    @Test
    void listAllUsersInTeam() {
    }

    @Test
    void addPlayerToTeam() {
    }

    @Test
    void addPlayersToTeam() {
    }

    @Override
    JdbcRepository createJdbcRepository(Jdbi jdbi) {
        return new JdbcRepository(null, null, null, jdbi.onDemand(IPlayerTeamDao.class));
    }

    @Override
    void createServices(BackendRepository backendRepository) {
        service = new PlayerTeamService(backendRepository);
    }

    private void assertPlayerWithRequest(PlayerObject player, PlayerCreateRequest request) {
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

    private void assertPlayerWithRequest(PlayerObject dbPlayer, PlayerObject other) {
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
}