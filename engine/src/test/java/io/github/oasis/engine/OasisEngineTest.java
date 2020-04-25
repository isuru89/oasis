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

package io.github.oasis.engine;

import akka.actor.ActorRef;
import io.github.oasis.engine.EngineContext;
import io.github.oasis.core.configs.OasisConfigs;
import io.github.oasis.engine.OasisEngine;
import io.github.oasis.core.exception.OasisException;
import io.github.oasis.elements.badges.BadgesModuleFactory;
import io.github.oasis.elements.challenges.ChallengesModuleFactory;
import io.github.oasis.elements.milestones.MilestonesModuleFactory;
import io.github.oasis.engine.element.points.PointsModuleFactory;
import io.github.oasis.elements.ratings.RatingsModuleFactory;
import io.github.oasis.core.external.Db;
import io.github.oasis.core.external.DbContext;
import io.github.oasis.db.redis.RedisDb;
import io.github.oasis.db.redis.RedisEventLoader;
import io.github.oasis.engine.model.TEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * @author Isuru Weerarathna
 */
public class OasisEngineTest {

    static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private static final String TEST_SYSTEM = "test-oasis-system";

    protected static final String EVT_A = "event.a";
    protected static final String EVT_B = "event.b";
    private static final double AMOUNT_10 = 10.0;
    private static final double AMOUNT_50 = 50.0;

    static final long U1 = 1;
    static final long U2 = 2;
    static final long U3 = 3;
    static final long U4 = 4;
    static final long U5 = 5;

    protected OasisEngine engine;

    protected Db dbPool;

    @BeforeEach
    public void setup() throws IOException, OasisException {
        EngineContext context = new EngineContext();
        OasisConfigs oasisConfigs = new OasisConfigs.Builder()
                .withSupervisors(2, 1, 1)
                .withExecutors(1, 2)
                .build();
        dbPool = RedisDb.create(oasisConfigs);
        dbPool.init();

        context.setModuleFactoryList(List.of(
                RatingsModuleFactory.class,
                PointsModuleFactory.class,
                MilestonesModuleFactory.class,
                ChallengesModuleFactory.class,
                BadgesModuleFactory.class
                ));
        context.setConfigs(oasisConfigs);
        context.setDb(dbPool);
        context.setEventStore(new RedisEventLoader(dbPool, oasisConfigs));
        engine = new OasisEngine(context);
        engine.start();

        try (DbContext db = dbPool.createContext()) {
            db.allKeys("*").forEach(db::removeKey);
        }
    }

    @AfterEach
    public void shutdown() throws IOException, InterruptedException {

    }

    protected void awaitTerminated() {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    protected long TS(String timeStr) {
        return LocalDateTime.parse(timeStr, FORMATTER).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    protected void submit(ActorRef actorRef, TEvent... events) {
        for (TEvent event : events) {
            actorRef.tell(event, actorRef);
        }
    }

}
