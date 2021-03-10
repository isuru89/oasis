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

import io.github.oasis.core.Event;
import io.github.oasis.core.elements.AbstractRule;
import io.github.oasis.core.elements.AttributeInfo;
import io.github.oasis.core.elements.GameDef;
import io.github.oasis.core.external.DbContext;
import io.github.oasis.elements.badges.BadgeIDs;
import io.github.oasis.elements.badges.stats.BadgeStats;
import io.github.oasis.elements.badges.stats.to.*;
import io.github.oasis.engine.element.points.PointIDs;
import io.github.oasis.engine.model.TEvent;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

/**
 * @author Isuru Weerarathna
 */
public class EngineBadgesTest extends OasisEngineTest {

    private static final String UTC = "UTC";

    @Override
    public void setupDbBefore(DbContext db) throws IOException {
        super.setupDbBefore(db);

        metadataSupport.addAttribute(TEvent.GAME_ID, new AttributeInfo(10, "Bronze", 100));
        metadataSupport.addAttribute(TEvent.GAME_ID, new AttributeInfo(20, "Silver", 50));
        metadataSupport.addAttribute(TEvent.GAME_ID, new AttributeInfo(30, "Gold", 20));
        metadataSupport.addAttribute(TEvent.GAME_ID, new AttributeInfo(40, "Platinum", 10));
    }

    @Test
    public void testEngineBadges() {
        Event e1 = TEvent.createKeyValue(U1, TSZ("2020-03-23 11:15", UTC), EVT_A, 75);
        Event e2 = TEvent.createKeyValue(U1, TSZ("2020-03-25 09:55", UTC), EVT_A, 63);
        Event e3 = TEvent.createKeyValue(U1, TSZ("2020-03-31 14:15", UTC), EVT_A, 57);
        Event e4 = TEvent.createKeyValue(U1, TSZ("2020-04-01 07:15", UTC), EVT_A, 88);
        Event e5 = TEvent.createKeyValue(U1, TSZ("2020-03-24 11:15", UTC), EVT_A, 76);
        Event e6 = TEvent.createKeyValue(U1, TSZ("2020-04-05 11:15", UTC), EVT_A, 26);

        GameDef gameDef = loadRulesFromResource("rules/badges-basic.yml");

        List<AbstractRule> rules = engine.createGame(TEvent.GAME_ID).startGame(TEvent.GAME_ID, gameDef);
        addRulesToMetadata(TEvent.GAME_ID, rules);

        engine.submitAll(e1, e2, e3, e4, e5, e6);
        awaitTerminated();

        String rid = findRuleByName(rules, "test.badge.rule").getId();
        RedisAssert.assertMap(dbPool, BadgeIDs.getGameUserBadgesSummary(TEvent.GAME_ID, U1),
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
                        "rule:BDG00001","2",
                        "rule:BDG00001:10","1",
                        "rule:BDG00001:10:D20200331","1",
                        "rule:BDG00001:10:M202003","1",
                        "rule:BDG00001:10:Q202001","1",
                        "rule:BDG00001:10:W202014","1",
                        "rule:BDG00001:10:Y2020","1",
                        "rule:BDG00001:20","1",
                        "rule:BDG00001:20:D20200324","1",
                        "rule:BDG00001:20:M202003","1",
                        "rule:BDG00001:20:Q202001","1",
                        "rule:BDG00001:20:W202013","1",
                        "rule:BDG00001:20:Y2020","1"
                ));
        RedisAssert.assertSorted(dbPool, BadgeIDs.getGameUserBadgesLog(TEvent.GAME_ID, U1),
                RedisAssert.ofSortedEntries(
                        rid + ":10:" + e1.getTimestamp(), e5.getTimestamp(),
                        rid + ":20:" + e1.getTimestamp(), e5.getTimestamp()
                ));

        BadgeStats stats = new BadgeStats(dbPool, metadataSupport);

        compareStatReqRes("stats/badges/summary-attr-req.json", UserBadgeRequest.class,
                "stats/badges/summary-attr-res.json", UserBadgeSummary.class,
                req ->(UserBadgeSummary) stats.getBadgeSummary(req));

        compareStatReqRes("stats/badges/summary-rules-req.json", UserBadgeRequest.class,
                "stats/badges/summary-rules-res.json", UserBadgeSummary.class,
                req ->(UserBadgeSummary) stats.getBadgeSummary(req));

