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
import io.github.oasis.core.ID;
import io.github.oasis.core.elements.matchers.SingleEventTypeMatcher;
import io.github.oasis.core.events.BasePointEvent;
import io.github.oasis.core.external.messages.GameCommand;
import io.github.oasis.elements.milestones.MilestoneRule;
import io.github.oasis.engine.actors.cmds.RuleAddedMessage;
import io.github.oasis.engine.element.points.PointRule;
import io.github.oasis.engine.model.TEvent;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Set;

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
        Event e7 = TEvent.createKeyValue(130, EVT_A, 100);
        Event e8 = TEvent.createKeyValue(135, EVT_B, 120);

        MilestoneRule rule = new MilestoneRule("test.milestone.rule");
        rule.setEventTypeMatcher(new SingleEventTypeMatcher(EVT_A));
        rule.setValueExtractor((event, rule1, ctx) -> BigDecimal.valueOf((long)event.getFieldValue("value")));
        rule.setLevels(Arrays.asList(new MilestoneRule.Level(1, BigDecimal.valueOf(100)),
                new MilestoneRule.Level(2, BigDecimal.valueOf(200)),
                new MilestoneRule.Level(3, BigDecimal.valueOf(300)),
                new MilestoneRule.Level(4, BigDecimal.valueOf(500))));
        rule.setFlags(Set.of(MilestoneRule.SKIP_NEGATIVE_VALUES));

        engine.submit(GameCommand.create(TEvent.GAME_ID, GameCommand.GameLifecycle.CREATE));
        engine.submit(GameCommand.create(TEvent.GAME_ID, GameCommand.GameLifecycle.START));
        engine.submit(RuleAddedMessage.create(TEvent.GAME_ID, rule));
        engine.submitAll(e1, e2, e3, e4, e5, e6, e7, e8);
        awaitTerminated();

        String rid = rule.getId();
        RedisAssert.assertMap(dbPool, ID.getGameUserMilestonesSummary(TEvent.GAME_ID, TEvent.USER_ID),
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

        MilestoneRule rule = new MilestoneRule("test.milestone.rule");
        rule.setEventTypeMatcher(new SingleEventTypeMatcher(EVT_A));
        rule.setValueExtractor((event, rule1, ctx) -> BigDecimal.valueOf((long)event.getFieldValue("value")));
        rule.setLevels(Arrays.asList(new MilestoneRule.Level(1, BigDecimal.valueOf(100)),
                new MilestoneRule.Level(2, BigDecimal.valueOf(200))));
        rule.setFlags(Set.of(MilestoneRule.TRACK_PENALTIES));

        engine.submit(GameCommand.create(TEvent.GAME_ID, GameCommand.GameLifecycle.CREATE));
        engine.submit(GameCommand.create(TEvent.GAME_ID, GameCommand.GameLifecycle.START));
        engine.submit(RuleAddedMessage.create(TEvent.GAME_ID, rule));
        engine.submitAll(e1, e2, e3, e4, e5, e6, e7, e8);
        awaitTerminated();

        String rid = rule.getId();
        RedisAssert.assertMap(dbPool, ID.getGameUserMilestonesSummary(TEvent.GAME_ID, TEvent.USER_ID),
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

        PointRule pointRule = new PointRule("test.point.rule");
        pointRule.setPointId("star.points");
        pointRule.setEventTypeMatcher(new SingleEventTypeMatcher(EVT_A));
        pointRule.setAmountExpression((event, rule1) -> BigDecimal.valueOf((long)event.getFieldValue("value") - 50));
        pointRule.setCriteria((event, rule1, ctx) -> (long) event.getFieldValue("value") >= 50);

        MilestoneRule rule = new MilestoneRule("test.milestone.point.rule");
        rule.setEventTypeMatcher(new SingleEventTypeMatcher("star.points"));
        rule.setValueExtractor((event, rule1, ctx) -> {
            if (event instanceof BasePointEvent) {
                return ((BasePointEvent) event).getPoints();
            }
            return BigDecimal.ZERO;
        });
        rule.setLevels(Arrays.asList(new MilestoneRule.Level(1, BigDecimal.valueOf(100)),
                new MilestoneRule.Level(2, BigDecimal.valueOf(200))));
        rule.setFlags(Set.of(MilestoneRule.TRACK_PENALTIES));

        engine.submit(GameCommand.create(TEvent.GAME_ID, GameCommand.GameLifecycle.CREATE));
        engine.submit(GameCommand.create(TEvent.GAME_ID, GameCommand.GameLifecycle.START));
        engine.submit(RuleAddedMessage.create(TEvent.GAME_ID, pointRule));
        engine.submit(RuleAddedMessage.create(TEvent.GAME_ID, rule));
        engine.submitAll(e1, e2, e3, e4, e5, e6, e7, e8);
        awaitTerminated();

        String rid = rule.getId();
        RedisAssert.assertMap(dbPool, ID.getGameUserMilestonesSummary(TEvent.GAME_ID, TEvent.USER_ID),
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

        MilestoneRule rule = new MilestoneRule("test.milestone.rule");
        rule.setEventTypeMatcher(new SingleEventTypeMatcher(EVT_A));
        rule.setValueExtractor((event, rule1, ctx) -> BigDecimal.ONE);
        rule.setLevels(Arrays.asList(new MilestoneRule.Level(1, BigDecimal.valueOf(5)),
                new MilestoneRule.Level(2, BigDecimal.valueOf(10))));

        engine.submit(GameCommand.create(TEvent.GAME_ID, GameCommand.GameLifecycle.CREATE));
        engine.submit(GameCommand.create(TEvent.GAME_ID, GameCommand.GameLifecycle.START));
        engine.submit(RuleAddedMessage.create(TEvent.GAME_ID, rule));
        engine.submitAll(e1, e2, e3, e4, e5, e6, e7, e8);
        awaitTerminated();

        String rid = rule.getId();
        RedisAssert.assertMap(dbPool, ID.getGameUserMilestonesSummary(TEvent.GAME_ID, TEvent.USER_ID),
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

}
