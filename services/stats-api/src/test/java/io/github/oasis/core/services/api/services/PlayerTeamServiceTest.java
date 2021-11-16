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
import io.github.oasis.core.model.PlayerWithTeams;
import io.github.oasis.core.model.TeamObject;
import io.github.oasis.core.model.UserGender;
import io.github.oasis.core.services.api.exceptions.ErrorCodes;
import io.github.oasis.core.services.api.to.PlayerCreateRequest;
import io.github.oasis.core.services.api.to.PlayerGameAssociationRequest;
import io.github.oasis.core.services.api.to.PlayerUpdateRequest;
import io.github.oasis.core.services.api.to.TeamCreateRequest;
import io.github.oasis.core.services.api.to.TeamUpdateRequest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Isuru Weerarathna
 */
public class PlayerTeamServiceTest extends AbstractServiceTest {

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
        doGetError("/players?email=" + reqAlice.getEmail(), HttpStatus.NOT_FOUND, ErrorCodes.PLAYER_DOES_NOT_EXISTS);

        PlayerObject addedPlayer = callPlayerAdd(reqAlice);
        System.out.println(addedPlayer);
        assertFalse(addedPlayer.getId() <= 0);
        PlayerObject aliceInDb = doGetSuccess("/players?email=" + reqAlice.getEmail(), PlayerObject.class);
        System.out.println(aliceInDb);
        assertNotNull(aliceInDb);
        assertEquals(reqAlice.getEmail(), aliceInDb.getEmail());

