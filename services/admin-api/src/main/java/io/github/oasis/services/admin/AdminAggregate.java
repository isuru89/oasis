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

import io.github.oasis.services.admin.domain.ExternalAppService;
import io.github.oasis.services.admin.internal.ApplicationKey;
import io.github.oasis.services.admin.internal.exceptions.ExtAppAlreadyExistException;
import io.github.oasis.services.admin.internal.exceptions.ExtAppNotFoundException;
import io.github.oasis.services.admin.json.apps.AppUpdateResultJson;
import io.github.oasis.services.admin.json.apps.ApplicationAddedJson;
import io.github.oasis.services.admin.json.apps.ApplicationJson;
import io.github.oasis.services.admin.json.apps.NewApplicationJson;
import io.github.oasis.services.admin.json.apps.UpdateApplicationJson;
import io.github.oasis.services.common.internal.events.admin.ExternalAppEvent;
import io.github.oasis.services.common.internal.events.admin.ExternalAppEventType;
import io.github.oasis.services.common.internal.events.game.GamePausedEvent;
import io.github.oasis.services.common.internal.events.game.GameRestartedEvent;
import io.github.oasis.services.common.internal.events.game.GameStartedEvent;
import io.github.oasis.services.common.internal.events.game.GameStoppedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Isuru Weerarathna
 */
@Service
public class AdminAggregate {

    private ApplicationEventPublisher publisher;
    private ExternalAppService externalAppService;

    public AdminAggregate(ApplicationEventPublisher publisher, ExternalAppService externalAppService) {
        this.publisher = publisher;
        this.externalAppService = externalAppService;
    }

    /////////////////////////////////////////////////////////////////////////////
    //
    //  GAME - ACTIONS
    //
    /////////////////////////////////////////////////////////////////////////////

    public void startGame(int gameId) {
        System.out.println("Started");
        publisher.publishEvent(new GameStartedEvent(gameId));
    }

    public void stopGame(int gameId) {
        System.out.println("Stopped");
        publisher.publishEvent(new GameStoppedEvent(gameId));
    }

    public void pauseGame(int gameId) {
        System.out.println("Game Paused");
        publisher.publishEvent(new GamePausedEvent(gameId));
    }

    public void restartGame(int gameId) {
        System.out.println("Game restarted");
        publisher.publishEvent(new GameRestartedEvent(gameId));
    }

    /////////////////////////////////////////////////////////////////////////////
    //
    //  EXTERNAL APPLICATION - ACTIONS
    //
    /////////////////////////////////////////////////////////////////////////////

    public ApplicationKey readApplicationKey(int appId) throws ExtAppNotFoundException {
        return externalAppService.readApplicationSecretKey(appId);
    }

    public List<ApplicationJson> readAllApps() {
        return externalAppService.getAllRegisteredApplications();
    }

    public void deactivateApp(int appId) throws ExtAppNotFoundException {
        externalAppService.deactivateApplication(appId);
        publisher.publishEvent(new ExternalAppEvent(appId, ExternalAppEventType.DEACTIVATED));
    }

    public AppUpdateResultJson updateApp(int appId, UpdateApplicationJson freshData) throws ExtAppNotFoundException {
        if (freshData.hasSomethingToUpdate()) {
            AppUpdateResultJson updateResult = externalAppService.updateApplication(appId, freshData);
            publisher.publishEvent(new ExternalAppEvent(appId, ExternalAppEventType.MODIFIED));
            return updateResult;
        }
        return AppUpdateResultJson.EMPTY;
    }

    public ApplicationAddedJson registerNewApp(NewApplicationJson newApplicationData) throws ExtAppAlreadyExistException {
        newApplicationData.validate();

        ApplicationAddedJson addedApp = externalAppService.addApplication(newApplicationData);
        publisher.publishEvent(new ExternalAppEvent(addedApp.getId(), ExternalAppEventType.CREATED));
        return addedApp;
    }
}
