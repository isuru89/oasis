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
import akka.actor.ActorSystem;
import akka.testkit.TestKit;
import io.github.oasis.engine.actors.cmds.RuleAddedMessage;
import io.github.oasis.engine.external.Db;
import io.github.oasis.engine.external.DbContext;
import io.github.oasis.engine.factory.OasisDependencyModule;
import io.github.oasis.engine.model.ID;
import io.github.oasis.engine.rules.PointRule;
import io.github.oasis.engine.rules.TEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * @author Isuru Weerarathna
 */
public class OasisEngineTest {

    static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private static final String TEST_SYSTEM = "test-oasis-system";

    private static final String EVT_A = "event.a";
    private static final String EVT_B = "event.b";
    private static final double AMOUNT_10 = 10.0;
    private static final double AMOUNT_50 = 50.0;

    private OasisEngine engine;
    private ActorRef supervisor;
    private ActorSystem system;
    private TestKit testKit;
    @Inject
    private Db dbPool;

    @BeforeEach
    public void setup() throws IOException, InterruptedException {
        EngineContext context = new EngineContext();
        context.setModuleProvider(OasisDependencyModule::new);
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

    private void awaitTerminated() {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private Map<String, String> getDataOnKey(String key) {
        try (DbContext db = dbPool.createContext()) {
            return db.MAP(key).getAll();
        } catch (IOException e) {
            Assertions.fail("Unable to connect to db host!", e);
            return Map.of();
        }
    }

    private long TS(String timeStr) {
        return LocalDateTime.parse(timeStr, FORMATTER).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    @Test
    public void testOasisEngineStartup() {
        TEvent e1 = TEvent.createKeyValue(TS("2020-04-02 07:15"), EVT_A, 15);
        TEvent e2 = TEvent.createKeyValue(TS("2020-04-02 08:20"), EVT_A, 83);
        TEvent e3 = TEvent.createKeyValue(TS("2020-04-02 08:45"), EVT_A, 74);

        PointRule rule = new PointRule("test.point.rule");
        rule.setForEvent(EVT_A);
        rule.setAmountExpression((event, rule1) -> BigDecimal.valueOf((long)event.getFieldValue("value") - 50));
        rule.setCriteria((event, rule1) -> (long) event.getFieldValue("value") >= 50);

        supervisor.tell(RuleAddedMessage.create(TEvent.GAME_ID, rule), supervisor);
        supervisor.tell(e1, supervisor);
        supervisor.tell(e2, supervisor);
        supervisor.tell(e3, supervisor);
        awaitTerminated();

        Map<String, String> map = getDataOnKey(ID.getGameUserPointsSummary(TEvent.GAME_ID, TEvent.USER_ID));
        System.out.println(map);
    }

}
