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

package io.github.oasis.services.services;

import io.github.oasis.model.defs.GameDef;
import io.github.oasis.services.dto.defs.GameOptionsDto;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class LifecycleServiceTest extends AbstractServiceTest {

    @Autowired
    private LifecycleImplManager lifecycleImplManager;

    @Autowired
    private IGameDefService gameDefService;

    private long mainGameId;

    @Before
    public void before() throws Exception {
        resetSchema();

        ILifecycleService iLifecycleService = lifecycleImplManager.get();
        Assertions.assertThat(iLifecycleService).isInstanceOf(LocalLifeCycleServiceImpl.class);

        GameDef def = new GameDef();
        def.setName("so");
        def.setDisplayName("Stackoverflow");
        mainGameId = gameDefService.createGame(def, new GameOptionsDto());
        Assert.assertTrue(mainGameId > 0);
    }

    @Test
    public void testStartGame() throws Exception {
        Future<?> start = lifecycleImplManager.get().start(mainGameId);

        try {
            start.get(10, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            ;
        }

        lifecycleImplManager.get().stop(mainGameId);
    }

}