        compareStatReqRes("stats/badges/log-req.json", UserBadgeLogRequest.class,
                "stats/badges/log-res.json", UserBadgeLog.class,
                req ->(UserBadgeLog) stats.getBadgeLog(req));

        compareStatReqRes("stats/badges/rulewise-log-offset-req.json", GameRuleWiseBadgeLogRequest.class,
                "stats/badges/rulewise-log-offset-res.json", GameRuleWiseBadgeLog.class,
                req ->(GameRuleWiseBadgeLog) stats.getRuleWiseBadgeLog(req));
    }

    @Test
    public void testEngineBadgesWithPoints() {
        Event e1 = TEvent.createKeyValue(TS("2020-03-23 11:15"), EVT_A, 75);
        Event e2 = TEvent.createKeyValue(TS("2020-03-25 09:55"), EVT_A, 63);
        Event e3 = TEvent.createKeyValue(TS("2020-03-31 14:15"), EVT_A, 57);
        Event e4 = TEvent.createKeyValue(TS("2020-04-01 09:15"), EVT_A, 88);
        Event e5 = TEvent.createKeyValue(TS("2020-03-24 11:15"), EVT_A, 76);
        Event e6 = TEvent.createKeyValue(TS("2020-04-05 11:15"), EVT_A, 26);

        GameDef gameDef = loadRulesFromResource("rules/badges-points.yml");

        List<AbstractRule> rules = engine.createGame(TEvent.GAME_ID).startGame(TEvent.GAME_ID, gameDef);

        engine.submitAll(e1, e2, e3, e4, e5, e6);
        awaitTerminated();

        String rid = findRuleByName(rules, "test.badge.points.rule").getId();
        RedisAssert.assertMap(dbPool, BadgeIDs.getGameUserBadgesSummary(TEvent.GAME_ID, TEvent.USER_ID),
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
                        "rule:BDG00002","2",
                        "rule:BDG00002:10","1",
                        "rule:BDG00002:10:D20200331","1",
                        "rule:BDG00002:10:M202003","1",
                        "rule:BDG00002:10:Q202001","1",
                        "rule:BDG00002:10:W202014","1",
                        "rule:BDG00002:10:Y2020","1",
                        "rule:BDG00002:20","1",
                        "rule:BDG00002:20:D20200324","1",
                        "rule:BDG00002:20:M202003","1",
                        "rule:BDG00002:20:Q202001","1",
                        "rule:BDG00002:20:W202013","1",
                        "rule:BDG00002:20:Y2020","1"
                ));
        RedisAssert.assertSorted(dbPool, BadgeIDs.getGameUserBadgesLog(TEvent.GAME_ID, TEvent.USER_ID),
                RedisAssert.ofSortedEntries(
                        rid + ":10:" + e1.getTimestamp(), e5.getTimestamp(),
                        rid + ":20:" + e1.getTimestamp(), e5.getTimestamp()
                ));

        // assert points
        String pid = "star.points";
        long tid = e1.getTeam();
        RedisAssert.assertMap(dbPool,
                PointIDs.getGameUserPointsSummary(TEvent.GAME_ID, TEvent.USER_ID),
                RedisAssert.ofEntries("all", "150",
                        "source:" + e1.getSource(), "150",
                        "all:Y2020", "150",
                        "all:Q202001", "50",
                        "all:Q202002", "100",
                        "all:M202003", "50",
                        "all:M202004", "100",
                        "all:W202014", "150",
                        "all:D20200331", "50",
                        "all:D20200401", "100",
                        "rule:"+pid, "150",
                        "rule:"+pid+":Y2020", "150",
                        "rule:"+pid+":Q202001", "50",
                        "rule:"+pid+":Q202002", "100",
                        "rule:"+pid+":M202003", "50",
                        "rule:"+pid+":M202004", "100",
                        "rule:"+pid+":W202014", "150",
                        "rule:"+pid+":D20200331", "50",
                        "rule:"+pid+":D20200401", "100",
                        "team:"+tid, "150",
                        "team:"+tid+":Y2020", "150",
                        "team:"+tid+":Q202001", "50",
                        "team:"+tid+":Q202002", "100",
                        "team:"+tid+":M202003", "50",
                        "team:"+tid+":M202004", "100",
                        "team:"+tid+":W202014", "150",
                        "team:"+tid+":D20200331", "50",
                        "team:"+tid+":D20200401", "100"
                ));
    }

    @Test
    public void testEngineBadgesWithPointsRemoval() {
        Event e1 = TEvent.createKeyValue(TS("2020-03-23 11:15"), EVT_A, 75);
        Event e2 = TEvent.createKeyValue(TS("2020-03-25 09:55"), EVT_A, 63);
        Event e3 = TEvent.createKeyValue(TS("2020-03-31 14:15"), EVT_A, 57);
        Event e4 = TEvent.createKeyValue(TS("2020-04-01 09:15"), EVT_A, 88);
        Event e5 = TEvent.createKeyValue(TS("2020-03-24 11:15"), EVT_A, 26);

        GameDef gameDef = loadRulesFromResource("rules/badges-removal-points.yml");

        List<AbstractRule> rules = engine.createGame(TEvent.GAME_ID).startGame(TEvent.GAME_ID, gameDef);

        engine.submitAll(e1, e2, e3, e4, e5);
        awaitTerminated();

        String rid = findRuleByName(rules, "test.badge.remove.points.rule").getId();
        RedisAssert.assertMap(dbPool, BadgeIDs.getGameUserBadgesSummary(TEvent.GAME_ID, TEvent.USER_ID),
                RedisAssert.ofEntries(
                        "all","1",
                        "all:D20200323","-1",
                        "all:D20200324","1",
                        "all:D20200331","1",
                        "all:M202003","1",
                        "all:Q202001","1",
                        "all:W202013","0",
                        "all:W202014","1",
                        "all:Y2020","1",
                        "attr:10","1",
                        "attr:10:D20200323","-1",
                        "attr:10:D20200324","1",
                        "attr:10:D20200331","1",
                        "attr:10:M202003","1",
                        "attr:10:Q202001","1",
                        "attr:10:W202013","0",
                        "attr:10:W202014","1",
                        "attr:10:Y2020","1",
                        "rule:BDG00003","1",
                        "rule:BDG00003:10","1",
                        "rule:BDG00003:10:D20200323","-1",
                        "rule:BDG00003:10:D20200324","1",
                        "rule:BDG00003:10:D20200331","1",
                        "rule:BDG00003:10:M202003","1",
                        "rule:BDG00003:10:Q202001","1",
                        "rule:BDG00003:10:W202013","0",
                        "rule:BDG00003:10:W202014","1",
                        "rule:BDG00003:10:Y2020","1"
                ));
        RedisAssert.assertSorted(dbPool, BadgeIDs.getGameUserBadgesLog(TEvent.GAME_ID, TEvent.USER_ID),
                RedisAssert.ofSortedEntries(
                        rid + ":10:" + e2.getTimestamp(), e5.getTimestamp()
                ));

        // assert points
        String pid = "star.points";
        long tid = e1.getTeam();
        RedisAssert.assertMap(dbPool,
                PointIDs.getGameUserPointsSummary(TEvent.GAME_ID, TEvent.USER_ID),
                RedisAssert.ofEntries("all", "50",
                        "source:" + e1.getSource(), "50",
                        "all:Y2020", "50",
                        "all:Q202001", "0",
                        "all:Q202002", "50",
                        "all:M202003", "0",
                        "all:M202004", "50",
                        "all:W202014", "50",
                        "all:D20200331", "0",
                        "all:D20200401", "50",
                        "rule:"+pid, "50",
                        "rule:"+pid+":Y2020", "50",
                        "rule:"+pid+":Q202001", "0",
                        "rule:"+pid+":Q202002", "50",
                        "rule:"+pid+":M202003", "0",
                        "rule:"+pid+":M202004", "50",
                        "rule:"+pid+":W202014", "50",
                        "rule:"+pid+":D20200331", "0",
                        "rule:"+pid+":D20200401", "50",
                        "team:"+tid, "50",
                        "team:"+tid+":Y2020", "50",
                        "team:"+tid+":Q202001", "0",
                        "team:"+tid+":Q202002", "50",
                        "team:"+tid+":M202003", "0",
                        "team:"+tid+":M202004", "50",
                        "team:"+tid+":W202014", "50",
                        "team:"+tid+":D20200331", "0",
                        "team:"+tid+":D20200401", "50"
                ));
    }

}
