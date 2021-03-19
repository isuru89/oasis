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

package io.github.oasis.engine.actors;

import akka.routing.Routee;
import akka.routing.Router;
import io.github.oasis.core.Event;
import io.github.oasis.core.ID;
import io.github.oasis.core.configs.OasisConfigs;
import io.github.oasis.core.context.GameContext;
import io.github.oasis.core.external.DbContext;
import io.github.oasis.core.external.messages.GameCommand;
import io.github.oasis.core.external.messages.GameState;
import io.github.oasis.engine.EngineContext;
import io.github.oasis.engine.actors.cmds.EngineShutdownCommand;
import io.github.oasis.engine.actors.cmds.EventMessage;
import io.github.oasis.engine.actors.cmds.GameEventMessage;
import io.github.oasis.engine.actors.cmds.OasisRuleMessage;
import io.github.oasis.engine.actors.cmds.internal.GameStatusAsk;
import io.github.oasis.engine.actors.cmds.internal.GameStatusReply;
import io.github.oasis.engine.actors.cmds.internal.InternalAsk;
import io.github.oasis.engine.actors.routers.GameRouting;
import io.github.oasis.engine.ext.ExternalParty;
import io.github.oasis.engine.ext.ExternalPartyImpl;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Manages all games running in the system.
 *
 * @author Isuru Weerarathna
 */
public class OasisSupervisor extends OasisBaseActor {

    private static final Logger LOG = LoggerFactory.getLogger(OasisSupervisor.class);

    private static final AtomicInteger COUNTER = new AtomicInteger(0);

    private static final int GAME_SUPERVISORS = 2;

    private final Set<Integer> gamesRunning = new HashSet<>();
    private final Map<Integer, GameContext> contextMap = new HashMap<>();

    private Router gameProcessors;

    public OasisSupervisor(EngineContext context) {
        super(context);

        myId = "O" + COUNTER.incrementAndGet();
        createRuleRouters();
    }

    private void createRuleRouters() {
        int supervisorCount = configs.getInt(OasisConfigs.GAME_SUPERVISOR_COUNT, GAME_SUPERVISORS);
        List<Routee> routees = createChildRouteActorsOfType(GameSupervisor.class,
                index -> ActorNames.GAME_SUPERVISOR_PREFIX + index,
                supervisorCount);
        gameProcessors = new Router(new GameRouting(), routees);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(InternalAsk.class, this::handleInternalQuery)
                .match(EngineShutdownCommand.class, this::publish)
                .match(EventMessage.class, this::forwardEvent)
                .match(OasisRuleMessage.class, this::ruleSpecificCommand)
                .match(GameCommand.class, this::gameSpecificCommand)
                .build();
    }

    private void handleInternalQuery(InternalAsk ask) {
        if (ask instanceof GameStatusAsk) {
            GameStatusAsk statusAsk = (GameStatusAsk) ask;
            getSender().tell(new GameStatusReply(statusAsk.getGameId(), gamesRunning.contains(statusAsk.getGameId())), getSelf());
        }
    }

