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
import io.github.oasis.core.services.EngineDataReader;
import io.github.oasis.elements.badges.BadgeIDs;
import io.github.oasis.elements.badges.stats.BadgeStats;
import io.github.oasis.elements.badges.stats.to.GameRuleWiseBadgeLog;
import io.github.oasis.elements.badges.stats.to.GameRuleWiseBadgeLogRequest;
import io.github.oasis.elements.badges.stats.to.UserBadgeLog;
import io.github.oasis.elements.badges.stats.to.UserBadgeLogRequest;
import io.github.oasis.elements.badges.stats.to.UserBadgeRequest;
import io.github.oasis.elements.badges.stats.to.UserBadgeSummary;
import io.github.oasis.elements.badges.stats.to.UserBadgesProgressRequest;
import io.github.oasis.elements.badges.stats.to.UserBadgesProgressResponse;
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

        oasisRepository.addAttribute(TEvent.GAME_ID, new AttributeInfo(10, "Bronze", 100));
        oasisRepository.addAttribute(TEvent.GAME_ID, new AttributeInfo(20, "Silver", 50));
        oasisRepository.addAttribute(TEvent.GAME_ID, new AttributeInfo(30, "Gold", 20));
        oasisRepository.addAttribute(TEvent.GAME_ID, new AttributeInfo(40, "Platinum", 10));
    }

    @Test
    public void testEngineBadgePeriodicStreak() {
        Event e1 = TEvent.createKeyValue(U1, TSZ("2020-03-23 11:15", UTC), EVT_A, 75);
        Event e2 = TEvent.createKeyValue(U1, TSZ("2020-03-23 14:15", UTC), EVT_A, 63);
        Event e3 = TEvent.createKeyValue(U1, TSZ("2020-03-24 11:15", UTC), EVT_A, 57);
        Event e4 = TEvent.createKeyValue(U1, TSZ("2020-03-24 15:15", UTC), EVT_A, 88);
        Event e5 = TEvent.createKeyValue(U1, TSZ("2020-03-25 10:15", UTC), EVT_A, 26);
        Event e6 = TEvent.createKeyValue(U1, TSZ("2020-03-25 11:15", UTC), EVT_A, 96);
        Event e7 = TEvent.createKeyValue(U1, TSZ("2020-03-26 11:15", UTC), EVT_A, 91);
        Event e8 = TEvent.createKeyValue(U1, TSZ("2020-03-27 11:15", UTC), EVT_A, 80);
        Event e9 = TEvent.createKeyValue(U1, TSZ("2020-03-28 11:15", UTC), EVT_A, 2);
        Event e10 = TEvent.createKeyValue(U1, TSZ("2020-03-29 11:15", UTC), EVT_A, 95);
        Event e11 = TEvent.createKeyValue(U1, TSZ("2020-03-30 11:15", UTC), EVT_A, 88);
        Event e12 = TEvent.createKeyValue(U1, TSZ("2020-03-31 11:15", UTC), EVT_A, 89);
        Event e13 = TEvent.createKeyValue(U1, TSZ("2020-04-02 11:15", UTC), EVT_A, 95);
        Event e14 = TEvent.createKeyValue(U1, TSZ("2020-04-03 11:15", UTC), EVT_A, 96);

        GameDef gameDef = loadRulesFromResource("rules/badges-threshold.yml");

        List<AbstractRule> rules = engine.createGame(TEvent.GAME_ID).startGame(TEvent.GAME_ID, gameDef);
        addRulesToMetadata(TEvent.GAME_ID, rules, gameDef);

        engine.submitAll(e1, e2, e3, e4, e5, e6, e7, e8, e9, e10, e11, e12, e13, e14);
        awaitTerminated();

        RedisAssert.assertMap(dbPool, BadgeIDs.getGameUserBadgesSummary(TEvent.GAME_ID, U1),
                RedisAssert.ofEntries(
                        "all","3",
                        "all:D20200325","1",
                        "all:D20200327","1",
                        "all:D20200331","1",
                        "all:M202003","3",
                        "all:Q202001","3",
                        "all:W202013","2",
                        "all:W202014","1",
                        "all:Y2020","3",
                        "attr:10","2",
                        "attr:10:D20200325","1",
                        "attr:10:D20200331","1",
                        "attr:10:M202003","2",
                        "attr:10:Q202001","2",
                        "attr:10:W202013","1",
                        "attr:10:W202014","1",
                        "attr:10:Y2020","2",
                        "attr:20","1",
                        "attr:20:D20200327","1",
                        "attr:20:M202003","1",
                        "attr:20:Q202001","1",
                        "attr:20:W202013","1",
                        "attr:20:Y2020","1",
                        "rule:MULTIPLE_THRESHOLDS","3",
                        "rule:MULTIPLE_THRESHOLDS:10","2",
                        "rule:MULTIPLE_THRESHOLDS:10:D20200325","1",
                        "rule:MULTIPLE_THRESHOLDS:10:D20200331","1",
                        "rule:MULTIPLE_THRESHOLDS:10:M202003","2",
                        "rule:MULTIPLE_THRESHOLDS:10:Q202001","2",
                        "rule:MULTIPLE_THRESHOLDS:10:W202013","1",
                        "rule:MULTIPLE_THRESHOLDS:10:W202014","1",
                        "rule:MULTIPLE_THRESHOLDS:10:Y2020","2",
                        "rule:MULTIPLE_THRESHOLDS:20","1",
                        "rule:MULTIPLE_THRESHOLDS:20:D20200327","1",
                        "rule:MULTIPLE_THRESHOLDS:20:M202003","1",
                        "rule:MULTIPLE_THRESHOLDS:20:Q202001","1",
                        "rule:MULTIPLE_THRESHOLDS:20:W202013","1",
                        "rule:MULTIPLE_THRESHOLDS:20:Y2020","1"
                ));

        BadgeStats stats = new BadgeStats(new EngineDataReader(dbPool), metadataSupport);

        compareStatReqRes("stats/badges/progress-ps-req.json", UserBadgesProgressRequest.class,
                "stats/badges/progress-ps-res.json", UserBadgesProgressResponse.class,
                stats::getUserProgress);
    }

    @Test
    public void testEngineBadges() {
        Event e1 = TEvent.createKeyValue(U1, TSZ("2020-03-23 11:15", UTC), EVT_A, 75);
        Event e2 = TEvent.createKeyValue(U1, TSZ("2020-03-25 09:55", UTC), EVT_A, 63);
        Event e3 = TEvent.createKeyValue(U1, TSZ("2020-03-31 14:15", UTC), EVT_A, 57);
        Event e4 = TEvent.createKeyValue(U1, TSZ("2020-04-01 07:15", UTC), EVT_A, 88);
        Event e5 = TEvent.createKeyValue(U1, TSZ("2020-03-24 11:15", UTC), EVT_A, 76);
        Event e6 = TEvent.createKeyValue(U1, TSZ("2020-04-05 11:15", UTC), EVT_A, 26);
        Event e7 = TEvent.createKeyValue(U1, TSZ("2020-04-06 11:15", UTC), EVT_A, 66);

        GameDef gameDef = loadRulesFromResource("rules/badges-basic.yml");

        List<AbstractRule> rules = engine.createGame(TEvent.GAME_ID).startGame(TEvent.GAME_ID, gameDef);
        addRulesToMetadata(TEvent.GAME_ID, rules, gameDef);

        engine.submitAll(e1, e2, e3, e4, e5, e6, e7);
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

        BadgeStats stats = new BadgeStats(new EngineDataReader(dbPool), metadataSupport);

        compareStatReqRes("stats/badges/progress-req.json", UserBadgesProgressRequest.class,
                "stats/badges/progress-res.json", UserBadgesProgressResponse.class,
                stats::getUserProgress);

        compareStatReqRes("stats/badges/summary-attr-req.json", UserBadgeRequest.class,
                "stats/badges/summary-attr-res.json", UserBadgeSummary.class,
                stats::getBadgeSummary);

        compareStatReqRes("stats/badges/summary-rules-req.json", UserBadgeRequest.class,
                "stats/badges/summary-rules-res.json", UserBadgeSummary.class,
                stats::getBadgeSummary);

        compareStatReqRes("stats/badges/log-req.json", UserBadgeLogRequest.class,
                "stats/badges/log-res.json", UserBadgeLog.class,
                stats::getBadgeLog);

        compareStatReqRes("stats/badges/rulewise-log-offset-req.json", GameRuleWiseBadgeLogRequest.class,
                "stats/badges/rulewise-log-offset-res.json", GameRuleWiseBadgeLog.class,
                stats::getRuleWiseBadgeLog);
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
