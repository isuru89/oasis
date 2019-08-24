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

package io.github.oasis.services.admin;


import io.github.oasis.services.admin.controller.AdminController;
import io.github.oasis.services.common.internal.events.game.GameStartedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.verification.VerificationMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * @author Isuru Weerarathna
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = OasisAdminConfiguration.class)
@DisplayName("External Applications")
public class ExternalApplicationTest {

    private static final VerificationMode SINGLE = Mockito.times(1);

    @MockBean
    private ApplicationEventPublisher publisher;

    private AdminAggregate adminAggregate;

    @Autowired
    private AdminController adminController;

    @BeforeEach
    public void beforeEach() {
        MockitoAnnotations.initMocks(this);
        adminAggregate = new AdminAggregate(publisher);
    }

    @DisplayName("Only Admin can add external applications")
    @Test
    public void testAddApplicationsByAdmin() {
        System.out.println(adminAggregate);
        adminAggregate.startGame(100);

        Mockito.verify(publisher, SINGLE).publishEvent(Mockito.any(GameStartedEvent.class));
    }

    @DisplayName("When no applications exist, should return empty")
    @Test
    public void testListApps() {

    }

    @DisplayName("Application secret key can only be downloaded once forever")
    @Test
    public void testDownloadKeys() {

    }

    @DisplayName("At least one event type must be mapped with an application")
    @Test
    public void testAddApplicationsWithEventTypes() {

    }

    @DisplayName("Application can optionally restricted to subset of games")
    @Test
    public void testAddApplicationsForSubSetOfGames() {

    }

    @DisplayName("Application name is mandatory")
    @Test
    public void testAddApplicationsWithoutName() {

    }

    @DisplayName("Application event types can be updated while game is running")
    @Test
    public void testUpdateEventTypes() {

    }

    @DisplayName("Application can restrict to a game while that game is running")
    @Test
    public void testUpdateAppRestrictToGames() {

    }

    @DisplayName("Non existing applications cannot be deleted")
    @Test
    public void testTryDeleteApp() {

    }

    @DisplayName("Existing application can be deleted only by admin")
    @Test
    public void testDeleteAppByAdmin() {

    }

}
