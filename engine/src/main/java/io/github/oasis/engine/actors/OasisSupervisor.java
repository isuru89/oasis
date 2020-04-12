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
import io.github.oasis.engine.OasisConfigs;
import io.github.oasis.engine.actors.cmds.AbstractGameCommand;
import io.github.oasis.engine.actors.cmds.GameEventMessage;
import io.github.oasis.engine.actors.cmds.OasisRuleMessage;
import io.github.oasis.engine.actors.routers.GameRouting;
import io.github.oasis.engine.model.GameContext;
import io.github.oasis.model.Event;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Manages all games running in the system.
 *
 * @author Isuru Weerarathna
 */
public class OasisSupervisor extends OasisBaseActor {

    private static final AtomicInteger COUNTER = new AtomicInteger(0);

    private static final int GAME_SUPERVISORS = 2;

    private final Map<Integer, GameContext> contextMap = new HashMap<>();

    private Router gameProcessors;

    @Inject
    public OasisSupervisor(OasisConfigs configs) {
        super(configs);

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
                .match(OasisRuleMessage.class, oasisRuleMessage -> gameProcessors.route(oasisRuleMessage, getSender()))
                .match(AbstractGameCommand.class, this::gameSpecificCommand)
                .build();
    }

    private void forwardEvent(Event event) {
        GameContext gameContext = contextMap.computeIfAbsent(event.getGameId(), this::loadGameContext);
        gameProcessors.route(new GameEventMessage(event, gameContext), getSender());
    }

    private void gameSpecificCommand(AbstractGameCommand gameCommand) {

    }

    private GameContext loadGameContext(int gameId) {
        return new GameContext(gameId);
    }
}
