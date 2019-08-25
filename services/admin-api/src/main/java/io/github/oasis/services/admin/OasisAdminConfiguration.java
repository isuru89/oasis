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
import io.github.oasis.services.admin.controller.ExternalAppController;
import io.github.oasis.services.admin.controller.GameController;
import io.github.oasis.services.admin.domain.ExternalAppService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Isuru Weerarathna
 */
@Configuration
public class OasisAdminConfiguration {

    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    private AdminAggregate adminAggregate;

    @Autowired
    private ExternalAppService externalAppService;

    /////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////
    //
    // CONTROLLERS
    //
    /////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////

    @Bean
    public AdminController getAdminController() {
        return new AdminController();
    }

    @Bean
    public ExternalAppController getExternalAppController() {
        return new ExternalAppController(adminAggregate);
    }

    @Bean
    public GameController getGameController() {
        return new GameController();
    }

    /////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////
    //
    // AGGREGATES
    //
    /////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////

    @Bean
    public AdminAggregate getAdminAggregate() {
        return new AdminAggregate(publisher, externalAppService);
    }

}
