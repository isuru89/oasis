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
import org.springframework.beans.factory.annotation.Autowired;

public abstract class BaseDefServiceTest extends AbstractServiceTest {

    @Autowired
    IGameDefService ds;

    void verifyDefsAreEmpty() throws Exception {
        resetSchema();

        Assertions.assertThat(ds.listGames()).isEmpty();
        Assertions.assertThat(ds.listBadgeDefs()).isEmpty();
        Assertions.assertThat(ds.listKpiCalculations()).isEmpty();
        Assertions.assertThat(ds.listMilestoneDefs()).isEmpty();
    }

    GameDef createGame(String name, String displayName) {
        GameDef gameDef = new GameDef();
        gameDef.setName(name);
        gameDef.setDisplayName(displayName);
        return gameDef;
    }

    GameDef createSavedGame(String name, String displayName) throws Exception {
        GameDef gameDef = new GameDef();
        gameDef.setName(name);
        gameDef.setDisplayName(displayName);

        long game = ds.createGame(gameDef, new GameOptionsDto());
        Assert.assertTrue(game > 0);

        GameDef added = ds.readGame(game);
        Assert.assertNotNull(added);
        Assert.assertEquals(game, added.getId().longValue());
        Assert.assertEquals(gameDef.getName(), added.getName());
        Assert.assertEquals(gameDef.getDisplayName(), added.getDisplayName());
        return added;
    }

}
