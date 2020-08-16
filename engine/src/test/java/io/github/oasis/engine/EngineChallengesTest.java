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

import com.google.gson.Gson;
import io.github.oasis.core.Event;
import io.github.oasis.core.ID;
import io.github.oasis.core.elements.GameDef;
import io.github.oasis.core.external.DbContext;
import io.github.oasis.core.external.messages.GameCommand;
import io.github.oasis.elements.challenges.ChallengeOverEvent;
import io.github.oasis.elements.challenges.stats.ChallengeStats;
import io.github.oasis.elements.challenges.stats.to.UserChallengeRequest;
import io.github.oasis.elements.challenges.stats.to.UserChallengesLog;
import io.github.oasis.engine.model.TEvent;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.UUID;

import static io.github.oasis.engine.RedisAssert.assertKeyNotExist;
import static io.github.oasis.engine.RedisAssert.assertSorted;
import static io.github.oasis.engine.RedisAssert.ofSortedEntries;

/**
 * @author Isuru Weerarathna
 */
public class EngineChallengesTest extends OasisEngineTest {

    private final Gson gson = new Gson();

    @Test
    public void testChallenges() throws Exception {
        Event e1 = TEvent.createKeyValue(U1, TS("2020-03-21 07:15"), EVT_A, 57, UUID.fromString("90f50601-86e9-483c-aa75-6f8d80466d79"));
        Event e2 = TEvent.createKeyValue(U2, TS("2020-03-22 08:15"), EVT_A, 83, UUID.fromString("a0d05445-a007-4ab7-9999-6d81f29d889c"));
        Event e3 = TEvent.createKeyValue(U3, TS("2020-03-25 07:15"), EVT_A, 34);
        Event e4 = TEvent.createKeyValue(U4, TS("2020-04-01 11:15"), EVT_A, 75);
        Event e5 = TEvent.createKeyValue(U4, TS("2020-04-02 07:15"), EVT_A, 99);
        Event e6 = TEvent.createKeyValue(U3, TS("2020-05-02 07:15"), EVT_A, 99);

        GameDef gameDef = loadRulesFromResource("rules/challenges-basic.yml");

        engine.submit(GameCommand.create(TEvent.GAME_ID, GameCommand.GameLifecycle.CREATE));
        engine.submit(GameCommand.create(TEvent.GAME_ID, GameCommand.GameLifecycle.START));
        submitRules(engine, TEvent.GAME_ID, gameDef);
        engine.submitAll(e1, e2, e3, e4, e5, e6);
        awaitTerminated();

        int gameId = e1.getGameId();
        String rid = "CHG000001";
        try (DbContext db = dbPool.createContext()) {
            System.out.println("u1" + db.MAP(ID.getGameUserPointsSummary(gameId, U1)).getAll());
            System.out.println("u2" + db.MAP(ID.getGameUserPointsSummary(gameId, U2)).getAll());
            System.out.println("u4" + db.MAP(ID.getGameUserPointsSummary(gameId, U4)).getAll());
        } catch (IOException e) {
            e.printStackTrace();
        }

        RedisAssert.assertSortedRef(dbPool,
                ID.getGameUseChallengesLog(gameId, U1),
                ID.getGameUseChallengesSummary(gameId, U1),
                ofSortedEntries(rid + ":1:" + e1.getExternalId(), e1.getTimestamp()));
        RedisAssert.assertSortedRef(dbPool,
                ID.getGameUseChallengesLog(gameId, U2),
                ID.getGameUseChallengesSummary(gameId, U2),
                ofSortedEntries(rid + ":2:" + e2.getExternalId(), e2.getTimestamp()));
        RedisAssert.assertSortedRef(dbPool,
                ID.getGameUseChallengesLog(gameId, U4),
                ID.getGameUseChallengesSummary(gameId, U4),
                ofSortedEntries(rid + ":3:" + e4.getExternalId(), e4.getTimestamp()));

        String score = "300";
        RedisAssert.assertMap(dbPool, ID.getGameUserPointsSummary(e1.getGameId(), U1),
                RedisAssert.ofEntries("all:D20200321", score,
                        "all:M202003", score,
                        "all:Q202001", score,
                        "all:W202012", score,
                        "all:Y2020", score,
                        "all", score,
                        "rule:challenge.points:D20200321", score,
                        "rule:challenge.points:M202003", score,
                        "rule:challenge.points:Q202001", score,
                        "rule:challenge.points:W202012", score,
                        "rule:challenge.points:Y2020", score,
                        "rule:challenge.points", score,
                        "source:1", score,
                        "team:1:D20200321", score,
                        "team:1:M202003", score,
                        "team:1:Q202001", score,
                        "team:1:W202012", score,
                        "team:1:Y2020", score,
                        "team:1", score));
        score = "200";
        RedisAssert.assertMap(dbPool, ID.getGameUserPointsSummary(e1.getGameId(), U2),
                RedisAssert.ofEntries(
                        "all:D20200322", score,
                        "all:M202003", score,
                        "all:Q202001", score,
                        "all:W202012", score,
                        "all:Y2020", score,
                        "all", score,
                        "rule:challenge.points:D20200322", score,
                        "rule:challenge.points:M202003", score,
                        "rule:challenge.points:Q202001", score,
                        "rule:challenge.points:W202012", score,
                        "rule:challenge.points:Y2020", score,
                        "rule:challenge.points", score,
                        "source:1", score,
                        "team:1:D20200322", score,
                        "team:1:M202003", score,
                        "team:1:Q202001", score,
                        "team:1:W202012", score,
                        "team:1:Y2020", score,
                        "team:1", score));
        score = "100";
        RedisAssert.assertMap(dbPool, ID.getGameUserPointsSummary(e1.getGameId(), U4),
                RedisAssert.ofEntries(
                        "all:D20200401", score,
                        "all:M202004", score,
                        "all:Q202002", score,
                        "all:W202014", score,
                        "all:Y2020", score,
                        "all", score,
                        "rule:challenge.points:D20200401", score,
                        "rule:challenge.points:M202004", score,
                        "rule:challenge.points:Q202002", score,
                        "rule:challenge.points:W202014", score,
                        "rule:challenge.points:Y2020", score,
                        "rule:challenge.points", score,
                        "source:1", score,
                        "team:1:D20200401", score,
                        "team:1:M202004", score,
                        "team:1:Q202002", score,
                        "team:1:W202014", score,
                        "team:1:Y2020", score,
                        "team:1", score));


        ChallengeStats stats = new ChallengeStats(dbPool);

        compareStatReqRes("stats/challenges/user-log-req.json", UserChallengeRequest.class,
                "stats/challenges/user-log-res.json", UserChallengesLog.class,
                req -> (UserChallengesLog) stats.getUserChallengeLog(req));
        compareStatReqRes("stats/challenges/user-log-bytime-req.json", UserChallengeRequest.class,
                "stats/challenges/user-log-bytime-res.json", UserChallengesLog.class,
                req -> (UserChallengesLog) stats.getUserChallengeLog(req));
    }

