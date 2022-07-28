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
 */

package io.github.oasis.core.services.api.handlers;

import io.github.oasis.core.services.api.services.IEngineManager;
import io.github.oasis.core.services.api.services.impl.GameService;
import io.github.oasis.core.services.events.GameStatusChangeEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Listen to all game state events produced by {@link GameService}.
 * Having this listener separately will remove a cycle dependency between
 * {@link GameService} vs {@link IEngineManager} services.
 *
 * @author Isuru Weerarathna
 */
@Component
public class GameStatusListener {

    private final IEngineManager engineManager;

    public GameStatusListener(IEngineManager engineManager) {
        this.engineManager = engineManager;
    }

    @EventListener
    public void handleGameStatusChangedEvent(GameStatusChangeEvent event) {
        engineManager.notifyGameStatusChange(event.getNewGameState(), event.getGameRef());
    }

}
