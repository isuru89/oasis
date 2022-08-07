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

import io.github.oasis.core.Game;
import io.github.oasis.core.TeamMetadata;
import io.github.oasis.core.external.OasisRepository;
import io.github.oasis.core.external.PaginatedResult;
import io.github.oasis.core.model.PlayerObject;
import io.github.oasis.core.model.PlayerWithTeams;
import io.github.oasis.core.model.TeamObject;
import io.github.oasis.core.model.UserGender;
import io.github.oasis.core.services.api.exceptions.ErrorCodes;
import io.github.oasis.core.services.api.handlers.CacheClearanceListener;
import io.github.oasis.core.services.api.handlers.events.BasePlayerRelatedEvent;
import io.github.oasis.core.services.api.handlers.events.EntityChangeType;
import io.github.oasis.core.services.api.services.impl.PlayerTeamService;
import io.github.oasis.core.services.api.to.*;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Isuru Weerarathna
 */
public class PlayerTeamServiceTest extends AbstractServiceTest {

    @SpyBean
    private CacheClearanceListener cacheClearanceListener;

    private final GameCreateRequest stackOverflow = GameCreateRequest.builder()
            .name("Stack-overflow")
            .description("Stackoverflow badges and points system")
            .logoRef("https://oasis.io/assets/so.jpeg")
            .motto("Help the community")
            .build();

