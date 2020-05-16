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

import io.github.oasis.core.external.messages.GameCommand;
import io.github.oasis.engine.actors.cmds.RuleAddedMessage;
import io.github.oasis.elements.challenges.ChallengeOverEvent;
import io.github.oasis.elements.challenges.ChallengeRule;
import io.github.oasis.core.external.DbContext;
import io.github.oasis.core.ID;
import io.github.oasis.core.elements.matchers.SingleEventTypeMatcher;
import io.github.oasis.engine.model.TEvent;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Set;

import static io.github.oasis.engine.RedisAssert.assertKeyNotExist;
import static io.github.oasis.engine.RedisAssert.assertSorted;
import static io.github.oasis.engine.RedisAssert.ofSortedEntries;

/**
 * @author Isuru Weerarathna
 */
public class EngineChallengesTest extends OasisEngineTest {

    @Test
    public void testChallenges() {
        TEvent e1 = TEvent.createKeyValue(U1, TS("2020-03-21 07:15"), EVT_A, 57);
        TEvent e2 = TEvent.createKeyValue(U2, TS("2020-03-22 08:15"), EVT_A, 83);
        TEvent e3 = TEvent.createKeyValue(U3, TS("2020-03-25 07:15"), EVT_A, 34);
        TEvent e4 = TEvent.createKeyValue(U4, TS("2020-04-01 11:15"), EVT_A, 75);
        TEvent e5 = TEvent.createKeyValue(U4, TS("2020-04-02 07:15"), EVT_A, 99);
        TEvent e6 = TEvent.createKeyValue(U3, TS("2020-05-02 07:15"), EVT_A, 99);

        ChallengeRule rule = new ChallengeRule("test.challenge.rule");
        rule.setEventTypeMatcher(new SingleEventTypeMatcher(EVT_A));
        rule.setScope(ChallengeRule.ChallengeScope.GAME);
        rule.setCustomAwardPoints((event, rank, ctx) -> BigDecimal.valueOf(100 * (3-rank+1)));
        rule.setStartAt(TS("2020-03-01 07:15"));
        rule.setExpireAt(TS("2020-05-01 07:15"));
        rule.setCriteria((event, rule1, ctx) -> (long) event.getFieldValue("value") >= 50);
        rule.setWinnerCount(3);
        rule.setPointId("challenge.points");

        engine.submit(GameCommand.create(TEvent.GAME_ID, GameCommand.GameLifecycle.CREATE));
        engine.submit(GameCommand.create(TEvent.GAME_ID, GameCommand.GameLifecycle.START));
        engine.submit(RuleAddedMessage.create(TEvent.GAME_ID, rule));
        engine.submitAll(e1, e2, e3, e4, e5, e6);
        awaitTerminated();

        int gameId = e1.getGameId();
        String rid = rule.getId();
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
                        "rule:test.challenge.rule:D20200321", score,
                        "rule:test.challenge.rule:M202003", score,
                        "rule:test.challenge.rule:Q202001", score,
                        "rule:test.challenge.rule:W202012", score,
                        "rule:test.challenge.rule:Y2020", score,
                        "rule:test.challenge.rule", score,
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
                        "rule:test.challenge.rule:D20200322", score,
                        "rule:test.challenge.rule:M202003", score,
                        "rule:test.challenge.rule:Q202001", score,
                        "rule:test.challenge.rule:W202012", score,
                        "rule:test.challenge.rule:Y2020", score,
                        "rule:test.challenge.rule", score,
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
                        "rule:test.challenge.rule:D20200401", score,
                        "rule:test.challenge.rule:M202004", score,
                        "rule:test.challenge.rule:Q202002", score,
                        "rule:test.challenge.rule:W202014", score,
                        "rule:test.challenge.rule:Y2020", score,
                        "rule:test.challenge.rule", score,
                        "source:1", score,
                        "team:1:D20200401", score,
                        "team:1:M202004", score,
                        "team:1:Q202002", score,
                        "team:1:W202014", score,
                        "team:1:Y2020", score,
                        "team:1", score));
    }

    @Test
    public void testOutOfOrderChallenge() {
        TEvent e1 = TEvent.createWithTeam(U1, 1, TS("2020-03-23 08:00"), EVT_A, 57);
        TEvent e2 = TEvent.createWithTeam(U1, 2, TS("2020-03-24 08:00"), EVT_A, 83);
        TEvent e3 = TEvent.createWithTeam(U3, 2, TS("2020-03-25 08:00"), EVT_A, 98);
        TEvent e4 = TEvent.createWithTeam(U4, 2, TS("2020-03-26 08:00"), EVT_A, 75);
        TEvent e5 = TEvent.createWithTeam(U1, 1, TS("2020-03-26 08:00"), EVT_A, 88);
        TEvent e6 = TEvent.createWithTeam(U1, 1, TS("2020-04-02 08:00"), EVT_A, 71);
        TEvent e7 = TEvent.createWithTeam(U5, 2, TS("2020-03-25 11:00"), EVT_A, 64);
        TEvent e8 = TEvent.createWithTeam(U4, 2, TS("2020-04-03 08:00"), EVT_A, 50);

        ChallengeRule rule = new ChallengeRule("test.challenge.rule");
        rule.setEventTypeMatcher(new SingleEventTypeMatcher(EVT_A));
        rule.setScope(ChallengeRule.ChallengeScope.TEAM);
        rule.setScopeId(2);
        rule.setCustomAwardPoints((event, rank, r) -> BigDecimal.valueOf(100 * (3-rank+1)));
        rule.setStartAt(TS("2020-03-01 07:15"));
        rule.setExpireAt(TS("2020-05-01 07:15"));
        rule.setCriteria((event, rule1, ctx) -> (long) event.getFieldValue("value") >= 50);
        rule.setWinnerCount(3);
        rule.setPointId("challenge.points");
        rule.setFlags(Set.of(ChallengeRule.OUT_OF_ORDER_WINNERS));

        engine.submit(GameCommand.create(TEvent.GAME_ID, GameCommand.GameLifecycle.CREATE));
        engine.submit(GameCommand.create(TEvent.GAME_ID, GameCommand.GameLifecycle.START));
        engine.submit(RuleAddedMessage.create(TEvent.GAME_ID, rule));
        engine.submitAll(e1, e2, e3, e4, e5, e6, e7, e8,
                ChallengeOverEvent.createFor(e1.getGameId(), rule.getId()));
        awaitTerminated();

        int gameId = TEvent.GAME_ID;
        String ruleId = rule.getId();
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
    }

    @Test
    public void testOutOfOrderChallengeNoPointsUntil() {
        TEvent e1 = TEvent.createWithTeam(U1, 1, TS("2020-03-23 08:00"), EVT_A, 57);
        TEvent e2 = TEvent.createWithTeam(U1, 2, TS("2020-03-24 08:00"), EVT_A, 83);
        TEvent e3 = TEvent.createWithTeam(U3, 2, TS("2020-03-25 08:00"), EVT_A, 98);
        TEvent e4 = TEvent.createWithTeam(U1, 2, TS("2020-03-26 08:00"), EVT_A, 75);
        TEvent e5 = TEvent.createWithTeam(U1, 1, TS("2020-03-26 08:00"), EVT_A, 88);
        TEvent e6 = TEvent.createWithTeam(U1, 1, TS("2020-04-02 08:00"), EVT_A, 71);
        TEvent e7 = TEvent.createWithTeam(U5, 2, TS("2020-03-25 11:00"), EVT_A, 64);
        TEvent e8 = TEvent.createWithTeam(U4, 2, TS("2020-04-03 08:00"), EVT_A, 50);

        ChallengeRule rule = new ChallengeRule("test.challenge.rule");
        rule.setEventTypeMatcher(new SingleEventTypeMatcher(EVT_A));
        rule.setScope(ChallengeRule.ChallengeScope.TEAM);
        rule.setScopeId(2);
        rule.setCustomAwardPoints((event, rank, r) -> BigDecimal.valueOf(100 * (3-rank+1)));
        rule.setStartAt(TS("2020-03-01 07:15"));
        rule.setExpireAt(TS("2020-05-01 07:15"));
        rule.setCriteria((event, rule1, ctx) -> (long) event.getFieldValue("value") >= 50);
        rule.setWinnerCount(3);
        rule.setPointId("challenge.points");
        rule.setFlags(Set.of(ChallengeRule.OUT_OF_ORDER_WINNERS));

        engine.submit(GameCommand.create(TEvent.GAME_ID, GameCommand.GameLifecycle.CREATE));
        engine.submit(GameCommand.create(TEvent.GAME_ID, GameCommand.GameLifecycle.START));
        engine.submit(RuleAddedMessage.create(TEvent.GAME_ID, rule));
        engine.submitAll(e1, e2, e3, e4, e5, e6, e7, e8);
        awaitTerminated();

        int gameId = TEvent.GAME_ID;
        String ruleId = rule.getId();
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