    @Test
    public void testOutOfOrderChallenge() {
        Event e1 = TEvent.createWithTeam(U1, 1, TS("2020-03-23 08:00"), EVT_A, 57);
        Event e2 = TEvent.createWithTeam(U1, 2, TS("2020-03-24 08:00"), EVT_A, 83);
        Event e3 = TEvent.createWithTeam(U3, 2, TS("2020-03-25 08:00"), EVT_A, 98);
        Event e4 = TEvent.createWithTeam(U4, 2, TS("2020-03-26 08:00"), EVT_A, 75);
        Event e5 = TEvent.createWithTeam(U1, 1, TS("2020-03-26 08:00"), EVT_A, 88);
        Event e6 = TEvent.createWithTeam(U1, 1, TS("2020-04-02 08:00"), EVT_A, 71);
        Event e7 = TEvent.createWithTeam(U5, 2, TS("2020-03-25 11:00"), EVT_A, 64);
        Event e8 = TEvent.createWithTeam(U4, 2, TS("2020-04-03 08:00"), EVT_A, 50);

        GameDef gameDef = loadRulesFromResource("rules/challenges-outoforder.yml");
        String ruleId = "CHG000001";

        engine.submit(GameCommand.create(TEvent.GAME_ID, GameCommand.GameLifecycle.CREATE));
        engine.submit(GameCommand.create(TEvent.GAME_ID, GameCommand.GameLifecycle.START));
        submitRules(engine, TEvent.GAME_ID, gameDef);
        engine.submitAll(e1, e2, e3, e4, e5, e6, e7, e8,
                ChallengeOverEvent.createFor(e1.getGameId(), ruleId));
        awaitTerminated();

        int gameId = TEvent.GAME_ID;
        assertSorted(dbPool,
                ID.getGameChallengeKey(gameId, ruleId),
                ofSortedEntries(
                        "u" + U1, e2.getTimestamp(),
                        "u" + U3, e3.getTimestamp(),
                        "u" + U5, e7.getTimestamp(),
                        "u" + U4, e4.getTimestamp()
                ));

        assertSorted(dbPool,
                ID.getGameLeaderboard(gameId, "all", ""),
                ofSortedEntries(U1, 300,
                        U3, 200,
                        U5, 100));
        assertSorted(dbPool, ID.getGameLeaderboard(gameId, "d", "D20200324"), ofSortedEntries(U1, 300));
        assertSorted(dbPool, ID.getGameLeaderboard(gameId, "d", "D20200325"), ofSortedEntries(U5, 100, U3, 200));

        ChallengeStats stats = new ChallengeStats(dbPool);

    }

