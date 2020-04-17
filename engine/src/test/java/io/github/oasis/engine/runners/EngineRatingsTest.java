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

import io.github.oasis.engine.actors.cmds.RuleAddedMessage;
import io.github.oasis.engine.elements.ratings.RatingRule;
import io.github.oasis.core.external.DbContext;
import io.github.oasis.core.elements.EventExecutionFilter;
import io.github.oasis.core.elements.EventValueResolver;
import io.github.oasis.engine.model.ID;
import io.github.oasis.engine.model.SingleEventTypeMatcher;
import io.github.oasis.engine.model.TEvent;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;

/**
 * @author Isuru Weerarathna
 */
public class EngineRatingsTest extends OasisEngineTest {

    @Test
    public void testRatings() {
        TEvent e1 = TEvent.createKeyValue(TS("2020-03-24 07:15"), EVT_A, 87);
        TEvent e2 = TEvent.createKeyValue(TS("2020-03-24 11:15"), EVT_A, 66);
        TEvent e3 = TEvent.createKeyValue(TS("2020-03-24 20:15"), EVT_A, 54);

        RatingRule rule = new RatingRule("test.rating.rule");
        rule.setEventTypeMatcher(new SingleEventTypeMatcher(EVT_A));
        rule.setDefaultRating(1);
        rule.setRatings(Arrays.asList(
                new RatingRule.Rating(1, 3, checkGt(85), pointAward(3), "rating.points"),
                new RatingRule.Rating(2, 2, checkGt(65), pointAward(2), "rating.points"),
                new RatingRule.Rating(3, 1, checkGt(50), pointAward(1), "rating.points")
        ));

        supervisor.tell(RuleAddedMessage.create(TEvent.GAME_ID, rule), supervisor);
        submit(supervisor, e1, e2, e3);
        awaitTerminated();

        String rid = rule.getId();
        RedisAssert.assertMap(dbPool, ID.getGameUserPointsSummary(e1.getGameId(), e1.getUser()),
                RedisAssert.ofEntries(
                        "all","0",
                        "all:D20200324","0",
                        "all:M202003","0",
                        "all:Q202001","0",
                        "all:W202013","0",
                        "all:Y2020","0",
                        "rule:test.rating.rule","0",
                        "rule:test.rating.rule:D20200324","0",
                        "rule:test.rating.rule:M202003","0",
                        "rule:test.rating.rule:Q202001","0",
                        "rule:test.rating.rule:W202013","0",
                        "rule:test.rating.rule:Y2020","0",
                        "source:1","0",
                        "team:1","0",
                        "team:1:D20200324","0",
                        "team:1:M202003","0",
                        "team:1:Q202001","0",
                        "team:1:W202013","0",
                        "team:1:Y2020","0"
                ));
        RedisAssert.assertSorted(dbPool, ID.getGameUserRatingsLog(e1.getGameId(), e1.getUser()),
                RedisAssert.ofSortedEntries(
                        rid + ":1:3:" + e1.getExternalId(), e1.getTimestamp(),
                        rid + ":3:2:" + e2.getExternalId(), e2.getTimestamp(),
                        rid + ":2:1:" + e3.getExternalId(), e3.getTimestamp()
                ));
    }

    @Test
    public void testRatingsWithNetPoints() {
        TEvent e1 = TEvent.createKeyValue(TS("2020-03-24 07:15"), EVT_A, 87);
        TEvent e2 = TEvent.createKeyValue(TS("2020-03-24 11:15"), EVT_A, 66);
        TEvent e3 = TEvent.createKeyValue(TS("2020-03-24 20:15"), EVT_A, 68);

        RatingRule rule = new RatingRule("test.rating.rule");
        rule.setEventTypeMatcher(new SingleEventTypeMatcher(EVT_A));
        rule.setDefaultRating(1);
        rule.setRatings(Arrays.asList(
                new RatingRule.Rating(1, 3, checkGt(85), pointAward(3), "rating.points"),
                new RatingRule.Rating(2, 2, checkGt(65), pointAward(2), "rating.points"),
                new RatingRule.Rating(3, 1, checkGt(50), pointAward(1), "rating.points")
        ));

        supervisor.tell(RuleAddedMessage.create(TEvent.GAME_ID, rule), supervisor);
        submit(supervisor, e1, e2, e3);
        awaitTerminated();

        try (DbContext db = dbPool.createContext()) {
            System.out.println(db.MAP(ID.getGameUserPointsSummary(e1.getGameId(), e1.getUser())).getAll());
        } catch (IOException e) {
            e.printStackTrace();
        }
        String rid = rule.getId();
        RedisAssert.assertSorted(dbPool, ID.getGameUserRatingsLog(e1.getGameId(), e1.getUser()),
                RedisAssert.ofSortedEntries(
                        rid + ":1:3:" + e1.getExternalId(), e1.getTimestamp(),
                        rid + ":3:2:" + e2.getExternalId(), e2.getTimestamp()
                ));
        RedisAssert.assertMap(dbPool, ID.getGameUserPointsSummary(e1.getGameId(), e1.getUser()),
                RedisAssert.ofEntries(
                        "all","10",
                        "all:D20200324","10",
                        "all:M202003","10",
                        "all:Q202001","10",
                        "all:W202013","10",
                        "all:Y2020","10",
                        "rule:test.rating.rule","10",
                        "rule:test.rating.rule:D20200324","10",
                        "rule:test.rating.rule:M202003","10",
                        "rule:test.rating.rule:Q202001","10",
                        "rule:test.rating.rule:W202013","10",
                        "rule:test.rating.rule:Y2020","10",
                        "source:1","10",
                        "team:1","10",
                        "team:1:D20200324","10",
                        "team:1:M202003","10",
                        "team:1:Q202001","10",
                        "team:1:W202013","10",
                        "team:1:Y2020","10"
                ));

    }


    private EventValueResolver<Integer> pointAward(int currRating) {
        return (event, prevRating) -> BigDecimal.valueOf((currRating - prevRating) * 10.0);
    }

    private EventExecutionFilter checkGt(long margin) {
        return (e, r, ctx) -> (long) e.getFieldValue("value") >= margin;
    }

}
