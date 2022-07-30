/*
 *
 *  * Licensed to the Apache Software Foundation (ASF) under one
 *  * or more contributor license agreements.  See the NOTICE file
 *  * distributed with this work for additional information
 *  * regarding copyright ownership.  The ASF licenses this file
 *  * to you under the Apache License, Version 2.0 (the
 *  * "License"); you may not use this file except in compliance
 *  * with the License.  You may obtain a copy of the License at
 *  *
 *  *    http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing,
 *  * software distributed under the License is distributed on an
 *  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  * KIND, either express or implied.  See the License for the
 *  * specific language governing permissions and limitations
 *  * under the License.
 *
 *
 */

package io.github.oasis.core.services.api.beans;

import io.github.oasis.core.elements.ElementDef;
import io.github.oasis.core.exception.OasisException;
import io.github.oasis.core.external.OasisRepository;
import io.github.oasis.core.model.PlayerObject;
import io.github.oasis.core.model.TeamObject;
import io.github.oasis.core.model.UserGender;
import io.github.oasis.core.services.annotations.AdminDbRepository;
import io.github.oasis.core.services.api.TestUtils;
import io.github.oasis.core.services.api.beans.jdbc.JdbcMetadataProvider;
import io.github.oasis.core.services.api.services.AbstractServiceTest;
import io.github.oasis.core.services.api.services.IElementService;
import io.github.oasis.core.services.api.services.IGameRankingService;
import io.github.oasis.core.services.api.services.IPlayerManagementService;
import io.github.oasis.core.services.api.services.ITeamManagementService;
import io.github.oasis.core.services.api.to.ElementCreateRequest;
import io.github.oasis.core.services.api.to.ElementUpdateRequest;
import io.github.oasis.core.services.api.to.RankCreationRequest;
import io.github.oasis.core.services.api.to.PlayerCreateRequest;
import io.github.oasis.core.services.api.to.PlayerUpdateRequest;
import io.github.oasis.core.services.api.to.TeamCreateRequest;
import io.github.oasis.core.services.api.to.TeamUpdateRequest;
import io.github.oasis.core.services.helpers.OasisMetadataSupport;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.AdditionalAnswers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;


/**
 * @author Isuru Weerarathna
 */
@SpringBootTest
public class MetadataReadTest extends AbstractServiceTest {

    @Autowired
    @Qualifier("jdbcMetadataProvider")
    private OasisMetadataSupport metadataReader;

    @Autowired
    @AdminDbRepository
    private OasisRepository adminRepository;

    @Autowired
    private IPlayerManagementService playerManagementService;
    @Autowired
    private ITeamManagementService teamManagementService;
    @Autowired
    private IGameRankingService rankingService;
    @Autowired
    private IElementService elementService;

    private OasisRepository spy;

    @BeforeEach
    public void beforeEachTest() {
        spy = Mockito.mock(OasisRepository.class, AdditionalAnswers.delegatesTo(adminRepository));
        if (metadataReader instanceof JdbcMetadataProvider) {
            ((JdbcMetadataProvider) metadataReader).setAdminDbRepository(spy);
        }
    }

    @Test
    public void testPlayersCache() throws OasisException {
        final PlayerCreateRequest reqAlice = PlayerCreateRequest.builder()
                .email("alice@oasis.io")
                .timeZone("America/New_York")
                .displayName("alice88")
                .avatarRef("https://oasis.io/assets/alice.png")
                .gender(UserGender.FEMALE)
                .build();

        PlayerObject alice = playerManagementService.addPlayer(reqAlice);

        Mockito.reset(spy);
        metadataReader.readUserMetadata(alice.getId());
        Mockito.verify(spy, Mockito.times(1)).readPlayer(Mockito.anyLong());

        // second time from cache
        Mockito.reset(spy);
        metadataReader.readUserMetadata(alice.getId());
        Mockito.verify(spy, Mockito.never()).readPlayer(Mockito.anyLong());

        // npw update user, so cache should clear
        PlayerUpdateRequest aliceUpdated = PlayerUpdateRequest.builder()
                .displayName("alice updated")
                .version(alice.getVersion())
                .build();
        playerManagementService.updatePlayer(alice.getId(), aliceUpdated);

        Mockito.reset(spy);
        metadataReader.readUserMetadata(alice.getId());
        Mockito.verify(spy, Mockito.times(1)).readPlayer(Mockito.anyLong());

        Mockito.reset(spy);
        metadataReader.readUserMetadata(alice.getId());
        Mockito.verify(spy, Mockito.never()).readPlayer(Mockito.anyLong());

        // now delete user
        playerManagementService.deactivatePlayer(alice.getId());
        Mockito.reset(spy);
        metadataReader.readUserMetadata(alice.getId());
        Mockito.verify(spy, Mockito.times(1)).readPlayer(Mockito.anyLong());
    }

    @Test
    public void testTeamCache() throws OasisException {
        final TeamCreateRequest teamWarriors = TeamCreateRequest.builder()
                .name("Wuhan Warriors")
                .avatarRef("https://oasis.io/assets/wuhanw.jpeg")
                .gameId(1)
                .colorCode("#000000")
                .build();

        TeamObject warriors = teamManagementService.addTeam(teamWarriors);

        Mockito.reset(spy);
        metadataReader.readTeamMetadata(warriors.getId());
        Mockito.verify(spy, Mockito.times(1)).readTeam(Mockito.anyInt());

        // second time from cache
        Mockito.reset(spy);
        metadataReader.readTeamMetadata(warriors.getId());
        Mockito.verify(spy, Mockito.never()).readTeam(Mockito.anyInt());

        // npw update team, so cache should clear
        TeamUpdateRequest warriorUpdated = TeamUpdateRequest.builder()
                .colorCode("#ff0000")
                .version(warriors.getVersion())
                .build();
        teamManagementService.updateTeam(warriors.getId(), warriorUpdated);

        Mockito.reset(spy);
        metadataReader.readTeamMetadata(warriors.getId());
        Mockito.verify(spy, Mockito.times(1)).readTeam(Mockito.anyInt());

        Mockito.reset(spy);
        metadataReader.readTeamMetadata(warriors.getId());
        Mockito.verify(spy, Mockito.never()).readTeam(Mockito.anyInt());
    }

