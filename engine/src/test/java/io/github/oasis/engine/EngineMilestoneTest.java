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
import io.github.oasis.core.elements.GameDef;
import io.github.oasis.core.external.messages.GameCommand;
import io.github.oasis.elements.milestones.MilestoneIDs;
import io.github.oasis.elements.milestones.stats.MilestoneStats;
import io.github.oasis.elements.milestones.stats.to.GameMilestoneRequest;
import io.github.oasis.elements.milestones.stats.to.GameMilestoneResponse;
import io.github.oasis.elements.milestones.stats.to.UserMilestoneRequest;
import io.github.oasis.elements.milestones.stats.to.UserMilestoneSummary;
import io.github.oasis.engine.model.TEvent;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * @author Isuru Weerarathna
 */
public class EngineMilestoneTest extends OasisEngineTest {

    @Test
    public void testMilestones() {
        Event e1 = TEvent.createKeyValue(100, EVT_A, 87);
        Event e2 = TEvent.createKeyValue(105, EVT_A, 53);
        Event e3 = TEvent.createKeyValue(110, EVT_A, 34);
        Event e4 = TEvent.createKeyValue(115, EVT_A, 11);
        Event e5 = TEvent.createKeyValue(120, EVT_A, 84);
        Event e6 = TEvent.createKeyValue(125, EVT_A, 92);
        Event e7 = TEvent.createKeyValue(130, EVT_A, 100, "9b1132ea-0aaa-4724-bf2b-bc128ca57fea");
        Event e8 = TEvent.createKeyValue(135, EVT_B, 120);

        GameDef gameDef = loadRulesFromResource("rules/milestone-basic.yml");

        engine.submit(GameCommand.create(TEvent.GAME_ID, GameCommand.GameLifecycle.CREATE));
        engine.submit(GameCommand.create(TEvent.GAME_ID, GameCommand.GameLifecycle.START));
        submitRules(engine, TEvent.GAME_ID, gameDef);
        engine.submitAll(e1, e2, e3, e4, e5, e6, e7, e8);
        awaitTerminated();

        String rid = "MILE000001";
        RedisAssert.assertMap(dbPool, MilestoneIDs.getGameUserMilestonesSummary(TEvent.GAME_ID, TEvent.USER_ID),
                RedisAssert.ofEntries(rid, "461",
                        rid + ":levellastupdated", String.valueOf(e6.getTimestamp()),
                        rid + ":lastupdated", String.valueOf(e7.getTimestamp()),
                        rid + ":lastevent", String.valueOf(e7.getExternalId()),
                        rid + ":changedvalue", "361.0",
                        rid + ":currentlevel", "3",
                        rid + ":completed", "0",
                        rid + ":nextlevel", "4",
                        rid + ":nextlevelvalue", "500"
                ));

        MilestoneStats stats = new MilestoneStats(dbPool, metadataSupport);

        compareStatReqRes("stats/milestones/user-req.json", UserMilestoneRequest.class,
                "stats/milestones/user-res.json", UserMilestoneSummary.class,
                req -> (UserMilestoneSummary) stats.getUserMilestoneSummary(req));

        compareStatReqRes("stats/milestones/game-summary-req.json", GameMilestoneRequest.class,
                "stats/milestones/game-summary-res.json", GameMilestoneResponse.class,
                req -> (GameMilestoneResponse)stats.getGameMilestoneSummary(req));
    }

    @Test
    public void testMilestoneMultiUsers() {
        Event e1 = TEvent.createKeyValue(U1,100, EVT_A, 87);
        Event e2 = TEvent.createKeyValue(U4, 105, EVT_A, 53);
        Event e3 = TEvent.createKeyValue(U3,110, EVT_A, 34);
        Event e4 = TEvent.createKeyValue(U1,115, EVT_A, 11);
        Event e5 = TEvent.createKeyValue(U2, 120, EVT_A, 84);
        Event e6 = TEvent.createKeyValue(U5,125, EVT_A, 92);
        Event e7 = TEvent.createKeyValue(U3, 130, EVT_A, 100);
        Event e8 = TEvent.createKeyValue(U4,135, EVT_A, 120);

        GameDef gameDef = loadRulesFromResource("rules/milestone-basic.yml");

        engine.submit(GameCommand.create(TEvent.GAME_ID, GameCommand.GameLifecycle.CREATE));
        engine.submit(GameCommand.create(TEvent.GAME_ID, GameCommand.GameLifecycle.START));
        submitRules(engine, TEvent.GAME_ID, gameDef);
        engine.submitAll(e1, e2, e3, e4, e5, e6, e7, e8);
        awaitTerminated();

        MilestoneStats stats = new MilestoneStats(dbPool, metadataSupport);

        compareStatReqRes("stats/milestones/game-users-req.json", GameMilestoneRequest.class,
                "stats/milestones/game-users-res.json", GameMilestoneResponse.class,
                req -> (GameMilestoneResponse) stats.getGameMilestoneSummary(req));
    }

