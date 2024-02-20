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

package io.github.oasis.engine;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.oasis.core.configs.OasisConfigs;
import io.github.oasis.core.exception.OasisException;
import io.github.oasis.core.external.Db;
import io.github.oasis.core.external.DbContext;
import io.github.oasis.core.model.PlayerObject;
import io.github.oasis.core.model.TeamObject;
import io.github.oasis.core.services.api.beans.JsonSerializer;
import io.github.oasis.core.services.api.beans.RedisRepository;
import io.github.oasis.db.redis.RedisDb;
import io.github.oasis.db.redis.RedisEventLoaderHandler;
import io.github.oasis.elements.badges.BadgesModuleFactory;
import io.github.oasis.elements.challenges.ChallengesModuleFactory;
import io.github.oasis.elements.milestones.MilestonesModuleFactory;
import io.github.oasis.elements.ratings.RatingsModuleFactory;
import io.github.oasis.engine.element.points.PointsModuleFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * @author Isuru Weerarathna
 */
public class MultiEngineTest {

    protected final ObjectMapper mapper = new ObjectMapper();

    protected Db dbPool;
    protected RedisRepository metadataSupport;

    @BeforeEach
    public void beforeEach() {

    }

    @Test
    public void testMultipleEngines() throws OasisException, IOException, ExecutionException, InterruptedException {
        OasisConfigs oasisConfigs = new OasisConfigs.Builder().buildFromYamlResource("test-defaults.yml");
        dbPool = RedisDb.create(oasisConfigs, "oasis.redis");
        dbPool.init();

        metadataSupport = new RedisRepository(dbPool, new JsonSerializer(mapper));

        try (DbContext db = dbPool.createContext()) {
            db.allKeys("*").forEach(db::removeKey);

            metadataSupport.addPlayer(new PlayerObject(1, "Jakob Floyd", "jakob@oasis.io"));
            metadataSupport.addPlayer(new PlayerObject(2, "Thierry Hines", "thierry@oasis.io"));
            metadataSupport.addPlayer(new PlayerObject(3, "Ray Glenn", "ray@oasis.io"));
            metadataSupport.addPlayer(new PlayerObject(4, "Lilia Stewart", "lilia@oasis.io"));
            metadataSupport.addPlayer(new PlayerObject(5, "Archer Roberts", "archer@oasis.io"));

            metadataSupport.addTeam(TeamObject.builder().id(1).gameId(1).name("Warriors").build());
        }


        OasisEngine engine1 = spawnEngine("test-engine1", oasisConfigs, dbPool);
        OasisEngine engine2 = spawnEngine("test-engine2", oasisConfigs, dbPool);

        engine1.start();
        engine2.start();

        engine1.startGame(1);
        engine2.startGame(1);

        boolean gameRunningE1 = engine1.isGameRunning(1);
        boolean gameRunningE2 = engine2.isGameRunning(1);
        Assertions.assertTrue(gameRunningE1 && gameRunningE2);

        engine1.stopGame(1);
        engine2.stopGame(1);
        Assertions.assertFalse(engine1.isGameRunning(1) || engine2.isGameRunning(1));
    }

    private OasisEngine spawnEngine(String engineId, OasisConfigs oasisConfigs, Db dbPool) {
        EngineContext.Builder builder = EngineContext.builder();
        EngineContext context = builder.withConfigs(oasisConfigs)
                .havingId(engineId)
                .withDb(dbPool)
                .withEventStore(new RedisEventLoaderHandler(dbPool, oasisConfigs))
                .installModule(RatingsModuleFactory.class)
                .installModule(PointsModuleFactory.class)
                .installModule(MilestonesModuleFactory.class)
                .installModule(ChallengesModuleFactory.class)
                .installModule(BadgesModuleFactory.class)
                .build();
        return new OasisEngine(context);
    }

}
