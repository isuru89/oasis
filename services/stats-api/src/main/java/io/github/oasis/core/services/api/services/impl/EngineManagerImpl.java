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

package io.github.oasis.core.services.api.services.impl;

import io.github.oasis.core.Game;
import io.github.oasis.core.elements.ElementDef;
import io.github.oasis.core.external.EngineManagerSubscription;
import io.github.oasis.core.external.EventDispatcher;
import io.github.oasis.core.external.messages.EngineMessage;
import io.github.oasis.core.external.messages.EngineStatusChangedMessage;
import io.github.oasis.core.external.messages.GameState;
import io.github.oasis.core.services.api.exceptions.EngineManagerException;
import io.github.oasis.core.services.api.exceptions.ErrorCodes;
import io.github.oasis.core.services.api.services.IElementService;
import io.github.oasis.core.services.api.services.IEngineManager;
import io.github.oasis.core.services.api.services.IGameService;
import io.github.oasis.core.services.exceptions.OasisApiException;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

/**
 * Default implementation for engine manager through an external plugin.
 *
 * @author Isuru Weerarathna
 */
@Service
public class EngineManagerImpl implements IEngineManager, Closeable {

    private static final Logger LOG = LoggerFactory.getLogger(EngineManagerImpl.class);

    private final IElementService elementService;
    private final IGameService gameService;

    private final EventDispatcher dispatchSupport;
    private final EngineManagerSubscription engineManagerSubscription;

    public EngineManagerImpl(IElementService elementService,
                             IGameService gameService,
                             EventDispatcher eventDispatcher,
                             EngineManagerSubscription engineManagerSubscription) {
        this.elementService = elementService;
        this.gameService = gameService;
        this.dispatchSupport = eventDispatcher;
        this.engineManagerSubscription = engineManagerSubscription;
    }

    @PostConstruct
    public void beforeInitialized() {
        this.initEngineStatusSubscription();
    }

    @Override
    public void notifyGameStatusChange(GameState state, Game game) throws EngineManagerException {
        LOG.info("Announcing game (id: {}) state change to engine {}", game.getId(), state);
        try {
            if (state == GameState.STARTED) {
                prepareGameForExecution(game);
            }

            EngineMessage message = EngineMessage.createGameLifecycleEvent(game.getId(), state);
            dispatchSupport.broadcast(message);
        } catch (Exception e) {
            throw new EngineManagerException(ErrorCodes.UNABLE_TO_CHANGE_GAME_STATE, e);
        }
    }

    private void prepareGameForExecution(Game game) throws Exception {
        Integer gameId = game.getId();

        // first we will send the game create message
        EngineMessage createdMessage = EngineMessage.createGameLifecycleEvent(gameId, GameState.CREATED);
        dispatchSupport.broadcast(createdMessage);

        // then game rules
        EngineMessage.Scope eventScope = new EngineMessage.Scope(gameId);
        List<ElementDef> elementDefs = elementService.listElementsFromGameId(gameId);
        LOG.info("Number of game rules to be sent = {}", elementDefs.size());
        for (ElementDef def : elementDefs) {
            EngineMessage ruleAdded = new EngineMessage();
            if (def.getData() == null) {
                ElementDef elementWithData = elementService.readElement(gameId, def.getElementId(), true);
                ruleAdded.setData(elementWithData.getData());
            } else {
                ruleAdded.setData(def.getData());
            }
            ruleAdded.setImpl(def.getType());
            ruleAdded.setType(EngineMessage.GAME_RULE_ADDED);
            ruleAdded.setScope(eventScope);

            LOG.info(" Game rule dispatched: game: {}, rule: {}", gameId, ruleAdded);
            dispatchSupport.broadcast(ruleAdded);
        }
    }

    private void initEngineStatusSubscription() {
        if (engineManagerSubscription != null) {
            LOG.info("Subscribing to engine status stream {}", engineManagerSubscription.getClass().getName());
            engineManagerSubscription.subscribe(this::consume);
        }
    }

    private void consume(EngineStatusChangedMessage message) {
        LOG.info("Engine status change event received! {}", message);
        try {
            Game game = gameService.readGame(message.getGameId());
            if (game == null) {
                LOG.error("No game definition is found by game id {}!", message.getGameId());
                return;
            }
            gameService.changeStatusOfGameWithoutPublishing(message.getGameId(), message.getState().name().toLowerCase(), message.getTs());
        } catch (OasisApiException e) {
            LOG.error("Unable to change game status in admin database!", e);
        }
    }

    @Override
    public void close() throws IOException {
        if (this.dispatchSupport != null) {
            LOG.info("Closing dispatcher...");
            this.dispatchSupport.close();
        }
    }
}