    @Test
    public void testMilestonesPenalties() {
        Event e1 = TEvent.createKeyValue(100, EVT_A, 87);
        Event e2 = TEvent.createKeyValue(105, EVT_A, 53);
        Event e3 = TEvent.createKeyValue(110, EVT_A, 34);
        Event e4 = TEvent.createKeyValue(115, EVT_A, 11);
        Event e5 = TEvent.createKeyValue(120, EVT_A, -84);
        Event e6 = TEvent.createKeyValue(125, EVT_A, 92);
        Event e7 = TEvent.createKeyValue(130, EVT_A, 100);
        Event e8 = TEvent.createKeyValue(135, EVT_B, 120);

        GameDef gameDef = loadRulesFromResource("rules/milestones-penalties.yml");

        engine.submit(GameCommand.create(TEvent.GAME_ID, GameCommand.GameLifecycle.CREATE));
        engine.submit(GameCommand.create(TEvent.GAME_ID, GameCommand.GameLifecycle.START));
        List<AbstractRule> rules = submitRules(engine, TEvent.GAME_ID, gameDef);
        engine.submitAll(e1, e2, e3, e4, e5, e6, e7, e8);
        awaitTerminated();

        String rid = findRuleByName(rules, "Milestone-with-Penalties").getId();
        RedisAssert.assertMap(dbPool, MilestoneIDs.getGameUserMilestonesSummary(TEvent.GAME_ID, TEvent.USER_ID),
                RedisAssert.ofEntries(rid, "293",
                        rid + ":levellastupdated", String.valueOf(e7.getTimestamp()),
                        rid + ":lastupdated", String.valueOf(e7.getTimestamp()),
                        rid + ":lastevent", String.valueOf(e7.getExternalId()),
                        rid + ":penalties", "-84",
                        rid + ":changedvalue", "293.0",
                        rid + ":currentlevel", "2",
                        rid + ":completed", "1",
                        rid + ":nextlevel", "2",
                        rid + ":nextlevelvalue", "200"
                ));
    }

    @Test
    public void testMilestonesFromPoints() {
        Event e1 = TEvent.createKeyValue(100, EVT_A, 87);
        Event e2 = TEvent.createKeyValue(105, EVT_A, 53);
        Event e3 = TEvent.createKeyValue(110, EVT_A, 34);
        Event e4 = TEvent.createKeyValue(115, EVT_A, 11);
        Event e5 = TEvent.createKeyValue(120, EVT_A, 84);
        Event e6 = TEvent.createKeyValue(125, EVT_A, 92);
        Event e7 = TEvent.createKeyValue(130, EVT_A, 100);
        Event e8 = TEvent.createKeyValue(135, EVT_B, 120);

        GameDef gameDef = loadRulesFromResource("rules/milestones-from-points.yml");

        engine.submit(GameCommand.create(TEvent.GAME_ID, GameCommand.GameLifecycle.CREATE));
        engine.submit(GameCommand.create(TEvent.GAME_ID, GameCommand.GameLifecycle.START));
        List<AbstractRule> rules = submitRules(engine, TEvent.GAME_ID, gameDef);
        engine.submitAll(e1, e2, e3, e4, e5, e6, e7, e8);
        awaitTerminated();

        String rid = findRuleByName(rules, "Milestone-from-Points").getId();
        RedisAssert.assertMap(dbPool, MilestoneIDs.getGameUserMilestonesSummary(TEvent.GAME_ID, TEvent.USER_ID),
                RedisAssert.ofEntries(rid, "166",
                        rid + ":levellastupdated", String.valueOf(e6.getTimestamp()),
                        rid + ":lastupdated", String.valueOf(e7.getTimestamp()),
                        rid + ":lastevent", RedisAssert.ANY_VALUE,
                        rid + ":changedvalue", "116.0",
                        rid + ":currentlevel", "1",
                        rid + ":completed", "0",
                        rid + ":nextlevel", "2",
                        rid + ":nextlevelvalue", "200"
                ));
    }

    @Test
    public void testMilestonesByCount() {
        Event e1 = TEvent.createKeyValue(100, EVT_A, 87);
        Event e2 = TEvent.createKeyValue(105, EVT_A, 53);
        Event e3 = TEvent.createKeyValue(110, EVT_A, 34);
        Event e4 = TEvent.createKeyValue(115, EVT_A, 11);
        Event e5 = TEvent.createKeyValue(120, EVT_A, 84);
        Event e6 = TEvent.createKeyValue(125, EVT_A, 92);
        Event e7 = TEvent.createKeyValue(130, EVT_A, 100);
        Event e8 = TEvent.createKeyValue(135, EVT_B, 120);

        GameDef gameDef = loadRulesFromResource("rules/milestones-by-count.yml");

        engine.submit(GameCommand.create(TEvent.GAME_ID, GameCommand.GameLifecycle.CREATE));
        engine.submit(GameCommand.create(TEvent.GAME_ID, GameCommand.GameLifecycle.START));
        List<AbstractRule> rules = submitRules(engine, TEvent.GAME_ID, gameDef);
        engine.submitAll(e1, e2, e3, e4, e5, e6, e7, e8);
        awaitTerminated();

        String rid = findRuleByName(rules, "Milestone-with-Event-Count").getId();
        RedisAssert.assertMap(dbPool, MilestoneIDs.getGameUserMilestonesSummary(TEvent.GAME_ID, TEvent.USER_ID),
                RedisAssert.ofEntries(rid, "7",
                        rid + ":levellastupdated", String.valueOf(e5.getTimestamp()),
                        rid + ":lastupdated", String.valueOf(e7.getTimestamp()),
                        rid + ":lastevent", String.valueOf(e7.getExternalId()),
                        rid + ":changedvalue", "5.0",
                        rid + ":currentlevel", "1",
                        rid + ":completed", "0",
                        rid + ":nextlevel", "2",
                        rid + ":nextlevelvalue", "10"
                ));
    }

}
