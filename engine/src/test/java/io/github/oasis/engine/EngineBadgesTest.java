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
import io.github.oasis.elements.badges.rules.BadgeStreakNRule;
import io.github.oasis.core.ID;
import io.github.oasis.core.elements.matchers.SingleEventTypeMatcher;
import io.github.oasis.engine.model.TEvent;
import org.junit.jupiter.api.Test;

import java.util.Map;

/**
 * @author Isuru Weerarathna
 */
public class EngineBadgesTest extends OasisEngineTest {

    @Test
    public void testEngineBadges() {
        TEvent e1 = TEvent.createKeyValue(TS("2020-03-23 11:15"), EVT_A, 75);
        TEvent e2 = TEvent.createKeyValue(TS("2020-03-25 09:55"), EVT_A, 63);
        TEvent e3 = TEvent.createKeyValue(TS("2020-03-31 14:15"), EVT_A, 57);
        TEvent e4 = TEvent.createKeyValue(TS("2020-04-01 05:15"), EVT_A, 88);
        TEvent e5 = TEvent.createKeyValue(TS("2020-03-24 11:15"), EVT_A, 76);
        TEvent e6 = TEvent.createKeyValue(TS("2020-04-05 11:15"), EVT_A, 26);

        BadgeStreakNRule rule = new BadgeStreakNRule("test.badge.rule");
        rule.setEventTypeMatcher(new SingleEventTypeMatcher(EVT_A));
        rule.setStreaks(Map.of(3, 10, 5, 20));
        rule.setCriteria((e,r,c) -> (long) e.getFieldValue("value") >= 50);
        rule.setRetainTime(10);

        engine.submit(GameCommand.create(TEvent.GAME_ID, GameCommand.GameLifecycle.CREATE));
        engine.submit(GameCommand.create(TEvent.GAME_ID, GameCommand.GameLifecycle.START));
        engine.submit(RuleAddedMessage.create(TEvent.GAME_ID, rule));
        engine.submitAll(e1, e2, e3, e4, e5, e6);
        awaitTerminated();

        RedisAssert.assertMap(dbPool, ID.getGameUserBadgesSummary(TEvent.GAME_ID, TEvent.USER_ID),
                RedisAssert.ofEntries(
                        "all","2",
                        "all:D20200324","1",
                        "all:D20200331","1",
                        "all:M202003","2",
                        "all:Q202001","2",
                        "all:W202013","1",
                        "all:W202014","1",
                        "all:Y2020","2",
                        "attr:10","1",
                        "attr:10:D20200331","1",
                        "attr:10:M202003","1",
                        "attr:10:Q202001","1",
                        "attr:10:W202014","1",
                        "attr:10:Y2020","1",
                        "attr:20","1",
                        "attr:20:D20200324","1",
                        "attr:20:M202003","1",
                        "attr:20:Q202001","1",
                        "attr:20:W202013","1",
                        "attr:20:Y2020","1",
                        "rule:test.badge.rule:10","1",
                        "rule:test.badge.rule:10:D20200331","1",
                        "rule:test.badge.rule:10:M202003","1",
                        "rule:test.badge.rule:10:Q202001","1",
                        "rule:test.badge.rule:10:W202014","1",
                        "rule:test.badge.rule:10:Y2020","1",
                        "rule:test.badge.rule:20","1",
                        "rule:test.badge.rule:20:D20200324","1",
                        "rule:test.badge.rule:20:M202003","1",
                        "rule:test.badge.rule:20:Q202001","1",
                        "rule:test.badge.rule:20:W202013","1",
                        "rule:test.badge.rule:20:Y2020","1"
                ));
        String rid = rule.getId();
        RedisAssert.assertSorted(dbPool, ID.getGameUserBadgesLog(TEvent.GAME_ID, TEvent.USER_ID),
                RedisAssert.ofSortedEntries(
                        rid + ":10:" + e1.getTimestamp(), e5.getTimestamp(),
                        rid + ":20:" + e1.getTimestamp(), e5.getTimestamp()
                ));
    }

}
