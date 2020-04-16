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

package io.github.oasis.engine.runners;

import akka.actor.ActorRef;
import io.github.oasis.engine.EngineContext;
import io.github.oasis.engine.OasisConfigs;
import io.github.oasis.engine.OasisEngine;
import io.github.oasis.engine.external.Db;
import io.github.oasis.engine.external.DbContext;
import io.github.oasis.engine.factory.OasisDependencyModule;
import io.github.oasis.engine.model.TEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

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
    protected ActorRef supervisor;
    @Inject
    protected Db dbPool;

    @BeforeEach
    public void setup() throws IOException {
        EngineContext context = new EngineContext();
        context.setConfigsProvider(new TestConfigProvider());
        context.setModuleProvider(actorSystem -> new OasisDependencyModule(actorSystem, context));
        engine = new OasisEngine(context);
        engine.start();
        supervisor = engine.getOasisActor();
        engine.getProviderModule().getInjector().injectMembers(this);

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

    private static class TestConfigProvider implements Provider<OasisConfigs> {

        private static final OasisConfigs DEFAULT = new OasisConfigs.Builder()
                .withSupervisors(2, 1, 1)
                .withExecutors(1, 2)
                .build();

        @Override
        public OasisConfigs get() {
            return DEFAULT;
        }
    }
}