    @Test
    public void testOutOfOrderChallengeNoPointsUntil() {
        Event e1 = TEvent.createWithTeam(U1, 1, TS("2020-03-23 08:00"), EVT_A, 57);
        Event e2 = TEvent.createWithTeam(U1, 2, TS("2020-03-24 08:00"), EVT_A, 83);
        Event e3 = TEvent.createWithTeam(U3, 2, TS("2020-03-25 08:00"), EVT_A, 98);
        Event e4 = TEvent.createWithTeam(U1, 2, TS("2020-03-26 08:00"), EVT_A, 75);
        Event e5 = TEvent.createWithTeam(U1, 1, TS("2020-03-26 08:00"), EVT_A, 88);
        Event e6 = TEvent.createWithTeam(U1, 1, TS("2020-04-02 08:00"), EVT_A, 71);
        Event e7 = TEvent.createWithTeam(U5, 2, TS("2020-03-25 11:00"), EVT_A, 64);
        Event e8 = TEvent.createWithTeam(U4, 2, TS("2020-04-03 08:00"), EVT_A, 50);

        GameDef gameDef = loadRulesFromResource("rules/challenges-outoforder.yml");

        engine.submit(GameCommand.create(TEvent.GAME_ID, GameCommand.GameLifecycle.CREATE));
        engine.submit(GameCommand.create(TEvent.GAME_ID, GameCommand.GameLifecycle.START));
        submitRules(engine, TEvent.GAME_ID, gameDef);
        engine.submitAll(e1, e2, e3, e4, e5, e6, e7, e8);
        awaitTerminated();

        int gameId = TEvent.GAME_ID;
        String ruleId = "CHG000001";
        assertSorted(dbPool,
                ID.getGameChallengeKey(gameId, ruleId),
                ofSortedEntries(
                        "u" + U1, e2.getTimestamp(),
                        "u" + U3, e3.getTimestamp(),
                        "u" + U5, e7.getTimestamp()
                ));
        assertKeyNotExist(dbPool, ID.getGameLeaderboard(gameId, "all", ""));
    }
}
