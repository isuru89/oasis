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
import io.github.oasis.engine.model.EventExecutionFilter;
import io.github.oasis.engine.model.EventValueResolver;
import io.github.oasis.engine.model.ID;
import io.github.oasis.engine.model.TEvent;
import io.github.oasis.engine.rules.BadgeStreakNRule;
import io.github.oasis.engine.rules.ChallengeRule;
import io.github.oasis.engine.rules.MilestoneRule;
import io.github.oasis.engine.rules.PointRule;
import io.github.oasis.engine.rules.RatingRule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static io.github.oasis.engine.rules.MilestoneRule.MilestoneFlag.SKIP_NEGATIVE_VALUES;

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

    static final long U1 = 1;
    static final long U2 = 2;
    static final long U3 = 3;
    static final long U4 = 4;
    static final long U5 = 5;

    private OasisEngine engine;
    private ActorRef supervisor;
    private ActorSystem system;
    private TestKit testKit;
    @Inject
    private Db dbPool;

    @BeforeEach
    public void setup() throws IOException, InterruptedException {
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

    private void submit(ActorRef actorRef, TEvent... events) {
        for (TEvent event : events) {
            actorRef.tell(event, actorRef);
        }
    }

    @Test
    public void testEnginePoints() {
        TEvent e1 = TEvent.createKeyValue(TS("2020-04-02 07:15"), EVT_A, 15);
        TEvent e2 = TEvent.createKeyValue(TS("2020-04-02 08:20"), EVT_A, 83);
        TEvent e3 = TEvent.createKeyValue(TS("2020-04-02 08:45"), EVT_A, 74);

        PointRule rule = new PointRule("test.point.rule");
        rule.setForEvent(EVT_A);
        rule.setAmountExpression((event, rule1) -> BigDecimal.valueOf((long)event.getFieldValue("value") - 50));
        rule.setCriteria((event, rule1, ctx) -> (long) event.getFieldValue("value") >= 50);

        supervisor.tell(RuleAddedMessage.create(TEvent.GAME_ID, rule), supervisor);
        supervisor.tell(e1, supervisor);
        supervisor.tell(e2, supervisor);
        supervisor.tell(e3, supervisor);
        awaitTerminated();

        Map<String, String> map = getDataOnKey(ID.getGameUserPointsSummary(TEvent.GAME_ID, TEvent.USER_ID));
        System.out.println(map);
    }

    @Test
    public void testEngineBadges() {
        TEvent e1 = TEvent.createKeyValue(TS("2020-03-23 11:15"), EVT_A, 75);
        TEvent e2 = TEvent.createKeyValue(TS("2020-03-25 09:55"), EVT_A, 63);
        TEvent e3 = TEvent.createKeyValue(TS("2020-03-31 14:15"), EVT_A, 57);
        TEvent e4 = TEvent.createKeyValue(TS("2020-04-01 05:15"), EVT_A, 88);
        TEvent e5 = TEvent.createKeyValue(TS("2020-03-24 11:15"), EVT_A, 76);
        TEvent e6 = TEvent.createKeyValue(TS("2020-04-05 11:15"), EVT_A, 26);

        BadgeStreakNRule rule = new BadgeStreakNRule("abc");
        rule.setForEvent(EVT_A);
        rule.setStreaks(List.of(3, 5));
        rule.setCriteria((e,r,c) -> (long) e.getFieldValue("value") >= 50);
        rule.setRetainTime(10);

        supervisor.tell(RuleAddedMessage.create(TEvent.GAME_ID, rule), supervisor);
        supervisor.tell(e1, supervisor);
        supervisor.tell(e2, supervisor);
        supervisor.tell(e3, supervisor);
        supervisor.tell(e4, supervisor);
        supervisor.tell(e5, supervisor);
        supervisor.tell(e6, supervisor);
        awaitTerminated();

        Map<String, String> map = getDataOnKey(ID.getGameUserPointsSummary(TEvent.GAME_ID, TEvent.USER_ID));
        System.out.println(map);
    }

    @Test
    public void testMilestones() {
        TEvent e1 = TEvent.createKeyValue(100, EVT_A, 87);
        TEvent e2 = TEvent.createKeyValue(105, EVT_A, 53);
        TEvent e3 = TEvent.createKeyValue(110, EVT_A, 34);
        TEvent e4 = TEvent.createKeyValue(115, EVT_A, 11);
        TEvent e5 = TEvent.createKeyValue(120, EVT_A, 84);
        TEvent e6 = TEvent.createKeyValue(125, EVT_A, 92);
        TEvent e7 = TEvent.createKeyValue(130, EVT_A, 100);

        MilestoneRule rule = new MilestoneRule("test.milestone.rule");
        rule.setForEvent(EVT_A);
        rule.setValueExtractor((event, rule1, ctx) -> BigDecimal.valueOf((long)event.getFieldValue("value")));
        rule.setLevels(Arrays.asList(new MilestoneRule.Level(1, BigDecimal.valueOf(100)),
                new MilestoneRule.Level(2, BigDecimal.valueOf(200)),
                new MilestoneRule.Level(3, BigDecimal.valueOf(300)),
                new MilestoneRule.Level(4, BigDecimal.valueOf(500))));
        rule.setFlags(new HashSet<>(Collections.singletonList(SKIP_NEGATIVE_VALUES)));

        supervisor.tell(RuleAddedMessage.create(TEvent.GAME_ID, rule), supervisor);
        supervisor.tell(e1, supervisor);
        supervisor.tell(e2, supervisor);
        supervisor.tell(e3, supervisor);
        supervisor.tell(e4, supervisor);
        supervisor.tell(e5, supervisor);
        supervisor.tell(e6, supervisor);
        supervisor.tell(e7, supervisor);
        awaitTerminated();

        Map<String, String> map = getDataOnKey(ID.getGameUserPointsSummary(TEvent.GAME_ID, TEvent.USER_ID));
        System.out.println(map);
    }

    @Test
    public void testChallenges() {
        TEvent e1 = TEvent.createKeyValue(U1, TS("2020-03-21 07:15"), EVT_A, 57);
        TEvent e2 = TEvent.createKeyValue(U2, TS("2020-03-22 08:15"), EVT_A, 83);
        TEvent e3 = TEvent.createKeyValue(U3, TS("2020-03-25 07:15"), EVT_A, 34);
        TEvent e4 = TEvent.createKeyValue(U4, TS("2020-04-01 11:15"), EVT_A, 75);
        TEvent e5 = TEvent.createKeyValue(U4, TS("2020-04-02 07:15"), EVT_A, 99);

        ChallengeRule rule = new ChallengeRule("test.challenge.rule");
        rule.setForEvent(EVT_A);
        rule.setScope(ChallengeRule.ChallengeScope.GAME);
        rule.setCustomAwardPoints((event, rank, ctx) -> BigDecimal.valueOf(100 * rank));
        rule.setStartAt(TS("2020-03-01 07:15"));
        rule.setExpireAt(TS("2020-05-01 07:15"));
        rule.setCriteria((event, rule1, ctx) -> (long) event.getFieldValue("value") >= 50);
        rule.setWinnerCount(3);
        rule.setPointId("challenge.points");

        supervisor.tell(RuleAddedMessage.create(TEvent.GAME_ID, rule), supervisor);
        submit(supervisor, e1, e2, e3, e4, e5);
        awaitTerminated();


    }

    @Test
    public void testRatings() {
        TEvent e1 = TEvent.createKeyValue(100, EVT_A, 87);
        TEvent e2 = TEvent.createKeyValue(105, EVT_A, 66);
        TEvent e3 = TEvent.createKeyValue(110, EVT_A, 34);

        RatingRule rule = new RatingRule("test.rating.rule");
        rule.setForEvent(EVT_A);
        rule.setDefaultRating(1);
        rule.setRatings(Arrays.asList(
                new RatingRule.Rating(1, 3, checkGt(85), pointAward(3), "rating.points"),
                new RatingRule.Rating(2, 2, checkGt(65), pointAward(2), "rating.points"),
                new RatingRule.Rating(3, 1, checkGt(50), pointAward(1), "rating.points")
        ));

        supervisor.tell(RuleAddedMessage.create(TEvent.GAME_ID, rule), supervisor);
        submit(supervisor, e1, e2, e3);
        awaitTerminated();
    }

    private EventValueResolver<Integer> pointAward(int currRating) {
        return (event, prevRating) -> BigDecimal.valueOf((currRating - prevRating) * 10.0);
    }

    private EventExecutionFilter checkGt(long margin) {
        return (e, r, ctx) -> (long) e.getFieldValue("value") >= margin;
    }

    private static class TestConfigProvider implements Provider<OasisConfigs> {

        private static final OasisConfigs DEFAULT = new OasisConfigs.Builder()
                .withSupervisors(2, 2, 1)
                .withExecutors(2, 2)
                .build();

        @Override
        public OasisConfigs get() {
            return DEFAULT;
        }
    }
}