    @Test
    public void testRankingCache() throws Exception {
        final RankCreationRequest gold = RankCreationRequest.builder()
                .name("gold").colorCode("#FFD700").priority(1).build();
        final RankCreationRequest silver = RankCreationRequest.builder()
                .name("silver").colorCode("#00ff00").priority(2).build();

        FunctionEx<Integer> invokeOnce = (size) -> {
            Mockito.reset(spy);
            Assertions.assertEquals(size, metadataReader.readAllRankInfo(1).size());
            Mockito.verify(spy, Mockito.times(1)).listAllRanks(Mockito.anyInt());
        };
        FunctionEx<Integer> neverInvokedNow = (size) -> {
            Mockito.reset(spy);
            Assertions.assertEquals(size, metadataReader.readAllRankInfo(1).size());
            Mockito.verify(spy, Mockito.never()).listAllRanks(Mockito.anyInt());
        };

        rankingService.addRank(1, gold);

        invokeOnce.with(1);
        neverInvokedNow.with(1);

        // add a new rank
        rankingService.addRank(1, silver);

        invokeOnce.with(2);
        neverInvokedNow.with(2);
    }

    @Test
    public void testElementsCache() throws Exception {
        List<ElementCreateRequest> elementCreateRequests = TestUtils.parseElementRules("rules.yml", 1);
        ElementCreateRequest samplePoint = TestUtils.findById("testpoint", elementCreateRequests);
        ElementCreateRequest sampleBadge = TestUtils.findById("testbadge", elementCreateRequests);

        ElementDef point1Ref = elementService.addElement(1, samplePoint);
        ElementDef badge1Ref = elementService.addElement(1, sampleBadge);

        String point1Id = point1Ref.getElementId();
        String badge1Id = badge1Ref.getElementId();

        // single metadata element

        Mockito.reset(spy);
        metadataReader.readElementDefinition(1, point1Id);
        metadataReader.readElementDefinition(1, badge1Id);
        Mockito.verify(spy, Mockito.times(2)).readElement(Mockito.anyInt(), Mockito.anyString());

        Mockito.reset(spy);
        metadataReader.readElementDefinition(1, point1Id);
        metadataReader.readElementDefinition(1, badge1Id);
        Mockito.verify(spy, Mockito.never()).readElement(Mockito.anyInt(), Mockito.anyString());

        // full definition

        Mockito.reset(spy);
        metadataReader.readFullElementDef(1, point1Id);
        metadataReader.readFullElementDef(1, badge1Id);
        Mockito.verify(spy, Mockito.times(2)).readElement(Mockito.anyInt(), Mockito.anyString());

        Mockito.reset(spy);
        metadataReader.readFullElementDef(1, point1Id);
        metadataReader.readFullElementDef(1, badge1Id);
        Mockito.verify(spy, Mockito.never()).readElement(Mockito.anyInt(), Mockito.anyString());


        ///////////
        // UPDATE
        //////////
        ElementUpdateRequest pointUpdateReq = new ElementUpdateRequest();
        pointUpdateReq.setDescription("A updated description");
        pointUpdateReq.setName(samplePoint.getMetadata().getName());
        pointUpdateReq.setVersion(point1Ref.getVersion());
        elementService.updateElement(1, samplePoint.getMetadata().getId(), pointUpdateReq);

        Mockito.reset(spy);
        metadataReader.readElementDefinition(1, point1Id);
        metadataReader.readElementDefinition(1, badge1Id);
        Mockito.verify(spy, Mockito.times(1)).readElement(Mockito.anyInt(), Mockito.anyString());

        Mockito.reset(spy);
        metadataReader.readFullElementDef(1, point1Id);
        metadataReader.readFullElementDef(1, badge1Id);
        Mockito.verify(spy, Mockito.times(1)).readElement(Mockito.anyInt(), Mockito.anyString());

        Mockito.reset(spy);
        metadataReader.readElementDefinitions(1, List.of(point1Id, badge1Id));
        Mockito.verify(spy, Mockito.never()).readElement(Mockito.anyInt(), Mockito.anyString());

        ////////
        // DELETE
        ////////
        elementService.deleteElement(1, badge1Id);

        Mockito.reset(spy);
        metadataReader.readElementDefinitions(1, List.of(point1Id, badge1Id));
        Mockito.verify(spy, Mockito.times(1)).readElement(Mockito.anyInt(), Mockito.anyString());

        Mockito.reset(spy);
        metadataReader.readElementDefinition(1, point1Id);
        metadataReader.readElementDefinition(1, badge1Id);
        Mockito.verify(spy, Mockito.times(1)).readElement(Mockito.anyInt(), Mockito.anyString());

        Mockito.reset(spy);
        metadataReader.readFullElementDef(1, point1Id);
        Mockito.verify(spy, Mockito.never()).readElement(Mockito.anyInt(), Mockito.anyString());

    }

    private interface FunctionEx<T> {
        void with(T expectedSize) throws Exception;
    }
}