    private void publish(EngineShutdownCommand command) {
        if (!gamesRunning.isEmpty()) {
            try (DbContext db = engineContext.getDb().createContext()) {
                for (Integer gameId : gamesRunning) {
                    publishGameRemoval(gameId, db);
                }
            } catch (IOException e) {
                LOG.error("Error occurred while publishing running games!", e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void publishGameRemoval(int gameId, DbContext db) {
        JSONObject object = new JSONObject();
        object.put("gameId", gameId);
        object.put("state", GameState.STOPPED);
        object.put("engineId", engineContext.id());
        db.queueOffer(ID.ENGINE_STATUS_CHANNEL, object.toJSONString());
    }

    private void forwardEvent(EventMessage eventMessage) {
        Event event = eventMessage.getEvent();
        GameContext gameContext = contextMap.computeIfAbsent(event.getGameId(), this::loadGameContext);
        if (gameContext != GameContext.NOT_FOUND || gameContext.fallsWithinGamePeriod(event.getTimestamp())) {
            gameProcessors.route(new GameEventMessage(event, gameContext, eventMessage.getExternalMessageId()), getSelf());
        }
    }

    private void ruleSpecificCommand(OasisRuleMessage ruleCommand) {
        int gameId = ruleCommand.getGameId();
        if (gamesRunning.contains(gameId)) {
            gameProcessors.route(ruleCommand, getSelf());

            LOG.info("Acknowledging rule message receive success {}", ruleCommand.getExternalMessageId());
            ExternalParty.EXTERNAL_PARTY.get(getContext().getSystem())
                    .ackMessage(ruleCommand.getGameId(), ruleCommand.getExternalMessageId());
        } else {
            LOG.warn("No games by the id '{}' is running in the engine. Skipped rule update.", gameId);
        }
    }

    private void gameSpecificCommand(GameCommand gameCommand) {
        GameCommand.GameLifecycle status = gameCommand.getStatus();
        int gameId = gameCommand.getGameId();
        ExternalPartyImpl eventSource = ExternalParty.EXTERNAL_PARTY.get(getContext().getSystem());
        if (status == GameCommand.GameLifecycle.CREATE || status == GameCommand.GameLifecycle.START) {
            if (acquireGameLock(gameId, engineContext.id())) {
                createGameRuleRefNx(gameId);
                gamesRunning.add(gameId);
                contextMap.put(gameId, loadGameContext(gameId));
                eventSource.ackGameStateChanged(gameCommand);
                LOG.info("Successfully acquired the game {} to be run on this engine having id {}. Game Event: {}",
                        gameId, engineContext.id(), status);
            } else {
                LOG.warn("Cannot acquire game {} for this engine, because it is already owned by another engine!", gameId);
                eventSource.nackGameStateChanged(gameCommand);
            }
        } else if (status == GameCommand.GameLifecycle.REMOVE) {
            if (releaseGameLock(gameId, engineContext.id())) {
                gamesRunning.remove(gameId);
                contextMap.remove(gameId);
            } else {
                LOG.debug("The game {} is not running in this engine. Skipping remove message.", gameId);
            }
            eventSource.ackGameStateChanged(gameCommand);
        } else if (status == GameCommand.GameLifecycle.UPDATE) {
            if (gamesRunning.contains(gameId)) {
                contextMap.put(gameId, loadGameContext(gameId));
                eventSource.ackGameStateChanged(gameCommand);
            } else {
                LOG.warn("No games by the id '{}' is running in the engine. Skipped game update.", gameId);
                eventSource.nackGameStateChanged(gameCommand);
            }
        } else {
            eventSource.ackGameStateChanged(gameCommand);
        }
    }

    private boolean releaseGameLock(int gameId, String myId) {
        String gameIdStr = String.valueOf(gameId);
        try (DbContext context = engineContext.getDb().createContext()) {
            String owningEngine = context.getValueFromMap(ID.GAME_ENGINES, gameIdStr);
            if (myId.equals(owningEngine)) {
                LOG.info("Removing game lock... (gameId: {})", gameId);
                context.removeKeyFromMap(ID.GAME_ENGINES, gameIdStr);
                publishGameRemoval(gameId, context);
                LOG.info("Removed game lock! (gameId: {})", gameId);
                return true;
            }
        } catch (IOException e) {
            LOG.error("Cannot acquire game lock! Unexpected error!", e);
        }
        return false;
    }

    private boolean acquireGameLock(int gameId, String myId) {
        String gameIdStr = String.valueOf(gameId);
        try (DbContext context = engineContext.getDb().createContext()) {
            boolean locked = context.setIfNotExistsInMap(ID.GAME_ENGINES, gameIdStr, myId);
            if (!locked) {
                String currentEngineRunning = context.getValueFromMap(ID.GAME_ENGINES, gameIdStr);
                LOG.info("Game {} is currently run by the engine having id {}. My engine id = {}",
                        gameId, currentEngineRunning, myId);
                return myId.equals(currentEngineRunning);
            }
            return true;
        } catch (IOException e) {
            LOG.error("Cannot acquire game lock! Unexpected error!", e);
        }
        return false;
    }

    private GameContext loadGameContext(int gameId) {
        return new GameContext(gameId);
    }

}
