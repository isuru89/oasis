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
import io.github.oasis.core.external.messages.EngineStatusChangedMessage;
import io.github.oasis.core.services.api.to.EngineStatusChangedEvent;
import io.github.oasis.core.services.exceptions.OasisApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * @author Isuru Weerarathna
 */
@Component
public class EngineStatusListener {

    private static final Logger LOG = LoggerFactory.getLogger(EngineStatusListener.class);

    private final GameService gameService;

    public EngineStatusListener(GameService gameService) {
        this.gameService = gameService;
    }

    @EventListener
    public void handleEngineStatusChangedEvent(EngineStatusChangedEvent event) {
        EngineStatusChangedMessage message = event.getMessage();
        LOG.info("Engine status change event received! {}", message);
        try {
            Game game = gameService.readGame(message.getGameId());
            if (game == null) {
                LOG.error("No game definition is found by game id {}!", message.getGameId());
                return;
            }
            gameService.changeStatusOfGame(message.getGameId(), message.getState().name().toLowerCase(), message.getTs());
        } catch (OasisApiException e) {
            LOG.error("Unable to change game status in admin database!", e);
        }
    }

}