    private final GameCreateRequest promotions = GameCreateRequest.builder()
            .name("Promotions")
            .description("Provides promotions for customers based on their loyality")
            .logoRef("https://oasis.io/assets/pm.jpeg")
            .motto("Serve your customers")
            .build();

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
    void mockCreation() {
        new PlayerTeamService(Mockito.mock(OasisRepository.class), Mockito.mock(ApplicationEventPublisher.class));
    }

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
    void addPlayerValidations() {
        // without name
        doPostError("/players", PlayerCreateRequest.builder()
                .email("alice@oasis.io")
                .timeZone("America/New_York")
                .avatarRef("https://oasis.io/assets/alice.png")
                .gender(UserGender.FEMALE)
                .build(), HttpStatus.BAD_REQUEST, ErrorCodes.INVALID_PARAMETER);

        // without email
        doPostError("/players", PlayerCreateRequest.builder()
                .timeZone("America/New_York")
                .displayName("alice88")
                .avatarRef("https://oasis.io/assets/alice.png")
                .gender(UserGender.FEMALE)
                .build(), HttpStatus.BAD_REQUEST, ErrorCodes.INVALID_PARAMETER);

        // without timezone
        doPostError("/players", PlayerCreateRequest.builder()
                .email("alice@oasis.io")
                .displayName("alice88")
                .avatarRef("https://oasis.io/assets/alice.png")
                .gender(UserGender.FEMALE)
                .build(), HttpStatus.BAD_REQUEST, ErrorCodes.INVALID_PARAMETER);

        // without gender
        doPostError("/players", PlayerCreateRequest.builder()
                .email("alice@oasis.io")
                .displayName("alice88")
                .timeZone("America/New_York")
                .avatarRef("https://oasis.io/assets/alice.png")
                .build(), HttpStatus.BAD_REQUEST, ErrorCodes.INVALID_PARAMETER);
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
    void readPlayerByEmail() {
        PlayerObject bob = callPlayerAdd(reqBob);
        System.out.println(bob);
        assertPlayerWithAnother(bob, reqBob);

        {
            PlayerWithTeams bobByEmail = doGetSuccess("/players?email=" + bob.getEmail(), PlayerWithTeams.class);
            System.out.println(bobByEmail);
            assertPlayerWithAnother(bobByEmail, reqBob);
            assertNull(bobByEmail.getTeams());
        }

        doGetError("/players?email=nonexist@oasis.io", HttpStatus.NOT_FOUND, ErrorCodes.PLAYER_DOES_NOT_EXISTS);
    }

    @Test
    void readPlayerWithTeams() {
        Integer gameId1 = callAddGame(stackOverflow).getId();
        Integer gameId2 = callAddGame(promotions).getId();
        PlayerObject bob = callPlayerAdd(reqBob);
        System.out.println(bob);
        assertPlayerWithAnother(bob, reqBob);

        PlayerWithTeams playerObject = doGetSuccess("/players?verbose=true&email=" + bob.getEmail(), PlayerWithTeams.class);
        Assertions.assertThat(playerObject.getTeams()).isEmpty();

        TeamObject renegades = callTeamAdd(teamRenegades);
        TeamObject warriors = callTeamAdd(teamWarriors);

        callAddPlayerToTeam(bob.getId(), gameId1, renegades.getId());
        callAddPlayerToTeam(bob.getId(), gameId2, warriors.getId());

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
                .version(alice.getVersion())
                .build();

        Mockito.reset(cacheClearanceListener);
        PlayerObject aliceUpdated = callPlayerUpdate(alice.getId(), toUpdateAlice);
        assertOnceCacheClearanceCalled(aliceUpdated.getEmail(), EntityChangeType.MODIFIED);

        assertEquals(toUpdateAlice.getDisplayName(), aliceUpdated.getDisplayName());
        assertEquals(toUpdateAlice.getAvatarRef(), aliceUpdated.getAvatarRef());
        assertEquals(alice.getGender(), aliceUpdated.getGender());
        assertEquals(alice.getVersion() + 1, aliceUpdated.getVersion());
    }

    @Test
    void updatePlayerWithoutVersion() {
        PlayerObject alice = callPlayerAdd(reqAlice);

        assertNotNull(doGetSuccess("/players?verbose=true&email=" + reqAlice.getEmail(), PlayerWithTeams.class));

        doPatchError("/players/" + alice.getId(), PlayerUpdateRequest.builder()
                .displayName("new alice name")
                .avatarRef("https://oasis.io/assets/alice_new.jpg")
                .build(),
                HttpStatus.BAD_REQUEST, ErrorCodes.INVALID_PARAMETER);

        doPatchError("/players/" + alice.getId(), PlayerUpdateRequest.builder()
                .displayName("new alice name")
                .avatarRef("https://oasis.io/assets/alice_new.jpg")
                .version(alice.getVersion() + 10)
                .build(),
                HttpStatus.CONFLICT, ErrorCodes.PLAYER_UPDATE_CONFLICT);

        PlayerObject dbAlice = doGetSuccess("/players/" + alice.getId(), PlayerObject.class);
        assertEquals(dbAlice.getDisplayName(), alice.getDisplayName());
        assertEquals(dbAlice.getAvatarRef(), alice.getAvatarRef());
    }

    @Test
    void updateNonExistPlayer() {
        PlayerObject alice = callPlayerAdd(reqAlice);

        doPatchError("/players/" + (alice.getId()+500), PlayerUpdateRequest.builder()
                        .displayName("new alice name")
                        .avatarRef("https://oasis.io/assets/alice_new.jpg")
                        .version(alice.getVersion())
                        .build(),
                HttpStatus.NOT_FOUND, ErrorCodes.PLAYER_DOES_NOT_EXISTS);

        PlayerObject dbAlice = doGetSuccess("/players/" + alice.getId(), PlayerObject.class);
        assertEquals(dbAlice.getDisplayName(), alice.getDisplayName());
        assertEquals(dbAlice.getAvatarRef(), alice.getAvatarRef());
    }

    @Test
    void deactivatePlayer() {
        PlayerObject bob = callPlayerAdd(reqBob);
        System.out.println(bob);
        assertPlayerWithAnother(bob, reqBob);

        PlayerObject deletedPlayer = callPlayerDelete(bob.getId());
        assertOnceCacheClearanceCalled(deletedPlayer.getEmail(), EntityChangeType.REMOVED);
        assertPlayerWithAnother(bob, deletedPlayer);

        // delete already deleted player
        Mockito.reset(cacheClearanceListener);
        doDeletetError("/players/" + bob.getId(), HttpStatus.BAD_REQUEST, ErrorCodes.PLAYER_IS_DEACTIVATED);
        assertNeverCacheClearanceCalled();

        // delete non-existing player
        Mockito.reset(cacheClearanceListener);
        doDeletetError("/players/9999999", HttpStatus.NOT_FOUND, ErrorCodes.PLAYER_DOES_NOT_EXISTS);
        assertNeverCacheClearanceCalled();
    }

    @Test
    void reactivatePlayer() {
        PlayerObject bob = callPlayerAdd(reqBob);
        System.out.println(bob);
        assertPlayerWithAnother(bob, reqBob);

        PlayerObject deletedPlayer = callPlayerDelete(bob.getId());
        assertOnceCacheClearanceCalled(deletedPlayer.getEmail(), EntityChangeType.REMOVED);
        assertPlayerWithAnother(bob, deletedPlayer);

        // delete already deleted player
        Mockito.reset(cacheClearanceListener);
        doDeletetError("/players/" + bob.getId(), HttpStatus.BAD_REQUEST, ErrorCodes.PLAYER_IS_DEACTIVATED);
        assertNeverCacheClearanceCalled();

        {
            PlayerObject playerObject = doGetSuccess("/players/" + bob.getId(), PlayerObject.class);
            org.junit.jupiter.api.Assertions.assertFalse(playerObject.isActive());
        }

        PlayerUpdateRequest updateRequest = PlayerUpdateRequest.builder()
                .isActive(true)
                .version(bob.getVersion())
                .build();
        callPlayerUpdate(bob.getId(), updateRequest);

        {
            PlayerObject playerObject = doGetSuccess("/players/" + bob.getId(), PlayerObject.class);
            org.junit.jupiter.api.Assertions.assertTrue(playerObject.isActive());
        }

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
    void addTeamValidations() {
        // name validations
        doPostError("/teams", teamWarriors.toBuilder().name(null).build(), HttpStatus.BAD_REQUEST, ErrorCodes.INVALID_PARAMETER);
        doPostError("/teams", teamWarriors.toBuilder().name("").build(), HttpStatus.BAD_REQUEST, ErrorCodes.INVALID_PARAMETER);
        doPostError("/teams", teamWarriors.toBuilder().name(RandomStringUtils.randomAscii(256)).build(), HttpStatus.BAD_REQUEST, ErrorCodes.INVALID_PARAMETER);

        // game id validations
        doPostError("/teams", teamWarriors.toBuilder().gameId(null).build(), HttpStatus.BAD_REQUEST, ErrorCodes.INVALID_PARAMETER);
        doPostError("/teams", teamWarriors.toBuilder().gameId(-1).build(), HttpStatus.BAD_REQUEST, ErrorCodes.INVALID_PARAMETER);
        doPostError("/teams", teamWarriors.toBuilder().gameId(0).build(), HttpStatus.BAD_REQUEST, ErrorCodes.INVALID_PARAMETER);
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
                .version(renegades.getVersion())
                .build();
        TeamObject updatedTeam = callTeamUpdate(renegades.getId(), toBeUpdatedTeam);
        System.out.println(updatedTeam);
        assertTeamWithAnother(updatedTeam, toBeUpdatedTeam, renegades);
        assertEquals(renegades.getVersion() + 1, updatedTeam.getVersion());
    }

    @Test
    void updateTeamWithoutVersion() {
        TeamObject renegades = callTeamAdd(teamRenegades);

        doPatchError("/teams/" + renegades.getId(), TeamUpdateRequest.builder()
                .colorCode("#00ff00")
                .avatarRef("https://oasis.io/assets/new_rr.jpeg")
                .build(),
                HttpStatus.BAD_REQUEST, ErrorCodes.INVALID_PARAMETER);

        doPatchError("/teams/" + renegades.getId(), TeamUpdateRequest.builder()
                .colorCode("#00ff00")
                .avatarRef("https://oasis.io/assets/new_rr.jpeg")
                .version(renegades.getVersion() + 100)
                .build(),
                HttpStatus.CONFLICT, ErrorCodes.TEAM_UPDATE_CONFLICT);

        TeamObject dbTeam = doGetSuccess("/teams/" + renegades.getId(), TeamObject.class);
        assertTeamWithAnother(dbTeam, teamRenegades);
    }

    @Test
    void updateNonExistTeam() {
        TeamObject renegades = callTeamAdd(teamRenegades);

        doPatchError("/teams/" + (renegades.getId() + 500), TeamUpdateRequest.builder()
                        .colorCode("#00ff00")
                        .avatarRef("https://oasis.io/assets/new_rr.jpeg")
                        .version(renegades.getVersion())
                        .build(),
                HttpStatus.NOT_FOUND, ErrorCodes.TEAM_NOT_EXISTS);

        TeamObject dbTeam = doGetSuccess("/teams/" + renegades.getId(), TeamObject.class);
        assertTeamWithAnother(dbTeam, teamRenegades);
    }

    @Test
    void addPlayerToTeam() {
        Integer gameId1 = callAddGame(stackOverflow).getId();
        Integer gameId2 = callAddGame(promotions).getId();
        PlayerObject alice = callPlayerAdd(reqAlice);

        TeamObject renegades = callTeamAdd(teamRenegades);
        TeamObject warriors = callTeamAdd(teamWarriors);

        Mockito.reset(cacheClearanceListener);
        callAddPlayerToTeam(alice.getId(), gameId1, renegades.getId());
        assertOnceCacheClearanceCalled(alice.getEmail(), EntityChangeType.MODIFIED);

        callAddPlayerToTeam(alice.getId(), gameId2, warriors.getId());

        List<TeamObject> teamObjects = doGetListSuccess("/players/" + alice.getId() + "/teams", TeamObject.class);
        Assertions.assertThat(teamObjects).hasSize(2);

        // can't add same user in multiple teams of same game
        Mockito.reset(cacheClearanceListener);
        doPostError("/players/" + alice.getId() + "/teams/" + renegades.getId(),
                null,
                HttpStatus.BAD_REQUEST,
                ErrorCodes.PLAYER_ALREADY_IN_TEAM);
        assertNeverCacheClearanceCalled();
    }

    @Test
    void addPlayerToTeamFailures() {
        Integer gameId = callAddGame(stackOverflow).getId();
        PlayerObject alice = callPlayerAdd(reqAlice);
        TeamObject renegades = callTeamAdd(teamRenegades);

        // validations in request
        {
            doPostError("/players/" + alice.getId() + "/teams/-1",
                    null,
                    HttpStatus.NOT_FOUND, ErrorCodes.TEAM_NOT_EXISTS);
            doPostError("/players/" + alice.getId() + "/teams/0",
                    null,
                    HttpStatus.NOT_FOUND, ErrorCodes.TEAM_NOT_EXISTS);
        }

        callAddPlayerToTeam(alice.getId(), gameId, renegades.getId());

        // can't add non-existing user
        Mockito.reset(cacheClearanceListener);
        doPostError("/players/999999/teams/" + renegades.getId(),
                null,
                HttpStatus.NOT_FOUND,
                ErrorCodes.PLAYER_DOES_NOT_EXISTS);
        assertNeverCacheClearanceCalled();

        // can't add non-existing team
        Mockito.reset(cacheClearanceListener);
        doPostError("/players/" + alice.getId() + "/teams/9999999",
                null,
                HttpStatus.NOT_FOUND,
                ErrorCodes.TEAM_NOT_EXISTS);
        assertNeverCacheClearanceCalled();
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

        Mockito.reset(cacheClearanceListener);
        callAddPlayersToTeam(warriors.getId(), List.of(bob.getId(), alice.getId()));
        assertNTimesCacheClearanceCalled(2, EntityChangeType.MODIFIED, Set.of(bob.getEmail(), alice.getEmail()));

        List<PlayerObject> playerObjects = doGetListSuccess("/teams/" + warriors.getId() + "/players", PlayerObject.class);
        Assertions.assertThat(playerObjects).hasSize(2);

        // if one user failed, all should fail
        Mockito.reset(cacheClearanceListener);
        doPostError("/teams/" + warriors.getId() + "/players?playerIds=" + join(List.of(bob.getId(), candy.getId())), null,
                HttpStatus.BAD_REQUEST,
                ErrorCodes.PLAYER_ALREADY_IN_TEAM);
        assertEquals(0, doGetListSuccess("/players/" + candy.getId() + "/teams", TeamObject.class).size());
        assertNeverCacheClearanceCalled();

        // candy first in order
        doPostError("/teams/" + warriors.getId() + "/players?playerIds=" + join(List.of(candy.getId(), bob.getId())), null,
                HttpStatus.BAD_REQUEST,
                ErrorCodes.PLAYER_ALREADY_IN_TEAM);
        assertEquals(0, doGetListSuccess("/players/" + candy.getId() + "/teams", TeamObject.class).size());
    }

    @Test
    void getTeamsOfPlayer() {
        Integer gameId = callAddGame(stackOverflow).getId();
        TeamObject warriors = callTeamAdd(teamWarriors);
        TeamObject renegades = callTeamAdd(teamRenegades);

        PlayerObject bob = callPlayerAdd(reqBob);
        PlayerObject alice = callPlayerAdd(reqAlice);

        callAddPlayersToTeam(warriors.getId(), List.of(bob.getId(), alice.getId()));
        callAddPlayerToTeam(alice.getId(), gameId, renegades.getId());

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
        Integer gameId = callAddGame(stackOverflow).getId();
        TeamObject warriors = callTeamAdd(teamWarriors);
        TeamObject renegades = callTeamAdd(teamRenegades);

        PlayerObject bob = callPlayerAdd(reqBob);
        PlayerObject alice = callPlayerAdd(reqAlice);

        callAddPlayersToTeam(warriors.getId(), List.of(bob.getId(), alice.getId()));
        callAddPlayerToTeam(alice.getId(), gameId, renegades.getId());

        List<PlayerObject> playersInWarriors = doGetListSuccess("/teams/" + warriors.getId() + "/players", PlayerObject.class);
        assertEquals(2, playersInWarriors.size());
        assertTrue(playersInWarriors.stream().anyMatch(p -> p.getEmail().equals(bob.getEmail())));
        assertTrue(playersInWarriors.stream().anyMatch(p -> p.getEmail().equals(alice.getEmail())));
    }

    @Test
    void removePlayerFromTeam() {
        Integer gameId = callAddGame(stackOverflow).getId();
        TeamObject warriors = callTeamAdd(teamWarriors);

        PlayerObject bob = callPlayerAdd(reqBob);
        PlayerObject alice = callPlayerAdd(reqAlice);

        callAddPlayersToTeam(warriors.getId(), List.of(bob.getId(), alice.getId()));

        assertEquals(2, doGetListSuccess("/teams/" + warriors.getId() + "/players", PlayerObject.class).size());
        assertEquals(1, doGetListSuccess("/players/" + bob.getId() + "/teams", TeamObject.class).size());

        Mockito.reset(cacheClearanceListener);
        doDeleteSuccess("/teams/" + warriors.getId() + "/players/" + bob.getId(), null);
        assertOnceCacheClearanceCalled(bob.getEmail(), EntityChangeType.MODIFIED);

        assertEquals(1, doGetListSuccess("/teams/" + warriors.getId() + "/players", PlayerObject.class).size());
        assertEquals(0, doGetListSuccess("/players/" + bob.getId() + "/teams", TeamObject.class).size());

        // ability to add again
        Mockito.reset(cacheClearanceListener);
        callAddPlayerToTeam(bob.getId(), gameId, warriors.getId());
        assertOnceCacheClearanceCalled(bob.getEmail(), EntityChangeType.MODIFIED);

        assertEquals(2, doGetListSuccess("/teams/" + warriors.getId() + "/players", PlayerObject.class).size());
        assertEquals(1, doGetListSuccess("/players/" + bob.getId() + "/teams", TeamObject.class).size());
    }

    @Test
    void removePlayerFromTeamFailures() {
        Integer gameId = callAddGame(stackOverflow).getId();
        PlayerObject alice = callPlayerAdd(reqAlice);
        TeamObject renegades = callTeamAdd(teamRenegades);

        callAddPlayerToTeam(alice.getId(), gameId, renegades.getId());

        // can't remove non-existing user
        Mockito.reset(cacheClearanceListener);
        doDeletetError("/teams/" + renegades.getId() + "/players/999999",
                HttpStatus.NOT_FOUND,
                ErrorCodes.PLAYER_DOES_NOT_EXISTS);
        assertNeverCacheClearanceCalled();

        // can't remove from non-existing team
        Mockito.reset(cacheClearanceListener);
        doDeletetError("/teams/9999999/players/" + alice.getId(),
                HttpStatus.NOT_FOUND,
                ErrorCodes.TEAM_NOT_EXISTS);
        assertNeverCacheClearanceCalled();
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
        doPostSuccess("/players/" + playerId + "/teams/" + teamId, null, null);
    }

    private void callAddPlayersToTeam(int teamId, List<Long> playerIds) {
        doPostSuccess("/teams/" + teamId + "/players?playerIds=" + join(playerIds), null, null);
    }

    private TeamObject callTeamUpdate(int teamId, TeamUpdateRequest request) {
        return doPatchSuccess("/teams/" + teamId, request, TeamObject.class);
    }

    private Game callAddGame(GameCreateRequest request) {
        return doPostSuccess("/games", request, Game.class);
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

    private void assertNTimesCacheClearanceCalled(int n, EntityChangeType changeType, Set<String> emails) {
        ArgumentCaptor<BasePlayerRelatedEvent> captor = ArgumentCaptor.forClass(BasePlayerRelatedEvent.class);

        Mockito.verify(cacheClearanceListener, Mockito.times(n))
                .handlePlayerUpdateEvent(captor.capture());

        List<BasePlayerRelatedEvent> allValues = captor.getAllValues();
        allValues.forEach(v -> {
            org.junit.jupiter.api.Assertions.assertEquals(changeType, v.getChangeType());
        });
        Set<String> collectedEmails = allValues.stream().map(BasePlayerRelatedEvent::getEmail).collect(Collectors.toSet());
        org.junit.jupiter.api.Assertions.assertTrue(SetUtils.isEqualSet(emails, collectedEmails));
        Mockito.reset(cacheClearanceListener);
    }

    private void assertOnceCacheClearanceCalled(String email, EntityChangeType changeType) {
        ArgumentCaptor<BasePlayerRelatedEvent> captor = ArgumentCaptor.forClass(BasePlayerRelatedEvent.class);

        Mockito.verify(cacheClearanceListener, Mockito.times(1))
                .handlePlayerUpdateEvent(captor.capture());

        org.junit.jupiter.api.Assertions.assertEquals(email, captor.getValue().getEmail());
        assertEquals(changeType, captor.getValue().getChangeType());
        Mockito.reset(cacheClearanceListener);
    }

    private void assertNeverCacheClearanceCalled() {
        Mockito.verify(cacheClearanceListener, Mockito.never())
                .handlePlayerUpdateEvent(Mockito.any(BasePlayerRelatedEvent.class));
        Mockito.reset(cacheClearanceListener);
    }
}