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
import io.github.oasis.core.configs.OasisConfigs;
import io.github.oasis.core.context.GameContext;
import io.github.oasis.core.external.messages.GameCommand;
import io.github.oasis.engine.EngineContext;
import io.github.oasis.engine.actors.cmds.GameEventMessage;
import io.github.oasis.engine.actors.cmds.OasisRuleMessage;
import io.github.oasis.engine.actors.routers.GameRouting;
import io.github.oasis.engine.ext.ExternalParty;
import io.github.oasis.engine.ext.ExternalPartyImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
                .match(Event.class, this::forwardEvent)
                .match(OasisRuleMessage.class, this::ruleSpecificCommand)
                .match(GameCommand.class, this::gameSpecificCommand)
                .build();
    }

    private void forwardEvent(Event event) {
        GameContext gameContext = contextMap.computeIfAbsent(event.getGameId(), this::loadGameContext);
        if (gameContext != GameContext.NOT_FOUND || gameContext.fallsWithinGamePeriod(event.getTimestamp())) {
            gameProcessors.route(new GameEventMessage(event, gameContext), getSelf());
        }
    }

    private void ruleSpecificCommand(OasisRuleMessage ruleCommand) {
        int gameId = ruleCommand.getGameId();
        if (gamesRunning.contains(gameId)) {
            gameProcessors.route(ruleCommand, getSelf());
        } else {
            LOG.warn("No games by the id '{}' is running in the engine. Skipped rule update.", gameId);
        }
    }

    private void gameSpecificCommand(GameCommand gameCommand) {
        GameCommand.GameLifecycle status = gameCommand.getStatus();
        int gameId = gameCommand.getGameId();
        ExternalPartyImpl eventSource = ExternalParty.EXTERNAL_PARTY.get(getContext().getSystem());
        if (status == GameCommand.GameLifecycle.CREATE) {
            createGameRuleRefNx(gameId);
            gamesRunning.add(gameId);
            contextMap.put(gameId, loadGameContext(gameId));
            eventSource.ackGameStateChanged(gameCommand);
        } else if (status == GameCommand.GameLifecycle.REMOVE) {
            gamesRunning.remove(gameId);
            contextMap.remove(gameId);
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

    private GameContext loadGameContext(int gameId) {
        return new GameContext(gameId);
    }
}