        doPostError("/players", reqAlice, HttpStatus.BAD_REQUEST, ErrorCodes.PLAYER_EXISTS);
    }

    @Test
    void readPlayer() {
        PlayerObject bob = callPlayerAdd(reqBob);
        System.out.println(bob);
        assertPlayerWithAnother(bob, reqBob);

        PlayerObject bobById = doGetSuccess("/players/" + bob.getId(), PlayerObject.class);
        System.out.println(bobById);
        assertPlayerWithAnother(bobById, reqBob);

        doGetError("/players/" + (Long.MAX_VALUE-1), HttpStatus.NOT_FOUND, ErrorCodes.PLAYER_DOES_NOT_EXISTS);

        PlayerObject bobByEmail = doGetSuccess("/players?email=" + bob.getEmail(), PlayerObject.class);
        assertPlayerWithAnother(bobByEmail, bobById);
    }

    @Test
    void readPlayerWithTeams() {
        PlayerObject bob = callPlayerAdd(reqBob);
        System.out.println(bob);
        assertPlayerWithAnother(bob, reqBob);

        PlayerWithTeams playerObject = doGetSuccess("/players?verbose=true&email=" + bob.getEmail(), PlayerWithTeams.class);
        Assertions.assertThat(playerObject.getTeams()).isEmpty();

        TeamObject renegades = callTeamAdd(teamRenegades);
        TeamObject warriors = callTeamAdd(teamWarriors);

        callAddPlayerToTeam(bob.getId(), 100, renegades.getId());
        callAddPlayerToTeam(bob.getId(), 101, warriors.getId());

        PlayerWithTeams bobPlayer = doGetSuccess("/players?verbose=true&email=" + bob.getEmail(), PlayerWithTeams.class);
        List<TeamObject> teams = bobPlayer.getTeams();
        Assertions.assertThat(teams).hasSize(2);

        TeamObject renegadesInDb = teams.stream().filter(t -> t.getName().equals(renegades.getName())).findFirst().orElseThrow();
        assertTeamWithAnother(renegadesInDb, teamRenegades);
        TeamObject warriorsInDb = teams.stream().filter(t -> t.getName().equals(warriors.getName())).findFirst().orElseThrow();
        assertTeamWithAnother(warriorsInDb, teamWarriors);

        doGetError("/players?verbose=true&email=nonexist@oasis.io", HttpStatus.NOT_FOUND, ErrorCodes.PLAYER_DOES_NOT_EXISTS);
    }

    @Test
    void updatePlayer() {
        PlayerObject alice = callPlayerAdd(reqAlice);

        assertNotNull(doGetSuccess("/players?verbose=true&email=" + reqAlice.getEmail(), PlayerWithTeams.class));

        PlayerUpdateRequest toUpdateAlice = PlayerUpdateRequest.builder()
                .displayName("new alice name")
                .avatarRef("https://oasis.io/assets/alice_new.jpg")
                .build();

        PlayerObject aliceUpdated = callPlayerUpdate(alice.getId(), toUpdateAlice);
        assertEquals(toUpdateAlice.getDisplayName(), aliceUpdated.getDisplayName());
        assertEquals(toUpdateAlice.getAvatarRef(), aliceUpdated.getAvatarRef());
        assertEquals(alice.getGender(), aliceUpdated.getGender());
    }

    @Test
    void deactivatePlayer() {
        PlayerObject bob = callPlayerAdd(reqBob);
        System.out.println(bob);
        assertPlayerWithAnother(bob, reqBob);

        PlayerObject deletedPlayer = callPlayerDelete(bob.getId());
        assertPlayerWithAnother(bob, deletedPlayer);

        // delete non existing player
        // doDeletetError("/players/" + bob.getId(), HttpStatus.NOT_FOUND, ErrorCodes.PLAYER_DOES_NOT_EXISTS);
    }

    @Test
    void addTeam() {
        TeamObject warriors = callTeamAdd(teamWarriors);
        System.out.println(warriors);

        TeamObject renegades = callTeamAdd(teamRenegades);
        System.out.println(renegades);

        assertTeamWithAnother(doGetSuccess("/teams/" + warriors.getId(), TeamObject.class), teamWarriors);
        assertTeamWithAnother(doGetSuccess("/teams/" + renegades.getId(), TeamObject.class), teamRenegades);

        doPostError("/teams", teamWarriors, HttpStatus.BAD_REQUEST, ErrorCodes.TEAM_EXISTS);
    }

    @Test
    void readTeam() {
        TeamObject warriors = callTeamAdd(teamWarriors);
        System.out.println(warriors);
        assertTeamWithAnother(warriors, teamWarriors);

        {
            // by name
            TeamObject readWarriors = doGetSuccess("/teams?name=" + teamWarriors.getName(), TeamObject.class);
            assertTeamWithAnother(readWarriors, teamWarriors);
        }
        {
            // by id
            TeamObject readWarriors = doGetSuccess("/teams/" + warriors.getId(), TeamObject.class);
            assertTeamWithAnother(readWarriors, teamWarriors);
        }

        doGetError("/teams?name=nonexistingteamname", HttpStatus.NOT_FOUND, ErrorCodes.TEAM_NOT_EXISTS);
        doGetError("/teams/999999", HttpStatus.NOT_FOUND, ErrorCodes.TEAM_NOT_EXISTS);
    }

    @Test
    void updateTeam() {
        TeamObject renegades = callTeamAdd(teamRenegades);
        System.out.println(renegades);

        TeamUpdateRequest toBeUpdatedTeam = TeamUpdateRequest.builder()
                .colorCode("#00ff00")
                .avatarRef("https://oasis.io/assets/new_rr.jpeg")
                .build();
        TeamObject updatedTeam = callTeamUpdate(renegades.getId(), toBeUpdatedTeam);
        System.out.println(updatedTeam);
        assertTeamWithAnother(updatedTeam, toBeUpdatedTeam, renegades);
    }

    @Test
    void addPlayerToTeam() {
        PlayerObject alice = callPlayerAdd(reqAlice);

        TeamObject renegades = callTeamAdd(teamRenegades);
        TeamObject warriors = callTeamAdd(teamWarriors);

        callAddPlayerToTeam(alice.getId(), 100, renegades.getId());
        callAddPlayerToTeam(alice.getId(), 101, warriors.getId());

        List<TeamObject> teamObjects = doGetListSuccess("/players/" + alice.getId() + "/teams", TeamObject.class);
        Assertions.assertThat(teamObjects).hasSize(2);

        // can't add same user in multiple teams of same game
        doPostError("/players/" + alice.getId() + "/teams",
                PlayerGameAssociationRequest.builder().gameId(100).teamId(renegades.getId()).userId(alice.getId()).build(),
                HttpStatus.BAD_REQUEST,
                ErrorCodes.PLAYER_ALREADY_IN_TEAM);
    }

    @Test
    void addPlayersToTeam() {
        TeamObject warriors = callTeamAdd(teamWarriors);

        PlayerObject bob = callPlayerAdd(reqBob);
        PlayerObject alice = callPlayerAdd(reqAlice);
        PlayerObject candy = callPlayerAdd(reqAlice.toBuilder()
                                .displayName("Candy").email("candy@oasis.io")
                                .avatarRef("https://oasis.io/assets/cnd.png")
                                .build());

        callAddPlayersToTeam(warriors.getId(), List.of(bob.getId(), alice.getId()));

        List<PlayerObject> playerObjects = doGetListSuccess("/teams/" + warriors.getId() + "/players", PlayerObject.class);
        Assertions.assertThat(playerObjects).hasSize(2);

        // if one user failed, all should fail
        doPostError("/teams/" + warriors.getId() + "/players?playerIds=" + join(List.of(bob.getId(), candy.getId())), null,
                HttpStatus.BAD_REQUEST,
                ErrorCodes.PLAYER_ALREADY_IN_TEAM);
        assertEquals(0, doGetListSuccess("/players/" + candy.getId() + "/teams", TeamObject.class).size());

        // candy first in order
        doPostError("/teams/" + warriors.getId() + "/players?playerIds=" + join(List.of(candy.getId(), bob.getId())), null,
                HttpStatus.BAD_REQUEST,
                ErrorCodes.PLAYER_ALREADY_IN_TEAM);
        assertEquals(0, doGetListSuccess("/players/" + candy.getId() + "/teams", TeamObject.class).size());
    }

    @Test
    void getTeamsOfPlayer() {
        TeamObject warriors = callTeamAdd(teamWarriors);
        TeamObject renegades = callTeamAdd(teamRenegades);

        PlayerObject bob = callPlayerAdd(reqBob);
        PlayerObject alice = callPlayerAdd(reqAlice);

        callAddPlayersToTeam(warriors.getId(), List.of(bob.getId(), alice.getId()));
        callAddPlayerToTeam(alice.getId(), 100, renegades.getId());

        List<TeamObject> bobTeams = doGetListSuccess("/players/" + bob.getId() + "/teams", TeamObject.class);
        assertEquals(1, bobTeams.size());
        assertEquals(warriors.getName(), bobTeams.get(0).getName());

        List<TeamObject> aliceTeams = doGetListSuccess("/players/" + alice.getId() + "/teams", TeamObject.class);
        assertEquals(2, aliceTeams.size());
        assertTrue(aliceTeams.stream().anyMatch(t -> t.getName().equals(warriors.getName())));
        assertTrue(aliceTeams.stream().anyMatch(t -> t.getName().equals(renegades.getName())));
    }


    @Test
    void listAllUsersInTeam() {
        TeamObject warriors = callTeamAdd(teamWarriors);
        TeamObject renegades = callTeamAdd(teamRenegades);

        PlayerObject bob = callPlayerAdd(reqBob);
        PlayerObject alice = callPlayerAdd(reqAlice);

        callAddPlayersToTeam(warriors.getId(), List.of(bob.getId(), alice.getId()));
        callAddPlayerToTeam(alice.getId(), 100, renegades.getId());

        List<PlayerObject> playersInWarriors = doGetListSuccess("/teams/" + warriors.getId() + "/players", PlayerObject.class);
        assertEquals(2, playersInWarriors.size());
        assertTrue(playersInWarriors.stream().anyMatch(p -> p.getEmail().equals(bob.getEmail())));
        assertTrue(playersInWarriors.stream().anyMatch(p -> p.getEmail().equals(alice.getEmail())));
    }

    @Test
    void removePlayerFromTeam() {
        TeamObject warriors = callTeamAdd(teamWarriors);

        PlayerObject bob = callPlayerAdd(reqBob);
        PlayerObject alice = callPlayerAdd(reqAlice);

        callAddPlayersToTeam(warriors.getId(), List.of(bob.getId(), alice.getId()));

        assertEquals(2, doGetListSuccess("/teams/" + warriors.getId() + "/players", PlayerObject.class).size());
        assertEquals(1, doGetListSuccess("/players/" + bob.getId() + "/teams", TeamObject.class).size());

        doDeleteSuccess("/teams/" + warriors.getId() + "/players/" + bob.getId(), null);

        assertEquals(1, doGetListSuccess("/teams/" + warriors.getId() + "/players", PlayerObject.class).size());
        assertEquals(0, doGetListSuccess("/players/" + bob.getId() + "/teams", TeamObject.class).size());
    }

    @Test
    void searchTeam() {
        callTeamAdd(teamWarriors);
        callTeamAdd(teamWarriors.toBuilder().name("wuh war").build());
        callTeamAdd(teamWarriors.toBuilder().name("toronto tiq").build());
        callTeamAdd(teamWarriors.toBuilder().name("WUHAN WAR").build());
        callTeamAdd(teamWarriors.toBuilder().name("la liga").build());

        PaginatedResult<TeamMetadata> wuhResults = doGetPaginatedSuccess("/teams/search?name=wu&offset=0&pageSize=5", TeamMetadata.class);
        assertEquals(3, wuhResults.getRecords().size());
        assertEquals(2, doGetPaginatedSuccess("/teams/search?name=wuh&offset=1&pageSize=5", TeamMetadata.class).getRecords().size());
        assertEquals(0, doGetPaginatedSuccess("/teams/search?name=wuh&offset=4&pageSize=1", TeamMetadata.class).getRecords().size());
        assertEquals(1, doGetPaginatedSuccess("/teams/search?name=wuh&offset=0&pageSize=1", TeamMetadata.class).getRecords().size());
    }

    private PlayerObject callPlayerAdd(PlayerCreateRequest request) {
        return doPostSuccess("/players", request, PlayerObject.class);
    }

    private TeamObject callTeamAdd(TeamCreateRequest request) {
        return doPostSuccess("/teams", request, TeamObject.class);
    }

    private PlayerObject callPlayerUpdate(long playerId, PlayerUpdateRequest request) {
        return doPatchSuccess("/players/" + playerId, request, PlayerObject.class);
    }

    private PlayerObject callPlayerDelete(long playerId) {
        return doDeleteSuccess("/players/" + playerId, PlayerObject.class);
    }

    private void callAddPlayerToTeam(long playerId, int gameId, int teamId) {
        doPostSuccess("/players/" + playerId + "/teams", PlayerGameAssociationRequest.builder()
                .teamId(teamId).gameId(gameId).userId(playerId).build(), null);
    }

    private void callAddPlayersToTeam(int teamId, List<Long> playerIds) {
        doPostSuccess("/teams/" + teamId + "/players?playerIds=" + join(playerIds), null, null);
    }

    private TeamObject callTeamUpdate(int teamId, TeamUpdateRequest request) {
        return doPatchSuccess("/teams/" + teamId, request, TeamObject.class);
    }

    private String join(List<Long> ids) {
        return ids.stream().map(String::valueOf).collect(Collectors.joining(","));
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